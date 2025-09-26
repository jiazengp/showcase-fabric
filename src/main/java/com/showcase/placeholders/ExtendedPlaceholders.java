package com.showcase.placeholders;

import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import com.showcase.utils.ShareConstants;
import com.showcase.utils.stats.StatUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Extended placeholder system for Showcase mod
 * Provides comprehensive information about shares, statistics, permissions, and server data
 */
public class ExtendedPlaceholders {

    // Server-wide statistics tracking
    private static final AtomicInteger totalSharesCreated = new AtomicInteger(0);
    private static final AtomicLong totalViewsReceived = new AtomicLong(0);
    private static final Map<String, AtomicInteger> shareTypeStats = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> playerShareCounts = new ConcurrentHashMap<>();
    private static final Instant serverStartTime = Instant.now();

    // Performance metrics
    private static final AtomicLong totalResponseTime = new AtomicLong(0);
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    private static final AtomicInteger successfulShares = new AtomicInteger(0);
    private static final AtomicInteger failedShares = new AtomicInteger(0);
    private static final AtomicInteger slowPlaceholderRequests = new AtomicInteger(0);
    private static final long SLOW_THRESHOLD_MS = 50; // 50ms threshold for slow placeholders

    /**
     * Register all extended placeholders
     */
    public static void registerExtendedPlaceholders() {
        // Check if placeholder extensions are enabled
        if (!ModConfigManager.isPlaceholderExtensionsEnabled()) {
            return;
        }

        registerPlayerSharePlaceholders();

        if (ModConfigManager.isStatisticsTrackingEnabled()) {
            registerPlayerStatisticsPlaceholders();
        }

        registerPermissionPlaceholders();

        if (ModConfigManager.isServerStatisticsEnabled()) {
            registerServerStatisticsPlaceholders();
        }

        if (ModConfigManager.isPerformanceMetricsEnabled()) {
            registerPerformancePlaceholders();
        }

        if (ModConfigManager.isConditionalPlaceholdersEnabled()) {
            registerConditionalPlaceholders();
        }
    }

    /**
     * Register player share information placeholders
     */
    private static void registerPlayerSharePlaceholders() {
        // %showcase:shares_count% - Number of active shares
        Placeholders.register(id("shares_count"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            int count = ShowcaseStatistics.getActiveShareCount(player);
            return PlaceholderResult.value(Text.literal(String.valueOf(count)));
        });

        // %showcase:shares_remaining% - Shares until limit
        Placeholders.register(id("shares_remaining"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            int active = ShowcaseStatistics.getActiveShareCount(player);
            int limit = ModConfigManager.getMaxSharesPerPlayer();
            int remaining = Math.max(0, limit - active);
            return PlaceholderResult.value(Text.literal(String.valueOf(remaining)));
        });

        // %showcase:last_share_type% - Type of last share created
        Placeholders.register(id("last_share_type"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            ShowcaseManager.ShareType lastType = ShowcaseStatistics.getLastShareType(player);
            String typeName = lastType != null ? ShareConstants.ShareTypeNames.fromShareType(lastType) : "none";
            return PlaceholderResult.value(Text.literal(typeName));
        });

        // %showcase:last_share_time% - Time since last share
        Placeholders.register(id("last_share_time"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            Instant lastTime = ShowcaseStatistics.getLastShareTime(player);
            if (lastTime == null) {
                return PlaceholderResult.value(Text.literal("never"));
            }

            Duration elapsed = Duration.between(lastTime, Instant.now());
            return PlaceholderResult.value(Text.literal(formatDuration(elapsed) + " ago"));
        });

        // %showcase:next_share_expires% - When next share expires
        Placeholders.register(id("next_share_expires"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            Instant nextExpiry = ShowcaseStatistics.getNextShareExpiry(player);
            if (nextExpiry == null) {
                return PlaceholderResult.value(Text.literal("none"));
            }

            Duration remaining = Duration.between(Instant.now(), nextExpiry);
            return PlaceholderResult.value(Text.literal(formatDuration(remaining)));
        });
    }

    /**
     * Register player statistics placeholders
     */
    private static void registerPlayerStatisticsPlaceholders() {
        // %showcase:total_shares_created% - Total shares ever created
        Placeholders.register(id("total_shares_created"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            int total = playerShareCounts.getOrDefault(player.getUuidAsString(), new AtomicInteger(0)).get();
            return PlaceholderResult.value(Text.literal(String.valueOf(total)));
        });

        // %showcase:total_shares_viewed% - Total views received
        Placeholders.register(id("total_shares_viewed"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            long views = ShowcaseStatistics.getTotalViewsForPlayer(player);
            return PlaceholderResult.value(Text.literal(String.valueOf(views)));
        });

        // %showcase:most_shared_type% - Most frequently shared type
        Placeholders.register(id("most_shared_type"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            ShowcaseManager.ShareType mostShared = ShowcaseStatistics.getMostSharedType(player);
            String typeName = mostShared != null ? ShareConstants.ShareTypeNames.fromShareType(mostShared) : "none";
            return PlaceholderResult.value(Text.literal(typeName));
        });


        // %showcase:shares_today% - Shares created today
        Placeholders.register(id("shares_today"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            int todayShares = ShowcaseStatistics.getSharesCreatedToday(player);
            return PlaceholderResult.value(Text.literal(String.valueOf(todayShares)));
        });

        // %showcase:shares_this_week% - Shares created this week
        Placeholders.register(id("shares_this_week"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            int weekShares = ShowcaseStatistics.getSharesCreatedThisWeek(player);
            return PlaceholderResult.value(Text.literal(String.valueOf(weekShares)));
        });

        // %showcase:average_share_duration% - Average duration of shares
        Placeholders.register(id("average_share_duration"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            Duration avgDuration = ShowcaseStatistics.getAverageShareDuration(player);
            return PlaceholderResult.value(Text.literal(formatDuration(avgDuration)));
        });
    }

    /**
     * Register permission check placeholders
     */
    private static void registerPermissionPlaceholders() {
        // %showcase:can_share_item% - true/false
        Placeholders.register(id("can_share_item"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean canShare = PermissionChecker.hasPermission(player, Permissions.Command.ITEM, 0);
            return PlaceholderResult.value(Text.literal(String.valueOf(canShare)));
        });

        // %showcase:can_share_inventory% - true/false
        Placeholders.register(id("can_share_inventory"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean canShare = PermissionChecker.hasPermission(player, Permissions.Command.INVENTORY, 0);
            return PlaceholderResult.value(Text.literal(String.valueOf(canShare)));
        });

        // %showcase:can_share_stats% - true/false
        Placeholders.register(id("can_share_stats"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean canShare = PermissionChecker.hasPermission(player, Permissions.Command.STATS, 0);
            return PlaceholderResult.value(Text.literal(String.valueOf(canShare)));
        });

        // %showcase:can_use_chat_keywords% - true/false
        Placeholders.register(id("can_use_chat_keywords"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean canUse = PermissionChecker.hasPermission(player, Permissions.Chat.Placeholder.ITEM, 0);
            return PlaceholderResult.value(Text.literal(String.valueOf(canUse)));
        });

        // %showcase:has_admin_perms% - true/false
        Placeholders.register(id("has_admin_perms"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean isAdmin = PermissionChecker.hasPermission(player, Permissions.Manage.CANCEL, 0);
            return PlaceholderResult.value(Text.literal(String.valueOf(isAdmin)));
        });

        // Cooldown placeholders
        registerCooldownPlaceholders();
    }

    /**
     * Register cooldown-related placeholders
     */
    private static void registerCooldownPlaceholders() {
        // %showcase:item_cooldown% - Seconds until can share item
        Placeholders.register(id("item_cooldown"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            long cooldown = ShowcaseStatistics.getCooldownRemaining(player, ShowcaseManager.ShareType.ITEM);
            return PlaceholderResult.value(Text.literal(String.valueOf(cooldown)));
        });

        // %showcase:inventory_cooldown% - Seconds until can share inventory
        Placeholders.register(id("inventory_cooldown"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            long cooldown = ShowcaseStatistics.getCooldownRemaining(player, ShowcaseManager.ShareType.INVENTORY);
            return PlaceholderResult.value(Text.literal(String.valueOf(cooldown)));
        });

        // %showcase:chat_cooldown% - Seconds until can use keywords
        Placeholders.register(id("chat_cooldown"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            long cooldown = ShowcaseStatistics.getChatKeywordCooldown(player);
            return PlaceholderResult.value(Text.literal(String.valueOf(cooldown)));
        });
    }

    /**
     * Register server-wide statistics placeholders
     */
    private static void registerServerStatisticsPlaceholders() {
        // %showcase:server_total_shares% - Total shares on server
        Placeholders.register(id("server_total_shares"), (ctx, arg) -> {
            int total = totalSharesCreated.get();
            return PlaceholderResult.value(Text.literal(String.valueOf(total)));
        });

        // %showcase:server_active_shares% - Currently active shares
        Placeholders.register(id("server_active_shares"), (ctx, arg) -> {
            int active = ShowcaseStatistics.getTotalActiveShares();
            return PlaceholderResult.value(Text.literal(String.valueOf(active)));
        });

        // %showcase:server_most_active_user% - Player with most shares
        Placeholders.register(id("server_most_active_user"), (ctx, arg) -> {
            String mostActive = getMostActivePlayer();
            return PlaceholderResult.value(Text.literal(mostActive));
        });

        // %showcase:server_uptime% - Showcase mod uptime
        Placeholders.register(id("server_uptime"), (ctx, arg) -> {
            Duration uptime = Duration.between(serverStartTime, Instant.now());
            return PlaceholderResult.value(Text.literal(formatDuration(uptime)));
        });
    }

    /**
     * Register performance metric placeholders
     */
    private static void registerPerformancePlaceholders() {
        // %showcase:average_response_time% - Average command response time
        Placeholders.register(id("average_response_time"), (ctx, arg) -> {
            long avgTime = totalRequests.get() > 0 ? totalResponseTime.get() / totalRequests.get() : 0;
            return PlaceholderResult.value(Text.literal(avgTime + "ms"));
        });

        // %showcase:success_rate% - Percentage of successful shares
        Placeholders.register(id("success_rate"), (ctx, arg) -> {
            int total = successfulShares.get() + failedShares.get();
            double rate = total > 0 ? (double) successfulShares.get() / total * 100 : 100.0;
            return PlaceholderResult.value(Text.literal(String.format("%.1f%%", rate)));
        });

        // %showcase:cache_hit_rate% - Cache efficiency percentage
        Placeholders.register(id("cache_hit_rate"), (ctx, arg) -> {
            double hitRate = ShowcaseStatistics.getCacheHitRate();
            return PlaceholderResult.value(Text.literal(String.format("%.1f%%", hitRate)));
        });

        // %showcase:cache_size% - Current cache size
        Placeholders.register(id("cache_size"), (ctx, arg) -> {
            int size = ShowcaseStatistics.getCacheSize();
            return PlaceholderResult.value(Text.literal(String.valueOf(size)));
        });

        // %showcase:slow_placeholder_rate% - Percentage of slow placeholder requests
        Placeholders.register(id("slow_placeholder_rate"), (ctx, arg) -> {
            int total = totalRequests.get();
            int slow = slowPlaceholderRequests.get();
            double rate = total > 0 ? (double) slow / total * 100.0 : 0.0;
            return PlaceholderResult.value(Text.literal(String.format("%.2f%%", rate)));
        });
    }

    /**
     * Register conditional placeholders
     */
    private static void registerConditionalPlaceholders() {
        // %showcase:if_can_share_item_yes_no% - Returns "yes" or "no"
        Placeholders.register(id("if_can_share_item_yes_no"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean canShare = PermissionChecker.hasPermission(player, Permissions.Command.ITEM, 0);
            return PlaceholderResult.value(Text.literal(canShare ? "yes" : "no"));
        });

        // %showcase:if_admin_admin_player% - Returns "admin" or "player"
        Placeholders.register(id("if_admin_admin_player"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean isAdmin = PermissionChecker.hasPermission(player, Permissions.Manage.CANCEL, 0);
            return PlaceholderResult.value(Text.literal(isAdmin ? "admin" : "player"));
        });

        // %showcase:if_cooldown_ready_waiting% - "ready" or "waiting"
        Placeholders.register(id("if_cooldown_ready_waiting"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean onCooldown = ShowcaseStatistics.isOnAnyCooldown(player);
            return PlaceholderResult.value(Text.literal(onCooldown ? "waiting" : "ready"));
        });

        // %showcase:if_has_shares_active_none% - "active" or "none"
        Placeholders.register(id("if_has_shares_active_none"), (ctx, arg) -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean hasShares = ShowcaseStatistics.getActiveShareCount(player) > 0;
            return PlaceholderResult.value(Text.literal(hasShares ? "active" : "none"));
        });
    }

    // Utility methods

    private static Identifier id(String path) {
        return Identifier.of("showcase", path);
    }

    private static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "h";
        } else {
            return (seconds / 86400) + "d";
        }
    }


    private static String getMostActivePlayer() {
        return playerShareCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue((a, b) -> a.get() - b.get()))
            .map(Map.Entry::getKey)
            .orElse("none");
    }

    // Statistics tracking methods (to be called from ShowcaseManager)

    public static void incrementTotalShares() {
        totalSharesCreated.incrementAndGet();
    }

    public static void incrementPlayerShares(String playerUuid) {
        playerShareCounts.computeIfAbsent(playerUuid, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static void incrementShareTypeStats(ShowcaseManager.ShareType type) {
        String typeName = ShareConstants.ShareTypeNames.fromShareType(type);
        shareTypeStats.computeIfAbsent(typeName, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static void addResponseTime(long timeMs) {
        totalResponseTime.addAndGet(timeMs);
        totalRequests.incrementAndGet();
    }

    public static void incrementSuccessfulShares() {
        successfulShares.incrementAndGet();
    }

    public static void incrementFailedShares() {
        failedShares.incrementAndGet();
    }

    public static void recordSlowPlaceholder() {
        slowPlaceholderRequests.incrementAndGet();
    }

    /**
     * Get comprehensive performance statistics
     */
    public static Map<String, Object> getPerformanceStatistics() {
        int totalReqs = totalRequests.get();
        int slowReqs = slowPlaceholderRequests.get();
        int successful = successfulShares.get();
        int failed = failedShares.get();

        return Map.of(
            "totalPlaceholderRequests", totalReqs,
            "slowPlaceholderRequests", slowReqs,
            "slowPlaceholderRate", totalReqs > 0 ? (double) slowReqs / totalReqs * 100.0 : 0.0,
            "averageResponseTime", totalReqs > 0 ? totalResponseTime.get() / totalReqs : 0L,
            "successfulShares", successful,
            "failedShares", failed,
            "successRate", (successful + failed) > 0 ? (double) successful / (successful + failed) * 100.0 : 100.0,
            "cacheHitRate", ShowcaseStatistics.getCacheHitRate(),
            "cacheSize", ShowcaseStatistics.getCacheSize(),
            "uptime", Duration.between(serverStartTime, Instant.now()).toString()
        );
    }

    /**
     * Reset performance counters (useful for testing and maintenance)
     */
    public static void resetPerformanceCounters() {
        totalResponseTime.set(0);
        totalRequests.set(0);
        slowPlaceholderRequests.set(0);
        successfulShares.set(0);
        failedShares.set(0);
    }

    /**
     * Monitor placeholder execution with performance tracking
     */
    public static PlaceholderResult monitoredExecution(String placeholderName, PlaceholderContext ctx,
            java.util.function.Function<PlaceholderContext, PlaceholderResult> execution) {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();

        try {
            PlaceholderResult result = execution.apply(ctx);
            long duration = System.currentTimeMillis() - startTime;

            if (duration > SLOW_THRESHOLD_MS) {
                recordSlowPlaceholder();
                // Log slow placeholders for monitoring
                if (ctx.player() != null) {
                    System.out.println("Slow placeholder '" + placeholderName + "' took " + duration +
                        "ms for player " + ctx.player().getName().getString());
                }
            }

            addResponseTime(duration);
            return result;
        } catch (Exception e) {
            incrementFailedShares();
            return PlaceholderResult.invalid("Error: " + e.getMessage());
        }
    }
}