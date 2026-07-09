package com.showcase.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class MapViewerContext extends BaseViewerContext {
    public ItemStack map;

    public MapViewerContext(ServerPlayer player, ItemStack map) {
        super(player);
        this.map = map;
    }

    public MapViewerContext(ServerPlayer player, ItemStack map, int durationSeconds) {
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
