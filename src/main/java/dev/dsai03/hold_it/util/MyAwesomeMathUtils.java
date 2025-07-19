package dev.dsai03.hold_it.util;

import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;

public class MyAwesomeMathUtils {
    public static Vec3 rotateTowards(Vec3 current, Vec3 target, float angle) {
        if (current.toVector3f().angle(target.toVector3f()) <= angle) {
            return target;
        }
        return new Vec3(new AxisAngle4f(Math.min(current.toVector3f().angle(target.toVector3f()), angle), current.cross(target).toVector3f().normalize()).transform(current.toVector3f()));
    }
}
