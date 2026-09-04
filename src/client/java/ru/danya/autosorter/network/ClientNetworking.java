package ru.danya.autosorter.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ru.danya.autosorter.screen.AutoSorterScreen;

/** Клиентские сетевые обработчики — регистрируются только на клиенте. */
public class ClientNetworking {
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(ModPayloads.ChestListS2C.ID, (payload, context) -> {
			context.client().execute(() -> {
				if (context.client().currentScreen instanceof AutoSorterScreen screen) {
					screen.updateChestList(payload.sorterPos(), payload.entries());
				}
			});
		});
	}
}
