package ru.danya.autosorter.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/**
 * Один зарегистрированный сундук в сети сортировщика.
 * filterItem — образец предмета (сравнивается по Item, без учёта количества).
 * category — если не NONE, используется вместо конкретного предмета.
 */
public class ChestLink {
	public BlockPos pos;
	public ItemStack filterItem = ItemStack.EMPTY;
	public FilterCategory category = FilterCategory.NONE;

	public ChestLink(BlockPos pos) {
		this.pos = pos;
	}

	public boolean matches(ItemStack stack) {
		if (category != FilterCategory.NONE) {
			return FilterCategory.matches(category, stack);
		}
		if (filterItem.isEmpty()) return false;
		return ItemStack.areItemsEqual(filterItem, stack);
	}

	public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registries) {
		NbtCompound nbt = new NbtCompound();
		nbt.putInt("x", pos.getX());
		nbt.putInt("y", pos.getY());
		nbt.putInt("z", pos.getZ());
		nbt.put("filterItem", filterItem.encode(registries));
		nbt.putString("category", category.name());
		return nbt;
	}

	public static ChestLink readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		BlockPos pos = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
		ChestLink link = new ChestLink(pos);
		if (nbt.contains("filterItem")) {
			link.filterItem = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("filterItem"));
		}
		if (nbt.contains("category")) {
			try {
				link.category = FilterCategory.valueOf(nbt.getString("category"));
			} catch (IllegalArgumentException ignored) {
				link.category = FilterCategory.NONE;
			}
		}
		return link;
	}
}
