package com.showcase.utils;

import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfigManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ResourcePackChecker {
    private static boolean hasWarned = false;
    
    public static void checkResourcePackConfiguration(MinecraftServer server) {
        // Only check if icons are enabled
        if (!ModConfigManager.getConfig().itemIcons.enabled) {
            return;
        }
        
        // Only warn once per server session
        if (hasWarned) {
            return;
        }
        
        // Only works with dedicated servers
        if (!(server instanceof DedicatedServer dedicated)) {
            ShowcaseMod.LOGGER.debug("Not a dedicated server, skipping resource pack check");
            return;
        }
        
        try {
            ServerPropertiesHandler props = dedicated.getProperties();
            if (props.serverResourcePackProperties.isEmpty()) {
                warnAboutMissingResourcePack();
                hasWarned = true;
                return;
            }

            String resourcePackUrl = props.serverResourcePackProperties.get().url();
            String resourcePackSha1 = props.serverResourcePackProperties.get().hash();
            boolean requireResourcePack = props.serverResourcePackProperties.get().isRequired();
            
            if (resourcePackUrl == null || resourcePackUrl.trim().isEmpty()) {
                warnAboutMissingResourcePack();
                hasWarned = true;
            } else {
                ShowcaseMod.LOGGER.info("Resource pack detected: {}", resourcePackUrl);
                if (resourcePackSha1 == null || resourcePackSha1.trim().isEmpty()) {
                    ShowcaseMod.LOGGER.warn("Resource pack URL is set but SHA1 hash is missing. Consider adding resource-pack-sha1 for better security");
                }
                if (!requireResourcePack) {
                    ShowcaseMod.LOGGER.warn("Resource pack is configured but not required. Consider setting require-resource-pack=true for better icon display experience");
                }
            }
            
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to check resource pack configuration: {}", e.getMessage());
        }
    }
    
    private static void warnAboutMissingResourcePack() {
        ShowcaseMod.LOGGER.warn("=".repeat(60));
        ShowcaseMod.LOGGER.warn("SHOWCASE ICON FEATURE WARNING");
        ShowcaseMod.LOGGER.warn("=".repeat(60));
        ShowcaseMod.LOGGER.warn("Item icons are enabled but no resource pack is configured in server.properties!");
        ShowcaseMod.LOGGER.warn("Players will see translation keys instead of actual icons.");
        ShowcaseMod.LOGGER.warn("");
        ShowcaseMod.LOGGER.warn("To fix this, add the following lines to your server.properties:");
        ShowcaseMod.LOGGER.warn("  resource-pack=<your-resource-pack-url>");
        ShowcaseMod.LOGGER.warn("  resource-pack-sha1=<resource-pack-sha1-hash>");
        ShowcaseMod.LOGGER.warn("  require-resource-pack=true");
        ShowcaseMod.LOGGER.warn("");
        ShowcaseMod.LOGGER.warn("Alternatively, disable icon feature in config:");
        ShowcaseMod.LOGGER.warn("  itemIcons.enabled = false");
        ShowcaseMod.LOGGER.warn("=".repeat(60));
    }
    
    public static void sendResourcePackWarningToPlayer(ServerPlayerEntity player) {
        if (!ModConfigManager.getConfig().itemIcons.enabled) {
            return;
        }
        
        // Only send warning to ops and only if no resource pack is configured
        if (!player.hasPermissionLevel(2)) {
            return;
        }
        
        MinecraftServer server = player.getServer();
        if (server instanceof DedicatedServer dedicated && dedicated.getProperties().serverResourcePackProperties.isPresent()) {
            String resourcePackUrl = dedicated.getProperties().serverResourcePackProperties.get().url();
            if (resourcePackUrl == null || resourcePackUrl.trim().isEmpty()) {
                Text warningMessage = Text.literal("")
                        .append(Text.literal("[" + ShowcaseMod.MOD_ID.toUpperCase() + "] ").formatted(Formatting.GOLD, Formatting.BOLD))
                        .append(Text.literal("Warning: Item icons are enabled but no resource pack is configured! ")
                                .formatted(Formatting.YELLOW))
                        .append(Text.literal("Players will see translation keys instead of icons. ")
                                .formatted(Formatting.WHITE))
                        .append(Text.literal("Check server console for setup instructions.")
                                .formatted(Formatting.GRAY));
                
                player.sendMessage(warningMessage, false);
            }
        }
    }
    
    public static void reset() {
        hasWarned = false;
    }
}