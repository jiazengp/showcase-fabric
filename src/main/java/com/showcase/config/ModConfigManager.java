package com.showcase.config;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager.ShareType;
import de.exlll.configlib.YamlConfigurations;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ModConfigManager {
    private static volatile ModConfig CONFIG;
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(ShowcaseMod.MOD_ID);
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("config.yml");
    private static final Path BACKUP_DIR = CONFIG_DIR.resolve("backups");
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static void loadConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.createDirectories(BACKUP_DIR);

            if (!Files.exists(CONFIG_PATH)) {
                // First time setup - create default config
                ShowcaseMod.LOGGER.info("Creating default configuration file...");
                CONFIG = new ModConfig();
                saveConfig();
            } else {
                // Try to load existing config
                CONFIG = loadConfigWithValidation();
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to load configuration", e);
            throw new RuntimeException("Failed to load or create config file", e);
        }
    }

    /**
     * Load configuration with validation and automatic recovery
     */
    private static ModConfig loadConfigWithValidation() throws IOException {
        try {
            ModConfig config = YamlConfigurations.load(CONFIG_PATH, ModConfig.class);

            // Validate the loaded configuration
            if (isConfigValid(config)) {
                ShowcaseMod.LOGGER.info("Configuration loaded successfully");
                return config;
            } else {
                ShowcaseMod.LOGGER.warn("Configuration validation failed, attempting recovery...");
                return handleInvalidConfig(config);
            }
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to parse configuration file: {}", e.getMessage());
            return handleCorruptedConfig(e);
        }
    }

    /**
     * Validate configuration structure and values
     */
    private static boolean isConfigValid(ModConfig config) {
        try {
            // Check if critical fields are present
            if (config == null) {
                ShowcaseMod.LOGGER.warn("Config is null");
                return false;
            }

            if (config.shareSettings == null || config.shareSettings.isEmpty()) {
                ShowcaseMod.LOGGER.warn("Share settings are missing or empty");
                return false;
            }

            // Validate all required share types are present
            for (ShareType shareType : ShareType.values()) {
                ModConfig.ShareSettings settings = config.shareSettings.get(shareType);
                if (settings == null) {
                    ShowcaseMod.LOGGER.warn("Missing settings for share type: {}", shareType);
                    return false;
                }

                // Validate share settings values
                if (settings.cooldown < 0) {
                    ShowcaseMod.LOGGER.warn("Invalid cooldown value for {}: {}", shareType, settings.cooldown);
                    return false;
                }

                if (settings.defaultPermission < 0 || settings.defaultPermission > 4) {
                    ShowcaseMod.LOGGER.warn("Invalid permission level for {}: {}", shareType, settings.defaultPermission);
                    return false;
                }
            }

            // Validate other critical settings
            if (config.shareLink == null) {
                ShowcaseMod.LOGGER.warn("Share link settings are missing");
                return false;
            }

            if (config.shareLink.minExpiryTime <= 0 || config.shareLink.defaultExpiryTime <= 0) {
                ShowcaseMod.LOGGER.warn("Invalid expiry time settings");
                return false;
            }

            if (config.placeholders == null) {
                ShowcaseMod.LOGGER.warn("Placeholder settings are missing");
                return false;
            }

            if (config.placeholders.maxSharesPerPlayer <= 0) {
                ShowcaseMod.LOGGER.warn("Invalid max shares per player: {}", config.placeholders.maxSharesPerPlayer);
                return false;
            }

            return true;
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Error during config validation", e);
            return false;
        }
    }

    /**
     * Handle invalid but parseable configuration
     */
    private static ModConfig handleInvalidConfig(ModConfig invalidConfig) throws IOException {
        // Create backup of invalid config
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
        Path backupPath = BACKUP_DIR.resolve("config_invalid_" + timestamp + ".yml");

        try {
            Files.copy(CONFIG_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
            ShowcaseMod.LOGGER.info("Backed up invalid configuration to: {}", backupPath);
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("Failed to backup invalid configuration", e);
        }

        // Try to merge valid parts with default config
        ModConfig defaultConfig = new ModConfig();
        ModConfig mergedConfig = mergeConfigs(invalidConfig, defaultConfig);

        if (isConfigValid(mergedConfig)) {
            ShowcaseMod.LOGGER.info("Successfully merged configuration with defaults");
            YamlConfigurations.save(CONFIG_PATH, ModConfig.class, mergedConfig);
            return mergedConfig;
        } else {
            // Fall back to complete reset
            return resetToDefault("Failed to merge configuration");
        }
    }

    /**
     * Handle completely corrupted configuration file
     */
    private static ModConfig handleCorruptedConfig(Exception originalException) throws IOException {
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
        Path backupPath = BACKUP_DIR.resolve("config_corrupted_" + timestamp + ".yml");

        try {
            Files.copy(CONFIG_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
            ShowcaseMod.LOGGER.info("Backed up corrupted configuration to: {}", backupPath);
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("Failed to backup corrupted configuration", e);
        }

        return resetToDefault("Configuration file is corrupted: " + originalException.getMessage());
    }

    /**
     * Reset configuration to default values
     */
    private static ModConfig resetToDefault(String reason) throws IOException {
        ShowcaseMod.LOGGER.warn("Resetting configuration to defaults. Reason: {}", reason);

        ModConfig defaultConfig = new ModConfig();
        YamlConfigurations.save(CONFIG_PATH, ModConfig.class, defaultConfig);

        ShowcaseMod.LOGGER.info("Configuration has been reset to defaults and saved");
        return defaultConfig;
    }

    /**
     * Merge two configurations, preferring valid values from source, falling back to defaults
     */
    private static ModConfig mergeConfigs(ModConfig source, ModConfig defaults) {
        ModConfig merged = new ModConfig();

        try {
            // Merge basic settings
            merged.mapViewDuration = source.mapViewDuration >= -1 ? source.mapViewDuration : defaults.mapViewDuration;
            merged.itemViewDuration = source.itemViewDuration >= -1 ? source.itemViewDuration : defaults.itemViewDuration;
            merged.maxPlaceholdersPerMessage = source.maxPlaceholdersPerMessage > 0 ? source.maxPlaceholdersPerMessage : defaults.maxPlaceholdersPerMessage;

            // Merge share settings
            if (source.shareSettings != null && !source.shareSettings.isEmpty()) {
                for (ShareType shareType : ShareType.values()) {
                    ModConfig.ShareSettings sourceSettings = source.shareSettings.get(shareType);
                    ModConfig.ShareSettings defaultSettings = defaults.shareSettings.get(shareType);

                    if (sourceSettings != null && isShareSettingsValid(sourceSettings)) {
                        merged.shareSettings.put(shareType, sourceSettings);
                    } else {
                        merged.shareSettings.put(shareType, defaultSettings);
                        ShowcaseMod.LOGGER.info("Using default settings for share type: {}", shareType);
                    }
                }
            } else {
                merged.shareSettings = defaults.shareSettings;
            }

            // Merge other settings with validation
            merged.shareLink = (source.shareLink != null && isShareLinkSettingsValid(source.shareLink))
                ? source.shareLink : defaults.shareLink;

            merged.statsDisplay = (source.statsDisplay != null) ? source.statsDisplay : defaults.statsDisplay;
            merged.itemIcons = (source.itemIcons != null) ? source.itemIcons : defaults.itemIcons;

            merged.placeholders = (source.placeholders != null && isPlaceholderSettingsValid(source.placeholders))
                ? source.placeholders : defaults.placeholders;

        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Error during config merge, using defaults", e);
            return defaults;
        }

        return merged;
    }

    private static boolean isShareSettingsValid(ModConfig.ShareSettings settings) {
        return settings.cooldown >= 0 &&
               settings.defaultPermission >= 0 && settings.defaultPermission <= 4 &&
               settings.keywords != null;
    }

    private static boolean isShareLinkSettingsValid(ModConfig.ShareLinkSettings settings) {
        return settings.minExpiryTime > 0 && settings.defaultExpiryTime > 0;
    }

    private static boolean isPlaceholderSettingsValid(ModConfig.PlaceholderSettings settings) {
        return settings.maxSharesPerPlayer > 0 &&
               settings.cacheDuration >= 0 &&
               settings.statisticsUpdateInterval > 0;
    }

    public static void reloadConfig() {
        try {
            // Create backup before reloading
            createConfigBackup("reload");
            loadConfig();
            ShowcaseMod.LOGGER.info("Configuration reloaded successfully");
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to reload configuration", e);
            throw new RuntimeException("Configuration reload failed", e);
        }
    }

    public static void saveConfig() {
        if (CONFIG != null) {
            try {
                // Validate before saving
                if (isConfigValid(CONFIG)) {
                    YamlConfigurations.save(CONFIG_PATH, ModConfig.class, CONFIG);
                    ShowcaseMod.LOGGER.debug("Configuration saved successfully");
                } else {
                    ShowcaseMod.LOGGER.error("Cannot save invalid configuration");
                    throw new IllegalStateException("Configuration validation failed during save");
                }
            } catch (Exception e) {
                ShowcaseMod.LOGGER.error("Failed to save configuration", e);
                throw new RuntimeException("Failed to save configuration", e);
            }
        }
    }

    /**
     * Create a timestamped backup of the current configuration
     */
    public static boolean createConfigBackup(String reason) {
        if (!Files.exists(CONFIG_PATH)) {
            return false;
        }

        try {
            String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
            String filename = String.format("config_%s_%s.yml", reason, timestamp);
            Path backupPath = BACKUP_DIR.resolve(filename);

            Files.copy(CONFIG_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
            ShowcaseMod.LOGGER.info("Configuration backed up to: {}", backupPath);

            // Clean up old backups (keep last 10)
            cleanupOldBackups();
            return true;
        } catch (IOException e) {
            ShowcaseMod.LOGGER.error("Failed to create configuration backup", e);
            return false;
        }
    }

    /**
     * Clean up old backup files, keeping only the most recent ones
     */
    private static void cleanupOldBackups() {
        try {
            Files.list(BACKUP_DIR)
                .filter(path -> path.toString().endsWith(".yml"))
                .filter(path -> path.getFileName().toString().startsWith("config_"))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .skip(10) // Keep 10 most recent backups
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        ShowcaseMod.LOGGER.debug("Deleted old backup: {}", path.getFileName());
                    } catch (IOException e) {
                        ShowcaseMod.LOGGER.warn("Failed to delete old backup: {}", path.getFileName());
                    }
                });
        } catch (IOException e) {
            ShowcaseMod.LOGGER.warn("Failed to clean up old backups", e);
        }
    }

    /**
     * Reset configuration to defaults with backup
     */
    public static boolean resetConfigToDefaults() {
        try {
            // Create backup before reset
            createConfigBackup("manual_reset");

            ModConfig defaultConfig = new ModConfig();
            YamlConfigurations.save(CONFIG_PATH, ModConfig.class, defaultConfig);
            CONFIG = defaultConfig;

            ShowcaseMod.LOGGER.info("Configuration has been reset to defaults");
            return true;
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to reset configuration to defaults", e);
            return false;
        }
    }

    /**
     * Get configuration validation status
     */
    public static boolean isCurrentConfigValid() {
        return CONFIG != null && isConfigValid(CONFIG);
    }

    /**
     * Get backup directory path for external tools
     */
    public static Path getBackupDirectory() {
        return BACKUP_DIR;
    }

    /**
     * Get config directory path for external tools
     */
    public static Path getConfigDirectory() {
        return CONFIG_DIR;
    }

    /**
     * Get config file path for external tools
     */
    public static Path getConfigPath() {
        return CONFIG_PATH;
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

    public static int getMaxSharesPerPlayer() {
        return getConfig().placeholders.maxSharesPerPlayer;
    }

    public static boolean isPlaceholderExtensionsEnabled() {
        return getConfig().placeholders.enabled;
    }

    public static int getPlaceholderCacheDuration() {
        return getConfig().placeholders.cacheDuration;
    }

    public static boolean isStatisticsTrackingEnabled() {
        return getConfig().placeholders.enableStatisticsTracking;
    }

    public static boolean isServerStatisticsEnabled() {
        return getConfig().placeholders.enableServerStatistics;
    }

    public static boolean isPerformanceMetricsEnabled() {
        return getConfig().placeholders.enablePerformanceMetrics;
    }

    public static int getStatisticsUpdateInterval() {
        return getConfig().placeholders.statisticsUpdateInterval;
    }


    public static boolean isConditionalPlaceholdersEnabled() {
        return getConfig().placeholders.enableConditionalPlaceholders;
    }
}
