package dev.dsai03.hold_it.content.client.particles;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.bolt.FXLightningBolt;
import com.mna.particles.bolt.LightningData;
import com.mna.tools.math.Vector3;
import dev.dsai03.hold_it.content.client.particles.core.BaseParticle;
import dev.dsai03.hold_it.content.client.particles.core.IColoredParticle;
import dev.dsai03.hold_it.mixins.client.ParticleAccessor;
import dev.dsai03.hold_it.mixins.client.ParticleEngineAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleUtils {
    private static final WeakHashMap<Particle, Consumer<ParticleAccess>> tickers = new WeakHashMap<>();
    private static final WeakHashMap<Particle, Consumer<ParticleAccess>> renderTickers = new WeakHashMap<>();
    private static final Random random = new Random();

    public static MAParticleType getParticleType(Affinity affinity) {
        return switch (affinity) {
            case ARCANE -> ParticleInit.ARCANE.get();
            case EARTH -> ParticleInit.DUST.get();
            case ENDER -> ParticleInit.ENDER.get();
            case FIRE -> ParticleInit.FLAME.get();
            case WATER -> ParticleInit.WATER.get();
            case WIND -> ParticleInit.AIR_VELOCITY.get();
            case HELLFIRE -> ParticleInit.HELLFIRE.get();
            case ICE -> ParticleInit.FROST.get();
            case LIGHTNING -> ParticleInit.FLAME.get();
            case UNKNOWN -> null;
            case BLOOD -> ParticleInit.BLOOD.get();
        };
    }

    public static void addParticle(ParticleOptions particleOptions, Vec3 pos, Vec3 velocity, boolean alwaysRender) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        level.addParticle(particleOptions, alwaysRender, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
    }

    public static void addParticle(ParticleOptions particleOptions, Vec3 pos, Vec3 velocity) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        level.addParticle(particleOptions, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
    }

    public static void addParticle(Particle particle, Consumer<Particle> consumer) {
        consumer.accept(particle);
        Minecraft.getInstance().particleEngine.add(particle);
    }

    public static void addParticle(Particle particle) {
        Minecraft.getInstance().particleEngine.add(particle);
    }

    public static void addParticle(ParticleOptions particleOptions, Vec3 pos, Vec3 velocity, Function<Particle, Particle> function) {
        Minecraft.getInstance().particleEngine.add(function.apply(createParticle(particleOptions, Minecraft.getInstance().level, pos, velocity)));
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        for (var list : ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).getParticles().values()) {
            for (var particle : list) {
                if (tickers.containsKey(particle)) {
                    tickers.get(particle).accept(new ParticleAccess(particle));
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        for (var list : ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).getParticles().values()) {
            for (var particle : list) {
                if (renderTickers.containsKey(particle)) {
                    renderTickers.get(particle).accept(new ParticleAccess(particle));
                }
            }
        }
    }

    public static void addParticle(ParticleOptions particleOptions, Vec3 pos, Vec3 velocity, Consumer<ParticleAccess> ticker, Consumer<ParticleAccess> renderTicker) {
        addParticle(particleOptions, pos, velocity, p -> {
            tickers.put(p, ticker);
            renderTickers.put(p, renderTicker);
            return p;
        });
    }

    public static Particle createParticle(ParticleOptions particleOptions, ClientLevel level, Vec3 pos, Vec3 velocity) {
        var particleEngine = Minecraft.getInstance().particleEngine;
        return ((ParticleProvider<ParticleOptions>) ((ParticleEngineAccessor) particleEngine).getProviders().get(BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType()))).createParticle(particleOptions, level, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
    }

    public static class ParticleAccess {
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
    }

    public static final Consumer<ParticleAccess> EMPTY_TICKER = p -> {
    };

    public static Consumer<ParticleAccess> relativeTo(Supplier<Vec3> relative, Consumer<ParticleAccess> other) {
        Vec3[] pos = new Vec3[1];
        return p -> {
            other.accept(p);
            var pos1 = relative.get();
            if (pos[0] == null)
                pos[0] = pos1;
            if (pos1 != null) {
                p.setPos(p.getPos().add(pos1.subtract(pos[0])));
                pos[0] = pos1;
            }
        };
    }

    public static FXLightningBolt createLightning(ClientLevel level, Vec3 from, Vec3 to, Function<MAParticleType, MAParticleType> function) {
        return (FXLightningBolt) createParticle(function.apply(new MAParticleType(ParticleInit.LIGHTNING_BOLT.get())), level, from, to);
    }

    public static LightningData createLightningData(Vec3 start, Vec3 end, long seed, int maxAge) {
        var lightningData = new LightningData(new Vector3(start), new Vector3(end), seed, maxAge);
        lightningData.setMaxOffset(0.1f);
        lightningData.fractalize();
        lightningData.finalize();
        return lightningData;
    }

    public static ArrayList<Vec3> getJagged(Vec3 begin, Vec3 end, int slicesLeft, double maxJagMultiplier) {
        float a = 0.4f + random.nextFloat(0.2f);
        if(begin.subtract(end).length()==0)
            return new ArrayList<>(List.of(begin));
        Vec3 mid = begin.add(end.subtract(begin).scale(a)).add(getRandomRotNormal(end.subtract(begin)).normalize().scale(random.nextDouble(end.subtract(begin).length() * maxJagMultiplier)));
        if (slicesLeft > 0) {
            ArrayList<Vec3> left = getJagged(begin, mid, slicesLeft - 1, maxJagMultiplier);
            ArrayList<Vec3> right = getJagged(mid, end, slicesLeft - 1, maxJagMultiplier);
            left.add(mid);
            left.addAll(right);
            return left;
        } else return new ArrayList<Vec3>(List.of(mid));
    }

    private static Vec3 getRandomRotNormal(Vec3 vec) {
        Vec3 normal = vec.normalize();
        Vec3 x = !(normal.x < 0.001 && normal.z < 0.001) ? normal.cross(new Vec3(0, 1, 0)).normalize() : normal.cross(new Vec3(1, 0, 0)).normalize();
        Vec3 z = normal.cross(x).normalize();
        float deg = random.nextFloat(360);
        return x.scale(Math.cos(Math.toRadians(deg)))
                .add(z.scale(Math.sin(Math.toRadians(deg))));
    }

    public static Predicate<BaseParticle<?>> renderTickerToTicker(BiConsumer<BaseParticle<?>, Float> renderTicker) {
        return p -> {
            renderTicker.accept(p, 0.05f);
            return false;
        };
    }

    public static BiConsumer<BaseParticle<?>, Float> tickerToRenderTick(Predicate<BaseParticle<?>> ticker) {
        return (p, dt) -> ticker.test(p);
    }

    public static <T extends BaseParticle<T> & IColoredParticle> BiConsumer<T, Float> fadeOut(float time) {
        AtomicReference<Integer> initialAlpha = new AtomicReference<>();
        return (p, dt) -> {
            if (p.maxLifetime - p.lifetime <= time) {
                if (initialAlpha.get() == null)
                    initialAlpha.set(p.getColor().getAlpha());
                p.setColor(new Color(p.getColor().getRed(), p.getColor().getGreen(), p.getColor().getBlue(), Math.max(0, (int) (initialAlpha.get() * (p.maxLifetime - p.lifetime) / time))));
            }
        };
    }

    public static <T extends BaseParticle<T> & IColoredParticle> BiConsumer<T, Float> fadeIn(float time) {
        AtomicReference<Integer> initialAlpha = new AtomicReference<>();
        return (p, dt) -> {
            if (p.lifetime <= time) {
                if (initialAlpha.get() == null)
                    initialAlpha.set(p.getColor().getAlpha());
                p.setColor(new Color(p.getColor().getRed(), p.getColor().getGreen(), p.getColor().getBlue(), Math.max(0, (int) (initialAlpha.get() * p.lifetime / time))));
            }
        };
    }

}
