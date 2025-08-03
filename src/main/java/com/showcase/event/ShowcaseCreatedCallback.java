package com.showcase.event;

import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Callback interface for when a showcase is created.
 * <p>
 * Return values:
 * <ul>
 * <li>{@link ActionResult#SUCCESS}: Allow the showcase creation and prevent other listeners from running</li>
 * <li>{@link ActionResult#PASS}: Allow the showcase creation and let other listeners run</li>
 * <li>{@link ActionResult#FAIL}: Cancel the showcase creation</li>
 * </ul>
 */
@FunctionalInterface
public interface ShowcaseCreatedCallback {
    Event<ShowcaseCreatedCallback> EVENT = EventFactory.createArrayBacked(ShowcaseCreatedCallback.class,
        (listeners) -> (sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            for (ShowcaseCreatedCallback listener : listeners) {
                ActionResult result = listener.onShowcaseCreated(sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration);
                
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            
            return ActionResult.PASS;
        });

    /**
     * Called when a showcase is created.
     *
     * @param sender       the player who initiated the showcase command
     * @param sourcePlayer the player whose content is being showcased (may be different from sender for admin commands)
     * @param receivers    the players who will receive the showcase, null for public broadcast
     * @param shareType    the type of content being showcased
     * @param shareEntry   the share entry containing the showcased content
     * @param shareId      the unique identifier for this showcase
     * @param description  custom description for the showcase, null if none provided
     * @param duration     the duration in seconds for which the showcase will be available
     * @return the action result
     */
    ActionResult onShowcaseCreated(@NotNull ServerPlayerEntity sender,
                                   @NotNull ServerPlayerEntity sourcePlayer,
                                   @Nullable Collection<ServerPlayerEntity> receivers,
                                   @NotNull ShowcaseManager.ShareType shareType,
                                   @NotNull ShareEntry shareEntry,
                                   @NotNull String shareId,
                                   @Nullable String description,
                                   @Nullable Integer duration);
}