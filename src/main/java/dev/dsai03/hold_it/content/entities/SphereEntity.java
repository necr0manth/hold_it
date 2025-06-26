package dev.dsai03.hold_it.content.entities;

import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.content.client.particles.ParticleBallFx;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.AffinityDistribution;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.SpellHolder;
import dev.dsai03.hold_it.util.SpellUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class SphereEntity extends Projectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.FLOAT);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(SphereEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(SphereEntity.class);
    private boolean isStationary = false;
    public Vec3 targetPosition;
    private float lastPower = -1;
    private Entity2EntityReference<LivingEntity> casterRef;
    private SpellHolder spellHolder;

    private BallData renderBallData;
    @OnlyIn(Dist.CLIENT)
    private ParticleBallFx.BallFxData fxData;
    @OnlyIn(Dist.CLIENT)
    ParticleBallFx fx;

    @OnlyIn(Dist.CLIENT)
    public ParticleBallFx.BallData getFxSphereData() {
        var renderBallData = getRenderBallData();
        if (renderBallData == null)
            return null;
        return renderBallData.toFxBallData();
    }

    @OnlyIn(Dist.CLIENT)
    public BallData getRenderBallData() {
        if (isRemoved())
            return null;
        return renderBallData;
    }

    public SphereEntity(EntityType<? extends SphereEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SphereEntity(Level level, SpellSevenShapeEntity owner) {
        this(AwesomeEntityTypes.SPHERE_ENTITY_TYPE.get(), level);
        setOwner(owner);
        casterRef.set(owner.getCaster());
        spellHolder.setSpell(owner.getSpell());
        setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5f)).subtract(this.getBoundingBox().getCenter()));
    }


    public void setStationary() {
        this.isStationary = true;
        this.targetPosition = null;
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (isStationary) {
                setDeltaMovement(Vec3.ZERO);
                float powerFactor = getPower() / SpellSevenShapeEntity.defaultPower;
                int dynamicDelay = 5 + (int) (300 * powerFactor);
                if (tickCount >= dynamicDelay) {
                    explode();
                    discard();
                }
            } else if (targetPosition != null) {
                setDeltaMovement(targetPosition.subtract(position()).scale(0.1));
            }
        }

        super.tick();

        this.setPos(position().add(getDeltaMovement()));

        if (entityData.get(POWER) != lastPower && !this.level().isClientSide) {
            lastPower = entityData.get(POWER);
            refreshDimensions();
        }

        if (level().isClientSide) {
            clientTick();
        }
    }

    private void explode() {

        if (level().isClientSide || getOwner() == null) return;

        var targets = new ArrayList<SpellTarget>();
        float powerFactor = getPower() / SpellSevenShapeEntity.defaultPower; // От 0 до 1
        float dynamicRadius = 1 + (7 * powerFactor); // От 1 до 8

        level().getEntities(getOwner(), this.getBoundingBox().inflate(dynamicRadius),
                        (Entity e) -> e != this && e.position().distanceTo(this.position()) < dynamicRadius)
                .stream().map(SpellTarget::new).forEach(targets::add);

        for (int i = -Mth.ceil(dynamicRadius); i <= Mth.ceil(dynamicRadius); i++) {
            for (int j = -1; j <= Mth.ceil(dynamicRadius); j++) {
                for (int k = -Mth.ceil(dynamicRadius); k <= Mth.ceil(dynamicRadius); k++) {
                    var pos = BlockPos.containing(this.position().add(i, j, k));
                    if (pos.getCenter().distanceTo(this.position()) > dynamicRadius)
                        continue;
                    if (level().getBlockState(pos).isAir())
                        continue;
                    if (j == -1)
                        targets.add(new SpellTarget(pos, Direction.UP));
                    else
                        targets.add(new SpellTarget(pos, null));
                }
            }
        }

        for (var target : targets) {
            SpellSource source = new SpellSource(casterRef.get(), InteractionHand.MAIN_HAND);
            SpellContext context = new SpellContext(this.level(), spellHolder.getSpell());
            SpellUtils.cast(spellHolder.getSpell(), source, target, context);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        fxData = new ParticleBallFx.BallFxData(pt -> spellHolder.getSpell().colorParticle(pt, getCaster()), AffinityDistribution.fromSpell(spellHolder.getSpell()));
        if (fx == null)
            fx = new ParticleBallFx(() -> fxData, this::getFxSphereData);
        fx.tick();
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    public void setPower(float power) {
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
        entityData.define(POWER, 0.0f);
        spellHolder = SpellHolder.createAndDefine(SPELL, entityData, "spell");
        casterRef = Entity2EntityReference.createAndDefine(CASTER, "caster", this);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (POWER.equals(key)) {
            refreshDimensions();
        }
    }

    public void setSpell(ISpellDefinition spell) {
        spellHolder.setSpell(spell);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("power", this.entityData.get(POWER));
        compound.putBoolean("isStationary", isStationary);
        if (targetPosition != null) {
            compound.putDouble("targetX", targetPosition.x);
            compound.putDouble("targetY", targetPosition.y);
            compound.putDouble("targetZ", targetPosition.z);
        }
        spellHolder.save(compound);
        casterRef.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setPower(pCompound.getFloat("power"));
        isStationary = pCompound.getBoolean("isStationary");
        if (pCompound.contains("targetX")) {
            targetPosition = new Vec3(pCompound.getDouble("targetX"), pCompound.getDouble("targetY"), pCompound.getDouble("targetZ"));
        }
        spellHolder.load(pCompound);
        casterRef.load(pCompound);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!level().isClientSide && !isStationary) {
            explode();
            discard();
        }
    }
}