package com.showcase.utils;

import com.showcase.utils.compat.ServerPlayerCompat;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;

/**
 * Utility class for resolving container titles from various sources.
 * Tries to get the actual translation key from BlockEntity/Block when possible.
 */
public class ContainerTitleResolver {

    /**
     * Resolve the container title with priority:
     * 1. From BlockEntity's Block translation key (most accurate)
     * 2. From NamedScreenHandlerFactory displayName's translation key
     * 3. From literal displayName (fallback)
     */
    public static Text resolveContainerTitle(ServerPlayerEntity player, ScreenHandler handler, NamedScreenHandlerFactory factory) {
        if (factory == null) {
            return TextUtils.UNKNOWN_ENTRY;
        }

        // Try to get title from BlockEntity first (most accurate)
        Text blockEntityTitle = tryGetTitleFromBlockEntity(player, handler);
        if (blockEntityTitle != null) {
            return blockEntityTitle;
        }

        // Fallback to the factory's display name
        return resolveFromFactory(factory);
    }

    /**
     * Try to get the container title from the BlockEntity's Block
     */
    private static Text tryGetTitleFromBlockEntity(ServerPlayerEntity player, ScreenHandler handler) {
        try {
            // Only GenericContainerScreenHandler has a reliable way to get BlockPos
            if (handler instanceof GenericContainerScreenHandler containerHandler) {
                // Try to get the BlockEntity from the inventory
                var inventory = containerHandler.getInventory();

                // Check if this inventory is associated with a BlockEntity
                if (inventory instanceof BlockEntity blockEntity) {
                    BlockPos pos = blockEntity.getPos();
                    ServerWorld world = ServerPlayerCompat.getWorld(player);

                    // Verify the BlockEntity is still valid at this position
                    BlockEntity worldBlockEntity = world.getBlockEntity(pos);
                    if (worldBlockEntity != null && worldBlockEntity == blockEntity) {
                        Block block = world.getBlockState(pos).getBlock();
                        String translationKey = block.getTranslationKey();
                        return Text.translatable(translationKey);
                    }
                }
            }
        } catch (Exception e) {
            // If anything fails, fall back to other methods
        }

        return null;
    }

    /**
     * Resolve title from NamedScreenHandlerFactory with fallback logic
     */
    private static Text resolveFromFactory(NamedScreenHandlerFactory factory) {
        Text displayName = factory.getDisplayName();

        if (displayName == null || displayName.getString().isBlank()) {
            return TextUtils.UNKNOWN_ENTRY;
        }

        TextContent content = displayName.getContent();

        // If it's already a translatable text, return it
        if (content instanceof TranslatableTextContent translatable) {
            return Text.translatable(translatable.getKey());
        }

        // Last resort: return the literal display name
        return displayName;
    }

    /**
     * Simple resolution from factory only (for backward compatibility)
     */
    public static Text resolveContainerTitle(NamedScreenHandlerFactory factory) {
        return resolveFromFactory(factory);
    }
}
