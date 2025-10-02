package com.showcase.utils.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Compatibility layer for ServerPlayerEntity APIs across different Minecraft versions.
 * Handles API differences in 1.21.9+.
 */
public class ServerPlayerCompat {

    /**
     * Gets the MinecraftServer from a ServerPlayerEntity.
     * In 1.21.9+, getServer() was removed, use getEntityWorld().getServer() instead.
     */
    public static MinecraftServer getServer(ServerPlayerEntity player) {
        #if MC_VER >= 1219
        return player.getEntityWorld().getServer();
        #else
        return player.getServer();
        #endif
    }

    /**
     * Gets the ServerWorld from a ServerPlayerEntity.
     * In 1.21.9+, getWorld() was replaced with getEntityWorld().
     */
    public static ServerWorld getWorld(ServerPlayerEntity player) {
        #if MC_VER >= 1219
        return player.getEntityWorld();
        #else
        return (ServerWorld) player.getWorld();
        #endif
    }
}
