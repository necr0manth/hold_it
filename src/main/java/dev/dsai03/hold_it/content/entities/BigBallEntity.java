package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.content.client.particles.ParticleBallFx;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.content.client.particles.core.ParticleAccess;
import dev.dsai03.hold_it.content.client.particles.core.ParticleTickerHolder;
import dev.dsai03.hold_it.content.client.particles.lightnings.LightningBall;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BigBallEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(BigBallEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THROWN = SynchedEntityData.defineId(BigBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(BigBallEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(BigBallEntity.class);
    private Entity2EntityReference<LivingEntity> casterRef;
    private float lastPower = 1;
    private double speed = -1;
    @Getter
    private SpellHolder spellHolder;

    private BallData renderBallData;
    @OnlyIn(Dist.CLIENT)
    private ParticleBallFx.BallFxData fxData;
    @OnlyIn(Dist.CLIENT)
    ParticleBallFx fx;

    @OnlyIn(Dist.CLIENT)
    public ParticleBallFx.BallData getFxBallData() {
        var renderBallData = getRenderBallData();
        if (renderBallData == null)
            return null;
        return renderBallData.toFxBallData();
    }

    public BigBallEntity(EntityType<? extends BigBallEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public BigBallEntity(Level level, BigBallSpellShapeEntity owner) {
        this(AwesomeEntityTypes.BIG_BALL_ENTITY_TYPE.get(), level);
        setOwner(owner);
        casterRef.set(owner.getCaster());
        spellHolder.setSpell(owner.getSpell());
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    public void tick() {
        if (getOwner() != null) {
            var ballData = getOwner().getBallData(1);
            setPower(ballData.power());
            setPos(ballData.pos().subtract(0, getBbHeight()/2, 0));
        }
        super.tick();
        if (!level().isClientSide && speed != -1)
            setDeltaMovement(getDeltaMovement().normalize().scale(speed));
        if (entityData.get(POWER) != lastPower && !level().isClientSide) {
            lastPower = entityData.get(POWER);
            refreshDimensions();
        }
        if (level().isClientSide)
            clientTick();
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (fx == null) {
            fxData = new ParticleBallFx.BallFxData(pt -> spellHolder.getSpell().colorParticle(pt, getCaster()), AffinityDistribution.fromSpell(spellHolder.getSpell()));
            fx = new ParticleBallFx(() -> fxData, this::getFxBallData);
        }
        fx.tick();
    }

    public void setPower(float power) {
        entityData.set(POWER, power);
    }

    @OnlyIn(Dist.CLIENT)
    public void calculateRenderBallData() {
        if (isRemoved()) {
            renderBallData = null;
            return;
        }
        if (getOwner() != null) {
            var partialTick = Minecraft.getInstance().getFrameTime();
            renderBallData = getOwner().getBallData(partialTick);
        } else
            renderBallData = new BallData(new Vec3(xo, yo, zo).lerp(position(), Minecraft.getInstance().getFrameTime()).add(0, getBbHeight() / 2, 0), entityData.get(POWER));
    }

    @OnlyIn(Dist.CLIENT)
    public BallData getRenderBallData() {
        if (isRemoved())
            return null;
        return renderBallData;
    }

    public BigBallSpellShapeEntity getOwner() {
        var superOwner = super.getOwner();
        if (superOwner instanceof BigBallSpellShapeEntity owner)
            return owner;
        else if (superOwner == null)
            return null;
        throw new RuntimeException("0_o");
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        var power = Math.min(entityData.get(POWER), 1);
        return new EntityDimensions(power, power, false);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(POWER, 0.0f);
        entityData.define(THROWN, false);
        spellHolder = SpellHolder.createAndDefine(SPELL, entityData, "spell");
        casterRef = Entity2EntityReference.createAndDefine(CASTER, "caster", this);
    }


    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (POWER.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("power", this.entityData.get(POWER));
        compound.putBoolean("thrown", this.entityData.get(THROWN));
        spellHolder.save(compound);
        casterRef.save(compound);
        compound.putDouble("speed", speed);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(POWER, pCompound.getFloat("power"));
        entityData.set(THROWN, pCompound.getBoolean("thrown"));
        spellHolder.load(pCompound);
        casterRef.load(pCompound);
        speed = pCompound.getDouble("speed");
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        onHit(hitResult.getLocation().add(hitResult.getBlockPos().getCenter().subtract(hitResult.getLocation()).normalize().scale(0.5)));
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
        SpellContext context = new SpellContext(this.level(), spellHolder.getSpell());
        SpellUtils.cast(spellHolder.getSpell(), source, new SpellTarget(hitResult.getEntity()), context);
        onHit(hitResult.getLocation());
    }

    public int precision() {
        return (int) spellHolder.getSpell().getShape().getValue(Attribute.PRECISION);
    }

    private void onHit(Vec3 location) {
        if (level().isClientSide || getOwner() != null) return;

        java.util.List<SpellTarget> blockTargets = new ArrayList<>();
        float power = entityData.get(POWER);
        var maxPower = spellHolder.getSpell().getShape().getValue(Attribute.RADIUS);
        var charge = Math.min(power / maxPower, 1);
        var radius = 1.5f * power;
        var entityRadius = 2 * power;
        List<SpellTarget> targets = new ArrayList<>();
        if (precision() != 2) {
            level().getEntities(getCaster(), this.getBoundingBox().inflate(entityRadius),
                            (Entity e) -> e != this && e.position().distanceTo(location) < entityRadius)
                    .stream().map(SpellTarget::new).forEach(targets::add);
        }

        if (precision() != 1) {
            for (int i = -Mth.ceil(radius); i <= Mth.ceil(radius) + 1; i++) {
                for (int j = -Mth.ceil(radius); j <= Mth.ceil(radius) + 1; j++) {
                    for (int k = -Mth.ceil(radius); k <= Mth.ceil(radius) + 1; k++) {
                        var pos = BlockPos.containing(location.add(i, j, k));
                        if (pos.getCenter().distanceTo(location) > radius)
                            continue;
                        if (level().getBlockState(pos).isAir())
                            continue;
                        blockTargets.add(new SpellTarget(pos, null));
                    }
                }
            }
            Collections.shuffle(blockTargets);
            targets.addAll(blockTargets);
        }
        var targetCount = spellHolder.getSpell().getShape().getValue(Attribute.MAGNITUDE) * charge;
        SpellUtils.cast(spellHolder.getSpell(), new SpellSource(getCaster(), getCaster() instanceof Player player ? player.getUsedItemHand() : getCaster().swingingArm), targets, t -> new SpellContext(level(), spellHolder.getSpell()), spellHolder.getSpell().getManaCost(), spellHolder.getSpell().getManaCost() / targetCount, true);
        discard();
    }

    public void shoot(Vec3 dir) {
        entityData.set(THROWN, true);
        shoot(dir.x, dir.y, dir.z, (float) (1 / (entityData.get(POWER) + 0.5)), 0);
        speed = getDeltaMovement().length();
    }
}