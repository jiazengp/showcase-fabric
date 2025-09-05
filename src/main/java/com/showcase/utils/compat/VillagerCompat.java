package com.showcase.utils.compat;

import net.minecraft.village.VillagerData;

/**
 * Compatibility layer for Villager-related APIs across different Minecraft versions.
 * Handles API differences between 1.21.4 and 1.21.5+.
 */
public class VillagerCompat {
    
    /**
     * Gets the villager level from VillagerData.
     * Handles API changes between versions.
     */
    public static int getLevel(VillagerData villagerData) {
        #if MC_VER >= 1215
        return villagerData.level();
        #else
        return villagerData.getLevel();
        #endif
    }
}