package mcjty.efab.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for third-party EFab recipe requirements.
 *
 * <p>Register the type on both physical sides before datapacks are loaded. Recipe
 * JSON uses the registered id in a {@code requirements} entry:</p>
 *
 * <pre>
 * "requirements": [
 *   { "type": "othermod:pressure", "amount": 1000 }
 * ]
 * </pre>
 */

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class EFabRecipeRequirements {

    private static final Map<ResourceLocation, EFabRecipeRequirementType<?>> TYPES = new LinkedHashMap<>();
    private static final Map<EFabRecipeRequirementType<?>, ResourceLocation> IDS = new IdentityHashMap<>();

    public static final Codec<EFabRecipeRequirement> CODEC = ResourceLocation.CODEC.dispatch(
            "type",
            requirement -> getId(requirement.type()),
            EFabRecipeRequirements::codec
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EFabRecipeRequirement> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EFabRecipeRequirement decode(RegistryFriendlyByteBuf buf) {
            ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
            return decodeTyped(buf, get(id));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, EFabRecipeRequirement value) {
            EFabRecipeRequirementType<?> type = value.type();
            ResourceLocation id = getId(type);
            ResourceLocation.STREAM_CODEC.encode(buf, id);
            encodeTyped(buf, type, value);
        }
    };

    public static <T extends EFabRecipeRequirement> EFabRecipeRequirementType<T> type(
            MapCodec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec
    ) {
        return new SimpleType<>(codec, streamCodec);
    }

    public static synchronized <T extends EFabRecipeRequirement> void register(ResourceLocation id, EFabRecipeRequirementType<T> type) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        if (TYPES.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate EFab recipe requirement type id: " + id);
        }
        if (IDS.containsKey(type)) {
            throw new IllegalArgumentException("EFab recipe requirement type is already registered as: " + IDS.get(type));
        }
        TYPES.put(id, type);
        IDS.put(type, id);
    }

    public static synchronized boolean contains(ResourceLocation id) {
        return TYPES.containsKey(id);
    }

    public static synchronized EFabRecipeRequirementType<?> get(ResourceLocation id) {
        EFabRecipeRequirementType<?> type = TYPES.get(id);
        if (type == null) {
            throw new IllegalArgumentException("Unknown EFab recipe requirement type: " + id);
        }
        return type;
    }

    public static synchronized ResourceLocation getId(EFabRecipeRequirementType<?> type) {
        ResourceLocation id = IDS.get(type);
        if (id == null) {
            throw new IllegalArgumentException("Unregistered EFab recipe requirement type: " + type);
        }
        return id;
    }

    private static MapCodec<? extends EFabRecipeRequirement> codec(ResourceLocation id) {
        return get(id).codec();
    }

    private static <T extends EFabRecipeRequirement> T decodeTyped(RegistryFriendlyByteBuf buf, EFabRecipeRequirementType<T> type) {
        return type.streamCodec().decode(buf);
    }

    @SuppressWarnings("unchecked")
    private static <T extends EFabRecipeRequirement> void encodeTyped(
            RegistryFriendlyByteBuf buf,
            EFabRecipeRequirementType<T> type,
            EFabRecipeRequirement value
    ) {
        type.streamCodec().encode(buf, (T) value);
    }

    private record SimpleType<T extends EFabRecipeRequirement>(
            MapCodec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec
    ) implements EFabRecipeRequirementType<T> {
    }

    private EFabRecipeRequirements() {
    }
}
