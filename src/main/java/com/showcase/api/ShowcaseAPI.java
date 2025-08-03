package com.showcase.api;

import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfig;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.event.ShowcaseCreatedCallback;
import com.showcase.event.ShowcaseViewedCallback;
import com.showcase.event.ShowcaseEvents;
import com.showcase.utils.ModMetadataHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ActionResult;

/**
 * Main API class for the Showcase mod.
 * Provides access to core functionality for other mods and plugins.
 */
@ApiStatus.Experimental
public final class ShowcaseAPI {
    private static final ShowcaseAPI INSTANCE = new ShowcaseAPI();
    
    private ShowcaseAPI() {
        // Private constructor to enforce singleton pattern
    }

    /**
     * Gets the singleton instance of the ShowcaseAPI.
     * 
     * @return the ShowcaseAPI instance
     */
    @NotNull
    public static ShowcaseAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a ShowcaseManager wrapper that provides access to core showcase functionality.
     * This provides access to methods for creating shares, managing active shares, and retrieving share data.
     * 
     * @return a ShowcaseManager wrapper instance
     */
    @NotNull
    public ShowcaseManagerWrapper getShowcaseManager() {
        return new ShowcaseManagerWrapper();
    }

    /**
     * Gets the current mod configuration.
     * 
     * @return the mod configuration
     */
    @NotNull
    public ModConfig getConfig() {
        return ModConfigManager.getConfig();
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
     * Registers a listener for showcase creation events.
     * The listener will be called whenever a player creates a showcase.
     * 
     * @param listener the event listener
     */
    public void onShowcaseCreated(@NotNull ShowcaseCreatedCallback listener) {
        ShowcaseEvents.SHOWCASE_CREATED.register(listener);
    }

    /**
     * Registers a listener for showcase viewing events.
     * The listener will be called whenever a player views a showcase.
     * 
     * @param listener the event listener
     */
    public void onShowcaseViewed(@NotNull ShowcaseViewedCallback listener) {
        ShowcaseEvents.SHOWCASE_VIEWED.register(listener);
    }

    /**
     * Fires a showcase creation event. This method is intended for internal use.
     * 
     * @return the action result from the event
     */
    @ApiStatus.Internal
    public static ActionResult fireShowcaseCreatedEvent(@NotNull com.showcase.command.ShowcaseManager.ShareType shareType,
                                                         @NotNull com.showcase.data.ShareEntry shareEntry,
                                                         @NotNull net.minecraft.server.network.ServerPlayerEntity sender,
                                                         @NotNull net.minecraft.server.network.ServerPlayerEntity sourcePlayer,
                                                         @org.jetbrains.annotations.Nullable java.util.Collection<net.minecraft.server.network.ServerPlayerEntity> receivers,
                                                         @NotNull String shareId,
                                                         @org.jetbrains.annotations.Nullable String description,
                                                         @org.jetbrains.annotations.Nullable Integer duration) {
        return ShowcaseEvents.SHOWCASE_CREATED.invoker().onShowcaseCreated(sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration);
    }

    /**
     * Fires a showcase viewing event. This method is intended for internal use.
     * 
     * @return the action result from the event
     */
    @ApiStatus.Internal
    public static ActionResult fireShowcaseViewedEvent(@NotNull net.minecraft.server.network.ServerPlayerEntity viewer,
                                                        @NotNull com.showcase.data.ShareEntry shareEntry,
                                                        @NotNull String shareId,
                                                        @NotNull net.minecraft.server.network.ServerPlayerEntity originalOwner) {
        return ShowcaseEvents.SHOWCASE_VIEWED.invoker().onShowcaseViewed(viewer, shareEntry, shareId, originalOwner);
    }

    /**
     * Gets the version of the Showcase mod.
     * 
     * @return the mod version string
     */
    @NotNull
    public String getModVersion() {
        return ModMetadataHolder.VERSION;
    }

}