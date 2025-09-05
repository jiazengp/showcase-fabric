package com.showcase.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ItemViewerContext extends BaseViewerContext {
    public ItemStack item;

    public ItemViewerContext(ServerPlayerEntity player, ItemStack item) {
        super(player);
        this.item = item;
    }

    public ItemViewerContext(ServerPlayerEntity player, ItemStack item, int durationSeconds) {
        super(player, durationSeconds);
        this.item = item;
    }

    @Override
    public void close() {
        super.close();
        this.item = null;
    }

    @Override
    public boolean checkClosed() {
        return super.checkClosed() || this.item == null;
    }

    public ItemStack getItem() {
        return item;
    }
}