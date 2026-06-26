package mcjty.efab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.efab.block.GridBlock;
import mcjty.efab.blockentity.AbstractCraftingBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EFabCraftingBlockEntityRenderer<T extends AbstractCraftingBlockEntity> implements BlockEntityRenderer<T> {

    private static final double ITEM_Y_OFFSET = 0.04;
    private static final double FULL_BLOCK_TOP = 1.0;

    public EFabCraftingBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStackHandler items = blockEntity.getItemHandler();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees(blockEntity.getBlockState())));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ItemStack stack = items.getStackInSlot(row * 3 + col);
                if (!stack.isEmpty()) {
                    renderStack(blockEntity, stack, col, row, poseStack, bufferSource, packedLight);
                }
            }
        }

        poseStack.popPose();
    }

    private static <T extends AbstractCraftingBlockEntity> void renderStack(T blockEntity, ItemStack stack, int col, int row,
                                                                            PoseStack poseStack, MultiBufferSource bufferSource,
                                                                            int packedLight) {
        poseStack.pushPose();
        poseStack.translate(col * 0.3 - 0.3, itemRenderY(blockEntity.getBlockState()), row * 0.3 - 0.3);
        poseStack.scale(0.28f, 0.28f, 0.28f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                row * 3 + col);
        poseStack.popPose();
    }

    private static double itemRenderY(BlockState state) {
        if (state.hasProperty(GridBlock.HALF) && state.getValue(GridBlock.HALF)) {
            return GridBlock.HALF_HEIGHT + ITEM_Y_OFFSET;
        }
        return FULL_BLOCK_TOP + ITEM_Y_OFFSET;
    }

    private static float rotationDegrees(BlockState state) {
        Direction facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.getValue(HorizontalDirectionalBlock.FACING)
                : Direction.SOUTH;
        return switch (facing) {
            case NORTH -> 180.0f;
            case EAST -> 90.0f;
            case WEST -> 270.0f;
            default -> 0.0f;
        };
    }
}
