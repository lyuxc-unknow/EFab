package mcjty.efab.recipe;

import mcjty.efab.api.recipe.EFabRecipeRequirement;
import mcjty.efab.registry.ModRecipes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EFabRecipe implements Recipe<EFabRecipeInput> {

    private final List<String> pattern;
    private final Map<String, Ingredient> key;
    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private final int time;
    private final Set<RecipeTier> tiers;
    private final int fePerTick;
    private final List<FluidRequirement> fluids;
    private final List<EFabRecipeRequirement> requirements;

    public EFabRecipe(
            List<String> pattern,
            Map<String, Ingredient> key,
            List<Ingredient> ingredients,
            ItemStack result,
            int time,
            List<RecipeTier> tiers,
            int fePerTick,
            List<FluidRequirement> fluids,
            List<EFabRecipeRequirement> requirements
    ) {
        this.pattern = List.copyOf(pattern);
        this.key = Map.copyOf(key);
        this.ingredients = List.copyOf(ingredients);
        this.result = result.copy();
        this.time = time;
        this.tiers = tiers.stream().collect(Collectors.toUnmodifiableSet());
        this.fePerTick = fePerTick;
        this.fluids = List.copyOf(fluids);
        this.requirements = List.copyOf(requirements);
    }

    public List<String> pattern() {
        return pattern;
    }

    public Map<String, Ingredient> key() {
        return key;
    }

    public List<Ingredient> efabIngredients() {
        return ingredients;
    }

    public int time() {
        return time;
    }

    public Set<RecipeTier> tiers() {
        return tiers;
    }

    public int fePerTick() {
        return fePerTick;
    }

    public List<FluidRequirement> fluids() {
        return fluids;
    }

    public List<EFabRecipeRequirement> requirements() {
        return requirements;
    }

    public boolean isShaped() {
        return !pattern.isEmpty();
    }

    /**
     * The flat list of item ingredients this recipe needs (one entry per required item),
     * combining shaped pattern slots and shapeless ingredients. Used by the Crafter to
     * pull matching items from storage.
     */
    public List<Ingredient> requiredIngredients() {
        if (!isShaped()) {
            return List.copyOf(ingredients);
        }
        List<Ingredient> needed = new ArrayList<>();
        for (String row : pattern) {
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c != ' ') {
                    Ingredient ingredient = key.get(String.valueOf(c));
                    if (ingredient != null && ingredient != Ingredient.EMPTY) {
                        needed.add(ingredient);
                    }
                }
            }
        }
        return needed;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(requiredIngredients());
        return result;
    }

    @Override
    public boolean matches(EFabRecipeInput input, Level level) {
        if (isShaped()) {
            return matchesShaped(input);
        }
        return matchesShapeless(input);
    }

    private boolean matchesShaped(EFabRecipeInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int y = 0; y < 3; y++) {
            String row = y < pattern.size() ? pattern.get(y) : "";
            for (int x = 0; x < 3; x++) {
                Ingredient ingredient = Ingredient.EMPTY;
                if (x < row.length()) {
                    char c = row.charAt(x);
                    if (c != ' ') {
                        ingredient = key.getOrDefault(String.valueOf(c), Ingredient.EMPTY);
                    }
                }
                ItemStack stack = input.getItem(y * 3 + x);
                if (ingredient == Ingredient.EMPTY) {
                    if (!stack.isEmpty()) {
                        return false;
                    }
                } else if (!ingredient.test(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesShapeless(EFabRecipeInput input) {
        List<Ingredient> remaining = new ArrayList<>(ingredients);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            boolean matched = false;
            for (int j = 0; j < remaining.size(); j++) {
                if (remaining.get(j).test(stack)) {
                    remaining.remove(j);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    @Override
    public ItemStack assemble(EFabRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.EFAB_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.EFAB_TYPE.get();
    }
}
