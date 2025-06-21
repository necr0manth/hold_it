package dev.dsai03.hold_it.content.client.particles;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;

import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES;

public class OffsetedParticleEngine extends ParticleManager<OffsetedParticle, RenderLevelStageEvent> {
    public static OffsetedParticleEngine instance = new OffsetedParticleEngine();

    public OffsetedParticleEngine() {
        super(OffsetedParticleRenderer::new);
    }

    @Override
    void initRenderManager(ParticleRenderManager<OffsetedParticle, RenderLevelStageEvent> manager) {
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == AFTER_PARTICLES)
                manager.render(event);
        });
    }
}
