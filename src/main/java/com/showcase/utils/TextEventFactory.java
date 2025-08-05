package com.showcase.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.net.URI;

/**
 * Factory class for creating text events with version compatibility.
 * This class provides a unified interface for creating HoverEvent and ClickEvent instances
 * that can be easily modified for different Minecraft versions.
 */
public class TextEventFactory {

    /**
     * Creates a ClickEvent that runs a command when clicked
     * @param command The command to run (without leading slash)
     * @return ClickEvent instance
     */
    public static ClickEvent runCommand(String command) {
        return new ClickEvent.RunCommand("/" + command);
    }
    
    /**
     * Creates a ClickEvent that runs a command when clicked
     * @param command The full command to run (with leading slash)
     * @return ClickEvent instance
     */
    public static ClickEvent runFullCommand(String command) {
        return new ClickEvent.RunCommand(command);
    }
    
    /**
     * Creates a ClickEvent that suggests a command in the chat input
     * @param command The command to suggest
     * @return ClickEvent instance
     */
    public static ClickEvent suggestCommand(String command) {
        return new ClickEvent.SuggestCommand(command);
    }
    
    /**
     * Creates a ClickEvent that opens a URL when clicked
     * @param url The URL to open
     * @return ClickEvent instance
     */
    public static ClickEvent openUrl(String url) {
        return new ClickEvent.OpenUrl(URI.create(url));
    }
    
    /**
     * Creates a ClickEvent that opens a URL when clicked
     * @param uri The URI to open
     * @return ClickEvent instance
     */
    public static ClickEvent openUrl(URI uri) {
        return new ClickEvent.OpenUrl(uri);
    }
    
    /**
     * Creates a ClickEvent that copies text to clipboard when clicked
     * @param text The text to copy
     * @return ClickEvent instance
     */
    public static ClickEvent copyToClipboard(String text) {
        return new ClickEvent.CopyToClipboard(text);
    }

    // HoverEvent factory methods
    
    /**
     * Creates a HoverEvent that shows text when hovered
     * @param text The text to show
     * @return HoverEvent instance
     */
    public static HoverEvent showText(Text text) {
        return new HoverEvent.ShowText(text);
    }
    
    /**
     * Creates a HoverEvent that shows text when hovered
     * @param text The text to show as string
     * @return HoverEvent instance
     */
    public static HoverEvent showText(String text) {
        return new HoverEvent.ShowText(Text.literal(text));
    }
    
    /**
     * Creates a HoverEvent that shows an item when hovered
     * @param itemStack The item to show
     * @return HoverEvent instance
     */
    public static HoverEvent showItem(ItemStack itemStack) {
        return new HoverEvent.ShowItem(itemStack);
    }

    /**
     * Creates a ClickEvent for viewing a share with the given ID
     * @param shareId The share ID
     * @return ClickEvent instance
     */
    public static ClickEvent viewShare(String shareId) {
        return runFullCommand("/showcase-view " + shareId);
    }

    /**
     * Creates a ClickEvent for navigating to a specific page
     * @param commandPrefix The command prefix (e.g., "/showcase manage list")
     * @param page The page number
     * @return ClickEvent instance
     */
    public static ClickEvent navigateToPage(String commandPrefix, int page) {
        return runFullCommand(commandPrefix + " " + page);
    }

    /**
     * Creates a HoverEvent for previous page tooltip
     * @return HoverEvent instance
     */
    public static HoverEvent previousPageTooltip() {
        return showText(Text.translatable("spectatorMenu.previous_page"));
    }
    
    /**
     * Creates a HoverEvent for next page tooltip
     * @return HoverEvent instance
     */
    public static HoverEvent nextPageTooltip() {
        return showText(Text.translatable("spectatorMenu.next_page"));
    }
    
    /**
     * Creates a HoverEvent for cancel share tooltip
     * @return HoverEvent instance
     */
    public static HoverEvent cancelShareTooltip() {
        return showText(Text.translatable("showcase.message.manage.cancel.tip"));
    }
}