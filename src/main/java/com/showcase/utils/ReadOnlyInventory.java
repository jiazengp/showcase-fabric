package com.showcase.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReadOnlyInventory extends SimpleInventory {
    public Text name;
    public ScreenHandlerType<?> type;

    public record SlotEntry(int slot, ItemStack stack) {}

    public static final Codec<SlotEntry> SLOT_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("slot").forGetter(SlotEntry::slot),
                    ItemStack.CODEC.fieldOf("stack").forGetter(SlotEntry::stack)
            ).apply(instance, SlotEntry::new)
    );

    public static final Codec<ReadOnlyInventory> READ_ONLY_INVENTORY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TextCodecs.CODEC.optionalFieldOf("name").forGetter(inv -> Optional.ofNullable(inv.name)),
                    Registries.SCREEN_HANDLER.getCodec().fieldOf("type").forGetter(inv -> inv.type),
                    Codec.INT.fieldOf("size").forGetter(ReadOnlyInventory::size),
                    Codec.list(SLOT_ENTRY_CODEC).fieldOf("items").forGetter(ReadOnlyInventory::encodeNonEmptyStacks)
            ).apply(instance, (name, type, size, slotEntries) -> {
                ReadOnlyInventory inventory = new ReadOnlyInventory(size, name.orElse(null), type);
                for (SlotEntry entry : slotEntries) {
                    if (entry.slot >= 0 && entry.slot < size) {
                        inventory.setStack(entry.slot, entry.stack());
                    }
                }
                return inventory;
            })
    );

    public ReadOnlyInventory(int size, Text name, ScreenHandlerType<?> type) {
        super(size);
        this.name = name!= null ? name : TextUtils.UNKNOWN_ENTRY;
        this.type = type;
    }

    public Text getName() {
        return name;
    }

    public ScreenHandlerType<?> getType() {
        return type;
    }

    public static List<SlotEntry> encodeNonEmptyStacks(SimpleInventory inv) {
        List<SlotEntry> result = new ArrayList<>();
        if (inv == null) return result;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack != null && !stack.isEmpty()) {
                result.add(new SlotEntry(i, stack.copy()));
            }
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clear() {}

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return false;
    }
}
