package com.showcase.utils.minecraft;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKey;

import java.util.Map;
import java.util.Set;

/**
 * Registry of supported block categories and their translation keys
 */
public final class BlockCategoryRegistry {
    
    public static final Map<RegistryKey<ItemGroup>, String> CATEGORY_TRANSLATION_KEYS = Map.of(
            ItemGroups.BUILDING_BLOCKS, "showcase.stats.block.building_blocks",
            ItemGroups.COLORED_BLOCKS, "showcase.stats.block.colored_blocks",
            ItemGroups.NATURAL, "showcase.stats.block.natural_blocks",
            ItemGroups.FUNCTIONAL, "showcase.stats.block.functional_blocks",
            ItemGroups.REDSTONE, "showcase.stats.block.redstone_blocks",
            ItemGroups.COMBAT, "showcase.stats.block.combat",
            ItemGroups.INGREDIENTS, "showcase.stats.block.ingredients"
    );
    
    private BlockCategoryRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Gets all supported categories
     * @return set of all category keys
     */
    public static Set<RegistryKey<ItemGroup>> getAllCategories() {
        return CATEGORY_TRANSLATION_KEYS.keySet();
    }
    
    /**
     * Gets the translation key for a category
     * @param category the item group category
     * @return the translation key, or a default if not found
     */
    public static String getTranslationKey(RegistryKey<ItemGroup> category) {
        return CATEGORY_TRANSLATION_KEYS.getOrDefault(category, "showcase.stats.block.unknown");
    }
    
    /**
     * Checks if a category is supported
     * @param category the item group category to check
     * @return true if the category is supported
     */
    public static boolean isSupported(RegistryKey<ItemGroup> category) {
        return CATEGORY_TRANSLATION_KEYS.containsKey(category);
    }
}