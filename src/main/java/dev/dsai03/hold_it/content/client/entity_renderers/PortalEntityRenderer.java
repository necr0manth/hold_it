package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dsai03.hold_it.content.entities.PortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PortalEntityRenderer extends EntityRenderer<PortalEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("hold_it", "textures/entity/portal.png");

    public PortalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PortalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Простой рендер: цветная сфера
        float size = entity.getSize();
        poseStack.pushPose();
        poseStack.scale(size, size, size);
        VertexConsumer builder = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(TEXTURE));
        // Можно добавить простую геометрию или использовать готовую модель
        // Здесь будет просто куб/сфера (заглушка)
        // ... (можно добавить куб или частицы)
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return TEXTURE;
    }
} 