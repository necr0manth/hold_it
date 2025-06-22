package dev.dsai03.hold_it.content.client.particles.core;

public class ParticleRenderManager<P extends IParticle, RENDER_CONTEXT> {
    private final IParticleRenderer<P, RENDER_CONTEXT> renderer;
    private final ParticleManager<P, RENDER_CONTEXT> manager;
    private long lastRenderTime;

    public ParticleRenderManager(ParticleManager<P, RENDER_CONTEXT> manager, IParticleRenderer<P, RENDER_CONTEXT> renderer) {
        this.manager = manager;
        this.renderer = renderer;
    }

    public final void render(RENDER_CONTEXT renderContext) {
        synchronized (manager.particles) {
            if (lastRenderTime - System.nanoTime() > 100000000)
                lastRenderTime = System.nanoTime();
            var dt = (System.nanoTime() - lastRenderTime) / 1e9f;
            lastRenderTime = System.nanoTime();
            for (var particle : manager.particles)
                particle.renderTick(dt);
            renderer.renderParticles(renderContext, manager.particles);
        }
    }
}
