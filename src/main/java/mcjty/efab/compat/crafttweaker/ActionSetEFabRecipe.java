package mcjty.efab.compat.crafttweaker;

import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import mcjty.efab.EFab;
import mcjty.efab.recipe.EFabRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

final class ActionSetEFabRecipe implements IRuntimeAction {

    private final EFabGridRecipeManager manager;
    private final ResourceLocation id;
    private final EFabRecipe recipe;

    ActionSetEFabRecipe(EFabGridRecipeManager manager, ResourceLocation id, EFabRecipe recipe) {
        this.manager = manager;
        this.id = id;
        this.recipe = recipe;
    }

    @Override
    public void apply() {
        manager.getRecipeList().remove(id);
        manager.getRecipeList().add(id, new RecipeHolder<>(id, recipe));
    }

    @Override
    public String describe() {
        return "Setting EFab grid recipe with name: '" + id + "'";
    }

    @Override
    public String systemName() {
        return EFab.MODID;
    }
}
