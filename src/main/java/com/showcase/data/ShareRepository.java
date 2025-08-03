package com.showcase.data;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository class for managing share data storage and retrieval.
 */
public final class ShareRepository {
    private static final Map<String, ShareEntry> SHARES = new ConcurrentHashMap<>();

    private ShareRepository() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Stores a share entry with the given ID.
     *
     * @param shareId the unique identifier for the share
     * @param entry the share entry to store
     */
    public static void store(@NotNull String shareId, @NotNull ShareEntry entry) {
        SHARES.put(shareId, entry);
    }

    /**
     * Retrieves a share entry by ID.
     *
     * @param shareId the unique identifier of the share
     * @return the share entry, or null if not found
     */
    @Nullable
    public static ShareEntry get(@NotNull String shareId) {
        return SHARES.get(shareId);
    }

    /**
     * Removes a share entry by ID.
     *
     * @param shareId the unique identifier of the share
     * @return true if the share was removed, false if it didn't exist
     */
    public static boolean remove(@NotNull String shareId) {
        return SHARES.remove(shareId) != null;
    }

    /**
     * Gets all active shares.
     *
     * @return an unmodifiable view of all active shares
     */
    @NotNull
    public static Map<String, ShareEntry> getAllShares() {
        return Collections.unmodifiableMap(SHARES);
    }

    /**
     * Gets all shares owned by a specific player.
     *
     * @param playerUuid the UUID of the player
     * @return a list of shares owned by the player
     */
    @NotNull
    public static List<ShareEntry> getPlayerShares(@NotNull String playerUuid) {
        return SHARES.values().stream()
                .filter(entry -> entry.getOwnerUuid().toString().equals(playerUuid))
                .collect(Collectors.toList());
    }

    /**
     * Gets all shares owned by a specific player.
     *
     * @param player the player
     * @return a list of shares owned by the player
     */
    @NotNull
    public static List<ShareEntry> getPlayerShares(@NotNull ServerPlayerEntity player) {
        return getPlayerShares(player.getUuid().toString());
    }

    /**
     * Removes all shares owned by a specific player.
     *
     * @param playerUuid the UUID of the player
     * @return the number of shares removed
     */
    public static int removePlayerShares(@NotNull String playerUuid) {
        List<String> toRemove = SHARES.entrySet().stream()
                .filter(entry -> entry.getValue().getOwnerUuid().toString().equals(playerUuid))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        toRemove.forEach(SHARES::remove);
        return toRemove.size();
    }

    /**
     * Gets the total number of active shares.
     *
     * @return the number of active shares
     */
    public static int getShareCount() {
        return SHARES.size();
    }

    /**
     * Gets all share IDs.
     *
     * @return a list of all share IDs
     */
    @NotNull
    public static List<String> getAllShareIds() {
        return new ArrayList<>(SHARES.keySet());
    }

    /**
     * Checks if a share with the given ID exists.
     *
     * @param shareId the share ID to check
     * @return true if the share exists, false otherwise
     */
    public static boolean exists(@NotNull String shareId) {
        return SHARES.containsKey(shareId);
    }

    /**
     * Clears all shares.
     */
    public static void clear() {
        SHARES.clear();
    }

    /**
     * Loads shares from a map.
     *
     * @param shares the shares to load
     */
    public static void loadShares(@Nullable Map<String, ShareEntry> shares) {
        SHARES.clear();
        if (shares != null) {
            SHARES.putAll(shares);
        }
    }

    /**
     * Gets an unmodifiable view of the shares map.
     *
     * @return an unmodifiable view of the shares
     */
    @NotNull
    public static Map<String, ShareEntry> getUnmodifiableShares() {
        return Collections.unmodifiableMap(SHARES);
    }
}