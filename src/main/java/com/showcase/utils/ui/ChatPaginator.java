package com.showcase.utils.ui;

import com.showcase.utils.TextEventFactory;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatPaginator<T> {
    private final List<T> items;
    private final int pageSize;
    private final String commandPrefix;

    public ChatPaginator(List<T> items, int pageSize, String commandPrefix) {
        this.items = items != null ? items : Collections.emptyList();
        this.pageSize = pageSize;
        this.commandPrefix = commandPrefix;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) items.size() / pageSize);
    }

    public int clampPage(int page) {
        return Math.max(1, Math.min(page, getTotalPages()));
    }

    public synchronized List<T> getPageItems(int page) {
        page = clampPage(page);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(page * pageSize, items.size());
        return items.subList(fromIndex, toIndex);
    }

    public MutableComponent renderPage(int page, Function<T, Component> renderer, String title) {
        int totalPages = getTotalPages();
        page = clampPage(page);
        List<T> pageItems = getPageItems(page);

        MutableComponent result = Component.literal("")
                .append(Component.literal(title + " Page " + page + "/" + totalPages + "\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (T item : pageItems) {
            result.append(renderer.apply(item));
        }

        if (totalPages > 1) {
            result.append(buildNavLine(page, totalPages));
        }

        return result;
    }

    private MutableComponent buildNavLine(int page, int totalPages) {
        MutableComponent nav = Component.literal("\n");

        if (page > 1) {
            nav.append(Component.translatable("spectatorMenu.previous_page").withStyle(ChatFormatting.BLUE)
                    .withStyle(s -> s.withClickEvent(TextEventFactory.navigateToPage(commandPrefix, page - 1))
                            .withHoverEvent(TextEventFactory.previousPageTooltip())));
        }

        nav.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));

        if (page < totalPages) {
            nav.append(Component.translatable("spectatorMenu.next_page").withStyle(ChatFormatting.BLUE)
                    .withStyle(s -> s.withClickEvent(TextEventFactory.navigateToPage(commandPrefix, page + 1))
                            .withHoverEvent(TextEventFactory.nextPageTooltip())));
        }

        return nav;
    }
}