package com.showcase.utils.permissions;

import com.showcase.ShowcaseMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionChecker {
    public static boolean hasPermission(CommandSource ctx, String permission, int fallbackLevel) {
        return Permissions.check(ctx, buildFullPermission(permission), fallbackLevel);
    }

    public static boolean hasPermission(ServerPlayerEntity player, String permission, int fallbackLevel) {
        return Permissions.check(player, buildFullPermission(permission), fallbackLevel);
    }

    public static boolean isOp(CommandSource ctx) {
        return hasPermission(ctx, com.showcase.utils.permissions.Permissions.ADMIN, 4);
    }

    public static boolean isOp(ServerPlayerEntity player) {
        return hasPermission(player, com.showcase.utils.permissions.Permissions.ADMIN, 4);
    }
    
    private static String buildFullPermission(String permission) {
        return ShowcaseMod.MOD_ID.toLowerCase() + "." + permission;
    }
}
