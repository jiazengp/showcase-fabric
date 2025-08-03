package com.showcase.event;

import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Event that is fired when a player showcases content.
 * This event is fired after the showcase has been successfully created.
 */
@ApiStatus.Experimental
public record ShowcaseEvent(ServerPlayerEntity sender, ServerPlayerEntity sourcePlayer,
                            Collection<ServerPlayerEntity> receivers, ShowcaseManager.ShareType shareType,
                            ShareEntry shareEntry, String shareId, String description, Integer duration) {
    /**
     * Creates a new ShowcaseEvent.
     *
     * @param sender       the player who initiated the showcase command
     * @param sourcePlayer the player whose content is being showcased (may be different from sender for admin commands)
     * @param receivers    the players who will receive the showcase, null for public broadcast
     * @param shareType    the type of content being showcased
     * @param shareEntry   the share entry containing the showcased content
     * @param shareId      the unique identifier for this showcase
     * @param description  custom description for the showcase, null if none provided
     * @param duration     the duration in seconds for which the showcase will be available
     */
    public ShowcaseEvent(@NotNull ServerPlayerEntity sender,
                         @NotNull ServerPlayerEntity sourcePlayer,
                         Collection<ServerPlayerEntity> receivers,
                         @NotNull ShowcaseManager.ShareType shareType,
                         @NotNull ShareEntry shareEntry,
                         @NotNull String shareId,
                         @Nullable String description,
                         @Nullable Integer duration) {
        this.sender = sender;
        this.sourcePlayer = sourcePlayer;
        this.receivers = receivers;
        this.shareType = shareType;
        this.shareEntry = shareEntry;
        this.shareId = shareId;
        this.description = description;
        this.duration = duration;
    }

    /**
     * Gets the player who initiated the showcase command.
     *
     * @return the sender player
     */
    @Override
    public @NotNull ServerPlayerEntity sender() {
        return sender;
    }

    /**
     * Gets the player whose content is being showcased.
     * This may be different from the sender in case of admin commands.
     *
     * @return the source player
     */
    @Override
    public @NotNull ServerPlayerEntity sourcePlayer() {
        return sourcePlayer;
    }

    /**
     * Gets the players who will receive the showcase.
     *
     * @return collection of receiver players, or null if this is a public broadcast
     */
    @Override
    public Collection<ServerPlayerEntity> receivers() {
        return receivers;
    }

    /**
     * Gets the type of content being showcased.
     *
     * @return the share type
     */
    @Override
    public @NotNull ShowcaseManager.ShareType shareType() {
        return shareType;
    }

    /**
     * Gets the share entry containing the showcased content.
     *
     * @return the share entry
     */
    @Override
    public @NotNull ShareEntry shareEntry() {
        return shareEntry;
    }

    /**
     * Gets the unique identifier for this showcase.
     *
     * @return the share ID
     */
    @Override
    public @NotNull String shareId() {
        return shareId;
    }

    /**
     * Gets the custom description for the showcase.
     *
     * @return the description, or null if none was provided
     */
    @Override
    public @Nullable String description() {
        return description;
    }

    /**
     * Gets the duration in seconds for which the showcase will be available.
     *
     * @return the duration in seconds, or null if using default duration
     */
    @Override
    public @Nullable Integer duration() {
        return duration;
    }

    /**
     * Checks if this is a public broadcast (visible to all players).
     *
     * @return true if this is a public broadcast, false if it's a private share
     */
    public boolean isPublicBroadcast() {
        return receivers == null;
    }

    /**
     * Checks if this is an admin showcase (sender is different from source player).
     *
     * @return true if this is an admin showcase, false otherwise
     */
    public boolean isAdminShowcase() {
        return !sender.getUuid().equals(sourcePlayer.getUuid());
    }
}