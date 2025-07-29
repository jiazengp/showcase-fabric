package com.showcase.config;

import com.showcase.command.ShowcaseManager;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.*;

@Configuration
public class ModConfig {
    @Comment("Per-share type settings.")
    public Map<ShowcaseManager.ShareType, ShareSettings> shareSettings = defaultShareSettings();

    @Configuration
    public static class ShareSettings {
        @Comment("Cooldown time in seconds.")
        public int cooldown;

        @Comment("Default permission level (0-4).")
        public int defaultPermission;

        @Comment("Trigger keywords for this share type.")
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

        public ShareLinkSettings() {}
    }

    private static Map<ShowcaseManager.ShareType, ShareSettings> defaultShareSettings() {
        Map<ShowcaseManager.ShareType, ShareSettings> defaults = new EnumMap<>(ShowcaseManager.ShareType.class);

        add(defaults, ShowcaseManager.ShareType.ITEM, 10, 0, Arrays.asList("item", "item"), -1);
        add(defaults, ShowcaseManager.ShareType.INVENTORY, 10, 0, Arrays.asList("inventory", "inv"), -1);
        add(defaults, ShowcaseManager.ShareType.HOTBAR, 10, 0, Arrays.asList("hotbar", "hb"), -1);
        add(defaults, ShowcaseManager.ShareType.ENDER_CHEST, 10, 0, Arrays.asList("ender", "ec"), -1);
        add(defaults, ShowcaseManager.ShareType.CONTAINER, 10, 0, Arrays.asList("container", "chest"), 10);
        add(defaults, ShowcaseManager.ShareType.MERCHANT, 10, 0, Arrays.asList("merchant", "trade"), 10);

        return defaults;
    }

    private static void add(Map<ShowcaseManager.ShareType, ShareSettings> map, ShowcaseManager.ShareType type,
                            int cooldown, int perm, List<String> keywords, int listeningDuration) {
        map.put(type, new ShareSettings(cooldown, perm, keywords, listeningDuration));
    }
}
