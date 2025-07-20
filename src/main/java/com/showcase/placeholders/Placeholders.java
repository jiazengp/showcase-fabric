package com.showcase.placeholders;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShareCommandUtils;
import com.showcase.command.ShowcaseManager;
import com.showcase.utils.TextUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Placeholders {
    private static final Identifier INVENTORY = Identifier.of(ShowcaseMod.MOD_ID, "inventory");
    private static final Identifier HOTBAR = Identifier.of(ShowcaseMod.MOD_ID, "hotbar");
    private static final Identifier ITEM = Identifier.of(ShowcaseMod.MOD_ID, "item");
    private static final Identifier ENDER_CHEST = Identifier.of(ShowcaseMod.MOD_ID, "ender_chest");

    public static void registerPlaceholders() {
        eu.pb4.placeholders.api.Placeholders.register(INVENTORY, (ctx, arg) -> {
            if (!ctx.hasPlayer() || ctx.player() == null) return PlaceholderResult.invalid("No player");
            ServerPlayerEntity player = ctx.player();

            if (ShowcaseManager.isOnCooldown(player, ShowcaseManager.ShareType.INVENTORY))
                return PlaceholderResult.invalid("Too fast");

            String shareId = ShowcaseManager.createInventoryShare(player, null);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.INVENTORY);

            MutableText text = ShareCommandUtils.createClickableItemName(
                    ShowcaseManager.ShareType.INVENTORY,
                    TextUtils.INVENTORY,
                    shareId
            );

            return PlaceholderResult.value(text);
        });

        eu.pb4.placeholders.api.Placeholders.register(HOTBAR, (ctx, arg) -> {
            if (!ctx.hasPlayer() || ctx.player() == null) return PlaceholderResult.invalid("No player");
            ServerPlayerEntity player = ctx.player();

            if (ShowcaseManager.isOnCooldown(player, ShowcaseManager.ShareType.HOTBAR))
                return PlaceholderResult.invalid("Too fast");

            String shareId = ShowcaseManager.createHotbarShare(player, null);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.HOTBAR);

            MutableText text = ShareCommandUtils.createClickableItemName(
                    ShowcaseManager.ShareType.HOTBAR,
                    TextUtils.HOTBAR,
                    shareId
            );
            return PlaceholderResult.value(text);
        });

        eu.pb4.placeholders.api.Placeholders.register(ITEM, (ctx, arg) -> {
            if (!ctx.hasPlayer() || ctx.player() == null) return PlaceholderResult.invalid("No player");
            ServerPlayerEntity player = ctx.player();

            if (ShowcaseManager.isOnCooldown(player, ShowcaseManager.ShareType.ITEM))
                return PlaceholderResult.invalid("Too fast");


            ItemStack stack = player.getEquippedStack(EquipmentSlot.MAINHAND);

            if (stack.isEmpty()) {
                return PlaceholderResult.invalid(Text.translatable("showcase.message.no_item").getString());
            }

            String shareId = ShowcaseManager.createItemShare(player, stack, null);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.ITEM);

            MutableText text = ShareCommandUtils.createClickableItemName(
                    ShowcaseManager.ShareType.ITEM,
                    stack.getName(),
                    shareId
            );
            return PlaceholderResult.value(text);
        });

        eu.pb4.placeholders.api.Placeholders.register(ENDER_CHEST, (ctx, arg) -> {
            if (!ctx.hasPlayer() || ctx.player() == null) return PlaceholderResult.invalid("No player");
            ServerPlayerEntity player = ctx.player();

            if (ShowcaseManager.isOnCooldown(player, ShowcaseManager.ShareType.ENDER_CHEST))
                return PlaceholderResult.invalid("Too fast");

            String shareId = ShowcaseManager.createEnderChestShare(player, null);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.ENDER_CHEST);

            MutableText text =  ShareCommandUtils.createClickableItemName(
                    ShowcaseManager.ShareType.ENDER_CHEST,
                    TextUtils.ENDER_CHEST,
                    shareId
            );
            return PlaceholderResult.value(text);
        });


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
}