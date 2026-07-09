package com.showcase.utils.compat;

import net.minecraft.world.item.BlockItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * Helpers for item-tag checks that still need a manual fallback on the 26.x mainline.
 */
public class ItemTagsCompat {

    /**
     * Checks if an ItemStack is a shulker box.
     */
    public static boolean isShulkerBox(ItemStack stack) {
        return stack.is(ItemTags.SHULKER_BOXES)
            || stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }
}
