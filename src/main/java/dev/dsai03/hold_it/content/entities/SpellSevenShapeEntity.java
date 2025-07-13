package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Random;

public class SpellSevenShapeEntity extends ChargeableSpellEntity {
    private static final Entity2EntityReference.DataAccessor SPHERE = new Entity2EntityReference.DataAccessor(ChargeableSpellEntity.class);
    private Entity2EntityReference<SphereEntity> sphereRef;
    Random random = new Random();

    public SpellSevenShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
    }

    public SpellSevenShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.SEVEN_SHAPE.get(), caster, spell, world);
    }

    public static final float defaultPower = 0.7f;
    public static final float distanceToProjectile = 3.5f;

    public float radius() {
        return Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.RADIUS);
    }

    public float magnitude() {
        return Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.MAGNITUDE);
    }

    public float delay() {
        return  Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.DELAY);
    }

    public static float chargeTime() {
        return 6;
    }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        sphereRef = Entity2EntityReference.createAndDefine(SPHERE, "sphere", this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        sphereRef.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        sphereRef.load(compound);
    }

    public float getCharge() {
        return Math.min(getLifetime() / chargeTime(), 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide)
            return;

        if (getCaster() == null || !getCaster().isUsingItem()) {
            var projectile = sphereRef.get();
            if (projectile != null) {
                projectile.setStationary();
            }
            this.discard();
            return;
        }

        var centerPos = getCaster().getEyePosition().add(getCaster().getLookAngle().scale(distanceToProjectile));
        var projectile = sphereRef.get();
        float power = Math.min(getLifetime() / chargeTime(), 1) * radius();

        if (projectile != null) {
            projectile.setPower(power);
            projectile.targetPosition = centerPos;
        } else {
            var proj = new SphereEntity(level(), this, magnitude(), radius(), delay(), radius());
            proj.setOwner(getCaster());
            proj.setSpell(getSpell());
            proj.setPos(centerPos);
            proj.setPower(power);
            level().addFreshEntity(proj);
            sphereRef.set(proj);
        }
    }

    @Override
    protected void spellTick() {
    }
    @Override
    public float getRequestedManaCost() {
        return Math.min(getCharge() * (magnitude() + radius()) * getBaseSpellManaCost(), getCasterMana());
    }

    @Override
    protected void applySpell(float manaCost, float playerMana) {
        if (!level().isClientSide) {
            var projectile = sphereRef.get();
            if (projectile != null) {
                projectile.setStationary();
            }
            this.discard();
        }
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
        if (!level().isClientSide) {
            var projectile = sphereRef.get();
            if (projectile != null) {
                projectile.discard();
            }
            this.discard();
        }
    }
}