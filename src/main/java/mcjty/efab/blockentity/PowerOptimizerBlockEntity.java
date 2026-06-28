package mcjty.efab.blockentity;

import mcjty.efab.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PowerOptimizerBlockEntity extends BlockEntity {

    public PowerOptimizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POWER_OPTIMIZER.get(), pos, state);
    }
}
