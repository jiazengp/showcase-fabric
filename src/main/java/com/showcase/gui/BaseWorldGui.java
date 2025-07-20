package com.showcase.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;

public abstract class BaseWorldGui extends HotbarGui {
    protected MapViewerContext context;
    private final int currentBlockClickTick;

    public BaseWorldGui(MapViewerContext context, int selectedSlot) {
        super(context.player);
        this.setSelectedSlot(selectedSlot);
        this.context = context;
        this.currentBlockClickTick = context.player.age;
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
        if (this.player.age - this.currentBlockClickTick >= 5) {
            return super.onClickBlock(hitResult);
        }
        return false;
    }

    @Override
    public void onClickItem() {
        this.checkClosed();
        if (this.player.age - this.currentBlockClickTick >= 5) {
            super.onClickItem();
        }
    }

    @Override
    public boolean onHandSwing() {
        if (this.player.age - this.currentBlockClickTick >= 5) {
            return super.onHandSwing();
        }
        return false;
    }

    protected void rebuildUi() {
        for (int i = 0; i < this.size; i++) {
            this.clearSlot(i);
        }
        this.buildUi();
        this.setSlot(8, new GuiElementBuilder()
                .model(Items.BARRIER)
                .setName(Text.translatable(context.interfaceList.isEmpty() ? "mco.notification.dismiss" : "gui.back"))
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

        this.setSlot(37, this.player.getEquippedStack(EquipmentSlot.HEAD).copy());
        this.setSlot(38, this.player.getEquippedStack(EquipmentSlot.CHEST).copy());
        this.setSlot(39, this.player.getEquippedStack(EquipmentSlot.LEGS).copy());
        this.setSlot(40, this.player.getEquippedStack(EquipmentSlot.FEET).copy());
    }

    @Override
    public void setSelectedSlot(int value) {
        this.selectedSlot = MathHelper.clamp(value, 0, 8);
    }

    protected void playClickSound() {
        this.playSound();
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        if (type == ClickType.DROP || type == ClickType.CTRL_DROP) {
            this.close();
            return true;
        }

        return super.onClick(index, type, action, element);
    }

    protected abstract void buildUi();

    protected abstract MapViewerContext.SwitchEntry asSwitchableUi();

    protected GuiElementBuilder baseElement(ItemStack itemStack, String name, boolean selected) {
        var builder = new GuiElementBuilder(itemStack);

        if (selected) {
            builder.glow();
        }

        return builder;
    }

    protected GuiElementBuilder baseElement(Item item, MutableText text, boolean selected) {
        var builder = new GuiElementBuilder()
                .model(item)
                .setName(text.formatted(Formatting.WHITE))
                .hideDefaultTooltip();

        if (selected) {
            builder.glow();
        }

        return builder;
    }

    protected GuiElementBuilder switchElement(Item item, String name, MapViewerContext.SwitchableUi ui) {
        return new GuiElementBuilder()
                .model(item)
                .setName(Text.literal("entry." + name).formatted(Formatting.WHITE))
                .hideDefaultTooltip()
                .setCallback(switchCallback(ui));
    }

    protected GuiElementInterface.ClickCallback switchCallback(MapViewerContext.SwitchableUi ui) {
        return (x, y, z, c) -> {
            this.playSound();
            this.switchUi(new MapViewerContext.SwitchEntry(ui, 0), true);
        };
    }

    public void switchUi(MapViewerContext.SwitchEntry uiOpener, boolean addSelf) {
        var context = this.context;
        if (addSelf) {
            context.interfaceList.addFirst(this.asSwitchableUi());
        }
        this.context = null;
        uiOpener.open(context);
    }

    @Override
    public void onClose() {
        if (this.context != null) {
            this.context.close();
        }
    }

    protected void playSound() {
        this.player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, this.player.getX(), this.player.getY(), this.player.getZ(), (float) 0.5, (float) 1.0, 0));
    }
}