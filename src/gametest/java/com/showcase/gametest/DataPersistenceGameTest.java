package com.showcase.gametest;

import com.mojang.authlib.GameProfile;
import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import com.showcase.data.ShareRepository;
import com.showcase.utils.ReadOnlyInventory;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

import java.util.*;

/**
 * GameTest for data persistence functionality
 * Tests data storage, retrieval, and persistence operations
 */
public class DataPersistenceGameTest {

    private static final int TEST_DURATION = 300;
    private static final String[] TEST_UUIDS = {
        "9cffa718-9571-4c6b-a683-b4f62dd72047",
        "ec561538-f3fd-461d-aff5-086b22154bce", 
        "8667ba71-b85a-4004-af54-457a9734eed7",
        "d8d5a923-7b20-43d8-883b-1150148d6955",
        "1168c028-de65-4698-bbde-9ae674404829",
        "bb88d2ab-2433-4c30-bbb8-e012edc19279"
    };

    private static int playerCounter = 0;

    private static ServerPlayerEntity createTestPlayer(TestContext context, String name) {
        ServerWorld world = context.getWorld();
        return new ServerPlayerEntity(
                context.getWorld().getServer(),
                world,
                new GameProfile(UUID.fromString(TEST_UUIDS[playerCounter % TEST_UUIDS.length]), name),
                SyncedClientOptions.createDefault()
        );
    }

    private static ServerPlayerEntity createTestPlayerCalled(TestContext context, String name) {
        int index = playerCounter++;
        ServerWorld world = context.getWorld();
        return new ServerPlayerEntity(
                context.getWorld().getServer(),
                world,
                new GameProfile(UUID.fromString(TEST_UUIDS[index % TEST_UUIDS.length]), name),
                SyncedClientOptions.createDefault()
        );
    }

    @GameTest
    public void testShareRepositoryBasicOperations(TestContext context) {
        ServerPlayerEntity player = createTestPlayer(context, "player1");
        
        // Clear repository for clean test
        ShareRepository.clear();
        context.assertTrue(ShareRepository.getShareCount() == 0, Text.of("Repository should be empty initially"));

        // Create a test share entry
        ReadOnlyInventory inv = new ReadOnlyInventory(9, Text.literal("Test Inventory"), ScreenHandlerType.GENERIC_9X1);
        inv.setStack(0, new ItemStack(Items.DIAMOND, 64));
        
        ShareEntry entry = new ShareEntry(
            player.getUuid(), 
            ShowcaseManager.ShareType.INVENTORY, 
            inv, 
            TEST_DURATION, 
            Collections.emptyList()
        );
        
        String testId = "test-share-123";
        
        // Test storage
        ShareRepository.store(testId, entry);
        context.assertTrue(ShareRepository.exists(testId), Text.of("Share should exist after storage"));
        context.assertTrue(ShareRepository.getShareCount() == 1, Text.of("Share count should be 1"));

        // Test retrieval
        ShareEntry retrieved = ShareRepository.get(testId);
        context.assertTrue(retrieved != null, Text.of("Retrieved share should not be null"));
        context.assertTrue(retrieved.getOwnerUuid().equals(player.getUuid()), Text.of("Owner UUID should match"));
        context.assertTrue(retrieved.getType() == ShowcaseManager.ShareType.INVENTORY, Text.of("Share type should match"));
        
        // Test removal
        boolean removed = ShareRepository.remove(testId);
        context.assertTrue(removed, Text.of("Share should be successfully removed"));
        context.assertTrue(!ShareRepository.exists(testId), Text.of("Share should not exist after removal"));
        context.assertTrue(ShareRepository.getShareCount() == 0, Text.of("Share count should be 0 after removal"));

        context.complete();
    }

    @GameTest
    public void testShareRepositoryPlayerSpecificOperations(TestContext context) {
        ServerPlayerEntity player1 = createTestPlayerCalled(context, "player1");
        ServerPlayerEntity player2 = createTestPlayerCalled(context, "player2");
        
        // Clear repository
        ShareRepository.clear();
        
        // Create shares for player1
        String share1Id = ShowcaseManager.createItemShare(player1, new ItemStack(Items.DIAMOND_SWORD), TEST_DURATION, Collections.emptyList());
        String share2Id = ShowcaseManager.createItemShare(player1, new ItemStack(Items.GOLDEN_APPLE), TEST_DURATION, Collections.emptyList());
        
        // Create share for player2
        String share3Id = ShowcaseManager.createItemShare(player2, new ItemStack(Items.EMERALD), TEST_DURATION, Collections.emptyList());

        context.assertTrue(ShareRepository.getShareCount() == 3, Text.of("Should have 3 total shares"));

        // Test player-specific retrieval
        List<ShareEntry> player1Shares = ShareRepository.getPlayerShares(player1.getUuid().toString());
        context.assertTrue(player1Shares.size() == 2, Text.of("Player1 should have 2 shares"));
        
        List<ShareEntry> player2Shares = ShareRepository.getPlayerShares(player2.getUuid().toString());
        context.assertTrue(player2Shares.size() == 1, Text.of("Player2 should have 1 share"));

        // Test player-specific removal
        int removedCount = ShareRepository.removePlayerShares(player1.getUuid().toString());
        context.assertTrue(removedCount == 2, Text.of("Should have removed 2 shares for player1"));
        context.assertTrue(ShareRepository.getShareCount() == 1, Text.of("Should have 1 share remaining"));
        
        // Verify player2's share still exists
        ShareEntry remaining = ShareRepository.get(share3Id);
        context.assertTrue(remaining != null, Text.of("Player2's share should still exist"));

        context.complete();
    }

    @GameTest
    public void testShareDataIntegrity(TestContext context) {
        ServerPlayerEntity owner = createTestPlayerCalled(context, "owner");
        ServerPlayerEntity receiver1 = createTestPlayerCalled(context, "receiver1");
        ServerPlayerEntity receiver2 = createTestPlayerCalled(context, "receiver2");
        
        ShareRepository.clear();
        
        // Create a complex share with multiple receivers
        ItemStack testItem = new ItemStack(Items.NETHERITE_CHESTPLATE);

        List<ServerPlayerEntity> receivers = Arrays.asList(receiver1, receiver2);
        String shareId = ShowcaseManager.createItemShare(owner, testItem, TEST_DURATION, receivers);
        
        // Retrieve and verify data integrity
        ShareEntry entry = ShareRepository.get(shareId);
        context.assertTrue(entry != null, Text.of("Share entry should exist"));
        
        // Test owner data
        context.assertTrue(entry.getOwnerUuid().equals(owner.getUuid()), Text.of("Owner UUID should be preserved"));
        
        // Test receiver data
        Set<UUID> receiverUuids = entry.getReceiverUuids();
        context.assertTrue(receiverUuids.size() == 2, Text.of("Should have 2 receivers"));
        context.assertTrue(receiverUuids.contains(receiver1.getUuid()), Text.of("Should contain receiver1"));
        context.assertTrue(receiverUuids.contains(receiver2.getUuid()), Text.of("Should contain receiver2"));
        
        // Test item data integrity
        ReadOnlyInventory inventory = entry.getInventory();
        context.assertTrue(inventory != null, Text.of("Inventory should not be null"));
        ItemStack storedItem = inventory.getStack(0);
        context.assertTrue(storedItem.getItem() == Items.NETHERITE_CHESTPLATE, Text.of("Item type should be preserved"));
        
        // Test duration and timestamp
        context.assertTrue(entry.getDuration() == TEST_DURATION, Text.of("Duration should be preserved"));
        context.assertTrue(entry.getTimestamp() > 0, Text.of("Timestamp should be set"));
        
        // Test share type
        context.assertTrue(entry.getType() == ShowcaseManager.ShareType.ITEM, Text.of("Share type should be preserved"));

        context.complete();
    }

    @GameTest
    public void testShareRepositoryLoadOperation(TestContext context) {
        ServerPlayerEntity player = createTestPlayerCalled(context, "player");
        
        ShareRepository.clear();
        
        // Create some initial shares
        String share1Id = ShowcaseManager.createItemShare(player, new ItemStack(Items.DIAMOND), TEST_DURATION, Collections.emptyList());
        String share2Id = ShowcaseManager.createItemShare(player, new ItemStack(Items.GOLD_INGOT), TEST_DURATION, Collections.emptyList());
        
        context.assertTrue(ShareRepository.getShareCount() == 2, Text.of("Should have 2 initial shares"));
        
        // Get current shares for backup
        Map<String, ShareEntry> currentShares = new HashMap<>(ShareRepository.getAllShares());
        
        // Create new shares to load
        Map<String, ShareEntry> newShares = new HashMap<>();
        
        ReadOnlyInventory inv1 = new ReadOnlyInventory(9, Text.literal("Loaded Share 1"), ScreenHandlerType.GENERIC_9X1);
        inv1.setStack(0, new ItemStack(Items.EMERALD, 32));
        
        ShareEntry newEntry1 = new ShareEntry(
            player.getUuid(),
            ShowcaseManager.ShareType.INVENTORY,
            inv1,
            TEST_DURATION,
            Collections.emptyList()
        );
        
        ReadOnlyInventory inv2 = new ReadOnlyInventory(9, Text.literal("Loaded Share 2"), ScreenHandlerType.GENERIC_9X1);
        inv2.setStack(0, new ItemStack(Items.NETHERITE_INGOT, 16));
        
        ShareEntry newEntry2 = new ShareEntry(
            player.getUuid(),
            ShowcaseManager.ShareType.INVENTORY,
            inv2,
            TEST_DURATION,
            Collections.emptyList()
        );
        
        newShares.put("loaded-share-1", newEntry1);
        newShares.put("loaded-share-2", newEntry2);
        
        // Test load operation
        ShareRepository.loadShares(newShares);
        
        // Verify old shares are replaced
        context.assertTrue(ShareRepository.getShareCount() == 2, Text.of("Should have 2 shares after load"));
        context.assertTrue(!ShareRepository.exists(share1Id), Text.of("Old share1 should not exist"));
        context.assertTrue(!ShareRepository.exists(share2Id), Text.of("Old share2 should not exist"));
        
        // Verify new shares exist
        context.assertTrue(ShareRepository.exists("loaded-share-1"), Text.of("Loaded share 1 should exist"));
        context.assertTrue(ShareRepository.exists("loaded-share-2"), Text.of("Loaded share 2 should exist"));
        
        // Verify loaded data integrity
        ShareEntry retrieved1 = ShareRepository.get("loaded-share-1");
        context.assertTrue(retrieved1 != null, Text.of("Loaded share 1 should be retrievable"));
        context.assertTrue(retrieved1.getInventory().getStack(0).getItem() == Items.EMERALD, Text.of("Loaded share 1 item should match"));
        
        ShareEntry retrieved2 = ShareRepository.get("loaded-share-2");
        context.assertTrue(retrieved2 != null, Text.of("Loaded share 2 should be retrievable"));
        context.assertTrue(retrieved2.getInventory().getStack(0).getItem() == Items.NETHERITE_INGOT, Text.of("Loaded share 2 item should match"));

        context.complete();
    }

    @GameTest
    public void testShareRepositoryViewCountPersistence(TestContext context) {
        ServerPlayerEntity owner = createTestPlayerCalled(context, "owner");
        ServerPlayerEntity viewer = createTestPlayerCalled(context, "viewer");
        
        ShareRepository.clear();
        
        // Create a share
        String shareId = ShowcaseManager.createItemShare(owner, new ItemStack(Items.DIAMOND_BLOCK), TEST_DURATION, Collections.emptyList());
        
        ShareEntry entry = ShareRepository.get(shareId);
        context.assertTrue(entry.getViewCount() == 0, Text.of("Initial view count should be 0"));
        
        // Simulate viewing the share multiple times by directly incrementing view count
        entry.incrementViewCount();
        context.assertTrue(entry.getViewCount() == 1, Text.of("View count should be 1 after first increment"));
        
        entry.incrementViewCount();
        context.assertTrue(entry.getViewCount() == 2, Text.of("View count should be 2 after second increment"));
        
        // Test view counts persistence through repository operations
        Map<String, ShareEntry> allShares = new HashMap<>(ShareRepository.getAllShares());
        ShareRepository.loadShares(allShares); // Reload same data
        
        ShareEntry reloadedEntry = ShareRepository.get(shareId);
        context.assertTrue(reloadedEntry != null, Text.of("Share should still exist after reload"));
        context.assertTrue(reloadedEntry.getViewCount() == 2, Text.of("View count should persist after reload"));

        context.complete();
    }

    @GameTest
    public void testShareRepositoryEdgeCases(TestContext context) {
        ShareRepository.clear();
        
        // Test operations on empty repository
        context.assertTrue(ShareRepository.get("nonexistent") == null, Text.of("Get non-existent share should return null"));
        context.assertTrue(!ShareRepository.remove("nonexistent"), Text.of("Remove non-existent share should return false"));
        context.assertTrue(!ShareRepository.exists("nonexistent"), Text.of("Non-existent share should not exist"));
        
        List<ShareEntry> emptyPlayerShares = ShareRepository.getPlayerShares("nonexistent-uuid");
        context.assertTrue(emptyPlayerShares.isEmpty(), Text.of("Non-existent player should have empty share list"));
        
        int removedCount = ShareRepository.removePlayerShares("nonexistent-uuid");
        context.assertTrue(removedCount == 0, Text.of("Removing shares for non-existent player should return 0"));
        
        // Test loading null/empty shares
        ShareRepository.loadShares(null);
        context.assertTrue(ShareRepository.getShareCount() == 0, Text.of("Loading null shares should not crash"));
        
        ShareRepository.loadShares(Collections.emptyMap());
        context.assertTrue(ShareRepository.getShareCount() == 0, Text.of("Loading empty map should result in empty repository"));
        
        // Test duplicate operations
        ServerPlayerEntity player = createTestPlayerCalled(context, "player");
        String shareId = ShowcaseManager.createItemShare(player, new ItemStack(Items.STICK), TEST_DURATION, Collections.emptyList());
        
        // Try to remove the same share twice
        boolean firstRemove = ShareRepository.remove(shareId);
        context.assertTrue(firstRemove, Text.of("First removal should succeed"));
        
        boolean secondRemove = ShareRepository.remove(shareId);
        context.assertTrue(!secondRemove, Text.of("Second removal should fail"));

        context.complete();
    }
}