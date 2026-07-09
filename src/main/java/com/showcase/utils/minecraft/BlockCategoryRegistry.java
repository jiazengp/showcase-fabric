package com.showcase.utils.minecraft;

import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

/**
 * Registry of supported block categories and their translation keys
 */
public final class BlockCategoryRegistry {
    
    public static final Map<ResourceKey<CreativeModeTab>, String> CATEGORY_TRANSLATION_KEYS = Map.of(
            CreativeModeTabs.BUILDING_BLOCKS, "showcase.stats.block.building_blocks",
            CreativeModeTabs.COLORED_BLOCKS, "showcase.stats.block.colored_blocks",
            CreativeModeTabs.NATURAL_BLOCKS, "showcase.stats.block.natural_blocks",
            CreativeModeTabs.FUNCTIONAL_BLOCKS, "showcase.stats.block.functional_blocks",
            CreativeModeTabs.REDSTONE_BLOCKS, "showcase.stats.block.redstone_blocks",
            CreativeModeTabs.COMBAT, "showcase.stats.block.combat",
            CreativeModeTabs.INGREDIENTS, "showcase.stats.block.ingredients"
    );
    
    private BlockCategoryRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Gets all supported categories
     * @return set of all category keys
     */
    public static Set<ResourceKey<CreativeModeTab>> getAllCategories() {
        return CATEGORY_TRANSLATION_KEYS.keySet();
    }
    
    /**
     * Gets the translation key for a category
     * @param category the item group category
     * @return the translation key, or a default if not found
     */
    public static String getTranslationKey(ResourceKey<CreativeModeTab> category) {
        return CATEGORY_TRANSLATION_KEYS.getOrDefault(category, "showcase.stats.block.unknown");
    }
    
    /**
     * Checks if a category is supported
     * @param category the item group category to check
     * @return true if the category is supported
     */
    public static boolean isSupported(ResourceKey<CreativeModeTab> category) {
        return CATEGORY_TRANSLATION_KEYS.containsKey(category);
    }
}