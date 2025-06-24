package com.showcase.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

public class ReadOnlyFactory implements NamedScreenHandlerFactory {
    private final Inventory inventory;
    private final Text name;
    private final ScreenHandlerType<?> type;

    public ReadOnlyFactory(ScreenHandlerType<?> type, PlayerInventory playerInventory, Inventory inventory, Text name) {
        this.inventory = inventory;
        this.name = name;
        this.type = type;
    }

    @Override
    public Text getDisplayName() {
        return name;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ReadOnlyInventoryScreenHandler(type, syncId, playerInventory, inventory);
    }
}
