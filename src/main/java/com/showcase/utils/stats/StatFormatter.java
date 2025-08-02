package com.showcase.utils.stats;

import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfigManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class StatFormatter {
    
    public static String formatStatValue(String statKey, int value) {
        if (isTimeRelated(statKey)) {
            return formatTime(value);
        } else if (isDistanceRelated(statKey)) {
            return formatDistance(value);
        } else if (isDamageRelated(statKey)) {
            return formatDamage(value);
        } else {
            return String.valueOf(value);
        }
    }
    
    public static String formatStatDisplayName(String statKey) {
        if (statKey.startsWith("stat.minecraft.use_item.")) {
            return getItemDisplayName(extractId(statKey, "stat.minecraft.use_item."));
        } else if (statKey.startsWith("stat.minecraft.craft_item.")) {
            return getItemDisplayName(extractId(statKey, "stat.minecraft.craft_item."));
        } else if (statKey.startsWith("stat.minecraft.break_item.")) {
            return getItemDisplayName(extractId(statKey, "stat.minecraft.break_item."));
        } else if (statKey.startsWith("stat.minecraft.pickup.")) {
            return getItemDisplayName(extractId(statKey, "stat.minecraft.pickup."));
        } else if (statKey.startsWith("stat.minecraft.drop.")) {
            return getItemDisplayName(extractId(statKey, "stat.minecraft.drop."));
        } else if (statKey.startsWith("stat.minecraft.mine_block.")) {
            return getBlockDisplayName(extractId(statKey, "stat.minecraft.mine_block."));
        } else if (statKey.startsWith("stat.minecraft.kill_entity.")) {
            return getEntityDisplayName(extractId(statKey, "stat.minecraft.kill_entity."));
        } else if (statKey.startsWith("stat.minecraft.entity_killed_by.")) {
            return getEntityDisplayName(extractId(statKey, "stat.minecraft.entity_killed_by."));
        } else {
            return statKey;
        }
    }
    
    private static boolean isTimeRelated(String statKey) {
        return statKey.contains("time") || statKey.equals("stat.minecraft.sneak_time");
    }
    
    private static boolean isDistanceRelated(String statKey) {
        return statKey.contains("_cm");
    }
    
    private static boolean isDamageRelated(String statKey) {
        return statKey.contains("damage");
    }
    
    private static String formatTime(int ticks) {
        int seconds = ticks / 20;
        var config = ModConfigManager.getConfig().statsDisplay.timeFormat;
        
        if (seconds < 60) {
            return "< 1m";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            
            if (config.useCompactFormat) {
                return minutes + "m";
            } else {
                return String.format("%d minutes", minutes);
            }
        } else {
            int hours = seconds / 3600;
            int remainingMinutes = (seconds % 3600) / 60;
            
            if (config.useCompactFormat) {
                if (config.hideZeroComponents && remainingMinutes == 0) {
                    return hours + "h";
                } else {
                    return String.format("%dh %dm", hours, remainingMinutes);
                }
            } else {
                return remainingMinutes > 0 ?
                        String.format("%d hours %d minutes", hours, remainingMinutes) :
                        String.format("%d hours", hours);
            }
        }
    }
    
    private static String formatDistance(int cm) {
        var config = ModConfigManager.getConfig().statsDisplay.distanceFormat;
        
        if (!config.autoConvert) {
            return cm + "cm";
        }
        
        if (cm < config.meterThreshold) {
            return cm + "cm";
        } else if (cm < config.kilometerThreshold) {
            double meters = cm / 100.0;
            return formatWithPrecision(config.meterDecimalPlaces, meters) + "m";
        } else {
            double kilometers = cm / 100000.0;
            return formatWithPrecision(config.kilometerDecimalPlaces, kilometers) + "km";
        }
    }
    
    private static String formatDamage(int damage) {
        var config = ModConfigManager.getConfig().statsDisplay.damageFormat;
        
        if (config.showAsHearts) {
            double hearts = damage / 10.0;
            String heartStr = String.format("%." + config.heartDecimalPlaces + "f♥", hearts);
            
            if (config.showBothFormats) {
                return String.format("%s (%d)", heartStr, damage);
            } else {
                return heartStr;
            }
        } else {
            return String.valueOf(damage);
        }
    }
    
    private static String formatWithPrecision(int precision, double value) {
        int safePrecision = Math.max(0, Math.min(precision, 10));
        return String.format("%." + safePrecision + "f", value);
    }
    
    private static String extractId(String statKey, String prefix) {
        return statKey.substring(prefix.length());
    }
    
    private static String getItemDisplayName(String itemId) {
        String cleanId = itemId.replace("minecraft:", "");
        return "item.minecraft." + cleanId;
    }
    
    private static String getBlockDisplayName(String blockId) {
        String cleanId = blockId.replace("minecraft:", "");
        return "block.minecraft." + cleanId;
    }
    
    private static String getEntityDisplayName(String entityId) {
        String cleanId = entityId.replace("minecraft:", "");
        return "entity.minecraft." + cleanId;
    }
}