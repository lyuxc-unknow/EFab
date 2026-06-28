package mcjty.efab.block;

import mcjty.efab.recipe.RecipeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class PowerOptimizerBlock extends TieredNonFullHorizontalEFabEntityBlock {

    public PowerOptimizerBlock(Properties properties, BiFunction<BlockPos, BlockState, BlockEntity> factory, RecipeTier... tiers) {
        super(properties, factory, tiers);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.45;
        double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.45;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.45;
        double vx = (random.nextDouble() - 0.5) * 0.01;
        double vy = (random.nextDouble() - 0.5) * 0.01;
        double vz = (random.nextDouble() - 0.5) * 0.01;

        level.addParticle(ParticleTypes.END_ROD, x, y, z, vx, vy, vz);
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, vx * 2.0, vy * 2.0, vz * 2.0);
        }
    }
}
