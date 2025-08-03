package com.showcase.event;

import net.fabricmc.fabric.api.event.Event;

/**
 * Groups together all Showcase-related events.
 * This class provides static access to all event instances used by the Showcase mod.
 */
public final class ShowcaseEvents {
    private ShowcaseEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Called when a showcase is created.
     * This event is fired after the showcase has been successfully created and stored.
     */
    public static final Event<ShowcaseCreatedCallback> SHOWCASE_CREATED = ShowcaseCreatedCallback.EVENT;

    /**
     * Called when a player views a showcase.
     * This event is fired before the showcase GUI is opened to the viewer.
     * Can be cancelled to prevent viewing.
     */
    public static final Event<ShowcaseViewedCallback> SHOWCASE_VIEWED = ShowcaseViewedCallback.EVENT;
}