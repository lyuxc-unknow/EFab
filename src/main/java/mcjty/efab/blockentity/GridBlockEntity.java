package mcjty.efab.blockentity;

import mcjty.efab.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GridBlockEntity extends AbstractCraftingBlockEntity {

    public GridBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRID.get(), pos, state);
    }
}
