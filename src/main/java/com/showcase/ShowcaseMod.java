package com.showcase;

import com.showcase.command.ShowcaseCommand;
import com.showcase.command.ShowcaseManager;
import com.showcase.config.ModConfig;
import com.showcase.listener.ChatMessageListener;
import com.showcase.network.NetworkHandler;
import com.showcase.placeholders.Placeholders;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowcaseMod implements ModInitializer {
	public static final String MOD_ID = "showcase";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
		try {
			CONFIG = ModConfig.load();
			
			// Register network first as it may be necessary for other components
			NetworkHandler.register();

			// Then register commands
			CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
				ShowcaseCommand.register(dispatcher);
				LOGGER.debug("Commands registered");
			});

			// Register placeholders and chat handlers
			Placeholders.registerPlaceholders();
			ChatMessageListener.registerChatHandler();

			// Register cleanup hook
			ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
				ShowcaseManager.clearAll();
				LOGGER.info("Cleaned up shared items on server shutdown");
			});

			LOGGER.info("Showcase Mod initialized successfully!");
		} catch (Exception e) {
			LOGGER.error("Failed to initialize Showcase Mod", e);
			throw new RuntimeException("Showcase Mod initialization failed", e);
		}
	}
}