package com.showcase.utils.stats;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

/**
 * Interface for collecting different types of player statistics
 * Allows for modular and extensible stat collection
 */
public interface StatProvider {
    
    /**
     * Collects statistics for a player
     * @param player the player to collect stats for
     * @return map of stat names to values
     */
    Map<String, Integer> collect(ServerPlayerEntity player);
    
    /**
     * Gets the category name for this stat provider
     * @return the category name for display
     */
    String getCategory();
    
    /**
     * Gets the priority of this provider (lower numbers = higher priority)
     * Used for ordering stats in displays
     * @return priority value
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * Checks if this provider should be enabled
     * @return true if the provider should collect stats
     */
    default boolean isEnabled() {
        return true;
    }
}