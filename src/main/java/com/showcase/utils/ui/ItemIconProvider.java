package com.showcase.utils.ui;

import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.utils.ReadOnlyInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ItemIconProvider {
    private static final String ITEM_MINECRAFT_PREFIX = "item.minecraft.";
    private static final String BLOCK_MINECRAFT_PREFIX = "block.minecraft.";
    private static final String MINECRAFT_ITEM_PATH = "minecraft.item.";
    private static final String MINECRAFT_BLOCK_PATH = "minecraft.block.";
    private static final String FONT_NAME = "default";
    private static final String ENDER_CHEST_NAME = "ender_chest";

    public static MutableText getIconForItem(ItemStack itemStack) {
        if (!isEnabled() || !shouldIncludeInItemNames() || itemStack == null || itemStack.isEmpty()) {
            return Text.empty();
        }
        
        String itemTranslationKey = itemStack.getItem().getTranslationKey();
        
        if (itemTranslationKey.startsWith(ITEM_MINECRAFT_PREFIX)) {
            String itemName = itemTranslationKey.substring(ITEM_MINECRAFT_PREFIX.length());
            return createItemIcon(itemName);
        } else if (itemTranslationKey.startsWith(BLOCK_MINECRAFT_PREFIX)) {
            String itemName = itemTranslationKey.substring(BLOCK_MINECRAFT_PREFIX.length());
            return createBlockIcon(itemName);
        }
        
        return Text.empty();
    }

    /**
     * Get icon for share type with share ID context (for container support)
     */
    public static MutableText getIconForShareTypeWithId(ShowcaseManager.ShareType shareType, String shareId) {
        if (!isEnabled()) {
            return Text.empty();
        }
        
        return switch (shareType) {
            case ITEM, STATS -> {
                ItemStack stack = ShowcaseManager.getItemStackWithID(shareId);
                yield getIconForItem(stack);
            }
            case ENDER_CHEST -> createBlockIcon(ENDER_CHEST_NAME);
            case CONTAINER -> getIconForContainer(shareId);
            default -> Text.empty();
        };
    }

    /**
     * Create icon text for Minecraft items
     */
    private static MutableText createItemIcon(String itemName) {
        return createIconText(MINECRAFT_ITEM_PATH + itemName);
    }
    
    /**
     * Create icon text for blocks
     */
    private static MutableText createBlockIcon(String blockName) {
        MutableText blockIcon = createIconText(MINECRAFT_BLOCK_PATH + blockName + (blockName.contains("shulker_box") ? "" : "_top"));
        return blockIcon;
    }
    
    /**
     * Create icon text with font styling
     */
    private static MutableText createIconText(String iconPath) {
        String fontNamespace = getFontNamespace();
        Identifier fontId = Identifier.of(fontNamespace, FONT_NAME);
        String fullIconKey = fontNamespace + "." + iconPath;
        
        return Text.translatable(fullIconKey)
                .setStyle(Style.EMPTY.withFont(fontId).withColor(Formatting.WHITE).withUnderline(false));
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

    private static MutableText getIconForContainer(String shareId) {
        ShareEntry shareEntry = ShowcaseManager.getShareEntry(shareId);
        if (shareEntry == null || shareEntry.getType() != ShowcaseManager.ShareType.CONTAINER) {
            return Text.empty();
        }
        
        ReadOnlyInventory inventory = shareEntry.getInventory();
        if (inventory == null) {
            return Text.empty();
        }
        
        Text name = inventory.getName();
        if (name == null) {
            return Text.empty();
        }
        
        TextContent content = name.getContent();
        if (content instanceof TranslatableTextContent translatable) {
            String key = translatable.getKey();
            if (key.contains(".")) {
                String containerName = key.substring(key.lastIndexOf(".") + 1);
                return createBlockIcon(containerName);
            }
        }
        
        return Text.empty();
    }
}