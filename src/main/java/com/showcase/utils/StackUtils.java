package com.showcase.utils;

import com.showcase.ShowcaseMod;
import com.showcase.utils.compat.ProfileCompat;
import com.showcase.utils.ui.ItemIconProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.showcase.utils.ScreenHandlerUtils.handlerTypeForRows;

public class StackUtils {
    public static final ItemStack DIVIDER_ITEM;
    private static final Item DIVIDER_ITEM_TYPE = resolveDividerItemType();

    static {
        ItemStack temp = new ItemStack(DIVIDER_ITEM_TYPE);
        temp.set(DataComponents.CUSTOM_NAME, Component.literal("⬛").withStyle(ChatFormatting.DARK_GRAY));
        DIVIDER_ITEM = temp;
    }

    private static Item resolveDividerItemType() {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", "gray_stained_glass_pane"));
        return item != null && item != Items.AIR ? item : Items.GLASS_PANE;
    }

    public static Boolean isMap(ItemStack itemStack) {
        return (itemStack.is(Items.MAP) || itemStack.is(Items.FILLED_MAP));
    }

    public static Boolean isBook(ItemStack itemStack) {
        return (itemStack.is(Items.WRITTEN_BOOK) || itemStack.is(Items.WRITABLE_BOOK));
    }

    public static boolean isShulkerBox(ItemStack itemStack) {
        return com.showcase.utils.compat.ItemTagsCompat.isShulkerBox(itemStack);
    }

    public static boolean isBundle(ItemStack itemStack) {
        return itemStack.is(ItemTags.BUNDLES);
    }

    public static boolean isDivider(ItemStack s) {
        return !s.isEmpty() && s.is(DIVIDER_ITEM_TYPE) &&
                s.has(DataComponents.CUSTOM_NAME) &&
                Objects.requireNonNull(s.get(DataComponents.CUSTOM_NAME)).getString().equals("⬛");
    }

    private static final Set<MenuType<?>> WHITELISTED_TYPES = Set.of(
            MenuType.GENERIC_9x3,
            MenuType.GENERIC_9x6,
            MenuType.GENERIC_3x3,
            MenuType.CRAFTER_3x3,
            MenuType.FURNACE,
            MenuType.SMOKER,
            MenuType.BLAST_FURNACE,
            MenuType.BREWING_STAND,
            MenuType.SHULKER_BOX
    );

    public static boolean isWhitelistedContainer(AbstractContainerMenu handler) {
        return WHITELISTED_TYPES.contains(handler.getType());
    }

    public static Component getDisplayName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return TextUtils.UNKNOWN_ENTRY;
        }

        boolean isBook = StackUtils.isBook(stack);
        
        // Add icon prefix for items outside the brackets on the left
        MutableComponent iconPrefix = ItemIconProvider.getIconForItem(stack);
        
        MutableComponent base = Component.empty();
        
        // Add icon outside the brackets/book markers on the left
        if (!iconPrefix.getString().isEmpty()) {
            base.append(iconPrefix).append(" ");
        }
        base.append(Component.literal(isBook ? "《" : "["));

        Component displayName = stack.getHoverName();
        Component translatedName = Component.translatable(stack.getItem().getDescriptionId());

        boolean hasCustomName = !displayName.equals(translatedName);

        base.append(displayName.copy());

        if (hasCustomName) {
            base.append(Component.literal(" ("))
                    .append(translatedName.copy())
                    .append(Component.literal(")"));
        }

        if (stack.getMaxStackSize() > 1 && stack.getCount() > 1) {
            base.append("×").append(Component.literal(String.valueOf(stack.getCount()))).withStyle(ChatFormatting.AQUA);
        }

        return base.append(isBook ? "》" : "]");
    }

    public static ReadOnlyInventory unpackFromItemStack(ItemStack stack) {
        List<ItemStack> tmp = new ArrayList<>();
        Component itemName = StackUtils.getDisplayName(stack);

        if (StackUtils.isBundle(stack)) {
            BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (bundle != null) bundle.itemCopyStream().forEach(tmp::add);
            int rows = Math.min(6, Math.max(1, (tmp.size() + 8) / 9));
            int size = rows * 9;
            ReadOnlyInventory inv = new ReadOnlyInventory(size, itemName, handlerTypeForRows(rows));

            for (int i = 0; i < tmp.size() && i < size; i++) inv.setItem(i, tmp.get(i));
            return inv;
        }

        if (StackUtils.isShulkerBox(stack)) {
            try {
                ItemContainerContents container = stack.get(DataComponents.CONTAINER);
                if (container != null) tmp.addAll(container.allItemsCopyStream().toList());
                int size = 27;
                ReadOnlyInventory inv = new ReadOnlyInventory(size, itemName, MenuType.SHULKER_BOX);
                for (int i = 0; i < tmp.size() && i < size; i++) inv.setItem(i, tmp.get(i));
                return inv;
            } catch (RuntimeException e) {
                ShowcaseMod.LOGGER.error(e.toString());
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    public static ReadOnlyInventory snapshotFullInventory(ServerPlayer player) {
        ReadOnlyInventory inv = new ReadOnlyInventory(54, TextUtils.INVENTORY, MenuType.GENERIC_9x6);
        ItemStack playerHead = StackUtils.getPlayerHead(player);
        int level = Math.max(0, player.experienceLevel);

        inv.setItem(0, playerHead.copy());

        if (level > 0) {
            ItemStack experienceBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
            experienceBottle.set(DataComponents.CUSTOM_NAME,
                Component.translatable("showcase.screen.player_level", level));
            inv.setItem(1, experienceBottle);
        }
        
        inv.setItem(2, DIVIDER_ITEM.copy());
        inv.setItem(3, player.getItemBySlot(EquipmentSlot.HEAD).copy());
        inv.setItem(4, player.getItemBySlot(EquipmentSlot.CHEST).copy());
        inv.setItem(5, player.getItemBySlot(EquipmentSlot.LEGS).copy());
        inv.setItem(6, player.getItemBySlot(EquipmentSlot.FEET).copy());
        inv.setItem(7, DIVIDER_ITEM.copy());
        inv.setItem(8, player.getItemBySlot(EquipmentSlot.OFFHAND).copy());

        for (int i = 0; i < 9; i++) inv.setItem(i + 9, player.getInventory().getItem(i).copy());
        for (int i = 18; i < 27; i++) inv.setItem(i, DIVIDER_ITEM.copy());
        for (int i = 9; i < 36; i++) inv.setItem(i + 18, player.getInventory().getItem(i).copy());

        return inv;
    }

    public static ItemStack getPlayerHead(ServerPlayer player) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, ProfileCompat.createProfileComponent(player.getGameProfile()));
        return head;
    }

    public static boolean isValid(ItemStack stack) {
        return !(stack == null || stack.isEmpty() || stack.getCount() <= 0);
    }
}
