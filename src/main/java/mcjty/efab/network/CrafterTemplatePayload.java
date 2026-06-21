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
public record CrafterTemplatePayload(BlockPos pos, List<ItemStack> stacks) implements CustomPacketPayload {

    public static final int TEMPLATE_SLOTS = 9;

    public static final Type<CrafterTemplatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EFab.MODID, "crafter_template"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrafterTemplatePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CrafterTemplatePayload decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
            int size = buf.readVarInt();
            if (size < 0 || size > TEMPLATE_SLOTS) {
                throw new IllegalArgumentException("Invalid crafter template size: " + size);
            }
            List<ItemStack> stacks = new ArrayList<>(TEMPLATE_SLOTS);
            for (int i = 0; i < size; i++) {
                stacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            return new CrafterTemplatePayload(pos, stacks);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CrafterTemplatePayload payload) {
            buf.writeBlockPos(payload.pos());
            int size = Math.min(payload.stacks().size(), TEMPLATE_SLOTS);
            buf.writeVarInt(size);
            for (int i = 0; i < size; i++) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.stacks().get(i));
            }
        }
    };

    public CrafterTemplatePayload {
        stacks = List.copyOf(stacks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
