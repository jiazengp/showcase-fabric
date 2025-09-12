package com.showcase.utils.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
#if MC_VER < 1212
import net.minecraft.util.TypedActionResult;
#endif
import net.minecraft.item.ItemStack;

/**
 * Compatibility layer for Callback APIs across different Minecraft versions.
 * Handles return type differences between 1.21.1 and 1.21.2+.
 */
public class CallbackCompat {
    
    /**
     * Creates an appropriate return value for UseItemCallback.
     * In MC 1.21.1, returns TypedActionResult<ItemStack>
     * In MC 1.21.2+, returns ActionResult
     */
    public static Object getUseItemCallbackResult(PlayerEntity player, ActionResult result) {
        #if MC_VER >= 1212
        return result;
        #else
        return new TypedActionResult<>(result, ItemStack.EMPTY);
        #endif
    }
}