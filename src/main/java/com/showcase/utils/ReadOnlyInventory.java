package com.showcase.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ReadOnlyInventory extends SimpleContainer {
    public Component name;
    public MenuType<?> type;

    public record SlotEntry(int slot, ItemStack stack) {}

    public static final Codec<SlotEntry> SLOT_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("slot").forGetter(SlotEntry::slot),
                    ItemStack.CODEC.fieldOf("stack").forGetter(SlotEntry::stack)
            ).apply(instance, SlotEntry::new)
    );

    public static final Codec<ReadOnlyInventory> READ_ONLY_INVENTORY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(inv -> Optional.ofNullable(inv.name)),
                    BuiltInRegistries.MENU.byNameCodec().fieldOf("type").forGetter(inv -> inv.type),
                    Codec.INT.fieldOf("size").forGetter(ReadOnlyInventory::getContainerSize),
                    Codec.list(SLOT_ENTRY_CODEC).fieldOf("items").forGetter(ReadOnlyInventory::encodeNonEmptyStacks)
            ).apply(instance, (name, type, size, slotEntries) -> {
                ReadOnlyInventory inventory = new ReadOnlyInventory(size, name.orElse(null), type);
                for (SlotEntry entry : slotEntries) {
                    if (entry.slot >= 0 && entry.slot < size) {
                        inventory.setItem(entry.slot, entry.stack());
                    }
                }
                return inventory;
            })
    );

    public ReadOnlyInventory(int size, Component name, MenuType<?> type) {
        super(size);
        this.name = name!= null ? name : TextUtils.UNKNOWN_ENTRY;
        this.type = type;
    }

    public Component getName() {
        return name;
    }

    public MenuType<?> getType() {
        return type;
    }

    public static List<SlotEntry> encodeNonEmptyStacks(SimpleContainer inv) {
        List<SlotEntry> result = new ArrayList<>();
        if (inv == null) return result;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && !stack.isEmpty()) {
                result.add(new SlotEntry(i, stack.copy()));
            }
        }
        return result;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clearContent() {}

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }
}
