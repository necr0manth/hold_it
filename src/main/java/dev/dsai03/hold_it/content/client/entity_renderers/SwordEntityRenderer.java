package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dsai03.hold_it.content.entities.SwordEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SwordEntityRenderer extends EntityRenderer<SwordEntity> {
    public SwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SwordEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Рисуем красный куб вокруг меча
        float size = 0.5f;
        poseStack.pushPose();
        poseStack.translate(0, size / 2, 0);
        poseStack.scale(size, size, size);
        VertexConsumer builder = buffer.getBuffer(net.minecraft.client.renderer.RenderType.solid());
        float r = 1.0f, g = 0.2f, b = 0.2f, a = 1.0f; // Красный цвет, не прозрачный
        // Куб (6 граней)
        float[][] vertices = {
            {-0.5f, -0.5f, -0.5f}, {0.5f, -0.5f, -0.5f}, {0.5f, 0.5f, -0.5f}, {-0.5f, 0.5f, -0.5f}, // back
            {-0.5f, -0.5f, 0.5f}, {0.5f, -0.5f, 0.5f}, {0.5f, 0.5f, 0.5f}, {-0.5f, 0.5f, 0.5f}      // front
        };
        int[][] faces = {
            {0, 1, 2, 3}, // back
            {4, 5, 6, 7}, // front
            {0, 1, 5, 4}, // bottom
            {2, 3, 7, 6}, // top
            {1, 2, 6, 5}, // right
            {0, 3, 7, 4}  // left
        };
        for (int[] face : faces) {
            builder.vertex(poseStack.last().pose(), vertices[face[0]][0], vertices[face[0]][1], vertices[face[0]][2]).color(r, g, b, a).endVertex();
            builder.vertex(poseStack.last().pose(), vertices[face[1]][0], vertices[face[1]][1], vertices[face[1]][2]).color(r, g, b, a).endVertex();
            builder.vertex(poseStack.last().pose(), vertices[face[2]][0], vertices[face[2]][1], vertices[face[2]][2]).color(r, g, b, a).endVertex();
            builder.vertex(poseStack.last().pose(), vertices[face[3]][0], vertices[face[3]][1], vertices[face[3]][2]).color(r, g, b, a).endVertex();
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SwordEntity entity) {
        return null;
    }
} 