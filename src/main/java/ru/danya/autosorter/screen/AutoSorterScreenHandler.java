package ru.danya.autosorter.screen;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import ru.danya.autosorter.block.entity.AutoSorterBlockEntity;

/**
 * Хендлер экрана. Слоты 0..8 — буфер сортировщика ("Сортировка"),
 * слоты 9..35 — инвентарь игрока.
 * Вкладка "Настройки" (список сундуков/фильтров) НЕ использует слоты —
 * она отрисовывается и управляется через кастомные сетевые пакеты
 * (см. ru.danya.autosorter.network.ModNetworking) и рендерится
 * на клиенте в AutoSorterScreen.
 */
public class AutoSorterScreenHandler extends ScreenHandler {

	public final AutoSorterBlockEntity blockEntity;
	/** Позиция блока — всегда известна и на клиенте, и на сервере (см. ExtendedScreenHandlerType). */
	public final BlockPos sorterPos;
	private final Inventory inventory;

	/**
	 * Конструктор, который вызывает клиент, получив BlockPos через
	 * ExtendedScreenHandlerType. На клиенте world.getBlockEntity(pos) уже
	 * содержит синхронизированный AutoSorterBlockEntity (или ещё нет,
	 * если чанк не догрузился/не синхронизировался — тогда работаем
	 * с временным пустым инвентарём, чтобы GUI не крашнулся).
	 */
	public AutoSorterScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
		super(ModScreenHandlers.AUTO_SORTER_SCREEN_HANDLER, syncId);
		this.sorterPos = pos;
		BlockEntity be = playerInventory.player.getWorld().getBlockEntity(pos);
		this.blockEntity = be instanceof AutoSorterBlockEntity sorter ? sorter : null;
		this.inventory = this.blockEntity != null ? this.blockEntity.getInputBuffer() : new net.minecraft.inventory.SimpleInventory(9);
		inventory.onOpen(playerInventory.player);

		// Буфер сортировщика: 3x3
		int bufferX = 62;
		int bufferY = 18;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				this.addSlot(new Slot(inventory, col + row * 3, bufferX + col * 18, bufferY + row * 18));
			}
		}

		// Инвентарь игрока
		int playerInvX = 8;
		int playerInvY = 84;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, playerInvX + col * 18, playerInvY + row * 18));
			}
		}
		int hotbarY = 142;
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, playerInvX + col * 18, hotbarY));
		}
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			ItemStack original = slot.getStack();
			newStack = original.copy();
			int bufferSize = 9;
			if (index < bufferSize) {
				if (!this.insertItem(original, bufferSize, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (!this.insertItem(original, 0, bufferSize, false)) {
					return ItemStack.EMPTY;
				}
			}
			if (original.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}
		return newStack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return inventory.canPlayerUse(player);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		inventory.onClose(player);
	}
}
