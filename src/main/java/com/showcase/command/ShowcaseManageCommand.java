package com.showcase.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfig;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.listener.ChatMessageListener;
import com.showcase.utils.ChatPaginator;
import com.showcase.utils.ModMetadataHolder;
import com.showcase.utils.permissions.Permissions;
import com.showcase.utils.TextUtils;
import com.showcase.utils.TextEventFactory;
import com.showcase.utils.ui.TextBuilder;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.showcase.command.ShareCommandUtils.*;
import static com.showcase.command.ShowcaseCommand.CANCEL_COMMAND;
import static com.showcase.command.ShowcaseCommand.MANAGE_COMMAND;
import static com.showcase.utils.permissions.PermissionChecker.hasPermission;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ShowcaseManageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, String command) {
        dispatcher.register(
                literal(command)
                        .requires(src -> hasPermission(src, Permissions.MANAGE, 4))
                        .then(createReloadCommand())
                        .then(createListCommand())
                        .then(createCancelCommand())
                        .then(createAboutCommand())
                        .then(createConfigCommands())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createAboutCommand() {
        return literal("about")
                .requires(src -> hasPermission(src, Permissions.Manage.ABOUT, 0))
                .executes(ctx -> {
                    ServerPlayer player = getSenderPlayer(ctx);

                    BiConsumer<String, String> sendInfoLine = getStringStringBiConsumer(player);

                    player.sendSystemMessage(Component.literal("=== Showcase Mod About ===").withStyle(ChatFormatting.GOLD), false);

                    sendInfoLine.accept("Mod ID", ModMetadataHolder.MOD_ID);
                    sendInfoLine.accept("Name", ModMetadataHolder.MOD_NAME);
                    sendInfoLine.accept("Author", ModMetadataHolder.AUTHORS.stream()
                            .map(Person::getName)
                            .collect(Collectors.joining(", ")));
                    sendInfoLine.accept("Version", ModMetadataHolder.VERSION);
                    sendInfoLine.accept("Source Code", ModMetadataHolder.SOURCE);
                    sendInfoLine.accept("Issues", ModMetadataHolder.ISSUES);
                    sendInfoLine.accept("Homepage (CN)", ModMetadataHolder.HOMEPAGE);
                    sendInfoLine.accept("License", ModMetadataHolder.LICENSE);

                    player.sendSystemMessage(Component.literal("===========================").withStyle(ChatFormatting.GOLD), false);

                    return Command.SINGLE_SUCCESS;
                });
    }

    private static @NotNull BiConsumer<String, String> getStringStringBiConsumer(ServerPlayer player) {
        return (name, value) -> {
            MutableComponent line = Component.literal("")
                    .append(Component.literal(name).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY));

            if (value.startsWith("https://")) {
                line.append(TextBuilder.clickableWithConfig(
                        Component.literal(value.replaceAll("https://", "")),
                        TextEventFactory.openUrl(value)
                ).withStyle(style -> style.withColor(ChatFormatting.AQUA)));
            } else {
                line.append(Component.literal(value).withStyle(ChatFormatting.WHITE));
            }

            player.sendSystemMessage(line, false);
        };
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createReloadCommand() {
        return literal("reload")
                .requires(src -> hasPermission(src, Permissions.Manage.RELOAD, 4))
                .executes(ctx -> {
                    try {
                        ModConfigManager.reloadConfig();
                        ChatMessageListener.loadConfig();
                        ctx.getSource().sendSystemMessage(TextUtils.success(Component.translatable("showcase.message.reload.success")));
                        return Command.SINGLE_SUCCESS;
                    } catch (Exception e) {
                        ShowcaseMod.LOGGER.error("Failed to reload config", e);
                        ctx.getSource().sendFailure(TextUtils.error(Component.translatable("showcase.message.reload.fail")));
                        return 0;
                    }
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createListCommand() {
        return literal("list")
                .requires(src -> hasPermission(src, Permissions.Manage.LIST, 4))
                .executes(ctx -> executeListCommand(ctx.getSource().getPlayerOrException(), 1))
                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int page = IntegerArgumentType.getInteger(ctx, "page");
                            return executeListCommand(ctx.getSource().getPlayerOrException(), page);
                        }));
    }

    private static int executeListCommand(ServerPlayer player, int page) {
        if (player == null) return 0;

        Map<String, ShareEntry> shares = ShowcaseManager.getUnmodifiableActiveShares();

        if (shares.isEmpty()) {
            player.sendSystemMessage(TextUtils.info(Component.translatable("showcase.message.manage.empty")));
            return 0;
        }

        List<Map.Entry<String, ShareEntry>> shareList = new ArrayList<>(shares.entrySet());
        ChatPaginator<Map.Entry<String, ShareEntry>> paginator = new ChatPaginator<>(shareList, 3, "/" + MANAGE_COMMAND + " list");

        Component result = paginator.renderPage(page, entry ->
                buildShareLine(player, entry.getKey(), entry.getValue()), "Active Shares");

        player.sendSystemMessage(result);
        return Command.SINGLE_SUCCESS;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createCancelCommand() {
        return literal(CANCEL_COMMAND)
                .requires(src -> hasPermission(src, Permissions.Manage.CANCEL, 4))
                .then(argument("target", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            ShowcaseManager.getShareIdCompletions().forEach(builder::suggest);
                            getPlayerNameCompletions(ctx).forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();
                            String target = StringArgumentType.getString(ctx, "target");

                            if (ShowcaseManager.expireShareById(target)) {
                                admin.sendSystemMessage(TextUtils.success(Component.translatable("showcase.message.manage.cancel.success_id", target)));
                                return Command.SINGLE_SUCCESS;
                            }

                            ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(target);
                            if (player != null) {
                                int count = ShowcaseManager.expireSharesByPlayer(player.getUUID());
                                if (count > 0) {
                                    admin.sendSystemMessage(TextUtils.success(Component.translatable("showcase.message.manage.cancel.success_player", count, player.getDisplayName())));
                                } else {
                                    admin.sendSystemMessage(TextUtils.error(Component.translatable("showcase.message.manage.cancel.empty", player.getDisplayName())));
                                }
                                return count > 0 ? Command.SINGLE_SUCCESS : 0;
                            }

                            admin.sendSystemMessage(TextUtils.error(Component.translatable("showcase.message.manage.cancel.not_found")));
                            return 0;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createConfigCommands() {
        return literal("config")
                .requires(src -> hasPermission(src, Permissions.MANAGE, 4))
                .then(literal("validate")
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();

                            boolean isValid = ModConfigManager.isCurrentConfigValid();
                            if (isValid) {
                                admin.sendSystemMessage(TextUtils.success(Component.literal("Configuration is valid")));
                            } else {
                                admin.sendSystemMessage(TextUtils.error(Component.literal("Configuration validation failed")));
                            }

                            return isValid ? Command.SINGLE_SUCCESS : 0;
                        }))
                .then(literal("backup")
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();

                            boolean success = ModConfigManager.createConfigBackup("manual");
                            if (success) {
                                admin.sendSystemMessage(TextUtils.success(Component.literal("Configuration backed up successfully")));
                                return Command.SINGLE_SUCCESS;
                            } else {
                                admin.sendSystemMessage(TextUtils.error(Component.literal("Failed to create configuration backup")));
                                return 0;
                            }
                        }))
                .then(literal("reset")
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();

                            boolean success = ModConfigManager.resetConfigToDefaults();
                            if (success) {
                                admin.sendSystemMessage(TextUtils.success(Component.literal("Configuration reset to defaults. Previous config backed up.")));
                                return Command.SINGLE_SUCCESS;
                            } else {
                                admin.sendSystemMessage(TextUtils.error(Component.literal("Failed to reset configuration")));
                                return 0;
                            }
                        }))
                .then(literal("status")
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();

                            // Configuration status information
                            boolean isValid = ModConfigManager.isCurrentConfigValid();

                            admin.sendSystemMessage(Component.literal("=== Configuration Status ===").withStyle(ChatFormatting.GOLD));

                            Component validationStatus = isValid
                                ? Component.literal("✓ Valid").withStyle(ChatFormatting.GREEN)
                                : Component.literal("✗ Invalid").withStyle(ChatFormatting.RED);
                            admin.sendSystemMessage(Component.literal("Validation: ").append(validationStatus));

                            // Config file info
                            String configPath = ModConfigManager.getConfigPath().toString();
                            admin.sendSystemMessage(Component.literal("Config File: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(configPath).withStyle(ChatFormatting.WHITE)));

                            // Backup directory info
                            String backupPath = ModConfigManager.getBackupDirectory().toString();
                            admin.sendSystemMessage(Component.literal("Backup Directory: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(backupPath).withStyle(ChatFormatting.WHITE)));

                            // Basic config info
                            ModConfig config = ModConfigManager.getConfig();
                            admin.sendSystemMessage(Component.literal("Share Types: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(String.valueOf(config.shareSettings.size())).withStyle(ChatFormatting.WHITE)));

                            admin.sendSystemMessage(Component.literal("Max Shares/Player: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(String.valueOf(config.placeholders.maxSharesPerPlayer)).withStyle(ChatFormatting.WHITE)));

                            admin.sendSystemMessage(Component.literal("==============================").withStyle(ChatFormatting.GOLD));

                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("info")
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();

                            admin.sendSystemMessage(Component.literal("=== Configuration Management ===").withStyle(ChatFormatting.GOLD));
                            admin.sendSystemMessage(Component.literal("Available Commands:").withStyle(ChatFormatting.YELLOW));
                            admin.sendSystemMessage(Component.literal("• /showcase-manage config validate").withStyle(ChatFormatting.WHITE)
                                .append(Component.literal(" - Check configuration validity").withStyle(ChatFormatting.GRAY)));
                            admin.sendSystemMessage(Component.literal("• /showcase-manage config backup").withStyle(ChatFormatting.WHITE)
                                .append(Component.literal(" - Create manual backup").withStyle(ChatFormatting.GRAY)));
                            admin.sendSystemMessage(Component.literal("• /showcase-manage config reset").withStyle(ChatFormatting.WHITE)
                                .append(Component.literal(" - Reset to defaults (creates backup)").withStyle(ChatFormatting.GRAY)));
                            admin.sendSystemMessage(Component.literal("• /showcase-manage config status").withStyle(ChatFormatting.WHITE)
                                .append(Component.literal(" - Show configuration status").withStyle(ChatFormatting.GRAY)));
                            admin.sendSystemMessage(Component.literal("• /showcase-manage reload").withStyle(ChatFormatting.WHITE)
                                .append(Component.literal(" - Reload configuration").withStyle(ChatFormatting.GRAY)));
                            admin.sendSystemMessage(Component.literal("=================================").withStyle(ChatFormatting.GOLD));

                            return Command.SINGLE_SUCCESS;
                        }));
    }
}

