package mcjty.efab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.efab.blockentity.CrafterBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Renders the auto-crafter's moving parts: one spinning flywheel plus two pipe pistons that bob up
 * and down out of phase. The whole assembly turns faster while the crafter is crafting.
 *
 * <p>The layout constants below are block-local (before the facing rotation) and are easy to tweak
 * if the wheel/pistons need to sit differently on the model.
 */
@ParametersAreNonnullByDefault
public class CrafterBlockEntityRenderer implements BlockEntityRenderer<CrafterBlockEntity> {

    private static final float WHEEL_Y = 0.0f;            // extra vertical offset for the flywheel
    private static final float WHEEL_Z = 0.0f;            // extra depth offset for the flywheel
    private static final float PISTON_SPREAD = 0.28f;     // horizontal distance of each piston from centre
    private static final float PISTON_Y = 0.60f;          // rest height of the pistons
    private static final float PISTON_Z = 0.30f;          // depth of the pistons (towards the front)
    private static final float PISTON_AMPLITUDE = 0.12f;  // how far the pistons travel

    public CrafterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrafterBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        float angle = blockEntity.wheelAngle(partialTick);

        poseStack.pushPose();
        // Orient the assembly to the block's facing (same convention as the block model).
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(SteamEngineBlockEntityRenderer.facingAngle(state)));
        poseStack.translate(-0.5, 0.0, -0.5);

        // Flywheel: spin around its axle (local Z) through the wheel centre (0.5, 0.5).
        poseStack.pushPose();
        poseStack.translate(0.0, WHEEL_Y, WHEEL_Z);
        poseStack.translate(0.5, 0.5, 0.0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, 0.7);
        EFabModelRenderer.render(EFabModelRenderer.WHEEL, poseStack, bufferSource, packedLight, packedOverlay, state);
        poseStack.popPose();

        // Two pistons bobbing up and down, half a cycle apart.
        double phase = Math.toRadians(angle);
        float off1 = (float) Math.sin(phase) * PISTON_AMPLITUDE;
        float off2 = (float) Math.sin(phase + Math.PI) * PISTON_AMPLITUDE;
        renderPiston(poseStack, bufferSource, packedLight, packedOverlay, state, 0.5f - PISTON_SPREAD, PISTON_Y + off1, PISTON_Z);
        renderPiston(poseStack, bufferSource, packedLight, packedOverlay, state, 0.5f + PISTON_SPREAD, PISTON_Y + off2, PISTON_Z);

        poseStack.popPose();
    }

    private void renderPiston(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                              BlockState state, float x, float y, float z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        EFabModelRenderer.render(EFabModelRenderer.PIPE, poseStack, bufferSource, packedLight, packedOverlay, state);
        poseStack.popPose();
    }
}
