package com.showcase.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/**
 * Simplified test base class that doesn't depend on Mockito and Minecraft environment
 */
public abstract class SimpleTestBase {
    
    @BeforeEach
    void setUpBase() {
        setUp();
    }
    
    @AfterEach
    void tearDownBase() {
        tearDown();
    }
    
    /**
     * Subclasses can override this method for test setup
     */
    protected void setUp() {
        // Default empty implementation
    }
    
    /**
     * Subclasses can override this method for test cleanup
     */
    protected void tearDown() {
        // Default empty implementation
    }
}