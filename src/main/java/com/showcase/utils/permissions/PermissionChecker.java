package com.showcase.utils.permissions;

import com.showcase.ShowcaseMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

public class PermissionChecker {
    public static boolean hasPermission(CommandSourceStack ctx, String permission, int fallbackLevel) {
        return ctx.checkPermission(permissionId(permission), PermissionLevel.byId(fallbackLevel));
    }

    public static boolean hasPermission(ServerPlayer player, String permission, int fallbackLevel) {
        return player.checkPermission(permissionId(permission), PermissionLevel.byId(fallbackLevel));
    }

    public static boolean isOp(CommandSourceStack ctx) {
        return hasPermission(ctx, com.showcase.utils.permissions.Permissions.ADMIN, 4);
    }

    public static boolean isOp(ServerPlayer player) {
        return hasPermission(player, com.showcase.utils.permissions.Permissions.ADMIN, 4);
    }
    
    private static Identifier permissionId(String permission) {
        return Identifier.fromNamespaceAndPath(ShowcaseMod.MOD_ID.toLowerCase(), permission.replace('.', '/'));
    }
}
