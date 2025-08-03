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
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import com.showcase.utils.TextEventFactory;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.ApiStatus;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.showcase.command.ShowcaseCommand.*;
import static com.showcase.command.ShowcaseManager.ShareType.*;
import static com.showcase.command.ShowcaseManager.getItemStackWithID;
import static net.minecraft.text.Text.translatable;

public class ShareCommandUtils {
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("MM/dd HH:mm:ss");

    public static Collection<ServerPlayerEntity> getReceivers(CommandContext<ServerCommandSource> ctx) {
        try {
            Collection<ServerPlayerEntity> receivers = EntityArgumentType.getPlayers(ctx, RECEIVERS_ARG);
            if (receivers.isEmpty()) return null;
            if (isAtAll(ctx, receivers)) return null;
            return receivers;
        } catch (Exception e) {
            return null;
        }
    }

    public static ServerPlayerEntity getSenderPlayer(CommandContext<ServerCommandSource> ctx) {
        return ctx.getSource().getPlayer();
    }

    public static ServerPlayerEntity getSourcePlayer(CommandContext<ServerCommandSource> ctx) {
        try {
            return EntityArgumentType.getPlayer(ctx, SOURCE_ARG);
        } catch (Exception e) {
            return null;
        }
    }

    public static ServerPlayerEntity getSourceOrSender(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity sourcePlayer = getSourcePlayer(ctx);
        if (sourcePlayer != null) return sourcePlayer;
        return getSenderPlayer(ctx);
    }

    public static String getDescription(CommandContext<ServerCommandSource> ctx) {
        try {
            return StringArgumentType.getString(ctx, DESCRIPTION_ARG);
        } catch (Exception e) {
            return null;
        }
    }

    public static MutableText createContainerPreview(ShareEntry shareEntry) {
        if (shareEntry == null) return null;

        Inventory inv = shareEntry.getInventory();
        Map<Text, Integer> counts = new HashMap<>();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && !StackUtils.isDivider(s)) {
                counts.merge(s.getName(), s.getCount(), Integer::sum);
            }
        }

        if (counts.isEmpty()) {
            return translatable("item.minecraft.bundle.empty");
        }

        int maxLines = 5;
        MutableText out = Text.literal("");
        counts.entrySet().stream()
                .sorted(Map.Entry.<Text, Integer>comparingByValue().reversed())
                .limit(maxLines)
                .forEach(e -> out.append(e.getKey()).append("×" + e.getValue() + "\n"));

        if (counts.size() > maxLines) {
            out.append(translatable("showcase.preview_text.more", counts.size() - maxLines)).append("\n");
        }
        return out;
    }

    private static boolean isAtAll(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> receivers) {
        String input = ctx.getInput();
        int online = ctx.getSource().getServer().getPlayerManager().getPlayerList().size();
        return input.contains("@a") && receivers.size() == online;
    }

    private static MutableText getFinalPreviewText(Text itemName, ShowcaseManager.ShareType type, String shareId) {
        ShareEntry shareEntry = ShowcaseManager.getShareEntry(shareId);
        MutableText sharePreview = shareEntry.getType() == MERCHANT ?
                createMerchantPreviewText(shareEntry.getMerchantContext()) : createContainerPreview(shareEntry);

        MutableText preview = Text.literal("")
                .append(itemName)
                .formatted(getFormattingColorForType(type, type == ITEM ? getItemStackWithID(shareId) : null))
                .append("\n");

        if (sharePreview != null) {
            preview.append(sharePreview)
                    .append(translatable("showcase.message.click_to_view"))
                    .append("\n\n")
                    .append(TextUtils.BADGE)
                    .formatted(Formatting.GRAY);
        } else {
            preview.append(TextUtils.EMPTY);
        }

        return preview;
    }

    private static MutableText createMerchantPreviewText(MerchantContext merchantContext) {
        MutableText preview = Text.literal("");

        if (merchantContext.getExperience() > 0) {
            preview.append(Text.translatable("argument.entity.options.level.description", merchantContext.getExperience()))
                    .formatted(Formatting.GREEN)
                    .append("\n");
        }

        TradeOfferList offers = merchantContext.getOffers();
        if (offers != null && !offers.isEmpty()) {
            preview.append(Text.translatable("merchant.trades", offers.size()))
                    .formatted(Formatting.AQUA)
                    .append("\n");

            int maxPreview = Math.min(5, offers.size());
            for (int i = 0; i < maxPreview; i++) {
                TradeOffer offer = offers.get(i);
                if (offer != null) {
                    preview.append("• ")
                            .append(offer.getSellItem().getName())
                            .formatted(Formatting.YELLOW);

                    if (i < maxPreview - 1) {
                        preview.append("\n");
                    }
                }
            }

            if (offers.size() > maxPreview) {
                preview.append("\n")
                        .append(translatable("showcase.preview_text.more_trades", offers.size() - maxPreview))
                        .formatted(Formatting.GRAY)
                        .append("\n");
            }
        }

        return preview;
    }

    public static MutableText createClickableItemName(ShowcaseManager.ShareType type, Text itemName, String shareId) {
        MutableText hoverableName = createClickableTag(itemName, type, shareId);

        if (type == ITEM || type == STATS) {
            ItemStack stack = ShowcaseManager.getItemStackWithID(shareId);

            if (stack != null && !stack.isEmpty()) {
                hoverableName.setStyle(hoverableName.getStyle()
                        .withHoverEvent(TextEventFactory.showItem(stack)));
            }
            return hoverableName;
        }

        MutableText preview = getFinalPreviewText(itemName, type, shareId);
        hoverableName.setStyle(hoverableName.getStyle()
                .withHoverEvent(TextEventFactory.showText(preview)));

        return hoverableName;
    }

    public static void sendShareMessage(ServerPlayerEntity sender, ServerPlayerEntity sourcePlayer,  Collection<ServerPlayerEntity> receivers,
                                        String description, ShowcaseManager.ShareType type, Text itemName, Integer duration, String shareId) {
        // Get the share entry for the event
        ShareEntry shareEntry = ShowcaseManager.getShareEntry(shareId);
        if (shareEntry != null) {
            // Fire the showcase created event
            ShowcaseAPI.fireShowcaseCreatedEvent(type, shareEntry, sender, sourcePlayer, receivers, shareId, description, duration);
        }

        MutableText clickableItemName = createClickableItemName(type, itemName, shareId);

        MutableText message = buildShareMessage(sender, sourcePlayer, receivers, description, type, clickableItemName);
        MutableText finalMessage = message.append("\n").append(TextUtils.info(translatable("showcase.message.expiry_notice", (duration == null ? ModConfigManager.getShareLinkDefaultExpiry() : duration) / 60)));

        if (receivers != null) {
            for (ServerPlayerEntity receiver : receivers) {
                receiver.sendMessage(message);
            }

            String receiverList = receivers.stream().map(ServerPlayerEntity::getDisplayName).filter(Objects::nonNull).map(Text::getString).collect(Collectors.joining(", "));
            sender.sendMessage(translatable("showcase.message.private_share_tip", sourcePlayer.getDisplayName(), clickableItemName, Text.literal(receiverList)));
        } else {
            MinecraftServer server = sender.getServer();
            if (server != null) {
                server.getPlayerManager().broadcast(finalMessage, false);
            }
        }
    }

    private static MutableText buildShareMessage(ServerPlayerEntity sender, ServerPlayerEntity sourcePlayer,
                                                 Collection<ServerPlayerEntity> receivers, String description,
                                                 ShowcaseManager.ShareType type, MutableText clickableItemName) {
        if (description != null) {
            return buildPlayerDescriptionMessage(sourcePlayer, description, clickableItemName);
        }

        boolean isPrivateMessage = receivers != null && sender.getUuid().equals(sourcePlayer.getUuid());

        return isPrivateMessage ?
                buildDefaultMessageForReceiver(sender, type, clickableItemName) :
                buildDefaultMessageForBroadcast(sender, sourcePlayer, type, clickableItemName);
    }

    public static MutableText buildPlayerDescriptionMessage(ServerPlayerEntity player, String description, MutableText clickableItemName) {
        return translatable("showcase.message.player_description",
                PlayerUtils.getSafeDisplayName(player), description, clickableItemName);
    }

    public static ClickEvent createShareClickEvent(String id) {
        return TextEventFactory.runFullCommand("/" + VIEW_COMMAND + " " + id);
    }

    public static MutableText buildDefaultMessageForReceiver(ServerPlayerEntity sender,
                                                             ShowcaseManager.ShareType type, MutableText clickableItemName) {
        MutableText senderPlayerDisplayName = PlayerUtils.getSafeDisplayName(sender);

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

    public static MutableText buildDefaultMessageForBroadcast(ServerPlayerEntity sender, ServerPlayerEntity sourcePlayer,
                                                              ShowcaseManager.ShareType type, MutableText clickableItemName) {
        boolean isSelf = sender.getUuid().equals(sourcePlayer.getUuid());
        MutableText senderPlayerDisplayName = PlayerUtils.getSafeDisplayName(sender);
        MutableText sourcePlayerDisplayName = PlayerUtils.getSafeDisplayName(sourcePlayer);

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

    public static Formatting getFormattingColorForType(ShowcaseManager.ShareType type, ItemStack itemStack) {
        if (itemStack != null && !itemStack.isEmpty()) {
            if (isSpecialItem(itemStack)) {
                return Formatting.GOLD;
            }

            return switch (itemStack.getRarity()) {
                case COMMON -> Formatting.WHITE;
                case UNCOMMON -> Formatting.YELLOW;
                case RARE -> Formatting.AQUA;
                case EPIC -> Formatting.LIGHT_PURPLE;
            };
        }

        return switch (type) {
            case ITEM -> Formatting.BLUE;
            case STATS -> Formatting.DARK_AQUA;
            case INVENTORY -> Formatting.GREEN;
            case ENDER_CHEST -> Formatting.DARK_PURPLE;
            case HOTBAR -> Formatting.YELLOW;
            case CONTAINER, MERCHANT -> Formatting.GOLD;
        };
    }

    private static boolean isSpecialItem(ItemStack itemStack) {
        return StackUtils.isMap(itemStack) ||
                itemStack.isOf(Items.BOOK) ||
                itemStack.isOf(Items.WRITABLE_BOOK) ||
                itemStack.isOf(Items.WRITTEN_BOOK) ||
                itemStack.isOf(Items.ENCHANTED_BOOK);
    }

    public static MutableText createClickableTag(Text name, ShowcaseManager.ShareType type, String id) {
        ItemStack stack = getItemStackWithID(id);
        Formatting color = getFormattingColorForType(type, type == ITEM ? stack : null);
        Text displayName = (type == ITEM  || type == STATS) ? name : Text.literal("[").append(name).append("]");

        return Text.literal("")
                .append(displayName)
                .styled(style -> style
                        .withColor(color)
                        .withUnderline(true)
                        .withClickEvent(createShareClickEvent(id)));
    }

    public static MutableText buildShareLine(ServerPlayerEntity viewer, String shareId, ShareEntry share) {
        MinecraftServer server = viewer.getServer();

        if (server == null) return Text.literal("Server not available");

        Text ownerName =  PlayerUtils.getSafeDisplayName(server, share.getOwnerUuid());
        Text itemName = getShareItemName(share);

        return Text.literal("")
                .append(Text.literal("▶ ").formatted(Formatting.DARK_GRAY))
                .append(buildIdSection(shareId))
                .append(buildOwnerSection(ownerName))
                .append(buildShareSection(share, itemName, shareId))
                .append(buildViewsSection(share.getViewCount()))
                .append(buildTimeSection(share))
                .append(buildCancelSection(shareId))
                .append(Text.literal("\n\n"));
    }

    private static MutableText buildIdSection(String shareId) {
        return Text.literal("")
                .append(Text.literal("[ID] ").formatted(Formatting.GRAY))
                .append(Text.literal(shareId).formatted(Formatting.YELLOW)
                        .styled(style -> style
                                .withClickEvent(TextEventFactory.copyToClipboard(shareId))
                                .withHoverEvent(TextEventFactory.copyIdTooltip())))
                .append(Text.literal("  "));
    }

    private static MutableText buildOwnerSection(Text ownerName) {
        return Text.literal("")
                .append(Text.literal("[Owner] ").formatted(Formatting.GRAY))
                .append(ownerName.copy().formatted(Formatting.GREEN))
                .append(Text.literal("  "));
    }

    private static MutableText buildShareSection(ShareEntry share,
                                                 Text itemName, String shareId) {
        return Text.literal("")
                .append(Text.literal("[Share] ").formatted(Formatting.GRAY))
                .append(createClickableItemName(share.getType(), itemName, shareId))
                .append(Text.literal("  "));
    }

    private static MutableText buildViewsSection(int viewCount) {
        return Text.literal("")
                .append(Text.literal("[Views] ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(viewCount)).formatted(Formatting.AQUA))
                .append(Text.literal("  "));
    }

    private static MutableText buildTimeSection(ShareEntry share) {
        long expiryTime = share.getTimestamp() + share.getDuration() * 1000L;

        return Text.literal("")
                .append(Text.literal("\n    "))
                .append(Text.literal("Created: ").formatted(Formatting.GRAY))
                .append(Text.literal(formatTime(share.getTimestamp())).formatted(Formatting.WHITE))
                .append(Text.literal(" | Expires: ").formatted(Formatting.GRAY))
                .append(Text.literal(formatTime(expiryTime)).formatted(Formatting.WHITE));
    }

    private static MutableText buildCancelSection(String shareId) {
        return Text.literal("")
                .append(Text.literal(" | [Cancel Share] ")
                        .styled(style -> style
                                .withColor(Formatting.RED)
                                .withClickEvent(TextEventFactory.runFullCommand("/" + MANAGE_COMMAND + " " + CANCEL_COMMAND + " " + shareId))
                                .withHoverEvent(TextEventFactory.cancelShareTooltip())))
                .append(Text.literal("  "));
    }

    private static Text getShareItemName(ShareEntry share) {
        return switch (share.getType()) {
            case ITEM, STATS -> share.getInventory().getName();
            case INVENTORY -> TextUtils.INVENTORY;
            case HOTBAR -> TextUtils.HOTBAR;
            case ENDER_CHEST -> TextUtils.ENDER_CHEST;
            case MERCHANT -> {
                Text name = share.getMerchantContext().getDisplayName();
                yield name != null ? name : TextUtils.CONTAINER;
            }
            case CONTAINER -> {
                Text name = share.getInventory().getName();
                yield name != null ? name : TextUtils.CONTAINER;
            }
        };
    }

    private static String formatTime(long timestamp) {
        return TIME_FORMATTER.format(new Date(timestamp));
    }

    public static List<String> getPlayerNameCompletions(CommandContext<ServerCommandSource> ctx) {
        List<String> names = new ArrayList<>();
        MinecraftServer server = ctx.getSource().getServer();

        if (server != null) {
            server.getPlayerManager()
                    .getPlayerList()
                    .forEach(player -> names.add(player.getName().getString()));
        }

        return names;
    }

    public static @ApiStatus.Internal void handleError(CommandContext<ServerCommandSource> context, Throwable e)
    {
        //handle errors
        if(e instanceof Error)
            throw new Error("Unable to handle errors.", e);

        //handle command syntax errors
        context.getSource().sendError(
                translatable("command.failed")
                        .append(":\n    " + e.getMessage()));
    }
}