package mcjty.efab.compat.kubejs.builder;

import mcjty.efab.api.kubejs.RecipeJSBuilder;

public interface TimeJS extends RecipeJSBuilder {

    default RecipeJSBuilder time(int ticks) {
        if (ticks <= 0) {
            return error("EFab recipe time must be greater than zero");
        }
        return setRecipeValue("time", ticks);
    }
}
