package ru.danya.autosorter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import ru.danya.autosorter.network.ClientNetworking;
import ru.danya.autosorter.screen.AutoSorterScreen;
import ru.danya.autosorter.screen.ModScreenHandlers;

public class AutoSorterModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ScreenRegistry.register(ModScreenHandlers.AUTO_SORTER_SCREEN_HANDLER, AutoSorterScreen::new);
		ClientNetworking.register();
	}
}
