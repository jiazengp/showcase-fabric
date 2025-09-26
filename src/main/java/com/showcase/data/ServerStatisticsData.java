package com.showcase.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Serializable data structure for server-wide statistics
 */
public record ServerStatisticsData(
    long totalViewsEver,
    Map<String, Integer> shareTypeGlobalStats,
    long cacheHits,
    long cacheMisses,
    Map<String, PlayerStatisticsData> playerStatistics
) {

    public static final Codec<ServerStatisticsData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("totalViewsEver").forGetter(ServerStatisticsData::totalViewsEver),
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                .fieldOf("shareTypeGlobalStats").forGetter(ServerStatisticsData::shareTypeGlobalStats),
            Codec.LONG.fieldOf("cacheHits").forGetter(ServerStatisticsData::cacheHits),
            Codec.LONG.fieldOf("cacheMisses").forGetter(ServerStatisticsData::cacheMisses),
            Codec.unboundedMap(Codec.STRING, PlayerStatisticsData.CODEC)
                .fieldOf("playerStatistics").forGetter(ServerStatisticsData::playerStatistics)
        ).apply(instance, ServerStatisticsData::new)
    );

    public static ServerStatisticsData empty() {
        return new ServerStatisticsData(
            0L,
            new HashMap<>(),
            0L,
            0L,
            new HashMap<>()
        );
    }
}