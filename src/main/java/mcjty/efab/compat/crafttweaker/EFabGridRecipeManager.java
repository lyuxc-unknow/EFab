package mcjty.efab.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mcjty.efab.recipe.EFabRecipe;
import mcjty.efab.registry.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.concurrent.atomic.AtomicInteger;

final class EFabGridRecipeManager implements IRecipeManager<EFabRecipe> {

    static final EFabGridRecipeManager INSTANCE = new EFabGridRecipeManager();
    private static final AtomicInteger NEXT_RECIPE_ID = new AtomicInteger();

    private EFabGridRecipeManager() {
    }

    EFabGridRecipeBuilder createShaped(String recipeName, IItemStack output, IIngredient[][] ingredients) {
        return new EFabGridRecipeBuilder(this, recipeName, output)
                .shapedIngredients(ingredients)
                .autoApply();
    }

    EFabGridRecipeBuilder createShapeless(String recipeName, IItemStack output, IIngredient[] ingredients) {
        return new EFabGridRecipeBuilder(this, recipeName, output)
                .ingredients(ingredients)
                .autoApply();
    }

    String nextRecipeName(IItemStack output) {
        ResourceLocation outputName = output.getRegistryName();
        String path = outputName.getPath().replace('/', '_');
        return "efab_" + outputName.getNamespace() + "_" + path + "_" + NEXT_RECIPE_ID.getAndIncrement();
    }

    void setRecipe(String recipeName, EFabRecipe recipe) {
        setRecipe(fixRecipeId(recipeName), recipe);
    }

    private void setRecipe(ResourceLocation id, EFabRecipe recipe) {
        CraftTweakerAPI.apply(new ActionSetEFabRecipe(this, id, recipe));
    }

    @Override
    public RecipeType<EFabRecipe> getRecipeType() {
        return ModRecipes.EFAB_TYPE.get();
    }
}
