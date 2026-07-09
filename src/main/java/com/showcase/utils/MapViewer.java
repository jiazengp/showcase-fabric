package com.showcase.utils;

import com.showcase.config.ModConfigManager;
import com.showcase.gui.MapViewerContext;
import com.showcase.gui.MapViewerGui;
import com.showcase.utils.compat.ServerPlayerCompat;
import eu.pb4.sgui.api.containerwrappers.SlotBasedWrapperMenu;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MapViewer {
    private static final String MAP_MARK = "isShowcaseMap";
    private static final Map<UUID, ViewingSession> viewingSessions = new ConcurrentHashMap<>();

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (viewingSessions.isEmpty()) return;

            int currentTick = server.getTickCount();
            List<UUID> toRemove = new ArrayList<>();

            for (var entry : viewingSessions.entrySet()) {
                UUID playerId = entry.getKey();
                ViewingSession session = entry.getValue();

                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                if (player == null || currentTick >= session.autoRestoreTick) {
                    closeSession(player);
                    toRemove.add(playerId);
                }
            }

            toRemove.forEach(viewingSessions::remove);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> com.showcase.utils.compat.CallbackCompat.getUseItemCallbackResult(player, getCheckResult(player)));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> getCheckResult(player));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> getCheckResult(player));
    }

    private static InteractionResult getCheckResult(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        return isPlayerViewingMap(serverPlayer) ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    public static void open(ServerPlayer player, ItemStack map, int displaySeconds) {
        if (player == null || player.isSpectator() || !StackUtils.isMap(map)) return;

        UUID playerId = player.getUUID();
        closeSession(player);

        ItemStack displayMap = map.copy();
        CustomData customData = displayMap.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = customData.copyTag();
        nbt.putBoolean(MAP_MARK, true);
        displayMap.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        MapViewerContext context = new MapViewerContext(player, displayMap, displaySeconds);
        context.setCloseCallback(() -> handleMapViewerClose(player, displayMap));
        
        MapViewerGui gui = new MapViewerGui(context, 0);
        gui.open();

        viewingSessions.put(playerId, new ViewingSession(displayMap, ServerPlayerCompat.getWorld(player).getServer().getTickCount() + displaySeconds * 20));
    }

    public static void viewMap(ServerPlayer player, ItemStack mapItem) {
        if (player != null && mapItem != null) {
            int mapViewDuration = ModConfigManager.getConfig().mapViewDuration;
            if (mapViewDuration == -1) {
                // Map preview is disabled, do nothing
                return;
            }
            open(player, mapItem, mapViewDuration);
        }
    }

    private static void handleMapViewerClose(ServerPlayer player, ItemStack mapItem) {
        UUID playerId = player.getUUID();
        viewingSessions.remove(playerId);
    }

    public static boolean isPlayerViewingMap(ServerPlayer player) {
        return viewingSessions.containsKey(player.getUUID());
    }

    public static void closeSession(ServerPlayer player) {
        if (player == null) return;

        if (player.containerMenu instanceof SlotBasedWrapperMenu) {
            player.closeContainer();
        } else {
            UUID playerId = player.getUUID();
            ViewingSession session = viewingSessions.get(playerId);
            if (session != null) {
                handleMapViewerClose(player, session.displayMap);
            }
        }
    }

    public static void restoreAll(MinecraftServer server) {
        for (UUID playerId : new ArrayList<>(viewingSessions.keySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                closeSession(player);
            }
        }
        viewingSessions.clear();
    }

    public static int getViewingPlayersCount() {
        return viewingSessions.size();
    }

    public static boolean extendViewingTime(ServerPlayer player, int additionalSeconds) {
        UUID playerId = player.getUUID();
        ViewingSession session = viewingSessions.get(playerId);
        if (session == null) return false;

        int currentTick = ServerPlayerCompat.getWorld(player).getServer().getTickCount();
        int newTick = currentTick + additionalSeconds * 20;
        viewingSessions.put(playerId, new ViewingSession(session.displayMap, newTick));
        return true;
    }

    public static int getRemainingViewingTime(ServerPlayer player) {
        ViewingSession session = viewingSessions.get(player.getUUID());
        if (session == null) return 0;

        int currentTick = ServerPlayerCompat.getWorld(player).getServer().getTickCount();
        return Math.max(0, (session.autoRestoreTick - currentTick) / 20);
    }

    private record ViewingSession(ItemStack displayMap, int autoRestoreTick) {}
}
