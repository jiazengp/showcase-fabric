package com.showcase.gui;

import eu.pb4.sgui.api.gui.MerchantGui;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;

public class ReadonlyMerchantGui extends MerchantGui {
    protected MerchantContext context;

    public ReadonlyMerchantGui(ServerPlayerEntity player, MerchantContext tradeContext) {
        super(player, false);
        this.context = tradeContext;

        this.setTitle(tradeContext.getDisplayName());

        if (tradeContext.isLeveled()) {
            this.setIsLeveled(true);

            if (tradeContext.getLevel() >= 0 && tradeContext.getLevel() < VillagerLevel.values().length) {
                this.setLevel(VillagerLevel.values()[tradeContext.getLevel()]);
            }

            this.setExperience(tradeContext.getExperience());
        }

        for (TradeOffer offer : tradeContext.getOffers()) {
            this.addTrade(offer);
        }
    }

    @Override
    public boolean onTrade(TradeOffer offer) {
        return false;
    }
}