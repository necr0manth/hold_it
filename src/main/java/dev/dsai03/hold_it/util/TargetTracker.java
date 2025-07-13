package dev.dsai03.hold_it.util;

import com.mna.tools.math.MathUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.function.Predicate;

public class TargetTracker {
    public static class TimeCalculator {
        public static double calculateTime(Vector3f objectPosition, Vector3f targetPosition, Vector3f velocity, double angularVelocity) {
            var localTargetPosition = new Vector3f(targetPosition).sub(objectPosition);
            var v = velocity.length();
            var y = localTargetPosition.dot(velocity) / v;
            var x = localTargetPosition.cross(velocity, new Vector3f()).length() / v;
            return Math.min(calculateTime(x, y, angularVelocity, v), calculateTime(x, y, -angularVelocity, v));
        }

        public static double calculateTime(double x, double y, double o, double v) {
            var t = t1(x, y, o, v);
            var r = v / o;
            var dx = x - Math.cos(o * t) * r + r;
            var dy = y - Math.sin(o * t) * r;
            return t + Math.sqrt(dx * dx + dy * dy) / v;
        }

        public static double t1(double x, double y, double o, double v) {
            var r = v / o;
            double var1 = Math.acos(r / Math.sqrt(x * x + y * y + r * r + 2 * x * r));
            var s1 = angle1(Math.atan2(y, x + r) + var1) / o;
            var s2 = angle1(Math.atan2(y, x + r) - var1) / o;
            if (check1(x, y, o, v, s1))
                return s1;
            return s2;
        }

        private static double angle1(double angle) {
            return angle + Math.ceil(-angle / (2 * Math.PI)) * 2 * Math.PI;
        }

        private static boolean check1(double x, double y, double o, double v, double t) {
            var vx = -Math.sin(o * t);
            var vy = Math.cos(o * t);
            var r = v / o;
            var dx = x - Math.cos(o * t) + r;
            var dy = y - Math.sin(o * t);
            return dx * vx + dy * vy >= 0;
        }
    }

    public static class DataAccessor {
        public final Entity2EntityReference.DataAccessor TARGET;
        public final EntityDataAccessor<Float> TURN_RATE;

        public DataAccessor(Entity2EntityReference.DataAccessor TARGET, EntityDataAccessor<Float> TURN_RATE) {
            this.TARGET = TARGET;
            this.TURN_RATE = TURN_RATE;
        }

        public DataAccessor(Class<? extends Entity> entityClass) {
            this(
                    new Entity2EntityReference.DataAccessor(entityClass),
                    SynchedEntityData.defineId(entityClass, EntityDataSerializers.FLOAT)
            );
        }

    }

    private final Entity2EntityReference<Entity> targetRef;
    private final DataAccessor dataAccessor;
    private final Predicate<Entity> filter;
    private final Entity entity;
    private final String name;
    private double speed = -1;


    public TargetTracker(DataAccessor dataAccessor, String name, Predicate<Entity> filter, Entity entity) {
        this.dataAccessor = dataAccessor;
        this.targetRef = new Entity2EntityReference<>(dataAccessor.TARGET, "target", entity);
        this.filter = filter;
        this.entity = entity;
        this.name = name;
    }

    public static TargetTracker createAndDefine(DataAccessor dataAccessor, String name, Predicate<Entity> filter, Entity entity) {
        var targetTracker = new TargetTracker(dataAccessor, name, filter, entity);
        targetTracker.define();
        return targetTracker;
    }

    public void define() {
        targetRef.define();
        entity.getEntityData().define(dataAccessor.TURN_RATE, 0f);
    }

    private void target() {
        setTarget(entity.level().getEntities(entity, entity.getBoundingBox().inflate(40), (e) -> {
            ClipContext ctx = new ClipContext(entity.position(), e.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
            if (entity.level().clip(ctx).getType() == HitResult.Type.BLOCK) {
                return false;
            } else {
                return filter.test(e);
            }
        }).stream().min(Comparator.comparing(e -> TimeCalculator.calculateTime(entity.position().toVector3f(), e.position().toVector3f(), entity.getDeltaMovement().toVector3f(), getTurnRate()))).orElse(null));
    }

    public void tick() {
        if (speed == -1)
            speed = entity.getDeltaMovement().length();
        float turnRate = getTurnRate();
        if (turnRate > 0.0F) {
            if (entity.tickCount % 5 == 0) {
                target();
            }
            var target = getTarget();
            if (target != null) {
                Vec3 myPos = entity.position();
                Vec3 theirPos = target.getBoundingBox().getCenter();
                float tickTheta = getTurnRate();
                if (tickTheta > 0.0F) {
                    Vec3 desiredHeading = theirPos.subtract(myPos).normalize();
                    Vec3 calculatedHeading = MathUtils.rotateTowards(entity.getDeltaMovement().normalize(), desiredHeading, tickTheta).normalize().scale(speed);
                    entity.setDeltaMovement(calculatedHeading);
                }
            }
        }
    }

    public void save(CompoundTag compound) {
        var tag = new CompoundTag();
        targetRef.save(tag);
        tag.putFloat("turnRate", entity.getEntityData().get(dataAccessor.TURN_RATE));
        compound.put(name, tag);
    }

    public void load(CompoundTag compound) {
        var tag = compound.getCompound(name);
        targetRef.load(tag);
        setTurnRate(tag.getFloat("turnRate"));
    }

    public Entity getTarget() {
        return targetRef.get();
    }

    private void setTarget(Entity entity) {
        targetRef.set(entity);
    }

    public float getTurnRate() {
        return entity.getEntityData().get(dataAccessor.TURN_RATE);
    }

    public void setTurnRate(float strength) {
        entity.getEntityData().set(dataAccessor.TURN_RATE, strength);
    }
}
