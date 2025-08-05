package com.showcase.utils;

import com.showcase.ShowcaseMod;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtils {
    public static final Text BADGE = Text.literal(ShowcaseMod.MOD_ID).formatted(Formatting.BOLD, Formatting.ITALIC);
    public static final Text UNKNOWN_ENTRY =  Text.translatable("commands.banlist.entry.unknown");
    public static final Text UNKNOWN_PLAYER = Text.translatable("argument.player.unknown");
    public static final Text INVENTORY  = Text.translatable("key.categories.inventory");
    public static final Text HOTBAR  = Text.translatable("options.attack.hotbar");
    public static final Text ENDER_CHEST = Text.translatable("container.enderchest");
    public static final Text EMPTY = Text.translatable("item.minecraft.bundle.empty");
    public static final Text CONTAINER = Text.translatable("showcase.screen.container_title");

    private static final Style ERROR_STYLE = Style.EMPTY.withColor(Formatting.RED);
    private static final Style WARNING_STYLE = Style.EMPTY.withColor(Formatting.YELLOW);
    private static final Style SUCCESS_STYLE = Style.EMPTY.withColor(Formatting.GREEN);
    private static final Style INFO_STYLE = Style.EMPTY.withColor(Formatting.AQUA);

    public static Text error(Text msg) {
        return Text.literal("").append(msg).setStyle(ERROR_STYLE);
    }

    public static Text warning(Text msg) {
        return Text.literal("").append(msg).setStyle(WARNING_STYLE);
    }

    public static Text success(Text msg) {
        return Text.literal("").append(msg).setStyle(SUCCESS_STYLE);
    }

    public static Text info(Text msg) {
        return Text.literal("").append(msg).setStyle(INFO_STYLE);
    }
}
