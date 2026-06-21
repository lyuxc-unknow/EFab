package mcjty.efab.block;

import mcjty.efab.recipe.RecipeTier;
import mcjty.efab.tooltip.EFabTooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
public class TierPartBlock extends Block implements TierProvider {

    private final Set<RecipeTier> tiers;

    public TierPartBlock(Properties properties, RecipeTier... tiers) {
        super(properties);
        this.tiers = tiers.length == 0 ? Set.of() : EnumSet.copyOf(Set.of(tiers));
    }

    @Override
    public Set<RecipeTier> getTiers() {
        return tiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EFabTooltips.appendBlockTooltip(this, tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
