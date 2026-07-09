package com.showcase.utils.compat;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.NameAndId;

/**
 * Helpers around the current server-side user-cache APIs.
 */
public class UserCacheCompat {

    /**
     * Gets the current server name-to-id cache.
     */
    public static CachedUserNameToIdResolver getUserCache(ServerPlayer player) {
        return (CachedUserNameToIdResolver) player.level().getServer().services().nameToIdCache();
    }

    /**
     * Resolves the cached UUID entry.
     */
    public static Optional<UUID> getByUuid(CachedUserNameToIdResolver userCache, UUID uuid) {
        return userCache.get(uuid).map(NameAndId::id);
    }
}
