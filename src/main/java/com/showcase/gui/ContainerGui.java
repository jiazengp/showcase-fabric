package com.showcase.gui;

import com.showcase.utils.BookOpener;
import com.showcase.utils.MapViewer;
import com.showcase.utils.StackUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

public class ContainerGui extends SimpleGui {
    private ContainerGui parentGui = null;
    private boolean isOpeningNestedContainer = false;

    public ContainerGui(MenuType<?> containerType, ServerPlayer player,
                        Component title, NonNullList<ItemStack> items) {
        super(containerType, player, false);
        int expectedSlots = this.getHeight() * this.getWidth();
        if (items.size() > expectedSlots) throw new IllegalArgumentException("Items size exceeds container capacity");
        this.setTitle(title);
        this.setupItems(items);
    }

    public ContainerGui(MenuType<?> containerType, ServerPlayer player,
                        Component title, Container inventory) {
        super(containerType, player, false);
        this.setTitle(title);
        this.setupItemsFromInventory(inventory);
    }

    public void setParentGui(ContainerGui parent) {
        this.parentGui = parent;
    }

    private void prepareToOpenNextGUI() {
        isOpeningNestedContainer = true;
    }

    private void openNestedContainer(ItemStack clickedItem, NonNullList<ItemStack> contents,
                                     MenuType<?> containerType) {
        if (contents.isEmpty()) return;

        prepareToOpenNextGUI();

        ContainerGui nestedGui = new ContainerGui(
                containerType,
                this.player,
                clickedItem.getHoverName(),
                contents
        );

        nestedGui.setParentGui(this);
        this.close(false);

        nestedGui.open();
        isOpeningNestedContainer = false;
    }

    private NonNullList<ItemStack> getCurrentItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(this.getVirtualSize(), ItemStack.EMPTY);
        for (int i = 0; i < this.getVirtualSize(); i++) {
            GuiElement element = this.getGuiElement(i);
            if (element != null && !element.getItemStack().isEmpty()) {
                items.set(i, element.getItemStack().copy());
            }
        }
        return items;
    }

    @Override
    public void onPlayerClose(boolean ignored) {
        if (isOpeningNestedContainer) {
            isOpeningNestedContainer = false;
            return;
        }

        openPreviousGui();
        super.onPlayerClose(ignored);
    }

    private void openPreviousGui() {
        if (parentGui != null) parentGui.open();
    }

    private static NonNullList<ItemStack> getShulkerBoxContents(ItemStack shulkerBox) {
        ItemContainerContents container = shulkerBox.get(DataComponents.CONTAINER);
        if (container != null) {
            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            container.copyInto(items);
            return items;
        }
        return NonNullList.create();
    }

    private static NonNullList<ItemStack> getBundleContents(ItemStack bundle) {
        NonNullList<ItemStack> items = NonNullList.create();
        BundleContents bundleContents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            bundleContents.itemCopyStream().forEach(items::add);
        }
        return items;
    }

    private static MenuType<?> getShulkerBoxContainerType() {
        return MenuType.SHULKER_BOX;
    }

    private static MenuType<?> getBundleContainerType() {return MenuType.GENERIC_9x1; }

    private void setupItems(NonNullList<ItemStack> items) {
        int maxSlots = Math.min(items.size(), this.getVirtualSize());

        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                this.setSlot(i, createReadOnlyElement(stack));
            }
        }
    }

    private void setupItemsFromInventory(Container inventory) {
        int maxSlots = Math.min(inventory.getContainerSize(), this.getVirtualSize());

        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                this.setSlot(i, createReadOnlyElement(stack));
            }
        }
    }

    private GuiElement createReadOnlyElement(ItemStack stack) {
        return new SimpleGuiElement(stack.copy(), this::handleItemClick);
    }

    private void handleItemClick(int i, ClickType clickType, ContainerInput slotActionType, eu.pb4.sgui.api.gui.SlotBasedGui slotGuiInterface) {}

    private void onShulkerBoxClick(ItemStack stack, ContainerGui gui) {
        gui.openNestedContainer(stack, getShulkerBoxContents(stack), getShulkerBoxContainerType());
    }

    private void onBundleClick(ItemStack stack, ContainerGui gui) {
        gui.openNestedContainer(stack, getBundleContents(stack), getBundleContainerType());
    }

    private void onBookClick(ItemStack stack, ServerPlayer player) {
        this.close();
        BookOpener bookOpener = new BookOpener(player, stack);
        bookOpener.open();
    }

    private void onMapClick(ItemStack stack, ServerPlayer player) {
        this.close();
        MapViewer.viewMap(player, stack);
    }

    public void refreshItems(NonNullList<ItemStack> items) {
        for (int i = 0; i < this.getVirtualSize(); i++) {
            this.clearSlot(i);
        }

        this.setupItems(items);
    }

    public void refreshItemsFromInventory(Container inventory) {
        for (int i = 0; i < this.getVirtualSize(); i++) {
            this.clearSlot(i);
        }

        this.setupItemsFromInventory(inventory);
    }

    @Override
    public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
        if (type == ClickType.DROP || type == ClickType.CTRL_DROP) {
            this.close();
            return true;
        }

        if (element == null) return false;

        ItemStack clickedStack = element.getItemStack();

        if (clickedStack == null || clickedStack.isEmpty()) return false;
        if (StackUtils.isShulkerBox(clickedStack)) onShulkerBoxClick(clickedStack, this);
        if (StackUtils.isBundle(clickedStack)) onBundleClick(clickedStack, this);
        if (StackUtils.isMap(clickedStack)) onMapClick(clickedStack, this.player);
        if (StackUtils.isBook(clickedStack)) onBookClick(clickedStack, this.player);

        return false;
    }
}
