package com.showcase.utils.countdown;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CountdownBossBarManager {
    private static final Set<CountdownBossBar> bars = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static void registerTickEvent() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Iterator<CountdownBossBar> iterator = bars.iterator();
            while (iterator.hasNext()) {
                CountdownBossBar bar = iterator.next();
                if (!bar.isRunning() || bar.isFinished()) {
                    bar.destroy();
                    iterator.remove();
                    continue;
                }
                bar.tick();
            }
        });
    }

    public static void add(CountdownBossBar bar) {
        if (bar != null) {
            bars.add(bar);
        }
    }

    public static void remove(CountdownBossBar bar) {
        if (bar != null) {
            bars.remove(bar);
            bar.destroy();
        }
    }

    public static void cleanup() {
        bars.forEach(CountdownBossBar::destroy);
        bars.clear();
    }

    public static Set<CountdownBossBar> getBars() {
        return Collections.unmodifiableSet(bars);
    }
}
