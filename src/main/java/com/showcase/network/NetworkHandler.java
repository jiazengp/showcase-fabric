package com.showcase.network;

import com.showcase.command.ShowcaseManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class NetworkHandler {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenInventoryPayload.ID, OpenInventoryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenInventoryPayload.ID, OpenInventoryPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                OpenInventoryPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    Objects.requireNonNull(player.getServer()).execute(() ->
                            ShowcaseManager.openSharedContent(player, payload.shareId())
                    );
                }
        );
    }

    public static void sendShareRequest(ServerPlayerEntity player, String shareId) {
        ServerPlayNetworking.send(player, new OpenInventoryPayload(shareId));
    }
}