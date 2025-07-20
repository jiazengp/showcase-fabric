package com.showcase.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.showcase.utils.TextUtils.getSafeDisplayName;

public class MessageUtils {
    public static final Pattern PREDEFINED_PLACEHOLDER_PATTERN = Pattern.compile(
            "(?<!((?<!(\\\\))\\\\))\\$\\{(?<id>[^}]+)}"
    );

    public static Text formatPlayerMessage(String inputText, ServerPlayerEntity player, Text itemName) {
        Map<String, Text> placeholders = Map.of(
                "sourcePlayer", getSafeDisplayName(player),
                "targetPlayer",  getSafeDisplayName(player),
                "itemName", itemName
        );

        return format(inputText, placeholders);
    }

    public static Text formatMessageWithPlayerTarget(String inputText, ServerPlayerEntity sourcePlayer, ServerPlayerEntity targetPlayer, Text itemName) {
        Map<String, Text> placeholders = Map.of(
                "sourcePlayer", getSafeDisplayName(sourcePlayer),
                "targetPlayer", getSafeDisplayName(targetPlayer),
                "itemName", itemName
        );

        return format(inputText, placeholders);
    }

    public static Text format(String template, Map<String, Text> replacements) {
        Matcher matcher = PREDEFINED_PLACEHOLDER_PATTERN.matcher(template);
        Text result = Text.literal("");
        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if (start > lastEnd) {
                String plainText = unescape(template.substring(lastEnd, start));
                result = result.copy().append(Text.literal(plainText));
            }

            String id = matcher.group("id");
            Text replacement = replacements.getOrDefault(id, Text.literal(matcher.group(0))); // 保留原格式
            result = result.copy().append(replacement);

            lastEnd = end;
        }

        if (lastEnd < template.length()) {
            String tail = unescape(template.substring(lastEnd));
            result = result.copy().append(Text.literal(tail));
        }

        return result;
    }

    private static String unescape(String input) {
        return input.replaceAll("\\\\(\\$\\{)", "\\${");
    }
}
