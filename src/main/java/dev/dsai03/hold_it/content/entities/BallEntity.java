package dev.dsai03.hold_it.content.entities;

import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.particles.OffsetedParticle;
import dev.dsai03.hold_it.particles.OffsetedParticleEngine;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.LazySpellHolder;
import dev.dsai03.hold_it.util.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
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

public class BallEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THROWN = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<CompoundTag> SPELL_RECIPE = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.COMPOUND_TAG);
    public static final EntityDataAccessor<Integer> ID = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> CASTER_DIMENSION = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.STRING);
    private Entity2EntityReference<LivingEntity> caster;
    public float lastPower = 1;
    public final LazySpellHolder spell = new LazySpellHolder(() -> {
        var s = entityData.get(SPELL_RECIPE);
        if (s.isEmpty())
            return null;
        return SpellRecipe.fromNBT(s);
    });

    public BallEntity(EntityType<? extends BallEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public BallEntity(Level level, AwesomeSpellShapeEntity owner, int id) {
        this(AwesomeEntityTypes.BALL_ENTITY_TYPE.get(), level);
        setOwner(owner);
        caster.set(owner.getCaster());
        setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5f)).subtract(this.getBoundingBox().getCenter()));
        entityData.set(ID, id);
    }

    public LivingEntity getCaster() {
        return caster.get();
    }

    public void tick() {
        var caster = getCaster();
        if (caster != null && getOwner() != null) {
            var ballData = getOwner().getBallData(entityData.get(ID), caster.getEyePosition(), caster.getLookAngle(), caster.yHeadRot * Mth.DEG_TO_RAD);
            setPower(ballData.radius());
            setPos(ballData.pos().subtract(0, ballData.radius() / 2, 0));
        }
        if (caster != null && getOwner() == null && getDeltaMovement().length() < 0.02) {
            entityData.set(THROWN, true);
            onHit(position());
        }
        super.tick();
        if (entityData.get(POWER) != lastPower && !this.level().isClientSide) {
            lastPower = entityData.get(POWER);
            refreshDimensions();
        }
        if (level().isClientSide)
            clientTick();
    }

    public void setPower(float power) {
        entityData.set(POWER, power);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        var affinity = spell.getRandomAffinity();
        if (affinity == null) return;
        for (int i = 0; i < 10; i++) {
            OffsetedParticleEngine.instance.addParticle(new OffsetedParticle(ParticleUtils.createParticle(spell.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getCaster()), Minecraft.getInstance().level, new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(Math.pow(random.nextDouble(), 1 / 3d) * entityData.get(POWER) * 0.5f), Vec3.ZERO)).offset(() -> {
                if(isRemoved())
                    return null;
                if (getOwner() != null) {
                    var caster = getCaster();
                    if (caster == null)
                        return null;
                    var partialTick = Minecraft.getInstance().getFrameTime();
                    var lookAngle = caster.getViewVector(partialTick);
                    var pos = new Vec3(Mth.lerp(partialTick, caster.xo, caster.getX()), Mth.lerp(partialTick, caster.yo, caster.getY()) + caster.getEyeHeight(), Mth.lerp(partialTick, caster.zo, caster.getZ()));
                    return getOwner().getBallData(entityData.get(ID), pos, lookAngle, caster.yHeadRot * Mth.DEG_TO_RAD).pos();
                }
                return new Vec3(xo, yo, zo).lerp(position(), Minecraft.getInstance().getFrameTime()).add(0, getBbHeight() / 2, 0);
            }));
        }
    }

    public AwesomeSpellShapeEntity getOwner() {
        var superOwner = super.getOwner();
        if (superOwner instanceof AwesomeSpellShapeEntity owner)
            return owner;
        else if (superOwner == null)
            return null;
        throw new RuntimeException("0_o");
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        var power = entityData.get(POWER);
        return new EntityDimensions(power, power, false);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(POWER, 0.0f);
        this.entityData.define(SPELL_RECIPE, new CompoundTag());
        this.entityData.define(ID, -1);
        this.entityData.define(THROWN, false);
        caster = new Entity2EntityReference<>(CASTER_ID, CASTER_DIMENSION, "caster", this);
        caster.define();
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
        compound.putBoolean("thrown", this.entityData.get(THROWN));
        compound.put("spell", entityData.get(SPELL_RECIPE));
        compound.putInt("index", entityData.get(ID));
        caster.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(POWER, pCompound.getFloat("power"));
        entityData.set(THROWN, pCompound.getBoolean("thrown"));
        entityData.set(SPELL_RECIPE, (CompoundTag) pCompound.get("spell"));
        caster.load(pCompound);
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
        if (!entityData.get(THROWN)) {
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

    private void onHit(Vec3 location) {
        if (level().isClientSide)
            return;
        if (getCaster() == null) {
            discard();
            return;
        }
        if (!entityData.get(THROWN)) {
            return;
        }
        var targets = new ArrayList<SpellTarget>();
        var power = entityData.get(POWER);
        for (int i = -Mth.ceil(power); i <= Mth.ceil(power); i++) {
            for (int j = -Mth.ceil(power); j <= Mth.ceil(power); j++) {
                for (int k = -Mth.ceil(power); k <= Mth.ceil(power); k++) {
                    if (new Vec3(i, j, k).length() <= power)
                        targets.add(new SpellTarget(BlockPos.containing(location.add(i, j, k)), null));
                }
            }
        }
        for (var target : targets) {
            SpellSource source = new SpellSource(getCaster(), InteractionHand.MAIN_HAND);
            SpellContext context = new SpellContext(this.level(), spell.getSpell());
            HashMap<SpellEffect, ComponentApplicationResult> results = SpellCaster.ApplyComponents(spell.getSpell(), source, target, context);
            if (getCaster() instanceof Player player) {
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
        entityData.set(THROWN, true);
        shoot(dir.x, dir.y, dir.z, (float) (2 / (entityData.get(POWER) + 0.5)), 0);
    }
}
