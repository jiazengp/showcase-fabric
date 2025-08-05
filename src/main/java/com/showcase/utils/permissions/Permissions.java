package com.showcase.utils.permissions;

import com.showcase.utils.ShareConstants;

public final class Permissions {
    // Root nodes
    public static final String ADMIN = "admin";
    public static final String COMMAND = "command";
    public static final String MANAGE = "manage";
    public static final String CHAT = "chat";
    
    // Command permissions using enum for better organization
    public enum CommandType {
        VIEW(ShareConstants.VIEW.toLowerCase()),
        CANCEL(ShareConstants.CANCEL.toLowerCase()),
        ITEM(ShareConstants.ITEM.toLowerCase()),
        INVENTORY(ShareConstants.INVENTORY.toLowerCase()),
        HOTBAR(ShareConstants.HOTBAR.toLowerCase()),
        ENDERCHEST(ShareConstants.ENDER_CHEST.toLowerCase()), // Note: using different name for permissions
        CONTAINER(ShareConstants.CONTAINER.toLowerCase()),
        MERCHANT(ShareConstants.MERCHANT.toLowerCase()),
        STATS(ShareConstants.STATS.toLowerCase());

        private final String permission;
        
        CommandType(String command) {
            this.permission = node(COMMAND, command);
        }
        
        public String getPermission() {
            return permission;
        }

        public String getCooldown() {
            return node(permission, "cooldown");
        }

        public String receivers() {
            return node(permission, "receivers");
        }
        
        public String duration() {
            return node(permission, "duration");
        }
        
        public String description() {
            return node(permission, "description");
        }
        
        @Override
        public String toString() {
            return permission;
        }
    }
    
    // Legacy Command class for backward compatibility
    public static final class Command {
        public static final String VIEW = CommandType.VIEW.getPermission();
        public static final String ITEM = CommandType.ITEM.getPermission();
        public static final String INVENTORY = CommandType.INVENTORY.getPermission();
        public static final String HOTBAR = CommandType.HOTBAR.getPermission();
        public static final String ENDERCHEST = CommandType.ENDERCHEST.getPermission();
        public static final String CONTAINER = CommandType.CONTAINER.getPermission();
        public static final String MERCHANT = CommandType.MERCHANT.getPermission();
        public static final String STATS = CommandType.STATS.getPermission();
        public static final String CANCEL = CommandType.CANCEL.getPermission();

        private Command() {}
    }
    
    public static final class Manage {
        public static final String ABOUT = node(MANAGE, "about");
        public static final String RELOAD = node(MANAGE, "reload");
        public static final String LIST = node(MANAGE, "list");
        public static final String CANCEL = node(MANAGE, "cancel");
        
        private Manage() {}
    }
    
    public static final class Chat {
        public static final String PLACEHOLDER = node(CHAT, "placeholder");
        
        public static final class Placeholder {
            public static final String INVENTORY = node(CHAT, "placeholder", ShareConstants.INVENTORY.toLowerCase());
            public static final String HOTBAR = node(CHAT, "placeholder", ShareConstants.HOTBAR.toLowerCase());
            public static final String ENDERCHEST = node(CHAT, "placeholder", ShareConstants.ENDER_CHEST.toLowerCase());
            public static final String STATS = node(CHAT, "placeholder", ShareConstants.STATS.toLowerCase());
            public static final String ITEM = node(CHAT, "placeholder", ShareConstants.ITEM.toLowerCase());

            private Placeholder() {}
        }
        
        private Chat() {}
    }

    /**
     * Convert ShowcaseManager.ShareType to CommandType for permission checking
     * @param shareType the share type to convert
     * @return corresponding CommandType
     * @throws IllegalArgumentException if shareType is not supported
     */
    public static CommandType getCommandTypeFromShareType(com.showcase.command.ShowcaseManager.ShareType shareType) {
        return switch (shareType) {
            case ITEM -> CommandType.ITEM;
            case INVENTORY -> CommandType.INVENTORY;
            case HOTBAR -> CommandType.HOTBAR;
            case ENDER_CHEST -> CommandType.ENDERCHEST;
            case STATS -> CommandType.STATS;
            case CONTAINER -> CommandType.CONTAINER;
            case MERCHANT -> CommandType.MERCHANT;
        };
    }

    /**
     * Build permission node path
     * @param parts permission node parts
     * @return full permission path joined with dots
     */
    public static String node(String... parts) {
        return String.join(".", parts);
    }
    
    private Permissions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}