package mcjty.efab.block;

import mcjty.efab.recipe.RecipeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiFunction;

public class TieredNonFullHorizontalEFabEntityBlock extends NonFullHorizontalEFabEntityBlock implements TierProvider {

    private final Set<RecipeTier> tiers;

    public TieredNonFullHorizontalEFabEntityBlock(Properties properties,
                                                  BiFunction<BlockPos, BlockState, BlockEntity> factory,
                                                  RecipeTier... tiers) {
        super(properties, factory);
        this.tiers = tiers.length == 0 ? Set.of() : EnumSet.copyOf(Set.of(tiers));
    }

    @Override
    public Set<RecipeTier> getTiers() {
        return tiers;
    }
}
