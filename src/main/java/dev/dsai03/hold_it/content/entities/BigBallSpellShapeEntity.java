package dev.dsai03.hold_it.content.entities;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BigBallSpellShapeEntity extends ChargeableSpellEntity {
    private Entity2EntityReference<SphereEntity> sphereRef;
    Random random = new Random();
    private static final EntityDataAccessor<CompoundTag> BALL = SynchedEntityData.defineId(BigBallSpellShapeEntity.class, EntityDataSerializers.COMPOUND_TAG);


    public BigBallSpellShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
    }

    public BigBallSpellShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.BIG_BALL_SPELL_SHAPE.get(), caster, spell, world);
    }

    public static final float defaultPower = 0.7f;
    public static final float distanceToProjectile = 2.5f;

    public static float radius() {
        return 5;
    }

    public static float chargeTime() {
        return 7;
    }

    public static float maxChargeTime() {
        return 30;
    }

    @Override
    protected boolean isCharged() {
        return getLifetime() > chargeTime();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        sphereRef = Entity2EntityReference.createAndDefine("big_ball", this, BigBallSpellShapeEntity.class);
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
            this.discard();
            return;
        }

        var centerPos = getCaster().getEyePosition().add(getCaster().getLookAngle().scale(distanceToProjectile));
        var projectile = sphereRef.get();
        float power = Math.min(getLifetime() / chargeTime(), 1) * defaultPower;

        if (projectile != null) {
            projectile.setPower(power);
        } else {
            var proj = new BigBallEntity(level(), this);
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
        if (level().isClientSide) {
            clientTick();
        }
    }

    @Override
    protected void overChargeTick() {
        if (level().isClientSide)
            clientTick();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
    }

    @Override
    protected boolean isOverCharged() {
        return getLifetime() > maxChargeTime();
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

    protected List<SpellTarget> target() {
        var targets = new ArrayList<SpellTarget>();
        var sphere = sphereRef.get();
        if (sphere == null) return targets;

        level().getEntities(getCaster(), sphere.getBoundingBox().inflate(radius()),
                        (Entity e) -> e != this && e != sphere && e.position().distanceTo(sphere.position()) < radius())
                .stream().map(SpellTarget::new).forEach(targets::add);

        for (int i = -Mth.ceil(radius()); i <= Mth.ceil(radius()); i++) {
            for (int j = -1; j <= Mth.ceil(radius()); j++) {
                for (int k = -Mth.ceil(radius()); k <= Mth.ceil(radius()); k++) {
                    var pos = BlockPos.containing(sphere.position().add(i, j, k));
                    if (pos.getCenter().distanceTo(sphere.position()) > radius())
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
    public void applySpell() {
        if (level().isClientSide)
            return;
        for (var ball : getBalls()) {
            ball.shoot(ball.position().subtract(getCaster().getEyePosition()).normalize());
        }
    }
    public List<BallEntity> getBalls() {
        var tag = entityData.get(BALL);

        var ans = new ArrayList<BallEntity>();
        if (!tag.contains("ball"))
            return ans;
        for (var i : tag.getList("ball", Tag.TAG_INT_ARRAY)) {
            ans.add((BallEntity) ((ServerLevel) level()).getEntity(NbtUtils.loadUUID(i)));
        }
        return ans;
    }

    @Override
    protected void onInterrupt() {
        if (!level().isClientSide) {
            var projectile = sphereRef.get();
            this.discard();
        }
    }
}