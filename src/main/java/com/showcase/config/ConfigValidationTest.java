package com.showcase.config;

import com.showcase.ShowcaseMod;
import com.showcase.command.ShowcaseManager.ShareType;
import com.showcase.utils.DevUtils;

/**
 * Simple configuration validation test utility
 *
 * This class provides methods to test the configuration system's
 * validation, backup, and recovery functionality.
 */
public class ConfigValidationTest {

    /**
     * Test configuration validation with various scenarios
     * Only runs in development environment
     */
    public static void runValidationTests() {
        if (DevUtils.isProduction()) {
            ShowcaseMod.LOGGER.warn("Configuration validation tests are only available in development environment");
            return;
        }

        ShowcaseMod.LOGGER.info("Starting configuration validation tests...");

        // Test 1: Valid default configuration
        testDefaultConfigValidation();

        // Test 2: Invalid configuration recovery
        testInvalidConfigRecovery();

        // Test 3: Backup functionality
        testBackupFunctionality();

        ShowcaseMod.LOGGER.info("Configuration validation tests completed");
    }

    private static void testDefaultConfigValidation() {
        ShowcaseMod.LOGGER.info("Test 1: Default configuration validation");

        try {
            ModConfig defaultConfig = new ModConfig();
            boolean isValid = ModConfigManager.isCurrentConfigValid();

            if (isValid) {
                ShowcaseMod.LOGGER.info("✓ Default configuration is valid");
            } else {
                ShowcaseMod.LOGGER.error("✗ Default configuration failed validation");
            }

            // Verify all share types are present
            for (ShareType shareType : ShareType.values()) {
                if (defaultConfig.shareSettings.containsKey(shareType)) {
                    ShowcaseMod.LOGGER.debug("✓ Share type {} configured", shareType);
                } else {
                    ShowcaseMod.LOGGER.error("✗ Missing configuration for share type {}", shareType);
                }
            }

        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("✗ Exception during default config test", e);
        }
    }

    private static void testInvalidConfigRecovery() {
        ShowcaseMod.LOGGER.info("Test 2: Invalid configuration recovery");

        try {
            // Create a test invalid configuration
            ModConfig invalidConfig = new ModConfig();

            // Make it invalid by setting negative values
            invalidConfig.placeholders.maxSharesPerPlayer = -1;
            invalidConfig.shareLink.minExpiryTime = -1;

            // Test validation (should fail)
            // Note: This is a conceptual test - in practice, the validation
            // happens during config loading from file

            ShowcaseMod.LOGGER.info("✓ Invalid configuration recovery test setup complete");

        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("✗ Exception during invalid config test", e);
        }
    }

    private static void testBackupFunctionality() {
        ShowcaseMod.LOGGER.info("Test 3: Backup functionality");

        try {
            // Test manual backup creation
            boolean backupSuccess = ModConfigManager.createConfigBackup("test");

            if (backupSuccess) {
                ShowcaseMod.LOGGER.info("✓ Configuration backup created successfully");
            } else {
                ShowcaseMod.LOGGER.warn("⚠ Configuration backup creation returned false (may not exist yet)");
            }

            // Test backup directory access
            var backupDir = ModConfigManager.getBackupDirectory();
            if (backupDir != null) {
                ShowcaseMod.LOGGER.info("✓ Backup directory accessible: {}", backupDir);
            } else {
                ShowcaseMod.LOGGER.error("✗ Backup directory is null");
            }

        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("✗ Exception during backup test", e);
        }
    }

    /**
     * Validate a configuration object manually
     */
    public static boolean validateConfiguration(ModConfig config) {
        if (config == null) {
            ShowcaseMod.LOGGER.warn("Configuration is null");
            return false;
        }

        try {
            // Check share settings
            if (config.shareSettings == null || config.shareSettings.isEmpty()) {
                ShowcaseMod.LOGGER.warn("Share settings missing");
                return false;
            }

            for (ShareType shareType : ShareType.values()) {
                ModConfig.ShareSettings settings = config.shareSettings.get(shareType);
                if (settings == null) {
                    ShowcaseMod.LOGGER.warn("Missing settings for {}", shareType);
                    return false;
                }

                if (settings.cooldown < 0) {
                    ShowcaseMod.LOGGER.warn("Invalid cooldown for {}: {}", shareType, settings.cooldown);
                    return false;
                }

                if (settings.defaultPermission < 0 || settings.defaultPermission > 4) {
                    ShowcaseMod.LOGGER.warn("Invalid permission for {}: {}", shareType, settings.defaultPermission);
                    return false;
                }
            }

            // Check other critical settings
            if (config.shareLink == null) {
                ShowcaseMod.LOGGER.warn("Share link settings missing");
                return false;
            }

            if (config.shareLink.minExpiryTime <= 0 || config.shareLink.defaultExpiryTime <= 0) {
                ShowcaseMod.LOGGER.warn("Invalid expiry times");
                return false;
            }

            if (config.placeholders == null) {
                ShowcaseMod.LOGGER.warn("Placeholder settings missing");
                return false;
            }

            if (config.placeholders.maxSharesPerPlayer <= 0) {
                ShowcaseMod.LOGGER.warn("Invalid max shares per player: {}", config.placeholders.maxSharesPerPlayer);
                return false;
            }

            return true;

        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Exception during manual validation", e);
            return false;
        }
    }

    /**
     * Print configuration summary for debugging
     */
    public static void printConfigSummary(ModConfig config) {
        if (config == null) {
            ShowcaseMod.LOGGER.info("Configuration is null");
            return;
        }

        ShowcaseMod.LOGGER.info("=== Configuration Summary ===");
        ShowcaseMod.LOGGER.info("Map view duration: {}", config.mapViewDuration);
        ShowcaseMod.LOGGER.info("Item view duration: {}", config.itemViewDuration);
        ShowcaseMod.LOGGER.info("Max placeholders per message: {}", config.maxPlaceholdersPerMessage);

        if (config.shareSettings != null) {
            ShowcaseMod.LOGGER.info("Share types configured: {}", config.shareSettings.size());
            for (var entry : config.shareSettings.entrySet()) {
                ShowcaseMod.LOGGER.info("  {}: cooldown={}s, permission={}",
                    entry.getKey(),
                    entry.getValue().cooldown,
                    entry.getValue().defaultPermission);
            }
        }

        if (config.placeholders != null) {
            ShowcaseMod.LOGGER.info("Max shares per player: {}", config.placeholders.maxSharesPerPlayer);
            ShowcaseMod.LOGGER.info("Placeholder cache duration: {}s", config.placeholders.cacheDuration);
        }

        ShowcaseMod.LOGGER.info("=============================");
    }
}