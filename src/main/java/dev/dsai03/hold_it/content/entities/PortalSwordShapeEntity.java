package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PortalSwordShapeEntity extends ChargeableSpellEntity {
    private Random random = new Random();
    private List<PortalEntity> portals = new ArrayList<>();
    private int portalSpawnDelay = 0;
    private static final int PORTAL_SPAWN_INTERVAL = 30; // 1.5 секунды между порталами

    public PortalSwordShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
    }

    public PortalSwordShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.PORTAL_SWORD_SHAPE.get(), caster, spell, world);
    }

    public static float chargeTime() {
        return 5.0f; // 5 секунд зарядки
    }

    public static float maxChargeTime() {
        return 15.0f; // Максимум 15 секунд зарядки
    }

    @Override
    protected boolean isCharged() {
        return getLifetime() > chargeTime();
    }

    @Override
    protected boolean isOverCharged() {
        return getLifetime() > maxChargeTime();
    }

    @Override
    protected void chargeTick() {
        // Создаем порталы во время зарядки
        portalSpawnDelay++;
        if (portalSpawnDelay >= PORTAL_SPAWN_INTERVAL) {
            spawnPortal();
            portalSpawnDelay = 0;
        }
    }

    @Override
    protected void overChargeTick() {
        chargeTick();
    }

    private void spawnPortal() {
        if (level().isClientSide) return;

        // Находим случайную позицию вокруг кастера
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = 3.0 + random.nextDouble() * 4.0; // От 3 до 7 блоков от кастера
        double height = -1.0 + random.nextDouble() * 3.0; // От -1 до 2 блоков по высоте

        Vec3 portalPos = getCaster().position().add(
            Math.cos(angle) * distance,
            height,
            Math.sin(angle) * distance
        );

        // Размер портала зависит от времени зарядки
        float portalSize = 1.0f + (getLifetime() / chargeTime()) * 0.5f;
        
        // Количество мечей зависит от времени зарядки
        int swordCount = 3 + (int)((getLifetime() / chargeTime()) * 4);

        PortalEntity portal = new PortalEntity(level(), getCaster(), getSpell(), portalPos, portalSize, swordCount);
        level().addFreshEntity(portal);
        portals.add(portal);
    }

    @Override
    protected void onCharged() {
        // При завершении зарядки создаем финальный портал
        if (!level().isClientSide) {
            Vec3 finalPortalPos = getCaster().position().add(0, 2, 0);
            float finalPortalSize = 2.0f;
            int finalSwordCount = 8;
            
            PortalEntity finalPortal = new PortalEntity(level(), getCaster(), getSpell(), finalPortalPos, finalPortalSize, finalSwordCount);
            level().addFreshEntity(finalPortal);
            portals.add(finalPortal);
        }
    }

    @Override
    protected void onInterrupt() {
        // Удаляем все порталы при прерывании
        if (!level().isClientSide) {
            for (PortalEntity portal : portals) {
                if (portal != null && portal.isAlive()) {
                    portal.discard();
                }
            }
            portals.clear();
        }
    }

    @Override
    public float getManaCost() {
        return Math.min(1, getLifetime() / chargeTime()) * 500; // Максимум 500 маны
    }

    @Override
    protected void applySpell(float manaCost) {
        // Заклинание применяется через порталы и мечи
    }

} 