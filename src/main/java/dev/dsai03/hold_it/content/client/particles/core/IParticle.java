package dev.dsai03.hold_it.content.client.particles.core;

public interface IParticle {
    void renderTick(float deltaTime);

    boolean tick();
}
