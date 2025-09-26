package com.showcase.utils;

import com.showcase.ShowcaseMod;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

/**
 * Dedicated cache service for statistics data
 * Provides thread-safe caching with expiration and hit rate tracking
 */
public class StatisticsCache {

    // Cache storage with timestamps
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    // Cache performance metrics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    // Cache configuration
    private final long cacheDurationMs;
    private final long cleanupIntervalMs;
    private volatile long lastCleanup = 0;

    // Cache entry with timestamp and expiration
    private static class CacheEntry {
        public final Object value;
        public final long timestamp;
        public final long expirationTime;

        public CacheEntry(Object value, long cacheDurationMs) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
            this.expirationTime = timestamp + cacheDurationMs;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    /**
     * Constructor with default cache settings
     */
    public StatisticsCache() {
        this(30000, 60000); // 30s cache, 60s cleanup
    }

    /**
     * Constructor with custom cache settings
     * @param cacheDurationMs How long entries stay valid
     * @param cleanupIntervalMs How often to clean expired entries
     */
    public StatisticsCache(long cacheDurationMs, long cleanupIntervalMs) {
        this.cacheDurationMs = cacheDurationMs;
        this.cleanupIntervalMs = cleanupIntervalMs;
        ShowcaseMod.LOGGER.debug("StatisticsCache initialized with {}ms duration, {}ms cleanup interval",
            cacheDurationMs, cleanupIntervalMs);
    }

    /**
     * Get value from cache with type safety and hit tracking
     * @param key Cache key
     * @param type Expected value type
     * @return Cached value or null if not found/expired
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        // Perform periodic cleanup
        performCleanupIfNeeded();

        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired() && type.isInstance(entry.value)) {
            cacheHits.incrementAndGet();
            return (T) entry.value;
        }

        // Remove expired entry
        if (entry != null && entry.isExpired()) {
            cache.remove(key);
        }

        cacheMisses.incrementAndGet();
        return null;
    }

    /**
     * Put value into cache with automatic expiration
     * @param key Cache key
     * @param value Value to cache
     */
    public void put(String key, Object value) {
        if (value != null) {
            cache.put(key, new CacheEntry(value, cacheDurationMs));
        }
    }

    /**
     * Remove specific key from cache
     * @param key Key to remove
     */
    public void remove(String key) {
        cache.remove(key);
    }

    /**
     * Remove all cache entries matching a pattern
     * @param pattern Pattern to match (simple contains check)
     */
    public void removePattern(String pattern) {
        cache.entrySet().removeIf(entry -> entry.getKey().contains(pattern));
    }

    /**
     * Clear all cached entries
     */
    public void clear() {
        cache.clear();
        // Don't reset hit/miss counters as they're cumulative statistics
    }

    /**
     * Force cleanup of expired entries
     */
    public void forceCleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        lastCleanup = System.currentTimeMillis();
        ShowcaseMod.LOGGER.debug("Cache cleanup performed, {} entries remain", cache.size());
    }

    /**
     * Perform cleanup if needed based on interval
     */
    private void performCleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup >= cleanupIntervalMs) {
            forceCleanup();
        }
    }

    /**
     * Get cache hit rate percentage
     * @return Hit rate as percentage (0.0 to 100.0)
     */
    public double getHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (double) hits / total * 100.0;
    }

    /**
     * Get current cache size
     * @return Number of cached entries
     */
    public int getSize() {
        return cache.size();
    }

    /**
     * Get total cache hits
     * @return Number of cache hits
     */
    public long getHits() {
        return cacheHits.get();
    }

    /**
     * Get total cache misses
     * @return Number of cache misses
     */
    public long getMisses() {
        return cacheMisses.get();
    }

    /**
     * Get cache statistics summary
     * @return Map with cache performance metrics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("size", getSize());
        stats.put("hitRate", getHitRate());
        stats.put("hits", getHits());
        stats.put("misses", getMisses());
        stats.put("cacheDurationMs", cacheDurationMs);
        return stats;
    }

    /**
     * Generate cache key for player-specific data
     * @param playerUuid Player UUID
     * @param operation Operation name
     * @return Formatted cache key
     */
    public static String playerKey(String playerUuid, String operation) {
        return "player:" + playerUuid + ":" + operation;
    }

    /**
     * Generate cache key for server-wide data
     * @param operation Operation name
     * @return Formatted cache key
     */
    public static String serverKey(String operation) {
        return "server:" + operation;
    }

    /**
     * Generate cache key for type-specific data
     * @param type Type identifier
     * @param operation Operation name
     * @return Formatted cache key
     */
    public static String typeKey(String type, String operation) {
        return "type:" + type + ":" + operation;
    }
}