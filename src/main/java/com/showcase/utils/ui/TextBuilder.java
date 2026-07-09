package com.showcase.utils.ui;

import com.showcase.config.ModConfigManager;
import com.showcase.utils.compat.TextEventCompat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

/**
 * Unified text creation utility that combines functionality from TextUtils and TextEventFactory
 * Provides a consistent API for creating formatted text with events
 */
public final class TextBuilder {
    
    // Common formatting styles
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final Style WARNING_STYLE = Style.EMPTY.withColor(ChatFormatting.YELLOW);
    private static final Style SUCCESS_STYLE = Style.EMPTY.withColor(ChatFormatting.GREEN);
    private static final Style INFO_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style HIGHLIGHT_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);
    
    private TextBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // Basic formatted text methods
    
    /**
     * Creates error text with red formatting
     * @param text the text content
     * @return formatted error text
     */
    public static MutableComponent error(Component text) {
        return text.copy().setStyle(ERROR_STYLE);
    }
    
    /**
     * Creates error text from translation key
     * @param key translation key
     * @param args translation arguments
     * @return formatted error text
     */
    public static MutableComponent error(String key, Object... args) {
        return Component.translatable(key, args).setStyle(ERROR_STYLE);
    }
    
    /**
     * Creates warning text with yellow formatting
     * @param text the text content
     * @return formatted warning text
     */
    public static MutableComponent warning(Component text) {
        return text.copy().setStyle(WARNING_STYLE);
    }
    
    /**
     * Creates warning text from translation key
     * @param key translation key
     * @param args translation arguments
     * @return formatted warning text
     */
    public static MutableComponent warning(String key, Object... args) {
        return Component.translatable(key, args).setStyle(WARNING_STYLE);
    }
    
    /**
     * Creates success text with green formatting  
     * @param text the text content
     * @return formatted success text
     */
    public static MutableComponent success(Component text) {
        return text.copy().setStyle(SUCCESS_STYLE);
    }
    
    /**
     * Creates success text from translation key
     * @param key translation key
     * @param args translation arguments
     * @return formatted success text
     */
    public static MutableComponent success(String key, Object... args) {
        return Component.translatable(key, args).setStyle(SUCCESS_STYLE);
    }
    
    /**
     * Creates info text with aqua formatting
     * @param text the text content
     * @return formatted info text
     */
    public static MutableComponent info(Component text) {
        return text.copy().setStyle(INFO_STYLE);
    }

    public static HoverEvent showItem(ItemStack itemStack) {
        return TextEventCompat.showItem(itemStack);
    }
    /**
     * Creates info text from translation key
     * @param key translation key
     * @param args translation arguments
     * @return formatted info text
     */
    public static MutableComponent info(String key, Object... args) {
        return Component.translatable(key, args).setStyle(INFO_STYLE);
    }
    
    /**
     * Creates highlighted text with gold formatting
     * @param text the text content
     * @return formatted highlight text
     */
    public static MutableComponent highlight(Component text) {
        return text.copy().setStyle(HIGHLIGHT_STYLE);
    }
    
    /**
     * Creates highlighted text from translation key
     * @param key translation key
     * @param args translation arguments
     * @return formatted highlight text
     */
    public static MutableComponent highlight(String key, Object... args) {
        return Component.translatable(key, args).setStyle(HIGHLIGHT_STYLE);
    }
    
    // Interactive text methods (combining TextEventFactory functionality)
    
    /**
     * Creates clickable text with a click event
     * @param text the text content
     * @param clickEvent the click event
     * @return clickable text
     */
    public static MutableComponent clickable(Component text, ClickEvent clickEvent) {
        return text.copy().withStyle(style -> style.withClickEvent(clickEvent));
    }

    /**
     * Creates clickable text with a click event, optionally applying underline based on configuration
     * @param text the text content
     * @param clickEvent the click event
     * @param applyUnderline whether to apply underline styling (respects config if true)
     * @return clickable text with optional underline
     */
    public static MutableComponent clickableWithConfig(Component text, ClickEvent clickEvent, boolean applyUnderline) {
        return text.copy().withStyle(style -> {
            Style newStyle = style.withClickEvent(clickEvent);
            if (applyUnderline && ModConfigManager.getConfig().shareLink.enableClickableTextUnderline) {
                newStyle = newStyle.withUnderlined(true);
            }
            return newStyle;
        });
    }

    /**
     * Creates clickable text with a click event, applying underline based on configuration
     * @param text the text content
     * @param clickEvent the click event
     * @return clickable text with conditional underline
     */
    public static MutableComponent clickableWithConfig(Component text, ClickEvent clickEvent) {
        return clickableWithConfig(text, clickEvent, true);
    }
    
    /**
     * Creates clickable text with hover and click events
     * @param text the text content
     * @param clickEvent the click event
     * @param hoverEvent the hover event
     * @return interactive text with both events
     */
    public static MutableComponent interactive(Component text, ClickEvent clickEvent, HoverEvent hoverEvent) {
        return text.copy().withStyle(style -> style
                .withClickEvent(clickEvent)
                .withHoverEvent(hoverEvent));
    }

    /**
     * Creates interactive text with hover and click events, applying underline based on configuration
     * @param text the text content
     * @param clickEvent the click event
     * @param hoverEvent the hover event
     * @param applyUnderline whether to apply underline styling (respects config if true)
     * @return interactive text with both events and optional underline
     */
    public static MutableComponent interactiveWithConfig(Component text, ClickEvent clickEvent, HoverEvent hoverEvent, boolean applyUnderline) {
        return text.copy().withStyle(style -> {
            Style newStyle = style.withClickEvent(clickEvent).withHoverEvent(hoverEvent);
            if (applyUnderline && ModConfigManager.getConfig().shareLink.enableClickableTextUnderline) {
                newStyle = newStyle.withUnderlined(true);
            }
            return newStyle;
        });
    }

    /**
     * Creates interactive text with hover and click events, applying underline based on configuration
     * @param text the text content
     * @param clickEvent the click event
     * @param hoverEvent the hover event
     * @return interactive text with both events and conditional underline
     */
    public static MutableComponent interactiveWithConfig(Component text, ClickEvent clickEvent, HoverEvent hoverEvent) {
        return interactiveWithConfig(text, clickEvent, hoverEvent, true);
    }
    
    /**
     * Creates a run command clickable text
     * @param text the display text
     * @param command the command to run (without /)
     * @return clickable command text
     */
    public static MutableComponent runCommand(Component text, String command) {
        return clickable(text, TextEventCompat.runCommand("/" + command));
    }
    
    /**
     * Creates a suggest command clickable text
     * @param text the display text
     * @param command the command to suggest (without /)
     * @return clickable suggestion text
     */
    public static MutableComponent suggestCommand(Component text, String command) {
        return clickable(text, TextEventCompat.suggestCommand("/" + command));
    }
    
    /**
     * Creates clickable URL text
     * @param text the display text
     * @param url the URL to open
     * @return clickable URL text
     */
    public static MutableComponent url(Component text, String url) {
        return clickable(text, TextEventCompat.openUrl(url));
    }
    
    /**
     * Creates text with hover tooltip
     * @param text the main text
     * @param tooltip the tooltip text
     * @return text with hover event
     */
    public static MutableComponent withTooltip(Component text, Component tooltip) {
        return text.copy().withStyle(style -> style.withHoverEvent(TextEventCompat.showText(tooltip)));
    }
    
    // Builder pattern for complex text construction
    
    /**
     * Starts building a complex text component
     * @param text the initial text
     * @return a TextComponentBuilder for chaining
     */
    public static TextComponentBuilder build(Component text) {
        return new TextComponentBuilder(text.copy());
    }
    
    /**
     * Starts building a complex text component from translation key
     * @param key translation key
     * @param args translation arguments
     * @return a TextComponentBuilder for chaining
     */
    public static TextComponentBuilder build(String key, Object... args) {
        return new TextComponentBuilder(Component.translatable(key, args));
    }
    
    /**
     * Builder class for creating complex text components with method chaining
     */
    public static class TextComponentBuilder {
        private MutableComponent text;
        
        private TextComponentBuilder(MutableComponent text) {
            this.text = text;
        }
        
        public TextComponentBuilder style(ChatFormatting... formatting) {
            this.text = (MutableComponent) text.withStyle(formatting);
            return this;
        }
        
        public TextComponentBuilder color(ChatFormatting color) {
            this.text = (MutableComponent) text.withStyle(style -> style.withColor(color));
            return this;
        }
        
        public TextComponentBuilder click(ClickEvent clickEvent) {
            this.text = (MutableComponent) text.withStyle(style -> style.withClickEvent(clickEvent));
            return this;
        }

        public TextComponentBuilder clickWithConfig(ClickEvent clickEvent) {
            return clickWithConfig(clickEvent, true);
        }

        public TextComponentBuilder clickWithConfig(ClickEvent clickEvent, boolean applyUnderline) {
            this.text = (MutableComponent) text.withStyle(style -> {
                Style newStyle = style.withClickEvent(clickEvent);
                if (applyUnderline && ModConfigManager.getConfig().shareLink.enableClickableTextUnderline) {
                    newStyle = newStyle.withUnderlined(true);
                }
                return newStyle;
            });
            return this;
        }
        
        public TextComponentBuilder hover(HoverEvent hoverEvent) {
            this.text = (MutableComponent) text.withStyle(style -> style.withHoverEvent(hoverEvent));
            return this;
        }
        
        public TextComponentBuilder runCommand(String command) {
            return click(TextEventCompat.runCommand("/" + command));
        }
        
        public TextComponentBuilder suggestCommand(String command) {
            return click(TextEventCompat.suggestCommand("/" + command));
        }
        
        public TextComponentBuilder url(String url) {
            return click(TextEventCompat.openUrl(url));
        }
        
        public TextComponentBuilder tooltip(Component tooltip) {
            return hover(TextEventCompat.showText(tooltip));
        }
        
        public TextComponentBuilder append(Component other) {
            text.append(other);
            return this;
        }
        
        public MutableComponent build() {
            return text;
        }
    }
}
