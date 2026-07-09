package com.showcase.utils.compat;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.Item;

/**
 * Small wrapper around the SGUI builder API used by the 26.x mainline.
 */
public class GuiElementBuilderCompat {
    
    /**
     * Sets the model item for a GuiElementBuilder.
     */
    public static GuiElementBuilder setModel(GuiElementBuilder builder, Item item) {
        return builder.model(item);
    }
}
