package com.showcase.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.showcase.utils.compat.VillagerCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffers;


public final class MerchantContext {
    private final MerchantOffers offers;
    private final int level;
    private final int experience;
    private final boolean isLeveled;
    private final Component displayName;

    public static final Codec<MerchantContext> MERCHANT_CONTEXT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MerchantOffers.CODEC.fieldOf("offers").forGetter(MerchantContext::getOffers),
                    Codec.INT.fieldOf("level").forGetter(MerchantContext::getLevel),
                    Codec.INT.fieldOf("experience").forGetter(MerchantContext::getExperience),
                    Codec.BOOL.fieldOf("isLeveled").forGetter(MerchantContext::isLeveled),
                    ComponentSerialization.CODEC.fieldOf("displayName").forGetter(MerchantContext::getDisplayName)
            ).apply(instance, MerchantContext::new)
    );

    public MerchantContext(Villager villager) {
        this.offers = villager.getOffers() != null ? villager.getOffers() : new MerchantOffers();
        this.level = VillagerCompat.getLevel(villager.getVillagerData());
        this.experience = villager.getVillagerXp();
        this.isLeveled = true;
        this.displayName = villager.getDisplayName() != null ? villager.getDisplayName() : Component.empty();
    }

    public MerchantContext(MerchantOffers offers, int level, int experience, boolean isLeveled, Component displayName) {
        this.offers = offers;
        this.level = level;
        this.experience = experience;
        this.isLeveled = isLeveled;
        this.displayName = displayName;
    }

    public MutableComponent getLevelText() {
        return Component.translatable("merchant.level." + Math.max(1, Math.min(5, level)));
    }

    public Component getFullDisplayName() {
        if (!isLeveled) return getDisplayName();
        return Component.translatable("merchant.title", getDisplayName(), getLevelText());
    }

    public MerchantOffers getOffers() { return offers; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public boolean isLeveled() { return isLeveled; }
    public Component getDisplayName() { return displayName; }
}