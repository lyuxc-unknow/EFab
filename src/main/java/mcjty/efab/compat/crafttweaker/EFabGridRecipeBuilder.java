package mcjty.efab.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import mcjty.efab.api.recipe.EFabRecipeRequirement;
import mcjty.efab.recipe.EFabRecipe;
import mcjty.efab.recipe.FluidRequirement;
import mcjty.efab.recipe.RecipeTier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType;

import java.util.*;

@ZenRegister
@ZenCodeType.Name("mods.efab.GridRecipeBuilder")
@Document("mods/EFab/GridRecipeBuilder")
public class EFabGridRecipeBuilder {

    private final EFabGridRecipeManager manager;
    private final String recipeName;
    private final IItemStack output;
    private final List<String> pattern = new ArrayList<>();
    private final Map<String, Ingredient> key = new LinkedHashMap<>();
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<RecipeTier> tiers = new ArrayList<>();
    private final List<FluidRequirement> fluids = new ArrayList<>();
    private final List<EFabRecipeRequirement> requirements = new ArrayList<>();
    private int time = 40;
    private int fePerTick = 0;
    private boolean autoApply = false;

    EFabGridRecipeBuilder(EFabGridRecipeManager manager, String recipeName, IItemStack output) {
        this.manager = manager;
        this.recipeName = recipeName;
        this.output = output;
    }

    EFabGridRecipeBuilder ingredients(IIngredient[] ingredients) {
        for (IIngredient ingredient : ingredients) {
            this.ingredients.add(ingredient.asVanillaIngredient());
        }
        applyIfAuto();
        return this;
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder tier(String tier) {
        this.tiers.add(parseTier(tier));
        applyIfAuto();
        return this;
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder tiers(String[] tiers) {
        for (String tier : tiers) {
            this.tiers.add(parseTier(tier));
        }
        applyIfAuto();
        return this;
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder time(int time) {
        if (time <= 0) {
            throw new IllegalArgumentException("EFab recipe time must be greater than zero");
        }
        this.time = time;
        applyIfAuto();
        return this;
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder fePerTick(int fePerTick) {
        if (fePerTick < 0) {
            throw new IllegalArgumentException("EFab recipe FE/t must not be negative");
        }
        this.fePerTick = fePerTick;
        applyIfAuto();
        return this;
    }

    @ZenCodeType.Method
    public EFabGridRecipeBuilder fluid(IFluidStack fluid) {
        if (fluid.isEmpty()) {
            throw new IllegalArgumentException("EFab recipe fluid requirement must not be empty");
        }
        long amount = fluid.getAmount();
        if (amount <= 0 || amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("EFab recipe fluid amount must be between 1 and " + Integer.MAX_VALUE);
        }
        this.fluids.add(new FluidRequirement(fluid.getRegistryName(), (int) amount));
        applyIfAuto();
        return this;
    }

    EFabGridRecipeBuilder shapedIngredients(IIngredient[][] ingredients) {
        this.pattern.clear();
        this.key.clear();
        String symbols = "abcdefghi";
        int symbolIndex = 0;
        if (ingredients.length > 3) {
            throw new IllegalArgumentException("EFab shaped recipe matrix can have at most 3 rows");
        }
        for (IIngredient[] rowIngredients : ingredients) {
            if (rowIngredients.length > 3) {
                throw new IllegalArgumentException("EFab shaped recipe matrix rows can have at most 3 ingredients");
            }
            StringBuilder row = new StringBuilder();
            for (IIngredient ingredient : rowIngredients) {
                if (ingredient == null || ingredient.isEmpty()) {
                    row.append(' ');
                } else {
                    String symbol = symbols.substring(symbolIndex, symbolIndex + 1);
                    symbolIndex++;
                    row.append(symbol);
                    this.key.put(symbol, ingredient.asVanillaIngredient());
                }
            }
            this.pattern.add(row.toString());
        }
        return this;
    }

    EFabGridRecipeBuilder autoApply() {
        this.autoApply = true;
        applyRecipe();
        return this;
    }

    private void applyIfAuto() {
        if (autoApply) {
            applyRecipe();
        }
    }

    private void applyRecipe() {
        validateRecipe();
        ItemStack result = output.getImmutableInternal();
        manager.setRecipe(recipeName, new EFabRecipe(
                pattern,
                key,
                ingredients,
                result,
                time,
                tiers,
                fePerTick,
                fluids,
                requirements
        ));
    }

    private void validateRecipe() {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("EFab recipe output must not be empty");
        }
        if (!pattern.isEmpty() && !ingredients.isEmpty()) {
            throw new IllegalArgumentException("EFab recipe cannot be both shaped and shapeless");
        }
        if (pattern.isEmpty() && ingredients.isEmpty()) {
            throw new IllegalArgumentException("EFab recipe needs a pattern or at least one ingredient");
        }
        validatePattern();
        validateKeyUsage();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                throw new IllegalArgumentException("EFab recipe shapeless ingredients must not be empty");
            }
        }
    }

    private void validatePattern() {
        if (pattern.isEmpty()) {
            return;
        }
        if (pattern.size() > 3) {
            throw new IllegalArgumentException("EFab shaped recipe pattern can have at most 3 rows");
        }
        for (String row : pattern) {
            if (row.isEmpty() || row.length() > 3) {
                throw new IllegalArgumentException("EFab shaped recipe pattern rows must be 1 to 3 characters wide");
            }
            for (int i = 0; i < row.length(); i++) {
                String symbol = row.substring(i, i + 1);
                if (!symbol.equals(" ") && !key.containsKey(symbol)) {
                    throw new IllegalArgumentException("EFab shaped recipe pattern uses undefined key: '" + symbol + "'");
                }
            }
        }
    }

    private void validateKeyUsage() {
        if (pattern.isEmpty()) {
            return;
        }
        for (String symbol : key.keySet()) {
            boolean used = pattern.stream().anyMatch(row -> row.contains(symbol));
            if (!used) {
                throw new IllegalArgumentException("EFab shaped recipe key is not used in the pattern: '" + symbol + "'");
            }
        }
    }

    private static RecipeTier parseTier(String tier) {
        try {
            return RecipeTier.valueOf(tier.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown EFab recipe tier: " + tier, e);
        }
    }

    public EFabGridRecipeBuilder addRequirements(EFabRecipeRequirement... requirements) {
        this.requirements.addAll(Arrays.asList(requirements));
        applyIfAuto();
        return this;
    }
}
