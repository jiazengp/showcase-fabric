package com.showcase.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.showcase.ShowcaseMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.UserCache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerUtils {
    public static Set<UUID> getReceiverUuids(Collection<ServerPlayerEntity> receivers) {
        if (receivers  == null) return null;
        return receivers.stream().map(ServerPlayerEntity::getUuid).collect(Collectors.toSet());
    }

    public static MutableText getSafeDisplayName(ServerPlayerEntity player) {
        if (player == null) return Text.translatable("argument.entity.notfound.player");
        Text displayName = Text.translatable("chat.type.text", player.getDisplayName(), "");
        return (MutableText) Objects.requireNonNullElseGet(displayName, player::getName);
    }

    public static MutableText getSafeDisplayName(MinecraftServer server, UUID uuid) {
        if (server == null) return Text.translatable("argument.entity.notfound.player");
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
        if (player != null) return getSafeDisplayName(player);
        Optional<String> playerName = getPlayerNameFromUuid(server, uuid);
        return playerName.map(s -> Text.translatable("chat.type.text", s, "")).orElseGet(() -> Text.translatable("argument.entity.notfound.player"));
    }

    public static Optional<String> getPlayerNameFromCache(MinecraftServer server, UUID uuid) {
        try {
            UserCache userCache = server.getUserCache();
            if (userCache != null) {
                Optional<GameProfile> profile = userCache.getByUuid(uuid);
                if (profile.isPresent()) {
                    return Optional.of(profile.get().getName());
                }
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Error getting player name from cache: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<String> getPlayerNameFromUserCacheFile(MinecraftServer server, UUID uuid) {
        try {
            Path worldPath = server.getSavePath(net.minecraft.util.WorldSavePath.ROOT);
            Path userCachePath = worldPath.resolve("usercache.json");

            if (Files.exists(userCachePath)) {
                String content = Files.readString(userCachePath);
                JsonObject[] entries = JsonParser.parseString(content).getAsJsonArray()
                        .asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .toArray(JsonObject[]::new);

                for (JsonObject entry : entries) {
                    if (entry.has("uuid") && entry.has("name")) {
                        String entryUuid = entry.get("uuid").getAsString();
                        if (uuid.toString().equals(entryUuid)) {
                            return Optional.of(entry.get("name").getAsString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Error reading usercache.json: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<String> getPlayerNameFromUuid(MinecraftServer server, UUID uuid) {
        Optional<String> name = getPlayerNameFromCache(server, uuid);
        if (name.isPresent()) {
            return name;
        }

        name = getPlayerNameFromUserCacheFile(server, uuid);

        if (name.isEmpty()) {
            String shortUuid = uuid.toString().substring(0, 8);
            return ("Player-" + shortUuid).describeConstable();
        }
        return name;
    }
}
