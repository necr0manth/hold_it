package dev.dsai03.hold_it.content.client.particles.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public abstract class ParticleManager<P extends IParticle, R> {
    final Set<P> particles = new HashSet<>();

    public ParticleManager(Supplier<IParticleRenderer<P, R>> rendererFactory) {
        initRenderManager(new ParticleRenderManager<>(this, rendererFactory.get()));
        MinecraftForge.EVENT_BUS.addListener(this::clientTick);
    }

    public final P addParticle(P particle) {
        synchronized (particles) {
            particles.add(particle);
        }
        return particle;
    }

    public void clearParticles() {
        synchronized (particles) {
            particles.clear();
        }
    }

    protected void clientTick(TickEvent.ClientTickEvent event) {
        synchronized (particles) {
            particles.removeIf(IParticle::tick);

        }
    }

    public int getParticlesCount() {
        synchronized (particles) {
            return particles.size();
        }
    }

    public abstract void initRenderManager(ParticleRenderManager<P, R> manager);
}
