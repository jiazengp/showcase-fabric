package com.showcase.utils.compat;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Compatibility layer for GuiElementBuilder across different Minecraft versions.
 * Handles API differences between 1.21.1 and 1.21.2+.
 */
public class GuiElementBuilderCompat {
    
    /**
     * Sets the model item for a GuiElementBuilder.
     * In MC 1.21.1, model() method doesn't exist, use setItem() instead.
     */
    public static GuiElementBuilder setModel(GuiElementBuilder builder, Item item) {
        #if MC_VER >= 1212
        return builder.model(item);
        #else
        return builder.setItem(item);
        #endif
    }
}