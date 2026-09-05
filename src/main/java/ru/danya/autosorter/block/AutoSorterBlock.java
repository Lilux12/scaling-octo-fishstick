package ru.danya.autosorter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemScatterer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.danya.autosorter.block.entity.AutoSorterBlockEntity;
import ru.danya.autosorter.block.entity.ModBlockEntities;

/**
 * Блок "Авто-сортировщик". Работает как контейнер со своим GUI.
 * Внутри: буфер входящих предметов + список привязанных сундуков с фильтрами.
 *
 * Заметки под 1.21.11:
 *  - World.isClient стало приватным полем — используем world.isClient().
 *  - onStateReplaced теперь принимает ServerWorld (а не просто World).
 *  - Inventories.dropContents с таким сигнатурой отсутствует — предметы
 *    высыпаем вручную через ItemScatterer.
 */
public class AutoSorterBlock extends BlockWithEntity {

	private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375);

	public AutoSorterBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(AutoSorterBlock::new);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new AutoSorterBlockEntity(pos, state);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
			BlockHitResult hit) {
		if (!world.isClient()) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof AutoSorterBlockEntity sorter) {
				player.openHandledScreen(sorter);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof AutoSorterBlockEntity sorter) {
			for (int i = 0; i < sorter.getInputBuffer().size(); i++) {
				ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), sorter.getInputBuffer().getStack(i));
			}
			world.updateComparators(pos, this);
		}
		super.onStateReplaced(state, world, pos, moved);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return world.isClient() ? null : checkType(type, ModBlockEntities.AUTO_SORTER, AutoSorterBlockEntity::serverTick);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <A extends BlockEntity, E extends BlockEntity> BlockEntityTicker<A> checkType(
			BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
		return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
	}
}
