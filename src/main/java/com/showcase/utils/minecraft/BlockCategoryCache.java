package com.showcase.utils.minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
/**
 * Thread-safe cache for block categorization to avoid repeated pattern matching
 */
public final class BlockCategoryCache {
    
    private static final AtomicReference<Map<ResourceKey<CreativeModeTab>, Set<Item>>> CACHE_REF = 
            new AtomicReference<>();
    
    private BlockCategoryCache() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Gets the cached category mapping, initializing if necessary (thread-safe)
     * @return immutable view of the category cache
     */
    public static Map<ResourceKey<CreativeModeTab>, Set<Item>> getCache() {
        return CACHE_REF.updateAndGet(cache -> cache != null ? cache : initializeCache());
    }
    
    /**
     * Finds the category for a specific item using the cache
     * @param item the item to categorize
     * @return the item group category, or BUILDING_BLOCKS as default
     */
    public static ResourceKey<CreativeModeTab> getCachedCategory(Item item) {
        Map<ResourceKey<CreativeModeTab>, Set<Item>> cache = getCache();
        
        return cache.entrySet().stream()
                .filter(entry -> entry.getValue().contains(item))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(net.minecraft.world.item.CreativeModeTabs.BUILDING_BLOCKS);
    }
    
    /**
     * Clears the cache (useful for testing or when registry changes)
     */
    public static void clearCache() {
        CACHE_REF.set(null);
    }
    
    private static Map<ResourceKey<CreativeModeTab>, Set<Item>> initializeCache() {
        Map<ResourceKey<CreativeModeTab>, Set<Item>> cache = new HashMap<>();
        
        // Initialize sets for all known categories
        BlockCategoryRegistry.getAllCategories().forEach(
                category -> cache.put(category, ConcurrentHashMap.newKeySet())
        );
        
        // Categorize all blocks
        for (Block block : BuiltInRegistries.BLOCK) {
            Item item = block.asItem();
            if (item != null) {
                String itemName = BuiltInRegistries.ITEM.getKey(item).getPath(); // Get path without namespace
                ResourceKey<CreativeModeTab> category = BlockCategoryMatcher.categorizeByName(itemName);
                cache.get(category).add(item);
            }
        }
        
        return Map.copyOf(cache); // Return immutable copy
    }
}