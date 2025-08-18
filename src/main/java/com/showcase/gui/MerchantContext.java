package com.showcase.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.village.TradeOfferList;


public final class MerchantContext {
    private final TradeOfferList offers;
    private final int level;
    private final int experience;
    private final boolean isLeveled;
    private final Text displayName;

    public static final Codec<MerchantContext> MERCHANT_CONTEXT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TradeOfferList.CODEC.fieldOf("offers").forGetter(MerchantContext::getOffers),
                    Codec.INT.fieldOf("level").forGetter(MerchantContext::getLevel),
                    Codec.INT.fieldOf("experience").forGetter(MerchantContext::getExperience),
                    Codec.BOOL.fieldOf("isLeveled").forGetter(MerchantContext::isLeveled),
                    TextCodecs.CODEC.fieldOf("displayName").forGetter(MerchantContext::getDisplayName)
            ).apply(instance, MerchantContext::new)
    );

    public MerchantContext(VillagerEntity villager) {
        this.offers = villager.getOffers() != null ? villager.getOffers() : new TradeOfferList();
        this.level = villager.getVillagerData().getLevel();
        this.experience = villager.getExperience();
        this.isLeveled = true;
        this.displayName = villager.getDisplayName() != null ? villager.getDisplayName() : Text.empty();
    }

    public MerchantContext(TradeOfferList offers, int level, int experience, boolean isLeveled, Text displayName) {
        this.offers = offers;
        this.level = level;
        this.experience = experience;
        this.isLeveled = isLeveled;
        this.displayName = displayName;
    }

    public MutableText getLevelText() {
        return Text.translatable("merchant.level." + Math.max(1, Math.min(5, level)));
    }

    public Text getFullDisplayName() {
        if (!isLeveled) return getDisplayName();
        return Text.translatable("merchant.title", getDisplayName(), getLevelText());
    }

    public TradeOfferList getOffers() { return offers; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public boolean isLeveled() { return isLeveled; }
    public Text getDisplayName() { return displayName; }
}