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

public class SpellSevenShapeEntity extends ChargeableSpellEntity{
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
        if (!tag.contains("sphere"))
            return ans;
        for (var i : tag.getList("sphere", Tag.TAG_INT_ARRAY)) {
            ans.add((SphereEntity) ((ServerLevel) level()).getEntity(NbtUtils.loadUUID(i)));
        }
        return ans;
    }

    public void saveSphere(List<SphereEntity> sphere) {
        ListTag list = new ListTag();
        for (var proj : sphere) {
            list.add(NbtUtils.createUUID(proj.getUUID()));
        }
        var tag = new CompoundTag();
        tag.put("sphere", list);
        entityData.set(SPHERE, tag);
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("sphere", entityData.get(SPHERE));
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("sphere"))
            entityData.set(SPHERE, compound.getCompound("sphere"));
    }

    @Override
    protected void chargeTick() {
        commonTick();
    }

    @Override
    protected void overChargeTick() {
        if (level().isClientSide)
            clientTick();
        System.out.println("Overcharging: " + getLifetime());
    }

    void commonTick() {
        if (level().isClientSide)
            return;

        var centerPos = getCaster().getEyePosition().add(getCaster().getLookAngle().scale(distanceToProjectile));
        var projectile = getSphere();
        int maxProj = 5;
        float speed = 0.25f;
        float s = Math.min(getLifetime() / chargeTime(), 1) * defaultPower * maxProj;
        int i = 0;
        while (s > 0) {
            float p;
            if (s >= defaultPower) {
                p = defaultPower;
                s -= defaultPower;
            } else {
                p = s;
                s = 0;
            }
            SphereEntity proj;
            if (i < projectile.size()){
                proj = projectile.get(i);
                proj.power = p;
            } else {
                proj = new SphereEntity(level(), getCaster());
                proj.setOwner(getCaster());
                proj.setSpell(getSpell());
                level().addFreshEntity(proj);
            }
            i++;
        }
        if (projectile.size() == 1) {
            projectile.get(0).targetPosition = projectile.get(0).position().add(centerPos.subtract(projectile.get(0).getBoundingBox().getCenter()));
            projectile.get(0).setDeltaMovement(projectile.get(0).targetPosition.subtract(projectile.get(0).position()));
        } else {
            var radius = 0.7;
            Quaternionf r;
            if (!getCaster().getLookAngle().normalize().equals(new Vec3(0, 0.8, 0)))
                r = new Quaternionf().lookAlong((float) getCaster().getLookAngle().x, (float) getCaster().getLookAngle().y, (float) getCaster().getLookAngle().z, 0, 1, 0);
            else
                r = new Quaternionf();
            for (int j = 0; j < projectile.size(); j++) {
                var angle = j * 2 * Math.PI / projectile.size() + getLifetime();
                var pos = r.transformInverse(new Vector3d(radius * Math.sin(angle), radius * Math.cos(angle), -distanceToProjectile)).add(getCaster().getX(), getCaster().getEyeY(), getCaster().getZ());
                projectile.get(j).targetPosition = new Vec3(pos.x, pos.y + 1, pos.z);
                projectile.get(j).setDeltaMovement(projectile.get(j).targetPosition.subtract(projectile.get(j).position()));
            }
        }

        saveSphere(projectile);

    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
    }


    @Override
    protected boolean isOverCharged() {
        return getLifetime() >= maxChargeTime();
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
        System.out.println("Interrupted");
    }
}