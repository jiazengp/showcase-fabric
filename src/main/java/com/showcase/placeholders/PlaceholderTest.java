package com.showcase.placeholders;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Test class for extended placeholder functionality
 * This class provides methods to test and validate placeholder features
 */
public class PlaceholderTest {

    /**
     * Test all available placeholders for a player
     */
    public static void testAllPlaceholders(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("§7Testing extended placeholders..."));

        // Test player share placeholders
        player.sendMessage(Text.literal("§6Player Share Placeholders:"));
        testPlaceholder(player, "shares_count");
        testPlaceholder(player, "shares_remaining");
        testPlaceholder(player, "last_share_type");
        testPlaceholder(player, "last_share_time");
        testPlaceholder(player, "next_share_expires");

        // Test player statistics placeholders
        if (ModConfigManager.isStatisticsTrackingEnabled()) {
            player.sendMessage(Text.literal("§6Player Statistics Placeholders:"));
            testPlaceholder(player, "total_shares_created");
            testPlaceholder(player, "total_shares_viewed");
            testPlaceholder(player, "most_shared_type");
            testPlaceholder(player, "shares_today");
            testPlaceholder(player, "shares_this_week");
            testPlaceholder(player, "average_share_duration");
        }

        // Test permission placeholders
        player.sendMessage(Text.literal("§6Permission Placeholders:"));
        testPlaceholder(player, "can_share_item");
        testPlaceholder(player, "can_share_inventory");
        testPlaceholder(player, "can_share_stats");
        testPlaceholder(player, "can_use_chat_keywords");
        testPlaceholder(player, "has_admin_perms");

        // Test cooldown placeholders
        player.sendMessage(Text.literal("§6Cooldown Placeholders:"));
        testPlaceholder(player, "item_cooldown");
        testPlaceholder(player, "inventory_cooldown");
        testPlaceholder(player, "chat_cooldown");

        // Test server statistics placeholders
        if (ModConfigManager.isServerStatisticsEnabled()) {
            player.sendMessage(Text.literal("§6Server Statistics Placeholders:"));
            testPlaceholder(player, "server_total_shares");
            testPlaceholder(player, "server_active_shares");
            testPlaceholder(player, "server_most_active_user");
            testPlaceholder(player, "server_uptime");
        }

        // Test performance placeholders
        if (ModConfigManager.isPerformanceMetricsEnabled()) {
            player.sendMessage(Text.literal("§6Performance Placeholders:"));
            testPlaceholder(player, "average_response_time");
            testPlaceholder(player, "success_rate");
            testPlaceholder(player, "cache_hit_rate");
        }

        // Test conditional placeholders
        if (ModConfigManager.isConditionalPlaceholdersEnabled()) {
            player.sendMessage(Text.literal("§6Conditional Placeholders:"));
            testPlaceholder(player, "if_can_share_item_yes_no");
            testPlaceholder(player, "if_admin_admin_player");
            testPlaceholder(player, "if_cooldown_ready_waiting");
            testPlaceholder(player, "if_has_shares_active_none");
        }

        player.sendMessage(Text.literal("§aPlaceholder testing completed!"));
    }

    /**
     * Test a specific placeholder
     */
    private static void testPlaceholder(ServerPlayerEntity player, String placeholderName) {
        try {
            Identifier placeholderId = Identifier.of("showcase", placeholderName);
            PlaceholderContext context = PlaceholderContext.of(player);

            Text result = Placeholders.parseText(
                Text.literal("%showcase:" + placeholderName + "%"),
                context
            );

            if (result != null) {
                player.sendMessage(Text.literal("§7  %showcase:" + placeholderName + "% → §f" + result.getString()));
            } else {
                player.sendMessage(Text.literal("§7  %showcase:" + placeholderName + "% → §cnull"));
            }
        } catch (Exception e) {
            player.sendMessage(Text.literal("§7  %showcase:" + placeholderName + "% → §cERROR: " + e.getMessage()));
        }
    }

    /**
     * Simulate share creation and test statistics
     */
    public static void simulateShareCreation(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("§7Simulating share creation for testing..."));

        // Record some test share creations
        for (int i = 0; i < 3; i++) {
            ShowcaseStatistics.recordShareCreation(
                player,
                ShowcaseManager.ShareType.ITEM,
                java.time.Duration.ofMinutes(30)
            );

            // Simulate views
            ShowcaseStatistics.recordShareView(player, player);
            ShowcaseStatistics.recordShareView(player, player);
        }

        ExtendedPlaceholders.incrementTotalShares();
        ExtendedPlaceholders.incrementPlayerShares(player.getUuidAsString());
        ExtendedPlaceholders.incrementSuccessfulShares();

        player.sendMessage(Text.literal("§a✓ Created 3 simulated ITEM shares"));
        player.sendMessage(Text.literal("§a✓ Generated 6 simulated share views"));
        player.sendMessage(Text.literal("§a✓ Updated player statistics"));
    }

    /**
     * Test configuration loading and validation
     */
    public static void testConfiguration() {
        // This method is called from command context, so no player parameter needed for logging
        ShowcaseMod.LOGGER.info("Testing placeholder configuration...");

        try {
            boolean enabled = ModConfigManager.isPlaceholderExtensionsEnabled();
            int maxShares = ModConfigManager.getMaxSharesPerPlayer();
            int cacheDuration = ModConfigManager.getPlaceholderCacheDuration();
            boolean statsEnabled = ModConfigManager.isStatisticsTrackingEnabled();

            ShowcaseMod.LOGGER.info("Configuration test results:");
            ShowcaseMod.LOGGER.info("  Placeholders enabled: {}", enabled);
            ShowcaseMod.LOGGER.info("  Max shares per player: {}", maxShares);
            ShowcaseMod.LOGGER.info("  Cache duration: {}s", cacheDuration);
            ShowcaseMod.LOGGER.info("  Statistics enabled: {}", statsEnabled);

            if (enabled && maxShares > 0 && cacheDuration > 0) {
                ShowcaseMod.LOGGER.info("Configuration validation: PASSED");
            } else {
                ShowcaseMod.LOGGER.warn("Configuration validation: FAILED - some values are invalid");
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Configuration test failed: {}", e.getMessage());
        }
    }

    /**
     * Test placeholder expansion with various contexts
     */
    public static void testPlaceholderExpansion(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("§6Testing placeholder expansion:"));

        String[] testMessages = {
            "Player has %showcase:shares_count% active shares",
            "Can create %showcase:shares_remaining% more shares",
            "Server uptime: %showcase:server_uptime%",
            "Admin status: %showcase:if_admin_admin_player%"
        };

        PlaceholderContext context = PlaceholderContext.of(player);

        for (String message : testMessages) {
            try {
                Text original = Text.literal(message);
                Text result = Placeholders.parseText(original, context);

                player.sendMessage(Text.literal("§7Original: §f" + message));
                player.sendMessage(Text.literal("§7Expanded: §a" + result.getString()));
                player.sendMessage(Text.literal("§7---"));
            } catch (Exception e) {
                player.sendMessage(Text.literal("§cError expanding message '" + message + "': " + e.getMessage()));
            }
        }
    }

    /**
     * Comprehensive test suite
     */
    public static void runFullTest(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("§e=== Starting Extended Placeholder Test Suite ==="));

        // Test configuration
        testConfiguration();

        // Simulate some data
        simulateShareCreation(player);

        // Test all placeholders
        testAllPlaceholders(player);

        // Test expansion
        testPlaceholderExpansion(player);

        player.sendMessage(Text.literal("§e=== Extended Placeholder Test Suite Complete ==="));
    }

    /**
     * Quick validation of core functionality
     */
    public static boolean validatePlaceholderSystem(ServerPlayerEntity player) {
        try {
            // Test basic configuration access
            if (!ModConfigManager.isPlaceholderExtensionsEnabled()) {
                player.sendMessage(Text.literal("§c✗ Placeholder extensions are disabled"));
                return false;
            }

            player.sendMessage(Text.literal("§a✓ Placeholder extensions enabled"));

            // Test basic placeholder resolution
            player.sendMessage(Text.literal("§7Testing basic placeholder resolution:"));
            testPlaceholder(player, "shares_count");
            testPlaceholder(player, "can_share_item");

            // Test statistics system
            ShowcaseStatistics.getActiveShareCount(player);
            player.sendMessage(Text.literal("§a✓ Statistics system accessible"));

            return true;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cPlaceholder system validation failed: " + e.getMessage()));
            return false;
        }
    }

    /**
     * Test performance statistics and monitoring
     */
    public static void testPerformanceStatistics(ServerPlayerEntity player) {
        sendColoredMessage(player, "§6=== Performance Statistics Test ===");

        try {
            // Get performance statistics from ExtendedPlaceholders
            var perfStats = ExtendedPlaceholders.getPerformanceStatistics();

            sendColoredMessage(player, "§7Performance Metrics:");
            perfStats.forEach((key, value) -> {
                sendColoredMessage(player, "§7  " + key + ": §f" + value);
            });

            // Test cache statistics
            sendColoredMessage(player, "§7Cache Statistics:");
            sendColoredMessage(player, "§7  Hit Rate: §f" + String.format("%.1f%%", ShowcaseStatistics.getCacheHitRate()));
            sendColoredMessage(player, "§7  Cache Size: §f" + ShowcaseStatistics.getCacheSize());
            sendColoredMessage(player, "§7  Cache Hits: §f" + ShowcaseStatistics.getCacheHits());
            sendColoredMessage(player, "§7  Cache Misses: §f" + ShowcaseStatistics.getCacheMisses());

            // Test performance placeholders
            sendColoredMessage(player, "§7Performance Placeholders:");
            testPlaceholder(player, "cache_hit_rate");
            testPlaceholder(player, "cache_size");
            testPlaceholder(player, "slow_placeholder_rate");
            testPlaceholder(player, "success_rate");
            testPlaceholder(player, "average_response_time");

        } catch (Exception e) {
            sendColoredMessage(player, "§cError testing performance statistics: " + e.getMessage());
            ShowcaseMod.LOGGER.error("Performance test error", e);
        }

        sendColoredMessage(player, "§6=== Performance Test Complete ===");
    }

    /**
     * Send colored message to player
     */
    private static void sendColoredMessage(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message));
    }

    /**
     * Performance benchmark test
     */
    public static void benchmarkPlaceholders(ServerPlayerEntity player, int iterations) {
        sendColoredMessage(player, "§6Starting placeholder performance benchmark (" + iterations + " iterations)...");

        String[] testPlaceholders = {
            "shares_count", "total_shares_created", "cache_hit_rate",
            "server_active_shares", "can_share_item"
        };

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < iterations; i++) {
            for (String placeholder : testPlaceholders) {
                try {
                    Identifier placeholderId = Identifier.of("showcase", placeholder);
                    PlaceholderContext context = PlaceholderContext.of(player);

                    long placeholderStart = System.nanoTime();
                    Text result = Placeholders.parseText(
                        Text.literal("%showcase:" + placeholder + "%"),
                        context
                    );
                    long placeholderEnd = System.nanoTime();

                    // Track slow placeholders (>50ms = 50,000,000 nanoseconds)
                    if ((placeholderEnd - placeholderStart) > 50_000_000) {
                        ExtendedPlaceholders.recordSlowPlaceholder();
                    }

                    if (result != null) {
                        successCount++;
                        ExtendedPlaceholders.incrementSuccessfulShares();
                    } else {
                        errorCount++;
                        ExtendedPlaceholders.incrementFailedShares();
                    }
                } catch (Exception e) {
                    errorCount++;
                    ExtendedPlaceholders.incrementFailedShares();
                }
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        long totalPlaceholders = (long) iterations * testPlaceholders.length;
        double avgTimePerPlaceholder = totalPlaceholders > 0 ? (double) totalTime / totalPlaceholders : 0;

        sendColoredMessage(player, "§6Benchmark Results:");
        sendColoredMessage(player, "§7  Total Time: §f" + totalTime + "ms");
        sendColoredMessage(player, "§7  Total Placeholders: §f" + totalPlaceholders);
        sendColoredMessage(player, "§7  Successful: §a" + successCount);
        sendColoredMessage(player, "§7  Errors: §c" + errorCount);
        sendColoredMessage(player, "§7  Avg Time/Placeholder: §f" + String.format("%.2f", avgTimePerPlaceholder) + "ms");

        // Record benchmark time for overall statistics
        ExtendedPlaceholders.addResponseTime(totalTime);

        sendColoredMessage(player, "§6Benchmark Complete!");
    }
}