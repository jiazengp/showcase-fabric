package com.showcase.placeholders;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseCommand;
import com.showcase.command.ShowcaseManager;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
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

            String shareId = ShowcaseManager.createInventoryShare(player);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.INVENTORY);

            MutableText text = ShowcaseCommand.createClickableItemName(
                    player,
                    "INVENTORY",
                    Text.translatable("itemGroup.inventory"),
                    shareId
            );

            return PlaceholderResult.value(text);
        });

        eu.pb4.placeholders.api.Placeholders.register(HOTBAR, (ctx, arg) -> {
            if (!ctx.hasPlayer() || ctx.player() == null) return PlaceholderResult.invalid("No player");
            ServerPlayerEntity player = ctx.player();

            if (ShowcaseManager.isOnCooldown(player, ShowcaseManager.ShareType.HOTBAR))
                return PlaceholderResult.invalid("Too fast");

            String shareId = ShowcaseManager.createHotbarShare(player);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.HOTBAR);

            MutableText text = ShowcaseCommand.createClickableItemName(
                    player,
                    "HOTBAR",
                    Text.translatable("itemGroup.hotbar"),
                    shareId
            );
            return PlaceholderResult.value(text);
        });

        eu.pb4.placeholders.api.Placeholders.register(ITEM, (ctx, arg) -> {
            ShowcaseMod.LOGGER.info("Processing item placeholder for player1");
            if (!ctx.hasPlayer() || ctx.player() == null) return PlaceholderResult.invalid("No player");
            ServerPlayerEntity player = ctx.player();
            ShowcaseMod.LOGGER.info("Processing item placeholder for player2");
            if (ShowcaseManager.isOnCooldown(player, ShowcaseManager.ShareType.ITEM))
                return PlaceholderResult.invalid("Too fast");


            ItemStack stack = player.getInventory().getMainHandStack();

            if (stack.isEmpty()) {
                return PlaceholderResult.invalid(ShowcaseMod.CONFIG.messages.noItem);
            }

            String shareId = ShowcaseManager.createItemShare(player, stack);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.ITEM);

            MutableText text = ShowcaseCommand.createClickableItemName(
                    player,
                    "ITEM",
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

            String shareId = ShowcaseManager.createEnderChestShare(player);
            ShowcaseManager.setCooldown(player, ShowcaseManager.ShareType.ENDER_CHEST);

            MutableText text =  ShowcaseCommand.createClickableItemName(
                    player,
                    "ENDER_CHEST",
                    Text.translatable("container.enderchest"),
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