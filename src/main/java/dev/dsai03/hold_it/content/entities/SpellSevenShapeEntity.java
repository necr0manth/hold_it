package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
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
    public static final float distanceToProjectile = 2.5f;

    public static float radius() {
        return 5;
    }

    public static float chargeTime() {
        return 7;
    }

    @Override
    protected boolean isCharged() {
        return getLifetime() > chargeTime();
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
        float power = Math.min(getLifetime() / chargeTime(), 1) * defaultPower;

        if (projectile != null) {
            projectile.setPower(power);
            projectile.targetPosition = centerPos;
        } else {
            var proj = new SphereEntity(level(), this);
            proj.setOwner(getCaster());
            proj.setSpell(getSpell());
            proj.setPos(centerPos);
            proj.setPower(power);
            level().addFreshEntity(proj);
            sphereRef.set(proj);
        }
    }

    @Override
    protected void chargeTick() {
    }

    @Override
    protected void overChargeTick() {
    }

    @Override
    protected boolean isOverCharged() {
        return false;
    }

    @Override
    protected void onCharged() {
    }

    @Override
    public float getManaCost() {
        return 0;
    }

    @Override
    protected void applySpell(float manaCost) {
    }

    @Override
    protected void onInterrupt() {
        if (!level().isClientSide) {
            var projectile = sphereRef.get();
            if (projectile != null) {
                projectile.setStationary();
            }
            this.discard();
        }
    }
}