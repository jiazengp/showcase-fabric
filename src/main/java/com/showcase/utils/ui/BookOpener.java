package com.showcase.utils.ui;

import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class BookOpener {
    private static final String BOOK_MARK = "isShowcaseBook";
    private final ItemStack book;
    private final BookGui gui;

    public BookOpener(ServerPlayer player, ItemStack book) {
        this.book = book;
        this.gui = new BookGui(player, getNormalizeBook());
    }

    public ItemStack getNormalizeBook() {
        ItemStack bookCopy = this.book.copy();
        CustomData customData = bookCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = customData.copyTag();
        nbt.putBoolean(BOOK_MARK, true);
        bookCopy.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        return bookCopy;
    }

    public void open() {
        gui.open();
    }
}