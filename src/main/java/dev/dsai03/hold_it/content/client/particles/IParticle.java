package dev.dsai03.hold_it.content.client.particles;

public interface IParticle {
    void renderTick(float deltaTime);

    boolean tick();
}
