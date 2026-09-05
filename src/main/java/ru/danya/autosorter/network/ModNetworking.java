package ru.danya.autosorter.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import ru.danya.autosorter.block.entity.AutoSorterBlockEntity;
import ru.danya.autosorter.block.entity.ChestLink;
import ru.danya.autosorter.block.entity.FilterCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Заметка под 1.21.11: ServerPlayerEntity.getServer()/getWorld() были
 * убраны/переименованы. Вместо этого используем context.server() (даёт
 * сам колбэк ServerPlayNetworking) и player.getEntityWorld() (новое имя
 * метода получения мира сущности).
 */
public class ModNetworking {

	public static void registerPayloadTypes() {
		PayloadTypeRegistry.playC2S().register(ModPayloads.RequestChestListC2S.ID, ModPayloads.RequestChestListC2S.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.ChestListS2C.ID, ModPayloads.ChestListS2C.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.ToggleLinkC2S.ID, ModPayloads.ToggleLinkC2S.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.SetFilterItemC2S.ID, ModPayloads.SetFilterItemC2S.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.SetFilterCategoryC2S.ID, ModPayloads.SetFilterCategoryC2S.CODEC);
	}

	public static void registerServerReceivers() {
		registerPayloadTypes();

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.RequestChestListC2S.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				BlockEntity be = player.getEntityWorld().getBlockEntity(payload.sorterPos());
				if (!(be instanceof AutoSorterBlockEntity sorter)) return;

				List<BlockPos> nearby = sorter.scanNearbyContainers(player.getEntityWorld());
				List<ModPayloads.ChestEntry> entries = new ArrayList<>();
				for (BlockPos p : nearby) {
					ChestLink existing = null;
					for (ChestLink l : sorter.getLinks()) {
						if (l.pos.equals(p)) {
							existing = l;
							break;
						}
					}
					BlockEntity targetBe = player.getEntityWorld().getBlockEntity(p);
					String label = targetBe != null
							? targetBe.getCachedState().getBlock().getName().getString() + "  [" + p.getX() + "," + p.getY() + "," + p.getZ() + "]"
							: ("? [" + p.getX() + "," + p.getY() + "," + p.getZ() + "]");

					entries.add(new ModPayloads.ChestEntry(
							p,
							label,
							existing != null,
							existing != null ? existing.filterItem : ItemStack.EMPTY,
							existing != null ? existing.category : FilterCategory.NONE
					));
				}
				ServerPlayNetworking.send(player, new ModPayloads.ChestListS2C(payload.sorterPos(), entries));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.ToggleLinkC2S.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				BlockEntity be = player.getEntityWorld().getBlockEntity(payload.sorterPos());
				if (!(be instanceof AutoSorterBlockEntity sorter)) return;

				boolean alreadyLinked = false;
				for (ChestLink l : sorter.getLinks()) {
					if (l.pos.equals(payload.targetPos())) {
						alreadyLinked = true;
						break;
					}
				}
				if (alreadyLinked) {
					sorter.removeLink(payload.targetPos());
				} else {
					sorter.addOrUpdateLink(payload.targetPos());
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SetFilterItemC2S.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				BlockEntity be = player.getEntityWorld().getBlockEntity(payload.sorterPos());
				if (!(be instanceof AutoSorterBlockEntity sorter)) return;

				// В качестве образца берём предмет, который игрок держит в руке.
				ItemStack held = player.getMainHandStack();
				if (held.isEmpty()) return;
				sorter.addOrUpdateLink(payload.targetPos());
				sorter.setFilterItem(payload.targetPos(), held);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SetFilterCategoryC2S.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				BlockEntity be = player.getEntityWorld().getBlockEntity(payload.sorterPos());
				if (!(be instanceof AutoSorterBlockEntity sorter)) return;
				sorter.addOrUpdateLink(payload.targetPos());
				sorter.setFilterCategory(payload.targetPos(), payload.category());
			});
		});
	}

	// Регистрация клиентского приёмника вынесена в клиентский исходный набор,
	// см. ru.danya.autosorter.network.ClientNetworking (src/client/...),
	// т.к. main-сорсет не может ссылаться на клиентские классы (AutoSorterScreen).
}
