package dev.dsai03.hold_it.content.client.particles.core;

import dev.dsai03.hold_it.mixins.client.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class ParticleAccess implements IColoredParticle, IParticleWithMaxLifetime {
    public final ParticleAccessor accessor;
    public final Particle particle;

    public ParticleAccess(Particle particle) {
        this.particle = particle;
        this.accessor = (ParticleAccessor) particle;
    }

    public Vec3 getVelocity() {
        return new Vec3(accessor.getXd(), accessor.getYd(), accessor.getZd());
    }

    public void setVelocity(Vec3 velocity) {
        accessor.setXd(velocity.x);
        accessor.setYd(velocity.y);
        accessor.setZd(velocity.z);
    }

    public Vec3 getPos() {
        return particle.getPos();
    }

    public void setPos(Vec3 pos) {
        particle.setPos(pos.x, pos.y, pos.z);
    }

    public Vec3 getPosRaw() {
        return new Vec3(accessor.getX(), accessor.getY(), accessor.getZ());
    }

    public Vec3 getPosO() {
        return new Vec3(accessor.getXo(), accessor.getYo(), accessor.getZo());
    }

    public void setPosO(Vec3 pos) {
        accessor.setXo(pos.x);
        accessor.setYo(pos.y);
        accessor.setZo(pos.z);
    }

    public void setPosRaw(Vec3 pos) {
        accessor.setX(pos.x);
        accessor.setY(pos.y);
        accessor.setZ(pos.z);
    }

    @Override
    public Color getColor() {
        return new Color(accessor.getRCol(), accessor.getGCol(), accessor.getBCol(), accessor.getAlpha());
    }

    @Override
    public void setColor(Color color) {
        particle.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        accessor.setAlpha(color.getAlpha() / 255f);
    }

    @Override
    public void setLifetime(float lifetime) {
        particle.setLifetime(Math.round(lifetime * 20));
    }

    @Override
    public float getLifetime() {
        return particle.getLifetime() / 20f;
    }

    @Override
    public float getMaxLifetime() {
        return particle.getLifetime();
    }
}
