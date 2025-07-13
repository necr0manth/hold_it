package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.content.client.particles.lightnings.LightningBall;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.*;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Collections;
import java.util.Random;

import java.util.ArrayList;
import java.util.List;

public class SphereEntity extends Projectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.FLOAT);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(SphereEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(SphereEntity.class);
    private static final EntityDataAccessor<Float> MAX_POWER = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_MAGNITUDE = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_DELAY = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_RADIUS = SynchedEntityData.defineId(SphereEntity.class, EntityDataSerializers.FLOAT);
    private boolean isStationary = false;
    public Vec3 targetPosition;
    private float lastPower = -1;
    private Entity2EntityReference<LivingEntity> casterRef;
    private SpellHolder spellHolder;

    public SphereEntity(EntityType<? extends SphereEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SphereEntity(Level level, SpellSevenShapeEntity owner, float magnitude, float power, float delay, float radius) {
        this(AwesomeEntityTypes.SPHERE_ENTITY_TYPE.get(), level);
        setOwner(owner);
        casterRef.set(owner.getCaster());
        spellHolder.setSpell(owner.getSpell());
        setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5f)).subtract(this.getBoundingBox().getCenter()));
        entityData.set(MAX_MAGNITUDE, magnitude);
        entityData.set(MAX_POWER, power);
        entityData.set(MAX_DELAY, delay);
        entityData.set(MAX_RADIUS, radius);
    }


    public void setStationary() {
        this.isStationary = true;
        this.targetPosition = null;
        this.setDeltaMovement(Vec3.ZERO);
    }

    int timer = 0;
    private double globalRotationAngle = 0;

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (isStationary) {
                timer++;
                setDeltaMovement(Vec3.ZERO);
                float powerFactor = getPower() / entityData.get(MAX_POWER);
                float dynamicDelay = (5 + (int) (40 * powerFactor)) * entityData.get(MAX_DELAY);
                if (timer >= dynamicDelay) {
                    explode();
                    discard();
                }
            } else if (targetPosition != null) {
                setDeltaMovement(targetPosition.subtract(getBoundingBox().getCenter()).scale(0.1));
            }
        }

        super.tick();
        globalRotationAngle += 0.004;

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

        List<SpellTarget> targets = new ArrayList<SpellTarget>();
        float powerFactor = getPower() / entityData.get(MAX_POWER);
        float dynamicRadius = (5 * powerFactor) * entityData.get(MAX_RADIUS);
//
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
        Collections.shuffle(targets);
        var targetCount = (int) (entityData.get(MAX_MAGNITUDE) * getPower() * entityData.get(MAX_RADIUS) / entityData.get(MAX_POWER));
        targets = targets.subList(0, Math.min(targetCount, targets.size()));
        for (var target : targets) {
            SpellSource source = new SpellSource(casterRef.get(), InteractionHand.MAIN_HAND);
            SpellContext context = new SpellContext(this.level(), spellHolder.getSpell());
            SpellUtils.cast(spellHolder.getSpell(), source, target, context);
        }
    }

    LightningBall lightningBall;

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (getOwner() instanceof LivingEntity owner) {
            this.isStationary = !owner.isUsingItem();
        }
        float powerFactor = Math.max(0.01f, getPower() / entityData.get(MAX_POWER));
        var affinity = AffinityDistribution.fromSpell(spellHolder.getSpell()).getRandomAffinity();
        var affinities = AffinityDistribution.fromSpell(spellHolder.getSpell());
        if (affinities.getAffinity(Affinity.LIGHTNING) != 0 && lightningBall == null) {
            var baseColor = new Color(100, 67, 255);
            lightningBall = new LightningBall(Minecraft.getInstance().level, 10, baseColor, new Color(255, 67, 255), () -> 1f, () -> isRemoved() ? null : position());
            lightningBall.spawn(0.3f);
        } else if (affinities.getAffinity(Affinity.LIGHTNING) == 0 && lightningBall != null) {
            lightningBall.fadeOut(0.5f);
            lightningBall = null;
        }

        Random random = new Random();
        float coreRadius = 0.05f + 1.95f * powerFactor;

        float rotationSpeed = isStationary ? 3.0f : 1.5f;
        float globalRotation = (tickCount * rotationSpeed) % 360;
        float radRotation = globalRotation * ((float) Math.PI / 180f);

        int particleCount = isStationary ?
                (int) (10 + 75 * powerFactor) :
                (int) (5 + 55 * powerFactor);

        for (int i = 0; i < particleCount; i++) {
            if (isStationary) {
                double distance;
                if (random.nextFloat() < 0.7f) {
                    distance = coreRadius * (0.5 + random.nextDouble() * 0.8);
                } else {
                    distance = coreRadius * (1.0 + random.nextDouble() * 1.5);
                }

                double angle = random.nextDouble() * Math.PI * 2 + radRotation;
                double height = (random.nextDouble() - 0.5) * coreRadius * 0.1;

                Vec3 pos = position().add(
                        distance * Math.cos(angle),
                        height,
                        distance * Math.sin(angle)
                );

                Vec3 tangent = new Vec3(-pos.z + position().z, 0, pos.x - position().x).normalize();
                double speed = 0.02 * Math.pow(coreRadius / distance, 0.6);
                speed *= (0.9 + random.nextDouble() * 0.2);

                ParticleUtils.addParticle(
                        affinity == Affinity.WIND ? new MAParticleType(ParticleInit.AIR_VELOCITY.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(177, 201, 223) :
                                affinity == Affinity.LIGHTNING ? new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(155,38,182) :
                                        affinity == Affinity.BLOOD ? new MAParticleType(ParticleInit.DROPLET.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(128,5,0)
                                : spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getOwner()),
                        pos,
                        tangent.scale(speed),
                        ParticleUtils.EMPTY_TICKER,
                        affinity == Affinity.LIGHTNING ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(25)) : affinity == Affinity.WIND ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(15)) : ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(75))
                );
            } else {
                for (int i3 = 0; i3 < 3; i3++) {
                    int armIndex = random.nextInt(5);
                    double armPhase = armIndex * (2 * Math.PI / 5) + globalRotationAngle;

                    int segmentCount = 12;
                    int segment = random.nextInt(segmentCount);
                    double progress = segment * (0.84 / (segmentCount - 1));

                    double distance = 0.7 + 5.0 * progress;
                    double angle = armPhase + 2.2 * progress * Math.PI * 2;

                    Vec3 pos = position().add(
                            distance * Math.cos(angle),
                            (random.nextDouble() - 0.5) * 0.1,
                            distance * Math.sin(angle)
                    );

                    Vec3 toCenter = position().subtract(pos).normalize().scale(0.12);
                    Vec3 tangent = new Vec3(-pos.z + position().z, 0, pos.x - position().x).normalize();
                    Vec3 velocity = toCenter.add(tangent.scale(0.000001));

                    // ============= ПОВОРОТ ВЕКТОРА СКОРОСТИ =============
                    final double rotationAngle = -0.9;
                    final double cosAngle = Math.cos(rotationAngle);
                    final double sinAngle = Math.sin(rotationAngle);

                    double rotatedX = velocity.x * cosAngle - velocity.z * sinAngle;
                    double rotatedZ = velocity.x * sinAngle + velocity.z * cosAngle;
                    Vec3 rotatedVelocity = new Vec3(rotatedX, velocity.y, rotatedZ);
                    // ====================================================

                    float particleSize = affinity == Affinity.WIND ? 0.06f : 0.07f;

                    ParticleUtils.addParticle(
                            affinity == Affinity.WIND ? new MAParticleType(ParticleInit.AIR_VELOCITY.get()).setScale(particleSize).setColor(177, 201, 223) :
                            affinity == Affinity.LIGHTNING ? new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get()).setScale(particleSize).setColor(155,38,182) :
                                    affinity == Affinity.BLOOD ? new MAParticleType(ParticleInit.DROPLET.get()).setScale(particleSize).setColor(128,5,0) :
                                            spellHolder.getSpell().colorParticle(
                                    new MAParticleType(ParticleUtils.getParticleType(affinity)),
                                    getOwner()).setScale(particleSize),
                            pos,
                            rotatedVelocity,
                            ParticleUtils.EMPTY_TICKER,
                            ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(165))
                    );
                }
            }

            if (random.nextFloat() < (isStationary ? 0.5f : 0.3f)) { //сфера
                for (int i2 = 0; i2 < 5; i2++) {
                    double theta = random.nextDouble() * Math.PI;
                    double phi = random.nextDouble() * Math.PI * 2;
                    double r = coreRadius * (0.5 + 0.2 * random.nextDouble());

                    Vec3 pos = position().add(
                            r * Math.sin(theta) * Math.cos(phi),
                            r * Math.cos(theta),
                            r * Math.sin(theta) * Math.sin(phi)
                    );
                    ParticleUtils.addParticle(
                            affinity == Affinity.WIND ? new MAParticleType(ParticleInit.AIR_VELOCITY.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(177, 201, 223) :
                                    affinity == Affinity.FIRE ? spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)).setColor(255,43,20), getOwner()) :
                                            affinity == Affinity.ICE ? spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)).setColor(111,122,159), getOwner()) :
                                                    affinity == Affinity.ARCANE ? spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)).setColor(128,49,167), getOwner()) :
                                                            affinity == Affinity.LIGHTNING ? spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)).setColor(128,49,167), getOwner()) :
                                                                    affinity == Affinity.BLOOD ? new MAParticleType(ParticleInit.DROPLET.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(128,5,0) :
                                                                            affinity == Affinity.EARTH ? new MAParticleType(ParticleInit.DUST.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(220,88,42) :
                                                    spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getOwner()), pos,
                            position().subtract(pos).normalize().scale(0.03),
                            ParticleUtils.EMPTY_TICKER,
                            affinity == Affinity.LIGHTNING ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(325)) : affinity == Affinity.WIND ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(15)) : ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(60))
                    );
                }
            }
        }

        if (tickCount % 2 == 0) { //точка
            for (int i = 0; i < (1); i++) {
                double angle = i * (Math.PI / 2);
                double offset = coreRadius * 0.001;

                ParticleUtils.addParticle(
                        affinity == Affinity.WIND ? new MAParticleType(ParticleInit.AIR_VELOCITY.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(177, 201, 223) :
                                affinity == Affinity.BLOOD ? new MAParticleType(ParticleInit.DROPLET.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(158,5,0) :
                                spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getOwner()), position().add(
                                offset * Math.cos(angle),
                                (random.nextDouble() - 0.5) * 0.1,
                                offset * Math.sin(angle)
                        ),
                        Vec3.ZERO,
                        ParticleUtils.EMPTY_TICKER,
                        affinity == Affinity.LIGHTNING ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(25)) : affinity == Affinity.WIND ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(15)) : ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(50))
                );
            }

            if (coreRadius > 0.3f) { //кольца
                int rings = 1;
                for (int r = 0; r < rings; r++) {
                    double radius = coreRadius * (0.4 + r * 0.4);
                    int particles = isStationary ? 18 + r * 6 : 12 + r * 4;

                    for (int i = 0; i < particles; i++) {
                        double angle = i * (2 * Math.PI / particles) + radRotation * 0.7;

                        ParticleUtils.addParticle(
                                affinity == Affinity.WIND ? new MAParticleType(ParticleInit.AIR_VELOCITY.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(177, 201, 223) :
                                        affinity == Affinity.LIGHTNING ? new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(155,38,182) :
                                                affinity == Affinity.BLOOD ? new MAParticleType(ParticleInit.DROPLET.get()).setScale(random.nextFloat(0.02f, 0.05f)).setColor(128,5,0) :
                                        spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getOwner()), position().add(
                                        radius * Math.cos(angle),
                                        (random.nextDouble() - 0.5) * 0.2,
                                        radius * Math.sin(angle)
                                ),
                                new Vec3(-Math.sin(angle), 0, Math.cos(angle)).scale(0.02),
                                ParticleUtils.EMPTY_TICKER,
                                affinity == Affinity.LIGHTNING ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(25)) : affinity == Affinity.WIND ? ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(15)) : ParticleUtils.relativeTo(() -> position(), ParticleUtils.fadeInHuy(125))
                        );
                    }
                }
            }
        }
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
        return new EntityDimensions(0.1f, 0.1f, false);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(POWER, 0.0f);
        entityData.define(MAX_POWER, 0.0f);
        entityData.define(MAX_MAGNITUDE, 0.0f);
        entityData.define(MAX_DELAY, 0.0f);
        entityData.define(MAX_RADIUS, 0.0f);
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
        compound.putFloat("maxPower", this.entityData.get(MAX_POWER));
        compound.putFloat("maxMagnitude", this.entityData.get(MAX_MAGNITUDE));
        compound.putFloat("maxDelay", this.entityData.get(MAX_DELAY));
        compound.putFloat("maxRadius", this.entityData.get(MAX_RADIUS));
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
        var maxPower = pCompound.getFloat("maxPower");
        var maxMagnitude = pCompound.getFloat("maxMagnitude");
        var maxDelay = pCompound.getFloat("maxDelay");
        var maxRadius = pCompound.getFloat("maxRadius");
        entityData.set(MAX_POWER, maxPower);
        entityData.set(MAX_MAGNITUDE, maxMagnitude);
        entityData.set(MAX_DELAY, maxDelay);
        entityData.set(MAX_RADIUS, maxRadius);

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