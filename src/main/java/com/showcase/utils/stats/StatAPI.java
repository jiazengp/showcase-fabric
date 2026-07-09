package com.showcase.utils.stats;

import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatsCounter;

public interface StatAPI {
    Map<String, Integer> getPlayerStats(ServerPlayer player);
    default StatsCounter getStatHandler(ServerPlayer player) {
        return player.getStats();
    }
}
