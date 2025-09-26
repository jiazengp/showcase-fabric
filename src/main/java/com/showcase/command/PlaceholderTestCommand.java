package com.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
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
 * Command for testing extended placeholder functionality
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
                .then(CommandManager.literal("config")
                    .executes(PlaceholderTestCommand::testConfig))
                .then(CommandManager.literal("simulate")
                    .executes(PlaceholderTestCommand::simulateData))
                .then(CommandManager.literal("validate")
                    .executes(PlaceholderTestCommand::validateSystem))
                .then(CommandManager.literal("performance")
                    .executes(PlaceholderTestCommand::testPerformance))
                .then(CommandManager.literal("benchmark")
                    .then(CommandManager.argument("iterations", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 1000))
                        .executes(PlaceholderTestCommand::runBenchmark)))
        );
    }

    private static int testPlaceholders(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        if (!ModConfigManager.isPlaceholderExtensionsEnabled()) {
            context.getSource().sendError(Text.literal("Extended placeholders are disabled in configuration"));
            return 0;
        }

        player.sendMessage(Text.literal("§6Testing all extended placeholders..."));

        try {
            PlaceholderTest.testAllPlaceholders(player);
            return 1;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cPlaceholder test failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int testConfig(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        player.sendMessage(Text.literal("§6Testing placeholder configuration..."));

        try {
            PlaceholderTest.testConfiguration();

            // Send configuration summary to player
            boolean enabled = ModConfigManager.isPlaceholderExtensionsEnabled();
            int maxShares = ModConfigManager.getMaxSharesPerPlayer();
            boolean statsEnabled = ModConfigManager.isStatisticsTrackingEnabled();

            player.sendMessage(Text.literal("§7Configuration Status:"));
            player.sendMessage(Text.literal("§7- Placeholders enabled: " + (enabled ? "§aYes" : "§cNo")));
            player.sendMessage(Text.literal("§7- Max shares per player: §f" + maxShares));
            player.sendMessage(Text.literal("§7- Statistics tracking: " + (statsEnabled ? "§aEnabled" : "§cDisabled")));

            player.sendMessage(Text.literal("§aConfiguration test completed!"));
            return 1;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cConfiguration test failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int simulateData(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        player.sendMessage(Text.literal("§6Simulating share data for testing..."));

        try {
            PlaceholderTest.simulateShareCreation(player);
            player.sendMessage(Text.literal("§aTest data simulation completed!"));
            player.sendMessage(Text.literal("§7You can now test placeholders with simulated data."));
            return 1;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cData simulation failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int validateSystem(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        player.sendMessage(Text.literal("§6Validating placeholder system..."));

        try {
            boolean isValid = PlaceholderTest.validatePlaceholderSystem(player);

            if (isValid) {
                player.sendMessage(Text.literal("§aPlaceholder system validation: §lPASSED"));
                player.sendMessage(Text.literal("§7All core functionality is working correctly."));
            } else {
                player.sendMessage(Text.literal("§cPlaceholder system validation: §lFAILED"));
                player.sendMessage(Text.literal("§7Some features may not be working correctly."));
            }

            return isValid ? 1 : 0;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cSystem validation failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int testPerformance(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        try {
            PlaceholderTest.testPerformanceStatistics(player);
            return 1;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cPerformance test failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int runBenchmark(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        int iterations = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "iterations");

        try {
            PlaceholderTest.benchmarkPlaceholders(player, iterations);
            return 1;
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cBenchmark failed: " + e.getMessage()));
            return 0;
        }
    }
}