package com.showcase.test;

import java.util.UUID;

/**
 * Simplified test utility class that avoids dependency on Minecraft runtime environment
 */
public class SimplifiedTestUtils {
    
    /**
     * Create test UUID
     */
    public static UUID createTestUUID() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
    
    /**
     * Create another test UUID
     */
    public static UUID createTestUUID2() {
        return UUID.fromString("00000000-0000-0000-0000-000000000002");
    }
    
    /**
     * Create test share ID
     */
    public static String createTestShareId() {
        return "test-share-" + System.currentTimeMillis();
    }
    
    /**
     * Verify string contains specified content
     */
    public static boolean stringContains(String text, String content) {
        return text != null && content != null && text.contains(content);
    }
    
    /**
     * Verify string equality
     */
    public static boolean stringEquals(String text1, String text2) {
        return java.util.Objects.equals(text1, text2);
    }
    
    /**
     * Short sleep
     */
    public static void shortSleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Verify string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }
    
    /**
     * Verify string is empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}