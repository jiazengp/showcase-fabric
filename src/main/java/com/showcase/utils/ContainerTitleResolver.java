package com.showcase.utils;

import com.showcase.utils.compat.ServerPlayerCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

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
    public static Component resolveContainerTitle(ServerPlayer player, AbstractContainerMenu handler, MenuProvider factory) {
        if (factory == null) {
            return TextUtils.UNKNOWN_ENTRY;
        }

        // Try to get title from BlockEntity first (most accurate)
        Component blockEntityTitle = tryGetTitleFromBlockEntity(player, handler);
        if (blockEntityTitle != null) {
            return blockEntityTitle;
        }

        // Fallback to the factory's display name
        return resolveFromFactory(factory);
    }

    /**
     * Try to get the container title from the BlockEntity's Block
     */
    private static Component tryGetTitleFromBlockEntity(ServerPlayer player, AbstractContainerMenu handler) {
        try {
            // Only GenericContainerScreenHandler has a reliable way to get BlockPos
            if (handler instanceof ChestMenu containerHandler) {
                // Try to get the BlockEntity from the inventory
                var inventory = containerHandler.getContainer();

                // Check if this inventory is associated with a BlockEntity
                if (inventory instanceof BlockEntity blockEntity) {
                    BlockPos pos = blockEntity.getBlockPos();
                    ServerLevel world = ServerPlayerCompat.getWorld(player);

                    // Verify the BlockEntity is still valid at this position
                    BlockEntity worldBlockEntity = world.getBlockEntity(pos);
                    if (worldBlockEntity != null && worldBlockEntity == blockEntity) {
                        Block block = world.getBlockState(pos).getBlock();
                        String translationKey = block.getDescriptionId();
                        return Component.translatable(translationKey);
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
    private static Component resolveFromFactory(MenuProvider factory) {
        Component displayName = factory.getDisplayName();

        if (displayName == null || displayName.getString().isBlank()) {
            return TextUtils.UNKNOWN_ENTRY;
        }

        ComponentContents content = displayName.getContents();

        // If it's already a translatable text, return it
        if (content instanceof TranslatableContents translatable) {
            return Component.translatable(translatable.getKey());
        }

        // Last resort: return the literal display name
        return displayName;
    }

    /**
     * Simple resolution from factory only (for backward compatibility)
     */
    public static Component resolveContainerTitle(MenuProvider factory) {
        return resolveFromFactory(factory);
    }
}
