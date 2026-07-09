package com.showcase.utils.compat;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/**
 * Helpers around Style APIs on the 26.x mainline.
 */
public class StyleCompat {

    /**
     * Sets the font for a Style.
     * In 26.x+, withFont() takes a FontDescription.
     */
    public static Style withFont(Style style, Identifier fontId) {
        return style.withFont(new FontDescription.Resource(fontId));
    }
}
