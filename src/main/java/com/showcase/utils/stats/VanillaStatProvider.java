package com.showcase.utils.stats;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides vanilla Minecraft statistics using original stat identifiers.
 * This avoids creating custom translation keys for existing stats.
 */
public class VanillaStatProvider implements StatProvider {
    
    @Override
    public Map<String, Integer> collect(ServerPlayerEntity player) {
        Map<String, Integer> stats = new HashMap<>();
        
        // Use stat identifiers directly - these are handled by vanilla translations
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_TAKEN));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.JUMP));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.TRADED_WITH_VILLAGER));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.OPEN_CHEST));
        addStatIfNonZero(stats, player, Stats.CUSTOM.getOrCreateStat(Stats.SLEEP_IN_BED));
        
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
    
    private void addStatIfNonZero(Map<String, Integer> stats, ServerPlayerEntity player, Stat<?> stat) {
        int value = player.getStatHandler().getStat(stat);
        if (value > 0) {
            // Use the stat's name directly for custom stats
            String statName = stat.getName();
            stats.put(statName, value);
        }
    }
}