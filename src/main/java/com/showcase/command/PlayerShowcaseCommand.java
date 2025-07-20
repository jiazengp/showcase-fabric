package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.showcase.utils.PermissionChecker;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerShowcaseCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, String command) {
        var root = literal(command)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands", 0)) // 主命令权限
                .executes(ctx -> ShareCommandExecutor.shareItem(ctx, null, null, null));

        root.then(ShareCommandExecutor.createInventoryShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.inventory", 0)));

        root.then(ShareCommandExecutor.createHotbarShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.hotbar", 0)));

        root.then(ShareCommandExecutor.createEnderChestShareCommand(false)
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.ender_chest", 0)));

        root.then(ShareCommandExecutor.createItemShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.item", 0)));

        root.then(ShareCommandExecutor.createContainerShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.container", 0)));

        root.then(ShareCommandExecutor.createCancelCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.cancel", 0)));

        root.then(ShareCommandExecutor.shareMerchantShareCommand()
                .requires(ctx -> PermissionChecker.hasPermission(ctx, "commands.merchant", 0)));

        dispatcher.register(root);
    }
}
