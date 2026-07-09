package com.showcase.utils.stats;

import com.showcase.config.ModConfigManager;
import com.showcase.utils.PlayerUtils;
import com.showcase.utils.TextUtils;
import com.showcase.utils.TextEventFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatUtils {
    private static final StatCollector statCollector = new StatCollector();
    
    public static Map<String, Integer> getPlayerStats(ServerPlayer player) {
        return statCollector.getPlayerStats(player);
    }
    
    public static Map<String, Map<String, Integer>> categorizeStats(Map<String, Integer> stats) {
        return StatCategories.categorizeStats(stats);
    }
    
    public static ItemStack createStatsBook(ServerPlayer player) {
        Map<String, Integer> playerStats = getPlayerStats(player);
        Map<String, Map<String, Integer>> categorizedStats = categorizeStats(playerStats);
        
        List<Component> pages = new ArrayList<>();
        pages.add(createTitlePage(player));
        
        categorizedStats.forEach((categoryKey, categoryStats) -> 
            pages.addAll(createCategoryPages(categoryKey, categoryStats)));
        
        if (categorizedStats.isEmpty()) {
            pages.add(createEmptyPage());
        }
        
        return createBookItemStack(player, pages);
    }
    
    private static Component createTitlePage(ServerPlayer player) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm");
        String currentTime = now.format(formatter);

        MutableComponent coverPage = Component.empty()
                .append(Component.translatable("gui.stats")
                        .withStyle(ChatFormatting.BOLD))
                .append("\n\n")
                .append(Component.literal("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .append("\n\n")
                .append(Component.translatable("gui.abuseReport.type.name"))
                .append(": ")
                .append(player.getName())
                .append("\n\n")
                .append(Component.literal("UUID: "))
                .append(Component.literal(player.getStringUUID())
                        .withStyle(ChatFormatting.GRAY))
                .append("\n\n")
                .append(Component.translatable("showcase.stats.message.create_date"))
                .append(": ")
                .append(Component.literal(currentTime))
                .append("\n\n");

        return coverPage;

    }
    
    private static List<Component> createCategoryPages(String categoryKey, Map<String, Integer> categoryStats) {
        List<Component> pages = new ArrayList<>();
        var config = ModConfigManager.getConfig().statsDisplay;
        
        List<Map.Entry<String, Integer>> sortedStats = categoryStats.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null &&
                        (!config.hideZeroStats || entry.getValue() > 0))
                .sorted(config.sortStatsByValue ?
                        (a, b) -> b.getValue().compareTo(a.getValue()) :
                        (a, b) -> a.getKey().compareTo(b.getKey()))
                .limit(config.showOnlyTopStats ?
                        config.topStatsCount :
                        config.maxStatsPerCategory)
                .toList();
        
        if (sortedStats.isEmpty()) {
            MutableComponent emptyPage = createCategoryHeader(categoryKey, false, 1);
            emptyPage.append(Component.translatable("item.minecraft.bundle.empty")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            pages.add(emptyPage);
            return pages;
        }
        
        final int maxLinesPerPage = 13;
        int lineCount = 0;
        int pageNumber = 1;
        MutableComponent currentPage = createCategoryHeader(categoryKey, false, pageNumber);
        
        for (Map.Entry<String, Integer> stat : sortedStats) {
            if (lineCount >= maxLinesPerPage) {
                pages.add(currentPage);
                pageNumber++;
                currentPage = createCategoryHeader(categoryKey, true, pageNumber);
                lineCount = 0;
            }
            
            String formattedValue = StatFormatter.formatStatValue(stat.getKey(), stat.getValue());
            String displayName = StatFormatter.formatStatDisplayName(stat.getKey());
            
            Component statNameText = createStatNameText(displayName);
            
            currentPage.append(statNameText.copy())
                    .append(Component.literal(": "))
                    .append(Component.literal(formattedValue))
                    .append("\n");
            
            lineCount++;
        }
        
        pages.add(currentPage);
        return pages;
    }
    
    private static MutableComponent createCategoryHeader(String categoryKey, boolean isContinuation, int pageNumber) {
        MutableComponent header = Component.empty()
                .append(Component.translatable(categoryKey));
        
        if (isContinuation) {
            header.append(Component.literal("(" + pageNumber + ")")
                    .withStyle(ChatFormatting.GRAY));
        }
        
        header.append("\n")
                .append(Component.literal("━".repeat(20)).withStyle(ChatFormatting.DARK_GRAY))
                .append("\n\n");
        
        return header;
    }
    
    private static Component createEmptyPage() {
        return Component.empty()
                .append(Component.translatable("item.minecraft.bundle.empty")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
    }
    
    private static @NotNull Component createStatNameText(String displayName) {
        return Component.translatable(displayName);
    }
    
    private static ItemStack createBookItemStack(ServerPlayer player, List<Component> pages) {
        ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
        
        WrittenBookContent bookContent = new WrittenBookContent(
                Filterable.passThrough("title"),
                player.getName().getString(),
                0,
                pages.stream()
                        .map(Filterable::passThrough)
                        .collect(Collectors.toList()),
                true
        );
        String fileName = player.getName().getString();
        String createdAt = java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm")
                .format(java.time.LocalDateTime.now());
        int totalPages = pages.size();

        Component hoverText = Component.literal("")
                .append(fileName).withStyle(ChatFormatting.AQUA).append("\n\n")
                .append(Component.translatable("showcase.stats.message.create_date").append(": ").withStyle(ChatFormatting.GRAY).append(createdAt).append("\n"))
                .append(Component.translatable("showcase.stats.message.total_pages").append(": ").withStyle(ChatFormatting.GRAY).append(String.valueOf(totalPages)))
                .append("\n")
                .append(Component.translatable("showcase.message.click_to_view")
                .append("\n\n")
                .append(TextUtils.BADGE)
                .append("\n"));

        bookStack.set(DataComponents.WRITTEN_BOOK_CONTENT, bookContent);
        bookStack.set(DataComponents.CUSTOM_NAME,
                Component.translatable("gui.stats")
                        .append(Component.literal(" - "))
                        .append(PlayerUtils.getSafeDisplayName(player))
                        .setStyle(Style.EMPTY.withHoverEvent(TextEventFactory.showText(hoverText)))
        );
        
        return bookStack;
    }
}
