package mcjty.efab.registry;

import mcjty.efab.EFab;
import mcjty.efab.recipe.EFabRecipe;
import mcjty.efab.recipe.EFabRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, EFab.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, EFab.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EFabRecipe>> EFAB_TYPE =
            RECIPE_TYPES.register("grid", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return EFab.MODID + ":grid";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EFabRecipe>> EFAB_SERIALIZER =
            RECIPE_SERIALIZERS.register("grid", EFabRecipeSerializer::new);

    private ModRecipes() {
    }
}
