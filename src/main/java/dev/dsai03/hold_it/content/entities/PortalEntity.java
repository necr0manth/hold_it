package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.tools.math.MathUtils;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.AffinityDistribution;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.SpellHolder;
import dev.dsai03.hold_it.util.TargetTracker;
import lombok.Builder;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Comparator;
import java.util.Random;

public class PortalEntity extends Entity {
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(PortalEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(PortalEntity.class);
    private static final EntityDataAccessor<Integer> ACTIVATION_TIME = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);

    private Entity2EntityReference<LivingEntity> casterRef;
    private SpellHolder spellHolder;
    private float frequency;
    private float followViewSpeed;
    private int followViewTime;
    private int launchedSwords = 0;
    private float searchRadius;
    private float spread;
    private float swordSpeed;
    private float swordTurnRate;


    public PortalEntity(EntityType<? extends PortalEntity> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
        setInvulnerable(true);
    }


    @Builder
    public PortalEntity(Level level, LivingEntity caster, ISpellDefinition spell, Vec3 position, int lifetime, float frequency, float followViewSpeed, int followViewTime, float searchRadius, float spread, float swordSpeed, float swordTurnRate) {
        this(AwesomeEntityTypes.PORTAL_ENTITY_TYPE.get(), level);
        casterRef.set(caster);
        spellHolder.setSpell(spell);
        setPos(position);
        entityData.set(LIFETIME, lifetime);
        this.frequency = frequency;
        this.followViewSpeed = followViewSpeed;
        this.followViewTime = followViewTime;
        this.searchRadius = searchRadius;
        this.spread = spread;
        this.swordSpeed = swordSpeed;
        this.swordTurnRate = swordTurnRate;
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    public ISpellDefinition getSpell() {
        return spellHolder.getSpell();
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            float followViewFactor = isActivated() ? (float) (tickCount - entityData.get(ACTIVATION_TIME)) / followViewTime : 1;
            var currentView = getLookAngle();
            var targetView = getCaster().getLookAngle();
            lookAt(EntityAnchorArgument.Anchor.FEET, position().add(MathUtils.rotateTowards(currentView, targetView, followViewFactor * followViewSpeed)));
            if (isActivated()) {
                if (Math.random() < (frequency * entityData.get(LIFETIME) - (float) launchedSwords) / getRemainingLifetime()) {
                    launchSword();
                }
            }
            if (getRemainingLifetime() <= 0)
                discard();
        }
        super.tick();
        if (level().isClientSide) {
            clientTick();
        }
    }

    private void launchSword() {
        var direction = level().getEntities(this, getBoundingBox().inflate(searchRadius), e -> e instanceof LivingEntity living && living.isAlive() && living != getCaster())
                .stream()
                .min(Comparator.comparing(e -> {
                    var dir = MathUtils.rotateTowards(getLookAngle(), e.getBoundingBox().getCenter().subtract(getBoundingBox().getCenter()), spread);
                    return TargetTracker.TimeCalculator.calculateTime(getBoundingBox().getCenter().toVector3f(), e.position().toVector3f(), dir.toVector3f(), swordTurnRate);
                })).map(e -> MathUtils.rotateTowards(getLookAngle(), e.getBoundingBox().getCenter().subtract(getBoundingBox().getCenter()), spread)).orElse(getLookAngle()).normalize();
        var swordVelocity = direction.scale(swordSpeed);
        var sword = new SwordEntity(level(), getCaster(), getSpell());
        sword.setPos(getBoundingBox().getCenter().subtract(0, sword.getBbHeight(), 0));
        sword.lookAt(EntityAnchorArgument.Anchor.FEET, sword.position().add(swordVelocity));
        sword.shoot(swordVelocity, swordTurnRate);
        level().addFreshEntity(sword);
        launchedSwords++;
    }

    public int getRemainingLifetime() {
        return isActivated() ? entityData.get(LIFETIME) - (tickCount - entityData.get(ACTIVATION_TIME)) : Integer.MAX_VALUE;
    }

    public boolean isActivated() {
        return entityData.get(ACTIVATION_TIME) != -1;
    }

    public void activate() {
        entityData.set(ACTIVATION_TIME, tickCount);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {

    }

    @Override
    protected void defineSynchedData() {
        spellHolder = SpellHolder.createAndDefine(SPELL, entityData, "spell");
        casterRef = Entity2EntityReference.createAndDefine(CASTER, "caster", this);
        entityData.define(ACTIVATION_TIME, -1);
        entityData.define(LIFETIME, -1);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        spellHolder.save(compound);
        casterRef.save(compound);
        compound.putInt("activationTime", entityData.get(ACTIVATION_TIME));
        compound.putInt("lifetime", entityData.get(LIFETIME));
        compound.putFloat("frequency", frequency);
        compound.putFloat("launchedSwords", launchedSwords);
        compound.putFloat("followViewSpeed", followViewSpeed);
        compound.putInt("followViewTime", followViewTime);
        compound.putFloat("searchRadius", searchRadius);
        compound.putFloat("spread", spread);
        compound.putFloat("swordSpeed", swordSpeed);
        compound.putFloat("swordTurnRate", swordTurnRate);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        spellHolder.load(compound);
        casterRef.load(compound);
        entityData.set(ACTIVATION_TIME, compound.getInt("activationTime"));
        entityData.set(LIFETIME, compound.getInt("lifetime"));
        frequency = compound.getFloat("frequency");
        launchedSwords = compound.getInt("launchedSwords");
        followViewSpeed = compound.getFloat("followViewSpeed");
        followViewTime = compound.getInt("followViewTime");
        searchRadius = compound.getFloat("searchRadius");
        spread = compound.getFloat("spread");
        swordSpeed = compound.getFloat("swordSpeed");
        swordTurnRate = compound.getFloat("swordTurnRate");
    }
} 