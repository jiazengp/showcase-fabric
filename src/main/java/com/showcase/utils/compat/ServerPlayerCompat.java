package com.showcase.utils.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Helpers around ServerPlayer accessors on the 26.x mainline.
 */
public class ServerPlayerCompat {

    /**
     * Gets the MinecraftServer from a ServerPlayer.
     */
    public static MinecraftServer getServer(ServerPlayer player) {
        return player.level().getServer();
    }

    /**
     * Gets the ServerLevel from a ServerPlayer.
     */
    public static ServerLevel getWorld(ServerPlayer player) {
        return player.level();
    }
}
