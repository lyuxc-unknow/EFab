package mcjty.efab.network;

import mcjty.efab.EFab;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record CraftingStatusRequestPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<CraftingStatusRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EFab.MODID, "crafting_status_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStatusRequestPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CraftingStatusRequestPayload decode(RegistryFriendlyByteBuf buf) {
            return new CraftingStatusRequestPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CraftingStatusRequestPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
