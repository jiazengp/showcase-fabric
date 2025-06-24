package com.showcase.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.showcase.ShowcaseMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Objects;

public class ModConfig {
    public int shareLinkExpiryTime = 300;
    public int shareCommandCooldown = 10;
    public int containerListeningDuration = 10;
    public String locale = "en_us";

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(ShowcaseMod.MOD_ID);
    private static final Path LOCALES_DIR = CONFIG_DIR.resolve("locales");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final String DEFAULT_LOCALE = "en_us";
    public Messages messages;

    public static class Messages {
        public String cooldown = "§cYou need to wait %d seconds before sharing again";
        public String noItem = "§cYou must hold an item to share it!";

        // Sharing messages
        public String itemShared = "§e${sourcePlayer} I shared my ${itemName}";
        public String inventoryShared = "§e${sourcePlayer} I shared my ${itemName}";
        public String otherInventoryShared = "§e${sourcePlayer} I shared ${targetPlayer}'s ${itemName}";
        public String enderChestShared = "§e${sourcePlayer} I shared my ${itemName}";
        public String otherEnderChestShared = "§e${sourcePlayer} I shared ${targetPlayer}'s ${itemName}";
        public String hotbarShared = "§e${sourcePlayer} I shared my ${itemName}";
        public String otherHotbarShared = "§e${sourcePlayer} I shared ${targetPlayer}'s ${itemName}";
        public String containerShared = "§e${sourcePlayer} I shared my ${itemName}";

        // Private share messages
        public String privateShareTip = "§aYou shared your ${itemName} with ${targetPlayer}";

        // Container share messages
        public String shareContainerTip = "§ePlease open the container you want to share within %d seconds";
        public String shareContainerExpiryNotice = "§cYou didn't open any container, sharing failed";

        // Link messages
        public String expiryNotice = "§7\n(This link will expire in §c%d§7 minutes)";
        public String clickToView = "§bClick to view";
        public String invalidOrExpiredLinkTips = "§cThis share link is invalid or has expired";

        // Window titles
        public String inventoryTitle = "§6${sourcePlayer}'s ${itemName}";
        public String enderChestTitle = "§6${sourcePlayer}'s ${itemName}";
        public String hotBarTitle = "§6${sourcePlayer}'s ${itemName}";
        public String itemTitle = "§6${sourcePlayer}'s ${itemName}";
        public String containerTitle = "§6${sourcePlayer}'s container";

        // Default broadcast messages
        public String shareInventoryMessagesByDefault = "§d${sourcePlayer} showed you their ${itemName}";
        public String shareEnderChestMessagesByDefault = "§d${sourcePlayer} showed you their ${itemName}";
        public String shareHotbarMessagesByDefault = "§d${sourcePlayer} showed you their ${itemName}";
        public String shareItemMessagesByDefault = "§d${sourcePlayer} showed you their ${itemName}";
        public String shareContainerByDefault = "§d${sourcePlayer} showed you their ${itemName}";
    }

    public static ModConfig load() {
        try {
            // Create directories if they don't exist
            Files.createDirectories(LOCALES_DIR);

            // Load the main config
            ModConfig config;
            if (Files.exists(CONFIG_FILE)) {
                config = GSON.fromJson(Files.readString(CONFIG_FILE), ModConfig.class);
            } else {
                config = new ModConfig();
            }

            copyDefaultLocaleFiles();
            config.loadAndMergeMessages();
            config.saveConfig();

            return config;
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("Failed to load config", e);
            ModConfig config = new ModConfig();
            config.saveConfig();
            return config;
        }
    }

    private void loadAndMergeMessages() throws IOException {
        Messages defaultMessages = loadDefaultMessages();
        Messages localeMessages = loadLocaleMessages(locale);

        this.messages = mergeMessages(defaultMessages, localeMessages);

        // Save the default locale file to the config folder for reference
        saveDefaultLocaleFile();
    }

    private Messages loadDefaultMessages() {
        try {
            InputStream inputStream = ModConfig.class.getClassLoader()
                    .getResourceAsStream("assets/showcase/locales/" + DEFAULT_LOCALE + ".json");
            if (inputStream != null) {
                return GSON.fromJson(new InputStreamReader(inputStream), Messages.class);
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to load default locale messages", e);
        }
        return new Messages(); // Fallback to hardcoded defaults
    }

    private Messages loadLocaleMessages(String locale) {
        Path localeFile = LOCALES_DIR.resolve(locale + ".json");
        if (Files.exists(localeFile)) {
            try {
                return GSON.fromJson(Files.readString(localeFile), Messages.class);
            } catch (Exception e) {
                ShowcaseMod.LOGGER.error("Failed to load locale file: {}", localeFile, e);
            }
        }
        return null;
    }

    private Messages mergeMessages(Messages defaults, Messages overrides) {
        if (overrides == null) {
            return defaults;
        }

        Messages result = new Messages();

        // Use reflection to copy all fields
        for (java.lang.reflect.Field field : Messages.class.getDeclaredFields()) {
            try {
                Object overrideValue = field.get(overrides);
                Object defaultValue = field.get(defaults);
                field.set(result, overrideValue != null ? overrideValue : defaultValue);
            } catch (IllegalAccessException e) {
                ShowcaseMod.LOGGER.error("Failed to merge message field: {}", field.getName(), e);
            }
        }

        return result;
    }

    private void saveDefaultLocaleFile() throws IOException {
        Path defaultLocaleFile = LOCALES_DIR.resolve("en_us.json");
        if (!Files.exists(defaultLocaleFile)) {
            JsonObject jsonObject = new JsonObject();
            Messages defaultMessages = new Messages();

            for (java.lang.reflect.Field field : Messages.class.getDeclaredFields()) {
                try {
                    jsonObject.addProperty(field.getName(), (String) field.get(defaultMessages));
                } catch (IllegalAccessException e) {
                    ShowcaseMod.LOGGER.error("Failed to save default message field: {}", field.getName(), e);
                }
            }

            Files.writeString(defaultLocaleFile, GSON.toJson(jsonObject));
        }
    }

    private static void copyDefaultLocaleFiles() throws IOException {
        try (InputStream in = ModConfig.class.getResourceAsStream("/assets/showcase/locales");
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in)))) {
            String file;
            while ((file = reader.readLine()) != null) {
                if (file.endsWith(".json")) {
                    Path target = LOCALES_DIR.resolve(file);
                    if (!Files.exists(target)) {
                        try (InputStream fileIn = ModConfig.class.getResourceAsStream(
                                "/assets/showcase/locales/" + file)) {
                            if (fileIn != null) {
                                Files.copy(fileIn, target);
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
           ShowcaseMod.LOGGER.error("Copy locale files fail", e);
        }
    }

    public void saveConfig() {
        try {
            JsonObject configJson = new JsonObject();
            configJson.addProperty("shareLinkExpiryTime", shareLinkExpiryTime);
            configJson.addProperty("shareCommandCooldown", shareCommandCooldown);
            configJson.addProperty("containerListeningDuration", containerListeningDuration);
            configJson.addProperty("locale", locale);

            Files.writeString(CONFIG_FILE, GSON.toJson(configJson));
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("Failed to save config", e);
        }
    }
}