package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
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
import net.minecraft.client.Minecraft;
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
import java.util.Random;

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
        float powerFactor = getPower() / SpellSevenShapeEntity.defaultPower;
        float dynamicRadius = 1 + (7 * powerFactor);

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
        var affinity = AffinityDistribution.fromSpell(spellHolder.getSpell()).getRandomAffinity();
        Random random = new Random();
        float powerFactor = Math.max(0.01f, getPower() / SpellSevenShapeEntity.defaultPower);
        float coreRadius = 0.05f + 0.95f * powerFactor;

        final int spiralArms = 3;
        final double spiralTightness = 2.5;
        final double spiralThickness = 0.3;

        int particleCount;
        if (isStationary) {
            particleCount = (int) (8 + 15 * powerFactor);
        } else {
            particleCount = (int) (30 + 70 * powerFactor);
        }

        for (int i = 0; i < particleCount; i++) {
            if (isStationary) {
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = coreRadius * 1.5 + random.nextDouble() * coreRadius * 2;

                Vec3 diskPos = position().add(
                        distance * Math.cos(angle),
                        (random.nextDouble() - 0.5) * coreRadius * 0.3,
                        distance * Math.sin(angle)
                );

                Vec3 tangent = new Vec3(-diskPos.z + position().z, 0, diskPos.x - position().x).normalize();
                Vec3 velocity = tangent.scale(0.05 + 0.1 * powerFactor);

                ParticleUtils.addParticle(
                        spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)),
                                getOwner()
                        ), diskPos, velocity, ParticleUtils.EMPTY_TICKER, ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER));
            } else {
                int armIndex = random.nextInt(spiralArms);
                double armPhase = armIndex * (2 * Math.PI / spiralArms);

                double spiralProgress = random.nextDouble();
                double distance = 0.5 + 5.0 * spiralProgress;
                double angle = armPhase + spiralTightness * spiralProgress * Math.PI * 2;

                double radialOffset = (random.nextDouble() - 0.5) * spiralThickness;
                double verticalOffset = (random.nextDouble() - 0.5) * coreRadius * 0.5;

                Vec3 spiralPos = position().add(
                        (distance + radialOffset) * Math.cos(angle),
                        verticalOffset,
                        (distance + radialOffset) * Math.sin(angle)
                );

                Vec3 toCenter = position().subtract(spiralPos).normalize().scale(0.15);

                Vec3 tangent = new Vec3(-spiralPos.z + position().z, 0, spiralPos.x - position().x).normalize();
                Vec3 rotation = tangent.scale(0.08 * (1 + spiralProgress));

                Vec3 velocity = toCenter.add(rotation);

                ParticleUtils.addParticle(
                        spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)),
                                getOwner()
                        ), spiralPos, velocity, ParticleUtils.EMPTY_TICKER, ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER));
            }

            if (random.nextFloat() < 0.3f) {
                double theta = random.nextDouble() * Math.PI;
                double phi = random.nextDouble() * Math.PI * 2;
                double r = coreRadius * (0.95 + 0.1 * random.nextDouble());

                Vec3 surfacePos = position().add(
                        r * Math.sin(theta) * Math.cos(phi),
                        r * Math.cos(theta),
                        r * Math.sin(theta) * Math.sin(phi)
                );

                Vec3 velocity = position().subtract(surfacePos).normalize()
                        .scale(0.02 + 0.03 * random.nextDouble());

                ParticleUtils.addParticle(
                        spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)),
                                getOwner()
                        ), surfacePos, velocity, ParticleUtils.EMPTY_TICKER, ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER));
            }
        }

        if (tickCount % 3 == 0) {
            ParticleUtils.addParticle(
                    spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)),
                            getOwner()
                    ), position(), Vec3.ZERO, ParticleUtils.EMPTY_TICKER, ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER));

            if (coreRadius > 0.3f) {
                int rings = 1 + (int)(coreRadius * 2);
                for (int r = 0; r < rings; r++) {
                    double ringRadius = coreRadius * 1.5 + r * 0.15;
                    int particlesInRing = 8 + r * 2;

                    for (int i = 0; i < particlesInRing; i++) {
                        double angle = i * (Math.PI * 2 / particlesInRing);
                        Vec3 ringPos = position().add(
                                ringRadius * Math.cos(angle),
                                (random.nextDouble() - 0.5) * 0.1,
                                ringRadius * Math.sin(angle)
                        );

                        Vec3 tangent = new Vec3(-ringPos.z + position().z, 0, ringPos.x - position().x).normalize();
                        Vec3 velocity = tangent.scale(0.01);

                        ParticleUtils.addParticle(
                                spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)),
                                        getOwner()
                                ), ringPos, velocity, ParticleUtils.EMPTY_TICKER, ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER));
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