package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.AffinityDistribution;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import dev.dsai03.hold_it.util.SpellHolder;
import dev.dsai03.hold_it.util.SpellUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class SwordEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(SwordEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> THROWN = SynchedEntityData.defineId(SwordEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Entity2EntityReference.DataAccessor CASTER = new Entity2EntityReference.DataAccessor(SwordEntity.class);
    private static final EntityDataAccessor<CompoundTag> SPELL = SpellHolder.createDataAccessor(SwordEntity.class);
    private Entity2EntityReference<LivingEntity> casterRef;
    private float lastPower = 1;
    private SpellHolder spellHolder;
    private Random random = new Random();
    private UUID targetUUID;
    private static final double HOMING_STRENGTH = 0.15;
    private static final int HOMING_DELAY = 5;

    public SwordEntity(EntityType<? extends SwordEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public SwordEntity(Level level, PortalEntity portal) {
        this(AwesomeEntityTypes.SWORD_ENTITY_TYPE.get(), level);
        setOwner(portal);
        casterRef.set(portal.getCaster());
        spellHolder.setSpell(portal.getSpell());
        setPos(portal.position());
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    @Override
    public void tick() {
        super.tick();
        
        if (level().isClientSide) {
            clientTick();
            return;
        }

        if (!entityData.get(THROWN)) return;
        if (targetUUID != null && tickCount > HOMING_DELAY) {
            LivingEntity target = null;
            for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16))) {
                if (entity.getUUID().equals(targetUUID)) {
                    if (entity == getCaster()) continue;
                    target = entity;
                    break;
                }
            }
            if (target != null && target.isAlive()) {
                Vec3 toTarget = target.getBoundingBox().getCenter().subtract(position()).normalize();
                Vec3 newMotion = getDeltaMovement().add(toTarget.scale(HOMING_STRENGTH)).normalize().scale(0.8);
                setDeltaMovement(newMotion);
            }
        }
        if (getDeltaMovement().length() < 0.01) {
            onHit(position());
        }

        if (entityData.get(POWER) != lastPower) {
            lastPower = entityData.get(POWER);
            refreshDimensions();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        // Создаем частицы следа меча
        if (tickCount % 2 == 0) {
            Affinity affinity = AffinityDistribution.fromSpell(spellHolder.getSpell()).getRandomAffinity();
            Vec3 trailPos = position().add(
                (random.nextDouble() - 0.5) * 0.2,
                (random.nextDouble() - 0.5) * 0.2,
                (random.nextDouble() - 0.5) * 0.2
            );
            
            ParticleUtils.addParticle(
                spellHolder.getSpell().colorParticle(new MAParticleType(ParticleUtils.getParticleType(affinity)), getCaster()),
                trailPos,
                Vec3.ZERO,
                ParticleUtils.EMPTY_TICKER,
                ParticleUtils.relativeTo(() -> position(), ParticleUtils.EMPTY_TICKER)
            );
        }
    }

    public void setPower(float power) {
        entityData.set(POWER, power);
    }

    public float getPower() {
        return entityData.get(POWER);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.25f, 0.25f);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(POWER, 1.0f);
        entityData.define(THROWN, false);
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
        compound.putFloat("power", entityData.get(POWER));
        compound.putBoolean("thrown", entityData.get(THROWN));
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
        onHit(hitResult.getLocation());
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (level().isClientSide) return;
        if (!entityData.get(THROWN)) return;
        if (getCaster() == null) {
            discard();
            return;
        }
        Entity hit = hitResult.getEntity();
        if (hit instanceof LivingEntity living) {
            SpellSource source = new SpellSource(getCaster(), InteractionHand.MAIN_HAND);
            SpellContext context = new SpellContext(level(), spellHolder.getSpell());
            float power = getPower() * 1.5f;
            SpellUtils.cast(spellHolder.getSpell(), source, new SpellTarget(living), context);
        }
        discard();
    }

    private void onHit(Vec3 location) {
        if (level().isClientSide) return;
        if (getCaster() == null) {
            discard();
            return;
        }
        if (!entityData.get(THROWN)) return;

        var targets = new ArrayList<SpellTarget>();
        var power = entityData.get(POWER);
        
        // Поиск целей в радиусе

        // Поиск блоков в радиусе
        for (int i = -Mth.ceil(power); i <= Mth.ceil(power); i++) {
            for (int j = -1; j <= Mth.ceil(power); j++) {
                for (int k = -Mth.ceil(power); k <= Mth.ceil(power); k++) {
                    var pos = BlockPos.containing(location.add(i, j, k));
                    if (pos.getCenter().distanceTo(location) > power) continue;
                    if (level().getBlockState(pos).isAir()) continue;
                    if (j == -1)
                        targets.add(new SpellTarget(pos, Direction.UP));
                    else
                        targets.add(new SpellTarget(pos, null));
                }
            }
        }

        for (var target : targets) {
            SpellSource source = new SpellSource(getCaster(), InteractionHand.MAIN_HAND);
            SpellContext context = new SpellContext(level(), spellHolder.getSpell());
            SpellUtils.cast(spellHolder.getSpell(), source, target, context);
        }
        
        discard();
    }

    public void shoot(Vec3 direction) {
        entityData.set(THROWN, true);
        setDeltaMovement(direction.normalize().scale(0.7)); // скорость, как хочешь
        setYRot((float)(Mth.atan2(direction.z, direction.x) * (180F / Math.PI)) - 90F);
        setXRot((float)(-Mth.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)) * (180F / Math.PI)));
    }

    public ItemStack getItem() {
        return new ItemStack(Items.IRON_SWORD);
    }

    public void setTarget(LivingEntity target) {
        this.targetUUID = target.getUUID();
    }
} 