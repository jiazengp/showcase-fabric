package com.showcase.placeholders;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager;
import com.showcase.data.GlobalDataManager;
import com.showcase.data.JsonCodecDataStorage;
import com.showcase.data.PlayerStatisticsData;
import com.showcase.data.ServerStatisticsData;
import com.showcase.utils.StatisticsCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistics manager for extended placeholder support
 * Tracks detailed player and server statistics
 */
public class ShowcaseStatistics {

    // Storage identifiers
    public static final Identifier STATISTICS_STORAGE_ID = Identifier.of("showcase", "statistics");
    public static final JsonCodecDataStorage<ServerStatisticsData> STATISTICS_STORAGE =
            new JsonCodecDataStorage<>("showcase_statistics", ServerStatisticsData.CODEC);

    // Server instance reference for data persistence
    private static MinecraftServer currentServer;

    // Cache service
    private static final StatisticsCache cache = new StatisticsCache();

    // Player-specific statistics
    private static final Map<String, PlayerStats> playerStatistics = new ConcurrentHashMap<>();

    // Server-wide statistics
    private static final AtomicInteger totalActiveShares = new AtomicInteger(0);
    private static final AtomicLong totalViewsEver = new AtomicLong(0);
    private static final Map<String, AtomicInteger> shareTypeGlobalStats = new ConcurrentHashMap<>();

    // Performance tracking
    private static final Map<String, Long> lastShareTime = new ConcurrentHashMap<>();
    private static final Map<String, ShowcaseManager.ShareType> lastShareType = new ConcurrentHashMap<>();
    private static final Map<String, Instant> nextShareExpiry = new ConcurrentHashMap<>();

    // Player statistics data class
    public static class PlayerStats {
        public final AtomicInteger totalShares = new AtomicInteger(0);
        public final AtomicLong totalViews = new AtomicLong(0);
        public final Map<ShowcaseManager.ShareType, AtomicInteger> sharesByType = new ConcurrentHashMap<>();
        public final Map<String, Integer> sharesPerDay = new ConcurrentHashMap<>();
        public final List<Duration> shareDurations = new ArrayList<>();
        public ShowcaseManager.ShareType mostSharedType = null;
        public Instant lastShareTime = null;

        public PlayerStats() {
            // Initialize share type counters
            for (ShowcaseManager.ShareType type : ShowcaseManager.ShareType.values()) {
                sharesByType.put(type, new AtomicInteger(0));
            }
        }
    }

    /**
     * Get active share count for a player
     */
    public static int getActiveShareCount(ServerPlayerEntity player) {
        if (player == null) return 0;

        return (int) ShowcaseManager.getUnmodifiableActiveShares().values().stream()
            .filter(entry -> entry.getOwnerUuid().equals(player.getUuid()))
            .count();
    }

    /**
     * Get last share type for a player
     */
    public static ShowcaseManager.ShareType getLastShareType(ServerPlayerEntity player) {
        if (player == null) return null;
        return lastShareType.get(player.getUuidAsString());
    }

    /**
     * Get last share time for a player
     */
    public static Instant getLastShareTime(ServerPlayerEntity player) {
        if (player == null) return null;
        Long timeMs = lastShareTime.get(player.getUuidAsString());
        return timeMs != null ? Instant.ofEpochMilli(timeMs) : null;
    }

    /**
     * Get next share expiry time for a player
     */
    public static Instant getNextShareExpiry(ServerPlayerEntity player) {
        if (player == null) return null;
        return nextShareExpiry.get(player.getUuidAsString());
    }

    /**
     * Get total views received by a player (cached)
     */
    public static long getTotalViewsForPlayer(ServerPlayerEntity player) {
        if (player == null) return 0;

        String cacheKey = StatisticsCache.playerKey(player.getUuidAsString(), "total_views");
        Long cached = cache.get(cacheKey, Long.class);
        if (cached != null) {
            return cached;
        }

        PlayerStats stats = getPlayerStats(player);
        long result = stats.totalViews.get();

        cache.put(cacheKey, result);
        return result;
    }

    /**
     * Get most shared type for a player (cached)
     */
    public static ShowcaseManager.ShareType getMostSharedType(ServerPlayerEntity player) {
        if (player == null) return null;

        String cacheKey = StatisticsCache.playerKey(player.getUuidAsString(), "most_shared_type");
        ShowcaseManager.ShareType cached = cache.get(cacheKey, ShowcaseManager.ShareType.class);
        if (cached != null) {
            return cached;
        }

        PlayerStats stats = getPlayerStats(player);
        ShowcaseManager.ShareType result = stats.mostSharedType;

        cache.put(cacheKey, result);
        return result;
    }

    /**
     * Get shares created today by a player (cached)
     */
    public static int getSharesCreatedToday(ServerPlayerEntity player) {
        if (player == null) return 0;

        String cacheKey = StatisticsCache.playerKey(player.getUuidAsString(), "shares_today");
        Integer cached = cache.get(cacheKey, Integer.class);
        if (cached != null) {
            return cached;
        }

        PlayerStats stats = getPlayerStats(player);
        String today = LocalDate.now().toString();
        int result = stats.sharesPerDay.getOrDefault(today, 0);

        cache.put(cacheKey, result);
        return result;
    }

    /**
     * Get shares created this week by a player (cached)
     */
    public static int getSharesCreatedThisWeek(ServerPlayerEntity player) {
        if (player == null) return 0;

        String cacheKey = StatisticsCache.playerKey(player.getUuidAsString(), "shares_week");
        Integer cached = cache.get(cacheKey, Integer.class);
        if (cached != null) {
            return cached;
        }

        PlayerStats stats = getPlayerStats(player);
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);

        int result = stats.sharesPerDay.entrySet().stream()
            .filter(entry -> {
                LocalDate date = LocalDate.parse(entry.getKey());
                return !date.isBefore(weekStart) && !date.isAfter(now);
            })
            .mapToInt(Map.Entry::getValue)
            .sum();

        cache.put(cacheKey, result);
        return result;
    }

    /**
     * Get average share duration for a player (cached)
     */
    public static Duration getAverageShareDuration(ServerPlayerEntity player) {
        if (player == null) return Duration.ZERO;

        String cacheKey = StatisticsCache.playerKey(player.getUuidAsString(), "avg_duration");
        Duration cached = cache.get(cacheKey, Duration.class);
        if (cached != null) {
            return cached;
        }

        PlayerStats stats = getPlayerStats(player);

        if (stats.shareDurations.isEmpty()) {
            cache.put(cacheKey, Duration.ZERO);
            return Duration.ZERO;
        }

        long averageSeconds = stats.shareDurations.stream()
            .mapToLong(Duration::getSeconds)
            .sum() / stats.shareDurations.size();

        Duration result = Duration.ofSeconds(averageSeconds);
        cache.put(cacheKey, result);
        return result;
    }

    /**
     * Get cooldown remaining for a specific share type
     */
    public static long getCooldownRemaining(ServerPlayerEntity player, ShowcaseManager.ShareType type) {
        if (player == null) return 0;
        return com.showcase.utils.CooldownManager.getRemainingCooldown(player, type);
    }

    /**
     * Get chat keyword cooldown remaining
     * Note: Currently, chat keywords use the same cooldown as ITEM shares
     */
    public static long getChatKeywordCooldown(ServerPlayerEntity player) {
        if (player == null) return 0;
        // Use ITEM share cooldown as chat keywords typically trigger item sharing
        return com.showcase.utils.CooldownManager.getRemainingCooldown(player, ShowcaseManager.ShareType.ITEM);
    }

    /**
     * Check if player is on any cooldown
     */
    public static boolean isOnAnyCooldown(ServerPlayerEntity player) {
        if (player == null) return false;

        for (ShowcaseManager.ShareType type : ShowcaseManager.ShareType.values()) {
            if (ShowcaseManager.isOnCooldown(player, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get total active shares on server (cached)
     */
    public static int getTotalActiveShares() {
        String cacheKey = StatisticsCache.serverKey("total_active_shares");
        Integer cached = cache.get(cacheKey, Integer.class);
        if (cached != null) {
            return cached;
        }

        int result = ShowcaseManager.getUnmodifiableActiveShares().size();
        cache.put(cacheKey, result);
        return result;
    }

    /**
     * Get cache hit rate based on actual cache statistics
     */
    public static double getCacheHitRate() {
        return cache.getHitRate();
    }

    /**
     * Get cache size
     */
    public static int getCacheSize() {
        return cache.getSize();
    }

    /**
     * Get cache hits count
     */
    public static long getCacheHits() {
        return cache.getHits();
    }

    /**
     * Get cache misses count
     */
    public static long getCacheMisses() {
        return cache.getMisses();
    }

    /**
     * Clear all cached statistics
     */
    public static void clearAllCache() {
        cache.clear();
    }

    /**
     * Force cache cleanup
     */
    public static void forceCacheCleanup() {
        cache.forceCleanup();
    }

    /**
     * Get cache statistics summary
     */
    public static Map<String, Object> getCacheStatistics() {
        return cache.getStatistics();
    }

    /**
     * Get or create player statistics
     */
    private static PlayerStats getPlayerStats(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        return playerStatistics.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    // Event handlers (to be called when shares are created/viewed)

    /**
     * Record a new share creation
     */
    public static void recordShareCreation(ServerPlayerEntity player, ShowcaseManager.ShareType type, Duration duration) {
        if (player == null) return;

        String uuid = player.getUuidAsString();
        PlayerStats stats = getPlayerStats(player);

        // Update player statistics
        stats.totalShares.incrementAndGet();
        stats.sharesByType.get(type).incrementAndGet();
        stats.shareDurations.add(duration);
        stats.lastShareTime = Instant.now();

        // Update daily statistics
        String today = LocalDate.now().toString();
        stats.sharesPerDay.merge(today, 1, Integer::sum);

        // Update most shared type
        updateMostSharedType(stats);

        // Update tracking maps
        lastShareTime.put(uuid, System.currentTimeMillis());
        lastShareType.put(uuid, type);

        // Update next expiry time
        Instant expiry = Instant.now().plus(duration);
        nextShareExpiry.merge(uuid, expiry, (existing, newExpiry) ->
            existing.isBefore(newExpiry) ? existing : newExpiry);

        // Update global statistics
        totalActiveShares.incrementAndGet();
        shareTypeGlobalStats.computeIfAbsent(type.name(), k -> new AtomicInteger(0)).incrementAndGet();

        // Clear relevant cache entries
        clearCacheForPlayer(uuid);

        // Trigger periodic save
        scheduleAsyncSave();
    }

    /**
     * Record a share view
     */
    public static void recordShareView(ServerPlayerEntity creator, ServerPlayerEntity viewer) {
        if (creator == null) return;

        PlayerStats stats = getPlayerStats(creator);
        stats.totalViews.incrementAndGet();
        totalViewsEver.incrementAndGet();

        // Clear cache
        clearCacheForPlayer(creator.getUuidAsString());

        // Trigger periodic save
        scheduleAsyncSave();
    }

    /**
     * Record share expiry or cancellation
     */
    public static void recordShareExpiry(ServerPlayerEntity player, ShowcaseManager.ShareType type) {
        if (player == null) return;

        totalActiveShares.decrementAndGet();

        // Update next expiry time
        String uuid = player.getUuidAsString();
        updateNextExpiryTime(uuid);

        clearCacheForPlayer(uuid);

        // Trigger periodic save
        scheduleAsyncSave();
    }

    /**
     * Update most shared type for a player
     */
    private static void updateMostSharedType(PlayerStats stats) {
        stats.mostSharedType = stats.sharesByType.entrySet().stream()
            .max(Map.Entry.comparingByValue((a, b) -> a.get() - b.get()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Update next expiry time for a player
     */
    private static void updateNextExpiryTime(String playerUuid) {
        // This would need to scan all active shares for the player
        // For now, just remove the entry
        nextShareExpiry.remove(playerUuid);
    }

    /**
     * Clear cache entries for a specific player
     */
    private static void clearCacheForPlayer(String playerUuid) {
        cache.removePattern(playerUuid);
    }


    /**
     * Initialize statistics system
     */
    public static void initialize() {
        // Register storage with GlobalDataManager
        GlobalDataManager.register(STATISTICS_STORAGE_ID, STATISTICS_STORAGE);
        ShowcaseMod.LOGGER.info("Registered statistics storage system");

        // Load existing statistics from persistent storage if available
        loadStatisticsFromStorage();

        // Schedule periodic cache cleanup
        schedulePeriodicTasks();
    }

    /**
     * Set server instance for data persistence
     */
    public static void setServer(MinecraftServer server) {
        currentServer = server;
        if (server != null) {
            loadStatisticsFromStorage();
        }
    }

    /**
     * Load statistics from persistent storage
     */
    private static void loadStatisticsFromStorage() {
        if (currentServer == null) {
            ShowcaseMod.LOGGER.debug("Server not set, skipping statistics load");
            return;
        }

        try {
            ServerStatisticsData data = GlobalDataManager.getData(currentServer, STATISTICS_STORAGE_ID);
            if (data != null) {
                // Restore server-wide statistics
                totalViewsEver.set(data.totalViewsEver());

                // Restore share type global stats
                shareTypeGlobalStats.clear();
                data.shareTypeGlobalStats().forEach((key, value) ->
                    shareTypeGlobalStats.put(key, new AtomicInteger(value)));

                // Restore player statistics
                playerStatistics.clear();
                data.playerStatistics().forEach((uuid, playerData) -> {
                    PlayerStats stats = new PlayerStats();
                    stats.totalShares.set(playerData.totalShares());
                    stats.totalViews.set(playerData.totalViews());

                    // Restore shares by type
                    playerData.sharesByType().forEach((type, count) ->
                        stats.sharesByType.get(type).set(count));

                    // Restore other player data
                    stats.sharesPerDay.putAll(playerData.sharesPerDay());
                    stats.shareDurations.addAll(playerData.getShareDurations());
                    stats.mostSharedType = playerData.mostSharedType();
                    stats.lastShareTime = playerData.getLastShareTimeInstant();

                    playerStatistics.put(uuid, stats);
                });

                ShowcaseMod.LOGGER.info("Loaded statistics for {} players from storage",
                    data.playerStatistics().size());
            } else {
                ShowcaseMod.LOGGER.debug("No statistics found in storage, starting fresh");
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to load statistics from storage", e);
        }
    }

    /**
     * Schedule periodic maintenance tasks
     */
    private static void schedulePeriodicTasks() {
        // This would integrate with the existing scheduler in ShowcaseManager
        // For now, just a placeholder
    }

    /**
     * Save statistics to persistent storage
     */
    public static void saveStatistics() {
        if (currentServer == null) {
            ShowcaseMod.LOGGER.debug("Server not set, skipping statistics save");
            return;
        }

        try {
            // Convert internal data to serializable format
            Map<String, Integer> globalStats = new HashMap<>();
            shareTypeGlobalStats.forEach((key, value) -> globalStats.put(key, value.get()));

            Map<String, PlayerStatisticsData> playerData = new HashMap<>();
            playerStatistics.forEach((uuid, stats) -> {
                // Convert shares by type
                Map<ShowcaseManager.ShareType, Integer> sharesByType = new HashMap<>();
                stats.sharesByType.forEach((type, count) -> sharesByType.put(type, count.get()));

                // Convert share durations to seconds
                List<Long> durationSeconds = stats.shareDurations.stream()
                    .map(Duration::getSeconds)
                    .toList();

                PlayerStatisticsData data = new PlayerStatisticsData(
                    stats.totalShares.get(),
                    stats.totalViews.get(),
                    sharesByType,
                    new HashMap<>(stats.sharesPerDay),
                    durationSeconds,
                    stats.mostSharedType,
                    stats.lastShareTime != null ? stats.lastShareTime.toEpochMilli() : null
                );
                playerData.put(uuid, data);
            });

            ServerStatisticsData data = new ServerStatisticsData(
                totalViewsEver.get(),
                globalStats,
                cache.getHits(),
                cache.getMisses(),
                playerData
            );

            GlobalDataManager.setData(currentServer, STATISTICS_STORAGE_ID, data);
            ShowcaseMod.LOGGER.debug("Statistics saved to persistent storage");
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to save statistics to storage", e);
        }
    }

    // Save debouncing
    private static volatile long lastSaveRequest = 0;
    private static final long SAVE_DEBOUNCE_MS = 5000; // 5 seconds

    /**
     * Schedule periodic statistics save
     */
    public static void schedulePeriodicSave() {
        // This would integrate with Minecraft's scheduler
        // For now, just trigger save on data changes
        ShowcaseMod.LOGGER.debug("Periodic save scheduled (placeholder)");
    }

    /**
     * Schedule asynchronous save with debouncing to avoid excessive saves
     */
    public static void scheduleAsyncSave() {
        long now = System.currentTimeMillis();
        lastSaveRequest = now;

        // Use a simple thread to handle debounced saves
        new Thread(() -> {
            try {
                Thread.sleep(SAVE_DEBOUNCE_MS);
                // Only save if no new save requests came in during debounce period
                if (System.currentTimeMillis() - lastSaveRequest >= SAVE_DEBOUNCE_MS) {
                    saveStatistics();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ShowcaseStatistics-AsyncSave").start();
    }

    /**
     * Get server statistics summary
     */
    public static Map<String, Object> getServerStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActiveShares", getTotalActiveShares());
        stats.put("totalViewsEver", totalViewsEver.get());
        stats.put("shareTypeStats", new HashMap<>(shareTypeGlobalStats));
        stats.put("totalPlayers", playerStatistics.size());
        return stats;
    }

    /**
     * Get player statistics summary
     */
    public static Map<String, Object> getPlayerStatisticsSummary(ServerPlayerEntity player) {
        if (player == null) return new HashMap<>();

        PlayerStats stats = getPlayerStats(player);
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalShares", stats.totalShares.get());
        summary.put("totalViews", stats.totalViews.get());
        summary.put("mostSharedType", stats.mostSharedType);
        summary.put("lastShareTime", stats.lastShareTime);
        summary.put("sharesByType", new HashMap<>(stats.sharesByType));
        return summary;
    }
}