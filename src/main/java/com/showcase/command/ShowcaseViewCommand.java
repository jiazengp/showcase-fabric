package com.showcase.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ShowcaseViewCommand {
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher, String command) {
            dispatcher.register(literal(command)
                    .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.VIEW, 0))
                    .then(argument("id", StringArgumentType.string())
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                String id = StringArgumentType.getString(ctx, "id");
                                if (!ShowcaseManager.openSharedContent(player, id)) {
                                    return -1;
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );
        }
}
