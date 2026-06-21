package mcjty.efab.api.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Runtime view of an EFab craft. Custom recipe requirements can use this to
 * find nearby blocks and interact with their own mod's capabilities or APIs.
 */
public interface EFabCraftingContext {

    Level level();

    BlockEntity machine();

    BlockPos origin();

    Iterable<BlockPos> craftingArea();

    boolean automated();

    int progress();

    int requiredTime();

    int speedBonus();
}
