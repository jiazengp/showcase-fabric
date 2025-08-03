package com.showcase.test.data;

import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import com.showcase.test.ShowcaseTestHelper;
import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ShareEntry tests *
 * basic functionality of share entries
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShareEntryTest {
    
    @BeforeAll
    void setupAll() {
        // Ensure Fabric environment is initialized
        assertThat(FabricLoader.getInstance()).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle ShareEntry basic properties")
    void shouldHandleShareEntryBasicProperties() {
        // This test verifies basic properties and methods of ShareEntry
        // Since we cannot directly create ShareEntry (may require special construction process),
        // we mainly test ShareEntry obtained through ShowcaseManager
        
        assertThatCode(() -> {
            // Test getting non-existent share entry
            String testShareId = ShowcaseTestHelper.createTestShareId();
            ShareEntry entry = ShowcaseManager.getShareEntry(testShareId);
            
            // Non-existent entry should return null, this is normal behavior
            assertThat(entry).isNull();
            
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle ShareEntry with null ID according to contract")
    void shouldHandleShareEntryWithNullIdAccordingToContract() {
        // Test null handling according to actual code contract
        // ShareRepository is annotated with @NotNull, so null input should throw exception
        assertThatThrownBy(() -> {
            ShareEntry entry = ShowcaseManager.getShareEntry(null);
        }).isInstanceOf(NullPointerException.class);
        
        // Test empty string input, this should be safe
        assertThatCode(() -> {
            ShareEntry entry = ShowcaseManager.getShareEntry("");
            assertThat(entry).isNull(); // Empty string should return null
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle ShareEntry operations safely")
    void shouldHandleShareEntryOperationsSafely() {
        // Test safety of share entry operations
        assertThatCode(() -> {
            // Test various share entry related operations
            String[] testIds = {
                ShowcaseTestHelper.createTestShareId(),
                "",
                "invalid-id",
                "test-123"
            };
            
            for (String id : testIds) {
                // These operations should execute safely without throwing exceptions
                ShowcaseManager.getShareEntry(id);
                ShowcaseManager.expireShareById(id);
                ShowcaseManager.getItemStackWithID(id);
            }
            
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle concurrent ShareEntry access")
    void shouldHandleConcurrentShareEntryAccess() {
        // Test safety of concurrent share entry access
        assertThatCode(() -> {
            String shareId = ShowcaseTestHelper.createTestShareId();
            
            // Simulate concurrent access
            Runnable accessTask = () -> {
                ShowcaseManager.getShareEntry(shareId);
                ShowcaseManager.getItemStackWithID(shareId);
            };
            
            // Create multiple threads for concurrent access
            Thread[] threads = new Thread[5];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(accessTask);
            }
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join(1000); // Wait at most 1 second
            }
            
        }).doesNotThrowAnyException();
    }
}