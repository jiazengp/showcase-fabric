package com.showcase.utils;

import com.showcase.ShowcaseMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class TextUtils {
    public static final Text BADGE = Text.literal(ShowcaseMod.MOD_ID).formatted(Formatting.BOLD, Formatting.ITALIC);
    public static final Text UNKNOWN_ENTRY =  Text.translatable("commands.banlist.entry.unknown");
    public static final Text UNKNOW_PLAYER = Text.translatable("argument.player.unknown");
   public static final Text INVENTORY  = Text.translatable("key.categories.inventory");
   public static final Text HOTBAR  = Text.translatable("options.attack.hotbar");
   public static final Text ENDER_CHEST = Text.translatable("container.enderchest");
   public static final Text EMPTY = Text.translatable("item.minecraft.bundle.empty");
   public static final Text CONTAINER = Text.translatable("showcase.screen.container_title");

    public static Text error(Text msg) {
        return Text.literal("").append(msg).formatted(Formatting.RED);
    }

    public static Text warning(Text msg) {
        return Text.literal("").append(msg).formatted(Formatting.YELLOW);
    }

    public static Text success(Text msg) {
        return Text.literal("").append(msg).formatted(Formatting.GREEN);
    }

    public static Text info(Text msg) {
        return Text.literal("").append(msg).formatted(Formatting.GRAY);
    }
}
