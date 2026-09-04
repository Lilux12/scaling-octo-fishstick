package ru.danya.autosorter.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ru.danya.autosorter.AutoSorterMod;
import ru.danya.autosorter.block.entity.FilterCategory;

import java.util.List;

/**
 * Пакеты для вкладки "Настройки". Слоты инвентаря для неё не подходят
 * (список сундуков динамический), поэтому используем прямой обмен пакетами.
 */
public class ModPayloads {

	// --- Клиент -> Сервер: запросить список сундуков рядом с сортировщиком ---
	public record RequestChestListC2S(BlockPos sorterPos) implements CustomPayload {
		public static final Id<RequestChestListC2S> ID =
				new Id<>(Identifier.of(AutoSorterMod.MOD_ID, "request_chest_list"));
		public static final PacketCodec<RegistryByteBuf, RequestChestListC2S> CODEC =
				PacketCodec.tuple(BlockPos.PACKET_CODEC, RequestChestListC2S::sorterPos, RequestChestListC2S::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	// --- Сервер -> Клиент: список найденных сундуков + текущие привязки ---
	public record ChestListS2C(BlockPos sorterPos, List<ChestEntry> entries) implements CustomPayload {
		public static final Id<ChestListS2C> ID =
				new Id<>(Identifier.of(AutoSorterMod.MOD_ID, "chest_list"));
		public static final PacketCodec<RegistryByteBuf, ChestListS2C> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, ChestListS2C::sorterPos,
				ChestEntry.CODEC.collect(PacketCodecs.toList()), ChestListS2C::entries,
				ChestListS2C::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record ChestEntry(BlockPos pos, String label, boolean linked, ItemStack filterItem, FilterCategory category) {
		public static final PacketCodec<RegistryByteBuf, ChestEntry> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, ChestEntry::pos,
				PacketCodecs.STRING, ChestEntry::label,
				PacketCodecs.BOOLEAN, ChestEntry::linked,
				ItemStack.OPTIONAL_PACKET_CODEC, ChestEntry::filterItem,
				PacketCodecs.STRING.xmap(FilterCategory::valueOf, Enum::name), ChestEntry::category,
				ChestEntry::new);
	}

	// --- Клиент -> Сервер: добавить/убрать привязку ---
	public record ToggleLinkC2S(BlockPos sorterPos, BlockPos targetPos) implements CustomPayload {
		public static final Id<ToggleLinkC2S> ID =
				new Id<>(Identifier.of(AutoSorterMod.MOD_ID, "toggle_link"));
		public static final PacketCodec<RegistryByteBuf, ToggleLinkC2S> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, ToggleLinkC2S::sorterPos,
				BlockPos.PACKET_CODEC, ToggleLinkC2S::targetPos,
				ToggleLinkC2S::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	// --- Клиент -> Сервер: назначить фильтр-предмет (берётся предмет с курсора игрока) ---
	public record SetFilterItemC2S(BlockPos sorterPos, BlockPos targetPos) implements CustomPayload {
		public static final Id<SetFilterItemC2S> ID =
				new Id<>(Identifier.of(AutoSorterMod.MOD_ID, "set_filter_item"));
		public static final PacketCodec<RegistryByteBuf, SetFilterItemC2S> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, SetFilterItemC2S::sorterPos,
				BlockPos.PACKET_CODEC, SetFilterItemC2S::targetPos,
				SetFilterItemC2S::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	// --- Клиент -> Сервер: назначить категорию вместо предмета ---
	public record SetFilterCategoryC2S(BlockPos sorterPos, BlockPos targetPos, FilterCategory category) implements CustomPayload {
		public static final Id<SetFilterCategoryC2S> ID =
				new Id<>(Identifier.of(AutoSorterMod.MOD_ID, "set_filter_category"));
		public static final PacketCodec<RegistryByteBuf, SetFilterCategoryC2S> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, SetFilterCategoryC2S::sorterPos,
				BlockPos.PACKET_CODEC, SetFilterCategoryC2S::targetPos,
				PacketCodecs.STRING.xmap(FilterCategory::valueOf, Enum::name), SetFilterCategoryC2S::category,
				SetFilterCategoryC2S::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
