package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.sorcery.SpellBook;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class AwesomeSpellShapeEntity extends ChargeableSpellEntity {
    private static final EntityDataAccessor<CompoundTag> BALLS = SynchedEntityData.defineId(AwesomeSpellShapeEntity.class, EntityDataSerializers.COMPOUND_TAG);

    public AwesomeSpellShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
    }

    public AwesomeSpellShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.AWESOME_SHAPE.get(), caster, spell, world);
    }

    @Override
    protected boolean isCharged() {
        return getLifetime() > 10;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BALLS, new CompoundTag());
    }

    public List<BallEntity> getBalls() {
        var tag = entityData.get(BALLS);

        var ans = new ArrayList<BallEntity>();
        if (!tag.contains("balls"))
            return ans;
        for (var i : tag.getList("balls", Tag.TAG_INT_ARRAY)) {
            ans.add((BallEntity) ((ServerLevel) level()).getEntity(NbtUtils.loadUUID(i)));
        }
        return ans;
    }

    public void saveBalls(List<BallEntity> balls) {
        ListTag list = new ListTag();
        for (var proj : balls) {
            list.add(NbtUtils.createUUID(proj.getUUID()));
        }
        var tag = new CompoundTag();
        tag.put("balls", list);
        entityData.set(BALLS, tag);
    }

    public static float chargeTime() {
        return 10;
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("balls", entityData.get(BALLS));
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("balls"))
            entityData.set(BALLS, compound.getCompound("balls"));
    }

    @Override
    protected void chargeTick() {
        commonTick();
    }

    public int maxBalls() {
        return 5;
    }

    public float distanceToProjectiles() {
        return 3;
    }

    public float chargedBallPower() {
        return 0.7f;
    }

    public float radius() {
        return 0.8f;
    }

    public static BallEntity.BallData getBallData(int i, float charge, float ballPower, float radius, float distanceToProjectile, Vec3 casterPosition, Vec3 casterLookAngle, float casterYRot, float time) {
        int balls = Mth.ceil(charge / ballPower);
        if (charge <= ballPower)
            return new BallEntity.BallData(casterPosition.add(casterLookAngle.scale(distanceToProjectile)), Math.min(charge, ballPower));
        else if (charge < 2 * ballPower) {
            if (i == 0)
                return new BallEntity.BallData(getBallData(0, ballPower, ballPower, radius, distanceToProjectile, casterPosition, casterLookAngle, casterYRot, time).pos().lerp(
                        getBallData(0, 2 * ballPower, ballPower, radius, distanceToProjectile, casterPosition, casterLookAngle, casterYRot, time).pos(), (charge % ballPower) / ballPower
                ), ballPower);
            else
                return new BallEntity.BallData(getBallData(1, 2 * ballPower, ballPower, Mth.lerp((charge % ballPower) / ballPower, radius / 2, radius), distanceToProjectile, casterPosition, casterLookAngle, casterYRot, time).pos(), charge % ballPower);
        }
        if (Math.abs(casterLookAngle.normalize().dot(new Vec3(0, 1, 0)) - 1) < 0.000001f)
            casterLookAngle = new Vec3(-0.000001 * Math.sin(casterYRot), 1, 0.000001 * Math.cos(casterYRot));
        Quaternionf r = new Quaternionf().lookAlong((float) casterLookAngle.x, (float) casterLookAngle.y, (float) casterLookAngle.z, 0, 1, 0);
        var angle = i * 2 * Math.PI / charge * ballPower + 2 * Math.PI * (charge / ballPower / 2) + time;
        var pos = r.transformInverse(new Vector3d(radius * Math.sin(angle), radius * Math.cos(angle), -distanceToProjectile)).add(casterPosition.x, casterPosition.y, casterPosition.z);
        return new BallEntity.BallData(new Vec3(pos.get(new Vector3f())), i == balls - 1 ? (Math.abs(charge % ballPower) < 1e-6f ? ballPower : charge % ballPower) : ballPower);
    }

    public float getCharge() {
        return Math.min(getLifetime() / chargeTime(), 1) * chargedBallPower() * maxBalls();
    }

    void commonTick() {
        if (level().isClientSide)
            return;
        var projectiles = getBalls();
        int n = Mth.ceil(getCharge() / chargedBallPower()) - projectiles.size();
        for (int i = 0; i < n; i++) {
            var proj = new BallEntity(level(), this, projectiles.size());
            proj.setSpell(getSpell());
            level().addFreshEntity(proj);
            projectiles.add(proj);
        }
        saveBalls(projectiles);
        getSpell().setManaCost(tickCount);
        if (getCaster() instanceof ServerPlayer player) {
            var hand = player.getUsedItemHand();
            var item = player.getUseItem();
            CompoundTag spellTag;
            if (item.getItem() instanceof SpellBook book)
                spellTag = book.getSpellCompound(item, player);
            else
                spellTag = item.getOrCreateTag();
            getSpell().writeToNBT(spellTag);
            player.setItemInHand(hand, item);
//            player.getInventory().setChanged();
        }
    }

    public BallEntity.BallData getBallData(int ball, Vec3 castPosition, Vec3 castVector, float castYRot, float partialTick) {
        return getBallData(ball, getCharge(), chargedBallPower(), radius(), distanceToProjectiles(), castPosition.add(0, Math.max(0, -castVector.normalize().y * 0.5f + 1), 0), castVector, castYRot, (float) tickCount / 20 + partialTick);
    }

    @Override
    protected void overChargeTick() {
        commonTick();
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
        return getCharge() / chargedBallPower() * getBaseSpellManaCost();
    }

    public void applySpell(float manaCost) {
        if (level().isClientSide)
            return;
        for (var ball : getBalls()) {
            ball.shoot(ball.getBoundingBox().getCenter().subtract(getCaster().getEyePosition()).normalize());
        }
    }

    @Override
    protected void onInterrupt() {
        applySpell();
    }
}
