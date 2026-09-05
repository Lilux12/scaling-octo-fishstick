package ru.danya.autosorter;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import ru.danya.autosorter.network.ClientNetworking;
import ru.danya.autosorter.screen.AutoSorterScreen;
import ru.danya.autosorter.screen.ModScreenHandlers;

public class AutoSorterModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// ScreenRegistry из Fabric API убрали — регистрируем напрямую через ванильный HandledScreens.
		HandledScreens.register(ModScreenHandlers.AUTO_SORTER_SCREEN_HANDLER, AutoSorterScreen::new);
		ClientNetworking.register();
	}
}
