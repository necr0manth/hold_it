package dev.dsai03.hold_it.particles;

public interface IParticle {
    void renderTick(float deltaTime);

    boolean tick();
}
