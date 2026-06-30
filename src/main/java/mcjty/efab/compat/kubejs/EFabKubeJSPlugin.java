package mcjty.efab.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import mcjty.efab.EFab;
import mcjty.efab.api.kubejs.component.EFabKubeJSComponents;

public class EFabKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        registry.register(EFabKubeJSComponents.JSON_TYPE);
        registry.register(EFabKubeJSComponents.SHAPED_MATRIX_TYPE);
        registry.register(EFabKubeJSComponents.FLUID_REQUIREMENT_TYPE);
        registry.register(EFabKubeJSComponents.RECIPE_REQUIREMENT_TYPE);
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(EFab.rl("add_shaped"), EFabGridShapedRecipeJSBuilder.class, EFabGridShapedRecipeJSBuilder::new);
        registry.register(EFab.rl("add_shapeless"), EFabGridShapelessRecipeJSBuilder.class, EFabGridShapelessRecipeJSBuilder::new);
    }
}
