package com.showcase.utils.stats;

import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfigManager;
import java.util.*;

public class StatCategories {
    private static final Map<String, Set<String>> CATEGORY_MAPPINGS = new LinkedHashMap<>();
    private static final Map<String, Set<String>> DYNAMIC_CATEGORY_MAPPINGS = new LinkedHashMap<>();
    
    static {
        CATEGORY_MAPPINGS.put("showcase.stats.category.time", Set.of(
            "stat.minecraft.play_time",
            "stat.minecraft.time_since_death", 
            "stat.minecraft.time_since_rest",
            "stat.minecraft.sneak_time"
        ));
        
        CATEGORY_MAPPINGS.put("showcase.stats.category.movement", Set.of(
            "stat.minecraft.walk_one_cm",
            "stat.minecraft.crouch_one_cm",
            "stat.minecraft.sprint_one_cm", 
            "stat.minecraft.swim_one_cm",
            "stat.minecraft.fall_one_cm",
            "stat.minecraft.climb_one_cm",
            "stat.minecraft.fly_one_cm",
            "stat.minecraft.walk_under_water_one_cm",
            "stat.minecraft.minecart_one_cm",
            "stat.minecraft.boat_one_cm",
            "stat.minecraft.pig_one_cm",
            "stat.minecraft.horse_one_cm",
            "stat.minecraft.aviate_one_cm",
            "stat.minecraft.jump"
        ));
        
        CATEGORY_MAPPINGS.put("showcase.stats.category.combat", Set.of(
            "stat.minecraft.damage_dealt",
            "stat.minecraft.damage_dealt_absorbed",
            "stat.minecraft.damage_dealt_resisted",
            "stat.minecraft.damage_taken",
            "stat.minecraft.damage_blocked_by_shield",
            "stat.minecraft.damage_absorbed", 
            "stat.minecraft.damage_resisted",
            "stat.minecraft.deaths",
            "stat.minecraft.mob_kills",
            "stat.minecraft.player_kills"
        ));
        
        CATEGORY_MAPPINGS.put("showcase.stats.category.interaction", Set.of(
            "stat.minecraft.animals_bred",
            "stat.minecraft.fish_caught",
            "stat.minecraft.talked_to_villager",
            "stat.minecraft.traded_with_villager",
            "stat.minecraft.interact_with_brewingstand",
            "stat.minecraft.interact_with_beacon",
            "stat.minecraft.interact_with_furnace",
            "stat.minecraft.interact_with_crafting_table",
            "stat.minecraft.open_chest",
            "stat.minecraft.open_enderchest",
            "stat.minecraft.open_shulker_box",
            "stat.minecraft.sleep_in_bed"
        ));
        
        CATEGORY_MAPPINGS.put("showcase.stats.category.misc", Set.of(
            "stat.minecraft.drop",
            "stat.minecraft.eat_cake_slice",
            "stat.minecraft.fill_cauldron",
            "stat.minecraft.use_cauldron",
            "stat.minecraft.clean_armor",
            "stat.minecraft.clean_banner",
            "stat.minecraft.clean_shulker_box",
            "stat.minecraft.inspect_dropper",
            "stat.minecraft.inspect_hopper",
            "stat.minecraft.inspect_dispenser",
            "stat.minecraft.play_noteblock",
            "stat.minecraft.tune_noteblock",
            "stat.minecraft.pot_flower",
            "stat.minecraft.trigger_trapped_chest",
            "stat.minecraft.enchant_item",
            "stat.minecraft.play_record"
        ));
        
        // 动态分类 - 这些分类会匹配所有以特定前缀开头的键
        DYNAMIC_CATEGORY_MAPPINGS.put("showcase.stats.category.blocks", Set.of(
            "showcase.stats.blocks.",
            "showcase.stats.block."
        ));
        
        DYNAMIC_CATEGORY_MAPPINGS.put("showcase.stats.category.items", Set.of(
            "stat.minecraft.use_item.",
            "stat.minecraft.craft_item.",
            "stat.minecraft.break_item.",
            "stat.minecraft.pickup.",
            "stat.minecraft.drop."
        ));
        
        DYNAMIC_CATEGORY_MAPPINGS.put("showcase.stats.category.entities", Set.of(
            "stat.minecraft.kill_entity.",
            "stat.minecraft.entity_killed_by."
        ));
    }
    
    public static Map<String, Map<String, Integer>> categorizeStats(Map<String, Integer> stats) {
        Map<String, Map<String, Integer>> categorized = new LinkedHashMap<>();
        var config = ModConfigManager.getConfig().statsDisplay;
        
        if (config.showTimeStats) {
            addCategoryIfEnabled(categorized, "showcase.stats.category.time", stats);
        }
        if (config.showMovementStats) {
            addCategoryIfEnabled(categorized, "showcase.stats.category.movement", stats);
        }
        if (config.showCombatStats) {
            addCategoryIfEnabled(categorized, "showcase.stats.category.combat", stats);
        }
        if (config.showInteractionStats) {
            addCategoryIfEnabled(categorized, "showcase.stats.category.interaction", stats);
        }
        if (config.showMiscStats) {
            addCategoryIfEnabled(categorized, "showcase.stats.category.misc", stats);
        }
        
        addDynamicCategoryIfEnabled(categorized, "showcase.stats.category.blocks", stats);
        addDynamicCategoryIfEnabled(categorized, "showcase.stats.category.items", stats);
        addDynamicCategoryIfEnabled(categorized, "showcase.stats.category.entities", stats);
        
        return categorized;
    }
    
    private static void addCategoryIfEnabled(Map<String, Map<String, Integer>> categorized, 
                                           String categoryKey, Map<String, Integer> allStats) {
        Map<String, Integer> categoryStats = new LinkedHashMap<>();
        Set<String> statKeys = CATEGORY_MAPPINGS.get(categoryKey);
        
        if (statKeys != null) {
            statKeys.forEach(key -> {
                Integer value = allStats.get(key);
                if (value != null) {
                    categoryStats.put(key, value);
                }
            });
            
            if (!categoryStats.isEmpty()) {
                categorized.put(categoryKey, categoryStats);
            }
        }
    }
    
    private static void addDynamicCategoryIfEnabled(Map<String, Map<String, Integer>> categorized, 
                                                  String categoryKey, Map<String, Integer> allStats) {
        Map<String, Integer> categoryStats = new LinkedHashMap<>();
        Set<String> prefixes = DYNAMIC_CATEGORY_MAPPINGS.get(categoryKey);
        
        if (prefixes != null) {
            allStats.entrySet().forEach(entry -> {
                String statKey = entry.getKey();
                Integer value = entry.getValue();
                
                if (value != null && value > 0) {
                    for (String prefix : prefixes) {
                        if (statKey.startsWith(prefix)) {
                            categoryStats.put(statKey, value);
                            break;
                        }
                    }
                }
            });
            
            if (!categoryStats.isEmpty()) {
                categorized.put(categoryKey, categoryStats);
            }
        }
    }
}