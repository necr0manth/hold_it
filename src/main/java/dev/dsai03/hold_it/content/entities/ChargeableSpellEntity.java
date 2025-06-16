package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

import javax.annotation.Nullable;

public abstract class ChargeableSpellEntity extends Entity {
	public static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<Float> LIFETIME = SynchedEntityData.defineId(ChargeableSpellEntity.class, EntityDataSerializers.FLOAT);
	private LivingEntity cachedCaster;
	private SpellRecipe cachedRecipe;
	private boolean wasCharged = false;
	private long firstTimeTime = -1;

	public ChargeableSpellEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
		super(entityType, world);
		cachedCaster = null;
		cachedRecipe = null;
		setNoGravity(true);
		setInvulnerable(true);
	}

	@Override
	public boolean isAlwaysTicking() {
		return true;
	}

	public ChargeableSpellEntity(EntityType<? extends ChargeableSpellEntity> entityType, LivingEntity caster, ISpellDefinition spell, Level world) {
		this(entityType, world);
		setCaster(caster);
		setSpell(spell);
	}

	private void stopCast() {
		if (getCaster() instanceof Player player)
			player.stopUsingItem();
		discard();
	}

	public void tick() {
		if (!level().isClientSide) {
			if(getCaster() == null) {
				discard();
				return;
			}
			if (firstTimeTime == -1) {
				firstTimeTime = System.nanoTime();
			}
			entityData.set(LIFETIME, (System.nanoTime() - firstTimeTime) / 1e9f);
		}
		LivingEntity caster = getCaster();
		SpellRecipe recipe = getSpell();
		if (caster != null && caster.isAlive() && caster.level().dimension().equals(level().dimension()) && caster.getUseItemRemainingTicks() > 0) {
			if (isOverCharged() && !level().isClientSide) {
				applySpell();
				stopCast();
			} else {
				if (!level().isClientSide() && !recipe.isValid()) {
					stopCast();
				} else {
					boolean isCharged = isCharged();
					if (isCharged) {
						if (!wasCharged)
							onCharged();
						else
							overChargeTick();
					} else
						chargeTick();
					wasCharged = isCharged;
				}
			}
		} else {
			if (isCharged())
				applySpell();
			else
				onInterrupt();
			stopCast();
		}
	}

	protected abstract boolean isCharged();

	protected abstract void chargeTick();

	protected abstract void overChargeTick();

	protected abstract boolean isOverCharged();

	protected abstract void onCharged();

	protected void applySpell() {
		if (level().isClientSide())
			return;
		for (var target : target()) {
			SpellSource source = new SpellSource(getCaster(), InteractionHand.MAIN_HAND);
			SpellContext context = new SpellContext(this.level(), getSpell());
			HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(getSpell(), source, target, context);
			if (getCaster() instanceof Player player) {
				results.forEach((key, value) -> {
					if (value.is_success) {
						SpellCaster.addComponentRoteProgress(player, key);
					}
				});
			}
		}
	}

	protected abstract Collection<SpellTarget> target();

	protected abstract void onInterrupt();

	@Nullable
	public LivingEntity getCaster() {
		if (cachedCaster == null) {
			int id = entityData.get(CASTER_ID);
			Entity found = level().getEntity(id);
			if (found != null && found instanceof LivingEntity) {
				cachedCaster = (LivingEntity) found;
			}
		}

		return cachedCaster;
	}

	public float getLifetime() {
		return entityData.get(LIFETIME);
	}

	public void setCaster(LivingEntity caster) {
		if (caster != null) {
			entityData.set(CASTER_ID, caster.getId());
		}

	}

	public SpellRecipe getSpell() {
		if (cachedRecipe == null) {
			cachedRecipe = SpellRecipe.fromNBT(entityData.get(SPELL_RECIPE));
		}
		return cachedRecipe;
	}

	public void setSpell(ISpellDefinition spell) {
		CompoundTag nbt = new CompoundTag();
		spell.writeToNBT(nbt);
		entityData.set(SPELL_RECIPE, nbt);
	}

	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.put("spell", entityData.get(SPELL_RECIPE));
		int casterId = entityData.get(CASTER_ID);
		compound.putInt("caster_uuid", casterId);
	}

	protected void readAdditionalSaveData(CompoundTag compound) {
		if (compound.contains("spell")) {
			entityData.set(SPELL_RECIPE, (CompoundTag) compound.get("spell"));
		}

		if (compound.contains("caster_uuid")) {
			entityData.set(CASTER_ID, compound.getInt("caster_uuid"));
		}

	}

	protected void defineSynchedData() {
		entityData.define(CASTER_ID, -1);
		entityData.define(SPELL_RECIPE, new CompoundTag());
		entityData.define(LIFETIME, 0f);
	}

	public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public final int getOverrideColor() {
		LivingEntity caster = getCaster();
		SpellRecipe recipe = getSpell();
		if (caster != null && recipe != null) {
			MutableInt color = new MutableInt(-1);
			if (caster instanceof Player) {
				caster.getCapability(PlayerMagicProvider.MAGIC).ifPresent((m) -> {
					color.setValue(m.getParticleColorOverride());
				});
			}

			if (color.getValue() == -1) {
				color.setValue(recipe.getParticleColorOverride());
			}

			return color.getValue();
		} else {
			return -1;
		}
	}
}
