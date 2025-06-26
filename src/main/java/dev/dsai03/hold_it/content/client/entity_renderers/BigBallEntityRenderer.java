package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dsai03.hold_it.content.entities.BallEntity;
import dev.dsai03.hold_it.content.entities.BigBallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BigBallEntityRenderer extends EntityRenderer<BigBallEntity> {
    public BigBallEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(BigBallEntity pEntity) {
        return null;
    }

    @Override
    public void render(BigBallEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pEntity.calculateRenderBallData();
    }
}
