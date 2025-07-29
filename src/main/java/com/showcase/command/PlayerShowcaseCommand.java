package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.showcase.config.ModConfigManager;
import com.showcase.utils.PermissionChecker;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerShowcaseCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, String command) {
        var root = literal(command)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands", 0)) // 主命令权限
                .executes(ctx -> ShareCommandExecutor.shareItem(ctx, null, null, null));

        root.then(ShareCommandExecutor.createInventoryShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.inventory",
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.INVENTORY).defaultPermission)));

        root.then(ShareCommandExecutor.createHotbarShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.hotbar",
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.HOTBAR).defaultPermission)));

        root.then(ShareCommandExecutor.createEnderChestShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.ender_chest",
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.ENDER_CHEST).defaultPermission)));

        root.then(ShareCommandExecutor.createItemShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.item",
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.ITEM).defaultPermission)));

        root.then(ShareCommandExecutor.createContainerShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.container",
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.CONTAINER).defaultPermission)));

        root.then(ShareCommandExecutor.shareMerchantShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.merchant",
                        ModConfigManager.getShareSettings(ShowcaseManager.ShareType.MERCHANT).defaultPermission)));

        root.then(ShareCommandExecutor.createCancelCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.cancel",0)));

        dispatcher.register(root);
    }
}
