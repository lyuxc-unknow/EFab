package mcjty.efab.api.kubejs.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;
import mcjty.efab.EFab;
import mcjty.efab.api.recipe.EFabRecipeRequirement;
import mcjty.efab.api.recipe.EFabRecipeRequirements;
import mcjty.efab.recipe.FluidRequirement;

public final class EFabKubeJSComponents {

    public static final Codec<JsonElement> JSON_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json == null ? JsonNull.INSTANCE : json)
    );

    public static final RecipeComponentType.Unit<JsonElement> JSON_TYPE =
            RecipeComponentType.unit(EFab.rl("json"), JsonRecipeComponent::new);
    public static final RecipeComponent<JsonElement> JSON = JSON_TYPE.instance();

    public static final RecipeComponentType.Unit<ShapedMatrix> SHAPED_MATRIX_TYPE =
            RecipeComponentType.unit(EFab.rl("shaped_matrix"), ShapedMatrixComponent::new);
    public static final RecipeComponent<ShapedMatrix> SHAPED_MATRIX = SHAPED_MATRIX_TYPE.instance();

    public static final RecipeComponentType.Unit<FluidRequirement> FLUID_REQUIREMENT_TYPE =
            RecipeComponentType.unit(EFab.rl("fluid_requirement"), type -> new RecipeComponent<>() {
                @Override
                public RecipeComponentType<?> type() {
                    return type;
                }

                @Override
                public com.mojang.serialization.Codec<FluidRequirement> codec() {
                    return FluidRequirement.CODEC;
                }

                @Override
                public TypeInfo typeInfo() {
                    return TypeInfo.of(FluidRequirement.class);
                }
            });
    public static final RecipeComponent<FluidRequirement> FLUID_REQUIREMENT = FLUID_REQUIREMENT_TYPE.instance();

    public static final RecipeComponentType.Unit<EFabRecipeRequirement> RECIPE_REQUIREMENT_TYPE =
            RecipeComponentType.unit(EFab.rl("recipe_requirement"), type -> new RecipeComponent<>() {
                @Override
                public RecipeComponentType<?> type() {
                    return type;
                }

                @Override
                public com.mojang.serialization.Codec<EFabRecipeRequirement> codec() {
                    return EFabRecipeRequirements.CODEC;
                }

                @Override
                public TypeInfo typeInfo() {
                    return TypeInfo.of(EFabRecipeRequirement.class);
                }
            });
    public static final RecipeComponent<EFabRecipeRequirement> RECIPE_REQUIREMENT = RECIPE_REQUIREMENT_TYPE.instance();

    private EFabKubeJSComponents() {
    }
}
