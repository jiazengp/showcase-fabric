package com.showcase.listener;

import com.showcase.utils.PermissionChecker;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.Objects;

import static com.showcase.placeholders.Placeholders.containsPlaceholders;

public class ChatMessageListener {
    private static final NodeParser CHAT_PARSER = NodeParser.builder()
            .globalPlaceholders()
            .quickText()
            .requireSafe()
            .build();

    public static void registerChatHandler() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(ChatMessageListener::messageHandler);
    }

    private static boolean messageHandler(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        if (!PermissionChecker.hasPermission(sender, "chat.placeholder", 1)) return true;

        String originalText = message.getContent().getString();

        if (containsPlaceholders(originalText)) {
            Text parsedMessage = CHAT_PARSER.parseText(
                    originalText,
                    PlaceholderContext.of(sender).asParserContext()
            );

            Objects.requireNonNull(sender.getServer()).getPlayerManager().broadcast(
                    Text.translatable("chat.type.text", sender.getDisplayName(), parsedMessage),
                    false
            );

            return false;
        }

        return true;
    }

}