package dev.dsai03.hold_it.content.client.particles.lightnings;

import dev.dsai03.hold_it.content.client.particles.core.ParticleManager;
import dev.dsai03.hold_it.content.client.particles.core.ParticleRenderManager;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;

import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES;

public class LightningParticleEngine extends ParticleManager<LightningParticle, RenderLevelStageEvent> {
    public static LightningParticleEngine instance = new LightningParticleEngine();

    public LightningParticleEngine() {
        super(LightningRenderer::new);
    }

    @Override
    public void initRenderManager(ParticleRenderManager<LightningParticle, RenderLevelStageEvent> manager) {
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == AFTER_PARTICLES)
                manager.render(event);
        });
    }
}
