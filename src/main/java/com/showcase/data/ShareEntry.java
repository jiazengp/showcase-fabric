package com.showcase.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.gui.MerchantContext;
import com.showcase.utils.ReadOnlyInventory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.showcase.gui.MerchantContext.MERCHANT_CONTEXT_CODEC;
import static com.showcase.utils.ReadOnlyInventory.READ_ONLY_INVENTORY_CODEC;

public class ShareEntry {
    private final UUID ownerUuid;
    private final ShowcaseManager.ShareType type;
    private final ReadOnlyInventory inventory;
    private final MerchantContext merchantContext;
    private final long timestamp;
    private final int duration;
    private int viewCount;
    private boolean isInvalid;

    public static final Codec<ShareEntry> SHARE_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("ownerUuid").forGetter(ShareEntry::getOwnerUuid),
                    Codec.STRING.xmap(ShowcaseManager.ShareType::valueOf, ShowcaseManager.ShareType::name).fieldOf("type").forGetter(ShareEntry::getType),
                    READ_ONLY_INVENTORY_CODEC.optionalFieldOf("inventory").forGetter(entry -> Optional.ofNullable(entry.getInventory())),
                    MERCHANT_CONTEXT_CODEC.optionalFieldOf("merchantContext").forGetter(entry -> Optional.ofNullable(entry.getMerchantContext())),
                    Codec.LONG.fieldOf("timestamp").forGetter(ShareEntry::getTimestamp),
                    Codec.INT.fieldOf("duration").forGetter(ShareEntry::getDuration),
                    Codec.INT.fieldOf("viewCount").forGetter(ShareEntry::getViewCount),
                    Codec.BOOL.fieldOf("isInvalid").forGetter(ShareEntry::getIsInvalid)
            ).apply(instance, (ownerUuid, type, inventoryOpt, merchantContextOpt, timestamp, duration, viewCount, isInvalid) ->
                    new ShareEntry(
                            ownerUuid,
                            type,
                            inventoryOpt.orElse(null),
                            merchantContextOpt.orElse(null),
                            timestamp,
                            duration,
                            viewCount,
                            isInvalid
                    )
            )
    );

    public ShareEntry(UUID ownerUuid, ShowcaseManager.ShareType type, ReadOnlyInventory inventory, MerchantContext merchantContext, long timestamp, int duration, int viewCount, boolean isInvalid) {
        this.ownerUuid = ownerUuid;
        this.type = type;
        this.inventory = inventory;
        this.merchantContext = merchantContext;
        this.timestamp = timestamp;
        this.duration = duration;
        this.viewCount = viewCount;
        this.isInvalid = isInvalid;
    }

    public ShareEntry(UUID ownerUuid, ShowcaseManager.ShareType type, MerchantContext merchantContext, Integer duration) {
        this(ownerUuid, type, null, merchantContext, Instant.now().toEpochMilli(), duration == null ? ModConfigManager.getShareLinkDefaultExpiry() : duration, 0, false);
    }

    public ShareEntry(UUID ownerUuid, ShowcaseManager.ShareType type, ReadOnlyInventory inventory, Integer duration) {
        this(ownerUuid, type, inventory, null, Instant.now().toEpochMilli(), duration == null ? ModConfigManager.getShareLinkDefaultExpiry() : duration, 0, false);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public ShowcaseManager.ShareType getType() {
        return type;
    }

    public ReadOnlyInventory getInventory() {
        return inventory;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getViewCount() {
        return viewCount;
    }

    public boolean getIsInvalid() { return isInvalid; }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void invalidShare() {
        this.isInvalid = true;
    }

    public MerchantContext getMerchantContext() { return this.merchantContext; }

    public int getDuration() { return duration; }
}
