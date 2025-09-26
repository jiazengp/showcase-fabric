package com.showcase.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.showcase.command.ShowcaseManager;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializable data structure for player statistics
 */
public record PlayerStatisticsData(
    int totalShares,
    long totalViews,
    Map<ShowcaseManager.ShareType, Integer> sharesByType,
    Map<String, Integer> sharesPerDay,
    List<Long> shareDurationSeconds,
    ShowcaseManager.ShareType mostSharedType,
    Long lastShareTime
) {

    public static final Codec<PlayerStatisticsData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("totalShares").forGetter(PlayerStatisticsData::totalShares),
            Codec.LONG.fieldOf("totalViews").forGetter(PlayerStatisticsData::totalViews),
            Codec.unboundedMap(
                Codec.STRING.xmap(ShowcaseManager.ShareType::valueOf, ShowcaseManager.ShareType::name),
                Codec.INT
            ).fieldOf("sharesByType").forGetter(PlayerStatisticsData::sharesByType),
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                .fieldOf("sharesPerDay").forGetter(PlayerStatisticsData::sharesPerDay),
            Codec.LONG.listOf().fieldOf("shareDurationSeconds").forGetter(PlayerStatisticsData::shareDurationSeconds),
            Codec.STRING.xmap(
                name -> name.equals("null") ? null : ShowcaseManager.ShareType.valueOf(name),
                type -> type == null ? "null" : type.name()
            ).optionalFieldOf("mostSharedType", null).forGetter(PlayerStatisticsData::mostSharedType),
            Codec.LONG.optionalFieldOf("lastShareTime", null).forGetter(PlayerStatisticsData::lastShareTime)
        ).apply(instance, PlayerStatisticsData::new)
    );

    public static PlayerStatisticsData empty() {
        return new PlayerStatisticsData(
            0, 0L,
            new HashMap<>(),
            new HashMap<>(),
            List.of(),
            null,
            null
        );
    }

    public List<Duration> getShareDurations() {
        return shareDurationSeconds.stream()
            .map(Duration::ofSeconds)
            .toList();
    }

    public Instant getLastShareTimeInstant() {
        return lastShareTime != null ? Instant.ofEpochMilli(lastShareTime) : null;
    }
}