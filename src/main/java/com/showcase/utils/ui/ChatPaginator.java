package com.showcase.utils.ui;

import com.showcase.utils.TextEventFactory;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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

    public MutableText renderPage(int page, Function<T, Text> renderer, String title) {
        int totalPages = getTotalPages();
        page = clampPage(page);
        List<T> pageItems = getPageItems(page);

        MutableText result = Text.literal("")
                .append(Text.literal(title + " Page " + page + "/" + totalPages + "\n").formatted(Formatting.GOLD, Formatting.BOLD));

        for (T item : pageItems) {
            result.append(renderer.apply(item));
        }

        if (totalPages > 1) {
            result.append(buildNavLine(page, totalPages));
        }

        return result;
    }

    private MutableText buildNavLine(int page, int totalPages) {
        MutableText nav = Text.literal("\n");

        if (page > 1) {
            nav.append(Text.translatable("spectatorMenu.previous_page").formatted(Formatting.BLUE)
                    .styled(s -> s.withClickEvent(TextEventFactory.navigateToPage(commandPrefix, page - 1))
                            .withHoverEvent(TextEventFactory.previousPageTooltip())));
        }

        nav.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY));

        if (page < totalPages) {
            nav.append(Text.translatable("spectatorMenu.next_page").formatted(Formatting.BLUE)
                    .styled(s -> s.withClickEvent(TextEventFactory.navigateToPage(commandPrefix, page + 1))
                            .withHoverEvent(TextEventFactory.nextPageTooltip())));
        }

        return nav;
    }
}