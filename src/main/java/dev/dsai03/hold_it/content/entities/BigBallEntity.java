package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.content.client.particles.ParticleBallFx;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.AffinityDistribution;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.SpellHolder;
import dev.dsai03.hold_it.util.SpellUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class BigBallEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(BigBallEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THROWN = SynchedEntityData.defineId(BigBallEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> ID = SynchedEntityData.defineId(BigBallEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> SPELL = SynchedEntityData.defineId(BigBallEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(BigBallEntity.class);
    private Entity2EntityReference<LivingEntity> casterRef;
    private float lastPower = 1;
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
        setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5f)).subtract(this.getBoundingBox().getCenter()));
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    public void tick() {
        var caster = getCaster();
        if (caster != null && getOwner() != null) {
            var ballData = new BallData(getOwner().getEyePosition().add(getOwner().getLookAngle().scale(3)), 2);
            setPower(ballData.power());
            setPos(ballData.pos().subtract(0, ballData.power() / 2, 0));
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

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        fxData = new ParticleBallFx.BallFxData(pt -> spellHolder.getSpell().colorParticle(pt, getCaster()), AffinityDistribution.fromSpell(spellHolder.getSpell()));
        if (fx == null)
            fx = new ParticleBallFx(() -> fxData, this::getFxBallData);
        fx.tick();
    }

    public void setPower(float power) {
        entityData.set(POWER, power);
    }

    @OnlyIn(Dist.CLIENT)
    public void calculateRenderBallData() {
        if (isRemoved())
            renderBallData = null;
        else if (getOwner() != null) {
            var caster = getCaster();
            if (caster == null)
                renderBallData = null;
            else {
                var partialTick = Minecraft.getInstance().getFrameTime();
                var lookAngle = caster.getViewVector(partialTick);
                var pos = new Vec3(Mth.lerp(partialTick, caster.xo, caster.getX()), Mth.lerp(partialTick, caster.yo, caster.getY()) + caster.getEyeHeight(), Mth.lerp(partialTick, caster.zo, caster.getZ()));
//                renderBallData = getOwner().getBallData(entityData.get(ID), pos, lookAngle, caster.yHeadRot * Mth.DEG_TO_RAD, partialTick / 20);
            }
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
        var power = entityData.get(POWER);
        return new EntityDimensions(power, power, false);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(POWER, 0.0f);
        this.entityData.define(ID, -1);
        this.entityData.define(THROWN, false);
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

    public void setSpell(ISpellDefinition spell) {
        spellHolder.setSpell(spell);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("power", this.entityData.get(POWER));
        compound.putBoolean("thrown", this.entityData.get(THROWN));
        compound.putInt("index", entityData.get(ID));
        spellHolder.save(compound);
        casterRef.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(POWER, pCompound.getFloat("power"));
        entityData.set(THROWN, pCompound.getBoolean("thrown"));
        spellHolder.load(pCompound);
        casterRef.load(pCompound);
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
        SpellContext context = new SpellContext(this.level(), spellHolder.getSpell());
        SpellUtils.cast(spellHolder.getSpell(), source, new SpellTarget(hitResult.getEntity()), context);
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
            SpellContext context = new SpellContext(this.level(), spellHolder.getSpell());
            SpellUtils.cast(spellHolder.getSpell(), source, target, context);
        }
        discard();
    }

    public void shoot(Vec3 dir) {
        entityData.set(THROWN, true);
        shoot(dir.x, dir.y, dir.z, (float) (2 / (entityData.get(POWER) + 0.5)), 0);
    }
}
