package com.showcase.config;

import com.showcase.command.ShowcaseManager;
import com.showcase.utils.ShareConstants;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.*;

@Configuration
public class ModConfig {
    @Comment({"Map view mode configuration:",
              "-1: Disable map preview functionality",
              "0: Use map preview with no time limit", 
              ">0: Use map preview with specified duration in seconds"})
    public int mapViewDuration = 10;

    @Comment({"Item view mode configuration:",
              "-1: Use traditional container view (no time limit)",
              "0: Use hotbar preview with no time limit",
              ">0: Use hotbar preview with specified duration in seconds"})
    public int itemViewDuration = -1;

    @Comment("The maximum number of placeholders that can be replaced in a single chat message.")
    public int  maxPlaceholdersPerMessage = 2;

    @Comment("Per-share type settings.")
    public Map<ShowcaseManager.ShareType, ShareSettings> shareSettings = defaultShareSettings();

    @Configuration
    public static class ShareSettings {
        @Comment("Cooldown time in seconds.")
        public int cooldown;

        @Comment("Default permission level (0-4).")
        public int defaultPermission;

        @Comment("Trigger keywords for this share type (not available for CONTAINER and MERCHANT).")
        public List<String> keywords;

        @Comment("Listening duration in seconds (only for CONTAINER and MERCHANT). Set -1 if not applicable.")
        public int listeningDuration;

        public ShareSettings() {}

        public ShareSettings(int cooldown, int defaultPermission, List<String> keywords, int listeningDuration) {
            this.cooldown = cooldown;
            this.defaultPermission = defaultPermission;
            this.keywords = keywords;
            this.listeningDuration = listeningDuration;
        }
    }

    @Comment("Share link related configuration")
    public ShareLinkSettings shareLink = new ShareLinkSettings();

    @Configuration
    public static class ShareLinkSettings {
        @Comment("Minimum expiry time in seconds for share links")
        public int minExpiryTime = 60;

        @Comment("Default expiry time in seconds for share links")
        public int defaultExpiryTime = 300;

        @Comment("Enable underline for clickable text messages")
        public boolean enableClickableTextUnderline = false;

        public ShareLinkSettings() {}
    }

    @Comment("Settings related to how player statistics are displayed")
    public StatsDisplaySettings statsDisplay = new StatsDisplaySettings();

    @Comment("Settings for item icons in showcase messages")
    public ItemIconSettings itemIcons = new ItemIconSettings();

    @Comment("Settings for extended placeholder functionality")
    public PlaceholderSettings placeholders = new PlaceholderSettings();

    @Configuration
    public static class StatsDisplaySettings {
        @Comment("Whether to show time-related statistics (play time, death time, rest time, etc.)")
        public boolean showTimeStats = true;

        @Comment("Whether to show movement-related statistics (walking, running, swimming, etc.)")
        public boolean showMovementStats = true;

        @Comment("Whether to show combat-related statistics (damage dealt/taken, kills, deaths, etc.)")
        public boolean showCombatStats = true;

        @Comment("Whether to show interaction statistics (trading, breeding, container interactions, etc.)")
        public boolean showInteractionStats = true;

        @Comment("Whether to show miscellaneous statistics (drops, cauldron usage, note blocks, etc.)")
        public boolean showMiscStats = true;

        @Comment("Maximum number of statistics to display per category in the book")
        public int maxStatsPerCategory = 15;

        @Comment("Whether to sort statistics by value (highest first) within each category")
        public boolean sortStatsByValue = true;

        @Comment("Whether to hide statistics with zero values")
        public boolean hideZeroStats = true;

        @Comment("Whether to show only the top statistics in each category")
        public boolean showOnlyTopStats = false;

        @Comment("Number of top statistics to show when showOnlyTopStats is enabled")
        public int topStatsCount = 10;

        @Comment("Custom formatting settings for time display")
        public TimeFormatSettings timeFormat = new TimeFormatSettings();

        @Comment("Custom formatting settings for distance display")
        public DistanceFormatSettings distanceFormat = new DistanceFormatSettings();

        @Comment("Custom formatting settings for damage display")
        public DamageFormatSettings damageFormat = new DamageFormatSettings();

        public StatsDisplaySettings() {}
    }

    @Configuration
    public static class TimeFormatSettings {
        @Comment("Whether to use compact time format (e.g., '1h 30m' instead of '1 hour 30 minutes')")
        public boolean useCompactFormat = true;

        @Comment("Whether to show seconds when displaying time under 1 minute")
        public boolean showSecondsUnderMinute = true;

        @Comment("Whether to hide zero components (e.g., show '1h 5s' instead of '1h 0m 5s')")
        public boolean hideZeroComponents = true;

        public TimeFormatSettings() {}
    }

    @Configuration
    public static class DistanceFormatSettings {
        @Comment("Whether to automatically convert centimeters to meters/kilometers")
        public boolean autoConvert = true;

        @Comment("Threshold in centimeters above which to convert to meters (default: 100cm = 1m)")
        public int meterThreshold = 100;

        @Comment("Threshold in centimeters above which to convert to kilometers (default: 100000cm = 1km)")
        public int kilometerThreshold = 100000;

        @Comment("Number of decimal places for meter display")
        public int meterDecimalPlaces = 1;

        @Comment("Number of decimal places for kilometer display")
        public int kilometerDecimalPlaces = 2;

        public DistanceFormatSettings() {}
    }

    @Configuration
    public static class DamageFormatSettings {
        @Comment("Whether to show damage as hearts (♥) instead of raw damage points")
        public boolean showAsHearts = true;

        @Comment("Number of decimal places for heart display")
        public int heartDecimalPlaces = 1;

        @Comment("Whether to show both hearts and raw damage (e.g., '5.0♥ (50)')")
        public boolean showBothFormats = false;

        public DamageFormatSettings() {}
    }

    @Configuration
    public static class ItemIconSettings {
        @Comment("Enable item icons in showcase messages")
        public boolean enabled = false;

        @Comment("Font namespace for item icons (e.g., 'custom', 'showcase')")
        public String fontNamespace = "iconifycraft";

        @Comment("Include item icons in item names")
        public boolean includeInItemNames = true;

        public ItemIconSettings() {}
    }

    @Configuration
    public static class PlaceholderSettings {
        @Comment("Enable extended placeholder functionality")
        public boolean enabled = true;

        @Comment("Maximum number of active shares per player")
        public int maxSharesPerPlayer = 10;

        @Comment("Cache duration for placeholder results in seconds")
        public int cacheDuration = 30;

        @Comment("Enable player statistics tracking for placeholders")
        public boolean enableStatisticsTracking = true;

        @Comment("Enable server-wide statistics for placeholders")
        public boolean enableServerStatistics = true;

        @Comment("Enable performance metrics tracking")
        public boolean enablePerformanceMetrics = true;

        @Comment("How often to update statistics cache in seconds")
        public int statisticsUpdateInterval = 60;


        @Comment("Enable conditional placeholders (if_xxx_yes_no style)")
        public boolean enableConditionalPlaceholders = true;

        public PlaceholderSettings() {}
    }

    private static Map<ShowcaseManager.ShareType, ShareSettings> defaultShareSettings() {
        Map<ShowcaseManager.ShareType, ShareSettings> defaults = new EnumMap<>(ShowcaseManager.ShareType.class);

        add(defaults, ShowcaseManager.ShareType.ITEM, 10, 0, Arrays.asList(ShareConstants.ITEM, "i"), -1);
        add(defaults, ShowcaseManager.ShareType.INVENTORY, 10, 0, Arrays.asList(ShareConstants.INVENTORY, "inv"), -1);
        add(defaults, ShowcaseManager.ShareType.HOTBAR, 10, 0, Arrays.asList(ShareConstants.HOTBAR, "hb"), -1);
        add(defaults, ShowcaseManager.ShareType.ENDER_CHEST, 10, 0, Arrays.asList(ShareConstants.ENDER_CHEST, "ec"), -1);
        add(defaults, ShowcaseManager.ShareType.STATS, 10, 0, List.of(ShareConstants.STATS, "statistical", "stat"), -1);
        add(defaults, ShowcaseManager.ShareType.CONTAINER, 10, 0, List.of(), 10);
        add(defaults, ShowcaseManager.ShareType.MERCHANT, 10, 0, List.of(), 10);

        return defaults;
    }

    private static void add(Map<ShowcaseManager.ShareType, ShareSettings> map, ShowcaseManager.ShareType type,
                            int cooldown, int perm, List<String> keywords, int listeningDuration) {
        map.put(type, new ShareSettings(cooldown, perm, keywords, listeningDuration));
    }
}
