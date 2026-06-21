package mcjty.efab.network;

import mcjty.efab.EFab;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@MethodsReturnNonnullByDefault
public record StorageNameSetPayload(BlockPos pos, String name) implements CustomPacketPayload {

    public static final Type<StorageNameSetPayload> TYPE =
            new Type<>(EFab.rl("storage_name_set"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageNameSetPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public StorageNameSetPayload decode(RegistryFriendlyByteBuf buf) {
            return new StorageNameSetPayload(buf.readBlockPos(), buf.readUtf(64));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, StorageNameSetPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeUtf(payload.name(), 64);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
