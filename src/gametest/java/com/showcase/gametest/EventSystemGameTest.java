package com.showcase.gametest;

import com.showcase.api.ShowcaseAPI;
import com.showcase.command.ShowcaseManager;
import com.showcase.event.ShowcaseEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Optimized GameTest for the Fabric API-based event system.
 * Tests core event functionality with minimal redundancy.
 */
public class EventSystemGameTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSystemGameTest.class);

    @GameTest
    public void testEventSystemBasicFunctionality(GameTestHelper context) {
        // Test data
        String createdTestId = "basic-created-test";
        String viewedTestId = "basic-viewed-test";

        // Test state
        AtomicBoolean createdEventFired = new AtomicBoolean(false);
        AtomicBoolean viewedEventFired = new AtomicBoolean(false);
        AtomicReference<String> capturedCreatedId = new AtomicReference<>();
        AtomicReference<String> capturedViewedId = new AtomicReference<>();

        // Register listeners
        ShowcaseEvents.SHOWCASE_CREATED.register(createCreatedListener(createdTestId, createdEventFired, capturedCreatedId));
        ShowcaseEvents.SHOWCASE_VIEWED.register(createViewedListener(viewedTestId, viewedEventFired, capturedViewedId));

        try {
            ServerPlayer player = GameTestPlayerUtils.createTestPlayerCalled(context, "BasicTestPlayer");

            // Test showcase created event
            InteractionResult createdResult = ShowcaseAPI.fireShowcaseCreatedEvent(
                ShowcaseManager.ShareType.ITEM,
                createMockShareEntry(),
                player, player, null,
                createdTestId, "Test showcase", 300
            );

            // Test showcase viewed event
            InteractionResult viewedResult = ShowcaseAPI.fireShowcaseViewedEvent(
                player,
                createMockShareEntry(),
                viewedTestId,
                player
            );

            // Verify both events worked correctly
            verifyEventResults(context,
                createdEventFired.get(), capturedCreatedId.get(), createdTestId, createdResult,
                viewedEventFired.get(), capturedViewedId.get(), viewedTestId, viewedResult);

        } catch (Exception e) {
            LOGGER.error("Basic functionality test failed: {}", e.getMessage(), e);
            context.fail(Component.literal("Basic functionality test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    @GameTest
    public void testEventCancellationAndSuccess(GameTestHelper context) {
        String cancelTestId = "cancel-test";
        String successTestId = "success-test";

        AtomicInteger cancelListenerCount = new AtomicInteger(0);
        AtomicInteger successListenerCount = new AtomicInteger(0);

        // Register cancelling listener
        ShowcaseEvents.SHOWCASE_CREATED.register((sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!cancelTestId.equals(shareId)) return InteractionResult.PASS;
            cancelListenerCount.incrementAndGet();
            return InteractionResult.FAIL;
        });

        // Register listener that should not be called after cancellation
        ShowcaseEvents.SHOWCASE_CREATED.register((sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!cancelTestId.equals(shareId)) return InteractionResult.PASS;
            cancelListenerCount.incrementAndGet();
            return InteractionResult.PASS; // Should not execute
        });

        // Register success listeners
        ShowcaseEvents.SHOWCASE_VIEWED.register((viewer, shareEntry, shareId, originalOwner) -> {
            if (!successTestId.equals(shareId)) return InteractionResult.PASS;
            successListenerCount.incrementAndGet();
            return InteractionResult.PASS;
        });

        ShowcaseEvents.SHOWCASE_VIEWED.register((viewer, shareEntry, shareId, originalOwner) -> {
            if (!successTestId.equals(shareId)) return InteractionResult.PASS;
            successListenerCount.incrementAndGet();
            return InteractionResult.SUCCESS;
        });

        ShowcaseEvents.SHOWCASE_VIEWED.register((viewer, shareEntry, shareId, originalOwner) -> {
            if (!successTestId.equals(shareId)) return InteractionResult.PASS;
            successListenerCount.incrementAndGet();
            return InteractionResult.PASS; // Should not execute
        });

        try {
            ServerPlayer player = GameTestPlayerUtils.createTestPlayerCalled(context, "CancelSuccessTestPlayer");

            // Test cancellation
            InteractionResult cancelResult = ShowcaseAPI.fireShowcaseCreatedEvent(
                ShowcaseManager.ShareType.ITEM, createMockShareEntry(),
                player, player, null, cancelTestId, null, null
            );

            // Test success
            InteractionResult successResult = ShowcaseAPI.fireShowcaseViewedEvent(
                player, createMockShareEntry(), successTestId, player
            );

            // Verify cancellation behavior
            context.assertTrue(cancelResult == InteractionResult.FAIL,
                Component.literal("Event should be cancelled"));
            context.assertTrue(cancelListenerCount.get() == 1,
                Component.literal("Only first listener should be called for cancellation, got: " + cancelListenerCount.get()));

            // Verify success behavior
            context.assertTrue(successResult == InteractionResult.SUCCESS,
                Component.literal("Event should succeed"));
            context.assertTrue(successListenerCount.get() == 2,
                Component.literal("First two listeners should be called for success, got: " + successListenerCount.get()));

        } catch (Exception e) {
            LOGGER.error("Cancellation/success test failed: {}", e.getMessage(), e);
            context.fail(Component.literal("Cancellation/success test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    @GameTest
    public void testAPIIntegration(GameTestHelper context) {
        String apiTestId = "api-integration-test";
        AtomicBoolean apiEventReceived = new AtomicBoolean(false);

        ShowcaseAPI api = ShowcaseAPI.getInstance();

        // Test API registration
        api.onShowcaseCreated((sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!apiTestId.equals(shareId)) return InteractionResult.PASS;
            apiEventReceived.set(true);
            return InteractionResult.PASS;
        });

        try {
            ServerPlayer player = GameTestPlayerUtils.createTestPlayerCalled(context, "APITestPlayer");

            ShowcaseAPI.fireShowcaseCreatedEvent(
                ShowcaseManager.ShareType.ITEM, createMockShareEntry(),
                player, player, null, apiTestId, null, null
            );

            context.assertTrue(apiEventReceived.get(),
                Component.literal("API-registered listener should receive event"));

        } catch (Exception e) {
            LOGGER.error("API integration test failed: {}", e.getMessage(), e);
            context.fail(Component.literal("API integration test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    // Helper methods

    private com.showcase.event.ShowcaseCreatedCallback createCreatedListener(
            String testId, AtomicBoolean eventFired, AtomicReference<String> capturedId) {
        return (sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!testId.equals(shareId)) return InteractionResult.PASS;
            eventFired.set(true);
            capturedId.set(shareId);
            return InteractionResult.PASS;
        };
    }

    private com.showcase.event.ShowcaseViewedCallback createViewedListener(
            String testId, AtomicBoolean eventFired, AtomicReference<String> capturedId) {
        return (viewer, shareEntry, shareId, originalOwner) -> {
            if (!testId.equals(shareId)) return InteractionResult.PASS;
            eventFired.set(true);
            capturedId.set(shareId);
            return InteractionResult.PASS;
        };
    }

    private void verifyEventResults(GameTestHelper context,
                                  boolean createdFired, String capturedCreatedId, String expectedCreatedId, InteractionResult createdResult,
                                  boolean viewedFired, String capturedViewedId, String expectedViewedId, InteractionResult viewedResult) {

        context.assertTrue(createdFired, Component.literal("ShowcaseCreated event should fire"));
        context.assertTrue(expectedCreatedId.equals(capturedCreatedId),
            Component.literal("Created event should capture correct ID"));
        context.assertTrue(createdResult == InteractionResult.PASS,
            Component.literal("Created event should return PASS"));

        context.assertTrue(viewedFired, Component.literal("ShowcaseViewed event should fire"));
        context.assertTrue(expectedViewedId.equals(capturedViewedId),
            Component.literal("Viewed event should capture correct ID"));
        context.assertTrue(viewedResult == InteractionResult.PASS,
            Component.literal("Viewed event should return PASS"));
    }

    private com.showcase.data.ShareEntry createMockShareEntry() {
        return new com.showcase.data.ShareEntry(
            java.util.UUID.randomUUID(),
            ShowcaseManager.ShareType.ITEM,
            null, null,
            System.currentTimeMillis(), 300,
            0, false, new java.util.HashSet<>()
        );
    }
}
