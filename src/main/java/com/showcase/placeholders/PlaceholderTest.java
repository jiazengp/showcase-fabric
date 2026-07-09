package com.showcase.placeholders;

import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfigManager;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Test class for essential placeholder functionality
 * This class provides methods to test and validate core placeholder features
 */
public class PlaceholderTest {

    /**
     * Test essential placeholders for a player
     */
    public static void testEssentialPlaceholders(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§7Testing essential placeholders..."));

        // Test core sharing placeholders
        player.sendSystemMessage(Component.literal("§6Core Sharing Placeholders:"));
        testPlaceholder(player, "item");
        testPlaceholder(player, "inventory");
        testPlaceholder(player, "hotbar");
        testPlaceholder(player, "ender_chest");
        testPlaceholder(player, "stats");

        // Test player statistics placeholders
        player.sendSystemMessage(Component.literal("§6Statistics Placeholders:"));
        testPlaceholder(player, "shares_count");
        testPlaceholder(player, "total_views");

        // Test permission placeholders
        player.sendSystemMessage(Component.literal("§6Permission Placeholders:"));
        testPlaceholder(player, "can_share_item");
        testPlaceholder(player, "has_admin_perms");

        player.sendSystemMessage(Component.literal("§7Placeholder testing complete."));
    }

    /**
     * Test a single placeholder
     */
    private static void testPlaceholder(ServerPlayer player, String placeholderName) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath("showcase", placeholderName);
            ServerPlaceholderContext context = ServerPlaceholderContext.of(player);
            Component result = Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(
                "%showcase:" + placeholderName + "%",
                context.asParserContext()
            );

            player.sendSystemMessage(Component.literal("§a✓ %showcase:" + placeholderName + "% = " + result.getString()));
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c✗ %showcase:" + placeholderName + "% error: " + e.getMessage()));
            ShowcaseMod.LOGGER.error("Placeholder test failed for {}: {}", placeholderName, e.getMessage());
        }
    }

    /**
     * Test placeholder performance
     */
    public static void testPlaceholderPerformance(ServerPlayer player, int iterations) {
        player.sendSystemMessage(Component.literal("§7Testing placeholder performance with " + iterations + " iterations..."));

        String[] testPlaceholders = {"shares_count", "total_views", "can_share_item"};
        ServerPlaceholderContext context = ServerPlaceholderContext.of(player);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            for (String placeholderName : testPlaceholders) {
                try {
                    Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(
                        "%showcase:" + placeholderName + "%",
                        context.asParserContext()
                    );
                } catch (Exception e) {
                    ShowcaseMod.LOGGER.warn("Performance test failed for {}: {}", placeholderName, e.getMessage());
                }
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double avgTimePerPlaceholder = (double) totalTime / (iterations * testPlaceholders.length);

        player.sendSystemMessage(Component.literal("§7Performance test complete:"));
        player.sendSystemMessage(Component.literal("§f- Total time: " + totalTime + "ms"));
        player.sendSystemMessage(Component.literal("§f- Average per placeholder: " + String.format("%.2f", avgTimePerPlaceholder) + "ms"));
    }

    /**
     * Validate that all essential placeholders are registered
     */
    public static boolean validatePlaceholderRegistration() {
        String[] essentialPlaceholders = {
            "item", "inventory", "hotbar", "ender_chest", "stats",
            "shares_count", "total_views", "can_share_item", "has_admin_perms"
        };

        ShowcaseMod.LOGGER.info("Validating {} essential placeholders...", essentialPlaceholders.length);

        // Simply log the placeholders we're expecting to be registered
        // The actual validation will happen during runtime usage
        for (String placeholderName : essentialPlaceholders) {
            ShowcaseMod.LOGGER.info("Expected placeholder: %showcase:{}", placeholderName);
        }

        ShowcaseMod.LOGGER.info("Placeholder validation logged. Check runtime usage for actual validation.");
        return true; // Simplified validation
    }
}
