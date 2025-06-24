package com.showcase.utils;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;

public class HotbarSnapshotInventory extends ReadOnlyInventory {
    public HotbarSnapshotInventory(PlayerInventory playerInv) {
        super(9, "container.inventory", ScreenHandlerType.GENERIC_9X1);

        for (int i = 0; i < 9; i++) {
            setStack(i, playerInv.getStack(i).copy());
        }
    }
}