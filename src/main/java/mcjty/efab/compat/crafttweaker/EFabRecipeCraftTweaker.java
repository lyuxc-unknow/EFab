package mcjty.efab.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.efab.EFabRecipe")
@Document("mods/EFab/EFabRecipe")
public class EFabRecipeCraftTweaker {

    @ZenCodeGlobals.Global("EFabRecipe")
    public static final EFabRecipeCraftTweaker INSTANCE = new EFabRecipeCraftTweaker();

    private EFabRecipeCraftTweaker() {
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder addShaped(String recipeName, IItemStack output, IIngredient[][] ingredients) {
        return EFabGridRecipeManager.INSTANCE.createShaped(recipeName, output, ingredients);
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder addShaped(IItemStack output, IIngredient[][] ingredients) {
        return addShaped(EFabGridRecipeManager.INSTANCE.nextRecipeName(output), output, ingredients);
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder addShapeless(String recipeName, IItemStack output, IIngredient[] ingredients) {
        return EFabGridRecipeManager.INSTANCE.createShapeless(recipeName, output, ingredients);
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder addShapeless(IItemStack output, IIngredient[] ingredients) {
        return addShapeless(EFabGridRecipeManager.INSTANCE.nextRecipeName(output), output, ingredients);
    }
}
