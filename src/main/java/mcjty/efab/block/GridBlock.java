package mcjty.efab.block;

import mcjty.efab.registry.ModBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GridBlock extends NonFullHorizontalEFabEntityBlock {

    public static final BooleanProperty HALF = BooleanProperty.create("half");
    public static final double HALF_HEIGHT = 0.1875;

    public GridBlock(Properties properties, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
        super(properties, factory);
        registerDefaultState(defaultBlockState().setValue(HALF, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(HALF, isHalfBlock(context.getLevel(), context.getClickedPos()));
    }

    private static boolean isHalfBlock(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModBlocks.BASE.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF)) {
            return Shapes.box(0, 0, 0, 1, HALF_HEIGHT, 1);
        }
        return super.getShape(state, level, pos, context);
    }
}
