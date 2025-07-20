package com.showcase.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.showcase.ShowcaseMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.*;

public class ModConfig {
    public int shareLinkExpiryTime = 300;
    public int shareLinkMinimumExpiryTime = 60;
    public int shareCommandCooldown = 10;
    public int containerListeningDuration = 10;

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(ShowcaseMod.MOD_ID);
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    public static ModConfig load() {
        try {
            // Load the main config
            ModConfig config;
            if (Files.exists(CONFIG_FILE)) {
                config = GSON.fromJson(Files.readString(CONFIG_FILE), ModConfig.class);
            } else {
                config = new ModConfig();
            }

            config.saveConfig();

            return config;
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("Failed to load config", e);
            ModConfig config = new ModConfig();
            config.saveConfig();
            return config;
        }
    }

    public void saveConfig() {
        try {
            JsonObject configJson = new JsonObject();
            configJson.addProperty("shareLinkExpiryTime", shareLinkExpiryTime);
            configJson.addProperty("shareCommandCooldown", shareCommandCooldown);
            configJson.addProperty("shareLinkMinimumExpiryTime", shareLinkMinimumExpiryTime);
            configJson.addProperty("containerListeningDuration", containerListeningDuration);

            Files.writeString(CONFIG_FILE, GSON.toJson(configJson));
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("Failed to save config", e);
        }
    }
}