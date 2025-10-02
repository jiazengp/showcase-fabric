package com.showcase.utils.compat;

import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
#if MC_VER >= 1219
import net.minecraft.text.StyleSpriteSource;
#endif

/**
 * Compatibility layer for Style APIs across different Minecraft versions.
 * Handles API differences in 1.21.9+.
 */
public class StyleCompat {

    /**
     * Sets the font for a Style.
     * In 1.21.9+, withFont() parameter type changed from Identifier to StyleSpriteSource.
     */
    public static Style withFont(Style style, Identifier fontId) {
        #if MC_VER >= 1219
        return style.withFont(new StyleSpriteSource.Font(fontId));
        #else
        return style.withFont(fontId);
        #endif
    }
}
