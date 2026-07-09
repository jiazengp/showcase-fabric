package com.showcase.gui;

import com.showcase.utils.countdown.CountdownBossBar;
import com.showcase.utils.countdown.CountdownBossBarManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public abstract class BaseViewerContext {
    public final ServerPlayer player;
    public final List<SwitchEntry> interfaceList = new ArrayList<>();
    protected boolean closed = false;
    protected CountdownBossBar countdownBossBar;
    protected final int durationSeconds;
    protected ViewerCloseCallback closeCallback;

    public interface ViewerCloseCallback {
        void onViewerClose();
    }

    public BaseViewerContext(ServerPlayer player) {
        this.player = player;
        this.durationSeconds = 0;
    }

    public BaseViewerContext(ServerPlayer player, int durationSeconds) {
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
            Component.translatable("showcase.viewer.countdown_title"),
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
        Component instructionMessage = Component.translatable("showcase.viewer.instructions")
            .withStyle(ChatFormatting.GRAY);
        
        // Send as action bar message for better visibility
        player.sendSystemMessage(instructionMessage, true);
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
