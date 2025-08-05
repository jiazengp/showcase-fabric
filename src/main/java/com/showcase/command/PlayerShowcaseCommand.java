package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerShowcaseCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, String command) {
        var root = literal(command)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.COMMAND, 0))
                .executes(ctx -> ShareCommandExecutor.shareItem(ctx, ShareCommandUtils.getSenderPlayer(ctx), null, null, null));

        root.then(ShareCommandExecutor.createInventoryShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.INVENTORY,
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.INVENTORY).defaultPermission)));

        root.then(ShareCommandExecutor.createHotbarShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.HOTBAR,
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.HOTBAR).defaultPermission)));

        root.then(ShareCommandExecutor.createEnderChestShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.ENDERCHEST,
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.ENDER_CHEST).defaultPermission)));

        root.then(ShareCommandExecutor.createStatsShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.STATS,
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.STATS).defaultPermission)));

        root.then(ShareCommandExecutor.createItemShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.ITEM,
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.ITEM).defaultPermission)));

        root.then(ShareCommandExecutor.createContainerShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.CONTAINER,
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.CONTAINER).defaultPermission)));

        root.then(ShareCommandExecutor.shareMerchantShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.MERCHANT,
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.MERCHANT).defaultPermission)));

        root.then(ShareCommandExecutor.createCancelCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.CANCEL, 0)));

        dispatcher.register(root);
    }
}
