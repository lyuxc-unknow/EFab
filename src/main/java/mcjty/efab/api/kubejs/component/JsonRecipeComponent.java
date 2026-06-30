package mcjty.efab.api.kubejs.component;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;

public record JsonRecipeComponent(RecipeComponentType<?> type) implements RecipeComponent<JsonElement> {

    @Override
    public Codec<JsonElement> codec() {
        return EFabKubeJSComponents.JSON_CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.OBJECT;
    }

    @Override
    public JsonElement wrap(dev.latvian.mods.kubejs.recipe.RecipeScriptContext context, Object from) {
        if (from instanceof JsonElement jsonElement) {
            return JsonUtils.copy(jsonElement);
        }
        return JsonUtils.of(context.cx(), from);
    }

    @Override
    public boolean allowEmpty() {
        return true;
    }
}
