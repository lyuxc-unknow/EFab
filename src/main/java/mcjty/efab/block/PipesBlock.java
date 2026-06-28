package mcjty.efab.block;

import mcjty.efab.recipe.RecipeTier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@MethodsReturnNonnullByDefault
public class PipesBlock extends NonFullHorizontalTierPartBlock {

    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 1);

    private static final RandomSource RANDOM = RandomSource.create();

    public PipesBlock(Properties properties, RecipeTier... tiers) {
        super(properties, tiers);
        registerDefaultState(defaultBlockState().setValue(STATE, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(STATE, RANDOM.nextInt(2));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE);
    }
}
