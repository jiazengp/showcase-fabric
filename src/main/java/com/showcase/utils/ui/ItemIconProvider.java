package com.showcase.utils.ui;

import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.utils.ReadOnlyInventory;
import com.showcase.utils.compat.StyleCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ItemIconProvider {
    private static final String ITEM_MINECRAFT_PREFIX = "item.minecraft.";
    private static final String BLOCK_MINECRAFT_PREFIX = "block.minecraft.";
    private static final String MINECRAFT_ITEM_PATH = "minecraft.item.";
    private static final String MINECRAFT_BLOCK_PATH = "minecraft.block.";
    private static final String FONT_NAME = "default";
    private static final String ENDER_CHEST_NAME = "ender_chest";

    public static MutableComponent getIconForItem(ItemStack itemStack) {
        if (!isEnabled() || !shouldIncludeInItemNames() || itemStack == null || itemStack.isEmpty()) {
            return Component.empty();
        }
        
        String itemTranslationKey = itemStack.getItem().getDescriptionId();
        
        if (itemTranslationKey.startsWith(ITEM_MINECRAFT_PREFIX)) {
            String itemName = itemTranslationKey.substring(ITEM_MINECRAFT_PREFIX.length());
            return createItemIcon(itemName);
        } else if (itemTranslationKey.startsWith(BLOCK_MINECRAFT_PREFIX)) {
            String itemName = itemTranslationKey.substring(BLOCK_MINECRAFT_PREFIX.length());
            return createBlockIcon(itemName);
        }
        
        return Component.empty();
    }

    /**
     * Get icon for share type with share ID context (for container support)
     */
    public static MutableComponent getIconForShareTypeWithId(ShowcaseManager.ShareType shareType, String shareId) {
        if (!isEnabled()) {
            return Component.empty();
        }
        
        return switch (shareType) {
            case ITEM, STATS -> {
                ItemStack stack = ShowcaseManager.getItemStackWithID(shareId);
                yield getIconForItem(stack);
            }
            case ENDER_CHEST -> createBlockIcon(ENDER_CHEST_NAME);
            case CONTAINER -> getIconForContainer(shareId);
            default -> Component.empty();
        };
    }

    /**
     * Create icon text for Minecraft items
     */
    private static MutableComponent createItemIcon(String itemName) {
        return createIconText(MINECRAFT_ITEM_PATH + itemName);
    }
    
    /**
     * Create icon text for blocks
     */
    private static MutableComponent createBlockIcon(String blockName) {
        MutableComponent blockIcon = createIconText(MINECRAFT_BLOCK_PATH + blockName + (blockName.contains("shulker_box") ? "" : "_top"));
        return blockIcon;
    }
    
    /**
     * Create icon text with font styling
     */
    private static MutableComponent createIconText(String iconPath) {
        String fontNamespace = getFontNamespace();
        Identifier fontId = Identifier.fromNamespaceAndPath(fontNamespace, FONT_NAME);
        String fullIconKey = fontNamespace + "." + iconPath;
        
        return Component.translatable(fullIconKey)
                .setStyle(StyleCompat.withFont(Style.EMPTY, fontId).withColor(ChatFormatting.WHITE).withUnderlined(false));
    }

    private static boolean isEnabled() {
        return ModConfigManager.getConfig().itemIcons.enabled;
    }

    private static boolean shouldIncludeInItemNames() {
        return ModConfigManager.getConfig().itemIcons.includeInItemNames;
    }

    private static String getFontNamespace() {
        return ModConfigManager.getConfig().itemIcons.fontNamespace;
    }

    private static MutableComponent getIconForContainer(String shareId) {
        ShareEntry shareEntry = ShowcaseManager.getShareEntry(shareId);
        if (shareEntry == null || shareEntry.getType() != ShowcaseManager.ShareType.CONTAINER) {
            return Component.empty();
        }
        
        ReadOnlyInventory inventory = shareEntry.getInventory();
        if (inventory == null) {
            return Component.empty();
        }
        
        Component name = inventory.getName();
        if (name == null) {
            return Component.empty();
        }
        
        ComponentContents content = name.getContents();
        if (content instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            if (key.contains(".")) {
                String containerName = key.substring(key.lastIndexOf(".") + 1);
                return createBlockIcon(containerName);
            }
        }
        
        return Component.empty();
    }
}