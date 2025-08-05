package com.showcase.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.showcase.ShowcaseMod;
import com.showcase.config.ModConfigManager;
import com.showcase.data.ShareEntry;
import com.showcase.listener.ChatMessageListener;
import com.showcase.utils.ChatPaginator;
import com.showcase.utils.ModMetadataHolder;
import com.showcase.utils.permissions.Permissions;
import com.showcase.utils.TextUtils;
import com.showcase.utils.TextEventFactory;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.showcase.command.ShareCommandUtils.*;
import static com.showcase.command.ShowcaseCommand.CANCEL_COMMAND;
import static com.showcase.command.ShowcaseCommand.MANAGE_COMMAND;
import static com.showcase.utils.permissions.PermissionChecker.hasPermission;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShowcaseManageCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, String command) {
        dispatcher.register(
                literal(command)
                        .requires(src -> hasPermission(src, Permissions.MANAGE, 4))
                        .then(createReloadCommand())
                        .then(createListCommand())
                        .then(createCancelCommand())
                        .then(createAboutCommand())
        );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createAboutCommand() {
        return literal("about")
                .requires(src -> hasPermission(src, Permissions.Manage.ABOUT, 0))
                .executes(ctx -> {
                    ServerPlayerEntity player = getSenderPlayer(ctx);

                    BiConsumer<String, String> sendInfoLine = getStringStringBiConsumer(player);

                    player.sendMessage(Text.literal("=== Showcase Mod About ===").formatted(Formatting.GOLD), false);

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

                    player.sendMessage(Text.literal("===========================").formatted(Formatting.GOLD), false);

                    return Command.SINGLE_SUCCESS;
                });
    }

    private static @NotNull BiConsumer<String, String> getStringStringBiConsumer(ServerPlayerEntity player) {
        return (name, value) -> {
            MutableText line = Text.literal("")
                    .append(Text.literal(name).formatted(Formatting.YELLOW))
                    .append(Text.literal(": ").formatted(Formatting.GRAY));

            if (value.startsWith("https://")) {
                line.append(Text.literal(value.replaceAll("https://", "")).styled(style -> style
                        .withClickEvent(TextEventFactory.openUrl(value))
                        .withUnderline(true)
                        .withColor(Formatting.AQUA)
                ));
            } else {
                line.append(Text.literal(value).formatted(Formatting.WHITE));
            }

            player.sendMessage(line, false);
        };
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createReloadCommand() {
        return literal("reload")
                .requires(src -> hasPermission(src, Permissions.Manage.RELOAD, 4))
                .executes(ctx -> {
                    try {
                        ModConfigManager.reloadConfig();
                        ChatMessageListener.loadConfig();
                        ctx.getSource().sendMessage(TextUtils.success(Text.translatable("showcase.message.reload.success")));
                        return Command.SINGLE_SUCCESS;
                    } catch (Exception e) {
                        ShowcaseMod.LOGGER.error("Failed to reload config", e);
                        ctx.getSource().sendError(TextUtils.error(Text.translatable("showcase.message.reload.fail")));
                        return 0;
                    }
                });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createListCommand() {
        return literal("list")
                .requires(src -> hasPermission(src, Permissions.Manage.LIST, 4))
                .executes(ctx -> executeListCommand(ctx.getSource().getPlayerOrThrow(), 1))
                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int page = IntegerArgumentType.getInteger(ctx, "page");
                            return executeListCommand(ctx.getSource().getPlayerOrThrow(), page);
                        }));
    }

    private static int executeListCommand(ServerPlayerEntity player, int page) {
        if (player == null) return 0;

        Map<String, ShareEntry> shares = ShowcaseManager.getUnmodifiableActiveShares();

        if (shares.isEmpty()) {
            player.sendMessage(TextUtils.info(Text.translatable("showcase.message.manage.empty")));
            return 0;
        }

        List<Map.Entry<String, ShareEntry>> shareList = new ArrayList<>(shares.entrySet());
        ChatPaginator<Map.Entry<String, ShareEntry>> paginator = new ChatPaginator<>(shareList, 3, "/" + MANAGE_COMMAND + " list");

        Text result = paginator.renderPage(page, entry ->
                buildShareLine(player, entry.getKey(), entry.getValue()), "Active Shares");

        player.sendMessage(result);
        return Command.SINGLE_SUCCESS;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createCancelCommand() {
        return literal(CANCEL_COMMAND)
                .requires(src -> hasPermission(src, Permissions.Manage.CANCEL, 4))
                .then(argument("target", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            ShowcaseManager.getShareIdCompletions().forEach(builder::suggest);
                            getPlayerNameCompletions(ctx).forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();
                            String target = StringArgumentType.getString(ctx, "target");

                            if (ShowcaseManager.expireShareById(target)) {
                                admin.sendMessage(TextUtils.success(Text.translatable("showcase.message.manage.cancel.success_id", target)));
                                return Command.SINGLE_SUCCESS;
                            }

                            ServerPlayerEntity player = ctx.getSource().getServer().getPlayerManager().getPlayer(target);
                            if (player != null) {
                                int count = ShowcaseManager.expireSharesByPlayer(player.getUuid());
                                if (count > 0) {
                                    admin.sendMessage(TextUtils.success(Text.translatable("showcase.message.manage.cancel.success_player", count, player.getDisplayName())));
                                } else {
                                    admin.sendMessage(TextUtils.error(Text.translatable("showcase.message.manage.cancel.empty", player.getDisplayName())));
                                }
                                return count > 0 ? Command.SINGLE_SUCCESS : 0;
                            }

                            admin.sendMessage(TextUtils.error(Text.translatable("showcase.message.manage.cancel.not_found")));
                            return 0;
                        }));
    }
}
