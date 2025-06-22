package dev.dsai03.hold_it.util;

import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class RandomUtils {
    private static final Random random = new Random();

    public static Vec3 randomNormalizedVector() {
        return new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
    }

    public static Vec3 randomVectorFromBall() {
        return new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(Math.pow(random.nextDouble(), 1 / 3f));
    }

    public static Vec3 randomNormalized2DVector() {
        return new Vec3(random.nextGaussian(), 0, random.nextGaussian()).normalize();
    }

    public static Vec3 random2DVectorFromCircle() {
        return new Vec3(random.nextGaussian(), 0, random.nextGaussian()).normalize().scale(Math.pow(random.nextDouble(), 0.5f));
    }

}
