package mcjty.efab.compat.kubejs.builder;

import mcjty.efab.api.kubejs.RecipeJSBuilder;
import mcjty.efab.recipe.FluidRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface FluidJS extends RecipeJSBuilder {

    Pattern FLUID_SPEC = Pattern.compile("^(\\d+)\\s*x\\s+(.+)$");

    default RecipeJSBuilder fluid(String spec) {
        Matcher matcher = FLUID_SPEC.matcher(spec.trim());
        if (!matcher.matches()) {
            return error("EFab fluid requirement must be '<amount>x <fluid id>', for example '500x minecraft:water'");
        }

        int amount;
        try {
            amount = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return error("EFab fluid amount is too large: {}", matcher.group(1));
        }
        return fluid(matcher.group(2).trim(), amount);
    }

    default RecipeJSBuilder fluid(String fluid, int amount) {
        if (amount <= 0) {
            return error("EFab fluid amount must be greater than zero");
        }
        return addRecipeListValue("fluids", new FluidRequirement(ResourceLocation.parse(fluid), amount));
    }
}
