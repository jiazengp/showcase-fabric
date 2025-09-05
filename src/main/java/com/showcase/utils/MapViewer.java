package com.showcase.utils;

import com.showcase.config.ModConfigManager;
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

    static {
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

        UseItemCallback.EVENT.register((player, world, hand) -> getCheckResult(player));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> getCheckResult(player));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> getCheckResult(player));
    }

    private static ActionResult getCheckResult(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
        return isPlayerViewingMap(serverPlayer) ? ActionResult.FAIL : ActionResult.PASS;
    }

    public static void open(ServerPlayerEntity player, ItemStack map, int displaySeconds) {
        if (player == null || player.isSpectator() || !StackUtils.isMap(map)) return;

        UUID playerId = player.getUuid();
        closeSession(player);

        ItemStack displayMap = map.copy();
        NbtComponent customData = displayMap.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = customData.copyNbt();
        nbt.putBoolean(MAP_MARK, true);
        displayMap.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        MapViewerContext context = new MapViewerContext(player, displayMap, displaySeconds);
        context.setCloseCallback(() -> handleMapViewerClose(player, displayMap));
        
        MapViewerGui gui = new MapViewerGui(context, 0);
        gui.open();

        viewingSessions.put(playerId, new ViewingSession(displayMap, player.getWorld().getServer().getTicks() + displaySeconds * 20));
    }

    public static void viewMap(ServerPlayerEntity player, ItemStack mapItem) {
        if (player != null && mapItem != null) {
            int mapViewDuration = ModConfigManager.getConfig().mapViewDuration;
            if (mapViewDuration == -1) {
                // Map preview is disabled, do nothing
                return;
            }
            open(player, mapItem, mapViewDuration);
        }
    }

    private static void handleMapViewerClose(ServerPlayerEntity player, ItemStack mapItem) {
        UUID playerId = player.getUuid();
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

    private record ViewingSession(ItemStack displayMap, int autoRestoreTick) {}
}
