package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dsai03.hold_it.content.entities.AwesomeSpellShapeEntity;
import dev.dsai03.hold_it.content.entities.CoolShapeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AwesomeShapeEntityRenderer extends EntityRenderer<AwesomeSpellShapeEntity> {
	public AwesomeShapeEntityRenderer(EntityRendererProvider.Context pContext) {
		super(pContext);
	}

	@Override
	public ResourceLocation getTextureLocation(AwesomeSpellShapeEntity pEntity) {
		return null;
	}

	public void render(CoolShapeEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

	}
}