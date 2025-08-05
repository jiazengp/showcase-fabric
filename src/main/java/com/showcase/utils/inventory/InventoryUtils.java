package com.showcase.utils.inventory;

import com.showcase.utils.inventory.ReadOnlyInventory;
import com.showcase.utils.ScreenHandlerUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

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
    public static ReadOnlyInventory snapshotPlayerInventory(PlayerInventory player) {
        int rows = (PLAYER_INVENTORY_SIZE + 8) / 9; // Calculate rows needed
        ReadOnlyInventory snapshot = new ReadOnlyInventory(
                PLAYER_INVENTORY_SIZE, 
                Text.translatable("container.inventory"), 
                ScreenHandlerUtils.handlerTypeForRows(rows)
        );
        
        // Copy hotbar (slots 0-8)
        for (int i = HOTBAR_START; i <= HOTBAR_END; i++) {
            snapshot.setStack(i, player.getStack(i).copy());
        }
        
        // Copy main inventory (slots 9-35) 
        for (int i = MAIN_INVENTORY_START; i <= MAIN_INVENTORY_END; i++) {
            snapshot.setStack(i, player.getStack(i).copy());
        }
        
        return snapshot;
    }
    
    /**
     * Creates a snapshot of just the player's hotbar
     * @param player the player whose hotbar to snapshot
     * @return read-only inventory containing the hotbar
     */
    public static ReadOnlyInventory snapshotHotbar(PlayerInventory player) {
        ReadOnlyInventory snapshot = new ReadOnlyInventory(
                HOTBAR_SIZE,
                Text.translatable("showcase.inventory.hotbar"),
                ScreenHandlerType.GENERIC_9X1
        );
        
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            snapshot.setStack(i, player.getStack(i).copy());
        }
        
        return snapshot;
    }
    
    /**
     * Creates a snapshot of any generic inventory
     * @param source the source inventory
     * @param displayName the display name for the snapshot
     * @return read-only copy of the inventory
     */
    public static ReadOnlyInventory snapshotInventory(Inventory source, Text displayName) {
        int size = source.size();
        int rows = (size + 8) / 9; // Calculate rows needed, rounding up
        
        ReadOnlyInventory snapshot = new ReadOnlyInventory(
                size,
                displayName,
                ScreenHandlerUtils.handlerTypeForRows(rows)
        );
        
        for (int i = 0; i < size; i++) {
            snapshot.setStack(i, source.getStack(i).copy());
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
    public static ReadOnlyInventory snapshotInventory(Inventory source, Text displayName, ScreenHandlerType<?> screenHandler) {
        ReadOnlyInventory snapshot = new ReadOnlyInventory(source.size(), displayName, screenHandler);
        
        for (int i = 0; i < source.size(); i++) {
            snapshot.setStack(i, source.getStack(i).copy());
        }
        
        return snapshot;
    }
    
    /**
     * Counts non-empty stacks in an inventory
     * @param inventory the inventory to count
     * @return number of non-empty item stacks
     */
    public static int countNonEmptyStacks(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.getStack(i).isEmpty()) {
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
    public static boolean isEmpty(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.getStack(i).isEmpty()) {
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
    public static ItemStack getFirstNonEmptyStack(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
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
    public static void copySlots(Inventory source, Inventory target, int sourceStart, int targetStart, int count) {
        for (int i = 0; i < count; i++) {
            int sourceSlot = sourceStart + i;
            int targetSlot = targetStart + i;
            
            if (sourceSlot < source.size() && targetSlot < target.size()) {
                target.setStack(targetSlot, source.getStack(sourceSlot).copy());
            }
        }
    }
}