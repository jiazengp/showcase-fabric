package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.showcase.config.ModConfigManager;
import com.showcase.placeholders.PlaceholderTest;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Command for testing essential placeholder functionality
 * Usage: /showcase-test placeholders
 */
public class PlaceholderTestCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("showcase-test")
                .requires(source -> source.isExecutedByPlayer() &&
                    PermissionChecker.hasPermission(source.getPlayer(), Permissions.Manage.CANCEL, 0))
                .then(CommandManager.literal("placeholders")
                    .executes(PlaceholderTestCommand::testPlaceholders))
                .then(CommandManager.literal("validation")
                    .executes(PlaceholderTestCommand::validatePlaceholders))
                .then(CommandManager.literal("performance")
                    .then(CommandManager.argument("iterations", IntegerArgumentType.integer(1, 1000))
                        .executes(PlaceholderTestCommand::testPerformance)))
        );
    }

    /**
     * Test all essential placeholders
     */
    private static int testPlaceholders(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        if (!ModConfigManager.isPlaceholderExtensionsEnabled()) {
            player.sendMessage(Text.literal("§cPlaceholder extensions are disabled in config"));
            return 0;
        }

        player.sendMessage(Text.literal("§6Starting placeholder test..."));
        PlaceholderTest.testEssentialPlaceholders(player);
        return 1;
    }

    /**
     * Validate placeholder registration
     */
    private static int validatePlaceholders(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.sendMessage(Text.literal("§6Validating placeholder registration..."));

        boolean isValid = PlaceholderTest.validatePlaceholderRegistration();
        if (isValid) {
            player.sendMessage(Text.literal("§a✓ All essential placeholders are registered correctly"));
        } else {
            player.sendMessage(Text.literal("§c✗ Some placeholders are missing or not registered"));
        }

        return isValid ? 1 : 0;
    }

    /**
     * Test placeholder performance
     */
    private static int testPerformance(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        int iterations = IntegerArgumentType.getInteger(context, "iterations");

        player.sendMessage(Text.literal("§6Starting performance test..."));
        PlaceholderTest.testPlaceholderPerformance(player, iterations);
        return 1;
    }
}