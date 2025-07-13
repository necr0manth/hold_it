package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mna.tools.render.MARenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.dsai03.hold_it.content.entities.PortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PortalEntityRenderer extends EntityRenderer<PortalEntity> {

    public PortalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(PortalEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        var color = entity.getSpell().getParticleColorOverride();
        this.renderDefaultPortal(entity, matrixStackIn, bufferIn, packedLightIn, partialTicks, new int[]{color>>16, (color>>8)&0xFF, color&0xFF});
    }

    private float getScale(PortalEntity portal, float fadeIn, float fadeOut, float partialTick) {
        var time = portal.tickCount + partialTick;
        var remainingTime = portal.getRemainingLifetime() - partialTick;
        return time < fadeIn ? time / fadeIn : remainingTime < fadeOut ? remainingTime / fadeOut : 1;
    }

    private void renderDefaultPortal(PortalEntity portal, PoseStack pose, MultiBufferSource bufferSource, int packedLight, float partialTick, int[] color) {
        float scaleFactor = 3.0F * this.getScale(portal, 20, 20, partialTick);
        float portalSpinDegrees = (float) (portal.tickCount * 3 % 360);
        VertexConsumer vertexBuilder = bufferSource.getBuffer(MARenderTypes.PORTAL_RENDER);
        this.renderPortalTexture(portal, pose, vertexBuilder, packedLight, color, 230, scaleFactor, portalSpinDegrees, 0.0F);
        pose.translate(0.0F, 0.0F, 0.05F);
        this.renderPortalTexture(portal, pose, vertexBuilder, packedLight, color, 230, scaleFactor, -portalSpinDegrees, 0.0F);
    }

    private void renderPortalTexture(PortalEntity entity, PoseStack pose, VertexConsumer vertexBuilder, int packedLight, int[] color, int alpha, float scaleFactor, float spin, float tilt) {
        pose.pushPose();
        pose.translate(0, entity.getBbHeight() / 2, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        pose.scale(scaleFactor, scaleFactor, scaleFactor);
        pose.mulPose(Axis.XP.rotationDegrees(tilt));
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(spin));
        pose.translate(0.0F, -0.25F, 0.0F);
        PoseStack.Pose matrixstack$entry = pose.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        float nrmV = (float) Math.cos((double) spin * Math.PI / (double) 180.0F);
        float nrmH = (float) Math.cos((double) (spin - 90.0F) * Math.PI / (double) 180.0F);
        addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLight, 0.0F, 0.0F, 0.0F, 1.0F, nrmH, nrmV, color, alpha);
        addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLight, 1.0F, 0.0F, 1.0F, 1.0F, nrmH, nrmV, color, alpha);
        addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLight, 1.0F, 1.0F, 1.0F, 0.0F, nrmH, nrmV, color, alpha);
        addVertex(vertexBuilder, renderMatrix, normalMatrix, packedLight, 0.0F, 1.0F, 0.0F, 0.0F, nrmH, nrmV, color, alpha);
        pose.popPose();
    }

    private static void addVertex(VertexConsumer vertexBuilder_, Matrix4f renderMatrix, Matrix3f normalMatrix, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] rgb, int a) {
        vertexBuilder_.vertex(renderMatrix, x - 0.5F, y - 0.25F, 0.0F).color(rgb[0], rgb[1], rgb[2], a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, nrmH, nrmV, nrmH).endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(PortalEntity entity) {
        return MARenderTypes.PORTAL_TEXTURE;
    }

} 