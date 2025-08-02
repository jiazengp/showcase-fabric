package com.showcase.utils.stats;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import java.util.Map;

public interface StatAPI {
    Map<String, Integer> getPlayerStats(ServerPlayerEntity player);
    default StatHandler getStatHandler(ServerPlayerEntity player) {
        return player.getStatHandler();
    }
}
