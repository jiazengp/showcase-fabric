package com.showcase.utils;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager;
import net.minecraft.util.Identifier;

/**
 * Centralized constants for share types and actions to avoid hardcoded strings throughout the codebase
 */
public final class ShareConstants {
    
    // Command name constants
    public static final String ITEM = "item";
    public static final String INVENTORY = "inventory";
    public static final String HOTBAR = "hotbar";
    public static final String ENDER_CHEST = "ender_chest";
    public static final String STATS = "stats";
    public static final String CONTAINER = "container";
    public static final String MERCHANT = "merchant";
    public static final String CANCEL = "cancel";
    public static final String VIEW = "view";

    // Placeholder identifiers
    public static final class PlaceholderIds {
        public static final Identifier ITEM = Identifier.of(ShowcaseMod.MOD_ID, ShareConstants.ITEM);
        public static final Identifier INVENTORY = Identifier.of(ShowcaseMod.MOD_ID, ShareConstants.INVENTORY);
        public static final Identifier HOTBAR = Identifier.of(ShowcaseMod.MOD_ID, ShareConstants.HOTBAR);
        public static final Identifier ENDER_CHEST = Identifier.of(ShowcaseMod.MOD_ID, ShareConstants.ENDER_CHEST);
        public static final Identifier STATS = Identifier.of(ShowcaseMod.MOD_ID, ShareConstants.STATS);
        
        private PlaceholderIds() {}
    }
    
    // ShareType to string mapping
    public static final class ShareTypeNames {
        public static String fromShareType(ShowcaseManager.ShareType shareType) {
            return switch (shareType) {
                case ITEM -> ITEM;
                case INVENTORY -> INVENTORY;
                case HOTBAR -> HOTBAR;
                case ENDER_CHEST -> ENDER_CHEST;
                case STATS -> STATS;
                case CONTAINER -> CONTAINER;
                case MERCHANT -> MERCHANT;
            };
        }
        
        public static ShowcaseManager.ShareType toShareType(String name) {
            return switch (name.toLowerCase()) {
                case ITEM -> ShowcaseManager.ShareType.ITEM;
                case INVENTORY -> ShowcaseManager.ShareType.INVENTORY;
                case HOTBAR -> ShowcaseManager.ShareType.HOTBAR;
                case ENDER_CHEST -> ShowcaseManager.ShareType.ENDER_CHEST;
                case STATS -> ShowcaseManager.ShareType.STATS;
                case CONTAINER -> ShowcaseManager.ShareType.CONTAINER;
                case MERCHANT -> ShowcaseManager.ShareType.MERCHANT;
                default -> throw new IllegalArgumentException("Unknown share type: " + name);
            };
        }
        
        private ShareTypeNames() {}
    }
    
    private ShareConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}