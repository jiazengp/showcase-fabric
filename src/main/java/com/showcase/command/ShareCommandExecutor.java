package com.showcase.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.listener.ContainerOpenWatcher;
import com.showcase.utils.*;
import com.showcase.utils.countdown.CountdownBossBar;
import com.showcase.utils.countdown.CountdownBossBarManager;
import com.showcase.utils.stats.StatUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.showcase.command.ShareCommandUtils.*;
import static com.showcase.command.ShowcaseCommand.*;
import static com.showcase.utils.permissions.PermissionChecker.isOp;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static com.showcase.command.ShowcaseManager.ShareType.*;

public class ShareCommandExecutor {
    @FunctionalInterface
    private interface ShareExecutor {
        int execute(CommandContext<CommandSourceStack> ctx, ServerPlayer sourcePlayer, String description, Collection<ServerPlayer> receiver, Integer durationSeconds);
    }

    @FunctionalInterface
    private interface SimpleShareExecutor {
        int execute(CommandContext<CommandSourceStack> ctx, String description, Collection<ServerPlayer> receivers, Integer durationSeconds);
    }

    /**
     * Returns a RequiredArgumentBuilder for duration input with suggestions and validation
     */
    public static RequiredArgumentBuilder<CommandSourceStack, String> shareDurationArgument() {
        return argument(DURATION_ARG, StringArgumentType.word())
                .suggests(TIME_SUGGESTIONS);
    }

    /**
     * Suggestion provider for time duration with validation feedback
     */
    private static final SuggestionProvider<CommandSourceStack> TIME_SUGGESTIONS = (context, builder) -> {
        String input = builder.getRemaining().toLowerCase();
        int min = ModConfigManager.getShareLinkMinExpiry();
        int max = ModConfigManager.getShareLinkDefaultExpiry();

        // Check if player is OP
        boolean isOp = false;
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            isOp = isOp(player);
        } catch (Exception ignored) {
            // Player not available, default to non-OP
        }

        // Suggest common time formats
        if (input.isEmpty()) {
            // Calculate suggested values from config
            // Ensure suggestions are within valid range and make sense
            int suggestedMin = min;
            int suggestedMid = (min + max) / 2;
            int suggestedMax = max;

            String minFormat = TimeUtils.formatDurationShort(suggestedMin);
            String midFormat = TimeUtils.formatDurationShort(suggestedMid);
            String maxFormat = TimeUtils.formatDurationShort(suggestedMax);

            builder.suggest(minFormat, Component.translatable("showcase.command.duration.suggestion.minimum", TimeUtils.formatSeconds(suggestedMin)));
            builder.suggest(midFormat, Component.translatable("showcase.command.duration.suggestion.recommended", TimeUtils.formatSeconds(suggestedMid)));
            builder.suggest(maxFormat, Component.translatable("showcase.command.duration.suggestion.maximum", TimeUtils.formatSeconds(suggestedMax)));

            if (isOp) {
                builder.suggest("1h", Component.translatable("showcase.command.duration.suggestion.op_only"));
            }
        } else {
            // Try to parse the input and validate
            try {
                int seconds = TimeUtils.parseTimeToSeconds(input);

                if (!isOp && (seconds < min || seconds > max)) {
                    // Show validation error in tooltip
                    builder.suggest(input, Component.translatable("showcase.command.duration.error.out_of_range",
                        TimeUtils.formatSeconds(min), TimeUtils.formatSeconds(max)));
                } else {
                    // Show valid time in tooltip
                    String timeDisplay = TimeUtils.formatSeconds(seconds);
                    Component status = isOp ?
                        Component.translatable("showcase.command.duration.valid.op", timeDisplay) :
                        Component.translatable("showcase.command.duration.valid", timeDisplay);
                    builder.suggest(input, status);
                }
            } catch (IllegalArgumentException e) {
                // Invalid format - suggest corrections
                if (input.matches("\\d+")) {
                    try {
                        int numValue = Integer.parseInt(input);
                        builder.suggest(input + "s", Component.translatable("showcase.command.duration.hint.add_seconds"));
                        builder.suggest(input + "m", Component.translatable("showcase.command.duration.hint.add_minutes"));
                        if (isOp || numValue <= max / 60) {
                            builder.suggest(input + "h", Component.translatable("showcase.command.duration.hint.add_hours"));
                        }
                    } catch (NumberFormatException nfe) {
                        // Number too large, show error
                        builder.suggest(input, Component.translatable("showcase.command.duration.error.invalid_format"));
                    }
                } else {
                    builder.suggest(input, Component.translatable("showcase.command.duration.error.invalid_format"));
                }
            }
        }

        return builder.buildFuture();
    };

    public static LiteralArgumentBuilder<CommandSourceStack> createItemShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.ITEM, withSource, ShareCommandExecutor::shareItem);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createStatsShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.STATS, withSource, ShareCommandExecutor::shareStats);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createInventoryShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.INVENTORY, withSource, ShareCommandExecutor::shareInventory);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createHotbarShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.HOTBAR, withSource, ShareCommandExecutor::shareHotbar);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createEnderChestShareCommand(boolean withSource) {
        return createShareCommand(ShareConstants.ENDER_CHEST, withSource, ShareCommandExecutor::shareEnderChest);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createContainerShareCommand() {
        return createSimpleShareCommand(ShareConstants.CONTAINER, ShareCommandExecutor::shareContainer);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> shareMerchantShareCommand() {
        return createSimpleShareCommand(ShareConstants.MERCHANT, ShareCommandExecutor::shareMerchant);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createCancelCommand() {
        return literal(ShareConstants.CANCEL)
                .then(argument("id", string())
                        .suggests((ctx, builder) -> {
                            ServerPlayer player = getSenderPlayer(ctx);
                            if (player != null) {
                                ShowcaseManager.getUnmodifiableActiveShares().entrySet().stream()
                                        .filter(e -> e.getValue().getOwnerUuid().equals(player.getUUID()))
                                        .map(Map.Entry::getKey)
                                        .forEach(builder::suggest);
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer player = getSenderPlayer(ctx);
                            String id = StringArgumentType.getString(ctx, "id");
                            return cancelShare(player, id);
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createShareCommand(String commandName, boolean withSource, ShareExecutor executor) {
        var command = literal(commandName)
                .executes(ctx -> executor.execute(ctx, null, null, null, null));

        if (withSource) {
            command.then(argument(SOURCE_ARG, EntityArgument.player())
                    .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), null, null, null))
                    .then(argument(RECEIVERS_ARG, EntityArgument.players())
                            .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), null, getReceivers(ctx), null))
                            .then(shareDurationArgument()
                                    .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), null, getReceivers(ctx), getValidatedDuration(ctx)))
                                    .then(argument(DESCRIPTION_ARG, greedyString())
                                            .executes(ctx -> executor.execute(ctx, getSourcePlayer(ctx), getDescription(ctx), getReceivers(ctx), getValidatedDuration(ctx)))
                                    )
                            )
                    )
            );
        } else {
            command.then(argument(RECEIVERS_ARG, EntityArgument.players())
                    .executes(ctx -> executor.execute(ctx, null, null, getReceivers(ctx), null))
                    .then(shareDurationArgument()
                            .executes(ctx -> executor.execute(ctx, null, null, getReceivers(ctx), getValidatedDuration(ctx)))
                            .then(argument(DESCRIPTION_ARG, greedyString())
                                    .executes(ctx -> executor.execute(ctx, null, getDescription(ctx), getReceivers(ctx), getValidatedDuration(ctx)))
                            )
                    )
            );
        }

        return command;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSimpleShareCommand(String commandName, SimpleShareExecutor executor) {
        var command = literal(commandName)
                .executes(ctx -> executor.execute(ctx, null, null, null));

        command.then(argument(RECEIVERS_ARG, EntityArgument.players())
                .executes(ctx -> executor.execute(ctx, null, getReceivers(ctx), null))
                .then(shareDurationArgument()
                        .executes(ctx -> executor.execute(ctx, null, getReceivers(ctx), getValidatedDuration(ctx)))
                        .then(argument(DESCRIPTION_ARG, greedyString())
                                .executes(ctx -> executor.execute(ctx, getDescription(ctx), getReceivers(ctx), getValidatedDuration(ctx)))
                        )
                )
        );

        return command;
    }

    private static int executeShare(
            CommandContext<CommandSourceStack> ctx,
            ShowcaseManager.ShareType type,
            @Nullable Function<ServerPlayer, ItemStack> stackSupplier,
            @Nullable String emptyMessageKey,
            QuadFunction<ServerPlayer, @Nullable ItemStack, Integer, Collection<ServerPlayer>, String> shareCreator,
            Component defaultDisplayName,
            String description,
            Collection<ServerPlayer> receivers,
            Integer duration
    ) {
        try {
            ServerPlayer sender = getSenderPlayer(ctx);
            if (sender == null) return 0;
            if (ShowcaseManager.isOnCooldown(sender, type)) return 0;

            // Check sub-permissions for optional parameters
            if (receivers != null && !ShareCommandUtils.canSpecifyReceivers(sender, type)) {
                sender.sendSystemMessage(TextUtils.error(Component.translatable("showcase.command.no_permission.receivers")));
                return 0;
            }
            
            if (duration != null && !ShareCommandUtils.canSpecifyDuration(sender, type)) {
                sender.sendSystemMessage(TextUtils.error(Component.translatable("showcase.command.no_permission.duration")));
                return 0;
            }
            
            if (description != null && !description.isEmpty() && !ShareCommandUtils.canSpecifyDescription(sender, type)) {
                sender.sendSystemMessage(TextUtils.error(Component.translatable("showcase.command.no_permission.description")));
                return 0;
            }

            ServerPlayer source = getSourceOrSender(ctx);
            ItemStack stack = ItemStack.EMPTY;

            if (stackSupplier != null) {
                stack = stackSupplier.apply(source).copy();
                if (stack.isEmpty()) {
                    sender.sendSystemMessage(TextUtils.error(Component.translatable(emptyMessageKey)));
                    return 0;
                }
            }

            String shareId = shareCreator.apply(source, stackSupplier != null ? stack : null, duration, receivers);
            Component displayName = (stackSupplier != null ? StackUtils.getDisplayName(stack) : defaultDisplayName);

            ShareCommandUtils.sendShareMessage(sender, source, receivers, description, type, displayName, duration, shareId);
            ShowcaseManager.setCooldown(sender, type);

            return Command.SINGLE_SUCCESS;
        } catch (RuntimeException e) {
            handleError(ctx, e);
            return -1;
        }
    }

    public static int shareItem(CommandContext<CommandSourceStack> ctx, ServerPlayer sourcePlayer, String description, Collection<ServerPlayer> receivers, Integer duration) {
        return executeShare(
                ctx,
                ITEM,
                player -> player.getItemBySlot(EquipmentSlot.MAINHAND),
                "showcase.message.no_item",
                ShowcaseManager::createItemShare,
                Component.empty(),
                description, receivers, duration
        );
    }

    public static int shareStats(CommandContext<CommandSourceStack> ctx, ServerPlayer sourcePlayer, String description, Collection<ServerPlayer> receivers, Integer duration) {
        return executeShare(
                ctx,
                STATS,
                StatUtils::createStatsBook,
                "showcase.message.no_stats",
                ShowcaseManager::createStatsShare,
                Component.empty(),
                description, receivers, duration
        );
    }

    public static int shareInventory(CommandContext<CommandSourceStack> ctx,
                                     ServerPlayer sourcePlayer, String description,
                                     Collection<ServerPlayer> receivers, Integer duration) {
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

    public static int shareHotbar(CommandContext<CommandSourceStack> ctx,
                                  ServerPlayer sourcePlayer, String description,
                                  Collection<ServerPlayer> receivers, Integer duration) {
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

    public static int shareEnderChest(CommandContext<CommandSourceStack> ctx,
                                      ServerPlayer sourcePlayer, String description,
                                      Collection<ServerPlayer> receivers, Integer duration) {
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

    public static int shareContainer(CommandContext<CommandSourceStack> ctx, String description, Collection<ServerPlayer> receivers, Integer duration) {
        ServerPlayer sender = getSenderPlayer(ctx);
        if (sender == null) return 0;

        if (ShowcaseManager.isOnCooldown(sender, CONTAINER)) return 0;

        int durationSeconds = ModConfigManager.getShareSettings(CONTAINER).listeningDuration;
        Component message = Component.translatable("showcase.message.share_container_tip", durationSeconds);

        sender.sendSystemMessage(message, true);
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
                    sender.sendSystemMessage(TextUtils.warning(Component.translatable("showcase.message.share_container_expiry")), true);
                    CountdownBossBarManager.remove(countdown);
                });

        return Command.SINGLE_SUCCESS;
    }

    public static int shareMerchant(CommandContext<CommandSourceStack> ctx, String description, Collection<ServerPlayer> receivers, Integer duration) {
        ServerPlayer sender = getSenderPlayer(ctx);
        if (sender == null) return 0;

        if (ShowcaseManager.isOnCooldown(sender, MERCHANT)) return 0;

        int durationSeconds = ModConfigManager.getShareSettings(MERCHANT).listeningDuration;
        Component message = Component.translatable("showcase.message.share_merchant_tip", durationSeconds);
        sender.sendSystemMessage(message, true);
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
                    sender.sendSystemMessage(TextUtils.warning(Component.translatable("showcase.message.share_merchant_expiry")), true);
                    CountdownBossBarManager.remove(countdown);
                });

        return Command.SINGLE_SUCCESS;
    }

    private static int cancelShare(ServerPlayer player, String id) {
        if (player == null) return 0;

        ShareEntry entry = ShowcaseManager.getShareById(id);

        if (entry == null) {
            player.sendSystemMessage(TextUtils.error(Component.translatable("showcase.message.invalid_id")));
            return 0;
        }

        if (!entry.getOwnerUuid().equals(player.getUUID())) {
            player.sendSystemMessage(TextUtils.warning(Component.translatable("showcase.message.cancel_not_owner")));
            return 0;
        }

        boolean success = ShowcaseManager.expireShareById(id);
        Component message = success ?
                TextUtils.success(Component.translatable("showcase.message.cancel_success")) :
                TextUtils.error(Component.translatable("showcase.message.cancel_failed"));

        player.sendSystemMessage(message);
        return success ? Command.SINGLE_SUCCESS : 0;
    }

    private static int getValidatedDuration(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String timeString = StringArgumentType.getString(ctx, DURATION_ARG);
        int requested;

        try {
            requested = TimeUtils.parseTimeToSeconds(timeString);
        } catch (IllegalArgumentException e) {
            throw new SimpleCommandExceptionType(
                    Component.translatable("showcase.command.invalid_time_format")
            ).create();
        }

        int min = ModConfigManager.getShareLinkMinExpiry();
        int max = ModConfigManager.getShareLinkDefaultExpiry();

        ServerPlayer player = ctx.getSource().getPlayer();

        if (isOp(player)) {
            return requested;
        }

        if (requested < min || requested > max) {
            throw new SimpleCommandExceptionType(
                    Component.translatable("showcase.message.invalid_duration_exception", min, max)
            ).create();
        }

        return requested;
    }

    private interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}
