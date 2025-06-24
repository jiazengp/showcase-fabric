package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.showcase.data.ShareEntry;
import com.showcase.utils.ChatPaginator;
import com.showcase.utils.ContainerOpenWatcher;
import com.showcase.utils.MessageUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.showcase.command.ShowcaseManager.getDisplayName;
import static net.minecraft.server.command.CommandManager.*;

public class ShowcaseCommand {
    private static final String RECEIVER_ARG = "receiver";
    private static final String DESCRIPTION_ARG = "description";
    private static final String SOURCE_ARG = "source";
    private static final String VIEW_COMMAND = "showcase-view";
    private static final String CANCEL_COMMAND = "cancel";
    private static final String SHARE_COMMAND = "showcase";
    private static final String ADMIN_SHARE_COMMAND = "admin-showcase";
    private static final String MANAGE_COMMAND = "showcase-manage";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // View command
        registerShowcaseViewCommand(dispatcher);
        
        // Regular share commands (without source parameter)
        registerPlayerShowcaseCommands(dispatcher);
        
        // Admin share commands (with source parameter)
        registerAdminShareCommands(dispatcher);

        registerShowcaseManageCommand(dispatcher);
    }

    private static void registerShowcaseManageCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(MANAGE_COMMAND)
                .then(createReloadCommand())
                .then(createListCommand())
                .then(createCancelCommand()
        ));
    }

    private static void registerShowcaseViewCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(VIEW_COMMAND)
            .then(argument("id", StringArgumentType.string())
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    String id = StringArgumentType.getString(ctx, "id");
                    if (!ShowcaseManager.openSharedContent(player, id)) {
                        player.sendMessage(Text.literal(ShowcaseMod.CONFIG.messages.invalidOrExpiredLinkTips).formatted(Formatting.RED));
                    }
                    return Command.SINGLE_SUCCESS;
                })
            )
        );
    }

    private static void registerPlayerShowcaseCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        var shareCommand = literal(SHARE_COMMAND)
            .then(createInventoryShareCommand(false))
            .then(createEnderChestShareCommand(false))
            .then(createHotbarShareCommand(false))
                .then(createItemShareCommand())
            .then(createContainerShareCommand());

        dispatcher.register(shareCommand);
    }

    private static void registerAdminShareCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        var adminShareCommand = literal(ADMIN_SHARE_COMMAND)
            .requires(source -> hasPermission(source, "showcase.admin"))
            .then(createInventoryShareCommand(true))
            .then(createEnderChestShareCommand(true))
            .then(createHotbarShareCommand(true));

        dispatcher.register(adminShareCommand);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createItemShareCommand() {
        var command = literal("item")
            .executes(ctx -> shareItem(ctx, null, null));
        

        command.then(argument(RECEIVER_ARG, EntityArgumentType.player())
                .executes(ctx -> shareItem(ctx, null, getReceiver(ctx))).then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                    .executes(ctx -> shareItem(ctx, getDescription(ctx), getReceiver(ctx)))
                )
            );

        
        return command;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createInventoryShareCommand(boolean withSource) {
        var command = literal("inventory")
            .executes(ctx -> shareInventory(ctx, null, null, null));
        
        if (withSource) {
            command.then(argument(SOURCE_ARG, EntityArgumentType.player())
                .executes(ctx -> shareInventory(ctx, getSource(ctx), null, null))
                .then(argument(RECEIVER_ARG, EntityArgumentType.player())
                    .executes(ctx -> shareInventory(ctx, getSource(ctx), null, getReceiver(ctx)))
                    .then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                        .executes(ctx -> shareInventory(ctx, getSource(ctx), getDescription(ctx), getReceiver(ctx)))
                    )
                )
            );
        } else {
            command.then(argument(RECEIVER_ARG, EntityArgumentType.player())
                .executes(ctx -> shareInventory(ctx, null, null, getReceiver(ctx)))
                .then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                    .executes(ctx -> shareInventory(ctx, null, getDescription(ctx), getReceiver(ctx)))
                )
            );
        }
        
        return command;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createEnderChestShareCommand(boolean withSource) {
        var command = literal("enderchest")
            .executes(ctx -> shareEnderChest(ctx, null, null, null));

        if (withSource) {
            command.then(argument(SOURCE_ARG, EntityArgumentType.player())
                .executes(ctx -> shareEnderChest(ctx, getSource(ctx), null, null))
                .then(argument(RECEIVER_ARG, EntityArgumentType.player())
                    .executes(ctx -> shareEnderChest(ctx, getSource(ctx), null, getReceiver(ctx)))
                    .then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                        .executes(ctx -> shareEnderChest(ctx, getSource(ctx), getDescription(ctx), getReceiver(ctx)))
                    )
                ));
        } else {
            command.then(argument(RECEIVER_ARG, EntityArgumentType.player())
                .executes(ctx -> shareEnderChest(ctx, null, null, getReceiver(ctx)))
                .then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                    .executes(ctx -> shareEnderChest(ctx, null, getDescription(ctx), getReceiver(ctx)))
                ));
        }

        return command;
    }

    private static int shareEnderChest(CommandContext<ServerCommandSource> ctx,
                                     ServerPlayerEntity sourcePlayer,
                                     String description,
                                     ServerPlayerEntity receiver) throws CommandSyntaxException {
        ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity actualSource = sourcePlayer != null ? sourcePlayer : sender;

        if (ShowcaseManager.isOnCooldown(sender, ShowcaseManager.ShareType.ENDER_CHEST)) return 0;

        String shareId = ShowcaseManager.createEnderChestShare(actualSource);
        sendShareMessage(sender, actualSource, receiver, description, "enderchest", Text.translatable("container.enderchest") ,shareId);
        ShowcaseManager.setCooldown(sender, ShowcaseManager.ShareType.ENDER_CHEST);
        return Command.SINGLE_SUCCESS;
    }

    private static ServerPlayerEntity getReceiver(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return EntityArgumentType.getPlayer(ctx, RECEIVER_ARG);
    }

    private static String getDescription(CommandContext<ServerCommandSource> ctx) {
        return StringArgumentType.getString(ctx, DESCRIPTION_ARG);
    }

    private static ServerPlayerEntity getSource(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return EntityArgumentType.getPlayer(ctx, SOURCE_ARG);
    }

    private static int shareContainer(CommandContext<ServerCommandSource> ctx, String description, ServerPlayerEntity receiver) throws CommandSyntaxException {
        ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();

        if (ShowcaseManager.isOnCooldown(sender, ShowcaseManager.ShareType.CONTAINER)) return 0;

        sender.sendMessage(Text.literal(String.format(ShowcaseMod.CONFIG.messages.shareContainerTip, ShowcaseMod.CONFIG.containerListeningDuration)));

        ContainerOpenWatcher.awaitWithItems(sender, 10,
                (player, inventory) -> {
                    String shareId = ShowcaseManager.createContainerShare(sender, inventory);
                    sendShareMessage(sender, sender, receiver, description, "container", inventory.getName(), shareId);
                    ShowcaseManager.setCooldown(sender, ShowcaseManager.ShareType.CONTAINER);
                    },
                () -> {
                    sender.playSound(net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    sender.sendMessage(Text.literal(ShowcaseMod.CONFIG.messages.shareContainerExpiryNotice));
                });

        return Command.SINGLE_SUCCESS;
    }

    private static int shareItem(CommandContext<ServerCommandSource> ctx,
                               String description,
                               ServerPlayerEntity receiver) throws CommandSyntaxException {
        ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();

        if (ShowcaseManager.isOnCooldown(sender, ShowcaseManager.ShareType.ITEM)) return 0;

        ItemStack stack = sender.getInventory().getMainHandStack();
        ModConfig.Messages messages = ShowcaseMod.CONFIG.messages;

        if (stack.isEmpty()) {
            sender.sendMessage(Text.literal(messages.noItem).formatted(Formatting.RED));
            return 0;
        }

        String shareId = ShowcaseManager.createItemShare(sender, stack);
        sendShareMessage(sender, sender, receiver, description, "item", getDisplayName(stack), shareId);
        ShowcaseManager.setCooldown(sender, ShowcaseManager.ShareType.ITEM);
        return Command.SINGLE_SUCCESS;
    }

    private static int shareInventory(CommandContext<ServerCommandSource> ctx,
                                    ServerPlayerEntity sourcePlayer,
                                    String description,
                                    ServerPlayerEntity receiver) throws CommandSyntaxException {
        ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity actualSource = sourcePlayer != null ? sourcePlayer : sender;

        if (ShowcaseManager.isOnCooldown(sender, ShowcaseManager.ShareType.INVENTORY)) return 0;

        String shareId = ShowcaseManager.createInventoryShare(actualSource);
        sendShareMessage(sender, actualSource, receiver, description, "inventory", Text.translatable("itemGroup.inventory"), shareId);
        ShowcaseManager.setCooldown(sender, ShowcaseManager.ShareType.INVENTORY);
        return Command.SINGLE_SUCCESS;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createHotbarShareCommand(boolean withSource) {
        var command = literal("hotbar")
            .executes(ctx -> shareHotbar(ctx, null, null, null));

        if (withSource) {
            command.then(argument(SOURCE_ARG, EntityArgumentType.player())
                .executes(ctx -> shareHotbar(ctx, getSource(ctx), null, null))
                .then(argument(RECEIVER_ARG, EntityArgumentType.player())
                    .executes(ctx -> shareHotbar(ctx, getSource(ctx), null, getReceiver(ctx)))
                    .then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                        .executes(ctx -> shareHotbar(ctx, getSource(ctx), getDescription(ctx), getReceiver(ctx)))
                    )
                )
            );
        } else {
            command.then(argument(RECEIVER_ARG, EntityArgumentType.player())
                .executes(ctx -> shareHotbar(ctx, null, null, getReceiver(ctx)))
                .then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                    .executes(ctx -> shareHotbar(ctx, null, getDescription(ctx), getReceiver(ctx)))
                )
            );
        }

        return command;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createContainerShareCommand() {
        var command = literal("container")
                .executes(ctx -> shareContainer(ctx,null, null));

        command.then(argument(RECEIVER_ARG, EntityArgumentType.player())
                    .executes(ctx -> shareContainer(ctx, null, getReceiver(ctx)))
                    .then(argument(DESCRIPTION_ARG, StringArgumentType.greedyString())
                            .executes(ctx -> shareContainer(ctx, getDescription(ctx), getReceiver(ctx)))
                    )
            );


        return command;
    }

    private static int shareHotbar(CommandContext<ServerCommandSource> ctx,
                                 ServerPlayerEntity sourcePlayer,
                                 String description,
                                 ServerPlayerEntity receiver) throws CommandSyntaxException {
        ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity actualSource = sourcePlayer != null ? sourcePlayer : sender;

        if (ShowcaseManager.isOnCooldown(sender, ShowcaseManager.ShareType.HOTBAR)) return 0;

        String shareId = ShowcaseManager.createHotbarShare(actualSource);
        sendShareMessage(sender, actualSource, receiver, description, "hotbar", Text.translatable("itemGroup.hotbar"), shareId);
        ShowcaseManager.setCooldown(sender, ShowcaseManager.ShareType.HOTBAR);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableText createContainerPreviewText(Text itemName, String type, String shareId) {
        MutableText preview = Text.literal("").append(itemName).formatted(getFormattingColorForType(type)).append("\n");

        Text containerPreview = ShowcaseManager.getContainerPreview(shareId);

        if (containerPreview != null) {
            preview.append(containerPreview).append("\n------\n").append(ShowcaseMod.CONFIG.messages.clickToView).formatted(Formatting.GRAY);
        } else {
            preview.append(Text.translatable("item.minecraft.bundle.empty").formatted(Formatting.RED));
        }

        return preview;
    }

    public static MutableText createClickableItemName(ServerPlayerEntity source, String type, Text itemName, String shareId) {
        MutableText hoverableName = createClickableTag(itemName, type, shareId);

        if ("item".equals(type)) {
            ItemStack stack = source.getInventory().getMainHandStack();
            if (!stack.isEmpty()) {
                hoverableName.setStyle(hoverableName.getStyle()
                    .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_ITEM,
                        new HoverEvent.ItemStackContent(stack)
                    )));
            }
            return hoverableName;
        }

        MutableText preview = createContainerPreviewText(itemName, type, shareId);
        hoverableName.setStyle(hoverableName.getStyle()
                .withHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    preview
                )));

        return hoverableName;
    }

    public static void sendShareMessage(ServerPlayerEntity sender, ServerPlayerEntity source, ServerPlayerEntity receiver,
                                      String description, String type, Text itemName, String shareId) {
        ModConfig.Messages messages = ShowcaseMod.CONFIG.messages;
        ModConfig config = ShowcaseMod.CONFIG;
        MutableText clickableItemName = createClickableItemName(source, type, itemName, shareId);
        String descriptionMessage = "${sourcePlayer}" + description + "${itemName}";

        Text messageForBroadcast = MessageUtils.formatMessageWithPlayerTarget(
            description != null ? descriptionMessage : buildDefaultMessageForSender(sender == source, type),
            sender, receiver, clickableItemName);

        Text messageForReceiver = MessageUtils.formatPlayerMessage(
            description != null ? descriptionMessage : buildDefaultMessageForReceiver(type),
            source, clickableItemName);

        MutableText textMessage = Text.literal("")
            .append(receiver != null ? messageForReceiver : messageForBroadcast)
            .append(Text.literal(String.format(messages.expiryNotice, config.shareLinkExpiryTime / 60)));

        if (receiver != null) {
            receiver.sendMessage(textMessage);
            sender.sendMessage(MessageUtils.formatPlayerMessage(
                config.messages.privateShareTip,
                receiver, clickableItemName));
        } else {
            Objects.requireNonNull(sender.getServer()).getPlayerManager().broadcast(textMessage, false);
        }
    }

    private static ClickEvent createShareClickEvent(String id) {
        return new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/" + VIEW_COMMAND + " "  + id
        );
    }

    private static String buildDefaultMessageForReceiver(String type) {
        ModConfig.Messages message = ShowcaseMod.CONFIG.messages;
        return switch (type) {
            case "item" -> message.shareItemMessagesByDefault;
            case "inventory" -> message.shareInventoryMessagesByDefault;
            case "enderchest" -> message.shareEnderChestMessagesByDefault;
            case "hotbar" -> message.shareHotbarMessagesByDefault;
            case "container" -> message.shareContainerByDefault;
            default -> "";
        };
    }

    private static String buildDefaultMessageForSender(Boolean isSelf, String type) {
        ModConfig.Messages messages = ShowcaseMod.CONFIG.messages;

        return switch (type) {
            case "item" -> messages.itemShared;
            case "container" -> messages.containerShared;
            case "inventory" -> isSelf ?
                    messages.inventoryShared :
                    messages.otherInventoryShared;
            case "enderchest" -> isSelf ?
                    messages.enderChestShared :
                    messages.otherEnderChestShared;
            case "hotbar" -> isSelf ?
                    messages.hotbarShared :
                    messages.otherHotbarShared;
            default -> "";
        };
    }

    private static Formatting getFormattingColorForType(String type) {
        return switch (type) {
            case "item" -> Formatting.BLUE;
            case "inventory" -> Formatting.GREEN;
            case "enderchest" -> Formatting.DARK_PURPLE;
            case "hotbar" -> Formatting.YELLOW;
            case "container" -> Formatting.GOLD;
            default -> Formatting.WHITE;
        };
    }

    private static MutableText createClickableTag(Text name, String type, String id) {
        Formatting color = getFormattingColorForType(type);

        return Text.literal( "")
                .append(name)
                .styled(style -> style
                        .withColor(color)
                        .withUnderline(true)
                        .withClickEvent(createShareClickEvent(id)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createListCommand() {
        return literal("list")
                .requires(source -> hasPermission(source, "showcase.manage.list"))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    return executeListCommand(player, 1); // 默认第一页
                })
                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            int page = IntegerArgumentType.getInteger(ctx, "page");
                            return executeListCommand(player, page);
                        }));
    }

    private static int executeListCommand(ServerPlayerEntity player, int page) {
        Map<String, ShareEntry> shares = ShowcaseManager.getActiveShares();

        if (shares.isEmpty()) {
            player.sendMessage(Text.literal("No active shares").formatted(Formatting.RED));
            return 0;
        }

        List<Map.Entry<String, ShareEntry>> shareList = new ArrayList<>(shares.entrySet());
        ChatPaginator<Map.Entry<String, ShareEntry>> paginator =
                new ChatPaginator<>(shareList, 3, "/" + MANAGE_COMMAND + " " + "list");

        Text result = paginator.renderPage(page, entry -> {
            String shareId = entry.getKey();
            ShareEntry share = entry.getValue();
            return buildShareLine(player, shareId, share);
        }, "Active Shares");

        player.sendMessage(result);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableText buildShareLine(ServerPlayerEntity viewer, String shareId, ShareEntry share) {
        MinecraftServer server = viewer.getServer();
        ServerPlayerEntity owner = null;
        if (server != null) {
            owner = server.getPlayerManager().getPlayer(share.getOwnerUuid());
        }

        Text ownerName = owner != null
                ? Objects.requireNonNullElse(owner.getDisplayName(), Text.literal(owner.getName().getString()))
                : Text.translatable("argument.player.unknown");

        String typeStr = share.getType().name().toLowerCase();
        Formatting typeColor = getFormattingColorForType(typeStr);

        Text itemName = getShareItemName(share);

        return Text.literal("")
                .append(Text.literal("▶ ").formatted(Formatting.DARK_GRAY))

                .append(Text.literal("[ID] ").formatted(Formatting.GRAY))
                .append(Text.literal(shareId).formatted(Formatting.YELLOW)
                        .styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, shareId))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Copy ID")))))
                .append(Text.literal("  "))

                .append(Text.literal("[Owner] ").formatted(Formatting.GRAY))
                .append(Objects.requireNonNull(ownerName).copy().formatted(Formatting.GREEN))
                .append(Text.literal("  "))

                .append(Text.literal("[Share] ").formatted(Formatting.GRAY))
                .append(createClickableItemName(owner, share.getType().name().toLowerCase(), itemName, shareId))
                .formatted(typeColor)
                .append(Text.literal("  "))

                .append(Text.literal("[Views] ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(share.getViewCount())).formatted(Formatting.AQUA))
                .append(Text.literal("  "))

                .append(Text.literal("\n    "))
                .append(Text.literal("Created: ").formatted(Formatting.GRAY))
                .append(Text.literal(formatTime(share.getTimestamp())).formatted(Formatting.WHITE))
                .append(Text.literal(" | Expires: ").formatted(Formatting.GRAY))
                .append(Text.literal(formatTime(share.getTimestamp() + ShowcaseMod.CONFIG.shareLinkExpiryTime * 1000L)).formatted(Formatting.WHITE))

                .append(Text.literal(" | [Cancel Share] ")
                        .styled(style -> style
                                .withColor(Formatting.RED)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + MANAGE_COMMAND + " " + CANCEL_COMMAND + " " + shareId))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to cancel share")))))
                .append(Text.literal("  "))


                .append(Text.literal("\n\n"));
    }

    private static List<String> getShareIdCompletions() {
        return new ArrayList<>(ShowcaseManager.getActiveShares().keySet());
    }

    private static List<String> getPlayerNameCompletions(CommandContext<ServerCommandSource> ctx) {
        List<String> names = new ArrayList<>();
        Objects.requireNonNull(ctx.getSource().getServer())
            .getPlayerManager()
            .getPlayerList()
            .forEach(player -> names.add(player.getName().getString()));
        return names;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createCancelCommand() {
        return literal(CANCEL_COMMAND)
            .requires(source -> hasPermission(source, "showcase.manage.cancel"))
            .then(argument("target", StringArgumentType.string())
                .suggests((ctx, builder) -> {
                    getShareIdCompletions().forEach(builder::suggest);
                    getPlayerNameCompletions(ctx).forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();
                    String target = StringArgumentType.getString(ctx, "target");

                    // Try as share ID first
                    if (ShowcaseManager.expireShareById(target)) {
                        admin.sendMessage(Text.literal("Successfully canceled share " + target)
                            .formatted(Formatting.GREEN));
                        return Command.SINGLE_SUCCESS;
                    }

                    // Try as player name
                    ServerPlayerEntity player = Objects.requireNonNull(ctx.getSource().getServer())
                        .getPlayerManager()
                        .getPlayer(target);
                    if (player != null) {
                        int count = ShowcaseManager.expireSharesByPlayer(player.getUuid());
                        if (count > 0) {
                            admin.sendMessage(Text.literal("Canceled " + count + " shares from " + player.getName().getString())
                                .formatted(Formatting.GREEN));
                        } else {
                            admin.sendMessage(Text.literal("No active shares found for " + player.getName().getString())
                                .formatted(Formatting.RED));
                        }
                        return count > 0 ? Command.SINGLE_SUCCESS : 0;
                    }

                    admin.sendMessage(Text.literal("No matching share ID or player found")
                        .formatted(Formatting.RED));
                    return 0;
                })
            );
    }

    private static Text getShareItemName(ShareEntry share) {
        return switch (share.getType()) {
            case ITEM -> share.getInventory().getName();
            case INVENTORY -> Text.translatable("itemGroup.inventory");
            case HOTBAR -> Text.translatable("itemGroup.hotbar");
            case ENDER_CHEST -> Text.translatable("container.enderchest");
            case CONTAINER -> {
                Text name = share.getInventory().getName();
                yield name != null ? name : Text.literal("Container");
            }
        };
    }

    private static String formatTime(long timestamp) {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(timestamp));
    }

    private static boolean hasPermission(ServerCommandSource source, String permission) {
        try {
            Class<?> luckPermsApi = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = luckPermsApi.getMethod("get").invoke(null);
            Object user = api.getClass().getMethod("getUser", java.util.UUID.class)
                .invoke(api, Objects.requireNonNull(source.getPlayer()).getUuid());
            if (user != null) {
                Object result = user.getClass().getMethod("hasPermission", String.class)
                    .invoke(user, permission);
                if (result instanceof Boolean) {
                    return (boolean) result;
                }
            }
        } catch (Exception e) {
            return source.hasPermissionLevel(4);
        }
        return source.hasPermissionLevel(4);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createReloadCommand() {
        return literal("reload")
            .requires(source -> hasPermission(source, "showcase.manage.reload"))
            .executes(ctx -> {
                try {
                    ShowcaseMod.CONFIG = ModConfig.load();
                    ctx.getSource().sendMessage(Text.literal("Showcase config reloaded successfully!").formatted(Formatting.GREEN));
                    return Command.SINGLE_SUCCESS;
                } catch (Exception e) {
                    ShowcaseMod.LOGGER.error("Failed to reload config", e);
                    ctx.getSource().sendError(Text.literal("Failed to reload config!"));
                    return 0;
                }
            });
    }
}