package com.showcase.command;

import com.showcase.ShowcaseMod;
import com.showcase.api.ShowcaseAPI;
import com.showcase.data.ShareEntry;
import com.showcase.data.ShareRepository;
import com.showcase.gui.ContainerGui;
import com.showcase.gui.MerchantContext;
import com.showcase.gui.ReadonlyMerchantGui;
import com.showcase.utils.*;
import com.showcase.utils.permissions.PermissionChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.showcase.command.ShowcaseManager.ShareType.ITEM;
import static com.showcase.command.ShowcaseManager.ShareType.STATS;
import static com.showcase.utils.ScreenHandlerUtils.handlerTypeForRows;
import static com.showcase.utils.StackUtils.*;

public final class ShowcaseManager {
    public enum ShareType {
        ITEM, INVENTORY, HOTBAR, ENDER_CHEST, CONTAINER, MERCHANT, STATS
    }

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Showcase‑Cleanup");
        t.setDaemon(true);
        return t;
    });

    static {
        // Schedule the recycler.
        long intervalSeconds = 60;
        SCHEDULER.scheduleAtFixedRate(ShowcaseManager::purgeExpired, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    private ShowcaseManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(Map<String, ShareEntry> share) {
        if (share == null) {
            ShowcaseMod.LOGGER.info("No saved showcase shares to load");
            return;
        }
        ShareRepository.loadShares(share);
        ShowcaseMod.LOGGER.info("Loaded {} showcase shares from storage", share.size());
    }

    private static String nextId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String createStatsShare(ServerPlayerEntity owner, ItemStack stack, Integer duration, Collection<ServerPlayerEntity> receivers) {
        String id = nextId();

        ReadOnlyInventory inv = new ReadOnlyInventory(9, StackUtils.getDisplayName(stack), ScreenHandlerType.GENERIC_9X1);
        inv.addStack(stack.copy());

        ShareRepository.store(id, new ShareEntry(owner.getUuid(), STATS, inv, duration, receivers));
        return id;
    }

    public static String createItemShare(ServerPlayerEntity owner, ItemStack stack, Integer duration, Collection<ServerPlayerEntity> receivers) {
        String id = nextId();

        ReadOnlyInventory inv = new ReadOnlyInventory(9, StackUtils.getDisplayName(stack), ScreenHandlerType.GENERIC_9X1);
        inv.addStack(stack.copy());
        for (int i = 1; i < 9; i++) inv.setStack(i, DIVIDER_ITEM);

        ShareRepository.store(id, new ShareEntry(owner.getUuid(), ITEM, inv, duration, receivers));
        return id;
    }

    public static String createInventoryShare(ServerPlayerEntity owner, Integer duration, Collection<ServerPlayerEntity> receivers) {
        String id = nextId();
        ReadOnlyInventory inv = snapshotFullInventory(owner);
        ShareRepository.store(id, new ShareEntry(owner.getUuid(), ShareType.INVENTORY, inv, duration, receivers));
        return id;
    }

    public static String createHotbarShare(ServerPlayerEntity owner, Integer duration, Collection<ServerPlayerEntity> receivers) {
        String id = nextId();
        ReadOnlyInventory inv = new HotbarSnapshotInventory(owner.getInventory());
        ShareRepository.store(id, new ShareEntry(owner.getUuid(), ShareType.HOTBAR, inv, duration, receivers));
        return id;
    }

    public static String createEnderChestShare(ServerPlayerEntity owner, Integer duration, Collection<ServerPlayerEntity> receivers) {
        String id = nextId();
        int size = owner.getEnderChestInventory().size();
        ReadOnlyInventory inv = new ReadOnlyInventory(size, TextUtils.ENDER_CHEST, handlerTypeForRows(size / 9));
        for (int i = 0; i < size; i++) {
            inv.setStack(i, owner.getEnderChestInventory().getStack(i).copy());
        }
        ShareRepository.store(id, new ShareEntry(owner.getUuid(), ShareType.ENDER_CHEST, inv, duration, receivers));
        return id;
    }

    public static String createContainerShare(ServerPlayerEntity owner, ReadOnlyInventory container, Integer duration, Collection<ServerPlayerEntity> receivers) {
        String id = nextId();
        ShareRepository.store(id, new ShareEntry(owner.getUuid(), ShareType.CONTAINER, container, duration, receivers));
        return id;
    }

    public static String createMerchantShare(ServerPlayerEntity owner, MerchantContext merchantContext, Integer duration, Collection<ServerPlayerEntity> receivers) {
        String id = nextId();
        ShareRepository.store(id, new ShareEntry(owner.getUuid(), ShareType.MERCHANT, merchantContext, duration, receivers));
        return id;
    }

    public static boolean openSharedContent(ServerPlayerEntity viewer, String id) {
        ShareEntry entry = ShareRepository.get(id);

        if (entry == null || isExpired(entry)) {
            viewer.sendMessage(TextUtils.warning(Text.translatable("showcase.message.invalid_or_expired")), false);
            ShareRepository.remove(id);
            return false;
        }

        if (!canViewShare(entry, viewer) && !viewer.getUuid().equals(entry.getOwnerUuid()) && !PermissionChecker.isOp(viewer)) {
            viewer.sendMessage(TextUtils.warning(Text.translatable("showcase.message.manage.noPermission")));
            return false;
        }

        // Get the original owner for the event
        MinecraftServer server = viewer.getServer();
        if (server == null) return false;
        
        ServerPlayerEntity originalOwner = server.getPlayerManager().getPlayer(entry.getOwnerUuid());
        // Fire the showcase viewed event
        net.minecraft.util.ActionResult result = ShowcaseAPI.fireShowcaseViewedEvent(viewer, entry, id, Objects.requireNonNullElse(originalOwner, viewer));
        if (result == net.minecraft.util.ActionResult.FAIL) return false;

        entry.incrementViewCount();

        try {
            var gui = factory(viewer, entry);
            if (gui != null) gui.open();
            return true;
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to open showcase screen", e);
            return false;
        }
    }

    public static boolean isOnCooldown(ServerPlayerEntity player, ShareType type) {
        boolean onCooldown = CooldownManager.isOnCooldown(player, type);
        if (onCooldown) {
            long remaining = CooldownManager.getRemainingCooldown(player, type);
            player.sendMessage(TextUtils.warning(Text.translatable("showcase.message.cooldown", (int) Math.ceil(remaining))));
        }
        return onCooldown;
    }

    public static boolean canViewShare(ShareEntry entry, ServerPlayerEntity viewer) {
        if (entry.getReceiverUuids() == null || entry.getReceiverUuids().isEmpty()) return true;
        return entry.getReceiverUuids().contains(viewer.getUuid());
    }

    public static void setCooldown(ServerPlayerEntity player, ShareType type) {
        CooldownManager.setCooldown(player, type);
    }

    public static boolean expireShareById(String id) {
        ShareEntry e = ShareRepository.get(id);
        if (e == null) return false;
        e.invalidShare();
        return true;
    }

    public static int expireSharesByPlayer(UUID uuid) {
        int count = 0;
        for (Map.Entry<String, ShareEntry> e : ShareRepository.getAllShares().entrySet()) {
            if (uuid.equals(e.getValue().getOwnerUuid())) {
                e.getValue().invalidShare();
                count++;
            }
        }
        return count;
    }

    public static void setShares(Map<String, ShareEntry> shares) {
        if (shares == null || shares.isEmpty()) return;

        ShareRepository.loadShares(shares);
    }

    public static ShareEntry getShareById(String id) {
        return ShareRepository.get(id);
    }

    public static Map<String, ShareEntry> getActiveShares() {
        return ShareRepository.getAllShares();
    }
    
    public static Map<String, ShareEntry> getAllActiveShares() {
        return getActiveShares();
    }

    public static Map<String, ShareEntry> getUnmodifiableActiveShares() {
        return ShareRepository.getUnmodifiableShares();
    }

    public static void clearAll() {
        ShareRepository.clear();
        CooldownManager.clearAllCooldowns();
        if (!SCHEDULER.isShutdown()) {
            SCHEDULER.shutdownNow();
        }
    }

    public static ShareEntry getShareEntry(String id) {
        return ShareRepository.get(id);
    }

    public static ItemStack getItemStackWithID(String shareId) {
        ShareEntry shareEntry = getShareEntry(shareId);
        if (shareEntry == null || shareEntry.getType() != ITEM) return null;
        return shareEntry.getInventory().getStack(0).copy();
    }

    public static List<ShareEntry> getPlayerShares(String playerUuid) {
        return ShareRepository.getAllShares().values().stream()
                .filter(entry -> entry.getOwnerUuid().toString().equals(playerUuid))
                .collect(Collectors.toList());
    }

    public static boolean cancelShare(String shareId) {
        return expireShareById(shareId);
    }

    public static long getRemainingCooldown(ServerPlayerEntity player, ShareType type) {
        return CooldownManager.getRemainingCooldown(player, type);
    }

    public static List<String> getShareIdCompletions() {
        return new ArrayList<>(getUnmodifiableActiveShares().keySet());
    }

    private static ContainerGui factory(ServerPlayerEntity viewer, ShareEntry entry) {
        ReadOnlyInventory inv = entry.getInventory();
        MinecraftServer server = viewer.getServer();

        if (server == null) return null;

        Text ownerName =  PlayerUtils.getSafeDisplayName(server, entry.getOwnerUuid());

        return switch (entry.getType()) {
            case ITEM -> {
                ItemStack itemStack = inv.getStack(0);

                if (StackUtils.isBook(itemStack)) {
                    new BookOpener(viewer, itemStack).open();
                    yield null;
                }

                if (StackUtils.isMap(itemStack)) {
                    MapViewer.viewMap(viewer, itemStack);
                    yield null;
                }

                ReadOnlyInventory unpackInv = unpackFromItemStack(itemStack);
                ReadOnlyInventory finalInv = unpackInv != null ? unpackInv : inv;
                ScreenHandlerType<?> type = finalInv.getType();
                Text name = Text.translatable("showcase.screen.item_title", ownerName, finalInv.getName());

                yield new ContainerGui(type, viewer, name, finalInv);
            }

            case STATS -> {
                ItemStack itemStack = inv.getStack(0);

                if (StackUtils.isBook(itemStack)) {
                    new BookOpener(viewer, itemStack).open();
                    yield null;
                }
                yield null;
            }

            case MERCHANT -> {
                new ReadonlyMerchantGui(viewer, entry.getMerchantContext()).open();
                yield null;
            }

            case INVENTORY -> new ContainerGui(inv.getType(), viewer,
                    Text.translatable("showcase.screen.inventory_title", ownerName), inv);
            case HOTBAR -> new ContainerGui(inv.getType(),viewer,
                    Text.translatable("showcase.screen.hotbar_title", ownerName, TextUtils.HOTBAR), inv);
            case ENDER_CHEST -> new ContainerGui(inv.getType(), viewer,
                    Text.translatable("showcase.screen.ender_chest_title", ownerName, TextUtils.ENDER_CHEST), inv);
            case CONTAINER ->  new ContainerGui(inv.getType(), viewer,
                    Text.translatable("showcase.screen.container_title", ownerName, inv.getName()), inv);
        };
    }

    private static boolean isExpired(ShareEntry e) {
        if (e == null) return true;
        return e.getIsInvalid() || Instant.now().toEpochMilli() - e.getTimestamp() > e.getDuration() * 1000L;
    }

    private static void purgeExpired() {
        long now = Instant.now().toEpochMilli();
        Set<String> toRemove = ShareRepository.getAllShares().entrySet().stream()
                .filter(e -> now - e.getValue().getTimestamp() > e.getValue().getDuration() * 1000L || e.getValue().getIsInvalid())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        toRemove.forEach(ShareRepository::remove);
    }
}
