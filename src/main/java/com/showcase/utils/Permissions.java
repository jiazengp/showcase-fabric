package com.showcase.utils;

import com.showcase.command.ShowcaseManager;

import java.util.Locale;

/**
 * Centralized permission constants to avoid hardcoding and typos
 */
public class Permissions {
    
    // Base permissions
    public static final String ADMIN = "admin";
    
    // Command permissions
    public static final String COMMANDS = "commands";
    public static final String COMMANDS_VIEW = "commands.view";
    public static final String COMMANDS_CANCEL = "commands.cancel";
    
    // Chat permissions
    public static final String CHAT_PLACEHOLDER = "chat.placeholder";
    
    // Share type command permissions
    public static final String COMMANDS_ITEM = "commands.item";
    public static final String COMMANDS_INVENTORY = "commands.inventory";
    public static final String COMMANDS_HOTBAR = "commands.hotbar";
    public static final String COMMANDS_ENDER_CHEST = "commands.ender_chest";
    public static final String COMMANDS_STATS = "commands.stats";
    public static final String COMMANDS_CONTAINER = "commands.container";
    public static final String COMMANDS_MERCHANT = "commands.merchant";
    
    // Dynamic command permission generator for share types
    public static String commands(ShowcaseManager.ShareType shareType) {
        return "commands." + shareType.name().toLowerCase(Locale.ROOT);
    }
}