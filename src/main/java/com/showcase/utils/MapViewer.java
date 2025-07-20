package com.showcase.utils;

import com.showcase.gui.MapViewerContext;
import com.showcase.gui.MapViewerGui;
import eu.pb4.sgui.api.GuiHelpers;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MapViewer {
    private static final String MAP_MARK = "isShowcaseMap";
    private static final Map<UUID, ViewingSession> viewingSessions = new ConcurrentHashMap<>();
    private static final Map<UUID, MapViewerHandler> closeCallbacks = new ConcurrentHashMap<>();

    public interface MapViewerHandler {
        void onClose(ServerPlayerEntity player, ItemStack mapItem);
    }

    static {
        // 每 Tick 检查是否需要自动关闭 GUI
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (viewingSessions.isEmpty()) return;

            int currentTick = server.getTicks();
            List<UUID> toRemove = new ArrayList<>();

            for (var entry : viewingSessions.entrySet()) {
                UUID playerId = entry.getKey();
                ViewingSession session = entry.getValue();

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if (player == null || currentTick >= session.autoRestoreTick) {
                    closeSession(player);
                    toRemove.add(playerId);
                }
            }

            toRemove.forEach(viewingSessions::remove);
        });

        // 阻止玩家在查看地图时进行交互
        UseItemCallback.EVENT.register((player, world, hand) -> getCheckResult(player));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> getCheckResult(player));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> getCheckResult(player));
    }

    private static ActionResult getCheckResult(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
        return isPlayerViewingMap(serverPlayer) ? ActionResult.FAIL : ActionResult.PASS;
    }

    public static void open(ServerPlayerEntity player, ItemStack map, int displaySeconds) {
        open(player, map, displaySeconds, null);
    }

    public static void open(ServerPlayerEntity player, ItemStack map, int displaySeconds, MapViewerHandler callback) {
        if (player == null || player.isSpectator() || !StackUtils.isMap(map)) return;

        UUID playerId = player.getUuid();
        closeSession(player);

        ItemStack displayMap = map.copy();
        NbtComponent customData = displayMap.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = customData.copyNbt();
        nbt.putBoolean(MAP_MARK, true);
        displayMap.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        if (callback != null) closeCallbacks.put(playerId, callback);

        MapViewerGui gui = new MapViewerGui(new MapViewerContext(player, displayMap), 0) {
            @Override
            public void onClose() {
                super.onClose();
                handleMapViewerClose(player, displayMap);
            }
        };

        gui.open();

        int autoRestoreTick = player.getWorld().getServer().getTicks() + displaySeconds * 20;
        viewingSessions.put(playerId, new ViewingSession(displayMap, autoRestoreTick));
    }

    public static void viewMap(ServerPlayerEntity player, ItemStack mapItem) {
        open(player, mapItem, 10);
    }

    public static void viewMap(ServerPlayerEntity player, ItemStack mapItem, MapViewerHandler callback) {
        if (player != null && mapItem != null) {
            open(player, mapItem, 10, callback);
        }
    }

    private static void handleMapViewerClose(ServerPlayerEntity player, ItemStack mapItem) {
        UUID playerId = player.getUuid();
        MapViewerHandler callback = closeCallbacks.remove(playerId);
        if (callback != null) {
            try {
                callback.onClose(player, mapItem);
            } catch (Exception ignored) {}
        }
        viewingSessions.remove(playerId);
    }

    public static boolean isPlayerViewingMap(ServerPlayerEntity player) {
        return viewingSessions.containsKey(player.getUuid());
    }

    public static void closeSession(ServerPlayerEntity player) {
        if (player == null) return;

        var currentGui = GuiHelpers.getCurrentGui(player);
        if (currentGui instanceof MapViewerGui viewerGui) {
            viewerGui.close();
        } else {
            UUID playerId = player.getUuid();
            ViewingSession session = viewingSessions.get(playerId);
            if (session != null) {
                handleMapViewerClose(player, session.displayMap);
            }
        }
    }

    public static void restoreAll(MinecraftServer server) {
        for (UUID playerId : new ArrayList<>(viewingSessions.keySet())) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
                closeSession(player);
            }
        }
        viewingSessions.clear();
        closeCallbacks.clear();
    }

    public static int getViewingPlayersCount() {
        return viewingSessions.size();
    }

    public static boolean extendViewingTime(ServerPlayerEntity player, int additionalSeconds) {
        UUID playerId = player.getUuid();
        ViewingSession session = viewingSessions.get(playerId);
        if (session == null) return false;

        int currentTick = player.getWorld().getServer().getTicks();
        int newTick = currentTick + additionalSeconds * 20;
        viewingSessions.put(playerId, new ViewingSession(session.displayMap, newTick));
        return true;
    }

    public static int getRemainingViewingTime(ServerPlayerEntity player) {
        ViewingSession session = viewingSessions.get(player.getUuid());
        if (session == null) return 0;

        int currentTick = player.getWorld().getServer().getTicks();
        return Math.max(0, (session.autoRestoreTick - currentTick) / 20);
    }

    public static void setCloseCallback(ServerPlayerEntity player, MapViewerHandler callback) {
        if (isPlayerViewingMap(player)) {
            closeCallbacks.put(player.getUuid(), callback);
        }
    }

    public static void removeCloseCallback(ServerPlayerEntity player) {
        closeCallbacks.remove(player.getUuid());
    }

    private record ViewingSession(ItemStack displayMap, int autoRestoreTick) {}
}
