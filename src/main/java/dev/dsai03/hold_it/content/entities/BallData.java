package dev.dsai03.hold_it.content.entities;

import dev.dsai03.hold_it.content.client.particles.ParticleBallFx;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public record BallData(Vec3 pos, float power) {
    @OnlyIn(Dist.CLIENT)
    public ParticleBallFx.BallData toFxBallData() {
        return new ParticleBallFx.BallData(pos, power * 0.5f);
    }
}
