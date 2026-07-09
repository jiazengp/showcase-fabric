package com.showcase.utils.compat;

import java.net.URI;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Helpers for chat click and hover events on the 26.x mainline.
 */
public class TextEventCompat {
    
    /**
     * Creates a run command click event.
     */
    public static ClickEvent runCommand(String command) {
        return new ClickEvent.RunCommand(command);
    }
    
    /**
     * Creates a run full command click event (with / prefix).
     */
    public static ClickEvent runFullCommand(String command) {
        String fullCommand = command.startsWith("/") ? command : "/" + command;
        return runCommand(fullCommand);
    }
    
    /**
     * Creates a suggest command click event.
     */
    public static ClickEvent suggestCommand(String command) {
        return new ClickEvent.SuggestCommand(command);
    }
    
    /**
     * Creates an open URL click event.
     */
    public static ClickEvent openUrl(String url) {
        return new ClickEvent.OpenUrl(URI.create(url));
    }
    
    /**
     * Creates an open URL click event from URI.
     */
    public static ClickEvent openUrl(URI uri) {
        return new ClickEvent.OpenUrl(uri);
    }
    
    /**
     * Creates a copy to clipboard click event.
     */
    public static ClickEvent copyToClipboard(String text) {
        return new ClickEvent.CopyToClipboard(text);
    }
    
    /**
     * Creates a show text hover event.
     */
    public static HoverEvent showText(Component text) {
        return new HoverEvent.ShowText(text);
    }
    
    /**
     * Creates a show text hover event from string.
     */
    public static HoverEvent showText(String text) {
        return showText(Component.literal(text));
    }
    
    /**
     * Creates a show item hover event.
     */
    public static HoverEvent showItem(ItemStack itemStack) {
        ItemStackTemplate template = itemStack.isEmpty()
            ? new ItemStackTemplate(itemStack.getItem())
            : ItemStackTemplate.fromNonEmptyStack(itemStack);
        return new HoverEvent.ShowItem(template);
    }
}
