package mcjty.efab.api.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A data-driven requirement attached to an EFab recipe.
 *
 * <p>The requirement receives every phase. Return {@code true} for phases your
 * requirement does not care about. When {@code simulate} is {@code true}, the
 * method must only check availability and must not mutate world state.</p>
 */
public interface EFabRecipeRequirement {

    EFabRecipeRequirementType<? extends EFabRecipeRequirement> type();

    boolean apply(EFabCraftingContext context, EFabRequirementPhase phase, int amount, boolean simulate);

    default Component description() {
        ResourceLocation id = EFabRecipeRequirements.getId(type());
        return Component.literal(id.toString());
    }
}
