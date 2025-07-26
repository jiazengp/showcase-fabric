package com.showcase.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.Set;

public class StackUtils {
    public static Boolean isMap(ItemStack itemStack) {
        return (itemStack.isOf(Items.MAP) || itemStack.isOf(Items.FILLED_MAP));
    }

    public static Boolean isBook(ItemStack itemStack) {
        return (itemStack.isOf(Items.WRITTEN_BOOK) || itemStack.isOf(Items.WRITABLE_BOOK));
    }

    public static boolean isShulkerBox(ItemStack itemStack) {
        return itemStack.isIn(ItemTags.SHULKER_BOXES);
    }

    public static boolean isBundle(ItemStack itemStack) {
        return itemStack.isIn(ItemTags.BUNDLES);
    }

    public static boolean isDivider(ItemStack s) {
        return !s.isEmpty() && s.isOf(Items.GRAY_STAINED_GLASS_PANE) &&
                s.contains(DataComponentTypes.CUSTOM_NAME) &&
                Objects.requireNonNull(s.get(DataComponentTypes.CUSTOM_NAME)).getString().equals("⬛");
    }

    private static final Set<ScreenHandlerType<?>> WHITELISTED_TYPES = Set.of(
            ScreenHandlerType.GENERIC_9X3,
            ScreenHandlerType.GENERIC_9X6,
            ScreenHandlerType.GENERIC_3X3,
            ScreenHandlerType.CRAFTER_3X3,
            ScreenHandlerType.FURNACE,
            ScreenHandlerType.SMOKER,
            ScreenHandlerType.BLAST_FURNACE,
            ScreenHandlerType.BREWING_STAND,
            ScreenHandlerType.SHULKER_BOX
    );

    public static boolean isWhitelistedContainer(ScreenHandler handler) {
        return WHITELISTED_TYPES.contains(handler.getType());
    }

    public static Text getDisplayName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return TextUtils.UNKNOWN_ENTRY;
        }

        boolean isBook = StackUtils.isBook(stack);
        MutableText base = Text.literal(isBook ? "《" : "[");

        Text displayName = stack.getName();
        Text translatedName = Text.translatable(stack.getItem().getTranslationKey());

        boolean hasCustomName = !displayName.equals(translatedName);

        base.append(displayName.copy());

        if (hasCustomName) {
            base.append(Text.literal(" ("))
                    .append(translatedName.copy())
                    .append(Text.literal(")"));
        }

        if (stack.getMaxCount() > 1 && stack.getCount() > 1) {
            base.append("×").append(Text.literal(String.valueOf(stack.getCount())));
        }

        return base.append(isBook ? "》" : "]");
    }


    public static boolean isValid(ItemStack stack) {
        return !(stack == null || stack.isEmpty() || stack.getCount() <= 0);
    }
}
