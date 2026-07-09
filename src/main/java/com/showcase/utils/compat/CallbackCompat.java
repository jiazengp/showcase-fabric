package com.showcase.utils.compat;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/**
 * Small wrapper around callback return values for the 26.x mainline.
 */
public class CallbackCompat {
    
    /**
     * Creates the return value for UseItemCallback on the current mainline.
     */
    public static InteractionResult getUseItemCallbackResult(Player player, InteractionResult result) {
        return result;
    }
}
