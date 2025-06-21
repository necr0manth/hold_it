package dev.dsai03.hold_it.content.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.*;

public class OffsetedParticleRenderer implements IParticleRenderer<OffsetedParticle, RenderLevelStageEvent> {
    @Override
    public void renderParticles(RenderLevelStageEvent event, Set<OffsetedParticle> particles) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(event.getPoseStack().last().pose());
        RenderSystem.applyModelViewMatrix();
        Map<ParticleRenderType, List<OffsetedParticle>> particlesByTypes = new HashMap<>();
        for (var pt : particles) {
            particlesByTypes.computeIfAbsent(pt.particle.getRenderType(), p -> new ArrayList<>()).add(pt);
        }
        var pPartialTicks = event.getPartialTick();
        for (ParticleRenderType particlerendertype : particlesByTypes.keySet()) {
            if (particlerendertype == ParticleRenderType.NO_RENDER) continue;
            List<OffsetedParticle> list = particlesByTypes.get(particlerendertype);
            if (list != null) {
                RenderSystem.setShader(GameRenderer::getParticleShader);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                particlerendertype.begin(bufferbuilder, Minecraft.getInstance().textureManager);

                for (var particle : list) {
                    try {
                        var p = particle.particleAccess.getPosRaw();
                        var po = particle.particleAccess.getPosO();
                        var offset = particle.getOffset();
                        if (offset == null)
                            continue;
                        particle.particleAccess.setPosRaw(p.add(offset));
                        particle.particleAccess.setPosO(po.add(offset));
                        particle.particle.render(bufferbuilder, Minecraft.getInstance().gameRenderer.getMainCamera(), pPartialTicks);
                        particle.particleAccess.setPosRaw(p);
                        particle.particleAccess.setPosO(po);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                        crashreportcategory.setDetail("Particle", particle::toString);
                        crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                        throw new ReportedException(crashreport);
                    }
                }
                particlerendertype.end(tesselator);
            }
        }
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
    }
}
