package com.showcase.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class MapViewerContext {
    public final ServerPlayerEntity player;
    public ItemStack map;
    public final List<SwitchEntry> interfaceList = new ArrayList<>();


    public MapViewerContext(ServerPlayerEntity player, ItemStack map) {
        this.player = player;
        this.map = map;
    }

    public void close() {
        this.map = null;
    }

    public boolean checkClosed() {
        return this.map == null;
    }

    public ItemStack getMap() {
        return map;
    }

    @FunctionalInterface
    public interface SwitchableUi {
        void openUi(MapViewerContext context, int selectedSlot);
    }

    public record SwitchEntry(SwitchableUi ui, int currentSlot) {
        public void open(MapViewerContext context) {
            ui.openUi(context, currentSlot);
        }
    }
}
