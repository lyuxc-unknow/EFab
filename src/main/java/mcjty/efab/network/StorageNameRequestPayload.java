package mcjty.efab.network;

import mcjty.efab.EFab;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@MethodsReturnNonnullByDefault
public record StorageNameRequestPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<StorageNameRequestPayload> TYPE =
            new Type<>(EFab.rl("storage_name_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageNameRequestPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public StorageNameRequestPayload decode(RegistryFriendlyByteBuf buf) {
            return new StorageNameRequestPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, StorageNameRequestPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
