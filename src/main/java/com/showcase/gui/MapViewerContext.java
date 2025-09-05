package com.showcase.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MapViewerContext extends BaseViewerContext {
    public ItemStack map;

    public MapViewerContext(ServerPlayerEntity player, ItemStack map) {
        super(player);
        this.map = map;
    }

    public MapViewerContext(ServerPlayerEntity player, ItemStack map, int durationSeconds) {
        super(player, durationSeconds);
        this.map = map;
    }

    @Override
    public void close() {
        super.close();
        this.map = null;
    }

    @Override
    public boolean checkClosed() {
        return super.checkClosed() || this.map == null;
    }

    public ItemStack getMap() {
        return map;
    }
}
