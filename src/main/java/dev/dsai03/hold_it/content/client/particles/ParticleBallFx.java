package dev.dsai03.hold_it.content.client.particles;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import dev.dsai03.hold_it.content.client.particles.lightnings.LightningBall;
import dev.dsai03.hold_it.content.client.particles.offseted_particles.OffsetedParticle;
import dev.dsai03.hold_it.content.client.particles.offseted_particles.OffsetedParticleEngine;
import dev.dsai03.hold_it.util.AffinityDistribution;
import dev.dsai03.hold_it.util.RandomUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ParticleBallFx {
    private final Random random = new Random();
    private LightningBall lightningBall;

    public record BallData(Vec3 pos, float radius) {
    }

    public record BallFxData(Function<MAParticleType, MAParticleType> particleTypePrepare,
                             AffinityDistribution affinities) {
    }

    private final Supplier<BallFxData> ballFxDataSupplier;
    private final Supplier<BallData> ballDataSupplier;

    public ParticleBallFx(Supplier<BallFxData> ballFxDataSupplier, Supplier<BallData> ballDataSupplier) {
        this.ballFxDataSupplier = ballFxDataSupplier;
        this.ballDataSupplier = ballDataSupplier;
    }

    public void tick() {
        var data = ballFxDataSupplier.get();
        if (data == null)
            return;
        var affinities = data.affinities;
        var ballData = ballDataSupplier.get();
        if (ballData == null)
            return;
        Supplier<Vec3> positionSupplier = () -> Optional.ofNullable(ballDataSupplier.get()).map(BallData::pos).orElse(null);
        for (int i = 0; i < 10; i++) {
            var affinity = affinities.without(Affinity.LIGHTNING).getRandomAffinity();
            if (affinity == null)
                break;

            if (affinity == Affinity.WIND) {
                for (int j = 0; j < 5; j++) {
                    OffsetedParticleEngine.instance.addParticle(new OffsetedParticle(ParticleUtils.createParticle(data.particleTypePrepare.apply(new MAParticleType(ParticleInit.AIR_VELOCITY.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(50, 50, 50)), Minecraft.getInstance().level, RandomUtils.randomVectorFromBall().scale(ballData.radius() * 0.8f), Vec3.ZERO)).offset(positionSupplier).addTicker(p -> {
                        if (p.getSpeed().length() < 0.001f) {
                            p.setSpeed(RandomUtils.randomVectorFromBall().scale(0.3f * p.getPos().length()));
                        }
                        p.setSpeed(p.getSpeed().add(p.getPos().scale(-0.3f * p.getPos().length())));
                        return false;
                    }));
                }
            } else if (affinity == Affinity.BLOOD) {
                for (int j = 0; j < 10; j++) {
                    var pos = RandomUtils.randomVectorFromBall().scale(Math.pow(random.nextDouble(), 5) * ballData.radius()*0.7);
                    OffsetedParticleEngine.instance.addParticle(new OffsetedParticle(ParticleUtils.createParticle(data.particleTypePrepare.apply(new MAParticleType(ParticleInit.BLOOD.get()).setGravity(0.0005f).setScale(0.01f)), Minecraft.getInstance().level, pos, pos.normalize().scale(random.nextDouble() * 0.01))).offset(positionSupplier));
                }
            } else {
                var particleType = ParticleUtils.getParticleType(affinity);
                OffsetedParticleEngine.instance.addParticle(new OffsetedParticle(ParticleUtils.createParticle(data.particleTypePrepare.apply(new MAParticleType(particleType)), Minecraft.getInstance().level, RandomUtils.randomVectorFromBall().scale(ballData.radius()), Vec3.ZERO)).offset(positionSupplier));
            }
        }
        if (affinities.getAffinity(Affinity.LIGHTNING) != 0 && lightningBall == null) {
            var baseColor = new Color(100, 67, 255);
            lightningBall = new LightningBall(Minecraft.getInstance().level, 10, baseColor, new Color(255, 67, 255), () -> Optional.ofNullable(ballDataSupplier.get()).map(BallData::radius).orElse(-1f), positionSupplier);
            lightningBall.spawn(0.3f);
        } else if (affinities.getAffinity(Affinity.LIGHTNING) == 0 && lightningBall != null) {
            lightningBall.fadeOut(0.5f);
            lightningBall = null;
        }
    }

}
