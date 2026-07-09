package com.showcase.utils.stats;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

/**
 * Provides vanilla Minecraft statistics using original stat identifiers.
 * This avoids creating custom translation keys for existing stats.
 */
public class VanillaStatProvider implements StatProvider {
    
    @Override
    public Map<String, Integer> collect(ServerPlayer player) {
        Map<String, Integer> stats = new HashMap<>();
        
        // Use stat identifiers directly - these are handled by vanilla translations
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.PLAY_TIME));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.DEATHS));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.DAMAGE_DEALT));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.DAMAGE_TAKEN));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.WALK_ONE_CM));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.SPRINT_ONE_CM));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.JUMP));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.TRADED_WITH_VILLAGER));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.OPEN_CHEST));
        addStatIfNonZero(stats, player, Stats.CUSTOM.get(Stats.SLEEP_IN_BED));
        
        return stats;
    }
    
    @Override
    public String getCategory() {
        return "stat_type.minecraft.custom"; // Using vanilla category translation key
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
    
    private void addStatIfNonZero(Map<String, Integer> stats, ServerPlayer player, Stat<?> stat) {
        int value = player.getStats().getValue(stat);
        if (value > 0) {
            // Use the stat's name directly for custom stats
            String statName = stat.getName();
            stats.put(statName, value);
        }
    }
}