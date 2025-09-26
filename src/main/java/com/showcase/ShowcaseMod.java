package com.showcase;

import com.mojang.serialization.Codec;
import com.showcase.command.ShowcaseCommand;
import com.showcase.command.PlaceholderTestCommand;
import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfigManager;
import com.showcase.data.GlobalDataManager;
import com.showcase.data.JsonCodecDataStorage;
import com.showcase.data.ShareEntry;
import com.showcase.listener.ChatMessageListener;
import com.showcase.listener.ContainerOpenWatcher;
import com.showcase.placeholders.Placeholders;
import com.showcase.placeholders.ShowcaseStatistics;
import com.showcase.utils.*;
import com.showcase.utils.countdown.CountdownBossBarManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.showcase.utils.DevUtils;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.showcase.data.ShareEntry.SHARE_ENTRY_CODEC;

public class ShowcaseMod implements ModInitializer {
	public static final String MOD_ID = "showcase";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier PLAYER_SHARE_STORAGE_ID = Identifier.of(MOD_ID, "showcase_storage");
	public static final Codec<Map<String, ShareEntry>> PLAYER_SHARE_ENTRY_CODEC =
			Codec.unboundedMap(Codec.STRING, SHARE_ENTRY_CODEC);
	public static final JsonCodecDataStorage<Map<String, ShareEntry>> PLAYER_SHARE_STORAGE =
            new JsonCodecDataStorage<>("player_share_entry", PLAYER_SHARE_ENTRY_CODEC);

	@Override
	public void onInitialize() {
		ModConfigManager.loadConfig();
		ModMetadataHolder.load();
		ChatMessageListener.loadConfig();

		GlobalDataManager.register(PLAYER_SHARE_STORAGE_ID, PLAYER_SHARE_STORAGE);

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> {
					ShowcaseCommand.register(dispatcher);
					// Register test commands only in development environment
					DevUtils.ifDevelopment(() -> PlaceholderTestCommand.register(dispatcher));
				}
		);

		// Initialize statistics and placeholders
		ShowcaseStatistics.initialize();
		Placeholders.registerPlaceholders();
		ChatMessageListener.registerChatHandler();
		CountdownBossBarManager.registerTickEvent();
		ContainerOpenWatcher.registerTickEvent();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			try {
				Map<String, ShareEntry> data = GlobalDataManager.getData(server, PLAYER_SHARE_STORAGE_ID);
				if (data != null) ShowcaseManager.register(data);

				// Initialize statistics system with server instance
				ShowcaseStatistics.setServer(server);

				// Check resource pack configuration for icon feature
				ResourcePackChecker.checkResourcePackConfiguration(server);
			} catch (Exception e) {
				LOGGER.error("Failed to load showcase data", e);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			MapViewer.restoreAll(server);
			ContainerOpenWatcher.cleanup();
			CountdownBossBarManager.cleanup();
			try {
				// Save showcase data and statistics
				GlobalDataManager.setData(server, PLAYER_SHARE_STORAGE_ID, ShowcaseManager.getActiveShares());
				ShowcaseStatistics.saveStatistics(); // Final save before shutdown
				GlobalDataManager.saveAll(server);
			} catch (Exception e) {
				LOGGER.error("Failed to save showcase data", e);
			}
			ShowcaseManager.clearAll();
			ResourcePackChecker.reset();
			LOGGER.info("Cleaned up shared items on server shutdown");
		});

		// Send resource pack warning to ops when they join
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ResourcePackChecker.sendResourcePackWarningToPlayer(handler.getPlayer());
		});

		LOGGER.info("Showcase Mod initialized successfully!");
	}
}