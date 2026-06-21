package mcjty.efab.block;

import mcjty.efab.blockentity.TankBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TankBlock extends NonFullHorizontalEFabEntityBlock {

    public static final EnumProperty<TankPart> PART = EnumProperty.create("part", TankPart.class);

    public TankBlock(Properties properties, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
        super(properties, factory);
        registerDefaultState(defaultBlockState().setValue(PART, TankPart.FULL));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.setValue(PART, getPart(context.getLevel(), context.getClickedPos(), this));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return updated.setValue(PART, getPart(level, pos, this));
        }
        return updated;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        rebuildColumn(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            rebuildColumn(level, pos.below());
            rebuildColumn(level, pos.above());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    private static TankPart getPart(LevelAccessor level, BlockPos pos, Block block) {
        boolean tankUp = level.getBlockState(pos.above()).is(block);
        boolean tankDown = level.getBlockState(pos.below()).is(block);
        if (tankUp && tankDown) {
            return TankPart.MIDDLE;
        } else if (tankUp) {
            return TankPart.BOTTOM;
        } else if (tankDown) {
            return TankPart.TOP;
        } else {
            return TankPart.FULL;
        }
    }

    private static void rebuildColumn(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof TankBlockEntity tank) {
            tank.rebuildMultiblock();
        }
    }

    public enum TankPart implements StringRepresentable {
        FULL("full"),
        BOTTOM("bottom"),
        TOP("top"),
        MIDDLE("middle");

        private final String serializedName;

        TankPart(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
