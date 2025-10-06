package com.showcase.placeholders;

import com.showcase.command.ShareCommandUtils;
import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import com.showcase.utils.ShareConstants;
import com.showcase.utils.StackUtils;
import com.showcase.utils.TextUtils;
import com.showcase.utils.stats.StatUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.arguments.SimpleArguments;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static com.showcase.utils.permissions.PermissionChecker.isOp;

public class Placeholders {
    public static final Identifier INVENTORY = ShareConstants.PlaceholderIds.INVENTORY;
    public static final Identifier HOTBAR = ShareConstants.PlaceholderIds.HOTBAR;
    public static final Identifier ITEM = ShareConstants.PlaceholderIds.ITEM;
    public static final Identifier ENDER_CHEST = ShareConstants.PlaceholderIds.ENDER_CHEST;
    public static final Identifier STATS = ShareConstants.PlaceholderIds.STATS;

    private static final String NO_PERMISSION = "You don't have permission to use this placeholder!";
    private static final String ON_COOLDOWN = "You have reached the usage limit. Please try again later.";
    private static final String INVALID_DURATION = "Invalid valid duration";
    private static final String NO_PLAYER = "No valid player";

    private record ShareData(ServerPlayerEntity player, int duration) {}

    private static void registerPlaceholder(Identifier identifier, ShowcaseManager.ShareType shareType, 
                                          String permission, Function<ShareData, MutableText> shareCreator) {
        eu.pb4.placeholders.api.Placeholders.register(identifier, (ctx, arg) -> {
            try {
                PlaceholderResult validation = validatePlaceholderRequest(ctx, arg, shareType, permission);
                if (validation != null) return validation;

                ServerPlayerEntity player = ctx.player();
                int duration = getDurationFromSimpleArguments(arg, player);
                
                MutableText result = shareCreator.apply(new ShareData(player, duration));
                ShowcaseManager.setCooldown(player, shareType);
                
                return PlaceholderResult.value(result);
            } catch (Exception e) {
                return PlaceholderResult.invalid(e.getMessage());
            }
        });
    }

    private static PlaceholderResult validatePlaceholderRequest(PlaceholderContext ctx, String arg, 
                                                              ShowcaseManager.ShareType shareType, String permission) {
        ServerPlayerEntity player = ctx.player();
        if (player == null) return PlaceholderResult.invalid(NO_PLAYER);
        
        int duration = getDurationFromSimpleArguments(arg, player);
        
        var settings = ModConfigManager.getShareSettings(shareType);
        if (settings == null || !PermissionChecker.hasPermission(player, permission, settings.defaultPermission))
            return PlaceholderResult.invalid(NO_PERMISSION);
        
        if (ShowcaseManager.isOnCooldown(player, shareType))
            return PlaceholderResult.invalid(ON_COOLDOWN);
        
        return null;
    }

    public static void registerPlaceholders() {
        // Register original share placeholders
        registerPlaceholder(INVENTORY, ShowcaseManager.ShareType.INVENTORY, Permissions.Chat.Placeholder.INVENTORY,
                (shareData) -> ShareCommandUtils.createClickableItemName(
                        ShowcaseManager.ShareType.INVENTORY,
                        TextUtils.INVENTORY,
                        ShowcaseManager.createInventoryShare(shareData.player, shareData.duration, null)
                ));

        registerPlaceholder(HOTBAR, ShowcaseManager.ShareType.HOTBAR, Permissions.Chat.Placeholder.HOTBAR,
                (shareData) -> ShareCommandUtils.createClickableItemName(
                        ShowcaseManager.ShareType.HOTBAR,
                        TextUtils.HOTBAR,
                        ShowcaseManager.createHotbarShare(shareData.player, shareData.duration, null)
                ));

        registerPlaceholder(ENDER_CHEST, ShowcaseManager.ShareType.ENDER_CHEST, Permissions.Chat.Placeholder.ENDERCHEST,
                (shareData) -> ShareCommandUtils.createClickableItemName(
                        ShowcaseManager.ShareType.ENDER_CHEST,
                        TextUtils.ENDER_CHEST,
                        ShowcaseManager.createEnderChestShare(shareData.player, shareData.duration, null)
                ));

        registerPlaceholder(STATS, ShowcaseManager.ShareType.STATS, Permissions.Chat.Placeholder.STATS,
                (shareData) -> {
                    ItemStack stack = StatUtils.createStatsBook(shareData.player);
                    if (stack.isEmpty()) {
                        throw new RuntimeException("Statistics reading failed");
                    }
                    return ShareCommandUtils.createClickableItemName(
                            ShowcaseManager.ShareType.STATS,
                            StackUtils.getDisplayName(stack),
                            ShowcaseManager.createStatsShare(shareData.player, stack, shareData.duration, null)
                    );
                });

        registerPlaceholder(ITEM, ShowcaseManager.ShareType.ITEM, Permissions.Chat.Placeholder.ITEM,
                (shareData) -> {
                    ItemStack stack = shareData.player.getEquippedStack(EquipmentSlot.MAINHAND);
                    if (stack.isEmpty()) {
                        throw new RuntimeException(Text.translatable("showcase.message.no_item").getString());
                    }
                    return ShareCommandUtils.createClickableItemName(
                            ShowcaseManager.ShareType.ITEM,
                            StackUtils.getDisplayName(stack),
                            ShowcaseManager.createItemShare(shareData.player, stack, shareData.duration, null)
                    );
                });

        // Register extended placeholders
        ExtendedPlaceholders.registerExtendedPlaceholders();
    }

    public static boolean containsPlaceholders(String text) {
        TextNode[] nodes = eu.pb4.placeholders.api.Placeholders.DEFAULT_PLACEHOLDER_PARSER.parseNodes(
                new LiteralNode(text)
        );

        for (TextNode node : nodes) {
            if (node.isDynamic()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isOnlyPlaceholders(String text, PlaceholderContext context) {
        TextNode[] nodes = eu.pb4.placeholders.api.Placeholders.DEFAULT_PLACEHOLDER_PARSER.parseNodes(
                new LiteralNode(text)
        );

        for (TextNode node : nodes) {
            if (node instanceof LiteralNode(String value)) {
                if (!value.trim().isEmpty()) {
                    return false;
                }
            }
            else if (!node.isDynamic()) {
                return false;
            }
        }

        return true;
    }

    private static int getDurationFromSimpleArguments(String arg, ServerPlayerEntity player) {
        int inputDuration = SimpleArguments.intNumber(arg, ModConfigManager.getShareLinkDefaultExpiry());

        if (isOp(player)) {
            return inputDuration > 0 ? inputDuration : ModConfigManager.getShareLinkDefaultExpiry();
        }
        
        if (inputDuration <= 0) {
            return ModConfigManager.getShareLinkDefaultExpiry();
        }
        
        if (inputDuration < ModConfigManager.getShareLinkMinExpiry()) {
            return ModConfigManager.getShareLinkMinExpiry();
        }
        
        if (inputDuration > ModConfigManager.getShareLinkDefaultExpiry()) {
            return ModConfigManager.getShareLinkDefaultExpiry();
        }

        return inputDuration;
    }
}