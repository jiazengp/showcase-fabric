package com.showcase.utils.stats;

import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.PlayerUtils;
import com.showcase.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatUtils {
    private static final StatCollector statCollector = new StatCollector();
    
    public static Map<String, Integer> getPlayerStats(ServerPlayerEntity player) {
        return statCollector.getPlayerStats(player);
    }
    
    public static Map<String, Map<String, Integer>> categorizeStats(Map<String, Integer> stats) {
        return StatCategories.categorizeStats(stats);
    }
    
    public static ItemStack createStatsBook(ServerPlayerEntity player) {
        Map<String, Integer> playerStats = getPlayerStats(player);
        Map<String, Map<String, Integer>> categorizedStats = categorizeStats(playerStats);
        
        List<Text> pages = new ArrayList<>();
        pages.add(createTitlePage(player));
        
        categorizedStats.forEach((categoryKey, categoryStats) -> 
            pages.addAll(createCategoryPages(categoryKey, categoryStats)));
        
        if (categorizedStats.isEmpty()) {
            pages.add(createEmptyPage());
        }
        
        return createBookItemStack(player, pages);
    }
    
    private static Text createTitlePage(ServerPlayerEntity player) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm");
        String currentTime = now.format(formatter);
        
        return Text.empty()
                .append(Text.translatable("gui.stats")
                        .formatted(Formatting.BOLD))
                .append("\n\n")
                .append(Text.literal("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                        .formatted(Formatting.DARK_GRAY))
                .append("\n\n")

                .append(Text.translatable("gui.abuseReport.type.name"))
                .append(": ")
                .append(player.getName())
                .append("\n\n")

                .append(Text.translatable("showcase.stats.message.create_date"))
                .append(": ")
                .append(Text.literal(currentTime))
                .append("\n\n")

                .append(Text.literal("UUID: "))
                .append(Text.literal(player.getUuidAsString()))
                .append("\n\n");

    }
    
    private static List<Text> createCategoryPages(String categoryKey, Map<String, Integer> categoryStats) {
        List<Text> pages = new ArrayList<>();
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
            MutableText emptyPage = createCategoryHeader(categoryKey, false, 1);
            emptyPage.append(Text.translatable("item.minecraft.bundle.empty")
                    .formatted(Formatting.GRAY, Formatting.ITALIC));
            pages.add(emptyPage);
            return pages;
        }
        
        final int maxLinesPerPage = 13;
        int lineCount = 0;
        int pageNumber = 1;
        MutableText currentPage = createCategoryHeader(categoryKey, false, pageNumber);
        
        for (Map.Entry<String, Integer> stat : sortedStats) {
            if (lineCount >= maxLinesPerPage) {
                pages.add(currentPage);
                pageNumber++;
                currentPage = createCategoryHeader(categoryKey, true, pageNumber);
                lineCount = 0;
            }
            
            String formattedValue = StatFormatter.formatStatValue(stat.getKey(), stat.getValue());
            String displayName = StatFormatter.formatStatDisplayName(stat.getKey());
            
            Text statNameText = createStatNameText(displayName);
            
            currentPage.append(statNameText.copy())
                    .append(Text.literal(": "))
                    .append(Text.literal(formattedValue))
                    .append("\n");
            
            lineCount++;
        }
        
        pages.add(currentPage);
        return pages;
    }
    
    private static MutableText createCategoryHeader(String categoryKey, boolean isContinuation, int pageNumber) {
        MutableText header = Text.empty()
                .append(Text.translatable(categoryKey));
        
        if (isContinuation) {
            header.append(Text.literal("(" + pageNumber + ")")
                    .formatted(Formatting.GRAY));
        }
        
        header.append("\n")
                .append(Text.literal("━".repeat(20)).formatted(Formatting.DARK_GRAY))
                .append("\n\n");
        
        return header;
    }
    
    private static Text createEmptyPage() {
        return Text.empty()
                .append(Text.translatable("item.minecraft.bundle.empty")
                        .formatted(Formatting.RED, Formatting.BOLD));
    }
    
    private static @NotNull Text createStatNameText(String displayName) {
        return Text.translatable(displayName);
    }
    
    private static ItemStack createBookItemStack(ServerPlayerEntity player, List<Text> pages) {
        ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
        
        WrittenBookContentComponent bookContent = new WrittenBookContentComponent(
                RawFilteredPair.of("title"),
                player.getName().getString(),
                0,
                pages.stream()
                        .map(RawFilteredPair::of)
                        .collect(Collectors.toList()),
                true
        );
        String fileName = player.getGameProfile().getName();
        String createdAt = java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm")
                .format(java.time.LocalDateTime.now());
        int totalPages = pages.size();

        Text hoverText = Text.literal("")
                .append(fileName).formatted(Formatting.AQUA).append("\n\n")
                .append(Text.translatable("showcase.stats.message.create_date").append(": ").formatted(Formatting.GRAY).append(createdAt).append("\n"))
                .append(Text.translatable("showcase.stats.message.total_pages").append(": ").formatted(Formatting.GRAY).append(String.valueOf(totalPages)))
                .append("\n")
                .append(Text.translatable("showcase.message.click_to_view")
                .append("\n\n")
                .append(TextUtils.BADGE)
                .append("\n"));

        bookStack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, bookContent);
        bookStack.set(DataComponentTypes.CUSTOM_NAME,
                Text.translatable("gui.stats")
                        .append(Text.literal(" - "))
                        .append(PlayerUtils.getSafeDisplayName(player))
                        .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(hoverText)))
        );
        
        return bookStack;
    }
}