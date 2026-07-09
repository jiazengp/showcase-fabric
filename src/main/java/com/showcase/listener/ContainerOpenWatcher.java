package com.showcase.listener;

import com.showcase.gui.MerchantContext;
import com.showcase.utils.ContainerTitleResolver;
import com.showcase.utils.ReadOnlyInventory;
import com.showcase.utils.TextUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.showcase.utils.StackUtils.isValid;

public class ContainerOpenWatcher {
    private static final Map<UUID, WatchContainer> watchContainerPending = new ConcurrentHashMap<>();
    private static final Map<UUID, WatchMerchant> watchMerchantPending = new ConcurrentHashMap<>();

    public interface ContainerCallback {
        void onOpened(ServerPlayer player, ReadOnlyInventory inventory);
    }

    public interface MerchantGuiCallback {
        void onOpened(ServerPlayer player, MerchantContext merchantContext);
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

    public static void awaitMerchantGuiOpened(ServerPlayer player, int duration,
                                              MerchantGuiCallback onSuccess, Runnable onTimeout) {
        if (player == null) return;

        watchMerchantPending.put(player.getUUID(), new WatchMerchant(onSuccess, onTimeout, duration));
    }

    public static void awaitContainerOpened(ServerPlayer player, int duration,
                                            ContainerCallback onSuccess, Runnable onTimeout) {
        if (player == null) return;

        watchContainerPending.put(player.getUUID(), new WatchContainer(onSuccess, onTimeout, duration));
    }

    public static void onMerchantGuiOpened(ServerPlayer player, MenuProvider factory) {
        if (player == null) return;

        UUID playerId = player.getUUID();
        WatchMerchant entry = watchMerchantPending.remove(playerId);
        AbstractContainerMenu handler = player.containerMenu;

        if (handler == null || entry == null) return;

        MerchantContext merchantContext = getMerchantContext(handler, ContainerTitleResolver.resolveContainerTitle(player, handler, factory));

        if (merchantContext == null) {
            if (entry.onTimeout != null) entry.onTimeout.run();
            return;
        }

        entry.onSuccess.onOpened(player, merchantContext);
    }

    private static MerchantContext getMerchantContext(AbstractContainerMenu handler, Component name) {
        if (!(handler instanceof MerchantMenu merchantHandler)) return null;

        try {
            MerchantOffers offers = merchantHandler.getOffers();
            int level = merchantHandler.getTraderLevel();
            int experience = merchantHandler.getTraderXp();
            return new MerchantContext(
                    offers,
                    level,
                    experience,
                    merchantHandler.showProgressBar(),
                    name
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static void onContainerOpened(ServerPlayer player, MenuProvider factory) {
        if (player == null) return;

        UUID playerId = player.getUUID();
        WatchContainer entry = watchContainerPending.remove(playerId);
        if (entry == null) return;

        AbstractContainerMenu handler = player.containerMenu;
        if (handler == null) {
            if (entry.onTimeout != null) entry.onTimeout.run();
            return;
        }

        // Use the new resolver to get accurate container title
        Component name = ContainerTitleResolver.resolveContainerTitle(player, handler, factory);
        MenuType<?> type = handler.getType() == MenuType.CRAFTER_3x3 ? MenuType.GENERIC_3x3 : handler.getType();
        ReadOnlyInventory tempInv;

        if (handler instanceof ChestMenu containerHandler) {
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

    private static ReadOnlyInventory createGenericContainerInventory(ChestMenu containerHandler,
                                                                     Component name, MenuType<?> type) {
        Container containerInventory = containerHandler.getContainer();
        ReadOnlyInventory tempInv = new ReadOnlyInventory(containerInventory.getContainerSize(), name, type);

        for (int i = 0; i < containerInventory.getContainerSize(); i++) {
            ItemStack stack = containerInventory.getItem(i);
            tempInv.setItem(i, isValid(stack) ? stack.copy() : ItemStack.EMPTY);
        }

        return tempInv;
    }

    private static ReadOnlyInventory createSlotBasedInventory(AbstractContainerMenu handler, ServerPlayer player,
                                                              Component name, MenuType<?> type) {
        if (handler instanceof CrafterMenu crafterMenu) {
            return createCrafterInventory(crafterMenu, name);
        }

        List<Slot> nonPlayerSlots = handler.slots.stream()
                .filter(slot -> slot.container != player.getInventory())
                .toList();

        ReadOnlyInventory tempInv = new ReadOnlyInventory(nonPlayerSlots.size(), name, type);

        for (int i = 0; i < nonPlayerSlots.size(); i++) {
            ItemStack stack = nonPlayerSlots.get(i).getItem();
            tempInv.setItem(i, isValid(stack) ? stack.copy() : ItemStack.EMPTY);
        }

        return tempInv;
    }

    private static ReadOnlyInventory createCrafterInventory(CrafterMenu crafterMenu, Component name) {
        Container container = crafterMenu.getContainer();
        ReadOnlyInventory tempInv = new ReadOnlyInventory(9, name, MenuType.GENERIC_3x3);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = container.getItem(i);
            tempInv.setItem(i, isValid(stack) ? stack.copy() : ItemStack.EMPTY);
        }

        return tempInv;
    }

    /**
     * @deprecated Use ContainerTitleResolver.resolveContainerTitle() instead
     */
    @Deprecated
    public static Component resolveContainerTitle(MenuProvider factory) {
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
