package com.showcase.utils;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

public class ReadOnlyInventory extends SimpleInventory {
    public String name;
    public Text textName;
    public ScreenHandlerType<?> type;

    public ReadOnlyInventory(int size, String name, ScreenHandlerType<?> type) {
        super(size);
        this.name = name;
        this.type = type;
    }

    public ReadOnlyInventory(int size, Text name, ScreenHandlerType<?> type) {
        super(size);
        this.textName = name;
        this.type = type;
    }

    public Text getName() {
        if (textName != null) {
            return this.textName;
        }
        return Text.translatableWithFallback(name, "Container");
    }

    public ScreenHandlerType<?> getType() {
        return type;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clear() {}

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return false;
    }
}
