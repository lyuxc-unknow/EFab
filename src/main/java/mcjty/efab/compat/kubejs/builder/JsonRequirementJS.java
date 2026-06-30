package mcjty.efab.compat.kubejs.builder;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import mcjty.efab.api.kubejs.RecipeJSBuilder;
import mcjty.efab.api.recipe.EFabRecipeRequirement;
import mcjty.efab.api.recipe.EFabRecipeRequirements;

public interface JsonRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requirement(EFabRecipeRequirement requirement) {
        return addRequirement(requirement);
    }

    default RecipeJSBuilder requirement(JsonObject json) {
        EFabRecipeRequirement requirement = EFabRecipeRequirements.CODEC.parse(JsonOps.INSTANCE, json)
                .getOrThrow(message -> new IllegalArgumentException("Failed to parse EFab requirement: " + message));
        return addRequirement(requirement);
    }
}
