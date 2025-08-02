package com.showcase.api;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfig;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.event.ShowcaseEvent;
import com.showcase.event.ViewShowcaseEvent;
import com.showcase.utils.ModMetadataHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
     * Registers a listener for ShowcaseEvent.
     * The listener will be called whenever a player creates a showcase.
     * 
     * @param listener the event listener
     */
    public void onShowcaseCreated(@NotNull Consumer<ShowcaseEvent> listener) {
        ShowcaseEventManager.registerShowcaseListener(listener);
    }

    /**
     * Registers a listener for ViewShowcaseEvent.
     * The listener will be called whenever a player views a showcase.
     * 
     * @param listener the event listener
     */
    public void onShowcaseViewed(@NotNull Consumer<ViewShowcaseEvent> listener) {
        ShowcaseEventManager.registerViewListener(listener);
    }

    /**
     * Fires a ShowcaseEvent. This method is intended for internal use.
     * 
     * @param event the event to fire
     */
    @ApiStatus.Internal
    public static void fireShowcaseEvent(@NotNull ShowcaseEvent event) {
        ShowcaseEventManager.fireShowcaseEvent(event);
    }

    /**
     * Fires a ViewShowcaseEvent. This method is intended for internal use.
     * 
     * @param event the event to fire
     * @return true if the event was not cancelled, false if cancelled
     */
    @ApiStatus.Internal
    public static boolean fireViewEvent(@NotNull ViewShowcaseEvent event) {
        return ShowcaseEventManager.fireViewEvent(event);
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

    /**
     * Internal event manager for handling event listeners.
     */
    private static class ShowcaseEventManager {
        private static final List<Consumer<ShowcaseEvent>> showcaseListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
        private static final List<Consumer<ViewShowcaseEvent>> viewListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

        static void registerShowcaseListener(Consumer<ShowcaseEvent> listener) {
            showcaseListeners.add(listener);
        }

        static void registerViewListener(Consumer<ViewShowcaseEvent> listener) {
            viewListeners.add(listener);
        }

        static void fireShowcaseEvent(ShowcaseEvent event) {
            for (Consumer<ShowcaseEvent> listener : showcaseListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    ShowcaseMod.LOGGER.error("Error in ShowcaseEvent listener", e);
                }
            }
        }

        static boolean fireViewEvent(ViewShowcaseEvent event) {
            for (Consumer<ViewShowcaseEvent> listener : viewListeners) {
                try {
                    listener.accept(event);
                    if (event.isCancelled()) {
                        return false;
                    }
                } catch (Exception e) {
                    ShowcaseMod.LOGGER.error("Error in ViewShowcaseEvent listener", e);
                }
            }
            return !event.isCancelled();
        }
    }
}