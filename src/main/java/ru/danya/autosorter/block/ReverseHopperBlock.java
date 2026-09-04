package ru.danya.autosorter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import ru.danya.autosorter.block.entity.ModBlockEntities;
import ru.danya.autosorter.block.entity.ReverseHopperBlockEntity;

/**
 * "Обратная воронка": зеркало ванильной воронки, но тянет предметы
 * из инвентаря СНИЗУ и толкает их в инвентарь СВЕРХУ.
 * Нужна, чтобы поднимать предметы к сундукам, расположенным выше сортировщика.
 */
public class ReverseHopperBlock extends BlockWithEntity {

	public ReverseHopperBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(ReverseHopperBlock::new);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ReverseHopperBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.World world, BlockState state,
			BlockEntityType<T> type) {
		return world.isClient ? null : checkType(type, ModBlockEntities.REVERSE_HOPPER, ReverseHopperBlockEntity::serverTick);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> checkType(
			BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
		return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
	}
}
