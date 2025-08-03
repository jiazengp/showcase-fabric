package com.showcase.gametest;

import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import com.showcase.data.ShareRepository;
import com.showcase.utils.ReadOnlyInventory;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

import java.util.*;

/**
 * Optimized GameTest for data persistence functionality.
 * Tests core repository operations with improved structure and efficiency.
 */
public class DataPersistenceGameTest {
    
    private static final int TEST_DURATION = 300;

    @GameTest
    public void testRepositoryCoreOperations(TestContext context) {
        // Setup
        ServerPlayerEntity player = GameTestPlayerUtils.createTestPlayerCalled(context, "player1");
        ShareRepository.clear();
        
        String shareId = "test-share-core";
        ShareEntry entry = createTestShareEntry(player, Items.DIAMOND);
        
        try {
            // Test CRUD operations in sequence
            testCreate(context, shareId, entry);
            testRead(context, shareId, entry);
            testUpdate(context, shareId);
            testDelete(context, shareId);
            
        } catch (Exception e) {
            context.throwGameTestException(Text.of("Core operations test failed: " + e.getMessage()));
        }
        
        context.complete();
    }

    @GameTest
    public void testMultiPlayerDataIntegrity(TestContext context) {
        // Setup multiple players
        ServerPlayerEntity[] players = GameTestPlayerUtils.createMultipleTestPlayers(context, "player", 3);
        ShareRepository.clear();
        
        try {
            Map<String, String> playerShares = new HashMap<>();
            Item[] testItems = {
                    Items.DIAMOND,
                    Items.GOLD_INGOT,
                    Items.IRON_INGOT,
                    Items.EMERALD
            };
            // Create shares for each player
            for (int i = 0; i < players.length; i++) {
                String shareId = ShowcaseManager.createItemShare(
                    players[i],
                    new ItemStack(testItems[i % testItems.length]),
                    TEST_DURATION, 
                    Collections.emptyList()
                );
                playerShares.put(players[i].getUuid().toString(), shareId);
            }
            
            // Verify data integrity
            verifyMultiPlayerData(context, players, playerShares);
            
            // Test bulk operations
            testBulkOperations(context, players);
            
        } catch (Exception e) {
            context.throwGameTestException(Text.of("Multi-player test failed: " + e.getMessage()));
        }
        
        context.complete();
    }

    @GameTest
    public void testRepositoryEdgeCasesAndErrors(TestContext context) {
        ShareRepository.clear();
        
        try {
            // Test empty repository operations
            testEmptyRepositoryBehavior(context);
            
            // Test invalid data handling
            testInvalidDataHandling(context);
            
            // Test boundary conditions
            testBoundaryConditions(context);
            
        } catch (Exception e) {
            context.throwGameTestException(Text.of("Edge cases test failed: " + e.getMessage()));
        }
        
        context.complete();
    }

    // Helper Methods
    
    private void testCreate(TestContext context, String shareId, ShareEntry entry) {
        ShareRepository.store(shareId, entry);
        context.assertTrue(ShareRepository.exists(shareId), Text.of("Share should exist after creation"));
        context.assertTrue(ShareRepository.getShareCount() == 1, Text.of("Count should be 1"));
    }
    
    private void testRead(TestContext context, String shareId, ShareEntry originalEntry) {
        ShareEntry retrieved = ShareRepository.get(shareId);
        context.assertTrue(retrieved != null, Text.of("Retrieved share should not be null"));
        context.assertTrue(retrieved.getOwnerUuid().equals(originalEntry.getOwnerUuid()), 
            Text.of("Owner UUID should match"));
        context.assertTrue(retrieved.getType() == originalEntry.getType(), 
            Text.of("Share type should match"));
    }
    
    private void testUpdate(TestContext context, String shareId) {
        ShareEntry entry = ShareRepository.get(shareId);
        int initialViewCount = entry.getViewCount();
        
        entry.incrementViewCount();
        context.assertTrue(entry.getViewCount() == initialViewCount + 1, 
            Text.of("View count should increment"));
    }
    
    private void testDelete(TestContext context, String shareId) {
        boolean removed = ShareRepository.remove(shareId);
        context.assertTrue(removed, Text.of("Share should be successfully removed"));
        context.assertTrue(!ShareRepository.exists(shareId), Text.of("Share should not exist after removal"));
        context.assertTrue(ShareRepository.getShareCount() == 0, Text.of("Count should be 0"));
    }
    
    private void verifyMultiPlayerData(TestContext context, ServerPlayerEntity[] players, Map<String, String> playerShares) {
        // Verify each player has exactly one share
        for (ServerPlayerEntity player : players) {
            List<ShareEntry> shares = ShareRepository.getPlayerShares(player.getUuid().toString());
            context.assertTrue(shares.size() == 1, 
                Text.of("Player should have exactly 1 share, got: " + shares.size()));
        }
        
        // Verify total count
        context.assertTrue(ShareRepository.getShareCount() == players.length, 
            Text.of("Total shares should equal player count"));
    }
    
    private void testBulkOperations(TestContext context, ServerPlayerEntity[] players) {
        String playerUuid = players[0].getUuid().toString();
        
        // Remove one player's shares
        int removedCount = ShareRepository.removePlayerShares(playerUuid);
        context.assertTrue(removedCount == 1, Text.of("Should remove 1 share"));
        
        // Verify remaining shares
        context.assertTrue(ShareRepository.getShareCount() == players.length - 1, 
            Text.of("Should have " + (players.length - 1) + " shares remaining"));
    }
    
    private void testEmptyRepositoryBehavior(TestContext context) {
        context.assertTrue(ShareRepository.get("nonexistent") == null, 
            Text.of("Get non-existent should return null"));
        context.assertTrue(!ShareRepository.remove("nonexistent"), 
            Text.of("Remove non-existent should return false"));
        context.assertTrue(!ShareRepository.exists("nonexistent"), 
            Text.of("Non-existent should not exist"));
        
        List<ShareEntry> emptyShares = ShareRepository.getPlayerShares("fake-uuid");
        context.assertTrue(emptyShares.isEmpty(), 
            Text.of("Non-existent player should have empty list"));
    }
    
    private void testInvalidDataHandling(TestContext context) {
        // Test null data loading
        ShareRepository.loadShares(null);
        context.assertTrue(ShareRepository.getShareCount() == 0, 
            Text.of("Loading null should not crash"));
        
        ShareRepository.loadShares(Collections.emptyMap());
        context.assertTrue(ShareRepository.getShareCount() == 0, 
            Text.of("Loading empty map should work"));
    }
    
    private void testBoundaryConditions(TestContext context) {
        ServerPlayerEntity player = GameTestPlayerUtils.createTestPlayerCalled(context, "boundary-player");
        
        // Test duplicate removal
        String shareId = ShowcaseManager.createItemShare(player, new ItemStack(Items.STICK), TEST_DURATION, Collections.emptyList());
        
        boolean firstRemove = ShareRepository.remove(shareId);
        context.assertTrue(firstRemove, Text.of("First removal should succeed"));
        
        boolean secondRemove = ShareRepository.remove(shareId);
        context.assertTrue(!secondRemove, Text.of("Second removal should fail"));
    }
    
    private ShareEntry createTestShareEntry(ServerPlayerEntity player, net.minecraft.item.Item item) {
        ReadOnlyInventory inv = new ReadOnlyInventory(9, Text.literal("Test Inventory"), ScreenHandlerType.GENERIC_9X1);
        inv.setStack(0, new ItemStack(item, 1));
        
        return new ShareEntry(
            player.getUuid(), 
            ShowcaseManager.ShareType.INVENTORY, 
            inv, 
            TEST_DURATION, 
            Collections.emptyList()
        );
    }
}