package mcjty.efab.network;

import mcjty.efab.EFab;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record CraftingActionPayload(BlockPos pos, boolean repeat) implements CustomPacketPayload {

    public static final Type<CraftingActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EFab.MODID, "crafting_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CraftingActionPayload decode(RegistryFriendlyByteBuf buf) {
            return new CraftingActionPayload(buf.readBlockPos(), buf.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CraftingActionPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeBoolean(payload.repeat());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
