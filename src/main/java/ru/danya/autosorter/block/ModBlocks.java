package ru.danya.autosorter.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import ru.danya.autosorter.AutoSorterMod;

public class ModBlocks {

	public static final Block AUTO_SORTER = register(
			"auto_sorter",
			settings -> new AutoSorterBlock(settings),
			AbstractBlock.Settings.create().strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL).nonOpaque(),
			true
	);

	public static final Block REVERSE_HOPPER = register(
			"reverse_hopper",
			settings -> new ReverseHopperBlock(settings),
			AbstractBlock.Settings.create().strength(3.0f, 4.8f).sounds(BlockSoundGroup.METAL).nonOpaque(),
			true
	);

	private static Block register(String path, java.util.function.Function<AbstractBlock.Settings, Block> factory,
			AbstractBlock.Settings settings, boolean withItem) {
		RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AutoSorterMod.MOD_ID, path));
		Block block = factory.apply(settings.registryKey(key));
		Registry.register(Registries.BLOCK, key, block);
		if (withItem) {
			RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(AutoSorterMod.MOD_ID, path));
			Item item = new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
			Registry.register(Registries.ITEM, itemKey, item);
		}
		return block;
	}

	public static void register() {
		// static init trigger
	}
}
