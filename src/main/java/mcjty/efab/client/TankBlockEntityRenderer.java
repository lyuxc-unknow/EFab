package mcjty.efab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.efab.blockentity.TankBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TankBlockEntityRenderer implements BlockEntityRenderer<TankBlockEntity> {

    private final Font font;

    public TankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(TankBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isBottomTank()) {
            return;
        }
        FluidStack fluid = blockEntity.getDisplayFluid();
        if (fluid.isEmpty()) {
            return;
        }

        Component name = fluid.getHoverName();
        String amount = fluid.getAmount() + " / " + blockEntity.getDisplayCapacity() + " mB";

        poseStack.pushPose();
        poseStack.translate(0.5, 0.72, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees(blockEntity.getBlockState())));
        poseStack.translate(0.0, 0.0, 0.525);
        poseStack.scale(0.0065f, -0.0065f, 0.0065f);

        drawCentered(name.getString(), 33, poseStack, bufferSource, packedLight);
        drawCentered(amount, 45, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    private void drawCentered(String text, int y, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int x = -font.width(text) / 2;
        font.drawInBatch(text, x, y, 0xffffff, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
    }

    private static float rotationDegrees(BlockState state) {
        Direction facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
                ? state.getValue(HorizontalDirectionalBlock.FACING)
                : Direction.SOUTH;
        return switch (facing) {
            case NORTH -> 180.0f;
            case EAST -> 90.0f;
            case WEST -> -90.0f;
            default -> 0.0f;
        };
    }
}
