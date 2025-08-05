package com.showcase.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.listener.ContainerOpenWatcher;
import com.showcase.utils.*;
import com.showcase.utils.ShareConstants;
import com.showcase.utils.countdown.CountdownBossBar;
import com.showcase.utils.countdown.CountdownBossBarManager;
import com.showcase.utils.stats.StatUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.showcase.command.ShareCommandUtils.*;
import static com.showcase.command.ShowcaseCommand.*;
import static com.showcase.utils.permissions.PermissionChecker.isOp;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static com.showcase.command.ShowcaseManager.ShareType.*;

public class ShareCommandExecutor {
    @FunctionalInterface
    private interface ShareExecutor {
        int execute(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sourcePlayer, String description, Collection<ServerPlayerEntity> receiver, Integer durationSeconds);
    }

    @FunctionalInterface
    private interface SimpleShareExecutor {
        int execute(CommandContext<ServerCommandSource> ctx, String description, Collection<ServerPlayerEntity> receivers, Integer durationSeconds);
    }

    public static IntegerArgumentType shareDurationArgument() {
        return IntegerArgumentType.integer(
                ModConfigManager.getShareLinkMinExpiry(),
                ModConfigManager.getShareLinkDefaultExpiry()
        );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createItemShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.ITEM, withSource, ShareCommandExecutor::shareItem);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createStatsShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.STATS, withSource, ShareCommandExecutor::shareStats);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createInventoryShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.INVENTORY, withSource, ShareCommandExecutor::shareInventory);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createHotbarShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.HOTBAR, withSource, ShareCommandExecutor::shareHotbar);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createEnderChestShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.ENDER_CHEST, withSource, ShareCommandExecutor::shareEnderChest);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createContainerShareCommand() {
        return createSimpleShareCommand(ShareConstants.CONTAINER, ShareCommandExecutor::shareContainer);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> shareMerchantShareCommand() {
        return createSimpleShareCommand(ShareConstants.MERCHANT, ShareCommandExecutor::shareMerchant);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createCancelCommand() {
        return literal(ShareConstants.CANCEL)
                .then(argument("id", string())
                        .suggests((ctx, builder) -> {
                            ServerPlayerEntity player = getSenderPlayer(ctx);
                            if (player != null) {
                                ShowcaseManager.getUnmodifiableActiveShares().entrySet().stream()
                                        .filter(e -> e.getValue().getOwnerUuid().equals(player.getUuid()))
                                        .map(Map.Entry::getKey)
                                        .forEach(builder::suggest);
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity player = getSenderPlayer(ctx);
                            String id = StringArgumentType.getString(ctx, "id");
                            return cancelShare(player, id);
                        }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createShareCommand(String commandName, boolean withSource, ShareExecutor executor) {
        var command = literal(commandName)
                .executes(ctx -> executor.execute(ctx, null, null, null, null));

        if (withSource) {
            command.then(argument(SOURCE_ARG, EntityArgumentType.player())
                    .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), null, null, null))
                    .then(argument(RECEIVERS_ARG, EntityArgumentType.players())
                            .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), null, getReceivers(ctx), null))
                            .then(argument(DURATION_ARG, IntegerArgumentType.integer())
                                    .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), null, getReceivers(ctx), getValidatedDuration(ctx)))
                                    .then(argument(DESCRIPTION_ARG, greedyString())
                                            .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), getDescription(ctx), getReceivers(ctx), getValidatedDuration(ctx)))
                                    )
                            )
                    )
            );
        } else {
            command.then(argument(RECEIVERS_ARG, EntityArgumentType.players())
                    .executes(ctx -> executor.execute(ctx, null, null, getReceivers(ctx), null))
                    .then(argument(DURATION_ARG, shareDurationArgument())
                            .executes(ctx -> executor.execute(ctx, null, null, getReceivers(ctx), getValidatedDuration(ctx)))
                            .then(argument(DESCRIPTION_ARG, greedyString())
                                    .executes(ctx -> executor.execute(ctx, null, getDescription(ctx), getReceivers(ctx), getValidatedDuration(ctx)))
                            )
                    )
            );
        }

        return command;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createSimpleShareCommand(String commandName, SimpleShareExecutor executor) {
        var command = literal(commandName)
                .executes(ctx -> executor.execute(ctx, null, null, null));

        command.then(argument(RECEIVERS_ARG, EntityArgumentType.players())
                .executes(ctx -> executor.execute(ctx, null, getReceivers(ctx), null))
                .then(argument(DURATION_ARG, shareDurationArgument())
                        .executes(ctx -> executor.execute(ctx, null, getReceivers(ctx), getValidatedDuration(ctx)))
                        .then(argument(DESCRIPTION_ARG, greedyString())
                                .executes(ctx -> executor.execute(ctx, getDescription(ctx), getReceivers(ctx), getValidatedDuration(ctx)))
                        )
                )
        );

        return command;
    }

    private static int executeShare(
            CommandContext<ServerCommandSource> ctx,
            ShowcaseManager.ShareType type,
            @Nullable Function<ServerPlayerEntity, ItemStack> stackSupplier,
            @Nullable String emptyMessageKey,
            QuadFunction<ServerPlayerEntity, @Nullable ItemStack, Integer, Collection<ServerPlayerEntity>, String> shareCreator,
            Text defaultDisplayName,
            String description,
            Collection<ServerPlayerEntity> receivers,
            Integer duration
    ) {
        try {
            ServerPlayerEntity sender = getSenderPlayer(ctx);
            if (sender == null) return 0;
            if (ShowcaseManager.isOnCooldown(sender, type)) return 0;

            // Check sub-permissions for optional parameters
            if (receivers != null && !ShareCommandUtils.canSpecifyReceivers(sender, type)) {
                sender.sendMessage(TextUtils.error(Text.translatable("showcase.command.no_permission.receivers")));
                return 0;
            }
            
            if (duration != null && !ShareCommandUtils.canSpecifyDuration(sender, type)) {
                sender.sendMessage(TextUtils.error(Text.translatable("showcase.command.no_permission.duration")));
                return 0;
            }
            
            if (description != null && !description.isEmpty() && !ShareCommandUtils.canSpecifyDescription(sender, type)) {
                sender.sendMessage(TextUtils.error(Text.translatable("showcase.command.no_permission.description")));
                return 0;
            }

            ServerPlayerEntity source = getSourceOrSender(ctx);
            ItemStack stack = ItemStack.EMPTY;

            if (stackSupplier != null) {
                stack = stackSupplier.apply(source).copy();
                if (stack.isEmpty()) {
                    sender.sendMessage(TextUtils.error(Text.translatable(emptyMessageKey)));
                    return 0;
                }
            }

            String shareId = shareCreator.apply(source, stackSupplier != null ? stack : null, duration, receivers);
            Text displayName = (stackSupplier != null ? StackUtils.getDisplayName(stack) : defaultDisplayName);

            ShareCommandUtils.sendShareMessage(sender, source, receivers, description, type, displayName, duration, shareId);
            ShowcaseManager.setCooldown(sender, type);

            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException e) {
            handleError(ctx, e);
            return -1;
        }
    }

    public static int shareItem(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sourcePlayer, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(
                ctx,
                ITEM,
                player -> player.getEquippedStack(EquipmentSlot.MAINHAND),
                "showcase.message.no_item",
                ShowcaseManager::createItemShare,
                Text.empty(),
                description, receivers, duration
        );
    }

    public static int shareStats(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sourcePlayer, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(
                ctx,
                STATS,
                StatUtils::createStatsBook,
                "showcase.message.no_stats",
                ShowcaseManager::createStatsShare,
                Text.empty(),
                description, receivers, duration
        );
    }

    public static int shareInventory(CommandContext<ServerCommandSource> ctx,
                                     ServerPlayerEntity sourcePlayer, String description,
                                     Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(
                ctx,
                INVENTORY,
                null, // 不需要 stackSupplier
                null, // 不需要 emptyMessageKey
                (player, unused, dur, recv) -> ShowcaseManager.createInventoryShare(player, dur, recv),
                TextUtils.INVENTORY,
                description, receivers, duration
        );
    }

    public static int shareHotbar(CommandContext<ServerCommandSource> ctx,
                                  ServerPlayerEntity sourcePlayer, String description,
                                  Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(
                ctx,
                HOTBAR,
                null,
                null,
                (player, unused, dur, recv) -> ShowcaseManager.createHotbarShare(player, dur, recv),
                TextUtils.HOTBAR,
                description, receivers, duration
        );
    }

    public static int shareEnderChest(CommandContext<ServerCommandSource> ctx,
                                      ServerPlayerEntity sourcePlayer, String description,
                                      Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(
                ctx,
                ENDER_CHEST,
                null,
                null,
                (player, unused, dur, recv) -> ShowcaseManager.createEnderChestShare(player, dur, recv),
                TextUtils.ENDER_CHEST,
                description, receivers, duration
        );
    }

    public static int shareContainer(CommandContext<ServerCommandSource> ctx, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        ServerPlayerEntity sender = getSenderPlayer(ctx);
        if (sender == null) return 0;

        if (ShowcaseManager.isOnCooldown(sender, CONTAINER)) return 0;

        int durationSeconds = ModConfigManager.getShareSettings(CONTAINER).listeningDuration;
        Text message = Text.translatable("showcase.message.share_container_tip", durationSeconds);

        sender.sendMessage(message, true);
        CountdownBossBar countdown = new CountdownBossBar(sender, message, durationSeconds);
        countdown.start();
        CountdownBossBarManager.add(countdown);

        ContainerOpenWatcher.awaitContainerOpened(sender, durationSeconds,
                (player, inventory) -> {
                    String shareId = ShowcaseManager.createContainerShare(player, inventory, duration, receivers);
                    ShareCommandUtils.sendShareMessage(sender, player, receivers, description, CONTAINER, inventory.getName(), duration, shareId);
                    ShowcaseManager.setCooldown(sender, CONTAINER);
                    CountdownBossBarManager.remove(countdown);
                },
                () -> {
                    sender.sendMessage(TextUtils.warning(Text.translatable("showcase.message.share_container_expiry")), true);
                    CountdownBossBarManager.remove(countdown);
                });

        return Command.SINGLE_SUCCESS;
    }

    public static int shareMerchant(CommandContext<ServerCommandSource> ctx, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        ServerPlayerEntity sender = getSenderPlayer(ctx);
        if (sender == null) return 0;

        if (ShowcaseManager.isOnCooldown(sender, MERCHANT)) return 0;

        int durationSeconds = ModConfigManager.getShareSettings(MERCHANT).listeningDuration;
        Text message = Text.translatable("showcase.message.share_merchant_tip", durationSeconds);
        sender.sendMessage(message, true);
        CountdownBossBar countdown = new CountdownBossBar(sender, message, durationSeconds);
        countdown.start();
        CountdownBossBarManager.add(countdown);

        ContainerOpenWatcher.awaitMerchantGuiOpened(sender, durationSeconds,
                (player, merchantContext) -> {
                    String shareId = ShowcaseManager.createMerchantShare(player, merchantContext, duration, receivers);
                    ShareCommandUtils.sendShareMessage(sender, player, receivers, description, MERCHANT, merchantContext.getFullDisplayName(), duration, shareId);
                    ShowcaseManager.setCooldown(sender, MERCHANT);
                    CountdownBossBarManager.remove(countdown);
                },
                () -> {
                    sender.sendMessage(TextUtils.warning(Text.translatable("showcase.message.share_merchant_expiry")), true);
                    CountdownBossBarManager.remove(countdown);
                });

        return Command.SINGLE_SUCCESS;
    }

    private static int cancelShare(ServerPlayerEntity player, String id) {
        if (player == null) return 0;

        ShareEntry entry = ShowcaseManager.getShareById(id);

        if (entry == null) {
            player.sendMessage(TextUtils.error(Text.translatable("showcase.message.invalid_id")));
            return 0;
        }

        if (!entry.getOwnerUuid().equals(player.getUuid())) {
            player.sendMessage(TextUtils.warning(Text.translatable("showcase.message.cancel_not_owner")));
            return 0;
        }

        boolean success = ShowcaseManager.expireShareById(id);
        Text message = success ?
                TextUtils.success(Text.translatable("showcase.message.cancel_success")) :
                TextUtils.error(Text.translatable("showcase.message.cancel_failed"));

        player.sendMessage(message);
        return success ? Command.SINGLE_SUCCESS : 0;
    }

    private static int getValidatedDuration(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int requested = IntegerArgumentType.getInteger(ctx, DURATION_ARG);
        int min = ModConfigManager.getShareLinkMinExpiry();
        int max = ModConfigManager.getShareLinkDefaultExpiry();

        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (isOp(player)) {
            return requested;
        }

        if (requested < min || requested > max) {
            throw new SimpleCommandExceptionType(
                    Text.translatable("showcase.message.invalid_duration_exception", min, max)
            ).create();
        }

        return requested;
    }

    private interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}