package dev.dsai03.hold_it.content.entities;

import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.particles.types.movers.ParticleSphereOrbitMover;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.LazySpellHolder;
import dev.dsai03.hold_it.util.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;

public class SphereEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.COMPOUND_TAG);
    public Vec3 targetPosition;
    public float power = 1;
    private Entity2EntityReference<LivingEntity> caster;
    public final LazySpellHolder spell = new LazySpellHolder(() -> {
        var s = entityData.get(SPELL_RECIPE);
        if (s.isEmpty())
            return null;
        return SpellRecipe.fromNBT(s);
    });

    public SphereEntity(EntityType<? extends SphereEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SphereEntity(Level level, LivingEntity owner) {
        this(AwesomeEntityTypes.SPHERE_ENTITY_TYPE.get(), level);
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
        if (level().isClientSide)
            clientTick();
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        var affinity = spell.getRandomAffinity();
        if (affinity == null) return;
        for (int i = 0; i < 100; i++)
            ParticleUtils.addParticle(spell.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getOwner()), position().add(new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(Math.pow(random.nextDouble(), 1 / 3d))), Vec3.ZERO, ParticleUtils.EMPTY_TICKER, ParticleUtils.relativeTo(()->new Vec3(xo, yo, zo), ParticleUtils.EMPTY_TICKER));
    }

    public void setPower(float power) {
        this.power = power;
        this.entityData.set(POWER, power);
    }

    public float getPower() {
        return this.entityData.get(POWER);
    }

    public LivingEntity getCaster() {
        return caster.get();
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return new EntityDimensions(getPower(), getPower(), false);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(POWER, 0.0f);
        this.entityData.define(SPELL_RECIPE, new CompoundTag());
    }


    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (POWER.equals(key)) {
            this.refreshDimensions();
        }
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
        compound.put("spell", entityData.get(SPELL_RECIPE));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setPower(pCompound.getFloat("power"));
        if (pCompound.contains("spell")) {
            entityData.set(SPELL_RECIPE, (CompoundTag) pCompound.get("spell"));
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        onHit(hitResult.getLocation().add(hitResult.getBlockPos().getCenter().subtract(hitResult.getLocation()).normalize().scale(0.1)));
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (level().isClientSide)
            return;
        if (getCaster() == null) {
            discard();
            return;
        }
        SpellSource source = new SpellSource(getCaster(), InteractionHand.MAIN_HAND);
        SpellContext context = new SpellContext(this.level(), spell.getSpell());
        HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(spell.getSpell(), source, new SpellTarget(hitResult.getEntity()), context);
        if (getCaster() instanceof Player player) {
            results.forEach((key, value) -> {
                if (value.is_success) {
                    SpellCaster.addComponentRoteProgress(player, key);
                }
            });
        }
        onHit(hitResult.getLocation());
    }

    private void onHit(Vec3 pos) {
        if (getOwner() == null) {
            discard();
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
            SpellContext context = new SpellContext(this.level(), spell.getSpell());
            HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(spell.getSpell(), source, target, context);
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
}
