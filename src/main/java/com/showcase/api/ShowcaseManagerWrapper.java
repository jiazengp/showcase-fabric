package com.showcase.api;

import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Wrapper class that provides access to ShowcaseManager functionality for external developers.
 * This class bridges the gap between the static utility ShowcaseManager and the API interface.
 */
@ApiStatus.Experimental
public class ShowcaseManagerWrapper {

    /**
     * Package-private constructor to ensure only ShowcaseAPI can create instances.
     */
    ShowcaseManagerWrapper() {
        // Restricted access
    }

    /**
     * Gets a share entry by its ID.
     *
     * @param shareId the unique identifier of the share
     * @return the share entry, or null if not found
     */
    @Nullable
    public ShareEntry getShareEntry(@NotNull String shareId) {
        return ShowcaseManager.getShareEntry(shareId);
    }

    /**
     * Gets all active shares.
     *
     * @return a map of share IDs to share entries
     */
    @NotNull
    public Map<String, ShareEntry> getAllActiveShares() {
        return ShowcaseManager.getAllActiveShares();
    }

    /**
     * Gets all active shares owned by a specific player.
     *
     * @param playerUuid the UUID of the player
     * @return a list of share entries owned by the player
     */
    @NotNull
    public List<ShareEntry> getPlayerShares(@NotNull String playerUuid) {
        return ShowcaseManager.getPlayerShares(playerUuid);
    }

    /**
     * Cancels a share by its ID.
     *
     * @param shareId the unique identifier of the share to cancel
     * @return true if the share was successfully cancelled, false if not found
     */
    public boolean cancelShare(@NotNull String shareId) {
        return ShowcaseManager.cancelShare(shareId);
    }

    /**
     * Checks if a player is on cooldown for a specific share type.
     *
     * @param player the player to check
     * @param type the share type
     * @return true if the player is on cooldown, false otherwise
     */
    public boolean isOnCooldown(@NotNull ServerPlayerEntity player, @NotNull ShowcaseManager.ShareType type) {
        return ShowcaseManager.isOnCooldown(player, type);
    }

    /**
     * Gets the remaining cooldown time for a player and share type.
     *
     * @param player the player to check
     * @param type the share type
     * @return the remaining cooldown time in seconds, or 0 if not on cooldown
     */
    public long getRemainingCooldown(@NotNull ServerPlayerEntity player, @NotNull ShowcaseManager.ShareType type) {
        return ShowcaseManager.getRemainingCooldown(player, type);
    }

    /**
     * Opens shared content for a viewer.
     * This method will fire ViewShowcaseEvent before opening.
     *
     * @param viewer the player who wants to view the share
     * @param shareId the unique identifier of the share
     * @return true if the content was successfully opened, false otherwise
     */
    public boolean openSharedContent(@NotNull ServerPlayerEntity viewer, @NotNull String shareId) {
        return ShowcaseManager.openSharedContent(viewer, shareId);
    }

    /**
     * Checks if a share with the given ID exists and is still valid.
     *
     * @param shareId the unique identifier of the share
     * @return true if the share exists and is valid, false otherwise
     */
    public boolean isValidShare(@NotNull String shareId) {
        ShareEntry entry = getShareEntry(shareId);
        if (entry == null) return false;
        
        long expiryTime = entry.getTimestamp() + entry.getDuration() * 1000L;
        return System.currentTimeMillis() <= expiryTime;
    }

    /**
     * Gets the total number of active shares.
     *
     * @return the number of active shares
     */
    public int getActiveShareCount() {
        return getAllActiveShares().size();
    }

    /**
     * Gets the total number of shares created by a specific player.
     *
     * @param playerUuid the UUID of the player
     * @return the number of shares created by the player
     */
    public int getPlayerShareCount(@NotNull String playerUuid) {
        return getPlayerShares(playerUuid).size();
    }
}