package com.showcase.test;

import com.showcase.command.ShowcaseManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.UUID;

/**
 * Test helper utility class that provides common methods for sharing and management tests
 */
public class ShowcaseTestHelper {
    
    /**
     * Create test item
     */
    public static ItemStack createTestItem() {
        return new ItemStack(Items.DIAMOND_SWORD);
    }
    
    /**
     * Create test item with specified count
     */
    public static ItemStack createTestItem(int count) {
        return new ItemStack(Items.DIAMOND, count);
    }
    
    /**
     * Create test share ID
     */
    public static String createTestShareId() {
        return "test-share-" + System.currentTimeMillis();
    }
    
    /**
     * Create test UUID
     */
    public static UUID createTestUUID() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
    
    /**
     * Verify if share exists
     */
    public static boolean shareExists(String shareId) {
        return ShowcaseManager.getShareEntry(shareId) != null;
    }
    
    /**
     * Get current active share count
     */
    public static int getActiveShareCount() {
        return ShowcaseManager.getUnmodifiableActiveShares().size();
    }
    
    /**
     * Cleanup test shares by expiring all shares
     */
    public static void cleanupTestShares() {
        var shares = ShowcaseManager.getUnmodifiableActiveShares();
        for (String shareId : shares.keySet()) {
            ShowcaseManager.expireShareById(shareId);
        }
    }
    
    /**
     * Wait for specified milliseconds
     */
    public static void waitMs(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Validate share ID format
     */
    public static boolean isValidShareId(String shareId) {
        return shareId != null && shareId.length() > 5;
    }
}