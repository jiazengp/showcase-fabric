package com.showcase.event;

import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired when a player views a showcased item.
 * This event is fired before the showcase GUI is opened to the viewer.
 */
@ApiStatus.Experimental
public class ViewShowcaseEvent {
    private final ServerPlayerEntity viewer;
    private final ShareEntry shareEntry;
    private final String shareId;
    private final ServerPlayerEntity originalOwner;
    private boolean cancelled = false;

    /**
     * Creates a new ViewShowcaseEvent.
     *
     * @param viewer the player who is viewing the showcase
     * @param shareEntry the share entry being viewed
     * @param shareId the unique identifier of the showcase being viewed
     * @param originalOwner the original owner of the showcased content
     */
    public ViewShowcaseEvent(@NotNull ServerPlayerEntity viewer,
                            @NotNull ShareEntry shareEntry,
                            @NotNull String shareId,
                            @NotNull ServerPlayerEntity originalOwner) {
        this.viewer = viewer;
        this.shareEntry = shareEntry;
        this.shareId = shareId;
        this.originalOwner = originalOwner;
    }

    /**
     * Gets the player who is viewing the showcase.
     * 
     * @return the viewer player
     */
    @NotNull
    public ServerPlayerEntity getViewer() {
        return viewer;
    }

    /**
     * Gets the share entry being viewed.
     * 
     * @return the share entry
     */
    @NotNull
    public ShareEntry getShareEntry() {
        return shareEntry;
    }

    /**
     * Gets the unique identifier of the showcase being viewed.
     * 
     * @return the share ID
     */
    @NotNull
    public String getShareId() {
        return shareId;
    }

    /**
     * Gets the original owner of the showcased content.
     * 
     * @return the original owner player
     */
    @NotNull
    public ServerPlayerEntity getOriginalOwner() {
        return originalOwner;
    }

    /**
     * Gets the type of content being viewed.
     * 
     * @return the share type
     */
    @NotNull
    public ShowcaseManager.ShareType getShareType() {
        return shareEntry.getType();
    }

    /**
     * Gets the current view count of this showcase.
     * This includes the current view that is about to happen.
     * 
     * @return the view count
     */
    public int getViewCount() {
        return shareEntry.getViewCount();
    }

    /**
     * Gets the timestamp when this showcase was created.
     * 
     * @return the creation timestamp in milliseconds
     */
    public long getCreationTimestamp() {
        return shareEntry.getTimestamp();
    }

    /**
     * Gets the duration for which this showcase is valid.
     * 
     * @return the duration in seconds
     */
    public int getDuration() {
        return shareEntry.getDuration();
    }

    /**
     * Checks if the showcase has expired.
     * 
     * @return true if the showcase has expired, false otherwise
     */
    public boolean isExpired() {
        long expiryTime = shareEntry.getTimestamp() + shareEntry.getDuration() * 1000L;
        return System.currentTimeMillis() > expiryTime;
    }

    /**
     * Checks if the viewing is cancelled.
     * When cancelled, the viewer will not be able to view the showcase.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of this event.
     * When cancelled, the viewer will not be able to view the showcase.
     * 
     * @param cancelled true to cancel the viewing, false to allow it
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Checks if the viewer is the same as the original owner.
     * 
     * @return true if the viewer is the original owner, false otherwise
     */
    public boolean isViewingOwnShowcase() {
        return viewer.getUuid().equals(originalOwner.getUuid());
    }
}