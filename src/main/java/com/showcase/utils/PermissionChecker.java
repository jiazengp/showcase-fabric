package com.showcase.utils;

import com.showcase.ShowcaseMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionChecker {
    public static boolean hasPermission(CommandSource ctx, String permission, int fallbackLevel) {
        return Permissions.check(ctx,ShowcaseMod.MOD_ID.toLowerCase() + "." + permission, fallbackLevel);
    }

    public static boolean hasPermission(ServerPlayerEntity player, String permission, int fallbackLevel) {
        return Permissions.check(player,ShowcaseMod.MOD_ID.toLowerCase() + "." + permission, fallbackLevel);
    }

    public static boolean isOp(CommandSource ctx) {
        return hasPermission(ctx, "admin", 4);
    }

    public static boolean isOp(ServerPlayerEntity player) {
        return hasPermission(player, "admin", 4);
    }
}
