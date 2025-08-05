package com.showcase.utils;

import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player cooldowns for different share types.
 */
public final class CooldownManager {
    private static final Map<UUID, EnumMap<ShowcaseManager.ShareType, Long>> COOLDOWNS = new ConcurrentHashMap<>();

    private CooldownManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Checks if a player is on cooldown for a specific share type.
     *
     * @param player the player to check
     * @param type the share type
     * @return true if the player is on cooldown, false otherwise
     */
    public static boolean isOnCooldown(@NotNull ServerPlayerEntity player, @NotNull ShowcaseManager.ShareType type) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return false;
        if (PermissionChecker.isOp(player) || PermissionChecker.hasPermission(player, Permissions.getCommandTypeFromShareType(type).getCooldown(), 4)) return false;
        EnumMap<ShowcaseManager.ShareType, Long> map = COOLDOWNS.get(player.getUuid());
        if (map == null) return false;

        Long cooldownEnd = map.get(type);
        if (cooldownEnd == null) return false;

        return Instant.now().toEpochMilli() < cooldownEnd;
    }

    /**
     * Sets a cooldown for a player and share type.
     *
     * @param player the player
     * @param type the share type
     */
    public static void setCooldown(@NotNull ServerPlayerEntity player, @NotNull ShowcaseManager.ShareType type) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        int cooldownSeconds = ModConfigManager.getShareSettings(type).cooldown;
        long cooldownEnd = Instant.now().toEpochMilli() + (cooldownSeconds * 1000L);

        COOLDOWNS.computeIfAbsent(player.getUuid(), k -> new EnumMap<>(ShowcaseManager.ShareType.class))
                .put(type, cooldownEnd);
    }

    /**
     * Gets the remaining cooldown time for a player and share type.
     *
     * @param player the player
     * @param type the share type
     * @return the remaining cooldown time in seconds, or 0 if not on cooldown
     */
    public static long getRemainingCooldown(@NotNull ServerPlayerEntity player, @NotNull ShowcaseManager.ShareType type) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return 0;

        EnumMap<ShowcaseManager.ShareType, Long> map = COOLDOWNS.get(player.getUuid());
        if (map == null) return 0;

        Long cooldownEnd = map.get(type);
        if (cooldownEnd == null) return 0;

        long remaining = cooldownEnd - Instant.now().toEpochMilli();
        return Math.max(0, remaining / 1000); // Convert to seconds
    }

    /**
     * Clears all cooldowns for a specific player.
     *
     * @param player the player
     */
    public static void clearPlayerCooldowns(@NotNull ServerPlayerEntity player) {
        COOLDOWNS.remove(player.getUuid());
    }

    /**
     * Clears all cooldowns.
     */
    public static void clearAllCooldowns() {
        COOLDOWNS.clear();
    }

    /**
     * Removes expired cooldowns for cleanup.
     */
    public static void cleanupExpiredCooldowns() {
        long now = Instant.now().toEpochMilli();
        
        COOLDOWNS.entrySet().removeIf(playerEntry -> {
            EnumMap<ShowcaseManager.ShareType, Long> playerCooldowns = playerEntry.getValue();
            playerCooldowns.entrySet().removeIf(cooldownEntry -> cooldownEntry.getValue() <= now);
            return playerCooldowns.isEmpty();
        });
    }

    /**
     * Gets the total number of active cooldowns.
     *
     * @return the number of active cooldowns
     */
    public static int getActiveCooldownCount() {
        return COOLDOWNS.values().stream()
                .mapToInt(Map::size)
                .sum();
    }
}