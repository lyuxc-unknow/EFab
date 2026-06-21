package mcjty.efab.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.efab.api.recipe.EFabRecipeRequirement;
import mcjty.efab.api.recipe.EFabRecipeRequirements;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EFabRecipeSerializer implements RecipeSerializer<EFabRecipe> {

    private static final MapCodec<EFabRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("pattern", List.of()).forGetter(EFabRecipe::pattern),
            Codec.unboundedMap(Codec.STRING, Ingredient.CODEC_NONEMPTY).optionalFieldOf("key", Map.of()).forGetter(EFabRecipe::key),
            Ingredient.CODEC_NONEMPTY.listOf().optionalFieldOf("ingredients", List.of()).forGetter(EFabRecipe::efabIngredients),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(null)),
            Codec.INT.optionalFieldOf("time", 40).forGetter(EFabRecipe::time),
            RecipeTier.CODEC.listOf().optionalFieldOf("tiers", List.of()).forGetter(recipe -> List.copyOf(recipe.tiers())),
            Codec.INT.optionalFieldOf("fe_per_tick", 0).forGetter(EFabRecipe::fePerTick),
            FluidRequirement.CODEC.listOf().optionalFieldOf("fluids", List.of()).forGetter(EFabRecipe::fluids),
            EFabRecipeRequirements.CODEC.listOf().optionalFieldOf("requirements", List.of()).forGetter(EFabRecipe::requirements)
    ).apply(instance, EFabRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, EFabRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EFabRecipe decode(RegistryFriendlyByteBuf buf) {
            List<String> pattern = readStringList(buf);
            Map<String, Ingredient> key = readIngredientMap(buf);
            List<Ingredient> ingredients = readIngredientList(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int time = buf.readVarInt();
            List<RecipeTier> tiers = readTierList(buf);
            int fePerTick = buf.readVarInt();
            List<FluidRequirement> fluids = readFluidList(buf);
            List<EFabRecipeRequirement> requirements = readRequirementList(buf);
            return new EFabRecipe(pattern, key, ingredients, result, time, tiers, fePerTick, fluids, requirements);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, EFabRecipe recipe) {
            writeStringList(buf, recipe.pattern());
            writeIngredientMap(buf, recipe.key());
            writeIngredientList(buf, recipe.efabIngredients());
            ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItem(null));
            buf.writeVarInt(recipe.time());
            writeTierList(buf, List.copyOf(recipe.tiers()));
            buf.writeVarInt(recipe.fePerTick());
            writeFluidList(buf, recipe.fluids());
            writeRequirementList(buf, recipe.requirements());
        }
    };

    @Override
    public MapCodec<EFabRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EFabRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static List<String> readStringList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(buf.readUtf());
        }
        return result;
    }

    private static void writeStringList(RegistryFriendlyByteBuf buf, List<String> values) {
        buf.writeVarInt(values.size());
        for (String value : values) {
            buf.writeUtf(value);
        }
    }

    private static List<Ingredient> readIngredientList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Ingredient> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
        }
        return result;
    }

    private static void writeIngredientList(RegistryFriendlyByteBuf buf, List<Ingredient> values) {
        buf.writeVarInt(values.size());
        for (Ingredient value : values) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, value);
        }
    }

    private static Map<String, Ingredient> readIngredientMap(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Ingredient> result = new HashMap<>();
        for (int i = 0; i < size; i++) {
            result.put(buf.readUtf(), Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
        }
        return result;
    }

    private static void writeIngredientMap(RegistryFriendlyByteBuf buf, Map<String, Ingredient> values) {
        buf.writeVarInt(values.size());
        values.forEach((key, value) -> {
            buf.writeUtf(key);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, value);
        });
    }

    private static List<RecipeTier> readTierList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<RecipeTier> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(buf.readEnum(RecipeTier.class));
        }
        return result;
    }

    private static void writeTierList(RegistryFriendlyByteBuf buf, List<RecipeTier> tiers) {
        buf.writeVarInt(tiers.size());
        for (RecipeTier tier : tiers) {
            buf.writeEnum(tier);
        }
    }

    private static List<FluidRequirement> readFluidList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<FluidRequirement> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(FluidRequirement.STREAM_CODEC.decode(buf));
        }
        return result;
    }

    private static void writeFluidList(RegistryFriendlyByteBuf buf, List<FluidRequirement> fluids) {
        buf.writeVarInt(fluids.size());
        for (FluidRequirement fluid : fluids) {
            FluidRequirement.STREAM_CODEC.encode(buf, fluid);
        }
    }

    private static List<EFabRecipeRequirement> readRequirementList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<EFabRecipeRequirement> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(EFabRecipeRequirements.STREAM_CODEC.decode(buf));
        }
        return result;
    }

    private static void writeRequirementList(RegistryFriendlyByteBuf buf, List<EFabRecipeRequirement> requirements) {
        buf.writeVarInt(requirements.size());
        for (EFabRecipeRequirement requirement : requirements) {
            EFabRecipeRequirements.STREAM_CODEC.encode(buf, requirement);
        }
    }
}
