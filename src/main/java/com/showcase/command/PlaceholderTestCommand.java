package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.showcase.config.ModConfigManager;
import com.showcase.placeholders.PlaceholderTest;
import com.showcase.utils.permissions.PermissionChecker;
import com.showcase.utils.permissions.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command for testing essential placeholder functionality
 * Usage: /showcase-test placeholders
 */
public class PlaceholderTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("showcase-test")
                .requires(source -> source.isPlayer() &&
                    PermissionChecker.hasPermission(source.getPlayer(), Permissions.Manage.CANCEL, 0))
                .then(Commands.literal("placeholders")
                    .executes(PlaceholderTestCommand::testPlaceholders))
                .then(Commands.literal("validation")
                    .executes(PlaceholderTestCommand::validatePlaceholders))
                .then(Commands.literal("performance")
                    .then(Commands.argument("iterations", IntegerArgumentType.integer(1, 1000))
                        .executes(PlaceholderTestCommand::testPerformance)))
        );
    }

    /**
     * Test all essential placeholders
     */
    private static int testPlaceholders(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        if (!ModConfigManager.isPlaceholderExtensionsEnabled()) {
            player.sendSystemMessage(Component.literal("§cPlaceholder extensions are disabled in config"));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6Starting placeholder test..."));
        PlaceholderTest.testEssentialPlaceholders(player);
        return 1;
    }

    /**
     * Validate placeholder registration
     */
    private static int validatePlaceholders(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.sendSystemMessage(Component.literal("§6Validating placeholder registration..."));

        boolean isValid = PlaceholderTest.validatePlaceholderRegistration();
        if (isValid) {
            player.sendSystemMessage(Component.literal("§a✓ All essential placeholders are registered correctly"));
        } else {
            player.sendSystemMessage(Component.literal("§c✗ Some placeholders are missing or not registered"));
        }

        return isValid ? 1 : 0;
    }

    /**
     * Test placeholder performance
     */
    private static int testPerformance(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        int iterations = IntegerArgumentType.getInteger(context, "iterations");

        player.sendSystemMessage(Component.literal("§6Starting performance test..."));
        PlaceholderTest.testPlaceholderPerformance(player, iterations);
        return 1;
    }
}