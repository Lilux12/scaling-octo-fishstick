package ru.danya.autosorter.block.entity;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

/**
 * Категории для быстрого фильтра ("вместо конкретного предмета можно
 * выбрать категорию"). Список пополняемый — можно добавлять свои.
 */
public enum FilterCategory {
	WOOD("Дерево"),
	ORES("Руды"),
	GEAR("Снаряжение"),
	TRASH("Мусор"),
	NONE("");

	public final String displayName;

	FilterCategory(String displayName) {
		this.displayName = displayName;
	}

	public static boolean matches(FilterCategory category, ItemStack stack) {
		if (stack.isEmpty() || category == null || category == NONE) return false;
		Item item = stack.getItem();
		return switch (category) {
			case WOOD -> item.getBuiltInRegistryHolder().isIn(ItemTags.LOGS)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.PLANKS)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.WOODEN_SLABS)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.WOODEN_STAIRS)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.WOODEN_FENCES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.WOODEN_DOORS)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.SAPLINGS)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.LEAVES);
			case ORES -> item.getBuiltInRegistryHolder().isIn(ItemTags.COAL_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.IRON_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.GOLD_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.DIAMOND_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.EMERALD_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.LAPIS_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.REDSTONE_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.COPPER_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.RAW_IRON_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.RAW_GOLD_ORES)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.RAW_COPPER_ORES);
			case GEAR -> item instanceof ArmorItem || item instanceof ToolItem || item instanceof RangedWeaponItem
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.SWORDS)
					|| item.getBuiltInRegistryHolder().isIn(ItemTags.TRIMMABLE_ARMOR);
			case TRASH -> item.getBuiltInRegistryHolder().isIn(
					net.minecraft.registry.tag.TagKey.of(
							net.minecraft.registry.RegistryKeys.ITEM, Identifier.of("autosorter", "trash")));
			default -> false;
		};
	}
}
