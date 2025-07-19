package dev.dsai03.hold_it.content.entities;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.tools.math.MathUtils;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.util.AffinityDistribution;
import dev.dsai03.hold_it.util.SpellUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.PositionImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class CoolShapeEntity extends ChargeableSpellEntity {
    Random random = new Random();

    public CoolShapeEntity(EntityType<? extends CoolShapeEntity> entityType, Level world) {
        super(entityType, world);
    }

    public CoolShapeEntity(LivingEntity caster, ISpellDefinition spell, Level world) {
        super(AwesomeEntityTypes.COOL_SHAPE.get(), caster, spell, world);
    }

    public float radius() {
        return Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.RADIUS);
    }

    public float chargeTime() {
        return radius() * 0.2f;
    }

    @Override
    public boolean isPrepared() {
        return getLifetime() > chargeTime();
    }

    @Override
    protected void spellTick() {
        if (level().isClientSide)
            clientTick();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
        var r = Math.min(getLifetime() / chargeTime(), 1) * radius();
        int n = Mth.floor(r * r * r * 0.2);
        var affinities = AffinityDistribution.fromSpell(getSpell());
        for (int i = 0; i < n; i++) {
            var pos = getCaster().position().add(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(Math.pow(random.nextDouble(), 1 / 3f)).scale(r));
            level().addParticle(ParticleUtils.createDefaultConfiguredColoredParticleType(affinities.getRandomAffinity(), getSpell(), getCaster()), pos.x, pos.y, pos.z, 0, random.nextDouble(0.1), 0);
        }
        var angleStep = 1f;
        var particlePositions = new ArrayList<Vec3>();
        for (int kk = 0; kk < 1; kk++) {
            for (int i = -Mth.ceil(r); i < Mth.ceil(r) + 1; i++) {
                for (int j = -Mth.ceil(r); j < Mth.ceil(r) + 1; j++) {
                    for (int k = -Mth.ceil(r); k < Mth.ceil(r) + 1; k++) {
                        var pos = BlockPos.containing(getCaster().position().add(i, j, k));
                        if (pos.getCenter().distanceTo(getCaster().position()) > r + 1)
                            continue;
                        var state = level().getBlockState(pos);
                        if (state.isAir())
                            continue;
                        var shape = state.getCollisionShape(level(), pos);
                        for (var aabb : shape.toAabbs()) {
                            for (var dir : Direction.values()) {
                                var a = dir.getAxisDirection().getStep() == 1 ? aabb.max(dir.getAxis()) : aabb.min(dir.getAxis());
                                Direction[] axes = new Direction[2];
                                for (var axis : Direction.Axis.VALUES) {
                                    if (axes[0] == null && axis != dir.getAxis()) {
                                        axes[0] = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
                                    } else if (axes[0] != null && axes[1] == null && axis != dir.getAxis() && axes[0] != Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE)) {
                                        axes[1] = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
                                        break;
                                    }
                                }
                                var h = Vec3.ZERO;
                                h = h.add(dir.getStepX() * a, dir.getStepY() * a, dir.getStepZ() * a);
                                var v11 = aabb.min(axes[0].getAxis());
                                var v12 = aabb.max(axes[0].getAxis());
                                var v21 = aabb.min(axes[1].getAxis());
                                var v22 = aabb.max(axes[1].getAxis());

                                h = h.add(new Vec3(axes[0].step()).scale(v11 + random.nextDouble() * (v12 - v11)));
                                h = h.add(new Vec3(axes[1].step()).scale(v21 + random.nextDouble() * (v22 - v21)));
                                var p = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(h.add(-0.5, -0.5, -0.5).scale(1.1).add(0.5, 0.5, 0.5));
                                var t = h.add(-0.5, -0.5, -0.5).scale(1.1).add(0.5, 0.5, 0.5);
                                boolean spawn = true;
                                if ((Math.abs(t.x - 0.5) > 0.5 || Math.abs(t.y - 0.5) > 0.5 || Math.abs(t.z - 0.5) > 0.5) && !level().getBlockState(pos.relative(Direction.getNearest(t.x - 0.5, t.y - 0.5, t.z - 0.5))).isAir())
                                    spawn = false;
                                if (aabb.contains(h.add(-0.5, -0.5, -0.5).scale(1.1).add(0.5, 0.5, 0.5)))
                                    spawn = false;
                                if (spawn)
                                    particlePositions.add(p);

                                Vec3 sphereCenter = getCaster().position();

                                double planeCoord = pos.get(dir.getAxis()) + a;

                                double distToPlane = Math.abs(sphereCenter.get(dir.getAxis()) - planeCoord);
                                double circleRadius = Math.sqrt(r * r - distToPlane * distToPlane);

                                Vec3 circleCenter = new Vec3(
                                        dir.getAxis() == Direction.Axis.X ? planeCoord : sphereCenter.x,
                                        dir.getAxis() == Direction.Axis.Y ? planeCoord : sphereCenter.y,
                                        dir.getAxis() == Direction.Axis.Z ? planeCoord : sphereCenter.z
                                );

                                double min0 = pos.get(axes[0].getAxis()) + v11;
                                double max0 = pos.get(axes[0].getAxis()) + v12;
                                double min1 = pos.get(axes[1].getAxis()) + v21;
                                double max1 = pos.get(axes[1].getAxis()) + v22;

                                double centerCoord0 = circleCenter.get(axes[0].getAxis());
                                double centerCoord1 = circleCenter.get(axes[1].getAxis());

                                var intersections = new ArrayList<double[]>();
                                for (var side : new double[][]{{0, 1, 1}, {1, 0, -1}, {0, -1, -1}, {-1, 0, 1}}) {
                                    //(axis0-centerCoord0)^2+(axis1-centerCoord1)^2=circleRadius^2
                                    //axis1=±sqrt(circleRadius^2-(axis0-centerCoord0)^2)+centerCoord1
                                    var axis0Value = side[0] != 0 ? side[0] == 1 ? max0 : min0 : side[1] == 1 ? max1 : min1;
                                    var axis0centerCoord = side[0] != 0 ? centerCoord0 : centerCoord1;
                                    var axis1centerCoord = side[0] != 0 ? centerCoord1 : centerCoord0;
                                    var t0 = circleRadius * circleRadius - (axis0Value - axis0centerCoord) * (axis0Value - axis0centerCoord);
                                    if (t0 < 0)
                                        continue;
                                    var sqrtT0 = Math.sqrt(t0);
                                    var axis1Value1 = sqrtT0 + axis1centerCoord;
                                    var axis1Value2 = -sqrtT0 + axis1centerCoord;
                                    var solutions = axis1Value1 == axis1Value2 ? new double[][]{{axis0Value, axis1Value1}} : new double[][]{{axis0Value, axis1Value1}, {axis0Value, axis1Value2}};
                                    Arrays.sort(solutions, Comparator.comparingDouble(c -> c[1] * side[2]));
                                    for (var solution : solutions) {
                                        if (side[0] == 0) {
                                            var transformedSolution = new double[]{solution[1], solution[0]};
                                            solution[0] = transformedSolution[0];
                                            solution[1] = transformedSolution[1];
                                        }
                                    }
                                    for (var solution : solutions) {
                                        if (solution[0] >= min0 && solution[0] <= max0 && solution[1] >= min1 && solution[1] <= max1) {
                                            intersections.add(solution);
                                        }
                                    }
                                }
                                var intersections1 = new ArrayList<Vec3>();
                                for (var intersection : intersections) {
                                    intersections1.add(new Vec3(axes[0].step()).scale(intersection[0])
                                            .add(new Vec3(axes[1].step()).scale(intersection[1]))
                                            .add(new Vec3(dir.step().mul(dir.getAxisDirection().getStep())).scale(planeCoord)));
                                }
                                if (intersections1.size() < 2)
                                    continue;
//                                var cross = intersections1.get(0).subtract(intersections1.get(1)).cross(intersections1.get(0).subtract(getCaster().position())).toVector3f();
//                                if (intersections1.get(0).subtract(intersections1.get(1)).toVector3f().angleSigned(intersections1.get(0).subtract(getCaster().position()).toVector3f(), cross)>0)
//                                    Collections.reverse(intersections1);
                                for (int i1 = 0; i1 < intersections1.size(); i1++) {
                                    var pos0 = intersections1.get(i1);
                                    var pos1 = intersections1.get((i1 + intersections1.size() + 1) % intersections1.size());

                                    int subdivisions = Mth.ceil(pos1.subtract(circleCenter).toVector3f().angle(pos0.subtract(circleCenter).toVector3f()) * Mth.RAD_TO_DEG / angleStep);
                                    for (int i2 = 0; i2 < subdivisions; i2++) {
                                        var pos2 = rotateTowards(pos0.subtract(circleCenter), pos1.subtract(circleCenter), angleStep * i2 * Mth.DEG_TO_RAD).add(circleCenter).add(new Vec3(dir.step()).scale(0.01));
                                        var blockPos = BlockPos.containing(pos2.x, pos2.y, pos2.z);
                                        var state1 = level().getBlockState(blockPos);
                                        if (state1.isAir()) {
                                            var shape1 = state1.getCollisionShape(level(), blockPos);
                                            if (shape1.toAabbs().stream().noneMatch(a1 -> a1.contains(pos2.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ()))))
                                                ParticleUtils.addParticle(
                                                        ParticleUtils.createDefaultConfiguredColoredParticleType(affinities.getRandomAffinity(), getSpell(), getCaster()),
                                                        pos2, new Vec3(dir.step()).scale(0.005)
                                                );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.shuffle(particlePositions);
        for (int i = 0; i < Math.min(100, particlePositions.size()); i++) {
            var p = particlePositions.get(i);
            var affinity = affinities.getRandomAffinity();
            level().addParticle(ParticleUtils.createDefaultConfiguredColoredParticleType(affinity, getSpell(), getCaster()), p.x, p.y, p.z, 0, 0, 0);
        }
    }

    public static Vec3 rotateTowards(Vec3 current, Vec3 target, float angle) {
        angle = Math.min(current.toVector3f().angle(target.toVector3f()), angle);
        return new Vec3(new AxisAngle4f(angle, current.cross(target).toVector3f().normalize()).transform(current.toVector3f()));
    }

    public float getMaxManaCost() {
        var cap = Objects.requireNonNull(getCaster()).getCapability(ManaAndArtificeMod.getMagicCapability());
        if (!cap.isPresent())
            return 0;
        return Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.MAGNITUDE) * cap.orElseThrow(() -> new RuntimeException("0_o")).getCastingResource().getMaxAmount();
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
    }

    @Override
    public float getRequestedManaCost() {
        return Math.min(Math.min(1, getLifetime() / chargeTime()) * getMaxManaCost(), getCasterMana());
    }

    @Override
    protected void applySpell(float manaCost, float casterMana) {
        SpellUtils.cast(getSpell(), new SpellSource(getCaster(), getCaster() instanceof Player player ? player.getUsedItemHand() : getCaster().swingingArm), target(), t -> new SpellContext(level(), getSpell()), getManaCost(), getBaseSpellManaCost(), false);
    }

    protected List<SpellTarget> target() {
        var blockTargets = new ArrayList<SpellTarget>();
        for (int i = -Mth.ceil(radius()); i <= Mth.ceil(radius()); i++) {
            for (int j = -1; j <= Mth.ceil(radius()); j++) {
                for (int k = -Mth.ceil(radius()); k <= Mth.ceil(radius()); k++) {
                    var pos = BlockPos.containing(getCaster().position().add(i, j, k));
                    if (pos.getCenter().distanceTo(getCaster().position()) > radius())
                        continue;
                    if (level().getBlockState(pos).isAir())
                        continue;
                    if (j == -1)
                        blockTargets.add(new SpellTarget(pos, Direction.UP));
                    else
                        blockTargets.add(new SpellTarget(pos, null));
                }
            }
        }
        Collections.shuffle(blockTargets);
        var targets = new ArrayList<SpellTarget>();
        level().getEntities(getCaster(), getCaster().getBoundingBox().inflate(radius() + 2), (Entity e) -> e != this && e.position().distanceTo(getCaster().position()) < radius()).stream().map(SpellTarget::new).forEach(targets::add);
        targets.addAll(blockTargets);
        return targets;
    }
}
