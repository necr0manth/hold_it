package dev.dsai03.hold_it.content.client.particles.lightnings;

import com.mojang.math.Axis;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.content.client.particles.core.ParticleAccess;
import dev.dsai03.hold_it.content.client.particles.core.ParticleTickerHolder;
import dev.dsai03.hold_it.util.ClientScheduler;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Don't even try to understand this shitcode
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class LightningBall {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            for (var ring : allRings.keySet())
                ring.reset();
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            synchronized (allBalls) {
                for (var it = allBalls.iterator(); it.hasNext(); ) {
                    var ball = it.next();
                    if (Minecraft.getInstance().level != ball.level) {
                        it.remove();
                        continue;
                    }
                    var f = false;
                    for (var ring : ball.rings) {
                        if (!ring.removed) {
                            ball.tick();
                            f = true;
                            break;
                        }
                    }
                    if (f)
                        continue;
                    it.remove();
                }
            }
        }
    }

    private static WeakHashMap<LightningRing, Object> allRings = new WeakHashMap<>();
    private static final Set<LightningBall> allBalls = new HashSet<>();
    private final Supplier<Vec3> positionSupplier;
    private Vec3 cachedPosition;
    private float cachedRadius;

    private class LightningRing {
        long lastRenderTime = -1;

        @AllArgsConstructor
        static class Segment {
            public Vec3 start;
            public Vec3 end;
        }

        public void init(float fadeIn) {
            allRings.put(this, null);
            particles = new LightningParticle[segmentCount];
            for (int i = 0; i < segmentCount; i++) {
                int finalI = i;
                particles[i] = new LightningParticle(color, 0.03f).addRenderTicker(
                                (p, dt) -> {
                                    if (segments == null)
                                        render();
                                    p.color = color;
                                    var pos = cachedPosition = positionSupplier.get();
                                    if (pos == null) {
                                        remove();
                                        return;
                                    }
                                    p.setPos(pos.add(segments[finalI].start));
                                    p.setEndPos(pos.add(segments[finalI].end));
                                }
                        ).addRenderTicker(ParticleUtils.fadeIn(fadeIn).asRenderTicker())
                        .addTicker(p -> removed)
                        .addTicker(p -> {
                            p.regenerate();
                            return false;
                        });
                LightningParticleEngine.instance.addParticle(particles[i]);
            }
        }

        private boolean removed = false;

        public void remove() {
            removed = true;
        }

        public void fadeOut(float time) {
            ClientScheduler.schedule(Mth.ceil(20 * time), this::remove);
            for (var p : particles)
                p.addRenderTicker(ParticleUtils.fadeOut(time).asRenderTicker());
        }

        Quaternionf rotation;
        float minRadius;
        float maxRadius;
        int segmentCount;
        Color color;
        float seed;
        float rotationSpeed;
        float pulseSpeed;
        float d;
        long lastTimerRotated;
        Segment[] segments;
        LightningParticle[] particles;

        private void render() {
            if (lastRenderTime == -1)
                lastRenderTime = System.nanoTime();
            var deltaTime = (System.nanoTime() - lastRenderTime) / 1e9f;
            lastRenderTime = System.nanoTime();
            var radius = (float) ((maxRadius + minRadius) / 2 + ((maxRadius - minRadius) / 2) * Math.sin(d)) * (cachedRadius = radiusSupplier.get());
            color = new Color((int) (color0.getRed() + (Math.sin(d) + 1) / 2 * (color1.getRed() - color0.getRed())),
                    (int) (color0.getGreen() + (Math.sin(d) + 1) / 2 * (color1.getGreen() - color0.getGreen())),
                    (int) (color0.getBlue() + (Math.sin(d) + 1) / 2 * (color1.getBlue() - color0.getBlue())), 255);
            segments = new Segment[segmentCount];
            for (int i = 0; i < segmentCount; i++) {
                var angle = i * 2 * Math.PI / segmentCount;
                var angle1 = (i + 1) * 2 * Math.PI / segmentCount;
                segments[i] = new Segment(new Vec3(rotation.transform(new Vector3f((float) Math.cos(angle), (float) Math.sin(angle), 0))).scale(radius),
                        new Vec3(rotation.transform(new Vector3f((float) Math.cos(angle1), (float) Math.sin(angle1), 0))).scale(radius));
            }
            if (System.currentTimeMillis() - lastTimerRotated >= 50) {
                rotation.mul(new Quaternionf().integrate(0.02f * rotationSpeed, (1 + seed % 3), (1 + (seed + 1) % 3), (1 + (seed + 2) % 3)));
                lastTimerRotated = System.currentTimeMillis();
            }
            d += pulseSpeed * deltaTime;
            seed += deltaTime * (new Random(System.currentTimeMillis()).nextFloat() - 0.5f);
        }

        private void reset() {
            segments = null;
        }
    }

    private final List<LightningRing> rings = new ArrayList<>();
    private final ClientLevel level;
    private final int ringCount;
    private final Supplier<Float> radiusSupplier;
    Color color0;
    Color color1;

    public LightningBall(ClientLevel level, int ringCount, Color color0, Color color1, Supplier<Float> radiusSupplier, Supplier<Vec3> positionSupplier) {
        this.level = level;
        this.radiusSupplier = radiusSupplier;
        this.ringCount = ringCount;
        this.positionSupplier = positionSupplier;
        this.color0 = color0;
        this.color1 = color1;
    }

    private void tick() {
        var random = new Random();
        for (int i = 0; i < 10; i++) {
            var particle = new LightningParticle(color0, 1);
            if (cachedPosition == null)
                return;
            particle.setPos(cachedPosition.add(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(random.nextDouble() * cachedRadius)));
            particle.setEndPos(cachedPosition.add(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(random.nextDouble() * cachedRadius)));
            particle.addTicker(p -> Minecraft.getInstance().level != level);
            particle.maxLifetime = 0.1f;
            LightningParticleEngine.instance.addParticle(particle);
        }
    }

    public void spawn(float fadeIn) {
        for (int i = 0; i < ringCount; i++) {
            var ring = new LightningRing();
            ring.rotation = Axis.YP.rotationDegrees((float) (i * 360) / ringCount);
            ring.minRadius = 0.4f;
            ring.maxRadius = 1.1f;
            ring.d = (float) (i * 2 * Math.PI / ringCount);
            ring.segmentCount = 9;
            ring.seed = i;
            ring.rotationSpeed = 2;
            ring.pulseSpeed = 5;
            ring.init(fadeIn);
            rings.add(ring);
        }
        synchronized (allBalls) {
            allBalls.add(this);
        }
    }

    public void remove() {
        synchronized (allBalls) {
            allBalls.remove(this);
        }
        for (var ring : rings) {
            ring.remove();
        }
    }

    public void fadeOut(float time) {
        for (var ring : rings) {
            ring.fadeOut(time);
        }
        ClientScheduler.schedule(Mth.ceil(20 * time), () -> {
            synchronized (allBalls) {
                allBalls.remove(this);
            }
        });
    }
}
