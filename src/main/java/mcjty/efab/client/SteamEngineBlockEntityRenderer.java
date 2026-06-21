package mcjty.efab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.efab.blockentity.SteamEngineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SteamEngineBlockEntityRenderer implements BlockEntityRenderer<SteamEngineBlockEntity> {

    public SteamEngineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SteamEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        float angle = blockEntity.wheelAngle(partialTick);

        poseStack.pushPose();
        // Rotate the whole wheel to match the block's facing (same convention as the block model).
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingAngle(state)));
        poseStack.translate(-0.5, 0.0, -0.5);
        // Spin the flywheel around its axle (local Z) through the wheel centre (0.5, 0.5).
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, 0.0);
        EFabModelRenderer.render(EFabModelRenderer.WHEEL, poseStack, bufferSource, packedLight, packedOverlay, state);
        poseStack.popPose();
    }

    static float facingAngle(BlockState state) {
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
