package com.showcase.gametest;

import com.showcase.api.ShowcaseAPI;
import com.showcase.command.ShowcaseManager;
import com.showcase.event.ShowcaseEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
    public void testEventSystemBasicFunctionality(TestContext context) {
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
            ServerPlayerEntity player = GameTestPlayerUtils.createTestPlayerCalled(context, "BasicTestPlayer");

            // Test showcase created event
            ActionResult createdResult = ShowcaseAPI.fireShowcaseCreatedEvent(
                ShowcaseManager.ShareType.ITEM,
                createMockShareEntry(),
                player, player, null,
                createdTestId, "Test showcase", 300
            );

            // Test showcase viewed event
            ActionResult viewedResult = ShowcaseAPI.fireShowcaseViewedEvent(
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
            context.throwGameTestException(Text.of("Basic functionality test failed: " + e.getMessage()));
        }

        context.complete();
    }

    @GameTest 
    public void testEventCancellationAndSuccess(TestContext context) {
        String cancelTestId = "cancel-test";
        String successTestId = "success-test";
        
        AtomicInteger cancelListenerCount = new AtomicInteger(0);
        AtomicInteger successListenerCount = new AtomicInteger(0);

        // Register cancelling listener
        ShowcaseEvents.SHOWCASE_CREATED.register((sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!cancelTestId.equals(shareId)) return ActionResult.PASS;
            cancelListenerCount.incrementAndGet();
            return ActionResult.FAIL; // Cancel the event
        });

        // Register listener that should not be called after cancellation
        ShowcaseEvents.SHOWCASE_CREATED.register((sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!cancelTestId.equals(shareId)) return ActionResult.PASS;
            cancelListenerCount.incrementAndGet();
            return ActionResult.PASS; // Should not execute
        });

        // Register success listeners
        ShowcaseEvents.SHOWCASE_VIEWED.register((viewer, shareEntry, shareId, originalOwner) -> {
            if (!successTestId.equals(shareId)) return ActionResult.PASS;
            successListenerCount.incrementAndGet();
            return ActionResult.PASS;
        });

        ShowcaseEvents.SHOWCASE_VIEWED.register((viewer, shareEntry, shareId, originalOwner) -> {
            if (!successTestId.equals(shareId)) return ActionResult.PASS;
            successListenerCount.incrementAndGet();
            return ActionResult.SUCCESS; // Complete successfully
        });

        ShowcaseEvents.SHOWCASE_VIEWED.register((viewer, shareEntry, shareId, originalOwner) -> {
            if (!successTestId.equals(shareId)) return ActionResult.PASS;
            successListenerCount.incrementAndGet();
            return ActionResult.PASS; // Should not execute
        });

        try {
            ServerPlayerEntity player = GameTestPlayerUtils.createTestPlayerCalled(context, "CancelSuccessTestPlayer");

            // Test cancellation
            ActionResult cancelResult = ShowcaseAPI.fireShowcaseCreatedEvent(
                ShowcaseManager.ShareType.ITEM, createMockShareEntry(),
                player, player, null, cancelTestId, null, null
            );

            // Test success
            ActionResult successResult = ShowcaseAPI.fireShowcaseViewedEvent(
                player, createMockShareEntry(), successTestId, player
            );

            // Verify cancellation behavior
            context.assertTrue(cancelResult == ActionResult.FAIL, 
                Text.of("Event should be cancelled"));
            context.assertTrue(cancelListenerCount.get() == 1, 
                Text.of("Only first listener should be called for cancellation, got: " + cancelListenerCount.get()));

            // Verify success behavior  
            context.assertTrue(successResult == ActionResult.SUCCESS,
                Text.of("Event should succeed"));
            context.assertTrue(successListenerCount.get() == 2,
                Text.of("First two listeners should be called for success, got: " + successListenerCount.get()));

        } catch (Exception e) {
            LOGGER.error("Cancellation/success test failed: {}", e.getMessage(), e);
            context.throwGameTestException(Text.of("Cancellation/success test failed: " + e.getMessage()));
        }

        context.complete();
    }

    @GameTest
    public void testAPIIntegration(TestContext context) {
        String apiTestId = "api-integration-test";
        AtomicBoolean apiEventReceived = new AtomicBoolean(false);

        ShowcaseAPI api = ShowcaseAPI.getInstance();

        // Test API registration
        api.onShowcaseCreated((sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!apiTestId.equals(shareId)) return ActionResult.PASS;
            apiEventReceived.set(true);
            return ActionResult.PASS;
        });

        try {
            ServerPlayerEntity player = GameTestPlayerUtils.createTestPlayerCalled(context, "APITestPlayer");
            
            ShowcaseAPI.fireShowcaseCreatedEvent(
                ShowcaseManager.ShareType.ITEM, createMockShareEntry(),
                player, player, null, apiTestId, null, null
            );

            context.assertTrue(apiEventReceived.get(), 
                Text.of("API-registered listener should receive event"));

        } catch (Exception e) {
            LOGGER.error("API integration test failed: {}", e.getMessage(), e);
            context.throwGameTestException(Text.of("API integration test failed: " + e.getMessage()));
        }

        context.complete();
    }

    // Helper methods

    private com.showcase.event.ShowcaseCreatedCallback createCreatedListener(
            String testId, AtomicBoolean eventFired, AtomicReference<String> capturedId) {
        return (sender, sourcePlayer, receivers, shareType, shareEntry, shareId, description, duration) -> {
            if (!testId.equals(shareId)) return ActionResult.PASS;
            eventFired.set(true);
            capturedId.set(shareId);
            return ActionResult.PASS;
        };
    }

    private com.showcase.event.ShowcaseViewedCallback createViewedListener(
            String testId, AtomicBoolean eventFired, AtomicReference<String> capturedId) {
        return (viewer, shareEntry, shareId, originalOwner) -> {
            if (!testId.equals(shareId)) return ActionResult.PASS;
            eventFired.set(true);
            capturedId.set(shareId);
            return ActionResult.PASS;
        };
    }

    private void verifyEventResults(TestContext context,
                                  boolean createdFired, String capturedCreatedId, String expectedCreatedId, ActionResult createdResult,
                                  boolean viewedFired, String capturedViewedId, String expectedViewedId, ActionResult viewedResult) {
        
        context.assertTrue(createdFired, Text.of("ShowcaseCreated event should fire"));
        context.assertTrue(expectedCreatedId.equals(capturedCreatedId), 
            Text.of("Created event should capture correct ID"));
        context.assertTrue(createdResult == ActionResult.PASS, 
            Text.of("Created event should return PASS"));

        context.assertTrue(viewedFired, Text.of("ShowcaseViewed event should fire"));
        context.assertTrue(expectedViewedId.equals(capturedViewedId), 
            Text.of("Viewed event should capture correct ID"));
        context.assertTrue(viewedResult == ActionResult.PASS, 
            Text.of("Viewed event should return PASS"));
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