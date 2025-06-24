package com.showcase.screen;

import com.showcase.utils.ReadOnlySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ReadOnlyInventoryScreenHandler extends ScreenHandler {
    public ReadOnlyInventoryScreenHandler( ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(type, syncId);

        int totalSize = inventory.size();
        int rowCount = (totalSize + 8) / 9;
        int startY = 18;

        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = row * 9 + col;
                if (index >= totalSize) break;

                this.addSlot(new ReadOnlySlot(inventory, index, 8 + col * 18, startY + row * 18));
            }
        }

        int playerInvStartY = startY + rowCount * 18 + 14;

        // 玩家物品栏（中间三行）
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = col + row * 9 + 9;
                this.addSlot(new ReadOnlySlot(playerInventory, index, 8 + col * 18, playerInvStartY + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new ReadOnlySlot(playerInventory, col, 8 + col * 18, playerInvStartY + 58));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }
}
