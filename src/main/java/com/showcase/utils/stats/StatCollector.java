package com.showcase.utils.stats;

import com.showcase.utils.minecraft.BlockCategoryCache;
import com.showcase.utils.minecraft.BlockCategoryRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;

import java.util.HashMap;
import java.util.Map;

public class StatCollector implements StatAPI {
    
    @Override
    public Map<String, Integer> getPlayerStats(ServerPlayerEntity player) {
        Map<String, Integer> stats = new HashMap<>();
        StatHandler statHandler = getStatHandler(player);
        
        addCustomStats(stats, statHandler);
        addBlockCategoryStats(stats, statHandler);
        addEntityStats(stats, statHandler);
        
        return stats;
    }

    private void addCustomStats(Map<String, Integer> stats, StatHandler statHandler) {
        
        stats.put("stat.minecraft.play_time", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)));
        stats.put("stat.minecraft.time_since_death", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH)));
        stats.put("stat.minecraft.time_since_rest", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)));
        stats.put("stat.minecraft.sneak_time", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SNEAK_TIME)));
        stats.put("stat.minecraft.walk_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM)));
        stats.put("stat.minecraft.crouch_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.CROUCH_ONE_CM)));
        stats.put("stat.minecraft.sprint_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM)));
        stats.put("stat.minecraft.swim_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SWIM_ONE_CM)));
        stats.put("stat.minecraft.fall_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FALL_ONE_CM)));
        stats.put("stat.minecraft.climb_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.CLIMB_ONE_CM)));
        stats.put("stat.minecraft.fly_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FLY_ONE_CM)));
        stats.put("stat.minecraft.walk_under_water_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_UNDER_WATER_ONE_CM)));
        stats.put("stat.minecraft.minecart_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MINECART_ONE_CM)));
        stats.put("stat.minecraft.boat_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.BOAT_ONE_CM)));
        stats.put("stat.minecraft.pig_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PIG_ONE_CM)));
        stats.put("stat.minecraft.horse_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.HORSE_ONE_CM)));
        stats.put("stat.minecraft.aviate_one_cm", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.AVIATE_ONE_CM)));

        stats.put("stat.minecraft.jump", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP)));
        stats.put("stat.minecraft.drop", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DROP)));
        stats.put("stat.minecraft.damage_dealt", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT)));
        stats.put("stat.minecraft.damage_dealt_absorbed", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT_ABSORBED)));
        stats.put("stat.minecraft.damage_dealt_resisted", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT_RESISTED)));
        stats.put("stat.minecraft.damage_taken", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_TAKEN)));
        stats.put("stat.minecraft.damage_blocked_by_shield", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_BLOCKED_BY_SHIELD)));
        stats.put("stat.minecraft.damage_absorbed", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_ABSORBED)));
        stats.put("stat.minecraft.damage_resisted", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_RESISTED)));
        stats.put("stat.minecraft.deaths", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)));
        stats.put("stat.minecraft.mob_kills", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS)));
        stats.put("stat.minecraft.animals_bred", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.ANIMALS_BRED)));
        stats.put("stat.minecraft.player_kills", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAYER_KILLS)));
        stats.put("stat.minecraft.fish_caught", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FISH_CAUGHT)));
        stats.put("stat.minecraft.talked_to_villager", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TALKED_TO_VILLAGER)));
        stats.put("stat.minecraft.traded_with_villager", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TRADED_WITH_VILLAGER)));
        stats.put("stat.minecraft.eat_cake_slice", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.EAT_CAKE_SLICE)));
        stats.put("stat.minecraft.fill_cauldron", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FILL_CAULDRON)));
        stats.put("stat.minecraft.use_cauldron", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.USE_CAULDRON)));
        stats.put("stat.minecraft.clean_armor", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.CLEAN_ARMOR)));
        stats.put("stat.minecraft.clean_banner", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.CLEAN_BANNER)));
        stats.put("stat.minecraft.clean_shulker_box", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.CLEAN_SHULKER_BOX)));
        stats.put("stat.minecraft.interact_with_brewingstand", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_BREWINGSTAND)));
        stats.put("stat.minecraft.interact_with_beacon", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_BEACON)));
        stats.put("stat.minecraft.inspect_dropper", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.INSPECT_DROPPER)));
        stats.put("stat.minecraft.inspect_hopper", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.INSPECT_HOPPER)));
        stats.put("stat.minecraft.inspect_dispenser", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.INSPECT_DISPENSER)));
        stats.put("stat.minecraft.play_noteblock", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_NOTEBLOCK)));
        stats.put("stat.minecraft.tune_noteblock", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TUNE_NOTEBLOCK)));
        stats.put("stat.minecraft.pot_flower", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.POT_FLOWER)));
        stats.put("stat.minecraft.trigger_trapped_chest", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TRIGGER_TRAPPED_CHEST)));
        stats.put("stat.minecraft.open_enderchest", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.OPEN_ENDERCHEST)));
        stats.put("stat.minecraft.enchant_item", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.ENCHANT_ITEM)));
        stats.put("stat.minecraft.play_record", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_RECORD)));
        stats.put("stat.minecraft.interact_with_furnace", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_FURNACE)));
        stats.put("stat.minecraft.interact_with_crafting_table", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_CRAFTING_TABLE)));
        stats.put("stat.minecraft.open_chest", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.OPEN_CHEST)));
        stats.put("stat.minecraft.sleep_in_bed", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SLEEP_IN_BED)));
        stats.put("stat.minecraft.open_shulker_box", statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.OPEN_SHULKER_BOX)));

    }

    private void addBlockCategoryStats(Map<String, Integer> stats, StatHandler statHandler) {
        int totalBlocksPlaced = 0;
        int totalBlocksBroken = 0;
        Map<String, Integer> placedByCategory = new HashMap<>();
        Map<String, Integer> brokenByCategory = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            Item blockItem = block.asItem();
            if (blockItem != null) {
                int placed = statHandler.getStat(Stats.USED, blockItem);
                if (placed > 0) {
                    totalBlocksPlaced += placed;
                    var category = BlockCategoryCache.getCachedCategory(blockItem);
                    String categoryKey = BlockCategoryRegistry.getTranslationKey(category);
                    placedByCategory.merge(categoryKey + ".placed", placed, Integer::sum);
                }
            }

            int broken = statHandler.getStat(Stats.MINED, block);
            if (broken > 0) {
                totalBlocksBroken += broken;
                if (blockItem != null) {
                    var category = BlockCategoryCache.getCachedCategory(blockItem);
                    String categoryKey = BlockCategoryRegistry.getTranslationKey(category);
                    brokenByCategory.merge(categoryKey + ".broken", broken, Integer::sum);
                }
            }
        }

        if (totalBlocksPlaced > 0) {
            stats.put("showcase.stats.blocks.total_placed", totalBlocksPlaced);
        }
        if (totalBlocksBroken > 0) {
            stats.put("showcase.stats.blocks.total_broken", totalBlocksBroken);
        }
        
        stats.putAll(placedByCategory);
        stats.putAll(brokenByCategory);
    }
    
    
    private void addEntityStats(Map<String, Integer> stats, StatHandler statHandler) {
        Registries.ENTITY_TYPE.forEach(entityType -> {
            String entityId = Registries.ENTITY_TYPE.getId(entityType).toString();
            
            addStatIfNonZero(stats, "stat.minecraft.kill_entity." + entityId, 
                           statHandler.getStat(Stats.KILLED, entityType));
            addStatIfNonZero(stats, "stat.minecraft.entity_killed_by." + entityId, 
                           statHandler.getStat(Stats.KILLED_BY, entityType));
        });
    }
    
    private void addStatIfNonZero(Map<String, Integer> stats, String key, int value) {
        if (value > 0) {
            stats.put(key, value);
        }
    }
}