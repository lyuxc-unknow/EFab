package mcjty.efab.network;

import mcjty.efab.EFab;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public record GridTransferPayload(BlockPos pos, List<ItemStack> stacks) implements CustomPacketPayload {

    public static final int INPUT_SLOTS = 9;

    public static final Type<GridTransferPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EFab.MODID, "grid_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GridTransferPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GridTransferPayload decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
            int size = buf.readVarInt();
            if (size < 0 || size > INPUT_SLOTS) {
                throw new IllegalArgumentException("Invalid grid transfer size: " + size);
            }
            List<ItemStack> stacks = new ArrayList<>(INPUT_SLOTS);
            for (int i = 0; i < size; i++) {
                stacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            return new GridTransferPayload(pos, stacks);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, GridTransferPayload payload) {
            buf.writeBlockPos(payload.pos());
            int size = Math.min(payload.stacks().size(), INPUT_SLOTS);
            buf.writeVarInt(size);
            for (int i = 0; i < size; i++) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.stacks().get(i));
            }
        }
    };

    public GridTransferPayload {
        stacks = List.copyOf(stacks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
