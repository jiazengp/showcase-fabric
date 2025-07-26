package com.showcase.utils;

import com.showcase.gui.MerchantContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.village.TradeOfferList;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static com.showcase.utils.StackUtils.isValid;

public class ContainerOpenWatcher {
    private static final Map<UUID, WatchContainer> watchContainerPending = new ConcurrentHashMap<>();
    private static final Map<UUID, WatchMerchant> watchMerchantPending = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public interface ContainerCallback {
        void onOpened(ServerPlayerEntity player, ReadOnlyInventory inventory);
    }

    public interface MerchantGuiCallback {
        void onOpened(ServerPlayerEntity player, MerchantContext merchantContext);
    }

    private record WatchContainer(ContainerCallback onSuccess, Runnable onTimeout) { }
    private record WatchMerchant(MerchantGuiCallback onSuccess, Runnable onTimeout) { }


    public static void awaitMerchantGuiOpened(ServerPlayerEntity player, long durationSeconds,
                                              MerchantGuiCallback onSuccess, Runnable onTimeout) {
        if (player == null) return;

        UUID playerId = player.getUuid();
        MinecraftServer server = player.getServer();

        if (server == null) return;

        watchMerchantPending.put(playerId, new WatchMerchant(onSuccess, onTimeout));

        scheduler.schedule(() -> server.execute(() -> {
            WatchMerchant removed = watchMerchantPending.remove(playerId);
            if (removed != null && removed.onTimeout != null) {
                removed.onTimeout.run();
            }
        }), durationSeconds, TimeUnit.SECONDS);
    }

    public static void awaitContainerOpened(ServerPlayerEntity player, long durationSeconds,
                                            ContainerCallback onSuccess, Runnable onTimeout) {
        if (player == null) return;

        UUID playerId = player.getUuid();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        watchContainerPending.put(playerId, new WatchContainer(onSuccess, onTimeout));

        scheduler.schedule(() -> server.execute(() -> {
            WatchContainer removed = watchContainerPending.remove(playerId);
            if (removed != null && removed.onTimeout != null) {
                removed.onTimeout.run();
            }
        }), durationSeconds, TimeUnit.SECONDS);
    }

    public static void onMerchantGuiOpened(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
        if (player == null) return;

        UUID playerId = player.getUuid();
        WatchMerchant entry = watchMerchantPending.remove(playerId);
        ScreenHandler handler = player.currentScreenHandler;

        if (handler == null || entry == null) return;

        MerchantContext merchantContext = getMerchantContext(handler, resolveContainerTitle(factory));

        if (merchantContext == null){
            if (entry.onTimeout != null) entry.onTimeout.run();
            return;
        }

        entry.onSuccess.onOpened(player, merchantContext);
    }

    private static MerchantContext getMerchantContext(ScreenHandler handler, Text name) {
        if (!(handler instanceof MerchantScreenHandler merchantHandler)) return null;

        try {
            TradeOfferList offers = merchantHandler.getRecipes();
            int level = merchantHandler.getExperience();
            int experience = merchantHandler.getLevelProgress();
            return new MerchantContext(
                    offers,
                    level,
                    experience,
                    merchantHandler.isLeveled(),
                    name
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static void onContainerOpened(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
        if (player == null) return;

        UUID playerId = player.getUuid();
        WatchContainer entry = watchContainerPending.remove(playerId);
        if (entry == null) return;

        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) {
            if (entry.onTimeout != null) entry.onTimeout.run();
            return;
        }

        Text name = resolveContainerTitle(factory);
        ScreenHandlerType<?> type = handler.getType() == ScreenHandlerType.CRAFTER_3X3 ? ScreenHandlerType.CRAFTING : handler.getType();
        ReadOnlyInventory tempInv;

        if (handler instanceof GenericContainerScreenHandler containerHandler) {
            tempInv = createGenericContainerInventory(containerHandler, name, type);
        }
        else {
            tempInv = createSlotBasedInventory(handler, player, name, type);
        }

        if (tempInv.isEmpty()) {
            entry.onTimeout.run();
            return;
        }

        entry.onSuccess.onOpened(player, tempInv);
    }

    private static ReadOnlyInventory createGenericContainerInventory(GenericContainerScreenHandler containerHandler,
                                                                     Text name, ScreenHandlerType<?> type) {
        Inventory containerInventory = containerHandler.getInventory();
        ReadOnlyInventory tempInv = new ReadOnlyInventory(containerInventory.size(), name, type);

        for (int i = 0; i < containerInventory.size(); i++) {
            ItemStack stack = containerInventory.getStack(i);
            tempInv.setStack(i, isValid(stack) ? stack.copy() : ItemStack.EMPTY);
        }

        return tempInv;
    }

    private static ReadOnlyInventory createSlotBasedInventory(ScreenHandler handler, ServerPlayerEntity player,
                                                              Text name, ScreenHandlerType<?> type) {
        List<Slot> nonPlayerSlots = handler.slots.stream()
                .filter(slot -> slot.inventory != player.getInventory())
                .toList();

        ReadOnlyInventory tempInv = new ReadOnlyInventory(nonPlayerSlots.size(), name, type);

        for (int i = 0; i < nonPlayerSlots.size(); i++) {
            ItemStack stack = nonPlayerSlots.get(i).getStack();
            tempInv.setStack(i, isValid(stack) ? stack.copy() : ItemStack.EMPTY);
        }

        return tempInv;
    }

    public static Text resolveContainerTitle(NamedScreenHandlerFactory factory) {
        if (factory == null) return TextUtils.UNKNOWN_ENTRY;

        Text displayName = factory.getDisplayName();

        if (displayName == null || displayName.getString().isBlank()) {
            return TextUtils.UNKNOWN_ENTRY;
        }

        TextContent content = displayName.getContent();
        String fallbackKey = null;

        if (content instanceof TranslatableTextContent translatable) {
            return Text.translatable(translatable.getKey());
        }

        if (factory instanceof NamedScreenHandlerFactory named) {
            Text originalName = named.getDisplayName();
            if (originalName != null && originalName.getContent() instanceof TranslatableTextContent translatable) {
                fallbackKey = translatable.getKey();
            }
        }

        if (fallbackKey != null) {
            return Text.literal("")
                    .append(displayName)
                    .append(Text.literal("("))
                    .append(Text.translatable(fallbackKey))
                    .append(Text.literal(")"));
        }

        return displayName;

    }

    public static void cleanup() {
        watchContainerPending.clear();
        watchMerchantPending.clear();
        scheduler.shutdown();
    }

    public static int getPendingContainerCount() {
        return watchContainerPending.size();
    }

    public static int getPendingMerchantCount() {
        return watchMerchantPending.size();
    }
}