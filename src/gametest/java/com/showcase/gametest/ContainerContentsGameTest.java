package com.showcase.gametest;

import com.showcase.utils.ReadOnlyInventory;
import com.showcase.utils.StackUtils;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

/**
 * Covers unpacking of nested container items into a read-only view.
 *
 * <p>26.3 renamed the copy accessors on both component types, so these tests pin the slot layout that
 * {@link StackUtils#unpackFromItemStack} produces for bundles and shulker boxes.
 */
public class ContainerContentsGameTest {

    @GameTest
    public void testShulkerBoxContentsKeepSlotPositions(GameTestHelper context) {
        ItemStack shulkerBox = new ItemStack(Items.SHULKER_BOX);

        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            contents.add(ItemStack.EMPTY);
        }
        contents.set(0, new ItemStack(Items.DIAMOND, 3));
        contents.set(5, new ItemStack(Items.EMERALD, 7));
        contents.set(26, new ItemStack(Items.GOLD_INGOT, 2));
        shulkerBox.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));

        if (!StackUtils.isShulkerBox(shulkerBox)) {
            context.fail(Component.literal("Shulker box item was not detected as a shulker box"));
            return;
        }

        ReadOnlyInventory inventory = StackUtils.unpackFromItemStack(shulkerBox);
        if (inventory == null) {
            context.fail(Component.literal("unpackFromItemStack returned null for a shulker box"));
            return;
        }

        context.assertTrue(inventory.getContainerSize() == 27,
                Component.literal("Expected 27 slots, got " + inventory.getContainerSize()));
        assertSlot(context, inventory, 0, Items.DIAMOND, 3);
        assertSlot(context, inventory, 5, Items.EMERALD, 7);
        assertSlot(context, inventory, 26, Items.GOLD_INGOT, 2);
        context.assertTrue(inventory.getItem(1).isEmpty(),
                Component.literal("Slot 1 should stay empty"));
        context.succeed();
    }

    @GameTest
    public void testBundleContentsKeepOrder(GameTestHelper context) {
        ItemStack bundle = new ItemStack(Items.BUNDLE);

        List<ItemStackTemplate> templates = List.of(
                ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.DIAMOND, 2)),
                ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.EMERALD, 4)),
                ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.STICK, 8))
        );
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(templates));

        if (!StackUtils.isBundle(bundle)) {
            context.fail(Component.literal("Bundle item was not detected as a bundle"));
            return;
        }

        ReadOnlyInventory inventory = StackUtils.unpackFromItemStack(bundle);
        if (inventory == null) {
            context.fail(Component.literal("unpackFromItemStack returned null for a bundle"));
            return;
        }

        assertSlot(context, inventory, 0, Items.DIAMOND, 2);
        assertSlot(context, inventory, 1, Items.EMERALD, 4);
        assertSlot(context, inventory, 2, Items.STICK, 8);
        context.succeed();
    }

    @GameTest
    public void testNonContainerItemIsNotUnpacked(GameTestHelper context) {
        ItemStack plain = new ItemStack(Items.DIAMOND, 5);
        context.assertTrue(StackUtils.unpackFromItemStack(plain) == null,
                Component.literal("A plain item should not unpack into a container view"));
        context.succeed();
    }

    private void assertSlot(GameTestHelper context, ReadOnlyInventory inventory, int slot, Item item, int count) {
        ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty()) {
            context.fail(Component.literal("Slot " + slot + " should hold " + item + " but was empty"));
            return;
        }
        context.assertTrue(stack.is(item),
                Component.literal("Slot " + slot + " should hold " + item + " but held " + stack.getItem()));
        context.assertTrue(stack.getCount() == count,
                Component.literal("Slot " + slot + " should hold " + count + " but held " + stack.getCount()));
    }
}
