package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoolShapeEntity extends ChargeableSpellEntity {
//	Random random = new Random();
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

	public static int chargeTime() {
		return 40;
	}

	public static int maxChargeTime() {
		return 100;
	}

	@Override
	protected boolean isCharged() {
		return tickCount > chargeTime();
	}

	@Override
	protected void chargeTick() {
		if (level().isClientSide)
			clientTick();
		System.out.println("Charging: " + tickCount);
	}

	@Override
	protected void overChargeTick() {
		if (level().isClientSide)
			clientTick();
		System.out.println("Overcharging: " + tickCount);
	}

	private void clientTick() {
//		var r = Math.min((float) tickCount / chargeTime(), 1) * radius();
//		int n = Mth.floor(radius() * radius() * radius());
//		for (int i = 0; i < n; i++)
//			spawnParticles(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(Math.pow(random.nextDouble(), 1 / 3f)).scale(radius()));
//
	}


	@Override
	protected boolean isOverCharged() {
		return tickCount >= maxChargeTime();
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
