package ru.danya.autosorter.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.danya.autosorter.screen.AutoSorterScreenHandler;

import java.util.ArrayList;
import java.util.List;

public class AutoSorterBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {

	/** Радиус (в блоках) поиска сундуков для вкладки "Настройки". */
	public static final int SCAN_RADIUS = 16;
	/** Сколько предметов вставляется в целевой сундук за один тик, максимум. */
	private static final int TRANSFER_PER_TICK = 1;

	/** Буфер для входящих предметов — сюда игрок кладёт вещи / сюда толкает воронка (вкладка "Сортировка"). */
	private final SimpleInventory inputBuffer = new SimpleInventory(9);

	/** Зарегистрированные сундуки с фильтрами. */
	private final List<ChestLink> links = new ArrayList<>();

	private int tickCounter = 0;

	public AutoSorterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AUTO_SORTER, pos, state);
	}

	public SimpleInventory getInputBuffer() {
		return inputBuffer;
	}

	public List<ChestLink> getLinks() {
		return links;
	}

	// ---------------------------------------------------------------
	// Сканирование ближайших сундуков (для вкладки "Настройки")
	// ---------------------------------------------------------------
	public List<BlockPos> scanNearbyContainers(World world) {
		List<BlockPos> found = new ArrayList<>();
		BlockPos origin = this.pos;
		int r = SCAN_RADIUS;
		for (BlockPos p : BlockPos.iterate(
				origin.getX() - r, origin.getY() - r, origin.getZ() - r,
				origin.getX() + r, origin.getY() + r, origin.getZ() + r)) {
			if (p.equals(origin)) continue;
			BlockEntity be = world.getBlockEntity(p);
			if (be instanceof Inventory && !(be instanceof AutoSorterBlockEntity)) {
				found.add(p.toImmutable());
			}
		}
		return found;
	}

	public void addOrUpdateLink(BlockPos target) {
		for (ChestLink l : links) {
			if (l.pos.equals(target)) return; // уже есть
		}
		links.add(new ChestLink(target));
		markDirty();
	}

	public void removeLink(BlockPos target) {
		links.removeIf(l -> l.pos.equals(target));
		markDirty();
	}

	public void setFilterItem(BlockPos target, ItemStack sample) {
		for (ChestLink l : links) {
			if (l.pos.equals(target)) {
				l.filterItem = sample.copyWithCount(1);
				l.category = FilterCategory.NONE;
				markDirty();
				return;
			}
		}
	}

	public void setFilterCategory(BlockPos target, FilterCategory category) {
		for (ChestLink l : links) {
			if (l.pos.equals(target)) {
				l.category = category;
				l.filterItem = ItemStack.EMPTY;
				markDirty();
				return;
			}
		}
	}

	// ---------------------------------------------------------------
	// Тик: перекладываем предметы из буфера в подходящие сундуки
	// ---------------------------------------------------------------
	public static void serverTick(World world, BlockPos pos, BlockState state, AutoSorterBlockEntity be) {
		be.tickCounter++;
		if (be.tickCounter % 4 != 0) return; // не каждый тик — для производительности
		if (be.links.isEmpty()) return;

		for (int slot = 0; slot < be.inputBuffer.size(); slot++) {
			ItemStack stack = be.inputBuffer.getStack(slot);
			if (stack.isEmpty()) continue;

			for (ChestLink link : be.links) {
				if (!link.matches(stack)) continue;

				BlockEntity targetBe = world.getBlockEntity(link.pos);
				if (!(targetBe instanceof Inventory targetInv)) continue;

				ItemStack toMove = stack.copyWithCount(Math.min(TRANSFER_PER_TICK, stack.getCount()));
				ItemStack leftover = insertIntoInventory(targetInv, toMove);
				int moved = toMove.getCount() - leftover.getCount();
				if (moved > 0) {
					stack.decrement(moved);
					be.inputBuffer.setStack(slot, stack);
					be.markDirty();
					if (targetBe instanceof net.minecraft.block.entity.BlockEntity tbe) {
						tbe.markDirty();
					}
					break; // предмет пристроен, переходим к следующему слоту
				}
			}
		}
	}

	/** Пытается вставить стак в инвентарь (учитывая maxStackSize и существующие стаки). Возвращает остаток. */
	private static ItemStack insertIntoInventory(Inventory inv, ItemStack stack) {
		ItemStack remaining = stack.copy();
		// сначала докладываем в существующие такие же стаки
		for (int i = 0; i < inv.size() && !remaining.isEmpty(); i++) {
			ItemStack existing = inv.getStack(i);
			if (!existing.isEmpty() && ItemStack.areItemsAndComponentsEqual(existing, remaining)) {
				int space = Math.min(inv.getMaxCountPerStack(), existing.getMaxCount()) - existing.getCount();
				if (space > 0) {
					int moved = Math.min(space, remaining.getCount());
					existing.increment(moved);
					remaining.decrement(moved);
				}
			}
		}
		// затем в пустые слоты
		for (int i = 0; i < inv.size() && !remaining.isEmpty(); i++) {
			ItemStack existing = inv.getStack(i);
			if (existing.isEmpty()) {
				int moved = Math.min(remaining.getMaxCount(), remaining.getCount());
				inv.setStack(i, remaining.copyWithCount(moved));
				remaining.decrement(moved);
			}
		}
		return remaining;
	}

	// ---------------------------------------------------------------
	// GUI
	// ---------------------------------------------------------------
	@Override
	public Text getDisplayName() {
		return Text.translatable("block.autosorter.auto_sorter");
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new AutoSorterScreenHandler(syncId, playerInventory, this.pos);
	}

	/** Данные, которые сервер отправит клиенту при открытии GUI (см. ExtendedScreenHandlerType). */
	@Override
	public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
		return this.pos;
	}

	// ---------------------------------------------------------------
	// Сохранение / загрузка
	// ---------------------------------------------------------------
	@Override
	protected void writeNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.put("InputBuffer", inputBuffer.toNbtList(registries));
		NbtList linksNbt = new NbtList();
		for (ChestLink link : links) {
			linksNbt.add(link.writeNbt(registries));
		}
		nbt.put("Links", linksNbt);
	}

	@Override
	protected void readNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		inputBuffer.readNbtList(nbt.getList("InputBuffer", 10), registries);
		links.clear();
		NbtList linksNbt = nbt.getList("Links", 10);
		for (int i = 0; i < linksNbt.size(); i++) {
			links.add(ChestLink.readNbt(linksNbt.getCompound(i), registries));
		}
	}
}
