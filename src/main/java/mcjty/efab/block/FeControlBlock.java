package mcjty.efab.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
public class FeControlBlock extends NonFullHorizontalEFabEntityBlock {

    public static final BooleanProperty SPARKS = BooleanProperty.create("sparks");

    public FeControlBlock(Properties properties, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
        super(properties, factory);
        registerDefaultState(defaultBlockState().setValue(SPARKS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SPARKS);
    }
}
