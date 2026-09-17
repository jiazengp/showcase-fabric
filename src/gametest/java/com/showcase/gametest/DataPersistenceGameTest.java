package com.showcase.gametest;

import com.showcase.command.ShowcaseManager;
import com.showcase.data.ShareEntry;
import com.showcase.data.ShareRepository;
import com.showcase.utils.ReadOnlyInventory;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Optimized GameTest for data persistence functionality.
 * Tests core repository operations with improved structure and efficiency.
 */
public class DataPersistenceGameTest {

    private static final int TEST_DURATION = 300;

    @GameTest
    public void testRepositoryCoreOperations(GameTestHelper context) {
        // Setup
        ServerPlayer player = GameTestPlayerUtils.createTestPlayerCalled(context, "player1");
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
            context.fail(Component.literal("Core operations test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    @GameTest
    public void testMultiPlayerDataIntegrity(GameTestHelper context) {
        // Setup multiple players
        ServerPlayer[] players = GameTestPlayerUtils.createMultipleTestPlayers(context, "player", 3);
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
                playerShares.put(players[i].getUUID().toString(), shareId);
            }

            // Verify data integrity
            verifyMultiPlayerData(context, players, playerShares);

            // Test bulk operations
            testBulkOperations(context, players);

        } catch (Exception e) {
            context.fail(Component.literal("Multi-player test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    @GameTest
    public void testRepositoryEdgeCasesAndErrors(GameTestHelper context) {
        ShareRepository.clear();

        try {
            // Test empty repository operations
            testEmptyRepositoryBehavior(context);

            // Test invalid data handling
            testInvalidDataHandling(context);

            // Test boundary conditions
            testBoundaryConditions(context);

        } catch (Exception e) {
            context.fail(Component.literal("Edge cases test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    // Helper Methods

    private void testCreate(GameTestHelper context, String shareId, ShareEntry entry) {
        ShareRepository.store(shareId, entry);
        context.assertTrue(ShareRepository.exists(shareId), Component.literal("Share should exist after creation"));
        context.assertTrue(ShareRepository.getShareCount() == 1, Component.literal("Count should be 1"));
    }

    private void testRead(GameTestHelper context, String shareId, ShareEntry originalEntry) {
        ShareEntry retrieved = ShareRepository.get(shareId);
        context.assertTrue(retrieved != null, Component.literal("Retrieved share should not be null"));
        context.assertTrue(retrieved.getOwnerUuid().equals(originalEntry.getOwnerUuid()),
            Component.literal("Owner UUID should match"));
        context.assertTrue(retrieved.getType() == originalEntry.getType(),
            Component.literal("Share type should match"));
    }

    private void testUpdate(GameTestHelper context, String shareId) {
        ShareEntry entry = ShareRepository.get(shareId);
        int initialViewCount = entry.getViewCount();

        entry.incrementViewCount();
        context.assertTrue(entry.getViewCount() == initialViewCount + 1,
            Component.literal("View count should increment"));
    }

    private void testDelete(GameTestHelper context, String shareId) {
        boolean removed = ShareRepository.remove(shareId);
        context.assertTrue(removed, Component.literal("Share should be successfully removed"));
        context.assertTrue(!ShareRepository.exists(shareId), Component.literal("Share should not exist after removal"));
        context.assertTrue(ShareRepository.getShareCount() == 0, Component.literal("Count should be 0"));
    }

    private void verifyMultiPlayerData(GameTestHelper context, ServerPlayer[] players, Map<String, String> playerShares) {
        // Verify each player has exactly one share
        for (ServerPlayer player : players) {
            List<ShareEntry> shares = ShareRepository.getPlayerShares(player.getUUID().toString());
            context.assertTrue(shares.size() == 1,
                Component.literal("Player should have exactly 1 share, got: " + shares.size()));
        }

        // Verify total count
        context.assertTrue(ShareRepository.getShareCount() == players.length,
            Component.literal("Total shares should equal player count"));
    }

    private void testBulkOperations(GameTestHelper context, ServerPlayer[] players) {
        String playerUuid = players[0].getUUID().toString();

        // Remove one player's shares
        int removedCount = ShareRepository.removePlayerShares(playerUuid);
        context.assertTrue(removedCount == 1, Component.literal("Should remove 1 share"));

        // Verify remaining shares
        context.assertTrue(ShareRepository.getShareCount() == players.length - 1,
            Component.literal("Should have " + (players.length - 1) + " shares remaining"));
    }

    private void testEmptyRepositoryBehavior(GameTestHelper context) {
        context.assertTrue(ShareRepository.get("nonexistent") == null,
            Component.literal("Get non-existent should return null"));
        context.assertTrue(!ShareRepository.remove("nonexistent"),
            Component.literal("Remove non-existent should return false"));
        context.assertTrue(!ShareRepository.exists("nonexistent"),
            Component.literal("Non-existent should not exist"));

        List<ShareEntry> emptyShares = ShareRepository.getPlayerShares("fake-uuid");
        context.assertTrue(emptyShares.isEmpty(),
            Component.literal("Non-existent player should have empty list"));
    }

    private void testInvalidDataHandling(GameTestHelper context) {
        // Test null data loading
        ShareRepository.loadShares(null);
        context.assertTrue(ShareRepository.getShareCount() == 0,
            Component.literal("Loading null should not crash"));

        ShareRepository.loadShares(Collections.emptyMap());
        context.assertTrue(ShareRepository.getShareCount() == 0,
            Component.literal("Loading empty map should work"));
    }

    private void testBoundaryConditions(GameTestHelper context) {
        ServerPlayer player = GameTestPlayerUtils.createTestPlayerCalled(context, "boundary-player");

        // Test duplicate removal
        String shareId = ShowcaseManager.createItemShare(player, new ItemStack(Items.STICK), TEST_DURATION, Collections.emptyList());

        boolean firstRemove = ShareRepository.remove(shareId);
        context.assertTrue(firstRemove, Component.literal("First removal should succeed"));

        boolean secondRemove = ShareRepository.remove(shareId);
        context.assertTrue(!secondRemove, Component.literal("Second removal should fail"));
    }

    private ShareEntry createTestShareEntry(ServerPlayer player, Item item) {
        ReadOnlyInventory inv = new ReadOnlyInventory(9, Component.literal("Test Inventory"), MenuType.GENERIC_9x1);
        inv.setItem(0, new ItemStack(item, 1));

        return new ShareEntry(
            player.getUUID(),
            ShowcaseManager.ShareType.INVENTORY,
            inv,
            TEST_DURATION,
            Collections.emptyList()
        );
    }
}
