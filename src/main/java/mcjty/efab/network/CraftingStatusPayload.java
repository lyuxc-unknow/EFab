package mcjty.efab.network;

import mcjty.efab.EFab;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public record CraftingStatusPayload(BlockPos pos, int progress, int requiredTime, ItemStack result) implements CustomPacketPayload {

    public static final Type<CraftingStatusPayload> TYPE =
            new Type<>(EFab.rl("crafting_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStatusPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CraftingStatusPayload decode(RegistryFriendlyByteBuf buf) {
            return new CraftingStatusPayload(
                    buf.readBlockPos(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CraftingStatusPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeVarInt(payload.progress());
            buf.writeVarInt(payload.requiredTime());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.result());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
