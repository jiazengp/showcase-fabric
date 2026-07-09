package com.showcase.utils.inventory;

import com.showcase.utils.inventory.ReadOnlyInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import com.showcase.utils.ScreenHandlerUtils;

/**
 * Common inventory operations extracted from StackUtils and other classes
 * Provides reusable methods for inventory handling and snapshotting
 */
public final class InventoryUtils {
    
    // Constants for inventory sizes
    public static final int PLAYER_INVENTORY_SIZE = 36;
    public static final int HOTBAR_SIZE = 9;
    public static final int ENDERCHEST_SIZE = 27;
    
    // Slot ranges for player inventory
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int MAIN_INVENTORY_START = 9;
    public static final int MAIN_INVENTORY_END = 35;
    
    private InventoryUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Creates a complete snapshot of a player's inventory (hotbar + main inventory)
     * @param player the player whose inventory to snapshot
     * @return read-only inventory containing the full player inventory
     */
    public static ReadOnlyInventory snapshotPlayerInventory(Inventory player) {
        int rows = (PLAYER_INVENTORY_SIZE + 8) / 9; // Calculate rows needed
        ReadOnlyInventory snapshot = new ReadOnlyInventory(
                PLAYER_INVENTORY_SIZE, 
                Component.translatable("container.inventory"), 
                ScreenHandlerUtils.handlerTypeForRows(rows)
        );
        
        // Copy hotbar (slots 0-8)
        for (int i = HOTBAR_START; i <= HOTBAR_END; i++) {
            snapshot.setItem(i, player.getItem(i).copy());
        }
        
        // Copy main inventory (slots 9-35) 
        for (int i = MAIN_INVENTORY_START; i <= MAIN_INVENTORY_END; i++) {
            snapshot.setItem(i, player.getItem(i).copy());
        }
        
        return snapshot;
    }
    
    /**
     * Creates a snapshot of just the player's hotbar
     * @param player the player whose hotbar to snapshot
     * @return read-only inventory containing the hotbar
     */
    public static ReadOnlyInventory snapshotHotbar(Inventory player) {
        ReadOnlyInventory snapshot = new ReadOnlyInventory(
                HOTBAR_SIZE,
                Component.translatable("showcase.inventory.hotbar"),
                MenuType.GENERIC_9x1
        );
        
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            snapshot.setItem(i, player.getItem(i).copy());
        }
        
        return snapshot;
    }
    
    /**
     * Creates a snapshot of any generic inventory
     * @param source the source inventory
     * @param displayName the display name for the snapshot
     * @return read-only copy of the inventory
     */
    public static ReadOnlyInventory snapshotInventory(Container source, Component displayName) {
        int size = source.getContainerSize();
        int rows = (size + 8) / 9; // Calculate rows needed, rounding up
        
        ReadOnlyInventory snapshot = new ReadOnlyInventory(
                size,
                displayName,
                ScreenHandlerUtils.handlerTypeForRows(rows)
        );
        
        for (int i = 0; i < size; i++) {
            snapshot.setItem(i, source.getItem(i).copy());
        }
        
        return snapshot;
    }
    
    /**
     * Creates a snapshot with a custom screen handler type
     * @param source the source inventory
     * @param displayName the display name
     * @param screenHandler the screen handler type to use
     * @return read-only copy of the inventory
     */
    public static ReadOnlyInventory snapshotInventory(Container source, Component displayName, MenuType<?> screenHandler) {
        ReadOnlyInventory snapshot = new ReadOnlyInventory(source.getContainerSize(), displayName, screenHandler);
        
        for (int i = 0; i < source.getContainerSize(); i++) {
            snapshot.setItem(i, source.getItem(i).copy());
        }
        
        return snapshot;
    }
    
    /**
     * Counts non-empty stacks in an inventory
     * @param inventory the inventory to count
     * @return number of non-empty item stacks
     */
    public static int countNonEmptyStacks(Container inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks if an inventory is completely empty
     * @param inventory the inventory to check
     * @return true if all slots are empty
     */
    public static boolean isEmpty(Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets the first non-empty item stack from an inventory
     * @param inventory the inventory to search
     * @return the first non-empty ItemStack, or ItemStack.EMPTY if none found
     */
    public static ItemStack getFirstNonEmptyStack(Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * Copies specific slots from one inventory to another
     * @param source the source inventory
     * @param target the target inventory
     * @param sourceStart starting slot in source
     * @param targetStart starting slot in target
     * @param count number of slots to copy
     */
    public static void copySlots(Container source, Container target, int sourceStart, int targetStart, int count) {
        for (int i = 0; i < count; i++) {
            int sourceSlot = sourceStart + i;
            int targetSlot = targetStart + i;
            
            if (sourceSlot < source.getContainerSize() && targetSlot < target.getContainerSize()) {
                target.setItem(targetSlot, source.getItem(sourceSlot).copy());
            }
        }
    }
}