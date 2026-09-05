package ru.danya.autosorter.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ru.danya.autosorter.AutoSorterMod;
import ru.danya.autosorter.block.ModBlocks;

/**
 * Используем FabricBlockEntityTypeBuilder вместо ванильного
 * BlockEntityType.Builder — у ванильного билдера сигнатура/доступность
 * менялись между версиями, а Fabric-обёртка стабильна и является
 * рекомендованным способом для модов.
 */
public class ModBlockEntities {

	public static final BlockEntityType<AutoSorterBlockEntity> AUTO_SORTER = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			Identifier.of(AutoSorterMod.MOD_ID, "auto_sorter"),
			FabricBlockEntityTypeBuilder.create(AutoSorterBlockEntity::new, ModBlocks.AUTO_SORTER).build()
	);

	public static final BlockEntityType<ReverseHopperBlockEntity> REVERSE_HOPPER = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			Identifier.of(AutoSorterMod.MOD_ID, "reverse_hopper"),
			FabricBlockEntityTypeBuilder.create(ReverseHopperBlockEntity::new, ModBlocks.REVERSE_HOPPER).build()
	);

	public static void register() {
		// static init trigger
	}
}
