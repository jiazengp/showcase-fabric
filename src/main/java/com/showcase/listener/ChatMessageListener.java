package com.showcase.listener;

import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.showcase.listener.ChatKeywordHandler.getSupportedPlaceholders;
import static com.showcase.utils.permissions.PermissionChecker.isOp;

public class ChatMessageListener {
    private static ChatKeywordHandler keywordHandler;
    private static final NodeParser CHAT_PARSER = NodeParser.builder()
            .globalPlaceholders()
            .requireSafe()
            .build();

    public static void registerChatHandler() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(ChatMessageListener::messageHandler);
    }

    public static void loadConfig() {
        keywordHandler = new ChatKeywordHandler(ModConfigManager.getConfig());
    }

    private static boolean messageHandler(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        if (keywordHandler == null) {
            ShowcaseMod.LOGGER.warn("ChatKeywordHandler not initialized; skipping placeholder processing.");
            return true;
        }
        if (!PermissionChecker.hasPermission(sender, Permissions.Chat.PLACEHOLDER, 1)) return true;
        if (message.getContent().getString().length() >= 100 && !isOp(sender)) return true;

        String originalText = message.getContent().getString();
        String processedText = keywordHandler.processMessage(originalText, sender);
        ShowcaseMod.LOGGER.debug("Original: '{}', Processed: '{}'", originalText, processedText);

        if (containsPlaceholders(processedText)) {
            Text parsedMessage;
            try {
                parsedMessage = CHAT_PARSER.parseText(
                        processedText,
                        PlaceholderContext.of(sender).asParserContext()
                );
            } catch (Exception e) {
                ShowcaseMod.LOGGER.error("Failed to parse chat message: {}", processedText, e);
                return true;
            }

            if (sender.getServer() == null) return true;

            sender.getServer().getPlayerManager().broadcast(
                    Text.translatable("chat.type.text", sender.getDisplayName(), parsedMessage),
                    false
            );

            return false;
        }

        return true;
    }

    private static boolean containsPlaceholders(String text) {
        return text != null && !text.isEmpty() &&
                getSupportedPlaceholders().stream().anyMatch(text::contains);
    }
}