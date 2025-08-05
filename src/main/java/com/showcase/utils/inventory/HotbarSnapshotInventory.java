package com.showcase.utils.inventory;

import com.showcase.utils.TextUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;

public class HotbarSnapshotInventory extends ReadOnlyInventory {
    public HotbarSnapshotInventory(PlayerInventory playerInv) {
        super(9, TextUtils.HOTBAR, ScreenHandlerType.GENERIC_9X1);

        for (int i = 0; i < 9; i++) setStack(i, playerInv.getStack(i).copy());
    }
}