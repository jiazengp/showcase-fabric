package com.showcase.gui;

import com.showcase.utils.BookOpener;
import com.showcase.utils.MapViewer;
import com.showcase.utils.StackUtils;
import eu.pb4.sgui.api.ClickType;
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

import java.util.Stack;

public class ContainerGui extends SimpleGui {
    private static final Stack<GuiState> guiStack = new Stack<>();
    private boolean isOpeningNestedContainer = false;

    private record GuiState(ScreenHandlerType<?> containerType, Text title, DefaultedList<ItemStack> items,
                                ServerPlayerEntity player) {
        private GuiState(ScreenHandlerType<?> containerType, Text title, DefaultedList<ItemStack> items, ServerPlayerEntity player) {
            this.containerType = containerType;
            this.title = title;
            this.items = DefaultedList.copyOf(ItemStack.EMPTY, items.toArray(new ItemStack[0]));
            this.player = player;
        }
    }

    // 构造函数：接受容器类型、玩家、标题和物品列表  
    public ContainerGui(ScreenHandlerType<?> containerType, ServerPlayerEntity player,
                        Text title, DefaultedList<ItemStack> items) {
        super(containerType, player, false);
        this.setTitle(title);
        this.setupItems(items);
    }

    // 构造函数：接受容器类型、玩家、标题和Inventory  
    public ContainerGui(ScreenHandlerType<?> containerType, ServerPlayerEntity player,
                        Text title, Inventory inventory) {
        super(containerType, player, false);
        this.setTitle(title);
        this.setupItemsFromInventory(inventory);
    }

    private void prepareToOpenNextGUI() {
        isOpeningNestedContainer = true;

        GuiState currentState = new GuiState(
                this.type,
                this.getTitle(),
                getCurrentItems(),
                this.player
        );

        guiStack.push(currentState);

        this.close();
    }

    private void openNestedContainer(ItemStack clickedItem, DefaultedList<ItemStack> contents,
                                     ScreenHandlerType<?> containerType) {
        if (contents.isEmpty()) return;

        prepareToOpenNextGUI();

        Text nestedTitle = clickedItem.getName();

        this.player.getWorld().getServer().execute(() -> {
            ContainerGui nestedGui = new ContainerGui(
                    containerType,
                    this.player,
                    nestedTitle,
                    contents
            );

            nestedGui.open();
            isOpeningNestedContainer = false;
        });

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
        if (isOpeningNestedContainer) return;

        super.onClose();
        openPreviousGui();
    }

    private void openPreviousGui() {
        if (!guiStack.isEmpty()) {
            GuiState previousState = guiStack.pop();

            this.player.getWorld().getServer().execute(() -> {
                ContainerGui previousGui = new ContainerGui(
                        previousState.containerType,
                        previousState.player,
                        previousState.title,
                        previousState.items
                );
                previousGui.open();
            });
        }
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

    public static void clearGuiStack(ServerPlayerEntity player) {
        guiStack.removeIf(state -> state.player.getUuid().equals(player.getUuid()));
    }

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

        clearGuiStack(player);

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

        if (StackUtils.isShulkerBox(clickedStack)) onShulkerBoxClick(clickedStack, this);
        if (StackUtils.isBundle(clickedStack)) onBundleClick(clickedStack, this);
        if (StackUtils.isMap(clickedStack)) onMapClick(clickedStack, this.player);
        if (StackUtils.isBook(clickedStack)) onBookClick(clickedStack, this.player);

        return false;
    }
}