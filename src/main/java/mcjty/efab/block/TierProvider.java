package mcjty.efab.block;

import mcjty.efab.recipe.RecipeTier;

import java.util.Set;

public interface TierProvider {
    Set<RecipeTier> getTiers();
}
