package ru.danya.autosorter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.danya.autosorter.block.ModBlocks;
import ru.danya.autosorter.block.entity.ModBlockEntities;
import ru.danya.autosorter.network.ModNetworking;
import ru.danya.autosorter.screen.ModScreenHandlers;

public class AutoSorterMod implements ModInitializer {
	public static final String MOD_ID = "autosorter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[AutoSorter] Инициализация мода...");
		ModBlocks.register();
		ModBlockEntities.register();
		ModScreenHandlers.register();
		ModNetworking.registerServerReceivers();

		// Добавляем предметы во вкладку "Функциональные блоки" творческого инвентаря
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
			entries.add(ModBlocks.AUTO_SORTER);
			entries.add(ModBlocks.REVERSE_HOPPER);
		});

		LOGGER.info("[AutoSorter] Готово.");
	}
}
