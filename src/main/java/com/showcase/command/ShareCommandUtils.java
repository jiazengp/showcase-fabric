package com.showcase.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.showcase.api.ShowcaseAPI;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.gui.MerchantContext;
import com.showcase.utils.PlayerUtils;
import com.showcase.utils.StackUtils;
import com.showcase.utils.TextUtils;
import com.showcase.utils.compat.ServerPlayerCompat;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import com.showcase.utils.TextEventFactory;
import org.jetbrains.annotations.ApiStatus;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.showcase.command.ShowcaseCommand.*;
import static com.showcase.command.ShowcaseManager.ShareType.*;
import static com.showcase.command.ShowcaseManager.getItemStackWithID;
import static net.minecraft.network.chat.Component.translatable;

public class ShareCommandUtils {
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("MM/dd HH:mm:ss");

    public static Collection<ServerPlayer> getReceivers(CommandContext<CommandSourceStack> ctx) {
        try {
            Collection<ServerPlayer> receivers = EntityArgument.getPlayers(ctx, RECEIVERS_ARG);
            if (receivers.isEmpty()) return null;
            if (isAtAll(ctx, receivers)) return null;
            return receivers;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if player has permission to specify receivers for the given share type
     */
    public static boolean canSpecifyReceivers(ServerPlayer player, ShowcaseManager.ShareType shareType) {
        Permissions.CommandType commandType = Permissions.getCommandTypeFromShareType(shareType);
        return PermissionChecker.hasPermission(player, commandType.receivers(), 2);
    }

    /**
     * Check if player has permission to specify duration for the given share type
     */
    public static boolean canSpecifyDuration(ServerPlayer player, ShowcaseManager.ShareType shareType) {
        Permissions.CommandType commandType = Permissions.getCommandTypeFromShareType(shareType);
        return PermissionChecker.hasPermission(player, commandType.duration(), 2);
    }

    /**
     * Check if player has permission to specify description for the given share type
     */
    public static boolean canSpecifyDescription(ServerPlayer player, ShowcaseManager.ShareType shareType) {
        Permissions.CommandType commandType = Permissions.getCommandTypeFromShareType(shareType);
        return PermissionChecker.hasPermission(player, commandType.description(), 1);
    }

    public static ServerPlayer getSenderPlayer(CommandContext<CommandSourceStack> ctx) {
        try {
            return ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            return null;
        }
    }

    public static ServerPlayer getSourcePlayer(CommandContext<CommandSourceStack> ctx) {
        try {
            return EntityArgument.getPlayer(ctx, SOURCE_ARG);
        } catch (Exception e) {
            return null;
        }
    }

    public static ServerPlayer getSourceOrSender(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer sourcePlayer = getSourcePlayer(ctx);
        if (sourcePlayer != null) return sourcePlayer;
        return getSenderPlayer(ctx);
    }

    public static String getDescription(CommandContext<CommandSourceStack> ctx) {
        try {
            return StringArgumentType.getString(ctx, DESCRIPTION_ARG);
        } catch (Exception e) {
            return null;
        }
    }

    public static MutableComponent createContainerPreview(ShareEntry shareEntry) {
        if (shareEntry == null) return null;

        Container inv = shareEntry.getInventory();
        Map<Component, Integer> counts = new HashMap<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && !StackUtils.isDivider(s)) {
                counts.merge(s.getHoverName(), s.getCount(), Integer::sum);
            }
        }

        if (counts.isEmpty()) {
            return translatable("item.minecraft.bundle.empty");
        }

        int maxLines = 5;
        MutableComponent out = Component.literal("");
        counts.entrySet().stream()
                .sorted(Map.Entry.<Component, Integer>comparingByValue().reversed())
                .limit(maxLines)
                .forEach(e -> out.append(e.getKey()).append("×" + e.getValue() + "\n"));

        if (counts.size() > maxLines) {
            out.append(translatable("showcase.preview_text.more", counts.size() - maxLines)).append("\n");
        }
        return out;
    }

    private static boolean isAtAll(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> receivers) {
        String input = ctx.getInput();
        int online = ctx.getSource().getServer().getPlayerList().getPlayers().size();
        return input.contains("@a") && receivers.size() == online;
    }

    private static MutableComponent getFinalPreviewText(Component itemName, ShowcaseManager.ShareType type, String shareId) {
        ShareEntry shareEntry = ShowcaseManager.getShareEntry(shareId);
        MutableComponent sharePreview = shareEntry.getType() == MERCHANT ?
                createMerchantPreviewText(shareEntry.getMerchantContext()) : createContainerPreview(shareEntry);

        MutableComponent preview = Component.literal("")
                .append(itemName)
                .withStyle(getFormattingColorForType(type, type == ITEM ? getItemStackWithID(shareId) : null))
                .append("\n");

        if (sharePreview != null) {
            preview.append(sharePreview)
                    .append(translatable("showcase.message.click_to_view"))
                    .append("\n\n")
                    .append(TextUtils.BADGE)
                    .withStyle(ChatFormatting.GRAY);
        } else {
            preview.append(TextUtils.EMPTY);
        }

        return preview;
    }

    private static MutableComponent createMerchantPreviewText(MerchantContext merchantContext) {
        MutableComponent preview = Component.literal("");

        if (merchantContext.getExperience() > 0) {
            preview.append(Component.translatable("argument.entity.options.level.description", merchantContext.getExperience()))
                    .withStyle(ChatFormatting.GREEN)
                    .append("\n");
        }

        MerchantOffers offers = merchantContext.getOffers();
        if (offers != null && !offers.isEmpty()) {
            preview.append(Component.translatable("merchant.trades", offers.size()))
                    .withStyle(ChatFormatting.AQUA)
                    .append("\n");

            int maxPreview = Math.min(5, offers.size());
            for (int i = 0; i < maxPreview; i++) {
                MerchantOffer offer = offers.get(i);
                if (offer != null) {
                    preview.append("• ")
                            .append(offer.getResult().getHoverName())
                            .withStyle(ChatFormatting.YELLOW);

                    if (i < maxPreview - 1) {
                        preview.append("\n");
                    }
                }
            }

            if (offers.size() > maxPreview) {
                preview.append("\n")
                        .append(translatable("showcase.preview_text.more_trades", offers.size() - maxPreview))
                        .withStyle(ChatFormatting.GRAY)
                        .append("\n");
            }
        }

        return preview;
    }

    public static MutableComponent createClickableItemName(ShowcaseManager.ShareType type, Component itemName, String shareId) {
        MutableComponent hoverableName = createClickableTag(itemName, type, shareId);

        if (type == ITEM || type == STATS) {
            ItemStack stack = ShowcaseManager.getItemStackWithID(shareId);

            if (stack != null && !stack.isEmpty()) {
                hoverableName.setStyle(hoverableName.getStyle()
                        .withHoverEvent(TextEventFactory.showItem(stack)));
            }
            return hoverableName;
        }

        MutableComponent preview = getFinalPreviewText(itemName, type, shareId);
        hoverableName.setStyle(hoverableName.getStyle()
                .withHoverEvent(TextEventFactory.showText(preview)));

        return hoverableName;
    }

    public static void sendShareMessage(ServerPlayer sender, ServerPlayer sourcePlayer,  Collection<ServerPlayer> receivers,
                                        String description, ShowcaseManager.ShareType type, Component itemName, Integer duration, String shareId) {
        // Get the share entry for the event
        ShareEntry shareEntry = ShowcaseManager.getShareEntry(shareId);
        if (shareEntry != null) {
            // Fire the showcase created event
            ShowcaseAPI.fireShowcaseCreatedEvent(type, shareEntry, sender, sourcePlayer, receivers, shareId, description, duration);
        }

        MutableComponent clickableItemName = createClickableItemName(type, itemName, shareId);

        MutableComponent message = buildShareMessage(sender, sourcePlayer, receivers, description, type, clickableItemName);
        MutableComponent finalMessage = message.append("\n").append(TextUtils.info(translatable("showcase.message.expiry_notice", (duration == null ? ModConfigManager.getShareLinkDefaultExpiry() : duration) / 60)));

        if (receivers != null) {
            for (ServerPlayer receiver : receivers) {
                receiver.sendSystemMessage(message);
            }

            String receiverList = receivers.stream().map(ServerPlayer::getDisplayName).filter(Objects::nonNull).map(Component::getString).collect(Collectors.joining(", "));
            sender.sendSystemMessage(translatable("showcase.message.private_share_tip", sourcePlayer.getDisplayName(), clickableItemName, Component.literal(receiverList)));
        } else {
            MinecraftServer server = ServerPlayerCompat.getServer(sender);
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(finalMessage, false);
            }
        }
    }

    private static MutableComponent buildShareMessage(ServerPlayer sender, ServerPlayer sourcePlayer,
                                                 Collection<ServerPlayer> receivers, String description,
                                                 ShowcaseManager.ShareType type, MutableComponent clickableItemName) {
        if (description != null) {
            return buildPlayerDescriptionMessage(sourcePlayer, description, clickableItemName);
        }

        boolean isPrivateMessage = receivers != null && sender.getUUID().equals(sourcePlayer.getUUID());

        return isPrivateMessage ?
                buildDefaultMessageForReceiver(sender, type, clickableItemName) :
                buildDefaultMessageForBroadcast(sender, sourcePlayer, type, clickableItemName);
    }

    public static MutableComponent buildPlayerDescriptionMessage(ServerPlayer player, String description, MutableComponent clickableItemName) {
        return translatable("showcase.message.player_description",
                PlayerUtils.getSafeDisplayName(player), description, clickableItemName);
    }

    public static ClickEvent createShareClickEvent(String id) {
        return TextEventFactory.runFullCommand("/" + VIEW_COMMAND + " " + id);
    }

    public static MutableComponent buildDefaultMessageForReceiver(ServerPlayer sender,
                                                             ShowcaseManager.ShareType type, MutableComponent clickableItemName) {
        MutableComponent senderPlayerDisplayName = PlayerUtils.getSafeDisplayName(sender);

        return switch (type) {
            case ITEM -> translatable("showcase.message.default.item", senderPlayerDisplayName, clickableItemName);
            case STATS -> translatable("showcase.message.default.stats", senderPlayerDisplayName, clickableItemName);
            case INVENTORY -> translatable("showcase.message.default.inventory", senderPlayerDisplayName, clickableItemName);
            case ENDER_CHEST -> translatable("showcase.message.default.ender_chest", senderPlayerDisplayName, clickableItemName);
            case HOTBAR -> translatable("showcase.message.default.hotbar", senderPlayerDisplayName, clickableItemName);
            case CONTAINER -> translatable("showcase.message.default.container", senderPlayerDisplayName, clickableItemName);
            case MERCHANT -> translatable("showcase.message.default.merchant", senderPlayerDisplayName, clickableItemName);
        };
    }

    public static MutableComponent buildDefaultMessageForBroadcast(ServerPlayer sender, ServerPlayer sourcePlayer,
                                                              ShowcaseManager.ShareType type, MutableComponent clickableItemName) {
        boolean isSelf = sender.getUUID().equals(sourcePlayer.getUUID());
        MutableComponent senderPlayerDisplayName = PlayerUtils.getSafeDisplayName(sender);
        MutableComponent sourcePlayerDisplayName = PlayerUtils.getSafeDisplayName(sourcePlayer);

        return switch (type) {
            case ITEM -> isSelf ?
                    translatable("showcase.message.item_shared", senderPlayerDisplayName, clickableItemName) :
                    translatable("showcase.message.other_item_shared", senderPlayerDisplayName, sourcePlayerDisplayName, clickableItemName);
            case STATS -> isSelf ?
                    translatable("showcase.message.stats_shared", senderPlayerDisplayName, clickableItemName) :
                    translatable("showcase.message.other_stats_shared", senderPlayerDisplayName, sourcePlayerDisplayName, clickableItemName);
            case CONTAINER -> isSelf ?
                    translatable("showcase.message.container_shared", senderPlayerDisplayName, clickableItemName) :
                    translatable("showcase.message.other_container_shared", senderPlayerDisplayName, sourcePlayerDisplayName, clickableItemName);
            case MERCHANT -> isSelf ?
                    translatable("showcase.message.merchant_shared", senderPlayerDisplayName, clickableItemName) :
                    translatable("showcase.message.other_merchant_shared", senderPlayerDisplayName, sourcePlayerDisplayName, clickableItemName);
            case INVENTORY -> isSelf ?
                    translatable("showcase.message.inventory_shared", senderPlayerDisplayName, clickableItemName) :
                    translatable("showcase.message.other_inventory_shared", senderPlayerDisplayName, sourcePlayerDisplayName, clickableItemName);
            case ENDER_CHEST -> isSelf ?
                    translatable("showcase.message.ender_chest_shared", senderPlayerDisplayName, clickableItemName) :
                    translatable("showcase.message.other_ender_chest_shared", senderPlayerDisplayName, sourcePlayerDisplayName, clickableItemName);
            case HOTBAR -> isSelf ?
                    translatable("showcase.message.hotbar_shared", senderPlayerDisplayName, clickableItemName) :
                    translatable("showcase.message.other_hotbar_shared", senderPlayerDisplayName, sourcePlayerDisplayName, clickableItemName);
        };
    }

    public static ChatFormatting getFormattingColorForType(ShowcaseManager.ShareType type, ItemStack itemStack) {
        if (itemStack != null && !itemStack.isEmpty()) {
            if (isSpecialItem(itemStack)) {
                return ChatFormatting.GOLD;
            }

            return switch (itemStack.getRarity()) {
                case COMMON -> ChatFormatting.WHITE;
                case UNCOMMON -> ChatFormatting.YELLOW;
                case RARE -> ChatFormatting.AQUA;
                case EPIC -> ChatFormatting.LIGHT_PURPLE;
            };
        }

        return switch (type) {
            case ITEM -> ChatFormatting.BLUE;
            case STATS -> ChatFormatting.DARK_AQUA;
            case INVENTORY -> ChatFormatting.GREEN;
            case ENDER_CHEST -> ChatFormatting.DARK_PURPLE;
            case HOTBAR -> ChatFormatting.YELLOW;
            case CONTAINER, MERCHANT -> ChatFormatting.GOLD;
        };
    }

    private static boolean isSpecialItem(ItemStack itemStack) {
        return StackUtils.isMap(itemStack) ||
                itemStack.is(Items.BOOK) ||
                itemStack.is(Items.WRITABLE_BOOK) ||
                itemStack.is(Items.WRITTEN_BOOK) ||
                itemStack.is(Items.ENCHANTED_BOOK);
    }

    public static MutableComponent createClickableTag(Component name, ShowcaseManager.ShareType type, String id) {
        ItemStack stack = getItemStackWithID(id);
        ChatFormatting color = getFormattingColorForType(type, type == ITEM ? stack : null);
        Component displayName = (type == ITEM  || type == STATS) ? name : Component.literal("[").append(name).append("]");

        return Component.literal("")
                .append(displayName)
                .withStyle(style -> style
                        .withColor(color)
                        .withUnderlined(true)
                        .withClickEvent(createShareClickEvent(id)));
    }

    public static MutableComponent buildShareLine(ServerPlayer viewer, String shareId, ShareEntry share) {
        MinecraftServer server = ServerPlayerCompat.getServer(viewer);

        Component ownerName =  PlayerUtils.getSafeDisplayName(server, share.getOwnerUuid());
        Component itemName = getShareItemName(share);

        return Component.literal("")
                .append(Component.literal("▶ ").withStyle(ChatFormatting.DARK_GRAY))
                .append(buildIdSection(shareId))
                .append(buildOwnerSection(ownerName))
                .append(buildShareSection(share, itemName, shareId))
                .append(buildViewsSection(share.getViewCount()))
                .append(buildTimeSection(share))
                .append(buildCancelSection(shareId))
                .append(Component.literal("\n\n"));
    }

    private static MutableComponent buildIdSection(String shareId) {
        return Component.literal("")
                .append(Component.literal("[ID] ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(shareId).withStyle(ChatFormatting.YELLOW)
                        .withStyle(style -> style
                                .withClickEvent(TextEventFactory.copyToClipboard(shareId))
                                .withHoverEvent(TextEventFactory.showText("Copy ID"))))
                .append(Component.literal("  "));
    }

    private static MutableComponent buildOwnerSection(Component ownerName) {
        return Component.literal("")
                .append(Component.literal("[Owner] ").withStyle(ChatFormatting.GRAY))
                .append(ownerName.copy().withStyle(ChatFormatting.GREEN))
                .append(Component.literal("  "));
    }

    private static MutableComponent buildShareSection(ShareEntry share,
                                                 Component itemName, String shareId) {
        return Component.literal("")
                .append(Component.literal("[Share] ").withStyle(ChatFormatting.GRAY))
                .append(createClickableItemName(share.getType(), itemName, shareId))
                .append(Component.literal("  "));
    }

    private static MutableComponent buildViewsSection(int viewCount) {
        return Component.literal("")
                .append(Component.literal("[Views] ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(viewCount)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("  "));
    }

    private static MutableComponent buildTimeSection(ShareEntry share) {
        long expiryTime = share.getTimestamp() + share.getDuration() * 1000L;

        return Component.literal("")
                .append(Component.literal("\n    "))
                .append(Component.literal("Created: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(formatTime(share.getTimestamp())).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" | Expires: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(formatTime(expiryTime)).withStyle(ChatFormatting.WHITE));
    }

    private static MutableComponent buildCancelSection(String shareId) {
        return Component.literal("")
                .append(Component.literal(" | [Cancel Share] ")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.RED)
                                .withClickEvent(TextEventFactory.runFullCommand("/" + MANAGE_COMMAND + " " + CANCEL_COMMAND + " " + shareId))
                                .withHoverEvent(TextEventFactory.cancelShareTooltip())))
                .append(Component.literal("  "));
    }

    private static Component getShareItemName(ShareEntry share) {
        return switch (share.getType()) {
            case ITEM, STATS -> share.getInventory().getName();
            case INVENTORY -> TextUtils.INVENTORY;
            case HOTBAR -> TextUtils.HOTBAR;
            case ENDER_CHEST -> TextUtils.ENDER_CHEST;
            case MERCHANT -> {
                Component name = share.getMerchantContext().getDisplayName();
                yield name != null ? name : TextUtils.CONTAINER;
            }
            case CONTAINER -> {
                Component name = share.getInventory().getName();
                yield name != null ? name : TextUtils.CONTAINER;
            }
        };
    }

    private static String formatTime(long timestamp) {
        return TIME_FORMATTER.format(new Date(timestamp));
    }

    public static List<String> getPlayerNameCompletions(CommandContext<CommandSourceStack> ctx) {
        List<String> names = new ArrayList<>();
        MinecraftServer server = ctx.getSource().getServer();

        if (server != null) {
            server.getPlayerList()
                    .getPlayers()
                    .forEach(player -> names.add(player.getName().getString()));
        }

        return names;
    }

    public static @ApiStatus.Internal void handleError(CommandContext<CommandSourceStack> context, Throwable e)
    {
        //handle errors
        if(e instanceof Error)
            throw new Error("Unable to handle errors.", e);

        //handle command syntax errors
        context.getSource().sendFailure(
                translatable("command.failed")
                        .append(":\n    " + e.getMessage()));
    }
}
