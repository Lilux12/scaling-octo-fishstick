package ru.danya.autosorter.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ru.danya.autosorter.AutoSorterMod;

/**
 * Используем ExtendedScreenHandlerType (Fabric API), чтобы при открытии GUI
 * сервер передавал клиенту BlockPos сортировщика — иначе клиентский экран
 * не будет знать, к какому блоку он относится, и вкладка "Настройки" будет
 * слать запросы в никуда.
 */
public class ModScreenHandlers {

	public static final ExtendedScreenHandlerType<AutoSorterScreenHandler, BlockPos> AUTO_SORTER_SCREEN_HANDLER =
			Registry.register(
					Registries.SCREEN_HANDLER,
					Identifier.of(AutoSorterMod.MOD_ID, "auto_sorter"),
					new ExtendedScreenHandlerType<>(
							(syncId, playerInventory, pos) -> new AutoSorterScreenHandler(syncId, playerInventory, pos),
							BlockPos.PACKET_CODEC
					)
			);

	public static void register() {
		// static init trigger
	}
}
