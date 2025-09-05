package com.showcase.gui;

import com.showcase.utils.countdown.CountdownBossBar;
import com.showcase.utils.countdown.CountdownBossBarManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseViewerContext {
    public final ServerPlayerEntity player;
    public final List<SwitchEntry> interfaceList = new ArrayList<>();
    protected boolean closed = false;
    protected CountdownBossBar countdownBossBar;
    protected final int durationSeconds;
    protected ViewerCloseCallback closeCallback;

    public interface ViewerCloseCallback {
        void onViewerClose();
    }

    public BaseViewerContext(ServerPlayerEntity player) {
        this.player = player;
        this.durationSeconds = 0;
    }

    public BaseViewerContext(ServerPlayerEntity player, int durationSeconds) {
        this.player = player;
        this.durationSeconds = durationSeconds;
        
        if (hasTimeLimit()) {
            initializeCountdown();
        }
    }

    public void setCloseCallback(ViewerCloseCallback callback) {
        this.closeCallback = callback;
    }

    protected void initializeCountdown() {
        if (durationSeconds <= 0) return;
        
        countdownBossBar = new CountdownBossBar(
            player, 
            Text.translatable("showcase.viewer.countdown_title"),
            durationSeconds
        ).setCallback(new CountdownBossBar.CountdownCallback() {
            @Override
            public void onComplete() {
                close();
            }

            @Override
            public void onStop() {
                // Countdown was manually stopped
            }

            @Override
            public void onSecondTick(int secondsRemaining) {
                // Optional: could trigger additional effects here
            }
        });
        
        CountdownBossBarManager.add(countdownBossBar);
        countdownBossBar.start();
    }

    public void sendInstructionMessage() {
        Text instructionMessage = Text.translatable("showcase.viewer.instructions")
            .formatted(Formatting.GRAY);
        
        // Send as action bar message for better visibility
        player.sendMessage(instructionMessage, true);
    }

    public void extendTime(int additionalSeconds) {
        if (countdownBossBar != null && countdownBossBar.isRunning()) {
            countdownBossBar.addTime(additionalSeconds);
        }
    }

    public void pauseCountdown() {
        if (countdownBossBar != null && countdownBossBar.isRunning()) {
            countdownBossBar.pause();
        }
    }

    public void resumeCountdown() {
        if (countdownBossBar != null) {
            countdownBossBar.resume();
        }
    }

    protected void closeCountdown() {
        if (countdownBossBar != null) {
            CountdownBossBarManager.remove(countdownBossBar);
            countdownBossBar = null;
        }
    }

    public void close() {
        closeCountdown();
        this.closed = true;
        
        if (closeCallback != null) {
            closeCallback.onViewerClose();
        }
    }

    public boolean checkClosed() {
        return closed;
    }

    public boolean hasTimeLimit() {
        return durationSeconds > 0;
    }

    public int getRemainingTime() {
        if (countdownBossBar == null) return -1;
        return countdownBossBar.getSecondsLeft();
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    @FunctionalInterface
    public interface SwitchableUi {
        void openUi(BaseViewerContext context, int selectedSlot);
    }

    public record SwitchEntry(SwitchableUi ui, int currentSlot) {
        public void open(BaseViewerContext context) {
            ui.openUi(context, currentSlot);
        }
    }
}