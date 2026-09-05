package ru.danya.autosorter.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Один зарегистрированный сундук в сети сортировщика.
 * filterItem — образец предмета (сравнивается по Item, без учёта количества).
 * category — если не NONE, используется вместо конкретного предмета.
 *
 * Сериализуется через Codec (см. CODEC) — так требует новая система
 * сохранения BlockEntity (WriteView/ReadView) начиная с недавних версий
 * Minecraft, взамен ручной работы с NbtCompound.
 */
public class ChestLink {
	public BlockPos pos;
	public ItemStack filterItem;
	public FilterCategory category;

	public ChestLink(BlockPos pos) {
		this(pos, ItemStack.EMPTY, FilterCategory.NONE);
	}

	public ChestLink(BlockPos pos, ItemStack filterItem, FilterCategory category) {
		this.pos = pos;
		this.filterItem = filterItem;
		this.category = category;
	}

	public boolean matches(ItemStack stack) {
		if (category != FilterCategory.NONE) {
			return FilterCategory.matches(category, stack);
		}
		if (filterItem.isEmpty()) return false;
		return ItemStack.areItemsEqual(filterItem, stack);
	}

	public static final Codec<ChestLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.fieldOf("pos").forGetter(l -> l.pos),
			ItemStack.CODEC.optionalFieldOf("filter_item", ItemStack.EMPTY).forGetter(l -> l.filterItem),
			Codec.STRING.xmap(FilterCategory::valueOf, Enum::name)
					.optionalFieldOf("category", FilterCategory.NONE).forGetter(l -> l.category)
	).apply(instance, ChestLink::new));
}
