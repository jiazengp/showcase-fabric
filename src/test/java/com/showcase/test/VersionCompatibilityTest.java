package com.showcase.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Interface for tests that verify version compatibility.
 * Implement this interface for any test class that needs to verify
 * functionality works across different Minecraft versions.
 */
public interface VersionCompatibilityTest {
    
    /**
     * Test that verifies the feature works with the current version
     */
    @Test
    @DisplayName("Feature works with current Minecraft version")
    void testCurrentVersionCompatibility();
    
    /**
     * Test that verifies text events are properly created
     */
    @Test
    @DisplayName("Text events are properly created")
    default void testTextEventCreation() {
        // Default implementation - override if needed
    }
    
    /**
     * Test that verifies item handling works correctly
     */
    @Test
    @DisplayName("Item handling works correctly")
    default void testItemHandling() {
        // Default implementation - override if needed
    }
    
    /**
     * Test that verifies command integration works
     */
    @Test
    @DisplayName("Command integration works")
    default void testCommandIntegration() {
        // Default implementation - override if needed
    }
}