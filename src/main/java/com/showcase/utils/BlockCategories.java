package com.showcase.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class BlockCategories {
    private BlockCategories() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final Map<RegistryKey<ItemGroup>, String> CATEGORY_TRANSLATION_KEYS = Map.of(
            ItemGroups.BUILDING_BLOCKS, "showcase.stats.block.building_blocks",
            ItemGroups.COLORED_BLOCKS, "showcase.stats.block.colored_blocks",
            ItemGroups.NATURAL, "showcase.stats.block.natural_blocks",
            ItemGroups.FUNCTIONAL, "showcase.stats.block.functional_blocks",
            ItemGroups.REDSTONE, "showcase.stats.block.redstone_blocks",
            ItemGroups.COMBAT, "showcase.stats.block.combat",
            ItemGroups.INGREDIENTS, "showcase.stats.block.ingredients"
    );

    private static final Map<RegistryKey<ItemGroup>, Set<Item>> GROUP_ITEM_CACHE = new ConcurrentHashMap<>();
    private static volatile boolean cacheInitialized = false;

    private static final Set<String> COLOR_PREFIXES = Set.of(
            "white_", "orange_", "magenta_", "light_blue_", "yellow_",
            "lime_", "pink_", "gray_", "light_gray_", "cyan_", "purple_",
            "blue_", "brown_", "green_", "red_", "black_"
    );

    private static final List<Pattern> COLORED_BLOCK_PATTERNS = List.of(
            Pattern.compile(".*_(wool|concrete|terracotta|stained_glass|carpet|bed|banner|shulker_box)")
    );

    private static final List<Pattern> NATURAL_BLOCK_PATTERNS = List.of(
            Pattern.compile(".*(dirt|grass|sand|gravel|clay|snow|ice|leaves|sapling|flower|mushroom|kelp|seagrass|coral|tuff|calcite|bedrock|obsidian|netherrack|soul_sand|soul_soil).*"),
            Pattern.compile(".*ore.*"), // 矿石
            Pattern.compile("(stone|cobblestone|mossy_cobblestone)"),
            Pattern.compile("(?!.*polished).*(deepslate|granite|diorite|andesite).*"),
            Pattern.compile(".*_log.*")
    );

    private static final List<Pattern> FUNCTIONAL_BLOCK_PATTERNS = List.of(
            Pattern.compile(".*(crafting|furnace|chest|barrel|anvil|enchanting|brewing|cauldron|beacon|jukebox|lectern|loom|stonecutter|grindstone|smithing|cartography|fletching|composter|smoker|blast_furnace|respawn_anchor|lodestone|conduit).*")
    );

    private static final List<Pattern> REDSTONE_BLOCK_PATTERNS = List.of(
            Pattern.compile(".*(redstone|piston|hopper|dispenser|dropper|observer|repeater|comparator|lever|button|pressure_plate|rail|target|sculk_sensor|daylight_detector|tripwire_hook|noteblock|command_block|structure_block).*")
    );

    private static final List<Pattern> COMBAT_BLOCK_PATTERNS = List.of(
            Pattern.compile(".*(tnt|spawner|magma|cactus|sweet_berry|wither_rose).*")
    );

    private static final List<Pattern> INGREDIENT_BLOCK_PATTERNS = List.of(
            Pattern.compile(".*(cobweb|sponge|bookshelf|ladder|scaffolding|hay_block).*")
    );

    private static final List<Pattern> BUILDING_BLOCK_PATTERNS = List.of(
            Pattern.compile(".*(_planks|_wood|smooth_stone|stone_bricks|polished_|_bricks|chiseled_|cut_|sandstone|concrete|terracotta|glass|prismarine|purpur|end_stone|blackstone|basalt|quartz).*"),
            Pattern.compile(".*_block.*(iron|gold|diamond|emerald|netherite|copper|lapis).*")
    );

    /**
     * 获取方块的分类
     */
    public static RegistryKey<ItemGroup> getBlockCategory(Block block) {
        Item item = block.asItem();
        if (item == null) return ItemGroups.BUILDING_BLOCKS;

        ensureCacheInitialized();

        for (var entry : GROUP_ITEM_CACHE.entrySet()) {
            if (entry.getValue().contains(item)) {
                return entry.getKey();
            }
        }
        return ItemGroups.BUILDING_BLOCKS;
    }

    private static void ensureCacheInitialized() {
        if (!cacheInitialized) {
            synchronized (BlockCategories.class) {
                if (!cacheInitialized) {
                    populateCache();
                    cacheInitialized = true;
                }
            }
        }
    }

    private static void populateCache() {
        Map<RegistryKey<ItemGroup>, Set<Item>> tempCache = new HashMap<>();

        CATEGORY_TRANSLATION_KEYS.keySet().forEach(
                key -> tempCache.put(key, ConcurrentHashMap.newKeySet())
        );

        for (Block block : Registries.BLOCK) {
            Item item = block.asItem();
            if (item != null) {
                String itemId = Registries.ITEM.getId(item).toString();
                RegistryKey<ItemGroup> category = categorizeByItemId(itemId);
                tempCache.get(category).add(item);
            }
        }

        GROUP_ITEM_CACHE.putAll(tempCache);
    }

    private static RegistryKey<ItemGroup> categorizeByItemId(String itemId) {
        String itemName = itemId.replace("minecraft:", "");

        if (matchesColoredBlock(itemName)) return ItemGroups.COLORED_BLOCKS;
        if (matchesAny(NATURAL_BLOCK_PATTERNS, itemName)) return ItemGroups.NATURAL;
        if (matchesAny(FUNCTIONAL_BLOCK_PATTERNS, itemName)) return ItemGroups.FUNCTIONAL;
        if (matchesAny(REDSTONE_BLOCK_PATTERNS, itemName)) return ItemGroups.REDSTONE;
        if (matchesAny(COMBAT_BLOCK_PATTERNS, itemName)) return ItemGroups.COMBAT;
        if (matchesAny(INGREDIENT_BLOCK_PATTERNS, itemName)) return ItemGroups.INGREDIENTS;
        if (matchesAny(BUILDING_BLOCK_PATTERNS, itemName)) return ItemGroups.BUILDING_BLOCKS;

        return ItemGroups.BUILDING_BLOCKS;
    }

    private static boolean matchesColoredBlock(String name) {
        return COLOR_PREFIXES.stream().anyMatch(name::startsWith)
                && COLORED_BLOCK_PATTERNS.stream().anyMatch(p -> p.matcher(name).matches());
    }

    private static boolean matchesAny(List<Pattern> patterns, String name) {
        return patterns.stream().anyMatch(p -> p.matcher(name).matches());
    }

    public static String getCategoryTranslationKey(RegistryKey<ItemGroup> groupKey) {
        return CATEGORY_TRANSLATION_KEYS.getOrDefault(groupKey, "showcase.stats.block.unknown");
    }
}
