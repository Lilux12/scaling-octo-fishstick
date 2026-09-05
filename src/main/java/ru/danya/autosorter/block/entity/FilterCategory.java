package ru.danya.autosorter.block.entity;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Категории для быстрого фильтра ("вместо конкретного предмета можно
 * выбрать категорию"). Список пополняемый — можно добавлять свои.
 *
 * ВАЖНО: начиная с относительно недавних версий Minecraft (компонентная
 * система предметов) отдельных классов ArmorItem/ToolItem больше нет —
 * снаряжение определяется через компоненты (DataComponentTypes.EQUIPPABLE,
 * .TOOL) и теги, а не через instanceof.
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

	private static final TagKey<Item> TRASH_TAG =
			TagKey.of(RegistryKeys.ITEM, Identifier.of("autosorter", "trash"));

	public static boolean matches(FilterCategory category, ItemStack stack) {
		if (stack.isEmpty() || category == null || category == NONE) return false;
		return switch (category) {
			case WOOD -> stack.isIn(ItemTags.LOGS)
					|| stack.isIn(ItemTags.PLANKS)
					|| stack.isIn(ItemTags.WOODEN_SLABS)
					|| stack.isIn(ItemTags.WOODEN_STAIRS)
					|| stack.isIn(ItemTags.WOODEN_FENCES)
					|| stack.isIn(ItemTags.WOODEN_DOORS)
					|| stack.isIn(ItemTags.SAPLINGS)
					|| stack.isIn(ItemTags.LEAVES);
			case ORES -> stack.isIn(ItemTags.COAL_ORES)
					|| stack.isIn(ItemTags.IRON_ORES)
					|| stack.isIn(ItemTags.GOLD_ORES)
					|| stack.isIn(ItemTags.DIAMOND_ORES)
					|| stack.isIn(ItemTags.EMERALD_ORES)
					|| stack.isIn(ItemTags.LAPIS_ORES)
					|| stack.isIn(ItemTags.REDSTONE_ORES)
					|| stack.isIn(ItemTags.COPPER_ORES);
			case GEAR -> stack.contains(DataComponentTypes.EQUIPPABLE)
					|| stack.contains(DataComponentTypes.TOOL)
					|| stack.isIn(ItemTags.SWORDS)
					|| stack.isIn(ItemTags.TRIMMABLE_ARMOR);
			case TRASH -> stack.isIn(TRASH_TAG);
			default -> false;
		};
	}
}
