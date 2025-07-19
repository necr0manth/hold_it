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

    @Override
    protected void spellTick() {
        if (ballRef.get() == null) {
            var ball = new BigBallEntity(level(), this);
            level().addFreshEntity(ball);
            ballRef.set(ball);
        }
    }

    public BigBallSpellShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.BIG_BALL_SPELL_SHAPE.get(), caster, spell, world);
    }

    public float chargeTime() {
        return radius();
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

    public BallData getBallData(float partialTick) {
        var power = Math.min(getLifetime() / chargeTime(), 1) * radius();
        return new BallData(getCaster().getEyePosition(partialTick).add(getCaster().getViewVector(partialTick).scale(power * 1.5)), power);
    }


    @Override
    public float getRequestedManaCost() {
        return Math.min(getCharge() * magnitude() * getBaseSpellManaCost(), getCasterMana());
    }

    private float getCharge() {
        return Math.min(getLifetime() / chargeTime(), 1);
    }

    public float magnitude() {
        return getSpell().getShape().getValue(Attribute.MAGNITUDE);
    }

    public float radius() {
        return getSpell().getShape().getValue(Attribute.RADIUS);
    }

    public void applySpell(float manaCost, float casterMana) {
        if (level().isClientSide)
            return;
        var ball = ballRef.get();
        ball.shoot(ball.getBoundingBox().getCenter().subtract(getCaster().getEyePosition()).normalize());
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
        var ball = ballRef.get();
        if (ball != null)
            ball.discard();
    }
}