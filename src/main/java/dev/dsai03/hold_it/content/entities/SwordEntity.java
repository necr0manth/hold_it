package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.tools.math.MathUtils;
import dev.dsai03.hold_it.content.client.particles.ParticleUtils;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
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
    private static final TargetTracker.DataAccessor TARGET_TRACKER = new TargetTracker.DataAccessor(SwordEntity.class);
    private Entity2EntityReference<LivingEntity> casterRef;
    private SpellHolder spellHolder;
    private Random random = new Random();
    private TargetTracker targetTracker;

    public SwordEntity(EntityType<? extends SwordEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
        setInvulnerable(true);
    }

    public SwordEntity(Level level, LivingEntity caster, ISpellDefinition spell) {
        this(AwesomeEntityTypes.SWORD_ENTITY_TYPE.get(), level);
        casterRef.set(caster);
        spellHolder.setSpell(spell);
    }

    public LivingEntity getCaster() {
        return casterRef.get();
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (entityData.get(THROWN)) {
                targetTracker.tick();
            }
        }
        super.tick();
        if (!getDeltaMovement().equals(Vec3.ZERO))
            lookAt(EntityAnchorArgument.Anchor.FEET, getDeltaMovement().add(position()));


        if (level().isClientSide)
            clientTick();

        if (tickCount >= maxLifetime())
            discard();
    }

    @Override
    protected float getEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return pDimensions.height / 2;
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (spellHolder.getSpell() == null)
            return;
        for (int i = 0; i < 10 * getAlphaPercentage(); i++) {
            Affinity affinity = AffinityDistribution.fromSpell(spellHolder.getSpell()).getRandomAffinity();
            ParticleUtils.addParticle(
                    spellHolder.getSpell().colorParticle(ParticleUtils.configureParticleAffinity(new MAParticleType(ParticleUtils.getParticleType(affinity)), affinity), getCaster()),
                    getBoundingBox().getCenter().add(RandomUtils.randomVectorFromBall().scale(getBbWidth() / 2)),
                    RandomUtils.randomVectorFromBall().scale(0.03),
                    ParticleUtils.EMPTY_TICKER,
                    ParticleUtils.fadeOut(0.5f).asConsumerTicker()
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
    protected void defineSynchedData() {
        entityData.define(POWER, 1.0f);
        entityData.define(THROWN, false);
        spellHolder = SpellHolder.createAndDefine(SPELL, entityData, "spell");
        casterRef = Entity2EntityReference.createAndDefine(CASTER, "caster", this);
        targetTracker = TargetTracker.createAndDefine(TARGET_TRACKER, "targetTracking", e -> e != getCaster() && e instanceof LivingEntity, this);
    }

    public void setSpell(ISpellDefinition spell) {
        spellHolder.setSpell(spell);
    }

    public ISpellDefinition getSpell() {
        return spellHolder.getSpell();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("power", entityData.get(POWER));
        compound.putBoolean("thrown", entityData.get(THROWN));
        spellHolder.save(compound);
        casterRef.save(compound);
        targetTracker.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(POWER, pCompound.getFloat("power"));
        entityData.set(THROWN, pCompound.getBoolean("thrown"));
        spellHolder.load(pCompound);
        casterRef.load(pCompound);
        targetTracker.load(pCompound);
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
            float baseDamage = (0.05f) * getPower(); // ещё меньше урон
            living.hurt(level().damageSources().magic(), baseDamage);
            SpellSource source = new SpellSource(getCaster(), InteractionHand.MAIN_HAND);
            SpellContext context = new SpellContext(level(), spellHolder.getSpell());
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

    public void shoot(Vec3 veclocity, float turnRate) {
        entityData.set(THROWN, true);
        targetTracker.setTurnRate(turnRate);
        setDeltaMovement(veclocity);
    }

    public ItemStack getItem() {
        return new ItemStack(Items.IRON_SWORD);
    }

    public int maxLifetime() {
        return 100;
    }

    public float fadeOutTime() {
        return 30;
    }

    public float getAlphaPercentage() {
        return MathUtils.clamp01((maxLifetime() - tickCount) / fadeOutTime());
    }
}