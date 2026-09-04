package ru.danya.autosorter.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * "Обратная воронка": каждые несколько тиков тянет один предмет
 * из инвентаря снизу и толкает его в инвентарь сверху.
 * Работает с любым Inventory — включая ChestBlockEntity и AutoSorterBlockEntity.
 */
public class ReverseHopperBlockEntity extends BlockEntity {

	private static final int COOLDOWN_TICKS = 8;
	private int cooldown = 0;

	public ReverseHopperBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.REVERSE_HOPPER, pos, state);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, ReverseHopperBlockEntity be) {
		if (be.cooldown > 0) {
			be.cooldown--;
			return;
		}

		BlockEntity below = world.getBlockEntity(pos.down());
		BlockEntity above = world.getBlockEntity(pos.up());

		if (!(below instanceof Inventory source)) return;
		if (!(above instanceof Inventory destination)) return;

		for (int i = 0; i < source.size(); i++) {
			ItemStack stack = source.getStack(i);
			if (stack.isEmpty()) continue;

			ItemStack single = stack.copyWithCount(1);
			if (tryInsert(destination, single)) {
				stack.decrement(1);
				source.setStack(i, stack);
				source.markDirty();
				destination.markDirty();
				be.cooldown = COOLDOWN_TICKS;
				return;
			}
		}
	}

	private static boolean tryInsert(Inventory inv, ItemStack stack) {
		for (int i = 0; i < inv.size(); i++) {
			ItemStack existing = inv.getStack(i);
			if (!existing.isEmpty() && ItemStack.areItemsAndComponentsEqual(existing, stack)) {
				int max = Math.min(inv.getMaxCountPerStack(), existing.getMaxCount());
				if (existing.getCount() < max) {
					existing.increment(1);
					return true;
				}
			}
		}
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isEmpty()) {
				inv.setStack(i, stack.copy());
				return true;
			}
		}
		return false;
	}
}
