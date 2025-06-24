package com.showcase.utils;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
public class ContainerOpenWatcher {

    private static final Map<UUID, WatchEntry> pending = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public interface ContainerCallback {
        void onOpened(ServerPlayerEntity player, ReadOnlyInventory inventory);
    }

    private record WatchEntry(ContainerCallback onSuccess, Runnable onTimeout) {
    }

    public static void awaitWithItems(ServerPlayerEntity player, long durationSeconds, ContainerCallback onSuccess, Runnable onTimeout) {
        UUID playerId = player.getUuid();
        MinecraftServer server = player.getServer();
        if (server == null) {
            onTimeout.run();
            return;
        }

        pending.put(playerId, new WatchEntry(onSuccess, onTimeout));
        scheduler.schedule(() -> server.execute(() -> {
            if (pending.remove(playerId) != null) {
                onTimeout.run();
            }
        }), durationSeconds, TimeUnit.SECONDS);
    }

    public static void onContainerOpened(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        WatchEntry entry = pending.remove(playerId);
        if (entry == null) return;

        ScreenHandler handler = player.currentScreenHandler;
        String name = getVanillaContainerTranslationKey(handler);
        ScreenHandlerType<?> type = handler.getType();
        ReadOnlyInventory tempInv;

        if (handler instanceof GenericContainerScreenHandler containerHandler) {
            Inventory containerInventory = containerHandler.getInventory();
            tempInv = new ReadOnlyInventory(containerInventory.size(), name, type);

            for (int i = 0; i < containerInventory.size(); i++) {
                ItemStack stack = containerInventory.getStack(i);
                tempInv.setStack(i, stack.copy());
            }

        } else {
            List<Slot> nonPlayerSlots = handler.slots.stream()
                    .filter(slot -> slot.inventory != player.getInventory())
                    .toList();

            tempInv = new ReadOnlyInventory(nonPlayerSlots.size(), name, type);

            for (int i = 0; i < nonPlayerSlots.size(); i++) {
                ItemStack stack = nonPlayerSlots.get(i).getStack();
                tempInv.setStack(i, stack.copy());
            }
        }
        entry.onSuccess.onOpened(player, tempInv);
    }

    public static String getVanillaContainerTranslationKey(ScreenHandler handler) {
        if (handler == null) return "";

        String name = handler.getClass().getSimpleName();

        return switch (name) {
            case "GenericContainerScreenHandler" -> "container.chest";
            case "FurnaceScreenHandler"         -> "container.furnace";
            case "BlastFurnaceScreenHandler"    -> "container.blast_furnace";
            case "SmokerScreenHandler"          -> "container.smoker";
            case "HopperScreenHandler"          -> "container.hopper";
            case "DispenserScreenHandler"       -> "container.dispenser";
            case "DropperScreenHandler"         -> "container.dropper";
            case "CraftingScreenHandler"        -> "container.crafting";
            case "AnvilScreenHandler"           -> "container.repair";
            case "EnchantmentScreenHandler"     -> "container.enchant";
            case "GrindstoneScreenHandler"      -> "container.grindstone";
            case "LoomScreenHandler"            -> "container.loom";
            case "StonecutterScreenHandler"     -> "container.stonecutter";
            case "CartographyTableScreenHandler"-> "container.cartography_table";
            case "SmithingScreenHandler"        -> "container.upgrade";
            case "ShulkerBoxScreenHandler"      -> "container.shulkerBox";
            case "HorseScreenHandler"           -> "container.horse";
            case "BeaconScreenHandler"          -> "container.beacon";
            case "BrewingStandScreenHandler"    -> "container.brewing";
            case "VillagerScreenHandler"        -> "container.villager";
            case "MerchantScreenHandler"        -> "container.merchant";
            case "InventoryScreenHandler"       -> "container.inventory";
            case "LecternScreenHandler"         -> "container.lectern";
            default -> name;
        };
    }
}
