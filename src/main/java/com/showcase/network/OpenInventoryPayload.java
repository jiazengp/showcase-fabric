package com.showcase.network;

import com.showcase.ShowcaseMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenInventoryPayload(String shareId) implements CustomPayload {
    public static final Id<OpenInventoryPayload> ID =
            new Id<>(Identifier.of(ShowcaseMod.MOD_ID, "open_inventory"));

    public static final PacketCodec<PacketByteBuf, OpenInventoryPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeString(payload.shareId, 32767),
            buf -> new OpenInventoryPayload(buf.readString(32767))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}