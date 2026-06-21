package mcjty.efab.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.efab.blockentity.MonitorBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;

@ParametersAreNonnullByDefault
public class MonitorBlockEntityRenderer implements BlockEntityRenderer<MonitorBlockEntity> {

    private final Font font;

    public MonitorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(MonitorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        List<String> lines = blockEntity.getLines();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees(blockEntity.getBlockState())));
        poseStack.translate(-0.39, 0.39, 0.425);
        poseStack.scale(0.0060f, -0.0060f, 0.0060f);

        int y = 50;
        for (String line : lines) {
            if (!line.isEmpty()) {
                font.drawInBatch(font.plainSubstrByWidth(line, 125), 10, y, Color.GREEN.getRGB(), false,
                        poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
            }
            y += 10;
            if (y > 87) {
                break;
            }
        }
        poseStack.popPose();
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
