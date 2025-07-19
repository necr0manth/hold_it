package dev.dsai03.hold_it.content.client.particles.offseted_particles;

import dev.dsai03.hold_it.content.client.particles.core.BaseParticle;
import dev.dsai03.hold_it.content.client.particles.core.IColoredParticle;
import dev.dsai03.hold_it.content.client.particles.core.ParticleAccess;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.function.Supplier;

public class OffsetedParticle extends BaseParticle<OffsetedParticle> implements IColoredParticle {
    public Supplier<Vec3> offset = () -> Vec3.ZERO;
    public final Particle particle;
    public final ParticleAccess particleAccess;

    public OffsetedParticle(Particle particle) {
        this.particle = particle;
        this.particleAccess = new ParticleAccess(particle);
        addTicker((p -> {
            particle.tick();
            return !particle.isAlive();
        }));
    }

    @Override
    public void setPos(Vec3 pos) {
        particle.setPos(pos.x, pos.y, pos.z);
    }

    @Override
    public Vec3 getPos() {
        return particle.getPos();
    }

    @Override
    public void setSpeed(Vec3 speed) {
        particleAccess.setVelocity(speed);
    }

    @Override
    public Vec3 getSpeed() {
        return particleAccess.getVelocity();
    }

    public OffsetedParticle offset(Supplier<Vec3> offset) {
        this.offset = offset;
        return this;
    }

    public Vec3 getOffset() {
        return offset.get();
    }

    @Override
    public Color getColor() {
        return particleAccess.getColor();
    }

    @Override
    public void setColor(Color color) {
        particleAccess.setColor(color);
    }
}