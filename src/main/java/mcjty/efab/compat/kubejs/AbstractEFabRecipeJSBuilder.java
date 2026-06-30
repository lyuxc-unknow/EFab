package mcjty.efab.compat.kubejs;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;
import dev.latvian.mods.rhino.util.HideFromJS;
import mcjty.efab.api.kubejs.RecipeJSBuilder;
import mcjty.efab.api.recipe.EFabRecipeRequirement;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEFabRecipeJSBuilder extends KubeRecipe implements RecipeJSBuilder {

    @HideFromJS
    @Override
    public RecipeJSBuilder addRequirement(EFabRecipeRequirement requirement) {
        return addRecipeListValue("requirements", requirement);
    }

    @HideFromJS
    @Override
    public RecipeJSBuilder setRecipeValue(String key, Object value) {
        RecipeKey<Object> recipeKey = findKey(key);
        setValue(recipeKey, value);
        return this;
    }

    @HideFromJS
    @Override
    public RecipeJSBuilder addRecipeListValue(String key, Object value) {
        List<Object> values = new ArrayList<>((List<Object>) get(key));
        values.add(value);
        return setRecipeValue(key, values);
    }

    @HideFromJS
    @Override
    public RecipeJSBuilder error(String error, Object... arguments) {
        throw new KubeRuntimeException(MessageFormatter.arrayFormat(error, arguments).getMessage()).source(this.sourceLine);
    }

    @HideFromJS
    @SuppressWarnings("unchecked")
    private RecipeKey<Object> findKey(String name) {
        for (RecipeComponentValue<?> value : getRecipeComponentValues()) {
            if (value.key.name.equals(name)) {
                return (RecipeKey<Object>) value.key;
            }
        }
        throw new IllegalArgumentException("Unknown EFab KubeJS recipe key: " + name);
    }
}
