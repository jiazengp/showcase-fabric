package com.showcase.utils;

import com.showcase.gui.ContainerGui;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public final class BookOpener {
    private static final String BOOK_MARK = "isShowcaseBook";
    private final ItemStack book;
    private final BookGui gui;

    public BookOpener(ServerPlayerEntity player, ItemStack book) {
        this.book = book;
        this.gui = new BookGui(player, getNormalizeBook());
    }

    public ItemStack getNormalizeBook() {
        ItemStack bookCopy = this.book.copy();
        NbtComponent customData = bookCopy.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = customData.copyNbt();
        nbt.putBoolean(BOOK_MARK, true);
        bookCopy.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        return bookCopy;
    }

    public void open() {
        gui.open();
    }
}