package com.showcase.command;

import com.showcase.ShowcaseMod;
import com.showcase.data.ShareEntry;
import com.showcase.gui.ContainerGui;
import com.showcase.gui.MerchantContext;
import com.showcase.gui.ReadonlyMerchantGui;
import com.showcase.utils.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.showcase.command.ShowcaseManager.ShareType.ITEM;

public final class ShowcaseManager {
    public enum ShareType {
        ITEM, INVENTORY, HOTBAR, ENDER_CHEST, CONTAINER, MERCHANT,
    }

    private static final Map<String, ShareEntry> SHARES = new ConcurrentHashMap<>();
    private static final Map<UUID, EnumMap<ShareType, Long>> COOLDOWNS = new ConcurrentHashMap<>();
    private static final ItemStack DIVIDER_ITEM;

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Showcase‑Cleanup");
        t.setDaemon(true);
        return t;
    });

    static {
        ItemStack temp = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        temp.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⬛").formatted(Formatting.DARK_GRAY));
        DIVIDER_ITEM = temp;

        // Schedule the recycler.
        long intervalSeconds = 60;
        SCHEDULER.scheduleAtFixedRate(ShowcaseManager::purgeExpired, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    private ShowcaseManager() {}

    public static void register(Map<String, ShareEntry> share) {
        setShares(share);
    }

    private static String nextId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String createItemShare(ServerPlayerEntity owner, ItemStack stack, Integer duration) {
        String id = nextId();

        ReadOnlyInventory inv = new ReadOnlyInventory(9, StackUtils.getDisplayName(stack), ScreenHandlerType.GENERIC_9X1);
        inv.addStack(stack.copy());
        for (int i = 1; i < 9; i++) inv.setStack(i, DIVIDER_ITEM);

        SHARES.put(id, new ShareEntry(owner.getUuid(), ITEM, inv, duration));
        return id;
    }

    public static String createInventoryShare(ServerPlayerEntity owner, Integer duration) {
        String id = nextId();
        ReadOnlyInventory inv = snapshotFullInventory(owner);
        SHARES.put(id, new ShareEntry(owner.getUuid(), ShareType.INVENTORY, inv, duration));
        return id;
    }

    public static String createHotbarShare(ServerPlayerEntity owner, Integer duration) {
        String id = nextId();
        ReadOnlyInventory inv = new HotbarSnapshotInventory(owner.getInventory());
        SHARES.put(id, new ShareEntry(owner.getUuid(), ShareType.HOTBAR, inv, duration));
        return id;
    }

    public static String createEnderChestShare(ServerPlayerEntity owner, Integer duration) {
        String id = nextId();
        int size = owner.getEnderChestInventory().size();
        ReadOnlyInventory inv = new ReadOnlyInventory(size, TextUtils.ENDER_CHEST, handlerTypeForRows(size / 9));
        for (int i = 0; i < size; i++) {
            inv.setStack(i, owner.getEnderChestInventory().getStack(i).copy());
        }
        SHARES.put(id, new ShareEntry(owner.getUuid(), ShareType.ENDER_CHEST, inv, duration));
        return id;
    }

    public static String createContainerShare(ServerPlayerEntity owner, ReadOnlyInventory container, Integer duration) {
        String id = nextId();
        SHARES.put(id, new ShareEntry(owner.getUuid(), ShareType.CONTAINER, container, duration));
        return id;
    }

    public static String createMerchantShare(ServerPlayerEntity owner, MerchantContext merchantContext, Integer duration) {
        String id = nextId();
        SHARES.put(id, new ShareEntry(owner.getUuid(), ShareType.MERCHANT, merchantContext, duration));
        return id;
    }

    public static boolean openSharedContent(ServerPlayerEntity viewer, String id) {
        ShareEntry entry = SHARES.get(id);

        if (entry == null || isExpired(entry)) {
            viewer.sendMessage(TextUtils.warning(Text.translatable("showcase.message.invalid_or_expired")));
            SHARES.remove(id);
            return false;
        }

        ServerPlayerEntity owner = Objects.requireNonNull(viewer.getServer()).getPlayerManager().getPlayer(entry.getOwnerUuid());

        if (owner == null) {
            SHARES.remove(id);
            return false;
        }

        entry.incrementViewCount();

        try {
            var gui = factory(owner, viewer, entry);
            if (gui != null) gui.open();
            return true;
        } catch (Exception e) {
            ShowcaseMod.LOGGER.error("Failed to open showcase screen", e);
            return false;
        }
    }

    public static boolean isOnCooldown(ServerPlayerEntity player, ShareType type) {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) return false;
        EnumMap<ShareType, Long> map = COOLDOWNS.get(player.getUuid());
        if (map == null) return false;

        long last = map.getOrDefault(type, 0L);
        long remaining = last + ShowcaseMod.CONFIG.shareCommandCooldown * 1000L - Instant.now().toEpochMilli();
        if (remaining <= 0) return false;

        player.sendMessage(TextUtils.warning(Text.translatable("showcase.message.cooldown",  (int) Math.ceil(remaining / 1000.0))));
        return true;
    }

    public static void setCooldown(ServerPlayerEntity player, ShareType type) {
        COOLDOWNS.computeIfAbsent(player.getUuid(), k -> new EnumMap<>(ShareType.class))
                .put(type, Instant.now().toEpochMilli());
    }

    public static boolean expireShareById(String id) {
        ShareEntry e = SHARES.get(id);
        if (e == null) return false;
        e.invalidShare();
        return true;
    }

    public static int expireSharesByPlayer(UUID uuid) {
        int count = 0;
        for (Map.Entry<String, ShareEntry> e : SHARES.entrySet()) {
            if (uuid.equals(e.getValue().getOwnerUuid())) {
                e.getValue().invalidShare();
                count++;
            }
        }
        return count;
    }

    public static void setShares(Map<String, ShareEntry> shares) {
        if (shares == null || shares.isEmpty()) return;

        SHARES.clear();
        SHARES.putAll(shares);
    }

    public static ShareEntry getShareById(String id) {
        return SHARES.get(id);
    }

    public static Map<String, ShareEntry> getActiveShares() {
        return Map.copyOf(SHARES);
    }

    public static Map<String, ShareEntry> getUnmodifiableActiveShares() {
        return Collections.unmodifiableMap(SHARES);
    }

    public static void clearAll() {
        SHARES.clear();
        COOLDOWNS.clear();
        if (!SCHEDULER.isShutdown()) {
            SCHEDULER.shutdownNow();
        }
    }

    public static ShareEntry getShareEntry(String id) {
        return SHARES.get(id);
    }

    public static ItemStack getItemStackWithID(String shareId) {
        ShareEntry shareEntry = getShareEntry(shareId);
        if (shareEntry == null || shareEntry.getType() != ITEM) return null;
        return shareEntry.getInventory().getStack(0).copy();
    }

    public static List<String> getShareIdCompletions() {
        return new ArrayList<>(getUnmodifiableActiveShares().keySet());
    }

    private static ContainerGui factory(ServerPlayerEntity owner, ServerPlayerEntity viewer, ShareEntry entry) {
        ReadOnlyInventory inv = entry.getInventory();
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
                Text name = Text.translatable("showcase.screen.item_title", TextUtils.getSafeDisplayName(owner), finalInv.getName());

                yield new ContainerGui(type, viewer, name, finalInv);
            }

            case MERCHANT -> {
                new ReadonlyMerchantGui(viewer, entry.getMerchantContext()).open();
                yield null;
            }

            case INVENTORY -> new ContainerGui(inv.getType(), viewer,
                    Text.translatable("showcase.screen.inventory_title", TextUtils.getSafeDisplayName(owner)), inv);
            case HOTBAR -> new ContainerGui(inv.getType(),viewer,
                    Text.translatable("showcase.screen.hotbar_title", TextUtils.getSafeDisplayName(owner),
                            TextUtils.HOTBAR), inv);
            case ENDER_CHEST -> new ContainerGui(inv.getType(), viewer,
                    Text.translatable("showcase.screen.ender_chest_title", TextUtils.getSafeDisplayName(owner),
                            TextUtils.ENDER_CHEST), inv);
            case CONTAINER ->  new ContainerGui(inv.getType(), viewer,
                    Text.translatable("showcase.screen.container_title", TextUtils.getSafeDisplayName(owner), inv.getName()), inv);
        };
    }

    private static ReadOnlyInventory unpackFromItemStack(ItemStack stack) {
        List<ItemStack> tmp = new ArrayList<>();
        Text itemName = StackUtils.getDisplayName(stack);

        if (StackUtils.isBundle(stack)) {
            BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (bundle != null) bundle.iterateCopy().forEach(tmp::add);
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


    private static ReadOnlyInventory snapshotFullInventory(ServerPlayerEntity player) {
        ReadOnlyInventory inv = new ReadOnlyInventory(54, TextUtils.INVENTORY, ScreenHandlerType.GENERIC_9X6);
        ItemStack playerHead = StackUtils.getPlayerHead(player);

        inv.setStack(0, playerHead.copy());
        inv.setStack(1, DIVIDER_ITEM.copy());
        inv.setStack(2, player.getEquippedStack(EquipmentSlot.HEAD).copy());
        inv.setStack(3, player.getEquippedStack(EquipmentSlot.CHEST).copy());
        inv.setStack(4, player.getEquippedStack(EquipmentSlot.LEGS).copy());
        inv.setStack(5, player.getEquippedStack(EquipmentSlot.FEET).copy());
        inv.setStack(6, DIVIDER_ITEM.copy());
        inv.setStack(7, player.getEquippedStack(EquipmentSlot.OFFHAND).copy());

        for (int i = 8; i < 9; i++) inv.setStack(i, DIVIDER_ITEM.copy());
        for (int i = 0; i < 9; i++) inv.setStack(i + 9, player.getInventory().getStack(i).copy());
        for (int i = 18; i < 27; i++) inv.setStack(i, DIVIDER_ITEM.copy());
        for (int i = 9; i < 36; i++) inv.setStack(i + 18, player.getInventory().getStack(i).copy());

        return inv;
    }

    private static boolean isExpired(ShareEntry e) {
        return e.getIsInvalid() || Instant.now().toEpochMilli() - e.getTimestamp() > e.getDuration() * 1000L;
    }

    private static ScreenHandlerType<?> handlerTypeForRows(int rows) {
        return switch (Math.max(1, Math.min(rows, 6))) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> ScreenHandlerType.GENERIC_9X4;
        };
    }

    private static void purgeExpired() {
        long now = Instant.now().toEpochMilli();
        Set<String> toRemove = SHARES.entrySet().stream()
                .filter(e -> now - e.getValue().getTimestamp() > e.getValue().getDuration() * 1000L || e.getValue().getIsInvalid())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        toRemove.forEach(SHARES::remove);
    }
}
