package com.showcase.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShowcaseViewCommand {
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher, String command) {
            dispatcher.register(literal(command)
                    .requires(ctx -> PermissionChecker.hasPermission(ctx, Permissions.Command.VIEW, 0))
                    .then(argument("id", StringArgumentType.string())
                            .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
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
