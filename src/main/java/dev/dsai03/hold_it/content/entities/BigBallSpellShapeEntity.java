package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.Entity2EntityReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class BigBallSpellShapeEntity extends ChargeableSpellEntity {
    private static final Entity2EntityReference.DataAccessor BALL = new Entity2EntityReference.DataAccessor(BigBallSpellShapeEntity.class);
    private Entity2EntityReference<BigBallEntity> ballRef;
    Random random = new Random();


    public BigBallSpellShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
    }

    public BigBallSpellShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.BIG_BALL_SPELL_SHAPE.get(), caster, spell, world);
    }

    public static final float defaultPower = 3f;
    public static final float distanceToProjectile = 60.5f;

    public static float radius() {
        return 10f;
    }

    public float magnitude() {
        return Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.MAGNITUDE);
    }

    public static float chargeTime() {
        return 3f;
    }

    public static float maxChargeTime() {
        return 20;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        ballRef = Entity2EntityReference.createAndDefine(BALL, "big_ball", this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ballRef.save(compound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        ballRef.load(compound);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide)
            return;

        if (getCaster() == null || !getCaster().isUsingItem()) {
            var projectile = ballRef.get();
            this.discard();
            return;
        }

        var centerPos = getCaster().getEyePosition().add(getCaster().getLookAngle().scale(distanceToProjectile));
        var projectile = ballRef.get();
        float power = Math.min(getLifetime() / chargeTime(), 1) * defaultPower;

        if (projectile != null) {
            projectile.setPower(power);
        } else {
            var proj = new BigBallEntity(level(), this);
            proj.setSpell(getSpell());
            proj.setPos(centerPos);
            proj.setPower(power);
            level().addFreshEntity(proj);
            ballRef.set(proj);
        }
    }

    @Override
    protected void spellTick() {
        if (level().isClientSide) {
            clientTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
    }

    @Override
    public float getRequestedManaCost() {
        return Math.min(getCharge() * magnitude() * getBaseSpellManaCost(), getCasterMana());
    }

    private float getCharge() {
        return Math.max(getLifetime() / chargeTime(), 1);
    }


    protected List<SpellTarget> target() {
        var targets = new ArrayList<SpellTarget>();
        var sphere = ballRef.get();
        float power = Math.min(getLifetime() / chargeTime(), 1) * defaultPower;
        float radius = radius() * power;
        if (sphere == null) return targets;

        level().getEntities(getCaster(), sphere.getBoundingBox().inflate(radius),
                        (Entity e) -> e != this && e != sphere && e.position().distanceTo(sphere.position()) < radius)
                .stream().map(SpellTarget::new).forEach(targets::add);

        for (int i = -Mth.ceil(radius); i <= Mth.ceil(radius); i++) {
            for (int j = -1; j <= Mth.ceil(radius); j++) {
                for (int k = -Mth.ceil(radius); k <= Mth.ceil(radius); k++) {
                    var pos = BlockPos.containing(sphere.position().add(i, j, k));
                    if (pos.getCenter().distanceTo(sphere.position()) > radius)
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
        return targets;
    }

    public void applySpell(float manaCost, float casterMana) {
        if (level().isClientSide)
            return;
        var ball = ballRef.get();
        ball.shoot(ball.getBoundingBox().getCenter().subtract(getCaster().getEyePosition()).normalize());
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
    }
}