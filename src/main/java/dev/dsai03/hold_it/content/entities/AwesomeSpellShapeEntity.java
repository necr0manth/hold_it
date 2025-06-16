package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AwesomeSpellShapeEntity extends ChargeableSpellEntity {
	public static final EntityDataAccessor<CompoundTag> BALLS = SynchedEntityData.defineId(AwesomeSpellShapeEntity.class, EntityDataSerializers.COMPOUND_TAG);

	public AwesomeSpellShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
		super(entityType, world);
	}

	public AwesomeSpellShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
		super(AwesomeEntityTypes.AWESOME_SHAPE.get(), caster, spell, world);
	}

	public static final int maxUseTime = 10000000;
	public static final float maxPower = 2;
	public static final float defaultPower = 0.7f;
	public static final float distanceToProjectile = 3;

	@Override
	protected boolean isCharged() {
		return getLifetime() > 10;
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(BALLS, new CompoundTag());
	}

	public List<BallEntity> getBalls() {
		var tag = entityData.get(BALLS);

		var ans = new ArrayList<BallEntity>();
		if (!tag.contains("balls"))
			return ans;
		for (var i : tag.getList("balls", Tag.TAG_INT_ARRAY)) {
			ans.add((BallEntity) ((ServerLevel) level()).getEntity(NbtUtils.loadUUID(i)));
		}
		return ans;
	}

	public void saveBalls(List<BallEntity> balls) {
		ListTag list = new ListTag();
		for (var proj : balls) {
			list.add(NbtUtils.createUUID(proj.getUUID()));
		}
		var tag = new CompoundTag();
		tag.put("balls", list);
		entityData.set(BALLS, tag);
	}

	public static float chargeTime() {
		return 10;
	}

	protected void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
//		compound.put("balls", entityData.get(BALLS));
	}

	protected void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
//		if(compound.contains("balls"))
//			entityData.set(BALLS, compound.getCompound("balls"));
	}

	@Override
	protected void chargeTick() {
		var centerPos = getCaster().getEyePosition().add(getCaster().getLookAngle().scale(distanceToProjectile));
		var projectiles = getBalls();
		int maxProj = 5;
		float speed = 0.25f;
		float s = Math.min(getLifetime() / chargeTime(), 1);
		int i = 0;
		while (s > 0) {
			float p;
			if (s >= defaultPower) {
				p = defaultPower;
				s -= defaultPower;
			} else {
				p = s;
				s = 0;
			}
			BallEntity proj;
			if (i < projectiles.size())
				proj = projectiles.get(i);
			else {
				proj = new BallEntity(level(), getCaster());
				level().addFreshEntity(proj);
				projectiles.add(proj);
			}
			proj.power = p;
			i++;
		}
		if (projectiles.size() == 1) {
			projectiles.get(0).targetPosition = projectiles.get(0).position().add(centerPos.subtract(projectiles.get(0).getBoundingBox().getCenter()));
			projectiles.get(0).setDeltaMovement(projectiles.get(0).targetPosition.subtract(projectiles.get(0).position()));
		} else {
			var radius = 0.7;
			Quaternionf r;
			if (!getCaster().getLookAngle().normalize().equals(new Vec3(0, 0.8, 0)))
				r = new Quaternionf().lookAlong((float) getCaster().getLookAngle().x, (float) getCaster().getLookAngle().y, (float) getCaster().getLookAngle().z, 0, 1, 0);
			else
				r = new Quaternionf();
			for (int j = 0; j < projectiles.size(); j++) {
				var angle = j * 2 * Math.PI / projectiles.size() + getLifetime();
				var pos = r.transformInverse(new Vector3d(radius * Math.sin(angle), radius * Math.cos(angle), -distanceToProjectile)).add(getCaster().getX(), getCaster().getEyeY(), getCaster().getZ());
				projectiles.get(j).targetPosition = new Vec3(pos.x, pos.y + 1, pos.z);
				projectiles.get(j).setDeltaMovement(projectiles.get(j).targetPosition.subtract(projectiles.get(j).position()));
			}
		}

		saveBalls(projectiles);
	}

	@Override
	protected void overChargeTick() {

	}

	@Override
	protected boolean isOverCharged() {
		return getLifetime() > 20;
	}

	@Override
	protected void onCharged() {

	}

	@Override
	protected Collection<SpellTarget> target() {
		return List.of();
	}

	@Override
	protected void onInterrupt() {

	}
}
