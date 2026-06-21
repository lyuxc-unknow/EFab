package mcjty.efab.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FluidRequirement(ResourceLocation fluid, int amount) {

    public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidRequirement::fluid),
            Codec.INT.fieldOf("amount").forGetter(FluidRequirement::amount)
    ).apply(instance, FluidRequirement::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidRequirement> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidRequirement decode(RegistryFriendlyByteBuf buf) {
            return new FluidRequirement(ResourceLocation.STREAM_CODEC.decode(buf), buf.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidRequirement value) {
            ResourceLocation.STREAM_CODEC.encode(buf, value.fluid());
            buf.writeVarInt(value.amount());
        }
    };
}
