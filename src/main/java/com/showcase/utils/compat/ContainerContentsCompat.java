package com.showcase.utils.compat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Version-agnostic reads of bundle and container item contents.
 *
 * <p>26.3 renamed the copy accessors on both components: {@code BundleContents#itemCopyStream} and
 * {@code ItemContainerContents#allItemsCopyStream} both became {@code itemCopies}. The accessors used here
 * kept their names and semantics across 26.1-26.3, so shared code reads through them rather than through
 * the renamed streams.
 */
public final class ContainerContentsCompat {

    private ContainerContentsCompat() {
    }

    /**
     * Copies the bundle's contents in slot order.
     */
    public static List<ItemStack> bundleItems(BundleContents contents) {
        List<ItemStackTemplate> templates = contents.items();
        List<ItemStack> items = new ArrayList<>(templates.size());
        for (ItemStackTemplate template : templates) {
            items.add(template.create());
        }
        return items;
    }

    /**
     * Copies the container's contents into a fixed-size list, preserving slot positions.
     */
    public static NonNullList<ItemStack> containerItems(ItemContainerContents contents, int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        contents.copyInto(items);
        return items;
    }
}
