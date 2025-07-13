package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.attributes.Attribute;
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
import dev.dsai03.hold_it.util.TargetTracker;
import lombok.Getter;
import lombok.Setter;
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

public class BallEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THROWN = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ID = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.INT);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(BallEntity.class);
    private static final TargetTracker.DataAccessor TARGET_TRACKER = new TargetTracker.DataAccessor(BallEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(BallEntity.class);
    private Entity2EntityReference<LivingEntity> casterRef;
    private TargetTracker targetTracker;
    private float lastPower = 1;
    @Getter
    private SpellHolder spellHolder;
    // Настройки самонаведения
    @Getter
    private boolean isHomingEnabled = false;
    @Getter
    @Setter
    private int targetSearchCooldown = 0; // Кулдаун поиска новых целей

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

    public BallEntity(EntityType<? extends BallEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public BallEntity(Level level, AwesomeSpellShapeEntity owner, int id, boolean enableHoming) {
        this(AwesomeEntityTypes.BALL_ENTITY_TYPE.get(), level);
        setOwner(owner);

        // Проверяем что caster не null перед установкой
        LivingEntity caster = owner.getCaster();
        if (caster != null) {
            casterRef.set(caster);
        }

        setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5f)).subtract(this.getBoundingBox().getCenter()));
        entityData.set(ID, id);
        this.isHomingEnabled = enableHoming;
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    public void tick() {
        var caster = getCaster();
        if (caster != null && getOwner() != null) {
            var ballData = getOwner().getBallData(entityData.get(ID), caster.getEyePosition(), caster.getLookAngle(), caster.yHeadRot * Mth.DEG_TO_RAD, 0);
            setPower(ballData.power());
            setPos(ballData.pos().subtract(0, ballData.power() / 2, 0));
        }

        if (entityData.get(THROWN) && isHomingEnabled && !level().isClientSide) {
            targetTracker.setTurnRate(0.2f);
            targetTracker.tick();
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
                renderBallData = getOwner().getBallData(entityData.get(ID), pos, lookAngle, caster.yHeadRot * Mth.DEG_TO_RAD, partialTick / 20);
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
        entityData.define(POWER, 0.0f);
        entityData.define(ID, -1);
        entityData.define(THROWN, false);
        spellHolder = SpellHolder.createAndDefine(SPELL, entityData, "spell");
        casterRef = Entity2EntityReference.createAndDefine(CASTER, "caster", this);
        targetTracker = TargetTracker.createAndDefine(TARGET_TRACKER, "targetTracking", e -> e != getCaster() && e instanceof LivingEntity, this);
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
        compound.putBoolean("homingEnabled", isHomingEnabled);
        compound.putInt("targetSearchCooldown", targetSearchCooldown);
        spellHolder.save(compound);
        casterRef.save(compound);
        targetTracker.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(POWER, pCompound.getFloat("power"));
        entityData.set(THROWN, pCompound.getBoolean("thrown"));
        isHomingEnabled = pCompound.getBoolean("homingEnabled");
        targetSearchCooldown = pCompound.getInt("targetSearchCooldown");
        spellHolder.load(pCompound);
        casterRef.load(pCompound);
        targetTracker.load(pCompound);
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
            var charge = entityData.get(POWER) / 0.7;
            boolean[] wasScaled = new boolean[1];
            boolean[] cancel = new boolean[1];
            if (charge < 0.8) {
                spellHolder.getSpell().iterateComponents(c -> {
                    for (var attribute : c.getContainedAttributes()) {
                        var value = c.getValue(attribute);
                        if (c.getValue(attribute) != 0 && attribute != Attribute.DELAY) {
                            c.setMultiplier(attribute, (float) (c.getMultiplier(attribute) * charge));
                            if (value - c.getValue(attribute) < c.getStep(attribute)) {
                                c.setValue(attribute, value - c.getStep(attribute));
                            }
                            if (c.getValue(attribute) < c.getMinimumValue(attribute))
                                cancel[0] = true;
                            wasScaled[0] = true;
                        }
                    }
                });
            }
            if (!cancel[0] && (wasScaled[0] || charge > 0.7f))
                SpellUtils.cast(spellHolder.getSpell(), source, target, context);
        }
        discard();
    }

    public void shoot(Vec3 dir) {
        entityData.set(THROWN, true);
        shoot(dir.x, dir.y, dir.z, (float) (1 / (entityData.get(POWER) + 0.5)), 0);
    }
}
