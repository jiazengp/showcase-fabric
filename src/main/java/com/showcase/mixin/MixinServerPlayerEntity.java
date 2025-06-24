package com.showcase.mixin;

import com.showcase.utils.ContainerOpenWatcher;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {

    @Shadow @Final public MinecraftServer server;

    @Inject(method = "openHandledScreen", at = @At("RETURN"))
    private void afterOpenContainer(NamedScreenHandlerFactory factory, CallbackInfoReturnable<Integer> cir) {
        if (factory != null) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object) this;
            ScreenHandler screenHandler = player.currentScreenHandler;

            if (!Objects.equals(screenHandler.getClass().getSimpleName(), "ReadOnlyInventoryScreenHandler")) {
                ContainerOpenWatcher.onContainerOpened(player);
            }
        }
    }
}
