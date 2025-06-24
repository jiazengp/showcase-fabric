package com.showcase.data;

import com.showcase.command.ShowcaseManager;
import com.showcase.utils.ReadOnlyInventory;

import java.util.UUID;

public class ShareEntry {
    private final UUID ownerUuid;
    private final ShowcaseManager.ShareType type;
    private final ReadOnlyInventory inventory;
    private final long timestamp;
    private int viewCount;
    private boolean isInvalid;

    public ShareEntry(UUID ownerUuid, ShowcaseManager.ShareType type, ReadOnlyInventory inventory, long timestamp, int viewCount, boolean isInvalid) {
        this.ownerUuid = ownerUuid;
        this.type = type;
        this.inventory = inventory;
        this.timestamp = timestamp;
        this.viewCount = viewCount;
        this.isInvalid = isInvalid;
    }

    public ShareEntry(UUID ownerUuid, ShowcaseManager.ShareType type, ReadOnlyInventory inventory, long timestamp) {
        this(ownerUuid, type, inventory, timestamp, 0, false);
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

    public boolean getState() {
        return !isInvalid;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void invalidShare() {
        this.isInvalid = true;
    }
}
