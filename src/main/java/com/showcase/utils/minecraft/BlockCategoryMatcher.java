package com.showcase.utils.minecraft;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKey;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handles pattern matching for block categorization based on item names
 */
public final class BlockCategoryMatcher {
    
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
            Pattern.compile(".*ore.*"),
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

    private BlockCategoryMatcher() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Categorizes a block item by its identifier name
     * @param itemName the item name (without namespace)
     * @return the appropriate item group category
     */
    public static RegistryKey<ItemGroup> categorizeByName(String itemName) {
        if (matchesColoredBlock(itemName)) return ItemGroups.COLORED_BLOCKS;
        if (matchesAny(NATURAL_BLOCK_PATTERNS, itemName)) return ItemGroups.NATURAL;
        if (matchesAny(FUNCTIONAL_BLOCK_PATTERNS, itemName)) return ItemGroups.FUNCTIONAL;
        if (matchesAny(REDSTONE_BLOCK_PATTERNS, itemName)) return ItemGroups.REDSTONE;
        if (matchesAny(COMBAT_BLOCK_PATTERNS, itemName)) return ItemGroups.COMBAT;
        if (matchesAny(INGREDIENT_BLOCK_PATTERNS, itemName)) return ItemGroups.INGREDIENTS;
        if (matchesAny(BUILDING_BLOCK_PATTERNS, itemName)) return ItemGroups.BUILDING_BLOCKS;

        return ItemGroups.BUILDING_BLOCKS; // default fallback
    }

    private static boolean matchesColoredBlock(String name) {
        return COLOR_PREFIXES.stream().anyMatch(name::startsWith)
                && COLORED_BLOCK_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(name).matches());
    }

    private static boolean matchesAny(List<Pattern> patterns, String name) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(name).matches());
    }
}