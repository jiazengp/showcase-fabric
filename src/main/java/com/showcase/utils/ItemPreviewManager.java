package com.showcase.utils;

import com.showcase.config.ModConfigManager;
import com.showcase.gui.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ItemPreviewManager {
    private static final String ITEM_PREVIEW_MARK = "isShowcaseItemPreview";

    /**
     * Show an item preview using the configured view mode
     * @param player The player viewing the item
     * @param item The item to preview
     */
    public static void showItemPreview(ServerPlayer player, ItemStack item) {
        if (player == null || item == null || item.isEmpty()) return;
        
        int itemViewDuration = ModConfigManager.getConfig().itemViewDuration;
        
        if (itemViewDuration == -1) {
            // Use traditional container view (1x9)
            showTraditionalContainerView(player, item);
        } else {
            // Use hotbar preview mode
            int durationTicks = itemViewDuration == 0 ? 0 : itemViewDuration * 20;
            showHotbarPreview(player, item, durationTicks);
        }
    }

    /**
     * Show item in traditional 1x9 container view
     */
    private static void showTraditionalContainerView(ServerPlayer player, ItemStack item) {
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        items.set(0, item.copy());
        
        ContainerGui gui = new ContainerGui(
            MenuType.GENERIC_9x1,
            player,
            StackUtils.getDisplayName(item),
            items
        );
        
        gui.open();
    }

    /**
     * Show item in hotbar preview mode
     */
    private static void showHotbarPreview(ServerPlayer player, ItemStack item, int durationTicks) {
        if (player.isSpectator()) return;

        ItemStack displayItem = item.copy();
        CustomData customData = displayItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = customData.copyTag();
        nbt.putBoolean(ITEM_PREVIEW_MARK, true);
        displayItem.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        int durationSeconds = durationTicks / 20;
        ItemViewerContext context = new ItemViewerContext(player, displayItem, durationSeconds);
        ItemViewerGui gui = new ItemViewerGui(context, 0);
        // GUI automatically opens in constructor
    }

    /**
     * Show item preview with custom title
     * @param player The player viewing the item
     * @param item The item to preview  
     * @param customTitle Custom title for traditional container view
     */
    public static void showItemPreviewWithTitle(ServerPlayer player, ItemStack item, Component customTitle) {
        if (player == null || item == null || item.isEmpty()) return;
        
        int itemViewDuration = ModConfigManager.getConfig().itemViewDuration;
        
        if (itemViewDuration == -1 || (ModConfigManager.getConfig().mapViewDuration != -1 && StackUtils.isMap(item))) {
            // Use traditional container view with custom title
            NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
            items.set(0, item.copy());
            
            ContainerGui gui = new ContainerGui(
                MenuType.GENERIC_9x1,
                player,
                customTitle,
                items
            );
            
            gui.open();
        } else {
            // Use hotbar preview mode
            showHotbarPreview(player, item, itemViewDuration * 20);
        }
    }

    /**
     * Show item preview with custom title (for traditional view only)
     * @deprecated Use showItemPreviewWithTitle instead
     */
    @Deprecated
    public static void showItemPreview(ServerPlayer player, ItemStack item, Component customTitle) {
        // Fallback to basic preview
        showItemPreviewWithTitle(player, item, customTitle);
    }
}