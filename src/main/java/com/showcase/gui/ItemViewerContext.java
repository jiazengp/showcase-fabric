package com.showcase.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ItemViewerContext extends BaseViewerContext {
    public ItemStack item;

    public ItemViewerContext(ServerPlayer player, ItemStack item) {
        super(player);
        this.item = item;
    }

    public ItemViewerContext(ServerPlayer player, ItemStack item, int durationSeconds) {
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