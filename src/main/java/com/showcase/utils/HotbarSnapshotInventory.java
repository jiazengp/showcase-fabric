package com.showcase.utils;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class HotbarSnapshotInventory extends ReadOnlyInventory {
    public HotbarSnapshotInventory(Inventory playerInv) {
        super(9, TextUtils.HOTBAR, MenuType.GENERIC_9x1);

        for (int i = 0; i < 9; i++) setItem(i, playerInv.getItem(i).copy());
    }
}