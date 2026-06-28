package mcjty.efab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.efab.EFab;
import mcjty.efab.blockentity.PowerOptimizerBlockEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PowerOptimizerBlockEntityRenderer implements BlockEntityRenderer<PowerOptimizerBlockEntity> {

    private static final ResourceLocation GLOW = EFab.rl("textures/effects/glow.png");

    public PowerOptimizerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PowerOptimizerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        long time = Util.getMillis();
        long scaleTime = Math.abs(time % 1000L);
        if ((time / 1000L) % 2L == 0L) {
            scaleTime = 1000L - scaleTime;
        }
        float scale = scaleTime / 1000.0F * 0.1F + 0.3F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        drawGlowQuad(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(GLOW)), scale);
        poseStack.popPose();
    }

    private static void drawGlowQuad(PoseStack poseStack, VertexConsumer consumer, float scale) {
        Matrix4f matrix = poseStack.last().pose();
        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;
        int color = 0xB0FFFFFF;

        consumer.addVertex(matrix, -scale, -scale, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, scale, -scale, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, scale, scale, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -scale, scale, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, 1.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(PowerOptimizerBlockEntity blockEntity) {
        return true;
    }
}
