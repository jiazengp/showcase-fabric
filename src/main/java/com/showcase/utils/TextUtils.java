package com.showcase.utils;

import com.showcase.ShowcaseMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class TextUtils {
    public static final Component BADGE = Component.literal(ShowcaseMod.MOD_ID).withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC);
    public static final Component UNKNOWN_ENTRY =  Component.translatable("commands.banlist.entry.unknown");
    public static final Component UNKNOWN_PLAYER = Component.translatable("argument.player.unknown");
    public static final Component INVENTORY  = Component.translatable("showcase.name.inventory");
    public static final Component HOTBAR  = Component.translatable("options.attack.hotbar");
    public static final Component ENDER_CHEST = Component.translatable("container.enderchest");
    public static final Component EMPTY = Component.translatable("item.minecraft.bundle.empty");
    public static final Component CONTAINER = Component.translatable("showcase.name.container");

    private static final Style ERROR_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final Style WARNING_STYLE = Style.EMPTY.withColor(ChatFormatting.YELLOW);
    private static final Style SUCCESS_STYLE = Style.EMPTY.withColor(ChatFormatting.GREEN);
    private static final Style INFO_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);

    public static Component error(Component msg) {
        return Component.literal("").append(msg).setStyle(ERROR_STYLE);
    }

    public static Component warning(Component msg) {
        return Component.literal("").append(msg).setStyle(WARNING_STYLE);
    }

    public static Component success(Component msg) {
        return Component.literal("").append(msg).setStyle(SUCCESS_STYLE);
    }

    public static Component info(Component msg) {
        return Component.literal("").append(msg).setStyle(INFO_STYLE);
    }
}
