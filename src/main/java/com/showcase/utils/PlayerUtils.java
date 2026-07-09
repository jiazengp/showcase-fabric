package com.showcase.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.showcase.ShowcaseMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerUtils {
    public static Set<UUID> getReceiverUuids(Collection<ServerPlayer> receivers) {
        if (receivers  == null) return null;
        return receivers.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet());
    }

    public static MutableComponent getSafeDisplayName(ServerPlayer player) {
        if (player == null) return Component.translatable("argument.entity.notfound.player");
        Component displayName = Component.translatable("chat.type.text", player.getDisplayName(), "");
        return (MutableComponent) Objects.requireNonNullElseGet(displayName, player::getName);
    }

    public static MutableComponent getSafeDisplayName(MinecraftServer server, UUID uuid) {
        if (server == null) return Component.translatable("argument.entity.notfound.player");
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null) return getSafeDisplayName(player);
        Optional<String> playerName = getPlayerNameFromUuid(server, uuid);
        return playerName.map(s -> Component.translatable("chat.type.text", s, "")).orElseGet(() -> Component.translatable("argument.entity.notfound.player"));
    }

    public static Optional<String> getPlayerNameFromCache(MinecraftServer server, UUID uuid) {
        try {
            var profileCache = server.services().nameToIdCache();
            var profileEntry = profileCache.get(uuid);
            if (profileEntry.isPresent()) {
                return Optional.ofNullable(profileEntry.get().name());
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Error getting player name from cache: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<String> getPlayerNameFromUserCacheFile(MinecraftServer server, UUID uuid) {
        try {
            Path worldPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
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
