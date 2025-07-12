package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dsai03.hold_it.content.entities.SwordEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SwordEntityRenderer extends EntityRenderer<SwordEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("hold_it", "textures/entity/sword.png");

    public SwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SwordEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

    }

    @Override
    public ResourceLocation getTextureLocation(SwordEntity entity) {
        return TEXTURE;
    }
} 