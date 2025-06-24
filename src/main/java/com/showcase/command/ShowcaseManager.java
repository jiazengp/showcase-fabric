package com.showcase.command;

import com.showcase.ShowcaseMod;
import com.showcase.data.ShareEntry;
import com.showcase.screen.ReadOnlyFactory;
import com.showcase.utils.HotbarSnapshotInventory;
import com.showcase.utils.ItemStackUtils;
import com.showcase.utils.MessageUtils;
import com.showcase.utils.ReadOnlyInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShowcaseManager {

    public enum ShareType {
        ITEM, INVENTORY, HOTBAR, ENDER_CHEST, CONTAINER
    }

    private static final Map<String, ShareEntry> SHARED_ITEMS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<ShareType, Long>> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Timer CLEANUP_TIMER = new Timer("ShareManager-Cleanup", true);

    static {
        CLEANUP_TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                SHARED_ITEMS.entrySet().removeIf(entry ->
                        now - entry.getValue().getTimestamp() > ShowcaseMod.CONFIG.shareLinkExpiryTime * 1000L);
            }
        }, 60_000, 60_000);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String createContainerShare(ServerPlayerEntity player, ReadOnlyInventory inventory) {
        String shareId = generateUUID();

        SHARED_ITEMS.put(shareId,
                new ShareEntry(
                        player.getUuid(),
                        ShareType.CONTAINER,
                        inventory,
                        System.currentTimeMillis()
                )
        );
        return shareId;
    }

    public static Text getContainerPreview(String shareId) {
        ShareEntry shared = SHARED_ITEMS.get(shareId);
        if (shared == null) return null;

        Inventory inventory = shared.getInventory();
        Map<Text, Integer> itemCounts = new HashMap<>();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                Text name = stack.getName();
                itemCounts.put(name, itemCounts.getOrDefault(name, 0) + stack.getCount());
            }
        }

        if (itemCounts.isEmpty()) {
            return Text.translatable("item.minecraft.bundle.empty");
        }

        MutableText result = Text.literal("");

        itemCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .forEach(entry -> {
                    Text line = Text.literal("")
                            .append(entry.getKey())
                            .append(Text.literal(" x" + entry.getValue() + "\n"));
                    result.append(line);
                });

        if (itemCounts.size() > 5) {
            int more = itemCounts.size() - 5;
            result.append(Text.translatable("container.shulkerBox.more", more));
        }

        return result;
    }

    public static String createItemShare(ServerPlayerEntity player, ItemStack stack) {
        String shareId = generateUUID();
        ReadOnlyInventory tempInv = new ReadOnlyInventory(9, getDisplayName(stack), ScreenHandlerType.GENERIC_9X1);
        tempInv.addStack(stack);
        SHARED_ITEMS.put(shareId, new ShareEntry(player.getUuid(), ShareType.ITEM, tempInv, System.currentTimeMillis()));
        return shareId;
    }

    public static String createHotbarShare(ServerPlayerEntity player) {
        String shareId = generateUUID();
        ReadOnlyInventory hotbarSnapshot = new HotbarSnapshotInventory(player.getInventory());

        SHARED_ITEMS.put(shareId, new ShareEntry(player.getUuid(), ShareType.HOTBAR, hotbarSnapshot, System.currentTimeMillis()));
        return shareId;
    }

    public static String createInventoryShare(ServerPlayerEntity player) {
        String shareId = generateUUID();
        int size = 45;
        ReadOnlyInventory tempInv = new ReadOnlyInventory(size, "", ScreenHandlerType.GENERIC_9X5);
        ItemStack divider = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            tempInv.setStack(i, stack.copy());
        }

        for (int i = 9; i < 18; i++) {
            tempInv.setStack(i, divider.copy());
        }

        for (int i = 9; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            tempInv.setStack(i + 9, stack.copy());
        }

        SHARED_ITEMS.put(shareId, new ShareEntry(player.getUuid(), ShareType.INVENTORY, tempInv, System.currentTimeMillis()));
        return shareId;
    }

    public static String createEnderChestShare(ServerPlayerEntity player) {
        String shareId = generateUUID();
        int size = player.getEnderChestInventory().size();
        ReadOnlyInventory tempInv = new ReadOnlyInventory(size, "", determineHandlerType(size));

        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getEnderChestInventory().getStack(i);
            tempInv.setStack(i, stack.copy());
        }

        SHARED_ITEMS.put(shareId, new ShareEntry(player.getUuid(), ShareType.ENDER_CHEST, tempInv, System.currentTimeMillis()));
        return shareId;
    }

    public static boolean openSharedContent(ServerPlayerEntity viewer, String shareId) {
        ShareEntry entry = SHARED_ITEMS.get(shareId);
        if (entry == null) return false;

        if (!entry.getState()) {
            viewer.sendMessage(Text.of(ShowcaseMod.CONFIG.messages.invalidOrExpiredLinkTips));
            SHARED_ITEMS.remove(shareId);
            return false;
        }
        if (System.currentTimeMillis() - entry.getTimestamp() > ShowcaseMod.CONFIG.shareLinkExpiryTime * 1000L) {
            SHARED_ITEMS.remove(shareId);
            return false;
        }

        ServerPlayerEntity owner = Objects.requireNonNull(viewer.getServer()).getPlayerManager().getPlayer(entry.getOwnerUuid());

        if (owner == null) {
            SHARED_ITEMS.remove(shareId);
            return false;
        }

        try {
            entry.incrementViewCount();
            switch (entry.getType()) {
                case ITEM:
                    viewer.openHandledScreen(createSingleItemScreen(owner, entry.getInventory()));
                    break;
                case INVENTORY:
                    viewer.openHandledScreen(createInventoryScreen(owner, entry.getInventory()));
                    break;
                case ENDER_CHEST:
                    viewer.openHandledScreen(createEnderChestScreen(owner, entry.getInventory()));
                    break;
                case HOTBAR:
                    viewer.openHandledScreen(createHotBarScreen(owner, entry.getInventory()));
                    break;
                case CONTAINER:
                    viewer.openHandledScreen(createContainerScreen(owner, entry.getInventory()));
            }
        } catch (RuntimeException e) {
            ShowcaseMod.LOGGER.error("Can't open handled screen", e);
            throw new RuntimeException(e);
        }
        return true;
    }
    public static boolean isOnCooldown(ServerPlayerEntity player, ShareType type) {
        Map<ShareType, Long> playerCooldowns = COOLDOWNS.getOrDefault(player.getUuid(), new HashMap<>());
        Long lastUsed = playerCooldowns.get(type);
        if (lastUsed == null) return false;

        long cooldownTime = ShowcaseMod.CONFIG.shareCommandCooldown * 1000L;
        long elapsed = System.currentTimeMillis() - lastUsed;

        if (elapsed < cooldownTime) {
            long remainingMs = cooldownTime - elapsed;
            double remainingSec = remainingMs / 1000.0;

            String message = String.format(ShowcaseMod.CONFIG.messages.cooldown, (int)Math.ceil(remainingSec));
            player.sendMessage(Text.literal(message).formatted(Formatting.RED));

            return true;
        }

        return false;
    }

    public static void setCooldown(ServerPlayerEntity player, ShareType type) {
        Map<ShareType, Long> playerCooldowns = COOLDOWNS.computeIfAbsent(player.getUuid(), k -> new HashMap<>());
        playerCooldowns.put(type, System.currentTimeMillis());
    }

    public static boolean expireShareById(String shareId) {
        ShareEntry entry = SHARED_ITEMS.get(shareId);

        if (entry == null) return false;

        entry.invalidShare();
        return true;
    }

    public static int expireSharesByPlayer(UUID playerUuid) {
        int count = 0;
        for (Map.Entry<String, ShareEntry> entry : SHARED_ITEMS.entrySet()) {
            if (entry.getValue().getOwnerUuid().equals(playerUuid)) {
                expireShareById(entry.getKey());
                count++;
            }
        }
        return count;
    }

    public static Text getDisplayName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Text.translatable("commands.banlist.entry.unknown");
        return stack.getName().copy().append("×").append(Text.literal(String.valueOf(stack.getCount())));
    }

    private static NamedScreenHandlerFactory createContainerScreen(ServerPlayerEntity owner, ReadOnlyInventory inventory) {
        return new ReadOnlyFactory(inventory.getType(), owner.getInventory(), inventory, MessageUtils.formatPlayerMessage(
                ShowcaseMod.CONFIG.messages.containerTitle,
                owner,
                inventory.getName())
        );
    }

    private static NamedScreenHandlerFactory createSingleItemScreen(ServerPlayerEntity owner, ReadOnlyInventory inventory) {
        return new ReadOnlyFactory(inventory.getType(), owner.getInventory(), inventory, MessageUtils.formatPlayerMessage(ShowcaseMod.CONFIG.messages.itemTitle, owner, inventory.getName()));
    }

    private static NamedScreenHandlerFactory createInventoryScreen(ServerPlayerEntity owner, ReadOnlyInventory inventory) {
        return new ReadOnlyFactory(inventory.getType(), owner.getInventory(), inventory, MessageUtils.formatPlayerMessage(
                ShowcaseMod.CONFIG.messages.inventoryTitle,
                owner,
                Text.translatable("itemGroup.inventory")));
    }

    private static NamedScreenHandlerFactory createHotBarScreen(ServerPlayerEntity owner, ReadOnlyInventory inventory) {
        return new ReadOnlyFactory(inventory.getType(), owner.getInventory(), inventory, MessageUtils.formatPlayerMessage(
                ShowcaseMod.CONFIG.messages.hotBarTitle,
                owner,
                Text.translatable("itemGroup.hotbar"))
        );
    }

    private static NamedScreenHandlerFactory createEnderChestScreen(ServerPlayerEntity owner, ReadOnlyInventory inventory) {
        return new ReadOnlyFactory(inventory.getType(), owner.getInventory(), inventory, MessageUtils.formatPlayerMessage(
                ShowcaseMod.CONFIG.messages.enderChestTitle,
                owner,
                Text.translatable("container.enderchest"))
        );
    }

    private static ScreenHandlerType<?> determineHandlerType(int size) {
         return switch ((int) Math.clamp((double) (size / 9), 1, 6)) {
                    case 1 -> ScreenHandlerType.GENERIC_9X1;
                    case 2 -> ScreenHandlerType.GENERIC_9X2;
                    case 3 -> ScreenHandlerType.GENERIC_9X3;
                    case 5 -> ScreenHandlerType.GENERIC_9X5;
                    case 6 -> ScreenHandlerType.GENERIC_9X6;
                    default -> ScreenHandlerType.GENERIC_9X4;
                };
    }

    public static void clearAll() {
        SHARED_ITEMS.clear();
        COOLDOWNS.clear();
        CLEANUP_TIMER.cancel();
    }

    public static Map<String, ShareEntry> getActiveShares() {
        return new HashMap<>(SHARED_ITEMS);
    }
}