package com.showcase.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.showcase.ShowcaseMod;
import com.showcase.data.ShareEntry;
import com.showcase.utils.ContainerOpenWatcher;
import com.showcase.utils.StackUtils;
import com.showcase.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.showcase.command.ShareCommandUtils.*;
import static com.showcase.command.ShowcaseCommand.*;
import static com.showcase.utils.PermissionChecker.isOp;
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
                ShowcaseMod.CONFIG.shareLinkMinimumExpiryTime,
                ShowcaseMod.CONFIG.shareLinkExpiryTime
        );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createItemShareCommand() {
        return createSimpleShareCommand("item", ShareCommandExecutor::shareItem);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createInventoryShareCommand(boolean withSource) {
        return createShareCommand("inventory", withSource, ShareCommandExecutor::shareInventory);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createHotbarShareCommand(boolean withSource) {
        return createShareCommand("hotbar", withSource, ShareCommandExecutor::shareHotbar);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createEnderChestShareCommand(boolean withSource) {
        return createShareCommand("ender_chest", withSource, ShareCommandExecutor::shareEnderChest);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createContainerShareCommand() {
        return createSimpleShareCommand("container", ShareCommandExecutor::shareContainer);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> shareMerchantShareCommand() {
        return createSimpleShareCommand("merchant", ShareCommandExecutor::shareMerchant);
    }


    public static LiteralArgumentBuilder<ServerCommandSource> createCancelCommand() {
        return literal("cancel")
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

    @FunctionalInterface
    private interface ShareCreator {
        String create(ServerPlayerEntity player, Integer duration);
    }

    private static int executeShare(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sourcePlayer,
                                    ShowcaseManager.ShareType shareType, ShareCreator shareCreator,
                                    Text displayName, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        ServerPlayerEntity sender = getSenderPlayer(ctx);
        if (sender == null) return 0;

        if (ShowcaseManager.isOnCooldown(sender, shareType)) return 0;

        ServerPlayerEntity source = sourcePlayer != null ? sourcePlayer : sender;
        String shareId = shareCreator.create(source, duration);

        ShareCommandUtils.sendShareMessage(sender, source, receivers, description, shareType, displayName, duration, shareId);
        ShowcaseManager.setCooldown(sender, shareType);

        return Command.SINGLE_SUCCESS;
    }

    public static int shareItem(CommandContext<ServerCommandSource> ctx, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        ServerPlayerEntity sender = getSenderPlayer(ctx);

        if (sender == null) return 0;
        if (ShowcaseManager.isOnCooldown(sender, ITEM)) return 0;

        ItemStack stack = sender.getEquippedStack(EquipmentSlot.MAINHAND).copy();

        if (stack.isEmpty()) {
            sender.sendMessage(TextUtils.error(Text.translatable("showcase.message.no_item")));
            return 0;
        }

        String shareId = ShowcaseManager.createItemShare(sender, stack, duration);
        ShareCommandUtils.sendShareMessage(sender, sender, receivers, description, ITEM, StackUtils.getDisplayName(stack), duration, shareId);
        ShowcaseManager.setCooldown(sender, ITEM);

        return Command.SINGLE_SUCCESS;
    }

    public static int shareInventory(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sourcePlayer, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(ctx, sourcePlayer, INVENTORY, ShowcaseManager::createInventoryShare, TextUtils.INVENTORY, description, receivers, duration);
    }

    public static int shareHotbar(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sourcePlayer, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(ctx, sourcePlayer, HOTBAR, ShowcaseManager::createHotbarShare, TextUtils.HOTBAR, description, receivers, duration);
    }

    public static int shareEnderChest(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity sourcePlayer, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        return executeShare(ctx, sourcePlayer, ENDER_CHEST, ShowcaseManager::createEnderChestShare, TextUtils.ENDER_CHEST, description, receivers, duration);
    }

    public static int shareContainer(CommandContext<ServerCommandSource> ctx, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        ServerPlayerEntity sender = getSenderPlayer(ctx);
        if (sender == null) return 0;

        if (ShowcaseManager.isOnCooldown(sender, CONTAINER)) return 0;

        sender.sendMessage(Text.translatable("showcase.message.share_container_tip", ShowcaseMod.CONFIG.containerListeningDuration));

        ContainerOpenWatcher.awaitContainerOpened(sender, 10,
                (player, inventory) -> {
                    String shareId = ShowcaseManager.createContainerShare(player, inventory, duration);
                    ShareCommandUtils.sendShareMessage(sender, player, receivers, description, CONTAINER, inventory.getName(), duration, shareId);
                    ShowcaseManager.setCooldown(sender, CONTAINER);
                },
                () -> sender.sendMessage(TextUtils.warning(Text.translatable("showcase.message.share_container_expiry"))));

        return Command.SINGLE_SUCCESS;
    }

    public static int shareMerchant(CommandContext<ServerCommandSource> ctx, String description, Collection<ServerPlayerEntity> receivers, Integer duration) {
        ServerPlayerEntity sender = getSenderPlayer(ctx);
        if (sender == null) return 0;

        if (ShowcaseManager.isOnCooldown(sender, MERCHANT)) return 0;

        sender.sendMessage(Text.translatable("showcase.message.share_merchant_tip", ShowcaseMod.CONFIG.containerListeningDuration));

        ContainerOpenWatcher.awaitMerchantGuiOpened(sender, 10,
                (player, merchantContext) -> {
                    String shareId = ShowcaseManager.createMerchantShare(player, merchantContext, duration);
                    ShareCommandUtils.sendShareMessage(sender, player, receivers, description, MERCHANT, merchantContext.getFullDisplayName(), duration, shareId);
                    ShowcaseManager.setCooldown(sender, MERCHANT);
                },
                () -> sender.sendMessage(TextUtils.warning(Text.translatable("showcase.message.share_merchant_expiry"))));

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
        int min = ShowcaseMod.CONFIG.shareLinkMinimumExpiryTime;
        int max = ShowcaseMod.CONFIG.shareLinkExpiryTime;

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
}