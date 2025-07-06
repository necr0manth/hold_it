package dev.dsai03.hold_it.util;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Утилитный класс для отслеживания целей самонаводящимися снарядами
 * Работает аналогично Entity2EntityReference и SpellHolder
 */
public class TargetTracker {
    private final Entity2EntityReference<LivingEntity> targetRef;
    private final Entity owner;

    // Настройки поиска и отслеживания
    @Getter @Setter
    private double searchRadius = 10.0;
    @Getter @Setter
    private double maxTrackingDistance = 20.0;
    @Getter @Setter
    private Predicate<LivingEntity> targetFilter = this::defaultTargetFilter;

    /**
     * Приватный конструктор - используйте createAndDefine()
     */
    private TargetTracker(Entity2EntityReference<LivingEntity> targetRef, Entity owner) {
        this.targetRef = targetRef;
        this.owner = owner;
    }

    /**
     * Создает и регистрирует TargetTracker с EntityDataAccessor
     * @param dataAccessor accessor для синхронизации данных
     * @param owner entity-владелец
     */
    public static TargetTracker createAndDefine(Entity2EntityReference.DataAccessor dataAccessor, Entity owner) {
        Entity2EntityReference<LivingEntity> targetRef = Entity2EntityReference.createAndDefine(dataAccessor, "target", owner);
        return new TargetTracker(targetRef, owner);
    }

    /**
     * Устанавливает цель для отслеживания
     */
    public void setTarget(@Nullable LivingEntity target) {
        targetRef.set(target);
    }

    /**
     * Получает текущую цель
     */
    @Nullable
    public LivingEntity getTarget() {
        return targetRef.get();
    }

    /**
     * Проверяет валидность текущей цели
     */
    public boolean isTargetValid() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Проверяем расстояние
        double distance = owner.distanceTo(target);
        if (distance > maxTrackingDistance) {
            return false;
        }

        // Проверяем фильтр
        return targetFilter.test(target);
    }

    /**
     * Ищет ближайшую подходящую цель в радиусе поиска
     */
    public boolean findNearestTarget() {
        return findNearestTarget(searchRadius);
    }

    /**
     * Ищет ближайшую подходящую цель в указанном радиусе
     */
    public boolean findNearestTarget(double radius) {
        if (!owner.level().isClientSide) {
            Vec3 center = owner.position();
            AABB searchArea = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));

            List<LivingEntity> entities = owner.level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity -> entity != owner && targetFilter.test(entity)
            );

            LivingEntity nearestTarget = null;
            double nearestDistance = Double.MAX_VALUE;

            for (LivingEntity entity : entities) {
                double distance = owner.distanceTo(entity);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestTarget = entity;
                }
            }

            if (nearestTarget != null) {
                setTarget(nearestTarget);
                return true;
            }
        }
        return false;
    }

    /**
     * Ищет лучшую цель на основе комбинации факторов: расстояние, угол и скорость перехвата
     */
    public boolean findBestTarget(double radius) {
        if (!owner.level().isClientSide) {
            Vec3 center = owner.position();
            Vec3 velocity = owner.getDeltaMovement();
            Vec3 direction = velocity.length() > 0.01 ? velocity.normalize() : new Vec3(0, 0, 1);

            AABB searchArea = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));

            List<LivingEntity> entities = owner.level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity -> entity != owner && targetFilter.test(entity)
            );

            LivingEntity bestTarget = null;
            double bestScore = Double.MAX_VALUE;

            for (LivingEntity entity : entities) {
                double score = calculateTargetScore(entity, center, direction, velocity);
                if (score < bestScore) {
                    bestScore = score;
                    bestTarget = entity;
                }
            }

            if (bestTarget != null) {
                setTarget(bestTarget);
                return true;
            }
        }
        return false;
    }

    /**
     * Рассчитывает оценку цели (чем меньше, тем лучше)
     * Учитывает расстояние, угол отклонения и предсказанное время перехвата
     */
    private double calculateTargetScore(LivingEntity target, Vec3 ownerPos, Vec3 ownerDirection, Vec3 ownerVelocity) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 targetVelocity = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);

        // Расстояние до цели
        double distance = ownerPos.distanceTo(targetPos);

        // Угол между текущим направлением и направлением к цели
        Vec3 dirToTarget = targetPos.subtract(ownerPos).normalize();
        double angleDeviation = Math.acos(Math.max(-1, Math.min(1, ownerDirection.dot(dirToTarget))));

        // Предсказание позиции цели
        double timeToIntercept = predictInterceptTime(ownerPos, ownerVelocity, targetPos, targetVelocity);
        Vec3 predictedTargetPos = targetPos.add(targetVelocity.scale(timeToIntercept));
        double predictedDistance = ownerPos.distanceTo(predictedTargetPos);

        // Угол к предсказанной позиции
        Vec3 dirToPredicted = predictedTargetPos.subtract(ownerPos).normalize();
        double predictedAngleDeviation = Math.acos(Math.max(-1, Math.min(1, ownerDirection.dot(dirToPredicted))));

        // Составная оценка (комбинация факторов)
        double distanceScore = distance / maxTrackingDistance; // Нормализация по максимальной дистанции
        double angleScore = angleDeviation / Math.PI; // Нормализация по максимальному углу
        double predictedAngleScore = predictedAngleDeviation / Math.PI;

        // Веса для разных факторов
        return distanceScore * 0.3 + angleScore * 0.4 + predictedAngleScore * 0.3;
    }

    /**
     * Предсказывает время перехвата цели
     */
    private double predictInterceptTime(Vec3 ownerPos, Vec3 ownerVel, Vec3 targetPos, Vec3 targetVel) {
        Vec3 relativePos = targetPos.subtract(ownerPos);
        Vec3 relativeVel = targetVel.subtract(ownerVel);

        double a = relativeVel.dot(relativeVel);
        double b = 2 * relativePos.dot(relativeVel);
        double c = relativePos.dot(relativePos);

        if (Math.abs(a) < 1e-6) {
            return Math.abs(b) > 1e-6 ? -c / b : 0;
        }

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return 0;

        double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b + Math.sqrt(discriminant)) / (2 * a);

        return Math.max(0, Math.min(t1 > 0 ? t1 : t2, 10)); // Ограничиваем 10 секундами
    }

    /**
     * Получает предсказанную позицию цели
     */
    @Nullable
    public Vec3 getPredictedTargetPosition() {
        LivingEntity target = getTarget();
        if (target == null) {
            return null;
        }

        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 targetVelocity = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
        Vec3 ownerVelocity = owner.getDeltaMovement();

        double timeToIntercept = predictInterceptTime(owner.position(), ownerVelocity, targetPos, targetVelocity);
        return targetPos.add(targetVelocity.scale(timeToIntercept));
    }

    /**
     * Обновляет траекторию снаряда с плавным поворотом (как у ракеты)
     * @param currentVelocity текущая скорость снаряда
     * @param trackingStrength сила наведения (0.0 - 1.0)
     * @param maxTurnRate максимальная скорость поворота в радианах за тик
     * @return новая скорость с учетом наведения
     */
    public Vec3 updateTrajectorySmooth(Vec3 currentVelocity, float trackingStrength, double maxTurnRate) {
        if (!isTargetValid()) {
            return currentVelocity;
        }

        Vec3 predictedTargetPos = getPredictedTargetPosition();
        if (predictedTargetPos == null) {
            return currentVelocity;
        }

        Vec3 ownerPos = owner.position();
        Vec3 directionToTarget = predictedTargetPos.subtract(ownerPos).normalize();

        double currentSpeed = currentVelocity.length();
        if (currentSpeed < 0.01) {
            return currentVelocity;
        }

        Vec3 currentDirection = currentVelocity.normalize();

        // Вычисляем угол между текущим направлением и целью
        double dotProduct = Math.max(-1, Math.min(1, currentDirection.dot(directionToTarget)));
        double angleToTarget = Math.acos(dotProduct);

        // Ограничиваем максимальную скорость поворота
        double actualTurnRate = Math.min(angleToTarget, maxTurnRate * trackingStrength);

        if (actualTurnRate < 0.001) {
            return currentVelocity; // Уже смотрим на цель
        }

        // Вычисляем ось поворота
        Vec3 rotationAxis = currentDirection.cross(directionToTarget).normalize();
        if (rotationAxis.length() < 0.001) {
            return currentVelocity; // Векторы параллельны
        }

        // Поворачиваем текущее направление к цели
        Vec3 newDirection = rotateVector(currentDirection, rotationAxis, actualTurnRate);

        return newDirection.scale(currentSpeed);
    }

    /**
     * Поворачивает вектор вокруг оси на заданный угол
     */
    private Vec3 rotateVector(Vec3 vector, Vec3 axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = vector.dot(axis);

        Vec3 cross = axis.cross(vector);

        return vector.scale(cos)
               .add(cross.scale(sin))
               .add(axis.scale(dot * (1 - cos)));
    }

    /**
     * Фильтр целей по умолчанию - враждебные мобы и игроки
     */
    private boolean defaultTargetFilter(LivingEntity entity) {
        // Враждебные мобы
        if (entity instanceof Monster) {
            return true;
        }

        // Игроки
        if (entity instanceof Player) {
            return true;
        }

        // Мобы
        if (entity instanceof Mob) {
            return true;
        }

        return false;
    }

    /**
     * Сохраняет данные в NBT
     */
    public void save(CompoundTag compound) {
        targetRef.save(compound);
        compound.putDouble("searchRadius", searchRadius);
        compound.putDouble("maxTrackingDistance", maxTrackingDistance);
    }

    /**
     * Загружает данные из NBT
     */
    public void load(CompoundTag compound) {
        targetRef.load(compound);
        if (compound.contains("searchRadius")) {
            searchRadius = compound.getDouble("searchRadius");
        }
        if (compound.contains("maxTrackingDistance")) {
            maxTrackingDistance = compound.getDouble("maxTrackingDistance");
        }
    }

    /**
     * Очищает цель (например, при смерти или выходе из радиуса)
     */
    public void clearTarget() {
        setTarget(null);
    }
}
