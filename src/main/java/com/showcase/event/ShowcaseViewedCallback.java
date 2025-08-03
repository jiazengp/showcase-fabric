package com.showcase.event;

import com.showcase.data.ShareEntry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;

/**
 * Callback interface for when a showcase is viewed.
 * This event is fired before the showcase GUI is opened to the viewer.
 * <p>
 * Return values:
 * <ul>
 * <li>{@link ActionResult#SUCCESS}: Allow the viewing and prevent other listeners from running</li>
 * <li>{@link ActionResult#PASS}: Allow the viewing and let other listeners run</li>
 * <li>{@link ActionResult#FAIL}: Cancel the viewing</li>
 * </ul>
 */
@FunctionalInterface
public interface ShowcaseViewedCallback {
    Event<ShowcaseViewedCallback> EVENT = EventFactory.createArrayBacked(ShowcaseViewedCallback.class,
        (listeners) -> (viewer, shareEntry, shareId, originalOwner) -> {
            for (ShowcaseViewedCallback listener : listeners) {
                ActionResult result = listener.onShowcaseViewed(viewer, shareEntry, shareId, originalOwner);
                
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            
            return ActionResult.PASS;
        });

    /**
     * Called when a player views a showcase.
     *
     * @param viewer        the player who is viewing the showcase
     * @param shareEntry    the share entry being viewed
     * @param shareId       the unique identifier of the showcase being viewed
     * @param originalOwner the original owner of the showcased content
     * @return the action result
     */
    ActionResult onShowcaseViewed(@NotNull ServerPlayerEntity viewer,
                                  @NotNull ShareEntry shareEntry,
                                  @NotNull String shareId,
                                  @NotNull ServerPlayerEntity originalOwner);
}