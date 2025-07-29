package com.showcase.config;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager.ShareType;
import de.exlll.configlib.YamlConfigurations;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfigManager {
    private static volatile ModConfig CONFIG;
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(ShowcaseMod.MOD_ID);
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("config.yml");

    public static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_DIR);
                CONFIG = new ModConfig();
                saveConfig();
            } else {
                CONFIG = YamlConfigurations.load(CONFIG_PATH, ModConfig.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load or create config file", e);
        }
    }

    public static void reloadConfig() {
        loadConfig();
    }

    public static void saveConfig() {
        if (CONFIG != null) {
            YamlConfigurations.save(CONFIG_PATH, ModConfig.class, CONFIG);
        }
    }

    public static ModConfig getConfig() {
        if (CONFIG == null) {
            loadConfig();
        }
        return CONFIG;
    }

    public static ModConfig.ShareSettings getShareSettings(ShareType type) {
        return getConfig().shareSettings.get(type);
    }

    public static int getShareLinkMinExpiry() {
        return getConfig().shareLink.minExpiryTime;
    }

    public static int getShareLinkDefaultExpiry() {
        return getConfig().shareLink.defaultExpiryTime;
    }
}
