package mcjty.efab.api.kubejs.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public record ShapedMatrixComponent(RecipeComponentType<?> type) implements RecipeComponent<ShapedMatrix> {

    private static final RecipeComponent<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.instance();
    private static final Codec<ShapedMatrix> CODEC = Ingredient.CODEC.listOf()
            .listOf()
            .xmap(ShapedMatrix::new, ShapedMatrix::rows);
    private static final String SYMBOLS = "abcdefghi";

    @Override
    public Codec<ShapedMatrix> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.OBJECT;
    }

    @Override
    public ShapedMatrix wrap(RecipeScriptContext context, Object from) {
        JsonArray array = JsonUtils.arrayOf(context.cx(), from);
        if (array == null) throw new IllegalStateException("array is null");
        if (array.isEmpty() || array.size() > 3) {
            throw new IllegalArgumentException("EFab shaped recipe matrix must have 1 to 3 rows");
        }

        List<List<Ingredient>> rows = new ArrayList<>();
        for (int y = 0; y < array.size(); y++) {
            JsonElement rowElement = array.get(y);
            if (!rowElement.isJsonArray()) {
                throw new IllegalArgumentException("EFab shaped recipe matrix row " + y + " must be an array");
            }
            JsonArray rowArray = rowElement.getAsJsonArray();
            if (rowArray.isEmpty() || rowArray.size() > 3) {
                throw new IllegalArgumentException("EFab shaped recipe matrix rows must have 1 to 3 columns");
            }

            List<Ingredient> row = new ArrayList<>();
            for (JsonElement cell : rowArray) {
                row.add(wrapIngredient(context, cell));
            }
            rows.add(List.copyOf(row));
        }
        return new ShapedMatrix(List.copyOf(rows));
    }

    @Override
    public void writeToJson(KubeRecipe recipe, RecipeComponentValue<ShapedMatrix> value, JsonObject json) {
        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        int symbolIndex = 0;

        for (List<Ingredient> row : value.value.rows()) {
            StringBuilder patternRow = new StringBuilder();
            for (Ingredient ingredient : row) {
                if (ingredient == null || ingredient.isEmpty()) {
                    patternRow.append(' ');
                    continue;
                }
                if (symbolIndex >= SYMBOLS.length()) {
                    throw new IllegalArgumentException("EFab shaped recipe matrix can have at most 9 non-empty ingredients");
                }
                String symbol = SYMBOLS.substring(symbolIndex, symbolIndex + 1);
                symbolIndex++;
                patternRow.append(symbol);
                key.add(symbol, encodeIngredient(ingredient));
            }
            pattern.add(patternRow.toString());
        }

        json.add("pattern", pattern);
        json.add("key", key);
    }

    @Override
    public boolean allowEmpty() {
        return RecipeComponent.super.allowEmpty();
    }

    private static Ingredient wrapIngredient(RecipeScriptContext context, JsonElement cell) {
        if (cell == null || cell.isJsonNull()) {
            return Ingredient.EMPTY;
        }
        if (cell.isJsonPrimitive() && cell.getAsJsonPrimitive().isString() && cell.getAsString().isBlank()) {
            return Ingredient.EMPTY;
        }
        return INGREDIENT.wrap(context, JsonUtils.toObject(cell));
    }

    private static JsonElement encodeIngredient(Ingredient ingredient) {
        return Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, ingredient)
                .getOrThrow(message -> new IllegalArgumentException("Failed to encode EFab shaped ingredient: " + message));
    }
}
