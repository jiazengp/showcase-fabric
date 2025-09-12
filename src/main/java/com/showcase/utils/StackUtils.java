package com.showcase.utils;

import com.showcase.ShowcaseMod;
import com.showcase.utils.ui.ItemIconProvider;
import net.minecraft.component.DataComponentTypes;
#if MC_VER >= 1212
import net.minecraft.component.type.BundleContentsComponent;
#endif
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.showcase.utils.ScreenHandlerUtils.handlerTypeForRows;

public class StackUtils {
    public static final ItemStack DIVIDER_ITEM;

    static {
        ItemStack temp = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        temp.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⬛").formatted(Formatting.DARK_GRAY));
        DIVIDER_ITEM = temp;
    }

    public static Boolean isMap(ItemStack itemStack) {
        return (itemStack.isOf(Items.MAP) || itemStack.isOf(Items.FILLED_MAP));
    }

    public static Boolean isBook(ItemStack itemStack) {
        return (itemStack.isOf(Items.WRITTEN_BOOK) || itemStack.isOf(Items.WRITABLE_BOOK));
    }

    public static boolean isShulkerBox(ItemStack itemStack) {
        return com.showcase.utils.compat.ItemTagsCompat.isShulkerBox(itemStack);
    }

    public static boolean isBundle(ItemStack itemStack) {
        #if MC_VER >= 1212
        return itemStack.isIn(ItemTags.BUNDLES);
        #else
        // Bundles don't exist in versions < 1.21.2
        return false;
        #endif
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
        
        // Add icon prefix for items outside the brackets on the left
        MutableText iconPrefix = ItemIconProvider.getIconForItem(stack);
        
        MutableText base = Text.empty();
        
        // Add icon outside the brackets/book markers on the left
        if (!iconPrefix.getString().isEmpty()) {
            base.append(iconPrefix).append(" ");
        }
        base.append(Text.literal(isBook ? "《" : "["));

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
            base.append("×").append(Text.literal(String.valueOf(stack.getCount()))).formatted(Formatting.AQUA);
        }

        return base.append(isBook ? "》" : "]");
    }

    public static ReadOnlyInventory unpackFromItemStack(ItemStack stack) {
        List<ItemStack> tmp = new ArrayList<>();
        Text itemName = StackUtils.getDisplayName(stack);

        if (StackUtils.isBundle(stack)) {
            #if MC_VER >= 1212
            BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (bundle != null) bundle.iterateCopy().forEach(tmp::add);
            #endif
            int rows = Math.min(6, Math.max(1, (tmp.size() + 8) / 9));
            int size = rows * 9;
            ReadOnlyInventory inv = new ReadOnlyInventory(size, itemName, handlerTypeForRows(rows));

            for (int i = 0; i < tmp.size() && i < size; i++) inv.setStack(i, tmp.get(i));
            return inv;
        }

        if (StackUtils.isShulkerBox(stack)) {
            try {
                ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
                if (container != null) tmp.addAll(container.stream().toList());
                int size = 27;
                ReadOnlyInventory inv = new ReadOnlyInventory(size, itemName, ScreenHandlerType.SHULKER_BOX);
                for (int i = 0; i < tmp.size() && i < size; i++) inv.setStack(i, tmp.get(i));
                return inv;
            } catch (RuntimeException e) {
                ShowcaseMod.LOGGER.error(e.toString());
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    public static ReadOnlyInventory snapshotFullInventory(ServerPlayerEntity player) {
        ReadOnlyInventory inv = new ReadOnlyInventory(54, TextUtils.INVENTORY, ScreenHandlerType.GENERIC_9X6);
        ItemStack playerHead = StackUtils.getPlayerHead(player);

        inv.setStack(0, playerHead.copy());
        
        // Add experience bottle with player's level
        ItemStack experienceBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
        experienceBottle.set(DataComponentTypes.CUSTOM_NAME, 
            Text.translatable("showcase.screen.player_level", player.experienceLevel));
        inv.setStack(1, experienceBottle);
        
        inv.setStack(2, DIVIDER_ITEM.copy());
        inv.setStack(3, player.getEquippedStack(EquipmentSlot.HEAD).copy());
        inv.setStack(4, player.getEquippedStack(EquipmentSlot.CHEST).copy());
        inv.setStack(5, player.getEquippedStack(EquipmentSlot.LEGS).copy());
        inv.setStack(6, player.getEquippedStack(EquipmentSlot.FEET).copy());
        inv.setStack(7, DIVIDER_ITEM.copy());
        inv.setStack(8, player.getEquippedStack(EquipmentSlot.OFFHAND).copy());

        for (int i = 0; i < 9; i++) inv.setStack(i + 9, player.getInventory().getStack(i).copy());
        for (int i = 18; i < 27; i++) inv.setStack(i, DIVIDER_ITEM.copy());
        for (int i = 9; i < 36; i++) inv.setStack(i + 18, player.getInventory().getStack(i).copy());

        return inv;
    }

    public static ItemStack getPlayerHead(ServerPlayerEntity player) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
        return head;
    }

    public static boolean isValid(ItemStack stack) {
        return !(stack == null || stack.isEmpty() || stack.getCount() <= 0);
    }
}
