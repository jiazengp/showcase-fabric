package com.showcase.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseWorldGui extends HotbarGui {
    protected BaseViewerContext context;
    private final int currentBlockClickTick;

    public BaseWorldGui(BaseViewerContext context, int selectedSlot) {
        super(context.player);
        this.setSelectedSlot(selectedSlot);
        this.context = context;
        this.currentBlockClickTick = context.player.tickCount;
    }

    @Override
    public void onTick() {
        this.checkClosed();
        super.onTick();
    }

    private void checkClosed() {
        if (this.context.checkClosed()) {
            this.close();
        }
    }

    @Override
    public boolean onClickBlock(BlockHitResult hitResult) {
        this.checkClosed();
        if (this.player.tickCount - this.currentBlockClickTick >= 5) {
            return super.onClickBlock(hitResult);
        }
        return false;
    }

    @Override
    public void onClickItem() {
        this.checkClosed();
        if (this.player.tickCount - this.currentBlockClickTick >= 5) {
            super.onClickItem();
        }
    }

    @Override
    public boolean onHandSwing() {
        if (this.player.tickCount - this.currentBlockClickTick >= 5) {
            return super.onHandSwing();
        }
        return false;
    }

    protected void rebuildUi() {
        for (int i = 0; i < this.size; i++) {
            this.clearSlot(i);
        }
        this.buildUi();
        this.setSlot(8, com.showcase.utils.compat.GuiElementBuilderCompat.setModel(new GuiElementBuilder(), Items.BARRIER)
                .setName(Component.translatable(context.interfaceList.isEmpty() ? "mco.notification.dismiss" : "gui.back"))
                .setRarity(Rarity.COMMON)
                .hideDefaultTooltip()
                .setCallback((x, y, z, c) -> {
                    this.playClickSound();
                    if (this.context == null || this.context.interfaceList.isEmpty()) {
                        this.close();
                    } else {
                        this.switchUi(this.context.interfaceList.removeFirst(), false);
                    }
                })
        );

        this.setSlot(37, this.player.getItemBySlot(EquipmentSlot.HEAD).copy());
        this.setSlot(38, this.player.getItemBySlot(EquipmentSlot.CHEST).copy());
        this.setSlot(39, this.player.getItemBySlot(EquipmentSlot.LEGS).copy());
        this.setSlot(40, this.player.getItemBySlot(EquipmentSlot.FEET).copy());
    }

    @Override
    public void setSelectedSlot(int value) {
        this.selectedSlot = Mth.clamp(value, 0, 8);
    }

    protected void playClickSound() {
        this.playSound();
    }

    @Override
    public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
        if (type == ClickType.DROP || type == ClickType.CTRL_DROP) {
            this.close();
            return true;
        }

        return super.onClick(index, type, action, element);
    }

    protected abstract void buildUi();

    protected abstract BaseViewerContext.SwitchEntry asSwitchableUi();

    protected GuiElementBuilder baseElement(ItemStack itemStack, String name, boolean selected) {
        var builder = new GuiElementBuilder(itemStack);

        if (selected) {
            builder.glow();
        }

        return builder;
    }

    protected GuiElementBuilder baseElement(Item item, MutableComponent text, boolean selected) {
        var builder = com.showcase.utils.compat.GuiElementBuilderCompat.setModel(new GuiElementBuilder(), item)
                .setName(text.withStyle(ChatFormatting.WHITE))
                .hideDefaultTooltip();

        if (selected) {
            builder.glow();
        }

        return builder;
    }

    protected GuiElementBuilder switchElement(Item item, String name, BaseViewerContext.SwitchableUi ui) {
        return com.showcase.utils.compat.GuiElementBuilderCompat.setModel(new GuiElementBuilder(), item)
                .setName(Component.literal("entry." + name).withStyle(ChatFormatting.WHITE))
                .hideDefaultTooltip()
                .setCallback(switchCallback(ui));
    }

    protected GuiElement.ClickCallback switchCallback(BaseViewerContext.SwitchableUi ui) {
        return (x, y, z, c) -> {
            this.playSound();
            this.switchUi(new BaseViewerContext.SwitchEntry(ui, 0), true);
        };
    }

    public void switchUi(BaseViewerContext.SwitchEntry uiOpener, boolean addSelf) {
        var context = this.context;
        if (addSelf) {
            context.interfaceList.addFirst(this.asSwitchableUi());
        }
        this.context = null;
        uiOpener.open(context);
    }

    @Override
    public void close() {
        // A hotbar viewer has no container screen, so sgui never reports a container close for a Q/drop
        // exit. Tearing the viewer context down here is the only place that catches every exit path.
        // switchUi() detaches the context before opening the replacement UI, so switching is unaffected.
        if (this.context != null) {
            this.context.close();
        }
        super.close();
    }

    @Override
    public void onPlayerClose(boolean ignored) {
        if (this.context != null) {
            this.context.close();
        }
    }

    protected void playSound() {
        this.player.connection.send(new ClientboundSoundPacket(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, this.player.getX(), this.player.getY(), this.player.getZ(), (float) 0.5, (float) 1.0, 0));
    }
}
