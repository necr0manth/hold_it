package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoolShapeEntity extends ChargeableSpellEntity {
	Random random = new Random();
//	Affinity[] cachedAffinities;

	public CoolShapeEntity(EntityType<? extends CoolShapeEntity> entityType, Level world) {
		super(entityType, world);
	}

	public CoolShapeEntity(LivingEntity caster, ISpellDefinition spell, Level world) {
		super(AwesomeEntityTypes.COOL_SHAPE.get(), caster, spell, world);
		setPos(caster.position());
	}

	public static float radius() {
		return 10;
	}

	public static float chargeTime() {
		return 2;
	}

	public static float maxChargeTime() {
		return 10;
	}

	@Override
	protected boolean isCharged() {
		return getLifetime() > chargeTime();
	}

	@Override
	protected void chargeTick() {
		if (level().isClientSide)
			clientTick();
		System.out.println("Charging: " + getLifetime());
	}

	@Override
	protected void overChargeTick() {
		if (level().isClientSide)
			clientTick();
		System.out.println("Overcharging: " + getLifetime());
	}

	@OnlyIn(Dist.CLIENT)
	private void clientTick() {
		var r = Math.min(getLifetime() / chargeTime(), 1) * radius();
		int n = Mth.floor(r * r * r * 0.2);
		var affinities = getSpell().getAffinity().keySet().toArray(Affinity[]::new);
		for (int i = 0; i < n; i++) {
			var pos = getCaster().position().add(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(Math.pow(random.nextDouble(), 1 / 3f)).scale(r));
			level().addParticle(getSpell().colorParticle(ParticleUtils.getParticleType(affinities[random.nextInt(affinities.length)]), getCaster()), pos.x, pos.y, pos.z, 0, random.nextDouble(0.1), 0);
		}
		for (int kk = 0; kk < 1; kk++) {
			for (int i = -Mth.ceil(r); i < Mth.ceil(r); i++) {
				for (int j = -Mth.ceil(r); j < Mth.ceil(r); j++) {
					for (int k = -Mth.ceil(r); k < Mth.ceil(r); k++) {
						var pos = BlockPos.containing(getCaster().position().add(i, j, k));
						if (pos.getCenter().distanceTo(getCaster().position()) > r)
							continue;
						var state = level().getBlockState(pos);
						if (state.isAir())
							continue;
						var shape = state.getShape(level(), pos);
						for (var aabb : shape.toAabbs()) {
							for (var dir : Direction.values()) {
								var a = dir.getAxisDirection().getStep() == 1 ? aabb.max(dir.getAxis()) : aabb.min(dir.getAxis());
								Direction.Axis[] axes = new Direction.Axis[2];
								for (var axis : Direction.Axis.VALUES) {
									if (axes[0] == null && axis != dir.getAxis()) {
										axes[0] = axis;
									} else if (axes[0] != null && axes[1] == null && axis != dir.getAxis() && axes[0] != axis) {
										axes[1] = axis;
										break;
									}
								}
								var h = Vec3.ZERO;
								h = h.add(dir.getStepX() * a, dir.getStepY() * a, dir.getStepZ() * a);
								var v11 = aabb.min(axes[0]);
								var v12 = aabb.max(axes[0]);
								var v21 = aabb.min(axes[1]);
								var v22 = aabb.max(axes[1]);
								h = h.add((axes[0] == Direction.Axis.X ? new Vec3(1, 0, 0) : axes[0] == Direction.Axis.Y ? new Vec3(0, 1, 0) : new Vec3(0, 0, 1)).scale(v11 + random.nextDouble() * (v12 - v11)));
								h = h.add((axes[1] == Direction.Axis.X ? new Vec3(1, 0, 0) : axes[1] == Direction.Axis.Y ? new Vec3(0, 1, 0) : new Vec3(0, 0, 1)).scale(v21 + random.nextDouble() * (v22 - v21)));
								var p = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(h.add(-0.5, -0.5, -0.5).scale(1.1).add(0.5, 0.5, 0.5));
								var t = h.add(-0.5, -0.5, -0.5).scale(1.1).add(0.5, 0.5, 0.5);
								if ((Math.abs(t.x - 0.5) > 0.5 || Math.abs(t.y - 0.5) > 0.5 || Math.abs(t.z - 0.5) > 0.5) && !level().getBlockState(pos.relative(Direction.getNearest(t.x - 0.5, t.y - 0.5, t.z - 0.5))).isAir())
									continue;
								if (aabb.contains(h.add(-0.5, -0.5, -0.5).scale(1.1).add(0.5, 0.5, 0.5)))
									continue;
								level().addParticle(getSpell().colorParticle(ParticleUtils.getParticleType(affinities[random.nextInt(affinities.length)]), getCaster()), p.x, p.y, p.z, 0, 0, 0);
							}
						}

					}
				}
			}
		}
	}


	@Override
	protected boolean isOverCharged() {
		return getLifetime() >= maxChargeTime();
	}

	@Override
	protected void onInterrupt() {
		System.out.println("Interrupted");
	}

	@Override
	protected void onCharged() {
		System.out.println("Charged!");
	}

	@Override
	protected List<SpellTarget> target() {
		var targets = new ArrayList<SpellTarget>();
		level().getEntities(getCaster(), getCaster().getBoundingBox().inflate(radius()), (Entity e) -> e != this && e.position().distanceTo(getCaster().position()) < radius()).stream().map(SpellTarget::new).forEach(targets::add);
		for (int i = -Mth.ceil(radius()); i <= Mth.ceil(radius()); i++) {
			for (int j = 0; j <= Mth.ceil(radius()); j++) {
				for (int k = -Mth.ceil(radius()); k <= Mth.ceil(radius()); k++) {
					var pos = BlockPos.containing(getCaster().position().add(i, j, k));
					if (pos.getCenter().distanceTo(getCaster().position()) > radius())
						continue;
					if (level().getBlockState(pos).isAir())
						continue;
					targets.add(new SpellTarget(pos, null));
				}
			}
		}
		return targets;
	}
}
