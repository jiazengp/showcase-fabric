package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.showcase.utils.permissions.PermissionChecker;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class AdminShowcaseCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, String command) {
        dispatcher.register(literal(command)
                .requires(PermissionChecker::isOp)
                .executes(ctx -> ShareCommandExecutor.shareItem(ctx, ShareCommandUtils.getSenderPlayer(ctx), null, null, null))
                .then(ShareCommandExecutor.createInventoryShareCommand(true))
                .then(ShareCommandExecutor.createHotbarShareCommand(true))
                .then(ShareCommandExecutor.createEnderChestShareCommand(true))
                .then(ShareCommandExecutor.createStatsShareCommand(true))
                .then(ShareCommandExecutor.createItemShareCommand(true))
        );
    }
}
