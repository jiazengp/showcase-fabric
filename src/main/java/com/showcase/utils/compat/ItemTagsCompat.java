package com.showcase.utils.compat;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import net.minecraft.block.ShulkerBoxBlock;

import java.util.HashSet;
import java.util.Set;

/**
 * Compatibility layer for ItemTags across different Minecraft versions.
 * Handles differences in available tags between 1.21.1 and 1.21.2+.
 */
public class ItemTagsCompat {
    
    private static final Set<Item> SHULKER_ITEMS = buildShulkerItems();

    private static Set<Item> buildShulkerItems() {
        Set<Item> set = new HashSet<>();
        set.add(Items.SHULKER_BOX); // 无色
        for (DyeColor color : DyeColor.values()) {
            set.add(ShulkerBoxBlock.get(color).asItem());
        }
        return set;
    }
    
    /**
     * Checks if an ItemStack is a shulker box.
     * In MC 1.21.1, ItemTags.SHULKER_BOXES doesn't exist, so we check manually.
     */
    public static boolean isShulkerBox(ItemStack stack) {
        #if MC_VER >= 1212
        return stack.isIn(ItemTags.SHULKER_BOXES);
        #else
        return SHULKER_ITEMS.contains(stack.getItem());
        #endif
    }
}