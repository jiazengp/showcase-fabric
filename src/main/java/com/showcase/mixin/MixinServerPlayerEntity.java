package com.showcase.mixin;

import com.showcase.gui.BaseWorldGui;
import com.showcase.listener.ContainerOpenWatcher;
import com.showcase.utils.StackUtils;
import eu.pb4.sgui.api.GuiHelpers;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {
    @Inject(method = "openHandledScreen", at = @At("RETURN"))
    private void afterOpenContainer(NamedScreenHandlerFactory factory, CallbackInfoReturnable<Integer> cir) {
        if (factory != null) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object) this;
            ScreenHandler screenHandler = player.currentScreenHandler;

            if (screenHandler == null) return;

            if (screenHandler instanceof MerchantScreenHandler) {
                ContainerOpenWatcher.onMerchantGuiOpened(player, factory);
                return;
            }

            if (StackUtils.isWhitelistedContainer(screenHandler)) {
                ContainerOpenWatcher.onContainerOpened(player, factory);
            }
        }
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void ase$closeOnDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (GuiHelpers.getCurrentGui((ServerPlayerEntity) (Object) this) instanceof BaseWorldGui baseGui) {
            baseGui.close();
        }
    }
}
