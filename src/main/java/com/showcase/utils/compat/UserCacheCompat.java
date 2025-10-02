package com.showcase.utils.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;
#if MC_VER >= 1219
import net.minecraft.server.PlayerConfigEntry;
#endif

import java.util.Optional;
import java.util.UUID;

/**
 * Compatibility layer for UserCache APIs across different Minecraft versions.
 * Handles API differences in 1.21.9+.
 */
public class UserCacheCompat {

    /**
     * Gets the UserCache from MinecraftServer.
     * In 1.21.9+, getUserCache() may have been changed or removed.
     */
    public static UserCache getUserCache(ServerPlayerEntity player) {
        #if MC_VER >= 1219
        return (UserCache) player.getEntityWorld().getServer().getApiServices().nameToIdCache();
        #else
        return player.getServer().getUserCache();
        #endif
    }

    /**
     * Gets a GameProfile by UUID from UserCache.
     * In 1.21.9+, the return type changed from Optional<GameProfile> to Optional<PlayerConfigEntry>.
     */
    public static Optional<UUID> getByUuid(UserCache userCache, UUID uuid) {
        #if MC_VER >= 1219
        return userCache.getByUuid(uuid).map(PlayerConfigEntry::id);
        #else
        return userCache.getByUuid(uuid).map(profile -> profile.getId());
        #endif
    }
}
