package dev.dsai03.hold_it.content.client.particles;

import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class BaseParticle<T extends BaseParticle<T>> implements IParticle {
    public abstract void setPos(Vec3 pos);

    public abstract Vec3 getPos();

    public abstract void setSpeed(Vec3 speed);

    public abstract Vec3 getSpeed();

    float lifetime = 0;
    float maxLifetime = -1;
    private BiConsumer<T, Float> renderTick;
    private Predicate<T> tick;

    public T addRenderTicker(BiConsumer<? super T, Float> renderTick) {
        if (this.renderTick == null) {
            this.renderTick = (BiConsumer<T, Float>) renderTick;
            return cast();
        }
        var oldRenderTick = this.renderTick;
        this.renderTick = (p, dt) -> {
            oldRenderTick.accept(p, dt);
            renderTick.accept(p, dt);
        };
        return cast();
    }

    public T addTicker(Predicate<? super T> tick) {
        if (this.tick == null) {
            this.tick = (Predicate<T>) tick;
            return cast();
        }
        var oldTick = this.tick;
        this.tick = p -> oldTick.test(p) || tick.test(p);
        return cast();
    }

    @Override
    public void renderTick(float deltaTime) {
        lifetime += deltaTime;
        setPos(getPos().add(getSpeed().scale(deltaTime)));
        if (renderTick != null)
            renderTick.accept(cast(), deltaTime);
    }

    @Override
    public boolean tick() {
        if (maxLifetime != -1 && lifetime > maxLifetime)
            return true;
        if (tick != null)
            return tick.test(cast());
        return false;
    }

    public final T cast() {
        return (T) this;
    }

    public final T speed(Vec3 speed) {
        setSpeed(speed);
        return cast();
    }
}
