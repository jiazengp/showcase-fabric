package com.showcase.utils;

import com.showcase.config.ModConfigManager;
import com.showcase.gui.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public final class ItemPreviewManager {
    private static final String ITEM_PREVIEW_MARK = "isShowcaseItemPreview";

    /**
     * Show an item preview using the configured view mode
     * @param player The player viewing the item
     * @param item The item to preview
     */
    public static void showItemPreview(ServerPlayerEntity player, ItemStack item) {
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
    private static void showTraditionalContainerView(ServerPlayerEntity player, ItemStack item) {
        DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
        items.set(0, item.copy());
        
        ContainerGui gui = new ContainerGui(
            ScreenHandlerType.GENERIC_9X1,
            player,
            StackUtils.getDisplayName(item),
            items
        );
        
        gui.open();
    }

    /**
     * Show item in hotbar preview mode
     */
    private static void showHotbarPreview(ServerPlayerEntity player, ItemStack item, int durationTicks) {
        if (player.isSpectator()) return;

        ItemStack displayItem = item.copy();
        NbtComponent customData = displayItem.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = customData.copyNbt();
        nbt.putBoolean(ITEM_PREVIEW_MARK, true);
        displayItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

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
    public static void showItemPreviewWithTitle(ServerPlayerEntity player, ItemStack item, Text customTitle) {
        if (player == null || item == null || item.isEmpty()) return;
        
        int itemViewDuration = ModConfigManager.getConfig().itemViewDuration;
        
        if (itemViewDuration == -1 || (ModConfigManager.getConfig().mapViewDuration != -1 && StackUtils.isMap(item))) {
            // Use traditional container view with custom title
            DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
            items.set(0, item.copy());
            
            ContainerGui gui = new ContainerGui(
                ScreenHandlerType.GENERIC_9X1,
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
    public static void showItemPreview(ServerPlayerEntity player, ItemStack item, Text customTitle) {
        // Fallback to basic preview
        showItemPreviewWithTitle(player, item, customTitle);
    }
}