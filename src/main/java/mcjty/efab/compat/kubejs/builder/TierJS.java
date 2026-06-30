package mcjty.efab.compat.kubejs.builder;

import mcjty.efab.api.kubejs.RecipeJSBuilder;

public interface TierJS extends RecipeJSBuilder {

    default RecipeJSBuilder tier(String tier) {
        return addRecipeListValue("tiers", tier);
    }
}
