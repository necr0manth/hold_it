package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.items.sorcery.SpellBook;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AwesomeSpellShapeEntity extends ChargeableSpellEntity {
    public AwesomeSpellShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
    }

    public AwesomeSpellShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.AWESOME_SHAPE.get(), caster, spell, world);
    }

    private ListTag ballsTag = new ListTag();

    @Override
    public boolean isPrepared() {
        return getLifetime() > 0.1;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public List<BallEntity> getBalls() {
        var ans = new ArrayList<BallEntity>();
        for (var i : ballsTag) {
            var ball = (BallEntity) ((ServerLevel) level()).getEntity(NbtUtils.loadUUID(i));
            if (ball != null)
                ans.add(ball);
        }
        return ans;
    }

    public void saveBalls(List<BallEntity> balls) {
        ballsTag = new ListTag();
        for (var proj : balls) {
            ballsTag.add(NbtUtils.createUUID(proj.getUUID()));
        }
    }

    public float chargeTime() {
        return 2 * maxBalls();
    }

    public int precision() {
        return (int) Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.PRECISION);
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("balls", ballsTag);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        ballsTag = compound.getList("balls", ListTag.TAG_INT_ARRAY);
    }

    @Override
    protected void spellTick() {
        if (level().isClientSide)
            return;
        var projectiles = getBalls();
        int currentBallCount = projectiles.size();
        int targetBallCount = Mth.ceil(getCharge() / chargedBallPower());
        int newBallsNeeded = targetBallCount - currentBallCount;

        // Получаем доступную ману
        float currentMana = getCasterMana();
        float singleBallManaCost = getCastingSpellManaCost();

        // Сколько полных шаров можем создать с текущей маной
        int fullAffordableBalls = currentMana > 0 ? Mth.floor(currentMana / singleBallManaCost) : 0;

        // Если у нас есть мана на частичный шар (но не на полный), создаем его тоже
        float remainingMana = currentMana - (fullAffordableBalls * singleBallManaCost);
        boolean canCreatePartialBall = remainingMana > 0 && remainingMana < singleBallManaCost;

        int totalAffordableBalls = fullAffordableBalls + (canCreatePartialBall ? 1 : 0);

        // Сколько новых шаров можем создать
        int ballsToCreate = Math.min(newBallsNeeded, Math.max(0, totalAffordableBalls - currentBallCount));

        for (int i = 0; i < ballsToCreate; i++) {
            // Проверяем precision заклинания для включения самонаведения
            boolean enableHoming = precision() >= 2;
            var proj = new BallEntity(level(), this, projectiles.size(), enableHoming);
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

    public int maxBalls() {
        return (int) Objects.requireNonNull(getSpell().getShape()).getValue(Attribute.MAGNITUDE);
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

    public static BallData getBallData(int i, float charge, float ballPower, float radius, float distanceToProjectile, Vec3 casterPosition, Vec3 casterLookAngle, float casterYRot, float time) {
        int balls = Mth.ceil(charge / ballPower);
        if (charge <= ballPower)
            return new BallData(casterPosition.add(casterLookAngle.scale(distanceToProjectile)), Math.min(charge, ballPower));
        else if (charge < 2 * ballPower) {
            if (i == 0)
                return new BallData(getBallData(0, ballPower, ballPower, radius, distanceToProjectile, casterPosition, casterLookAngle, casterYRot, time).pos().lerp(
                        getBallData(0, 2 * ballPower, ballPower, radius, distanceToProjectile, casterPosition, casterLookAngle, casterYRot, time).pos(), (charge % ballPower) / ballPower
                ), ballPower);
            else
                return new BallData(getBallData(1, 2 * ballPower, ballPower, Mth.lerp((charge % ballPower) / ballPower, radius / 2, radius), distanceToProjectile, casterPosition, casterLookAngle, casterYRot, time).pos(), charge % ballPower);
        }
        if (Math.abs(casterLookAngle.normalize().dot(new Vec3(0, 1, 0)) - 1) < 0.000001f)
            casterLookAngle = new Vec3(-0.000001 * Math.sin(casterYRot), 1, 0.000001 * Math.cos(casterYRot));
        Quaternionf r = new Quaternionf().lookAlong((float) casterLookAngle.x, (float) casterLookAngle.y, (float) casterLookAngle.z, 0, 1, 0);
        var angle = i * 2 * Math.PI / charge * ballPower + 2 * Math.PI * (charge / ballPower / 2) + time;
        var pos = r.transformInverse(new Vector3d(radius * Math.sin(angle), radius * Math.cos(angle), -distanceToProjectile)).add(casterPosition.x, casterPosition.y, casterPosition.z);
        return new BallData(new Vec3(pos.get(new Vector3f())), i == balls - 1 ? (Math.abs(charge % ballPower) < 1e-6f ? ballPower : charge % ballPower) : ballPower);
    }

    /**
     * Возвращает эффективный заряд заклинания с учетом доступной маны
     * Ограничивает максимальное количество шаров тем, что может быть создано с текущим запасом маны
     */
    public float getEffectiveCharge() {
        float basicCharge = getCharge();
        int theoreticalBalls = Mth.ceil(basicCharge / chargedBallPower());

        // Сколько шаров можем создать с текущей маной
        float currentMana = getCasterMana();
        float singleBallManaCost = getCastingSpellManaCost();

        if (currentMana <= 0) {
            return 0;
        }

        // Сколько полных шаров можем создать
        int fullAffordableBalls = Mth.floor(currentMana / singleBallManaCost);

        // Оставшаяся мана после полных шаров
        float remainingMana = currentMana - (fullAffordableBalls * singleBallManaCost);

        // Если есть остаточная мана, добавляем заряд для частичного шара
        float partialBallCharge = 0;
        if (remainingMana > 0 && fullAffordableBalls < theoreticalBalls) {
            // Частичный заряд пропорционален доступной мане
            partialBallCharge = (remainingMana / singleBallManaCost) * chargedBallPower();
        }

        // Общий эффективный заряд = полные шары + частичный шар
        float effectiveCharge = fullAffordableBalls * chargedBallPower() + partialBallCharge;

        // Не превышаем базовый заряд и не превышаем теоретически возможное количество шаров
        return Math.min(basicCharge, effectiveCharge);
    }

    public float getCharge() {
        return Math.min(getLifetime() / chargeTime(), 1) * chargedBallPower() * maxBalls();
    }

    public BallData getBallData(int ball, Vec3 castPosition, Vec3 castVector, float castYRot, float partialTick) {
        return getBallData(ball, getEffectiveCharge(), chargedBallPower(), radius(), distanceToProjectiles(), castPosition.add(0, Math.max(0, -castVector.normalize().y * 0.5f + 1), 0), castVector, castYRot, tickCount / 20f + partialTick);
    }

    @Override
    public float getRequestedManaCost() {
        return Math.min(getCharge() / chargedBallPower() * getCastingSpellManaCost(), getCasterMana());
    }

    @Override
    protected void applySpell(float requestedManaCost, float casterMana) {
        if (level().isClientSide)
            return;
        var balls = getBalls();
        for (var ball : balls) {
            ball.shoot(precision() == 0 || balls.size() == 1 ? ball.getBoundingBox().getCenter().subtract(getCaster().getEyePosition()).normalize() : getBallData(0, 1, 1, 1, distanceToProjectiles(), getCaster().getEyePosition().add(0, Math.max(0, -getCaster().getLookAngle().normalize().y * 0.5f + 1), 0), getCaster().getLookAngle(), getCaster().yBodyRot, 0).pos().subtract(getCaster().getEyePosition()));
        }
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
        for (var ball : getBalls())
            if (ball != null)
                ball.discard();
    }
}
