package com.showcase.listener;

import com.showcase.gui.MerchantContext;
import com.showcase.utils.ContainerTitleResolver;
import com.showcase.utils.ReadOnlyInventory;
import com.showcase.utils.TextUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOfferList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.showcase.utils.StackUtils.isValid;

public class ContainerOpenWatcher {
    private static final Map<UUID, WatchContainer> watchContainerPending = new ConcurrentHashMap<>();
    private static final Map<UUID, WatchMerchant> watchMerchantPending = new ConcurrentHashMap<>();

    public interface ContainerCallback {
        void onOpened(ServerPlayerEntity player, ReadOnlyInventory inventory);
    }

    public interface MerchantGuiCallback {
        void onOpened(ServerPlayerEntity player, MerchantContext merchantContext);
    }

    private static class WatchContainer {
        ContainerCallback onSuccess;
        Runnable onTimeout;
        int ticksLeft;

        WatchContainer(ContainerCallback onSuccess, Runnable onTimeout, int ticksLeft) {
            this.onSuccess = onSuccess;
            this.onTimeout = onTimeout;
            this.ticksLeft = ticksLeft * 20;
        }
    }

    private static class WatchMerchant {
        MerchantGuiCallback onSuccess;
        Runnable onTimeout;
        int ticksLeft;

        WatchMerchant(MerchantGuiCallback onSuccess, Runnable onTimeout, int ticksLeft) {
            this.onSuccess = onSuccess;
            this.onTimeout = onTimeout;
            this.ticksLeft = ticksLeft * 20;
        }
    }

    public static void awaitMerchantGuiOpened(ServerPlayerEntity player, int duration,
                                              MerchantGuiCallback onSuccess, Runnable onTimeout) {
        if (player == null) return;

        watchMerchantPending.put(player.getUuid(), new WatchMerchant(onSuccess, onTimeout, duration));
    }

    public static void awaitContainerOpened(ServerPlayerEntity player, int duration,
                                            ContainerCallback onSuccess, Runnable onTimeout) {
        if (player == null) return;

        watchContainerPending.put(player.getUuid(), new WatchContainer(onSuccess, onTimeout, duration));
    }

    public static void onMerchantGuiOpened(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
        if (player == null) return;

        UUID playerId = player.getUuid();
        WatchMerchant entry = watchMerchantPending.remove(playerId);
        ScreenHandler handler = player.currentScreenHandler;

        if (handler == null || entry == null) return;

        MerchantContext merchantContext = getMerchantContext(handler, ContainerTitleResolver.resolveContainerTitle(player, handler, factory));

        if (merchantContext == null) {
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

        // Use the new resolver to get accurate container title
        Text name = ContainerTitleResolver.resolveContainerTitle(player, handler, factory);
        ScreenHandlerType<?> type = handler.getType() == ScreenHandlerType.CRAFTER_3X3 ? ScreenHandlerType.CRAFTING : handler.getType();
        ReadOnlyInventory tempInv;

        if (handler instanceof GenericContainerScreenHandler containerHandler) {
            tempInv = createGenericContainerInventory(containerHandler, name, type);
        } else {
            tempInv = createSlotBasedInventory(handler, player, name, type);
        }

        if (tempInv.isEmpty()) {
            if (entry.onTimeout != null) entry.onTimeout.run();
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

    /**
     * @deprecated Use ContainerTitleResolver.resolveContainerTitle() instead
     */
    @Deprecated
    public static Text resolveContainerTitle(NamedScreenHandlerFactory factory) {
        return ContainerTitleResolver.resolveContainerTitle(factory);
    }

    public static void registerTickEvent() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, WatchContainer>> containerIter = watchContainerPending.entrySet().iterator();
            while (containerIter.hasNext()) {
                Map.Entry<UUID, WatchContainer> entry = containerIter.next();
                WatchContainer wc = entry.getValue();
                wc.ticksLeft--;
                if (wc.ticksLeft <= 0) {
                    containerIter.remove();
                    if (wc.onTimeout != null) wc.onTimeout.run();
                }
            }

            Iterator<Map.Entry<UUID, WatchMerchant>> merchantIter = watchMerchantPending.entrySet().iterator();
            while (merchantIter.hasNext()) {
                Map.Entry<UUID, WatchMerchant> entry = merchantIter.next();
                WatchMerchant wm = entry.getValue();
                wm.ticksLeft--;
                if (wm.ticksLeft <= 0) {
                    merchantIter.remove();
                    if (wm.onTimeout != null) wm.onTimeout.run();
                }
            }
        });
    }

    public static void cleanup() {
        watchContainerPending.clear();
        watchMerchantPending.clear();
    }

    public static int getPendingContainerCount() {
        return watchContainerPending.size();
    }

    public static int getPendingMerchantCount() {
        return watchMerchantPending.size();
    }
}
