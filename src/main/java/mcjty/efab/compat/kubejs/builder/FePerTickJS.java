package mcjty.efab.compat.kubejs.builder;

import mcjty.efab.api.kubejs.RecipeJSBuilder;

public interface FePerTickJS extends RecipeJSBuilder {

    default RecipeJSBuilder fePerTick(int amount) {
        if (amount < 0) {
            return error("EFab FE per tick must not be negative");
        }
        return setRecipeValue("fe_per_tick", amount);
    }
}
