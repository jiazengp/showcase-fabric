package com.showcase.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

public class ReadOnlySlot extends Slot {
    public ReadOnlySlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        return Optional.empty();
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY; // 禁止拿取
    }

    @Override
    public ItemStack insertStack(ItemStack stack) {
        return stack; // 原样返回，不插入
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        return stack; // 原样返回，不插入
    }

    @Override
    public void setStack(ItemStack stack) {}

    @Override
    public void setStackNoCallbacks(ItemStack stack) {}

    @Override
    public int getMaxItemCount() {
        return 0;
    }
}
