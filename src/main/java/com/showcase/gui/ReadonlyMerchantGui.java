package com.showcase.gui;

import eu.pb4.sgui.api.gui.MerchantGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffer;

public class ReadonlyMerchantGui extends MerchantGui {
    protected MerchantContext context;

    public ReadonlyMerchantGui(ServerPlayer player, MerchantContext tradeContext) {
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

        for (MerchantOffer offer : tradeContext.getOffers()) {
            this.addTrade(offer);
        }
    }

    @Override
    public boolean onTrade(MerchantOffer offer) {
        return false;
    }
}