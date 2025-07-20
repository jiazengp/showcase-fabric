package com.showcase.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.showcase.ShowcaseMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public record JsonCodecDataStorage<T>(String path, Codec<T> codec) {
    private final static String DIR = "global-mod-data";

    public boolean save(MinecraftServer server, T data) {
        Path globalPath = server.getSavePath(WorldSavePath.ROOT).resolve(DIR);
        Path filePath = globalPath.resolve(this.path + ".json");

        if (data == null) {
            try {
                return Files.deleteIfExists(filePath);
            } catch (IOException e) {
                ShowcaseMod.LOGGER.error("Failed to delete global data file at {}\n{}", filePath, e.fillInStackTrace());
                return false;
            }
        }

        try {
            if (!Files.exists(globalPath)) {
                Files.createDirectories(globalPath);
            }

            var encoded = codec.encodeStart(server.getRegistryManager().getOps(JsonOps.INSTANCE), data)
                    .getOrThrow();

            Files.writeString(filePath, encoded.toString(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("IOException while saving global data for path {}\n{}", this.path, e.fillInStackTrace());
            return false;
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Unexpected error while saving global data for path {}\n{}", this.path, e.fillInStackTrace());
            return false;
        }
    }

    public T load(MinecraftServer server) {
        Path filePath = server.getSavePath(WorldSavePath.ROOT).resolve(DIR).resolve(this.path + ".json");
        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            String jsonString = Files.readString(filePath, StandardCharsets.UTF_8);
            JsonElement element = JsonParser.parseString(jsonString);

            var decoded = codec.decode(server.getRegistryManager().getOps(JsonOps.INSTANCE), element);

            if (decoded.result().isEmpty()) {
                ShowcaseMod.LOGGER.error("Decoding failed or returned empty for global data at path {}", this.path);
                return null;
            }

            return decoded.result().map(Pair::getFirst).orElse(null);
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("IOException while loading global data for path {}", this.path, e);
            return null;
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Unexpected error while loading global data for path {}", this.path, e);
            return null;
        }
    }
}
