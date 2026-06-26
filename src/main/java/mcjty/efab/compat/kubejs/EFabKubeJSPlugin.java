package mcjty.efab.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.CustomObjectRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeMappingRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.recipe.schema.function.RecipeFunctionInstance;
import dev.latvian.mods.kubejs.recipe.schema.function.ResolvedRecipeSchemaFunction;
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TinyMap;
import mcjty.efab.EFab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EFabKubeJSPlugin implements KubeJSPlugin {

    private static final Pattern FLUID_SPEC = Pattern.compile("^(\\d+)\\s*x\\s+(.+)$");
    private static final CustomObjectRecipeComponent.Key FLUID_ID_KEY =
            new CustomObjectRecipeComponent.Key("fluid", StringComponent.ID.instance());
    private static final CustomObjectRecipeComponent.Key FLUID_AMOUNT_KEY =
            new CustomObjectRecipeComponent.Key("amount", NumberComponent.POSITIVE_INT.instance());
    private static final CustomObjectRecipeComponent FLUID_REQUIREMENT =
            RecipeComponent.builder(FLUID_ID_KEY, FLUID_AMOUNT_KEY);

    private static final RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
    private static final RecipeKey<List<String>> PATTERN = StringComponent.STRING.instance().asList().otherKey("pattern");
    private static final RecipeKey<TinyMap<Character, Ingredient>> KEY = IngredientComponent.INGREDIENT.instance().asPatternKey().inputKey("key");
    private static final RecipeKey<List<Ingredient>> INGREDIENTS = IngredientComponent.INGREDIENT.instance().asList().inputKey("ingredients");
    private static final RecipeKey<Integer> TIME = NumberComponent.POSITIVE_INT.otherKey("time").optional(40);
    private static final RecipeKey<List<String>> TIERS = StringComponent.STRING.instance()
            .asList()
            .withBounds(IntBounds.OPTIONAL)
            .otherKey("tiers")
            .optional(List.of());
    private static final RecipeKey<Integer> FE_PER_TICK = NumberComponent.NON_NEGATIVE_INT.otherKey("fe_per_tick").functionNames("fePerTick").optional(0);
    private static final RecipeKey<List<List<CustomObjectRecipeComponent.Value>>> FLUIDS = FLUID_REQUIREMENT
            .asList()
            .withBounds(IntBounds.OPTIONAL)
            .otherKey("fluids")
            .noFunctions()
            .optional(List.of());

    @Override
    public void registerRecipeMappings(RecipeMappingRegistry registry) {
        registry.register("efabGridShaped", EFab.rl("grid_shaped"));
        registry.register("efabGridShapeless", EFab.rl("grid_shapeless"));
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(EFab.rl("grid_shaped"), gridShapedSchema());
        registry.register(EFab.rl("grid_shapeless"), gridShapelessSchema());
    }

    private static RecipeSchema gridShapedSchema() {
        return efabSchema(RESULT, PATTERN, KEY, TIME, TIERS, FE_PER_TICK, FLUIDS)
                .constructor(new RecipeConstructor(RESULT, PATTERN, KEY))
                .uniqueId(RESULT);
    }

    private static RecipeSchema gridShapelessSchema() {
        return efabSchema(RESULT, INGREDIENTS, TIME, TIERS, FE_PER_TICK, FLUIDS)
                .constructor(new RecipeConstructor(RESULT, INGREDIENTS))
                .uniqueId(RESULT);
    }

    private static RecipeSchema efabSchema(RecipeKey<?>... keys) {
        return new RecipeSchema(keys)
                .typeOverride(EFab.rl("grid"))
                .addToListOpFunction("tier", TIERS)
                .function(fluidFunction());
    }

    private static RecipeFunctionInstance fluidFunction() {
        ResolvedRecipeSchemaFunction function = new ResolvedRecipeSchemaFunction() {
            @Override
            public List<RecipeComponent<?>> arguments() {
                return List.of(StringComponent.STRING.instance());
            }

            @Override
            public void execute(dev.latvian.mods.kubejs.recipe.RecipeScriptContext cx, List<Object> args) {
                List<List<CustomObjectRecipeComponent.Value>> fluids = cx.recipe().getValue(FLUIDS);
                List<List<CustomObjectRecipeComponent.Value>> updated = fluids == null ? new ArrayList<>() : new ArrayList<>(fluids);
                updated.add(parseFluidRequirement(String.valueOf(args.getFirst())));
                cx.recipe().setValue(FLUIDS, updated);
            }
        };
        return new RecipeFunctionInstance("fluid", function);
    }

    private static List<CustomObjectRecipeComponent.Value> parseFluidRequirement(String spec) {
        Matcher matcher = FLUID_SPEC.matcher(spec.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("EFab fluid requirement must be '<amount>x <fluid id>', for example '500x minecraft:water'");
        }

        int amount;
        try {
            amount = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("EFab fluid amount is too large: " + matcher.group(1), e);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("EFab fluid amount must be greater than zero");
        }

        ResourceLocation fluid = ResourceLocation.parse(matcher.group(2).trim());
        return List.of(
                new CustomObjectRecipeComponent.Value(FLUID_ID_KEY, 0, fluid.toString()),
                new CustomObjectRecipeComponent.Value(FLUID_AMOUNT_KEY, 1, amount)
        );
    }
}
