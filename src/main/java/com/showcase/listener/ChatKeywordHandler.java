package com.showcase.listener;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfig;
import com.showcase.config.ModConfigManager;
import com.showcase.placeholders.Placeholders;
import com.showcase.utils.PermissionChecker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.showcase.utils.PermissionChecker.isOp;
import static com.showcase.utils.Permissions.commands;

public class ChatKeywordHandler {
    private final ModConfig config;
    private final Map<String, ShowcaseManager.ShareType> keywordToTypeMap;
    private final Pattern keywordPattern;

    public static final Set<String> SUPPORTED_PLACEHOLDERS;

    private static final Map<ShowcaseManager.ShareType, String> PLACEHOLDER_CACHE = new EnumMap<>(ShowcaseManager.ShareType.class);
    private static final Map<ShowcaseManager.ShareType, Identifier> TYPE_TO_IDENTIFIER = Map.of(
            ShowcaseManager.ShareType.ITEM, Placeholders.ITEM,
            ShowcaseManager.ShareType.INVENTORY, Placeholders.INVENTORY,
            ShowcaseManager.ShareType.HOTBAR, Placeholders.HOTBAR,
            ShowcaseManager.ShareType.ENDER_CHEST, Placeholders.ENDER_CHEST,
            ShowcaseManager.ShareType.STATS, Placeholders.STATS
    );

    static {
        TYPE_TO_IDENTIFIER.forEach((type, id) ->
                PLACEHOLDER_CACHE.put(type, "%" + id.getNamespace() + ":" + id.getPath() + "%")
        );

        SUPPORTED_PLACEHOLDERS = Set.copyOf(PLACEHOLDER_CACHE.values());
    }

    public ChatKeywordHandler(ModConfig config) {
        this.config = config;
        this.keywordToTypeMap = buildKeywordMap();
        this.keywordPattern = buildKeywordPattern();
    }

    private Map<String, ShowcaseManager.ShareType> buildKeywordMap() {
        Map<String, ShowcaseManager.ShareType> map = new HashMap<>();
        Set<String> duplicates = new HashSet<>();

        config.shareSettings.forEach((type, settings) -> {
            if (settings == null || settings.keywords == null) return;

            for (String keyword : settings.keywords) {
                if (keyword == null || keyword.isBlank()) continue;
                String normalized = keyword.toLowerCase(Locale.ROOT).trim();
                ShowcaseManager.ShareType old = map.putIfAbsent(normalized, type);

                if (old != null && old != type) {
                    duplicates.add(normalized);
                }
            }
        });

        if (!duplicates.isEmpty()) {
            String msg = "Duplicate keywords found in config: " + duplicates;
            ShowcaseMod.LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return map;
    }

    private Pattern buildKeywordPattern() {
        if (keywordToTypeMap.isEmpty()) {
            return Pattern.compile("(?!)"); // 永不匹配
        }

        String joined = keywordToTypeMap.keySet().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Pattern.compile("\\[\\s*(" + joined + ")\\s*]", Pattern.CASE_INSENSITIVE);
    }

    public String processMessage(String originalMessage, ServerPlayerEntity player) {
        if (originalMessage == null || originalMessage.isBlank()) return originalMessage;

        Matcher matcher = keywordPattern.matcher(originalMessage);
        if (!matcher.find()) return originalMessage;

        int maxReplacements = ModConfigManager.getConfig().maxPlaceholdersPerMessage;
        int replacements = 0;
        StringBuilder result = new StringBuilder();
        matcher.reset();

        while (matcher.find()) {
            if (replacements >= maxReplacements && !isOp(player)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            String keyword = matcher.group(1).toLowerCase(Locale.ROOT).trim();
            ShowcaseManager.ShareType shareType = keywordToTypeMap.get(keyword);

            if (shareType == null || !hasPermission(player, shareType)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(getCachedPlaceholder(shareType)));
            replacements++;
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String getCachedPlaceholder(ShowcaseManager.ShareType shareType) {
        String placeholder = PLACEHOLDER_CACHE.get(shareType);
        if (placeholder == null) {
            throw new IllegalArgumentException("No placeholder cached for share type: " + shareType);
        }
        return placeholder;
    }

    public static Set<String> getSupportedPlaceholders() {
        return SUPPORTED_PLACEHOLDERS;
    }

    private boolean hasPermission(ServerPlayerEntity player, ShowcaseManager.ShareType shareType) {
        ModConfig.ShareSettings settings = config.shareSettings.get(shareType);
        return settings != null &&
                PermissionChecker.hasPermission(player, commands(shareType), settings.defaultPermission);
    }

    public Map<ShowcaseManager.ShareType, List<String>> getSupportedKeywords() {
        Map<ShowcaseManager.ShareType, List<String>> result = new EnumMap<>(ShowcaseManager.ShareType.class);
        config.shareSettings.forEach((type, settings) -> {
            List<String> keywords = (settings != null && settings.keywords != null)
                    ? new ArrayList<>(settings.keywords)
                    : Collections.emptyList();
            result.put(type, keywords);
        });
        return result;
    }
}
