package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class ShowcaseCommand {
    public static final String RECEIVERS_ARG = "receivers";
    public static final String DESCRIPTION_ARG = "description";
    public static final String SOURCE_ARG = "source";
    public static final String DURATION_ARG = "duration";
    public static final String VIEW_COMMAND = "showcase-view";
    public static final String CANCEL_COMMAND = "cancel";
    public static final String SHARE_COMMAND = "showcase";
    public static final String ADMIN_SHARE_COMMAND = "admin-showcase";
    public static final String MANAGE_COMMAND = "showcase-manage";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ShowcaseViewCommand.register(dispatcher, VIEW_COMMAND);
        AdminShowcaseCommand.register(dispatcher, ADMIN_SHARE_COMMAND);
        PlayerShowcaseCommand.register(dispatcher, SHARE_COMMAND);
        ShowcaseManageCommand.register(dispatcher, MANAGE_COMMAND);
    }
}