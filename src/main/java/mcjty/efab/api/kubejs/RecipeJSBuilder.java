package mcjty.efab.api.kubejs;

import mcjty.efab.api.recipe.EFabRecipeRequirement;

public interface RecipeJSBuilder {

    RecipeJSBuilder addRequirement(EFabRecipeRequirement requirement);

    RecipeJSBuilder setRecipeValue(String key, Object value);

    RecipeJSBuilder addRecipeListValue(String key, Object value);

    RecipeJSBuilder error(String error, Object... arguments);
}
