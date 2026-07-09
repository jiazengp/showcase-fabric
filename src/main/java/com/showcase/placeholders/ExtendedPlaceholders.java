package com.showcase.placeholders;

import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Essential extended placeholder system for Showcase mod
 * Provides only the most useful placeholders for server integration
 */
public class ExtendedPlaceholders {

    // Basic tracking for essential statistics
    private static final AtomicInteger totalSharesCreated = new AtomicInteger(0);

    /**
     * Register essential extended placeholders only
     */
    public static void registerExtendedPlaceholders() {
        // Check if placeholder extensions are enabled
        if (!ModConfigManager.isPlaceholderExtensionsEnabled()) {
            return;
        }

        registerPlayerSharePlaceholders();
        registerPermissionPlaceholders();
    }

    /**
     * Register essential player share placeholders
     */
    private static void registerPlayerSharePlaceholders() {
        // %showcase:shares_count% - Number of active shares
        Placeholders.registerServer(id("shares_count"), (ctx, arg) -> {
            ServerPlayer player = ctx.serverPlayer();
            if (player == null) return PlaceholderResult.invalid("No player context");

            int count = ShowcaseStatistics.getActiveShareCount(player);
            return PlaceholderResult.value(Component.literal(String.valueOf(count)));
        });

        // %showcase:total_views% - Total views received by player
        Placeholders.registerServer(id("total_views"), (ctx, arg) -> {
            ServerPlayer player = ctx.serverPlayer();
            if (player == null) return PlaceholderResult.invalid("No player context");

            long views = ShowcaseStatistics.getTotalViewsForPlayer(player);
            return PlaceholderResult.value(Component.literal(String.valueOf(views)));
        });
    }

    /**
     * Register essential permission check placeholders
     */
    private static void registerPermissionPlaceholders() {
        // %showcase:can_share_item% - true/false
        Placeholders.registerServer(id("can_share_item"), (ctx, arg) -> {
            ServerPlayer player = ctx.serverPlayer();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean canShare = PermissionChecker.hasPermission(player, Permissions.Command.ITEM, 0);
            return PlaceholderResult.value(Component.literal(String.valueOf(canShare)));
        });

        // %showcase:has_admin_perms% - true/false
        Placeholders.registerServer(id("has_admin_perms"), (ctx, arg) -> {
            ServerPlayer player = ctx.serverPlayer();
            if (player == null) return PlaceholderResult.invalid("No player context");

            boolean isAdmin = PermissionChecker.hasPermission(player, Permissions.Manage.CANCEL, 0);
            return PlaceholderResult.value(Component.literal(String.valueOf(isAdmin)));
        });
    }

    // Utility methods

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("showcase", path);
    }

    // Statistics tracking methods (simplified)

    public static void incrementTotalShares() {
        totalSharesCreated.incrementAndGet();
    }

    public static void incrementShareTypeStats(ShowcaseManager.ShareType type) {
        // Simplified - no detailed tracking needed
    }

    public static void incrementPlayerShares(String playerUuid) {
        // Simplified - no detailed tracking needed
    }
}
