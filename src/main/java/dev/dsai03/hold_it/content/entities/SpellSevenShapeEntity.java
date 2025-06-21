package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpellSevenShapeEntity extends ChargeableSpellEntity {
    public static final EntityDataAccessor<CompoundTag> SPHERE = SynchedEntityData.defineId(SpellSevenShapeEntity.class, EntityDataSerializers.COMPOUND_TAG);
    Random random = new Random();

    public SpellSevenShapeEntity(EntityType<? extends SpellSevenShapeEntity> entityType, Level world) {
        super(entityType, world);
    }

    public SpellSevenShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.SEVEN_SHAPE.get(), caster, spell, world);
    }

    public static final float defaultPower = 0.7f;
    public static final float distanceToProjectile = 3;

    public static float radius() {
        return 8;
    }

    public static float chargeTime() {
        return 5;
    }

    public static float maxChargeTime() {
        return 15;
    }

    @Override
    protected boolean isCharged() {
        return getLifetime() > chargeTime();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPHERE, new CompoundTag());
    }

    public List<SphereEntity> getSphere() {
        var tag = entityData.get(SPHERE);
        var ans = new ArrayList<SphereEntity>();
        if (!tag.contains("sphere")) {
            return ans;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return ans;
        }
        for (var i : tag.getList("sphere", Tag.TAG_INT_ARRAY)) {
            Entity entity = serverLevel.getEntity(NbtUtils.loadUUID(i));
            if (entity instanceof SphereEntity sphere) {
                ans.add(sphere);
            }
        }
        if (ans.size() > 1) {
            ans.subList(1, ans.size()).clear();
        }
        return ans;
    }

    public void saveSphere(List<SphereEntity> sphere) {
        ListTag list = new ListTag();
        if (!sphere.isEmpty()) {
            list.add(NbtUtils.createUUID(sphere.get(0).getUUID()));
        }
        var tag = new CompoundTag();
        tag.put("sphere", list);
        entityData.set(SPHERE, tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("sphere", entityData.get(SPHERE));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("sphere"))
            entityData.set(SPHERE, compound.getCompound("sphere"));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide)
            return;

        if (getCaster() == null) {
            var projectile = getSphere();
            if (!projectile.isEmpty()) {
                projectile.get(0).discard();
            }
            this.discard();
            return;
        }

        var centerPos = getCaster().getEyePosition().add(getCaster().getLookAngle().scale(distanceToProjectile));
        var projectile = getSphere();
        float power = Math.min(getLifetime() / chargeTime(), 1) * defaultPower;
        SphereEntity proj;

        if (!projectile.isEmpty()) {
            proj = projectile.get(0);
            proj.power = power;
        } else {
            proj = new SphereEntity(level(), getCaster());
            proj.setOwner(getCaster());
            proj.setSpell(getSpell());
            level().addFreshEntity(proj);
            projectile = new ArrayList<>();
            projectile.add(proj);
        }

        proj.targetPosition = centerPos;
        proj.setDeltaMovement(proj.targetPosition.subtract(proj.position()));

        saveSphere(projectile);
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
        System.out.println("Overcharging: " + getLifetime());
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
        System.out.println("Charged!");
    }

    @Override
    protected List<SpellTarget> target() {
        var targets = new ArrayList<SpellTarget>();
        level().getEntities(getCaster(), getCaster().getBoundingBox().inflate(radius()), (Entity e) -> e != this && e.position().distanceTo(getCaster().position()) < radius()).stream().map(SpellTarget::new).forEach(targets::add);
        for (int i = -Mth.ceil(radius()); i <= Mth.ceil(radius()); i++) {
            for (int j = 0; j <= Mth.ceil(radius()); j++) {
                for (int k = -Mth.ceil(radius()); k <= Mth.ceil(radius()); k++) {
                    var pos = BlockPos.containing(getCaster().position().add(i, j, k));
                    if (pos.getCenter().distanceTo(getCaster().position()) > radius())
                        continue;
                    if (level().getBlockState(pos).isAir())
                        continue;
                    targets.add(new SpellTarget(pos, null));
                }
            }
        }
        return targets;
    }

    @Override
    protected void onInterrupt() {
        System.out.println("Interrupted, level.isClientSide: " + level().isClientSide);
        if (!level().isClientSide) {
            var projectile = getSphere();
            if (!projectile.isEmpty()) {
                System.out.println("Discarding sphere");
                projectile.get(0).discard();
            }
        }
    }
}