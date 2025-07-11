package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PortalSwordShapeEntity extends ChargeableSpellEntity {
    private Random random = new Random();
    private List<PortalEntity> portals = new ArrayList<>();
    private int portalSpawnDelay = 0;
    private static final int PORTAL_SPAWN_INTERVAL = 30; // 1.5 секунды между порталами
    private static final int MAX_PORTALS = 4;

    public PortalSwordShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
        System.out.println("[DEBUG] PortalSwordShapeEntity создан через EntityType!");
    }

    public PortalSwordShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.PORTAL_SWORD_SHAPE.get(), caster, spell, world);
        System.out.println("[DEBUG] PortalSwordShapeEntity создан через кастера!");
    }

    public static float chargeTime() {
        return 5.0f; // 5 секунд зарядки
    }

    public static float maxChargeTime() {
        return 15.0f; // Максимум 15 секунд зарядки
    }

    private void spawnPortal() {
        if (level().isClientSide) return;
        if (getCaster() == null) return;
        Vec3 offset;
        int attempts = 0;
        do {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 3.0 + random.nextDouble() * 4.0;
            double height = -1.0 + random.nextDouble() * 3.0;
            offset = new Vec3(Math.cos(angle) * distance, height, Math.sin(angle) * distance);
            attempts++;
        } while (offset.length() < 2.5 && attempts < 10);
        Vec3 portalPos = getCaster().position().add(offset);
        float portalSize = 1.0f + (getLifetime() / chargeTime()) * 0.5f;
        int swordCount = Math.min(10, 3 + (int) ((getLifetime() / chargeTime()) * 4));
        PortalEntity portal = new PortalEntity(level(), getCaster(), getSpell(), portalPos, portalSize, swordCount);
        level().addFreshEntity(portal);
        portals.add(portal);
        level().playSound(null, portalPos.x, portalPos.y, portalPos.z, SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
        for (PortalEntity portal : portals) {
            if (portal != null && portal.isAlive()) portal.discard();
        }
        portals.clear();
    }

    @Override
    public float getManaCost() {
        return Math.min(1, getLifetime() / chargeTime()) * 500; // Максимум 500 маны
    }

    @Override
    protected void applySpell(float requestedManaCost, float casterMana) {

    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount % 20 == 0) {
            System.out.println("[DEBUG] lifetime=" + getLifetime());
        }
    }

    @Override
    protected void spellTick() {
        if (getLifetime() >= maxChargeTime()) return;
        if (getLifetime() >= chargeTime())
            setCanLaunch(true);
        portalSpawnDelay++;
        if (portalSpawnDelay >= PORTAL_SPAWN_INTERVAL && portals.size() < MAX_PORTALS) {
            spawnPortal();
            portalSpawnDelay = 0;
        }
    }

    @Override
    public float getRequestedManaCost() {
        return 0;
    }

    public void setCanLaunch(boolean value) {
        for (PortalEntity portal : portals) {
            if (portal != null && portal.isAlive()) portal.canLaunch = value;
        }
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        setCanLaunch(true);
    }
} 