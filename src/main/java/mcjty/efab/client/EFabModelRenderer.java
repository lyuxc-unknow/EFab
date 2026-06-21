package mcjty.efab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.efab.EFab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Loads and renders the standalone OBJ models ({@code wheel.obj}, {@code pipe.obj}) that the
 * machine renderers draw. They are not referenced by any blockstate, so they must be registered
 * as additional models (see {@link ClientSetup}) before they can be baked and drawn.
 */
public final class EFabModelRenderer {

    public static final ModelResourceLocation WHEEL =
            ModelResourceLocation.standalone(EFab.rl("block/wheel"));
    public static final ModelResourceLocation PIPE =
            ModelResourceLocation.standalone(EFab.rl("block/pipe"));

    public static void render(ModelResourceLocation model, PoseStack poseStack, MultiBufferSource bufferSource,
                              int packedLight, int packedOverlay, BlockState state) {
        BakedModel baked = Minecraft.getInstance().getModelManager().getModel(model);
        RenderType renderType = RenderType.cutout();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), consumer, state, baked,
                1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, renderType);
    }

    private EFabModelRenderer() {
    }
}
