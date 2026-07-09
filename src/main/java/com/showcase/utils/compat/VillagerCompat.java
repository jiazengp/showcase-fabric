package com.showcase.utils.compat;

import net.minecraft.world.entity.npc.villager.VillagerData;

/**
 * Small wrapper around villager-related APIs on the 26.x mainline.
 */
public class VillagerCompat {
    
    /**
     * Gets the villager level from VillagerData.
     */
    public static int getLevel(VillagerData villagerData) {
        return villagerData.level();
    }
}
