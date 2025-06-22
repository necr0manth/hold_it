package dev.dsai03.hold_it.content.client.particles;

import com.mna.particles.types.render.ParticleRenderTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.Set;

public class LightningRenderer implements IParticleRenderer<LightningParticle, RenderLevelStageEvent> {
    @Override
    public void renderParticles(RenderLevelStageEvent event, Set<LightningParticle> renderData) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(event.getPoseStack().last().pose());
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getParticleShader);
        var builder = Tesselator.getInstance().getBuilder();
        ParticleRenderTypes.ADDITIVE.begin(builder, Minecraft.getInstance().textureManager);
        var camera = event.getCamera();
        float partialTick = Minecraft.getInstance().getFrameTime();
        for (var particle : renderData) {
            particle.render(builder, camera, partialTick);
        }
        ParticleRenderTypes.ADDITIVE.end(Tesselator.getInstance());
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
    }
}
