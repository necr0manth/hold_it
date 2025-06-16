package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class BallEntity extends ThrowableProjectile {
	private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> THROWN = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.COMPOUND_TAG);
	public Vec3 targetPosition;
	public float power = 1;
	private SpellRecipe cachedRecipe;
	private LivingEntity cachedCaster;

	public BallEntity(EntityType<? extends BallEntity> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		setNoGravity(true);
	}

	public BallEntity(Level level, LivingEntity owner) {
		this(AwesomeEntityTypes.BALL_ENTITY_TYPE.get(), level);
		setOwner(owner);
		setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5f)).subtract(this.getBoundingBox().getCenter()));
	}

	public void tick() {
		if (targetPosition != null)
			setDeltaMovement(targetPosition.subtract(position()));
		super.tick();
		if (getPower() != power && !this.level().isClientSide) {
			setPower(power);
			refreshDimensions();
		}
		if (targetPosition != null)
			setDeltaMovement(targetPosition.subtract(position()));
	}

	public void setPower(float power) {
		this.power = power;
		this.entityData.set(POWER, power);
	}

	public float getPower() {
		return this.entityData.get(POWER);
	}

	@Override
	public EntityDimensions getDimensions(Pose pPose) {
		return new EntityDimensions(getPower(), getPower(), false);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(POWER, 0.0f);
		this.entityData.define(THROWN, false);
		this.entityData.define(SPELL_RECIPE, new CompoundTag());
	}


	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (POWER.equals(key)) {
			this.refreshDimensions();
		}
	}

	public SpellRecipe getSpell() {
		if (cachedRecipe == null) {
			var nbt = entityData.get(SPELL_RECIPE);
			if (nbt.isEmpty())
				return null;
			cachedRecipe = SpellRecipe.fromNBT(nbt);
		}
		return cachedRecipe;
	}

	public void setSpell(ISpellDefinition spell) {
		CompoundTag nbt = new CompoundTag();
		spell.writeToNBT(nbt);
		entityData.set(SPELL_RECIPE, nbt);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat("power", this.entityData.get(POWER));
		compound.putBoolean("thrown", this.entityData.get(THROWN));
		compound.put("spell", entityData.get(SPELL_RECIPE));
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		setPower(pCompound.getFloat("power"));
		entityData.set(THROWN, pCompound.getBoolean("thrown"));
		if (pCompound.contains("spell")) {
			entityData.set(SPELL_RECIPE, (CompoundTag) pCompound.get("spell"));
		}

	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);
		onHit(position());
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		SpellSource source = new SpellSource((LivingEntity) getOwner(), InteractionHand.MAIN_HAND);
		SpellContext context = new SpellContext(this.level(), getSpell());
		HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(getSpell(), source, new SpellTarget(hitResult.getEntity()), context);
		if (getOwner() instanceof Player player) {
			results.forEach((key, value) -> {
				if (value.is_success) {
					SpellCaster.addComponentRoteProgress(player, key);
				}
			});
		}
		onHit(position());
	}

	private void onHit(Vec3 pos) {
//		if (this.level() instanceof ServerLevel serverLevel && entityData.get(THROWN)) {
//			LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
//			lightningBolt.moveTo(Vec3.atBottomCenterOf(this.blockPosition()));
//			lightningBolt.setDamage(getPower() * 10);
//			lightningBolt.setVisualOnly(false);
//			level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(), 4.0F * getPower(), Level.ExplosionInteraction.TNT);
//			serverLevel.addFreshEntity(lightningBolt);
//			this.remove(RemovalReason.DISCARDED);
//		}
		if (getOwner() == null) {
			discard();
			return;
		}
		if (getSpell() == null) {
			return;
		}
		if (level().isClientSide)
			return;
		var targets = new ArrayList<SpellTarget>();
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				for (int k = -2; k <= 2; k++) {
					targets.add(new SpellTarget(BlockPos.containing(position().add(i, j, k)), null));
				}
			}
		}
		for (var target : targets) {
			SpellSource source = new SpellSource((LivingEntity) getOwner(), InteractionHand.MAIN_HAND);
			SpellContext context = new SpellContext(this.level(), getSpell());
			HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(getSpell(), source, target, context);
			if (getOwner() instanceof Player player) {
				results.forEach((key, value) -> {
					if (value.is_success) {
						SpellCaster.addComponentRoteProgress(player, key);
					}
				});
			}
		}
		discard();
	}

	public void shoot(Vec3 dir) {
		targetPosition = null;
		entityData.set(THROWN, true);
		shoot(dir.x, dir.y, dir.z, (float) (2 / (getPower() + 0.5)), 0);
	}
}
