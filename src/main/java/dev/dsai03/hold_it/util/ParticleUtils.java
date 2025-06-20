package dev.dsai03.hold_it.util;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
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
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleUtils {
    private static final WeakHashMap<Particle, Consumer<ParticleAccess>> tickers = new WeakHashMap<>();
    private static final WeakHashMap<Particle, Consumer<ParticleAccess>> renderTickers = new WeakHashMap<>();

    public static MAParticleType getParticleType(Affinity affinity) {
        return switch (affinity) {
            case ARCANE -> ParticleInit.ARCANE.get();
            case EARTH -> ParticleInit.DUST.get();
            case ENDER -> ParticleInit.ENDER.get();
            case FIRE -> ParticleInit.FLAME.get();
            case WATER -> ParticleInit.WATER.get();
            case WIND -> ParticleInit.AIR_ORBIT.get();
            case HELLFIRE -> ParticleInit.HELLFIRE.get();
            case ICE -> ParticleInit.FROST.get();
            case LIGHTNING -> ParticleInit.LIGHTNING_BOLT.get();
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
//        System.out.println("renderTick "+System.currentTimeMillis());
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
}
