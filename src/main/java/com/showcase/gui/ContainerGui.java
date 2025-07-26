package com.showcase.gui;

import com.showcase.utils.BookOpener;
import com.showcase.utils.MapViewer;
import com.showcase.utils.StackUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.screen.slot.SlotActionType;

public class ContainerGui extends SimpleGui {
    private ContainerGui parentGui = null;
    private boolean isOpeningNestedContainer = false;

    public ContainerGui(ScreenHandlerType<?> containerType, ServerPlayerEntity player,
                        Text title, DefaultedList<ItemStack> items) {
        super(containerType, player, false);
        int expectedSlots = GuiHelpers.getHeight(containerType) * GuiHelpers.getWidth(containerType);
        if (items.size() > expectedSlots) throw new IllegalArgumentException("Items size exceeds container capacity");
        this.setTitle(title);
        this.setupItems(items);
    }

    public ContainerGui(ScreenHandlerType<?> containerType, ServerPlayerEntity player,
                        Text title, Inventory inventory) {
        super(containerType, player, false);
        this.setTitle(title);
        this.setupItemsFromInventory(inventory);
    }

    public void setParentGui(ContainerGui parent) {
        this.parentGui = parent;
    }

    private void prepareToOpenNextGUI() {
        isOpeningNestedContainer = true;
        GuiHelpers.ignoreNextGuiClosing(this.player);
    }

    private void openNestedContainer(ItemStack clickedItem, DefaultedList<ItemStack> contents,
                                     ScreenHandlerType<?> containerType) {
        if (contents.isEmpty()) return;

        prepareToOpenNextGUI();

        ContainerGui nestedGui = new ContainerGui(
                containerType,
                this.player,
                clickedItem.getName(),
                contents
        );

        nestedGui.setParentGui(this);
        this.close(false);

        nestedGui.open();
        isOpeningNestedContainer = false;
    }

    private DefaultedList<ItemStack> getCurrentItems() {
        DefaultedList<ItemStack> items = DefaultedList.ofSize(this.getVirtualSize(), ItemStack.EMPTY);
        for (int i = 0; i < this.getVirtualSize(); i++) {
            GuiElementInterface element = this.getSlot(i);
            if (element != null && !element.getItemStack().isEmpty()) {
                items.set(i, element.getItemStack().copy());
            }
        }
        return items;
    }

    @Override
    public void onClose() {
        if (isOpeningNestedContainer) {
            isOpeningNestedContainer = false;
            return;
        }

        super.onClose();

        openPreviousGui();
    }

    private void openPreviousGui() {
        if (parentGui != null) parentGui.open();
    }

    private static DefaultedList<ItemStack> getShulkerBoxContents(ItemStack shulkerBox) {
        ContainerComponent container = shulkerBox.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
            container.copyTo(items);
            return items;
        }
        return DefaultedList.of();
    }

    private static DefaultedList<ItemStack> getBundleContents(ItemStack bundle) {
        BundleContentsComponent bundleContents = bundle.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            DefaultedList<ItemStack> items = DefaultedList.of();
            bundleContents.iterate().forEach(items::add);
            return items;
        }
        return DefaultedList.of();
    }

    private static ScreenHandlerType<?> getShulkerBoxContainerType() {
        return ScreenHandlerType.SHULKER_BOX;
    }

    private static ScreenHandlerType<?> getBundleContainerType() {return ScreenHandlerType.GENERIC_9X1; }

    private void setupItems(DefaultedList<ItemStack> items) {
        int maxSlots = Math.min(items.size(), this.getVirtualSize());

        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                this.setSlot(i, createReadOnlyElement(stack));
            }
        }
    }

    private void setupItemsFromInventory(Inventory inventory) {
        int maxSlots = Math.min(inventory.size(), this.getVirtualSize());

        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                this.setSlot(i, createReadOnlyElement(stack));
            }
        }
    }

    private GuiElementInterface createReadOnlyElement(ItemStack stack) {
        return new GuiElement(stack.copy(), this::handleItemClick);
    }

    private void handleItemClick(int i, ClickType clickType, SlotActionType slotActionType, SlotGuiInterface slotGuiInterface) {}

    private void onShulkerBoxClick(ItemStack stack, ContainerGui gui) {
        gui.openNestedContainer(stack, getShulkerBoxContents(stack), getShulkerBoxContainerType());
    }

    private void onBundleClick(ItemStack stack, ContainerGui gui) {
        gui.openNestedContainer(stack, getBundleContents(stack), getBundleContainerType());
    }

    private void onBookClick(ItemStack stack, ServerPlayerEntity player) {
        prepareToOpenNextGUI();

        BookOpener bookOpener = new BookOpener(player, stack, () -> {});

        this.player.getWorld().getServer().execute(() -> {
            bookOpener.open();
            isOpeningNestedContainer = false;
        });
    }

    private void onMapClick(ItemStack stack, ServerPlayerEntity player) {
        prepareToOpenNextGUI();

        this.player.getWorld().getServer().execute(() -> {
            MapViewer.viewMap(player, stack, (closedPlayer, mapItem) ->
            {
                if (!isOpeningNestedContainer) openPreviousGui();
            });

            isOpeningNestedContainer = false;
        });
    }

    public void refreshItems(DefaultedList<ItemStack> items) {
        for (int i = 0; i < this.getVirtualSize(); i++) {
            this.clearSlot(i);
        }

        this.setupItems(items);
    }

    public void refreshItemsFromInventory(Inventory inventory) {
        for (int i = 0; i < this.getVirtualSize(); i++) {
            this.clearSlot(i);
        }

        this.setupItemsFromInventory(inventory);
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        if (type == ClickType.DROP || type == ClickType.CTRL_DROP) {
            this.close();
            return true;
        }

        ItemStack clickedStack = element.getItemStack();

        if (clickedStack == null) return false;
        if (StackUtils.isShulkerBox(clickedStack)) onShulkerBoxClick(clickedStack, this);
        if (StackUtils.isBundle(clickedStack)) onBundleClick(clickedStack, this);
        if (StackUtils.isMap(clickedStack)) onMapClick(clickedStack, this.player);
        if (StackUtils.isBook(clickedStack)) onBookClick(clickedStack, this.player);

        return false;
    }
}