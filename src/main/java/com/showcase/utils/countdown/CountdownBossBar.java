package com.showcase.utils.countdown;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;

public class CountdownBossBar {
    private ServerBossBar bossBar;
    private ServerPlayerEntity player;
    private final int totalTicks;
    private int ticksLeft;
    private final Text baseTitle;
    private boolean isRunning;
    private boolean isFinished;
    private boolean isPaused;

    public interface CountdownCallback {
        void onComplete();
        void onStop();
        void onSecondTick(int secondsRemaining);
    }

    private CountdownCallback callback;
    private int lastSecond = -1;

    public CountdownBossBar(ServerPlayerEntity player, Text title, int seconds) {
        this.player = player;
        this.totalTicks = seconds * 20;
        this.ticksLeft = this.totalTicks;
        this.baseTitle = title;
        this.isRunning = false;
        this.isFinished = false;
        this.isPaused = false;

        this.bossBar = new ServerBossBar(title, BossBar.Color.GREEN, BossBar.Style.PROGRESS);
        this.bossBar.addPlayer(player);
        updateBossBar();
    }

    public CountdownBossBar setCallback(CountdownCallback callback) {
        this.callback = callback;
        return this;
    }

    public void start() {
        if (isFinished) {
            return;
        }
        this.isRunning = true;
        this.isPaused = false;
    }

    public void stop() {
        if (!isRunning || isFinished) {
            return;
        }

        this.isRunning = false;
        this.isFinished = true;
        cleanup();

        if (callback != null) {
            callback.onStop();
        }
    }

    public void pause() {
        if (isRunning && !isFinished) {
            this.isPaused = true;
        }
    }

    public void resume() {
        if (isRunning && !isFinished) {
            this.isPaused = false;
        }
    }

    public void setTimeLeft(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        this.ticksLeft = seconds * 20;
        updateBossBar();
    }

    public void addTime(int seconds) {
        this.ticksLeft += seconds * 20;
        if (this.ticksLeft < 0) {
            this.ticksLeft = 0;
        }
        updateBossBar();
    }

    public void tick() {
        if (!isRunning || isFinished || isPaused) {
            return;
        }

        if (player == null || player.isRemoved()) {
            stop();
            return;
        }

        if (ticksLeft <= 0) {
            complete();
            return;
        }

        ticksLeft--;
        updateBossBar();

        int currentSecond = getSecondsLeft();
        if (currentSecond != lastSecond && callback != null) {
            callback.onSecondTick(currentSecond);
            lastSecond = currentSecond;
        }
    }

    private void complete() {
        if (isFinished) {
            return;
        }

        this.isFinished = true;
        this.isRunning = false;
        cleanup();

        if (callback != null) {
            callback.onComplete();
        }
    }

    private void updateBossBar() {
        if (bossBar == null || player == null) {
            return;
        }

        int secondsLeft = getSecondsLeft();

        float progress = Math.max(0.0f, Math.min(1.0f, ticksLeft / (float) totalTicks));
        bossBar.setPercent(progress);

        Text newTitle = baseTitle.copy().append(Text.literal(" (" + secondsLeft + "s)"));

        if (!bossBar.getName().equals(newTitle)) {
            bossBar.setName(newTitle);
        }

        if (progress <= 0.3f) {
            bossBar.setColor(BossBar.Color.RED);
        } else if (progress <= 0.5f) {
            bossBar.setColor(BossBar.Color.YELLOW);
        } else {
            bossBar.setColor(BossBar.Color.GREEN);
        }

        if (progress <= 0.05f) {
            bossBar.setStyle(BossBar.Style.NOTCHED_20);
        } else {
            bossBar.setStyle(BossBar.Style.PROGRESS);
        }
    }

    private void cleanup() {
        if (bossBar != null && player != null) {
            bossBar.removePlayer(player);
        }
    }

    public void destroy() {
        stop();
        if (bossBar != null) {
            bossBar.clearPlayers();
        }
        this.bossBar = null;
        this.player = null;
        this.callback = null;
    }

    public int getSecondsLeft() {
        return Math.max(0, (ticksLeft + 19) / 20);
    }

    public int getTicksLeft() {
        return Math.max(0, ticksLeft);
    }

    public int getTotalSeconds() {
        return totalTicks / 20;
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public boolean isRunning() {
        return isRunning && !isFinished;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public float getProgress() {
        return ticksLeft / (float) totalTicks;
    }
}