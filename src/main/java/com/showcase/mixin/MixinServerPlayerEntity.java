package com.showcase.mixin;

import com.showcase.listener.ContainerOpenWatcher;
import com.showcase.utils.StackUtils;
import eu.pb4.sgui.api.containerwrappers.SlotBasedWrapperMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity {
    @Inject(method = "openMenu", at = @At("RETURN"))
    private void afterOpenContainer(MenuProvider factory, CallbackInfoReturnable<Integer> cir) {
        if (factory != null) {
            ServerPlayer player = (ServerPlayer)(Object) this;
            AbstractContainerMenu screenHandler = player.containerMenu;

            if (screenHandler == null) return;

            if (screenHandler instanceof MerchantMenu) {
                ContainerOpenWatcher.onMerchantGuiOpened(player, factory);
                return;
            }

            if (StackUtils.isWhitelistedContainer(screenHandler)) {
                ContainerOpenWatcher.onContainerOpened(player, factory);
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("TAIL"))
    private void ase$closeOnDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // sgui v2: GuiHelpers.getCurrentGui() removed; SlotBasedWrapperMenu is the replacement detection
        // NOTE: Scope broadened from BaseWorldGui-only to all sgui GUIs — intentional with sgui v2 migration
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.containerMenu instanceof SlotBasedWrapperMenu) {
            player.closeContainer();
        }
    }
}
