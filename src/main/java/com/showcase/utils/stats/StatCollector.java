package com.showcase.utils.stats;

import com.showcase.utils.minecraft.BlockCategoryCache;
import com.showcase.utils.minecraft.BlockCategoryRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class StatCollector implements StatAPI {
    
    @Override
    public Map<String, Integer> getPlayerStats(ServerPlayer player) {
        Map<String, Integer> stats = new HashMap<>();
        StatsCounter statHandler = getStatHandler(player);
        
        addCustomStats(stats, statHandler);
        addBlockCategoryStats(stats, statHandler);
        addEntityStats(stats, statHandler);
        
        return stats;
    }

    private void addCustomStats(Map<String, Integer> stats, StatsCounter statHandler) {
        
        stats.put("stat.minecraft.play_time", statHandler.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)));
        stats.put("stat.minecraft.time_since_death", statHandler.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH)));
        stats.put("stat.minecraft.time_since_rest", statHandler.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)));
        stats.put("stat.minecraft.sneak_time", statHandler.getValue(Stats.CUSTOM.get(Stats.CROUCH_TIME)));
        stats.put("stat.minecraft.walk_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)));
        stats.put("stat.minecraft.crouch_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM)));
        stats.put("stat.minecraft.sprint_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)));
        stats.put("stat.minecraft.swim_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.SWIM_ONE_CM)));
        stats.put("stat.minecraft.fall_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.FALL_ONE_CM)));
        stats.put("stat.minecraft.climb_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.CLIMB_ONE_CM)));
        stats.put("stat.minecraft.fly_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.FLY_ONE_CM)));
        stats.put("stat.minecraft.walk_under_water_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.WALK_UNDER_WATER_ONE_CM)));
        stats.put("stat.minecraft.minecart_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.MINECART_ONE_CM)));
        stats.put("stat.minecraft.boat_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.BOAT_ONE_CM)));
        stats.put("stat.minecraft.pig_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.PIG_ONE_CM)));
        stats.put("stat.minecraft.horse_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.HORSE_ONE_CM)));
        stats.put("stat.minecraft.aviate_one_cm", statHandler.getValue(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM)));

        stats.put("stat.minecraft.jump", statHandler.getValue(Stats.CUSTOM.get(Stats.JUMP)));
        stats.put("stat.minecraft.drop", statHandler.getValue(Stats.CUSTOM.get(Stats.DROP)));
        stats.put("stat.minecraft.damage_dealt", statHandler.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT)));
        stats.put("stat.minecraft.damage_dealt_absorbed", statHandler.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_ABSORBED)));
        stats.put("stat.minecraft.damage_dealt_resisted", statHandler.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_RESISTED)));
        stats.put("stat.minecraft.damage_taken", statHandler.getValue(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN)));
        stats.put("stat.minecraft.damage_blocked_by_shield", statHandler.getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD)));
        stats.put("stat.minecraft.damage_absorbed", statHandler.getValue(Stats.CUSTOM.get(Stats.DAMAGE_ABSORBED)));
        stats.put("stat.minecraft.damage_resisted", statHandler.getValue(Stats.CUSTOM.get(Stats.DAMAGE_RESISTED)));
        stats.put("stat.minecraft.deaths", statHandler.getValue(Stats.CUSTOM.get(Stats.DEATHS)));
        stats.put("stat.minecraft.mob_kills", statHandler.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS)));
        stats.put("stat.minecraft.animals_bred", statHandler.getValue(Stats.CUSTOM.get(Stats.ANIMALS_BRED)));
        stats.put("stat.minecraft.player_kills", statHandler.getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS)));
        stats.put("stat.minecraft.fish_caught", statHandler.getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT)));
        stats.put("stat.minecraft.talked_to_villager", statHandler.getValue(Stats.CUSTOM.get(Stats.TALKED_TO_VILLAGER)));
        stats.put("stat.minecraft.traded_with_villager", statHandler.getValue(Stats.CUSTOM.get(Stats.TRADED_WITH_VILLAGER)));
        stats.put("stat.minecraft.eat_cake_slice", statHandler.getValue(Stats.CUSTOM.get(Stats.EAT_CAKE_SLICE)));
        stats.put("stat.minecraft.fill_cauldron", statHandler.getValue(Stats.CUSTOM.get(Stats.FILL_CAULDRON)));
        stats.put("stat.minecraft.use_cauldron", statHandler.getValue(Stats.CUSTOM.get(Stats.USE_CAULDRON)));
        stats.put("stat.minecraft.clean_armor", statHandler.getValue(Stats.CUSTOM.get(Stats.CLEAN_ARMOR)));
        stats.put("stat.minecraft.clean_banner", statHandler.getValue(Stats.CUSTOM.get(Stats.CLEAN_BANNER)));
        stats.put("stat.minecraft.clean_shulker_box", statHandler.getValue(Stats.CUSTOM.get(Stats.CLEAN_SHULKER_BOX)));
        stats.put("stat.minecraft.interact_with_brewingstand", statHandler.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_BREWINGSTAND)));
        stats.put("stat.minecraft.interact_with_beacon", statHandler.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_BEACON)));
        stats.put("stat.minecraft.inspect_dropper", statHandler.getValue(Stats.CUSTOM.get(Stats.INSPECT_DROPPER)));
        stats.put("stat.minecraft.inspect_hopper", statHandler.getValue(Stats.CUSTOM.get(Stats.INSPECT_HOPPER)));
        stats.put("stat.minecraft.inspect_dispenser", statHandler.getValue(Stats.CUSTOM.get(Stats.INSPECT_DISPENSER)));
        stats.put("stat.minecraft.play_noteblock", statHandler.getValue(Stats.CUSTOM.get(Stats.PLAY_NOTEBLOCK)));
        stats.put("stat.minecraft.tune_noteblock", statHandler.getValue(Stats.CUSTOM.get(Stats.TUNE_NOTEBLOCK)));
        stats.put("stat.minecraft.pot_flower", statHandler.getValue(Stats.CUSTOM.get(Stats.POT_FLOWER)));
        stats.put("stat.minecraft.trigger_trapped_chest", statHandler.getValue(Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST)));
        stats.put("stat.minecraft.open_enderchest", statHandler.getValue(Stats.CUSTOM.get(Stats.OPEN_ENDERCHEST)));
        stats.put("stat.minecraft.enchant_item", statHandler.getValue(Stats.CUSTOM.get(Stats.ENCHANT_ITEM)));
        stats.put("stat.minecraft.play_record", statHandler.getValue(Stats.CUSTOM.get(Stats.PLAY_RECORD)));
        stats.put("stat.minecraft.interact_with_furnace", statHandler.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_FURNACE)));
        stats.put("stat.minecraft.interact_with_crafting_table", statHandler.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_CRAFTING_TABLE)));
        stats.put("stat.minecraft.open_chest", statHandler.getValue(Stats.CUSTOM.get(Stats.OPEN_CHEST)));
        stats.put("stat.minecraft.sleep_in_bed", statHandler.getValue(Stats.CUSTOM.get(Stats.SLEEP_IN_BED)));
        stats.put("stat.minecraft.open_shulker_box", statHandler.getValue(Stats.CUSTOM.get(Stats.OPEN_SHULKER_BOX)));

    }

    private void addBlockCategoryStats(Map<String, Integer> stats, StatsCounter statHandler) {
        int totalBlocksPlaced = 0;
        int totalBlocksBroken = 0;
        Map<String, Integer> placedByCategory = new HashMap<>();
        Map<String, Integer> brokenByCategory = new HashMap<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            Item blockItem = block.asItem();
            if (blockItem != null) {
                int placed = statHandler.getValue(Stats.ITEM_USED, blockItem);
                if (placed > 0) {
                    totalBlocksPlaced += placed;
                    var category = BlockCategoryCache.getCachedCategory(blockItem);
                    String categoryKey = BlockCategoryRegistry.getTranslationKey(category);
                    placedByCategory.merge(categoryKey + ".placed", placed, Integer::sum);
                }
            }

            int broken = statHandler.getValue(Stats.BLOCK_MINED, block);
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
    
    
    private void addEntityStats(Map<String, Integer> stats, StatsCounter statHandler) {
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
            
            addStatIfNonZero(stats, "stat.minecraft.kill_entity." + entityId, 
                           statHandler.getValue(Stats.ENTITY_KILLED, entityType));
            addStatIfNonZero(stats, "stat.minecraft.entity_killed_by." + entityId, 
                           statHandler.getValue(Stats.ENTITY_KILLED_BY, entityType));
        });
    }
    
    private void addStatIfNonZero(Map<String, Integer> stats, String key, int value) {
        if (value > 0) {
            stats.put(key, value);
        }
    }
}