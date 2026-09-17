package com.showcase.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Utility class for creating test players in GameTests.
 * Provides consistent player creation methods across all game tests.
 */
public final class GameTestPlayerUtils {

    private GameTestPlayerUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final String[] TEST_UUIDS = {
        "9cffa718-9571-4c6b-a683-b4f62dd72047",
        "ec561538-f3fd-461d-aff5-086b22154bce",
        "8667ba71-b85a-4004-af54-457a9734eed7",
        "d8d5a923-7b20-43d8-883b-1150148d6955",
        "1168c028-de65-4698-bbde-9ae674404829",
        "bb88d2ab-2433-4c30-bbb8-e012edc19279"
    };

    private static int playerCounter = 0;

    /**
     * Creates a test player with a specific name using a predefined UUID.
     *
     * @param context the test context
     * @param name the player name
     * @return a new ServerPlayer for testing
     */
    public static ServerPlayer createTestPlayer(GameTestHelper context, String name) {
        return createPlayer(context, name, playerCounter % TEST_UUIDS.length);
    }

    /**
     * Creates a test player with a specific name and increments the counter.
     * This ensures each call gets a different UUID.
     *
     * @param context the test context
     * @param name the player name
     * @return a new ServerPlayer for testing
     */
    public static ServerPlayer createTestPlayerCalled(GameTestHelper context, String name) {
        return createPlayer(context, name, playerCounter++);
    }

    private static ServerPlayer createPlayer(GameTestHelper context, String name, int index) {
        ServerLevel level = context.getLevel();
        return new ServerPlayer(
                level.getServer(),
                level,
                new GameProfile(UUID.fromString(TEST_UUIDS[index % TEST_UUIDS.length]), name),
                ClientInformation.createDefault()
        );
    }

    /**
     * Resets the player counter. Useful for tests that need consistent player creation.
     */
    public static void resetPlayerCounter() {
        playerCounter = 0;
    }

    /**
     * Gets the current player counter-value.
     *
     * @return the current counter-value
     */
    public static int getPlayerCounter() {
        return playerCounter;
    }

    /**
     * Creates multiple test players with sequential names.
     *
     * @param context the test context
     * @param baseName the base name for players (will be suffixed with numbers)
     * @param count the number of players to create
     * @return an array of ServerPlayer instances
     */
    public static ServerPlayer[] createMultipleTestPlayers(GameTestHelper context, String baseName, int count) {
        ServerPlayer[] players = new ServerPlayer[count];
        for (int i = 0; i < count; i++) {
            players[i] = createTestPlayerCalled(context, baseName + (i + 1));
        }
        return players;
    }
}
