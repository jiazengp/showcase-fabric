package com.showcase.gui;

import com.showcase.utils.BookOpener;
import com.showcase.utils.MapViewer;
import com.showcase.utils.StackUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ItemViewerGui extends BaseWorldGui {
    public ItemViewerGui(ItemViewerContext context, int slot) {
        super(context, slot);
        this.rebuildUi();
        this.open();
        
        // Send instruction message when GUI is opened
        context.sendInstructionMessage();
    }

    @Override
    protected void buildUi() {
        ItemViewerContext itemContext = (ItemViewerContext) context;
        // Place the item in the main hand (slot 0)
        this.addSlot(itemContext.getItem());
    }

    @Override
    protected BaseViewerContext.SwitchEntry asSwitchableUi() {
        return new BaseViewerContext.SwitchEntry((context, slot) -> new ItemViewerGui((ItemViewerContext) context, slot), this.getSelectedSlot());
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        if (type == ClickType.DROP || type == ClickType.CTRL_DROP) {
            this.close();
            return true;
        }

        // Handle special item interactions similar to ContainerGui
        if (index == this.getSelectedSlot() && element != null) {
            ItemViewerContext itemContext = (ItemViewerContext) context;
            var clickedStack = itemContext.getItem();
            
            if (clickedStack != null && !clickedStack.isEmpty()) {
                // Handle different item types
                if (StackUtils.isMap(clickedStack)) {
                    this.close();
                    MapViewer.viewMap(this.player, clickedStack);
                    return true;
                } else if (StackUtils.isBook(clickedStack)) {
                    this.close();
                    BookOpener bookOpener = new BookOpener(this.player, clickedStack);
                    bookOpener.open();
                    return true;
                }
                // Note: Shulker boxes and bundles don't make sense in single-item view
                // as they require container display
            }
        }

        return super.onClick(index, type, action, element);
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}