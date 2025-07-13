package dev.dsai03.hold_it.content.entities;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.util.SpellUtils;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
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
        return 5.0f;
    }

    public static float maxChargeTime() {
        return 15.0f;
    }

    public static float radius() {
        return 8.0f;
    }

    private void spawnPortal() {
        if (level().isClientSide) return;
        if (getCaster() == null) return;
        Vec3 offset;
        int attempts = 0;
        double minY = getCaster().getEyePosition().y - getCaster().position().y; // смещение глаз относительно позиции
        Vec3 look = getCaster().getLookAngle().normalize();
        do {
            double angle = random.nextDouble() * (2.0/3.0 * Math.PI) - (1.0/3.0 * Math.PI); // -60°..+60°
            double baseYaw = Math.atan2(look.z, look.x);
            double portalYaw = baseYaw + angle;
            double distance = 3.0 + random.nextDouble() * 4.0;
            double height = minY + random.nextDouble() * 0.5; // на уровне глаз и чуть выше
            offset = new Vec3(Math.cos(portalYaw) * distance, height, Math.sin(portalYaw) * distance);
            attempts++;
        } while ((offset.length() < 2.5 || offset.y < minY) && attempts < 10);
        Vec3 portalPos = getCaster().position().add(offset);
        
        // Проверяем, не слишком ли близко к существующим порталам
        boolean tooClose = false;
        for (PortalEntity existingPortal : portals) {
            if (existingPortal != null && existingPortal.isAlive()) {
                double distance = portalPos.distanceTo(existingPortal.position());
                if (distance < 2.0) { // минимальное расстояние между порталами
                    tooClose = true;
                    break;
                }
            }
        }
        if (tooClose) return; // не создаем портал, если он слишком близко
        
        float portalSize = 1.0f + (getLifetime() / chargeTime()) * 0.5f;
        int swordCount = Math.min(10, 3 + (int) ((getLifetime() / chargeTime()) * 4));
        PortalEntity portal = new PortalEntity(level(), getCaster(), getSpell(), portalPos, portalSize, swordCount);
        portal.lookAt(EntityAnchorArgument.Anchor.FEET, portalPos.add(getCaster().getLookAngle()));
        level().addFreshEntity(portal);
        portals.add(portal);
        level().playSound(null, portalPos.x, portalPos.y, portalPos.z, SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
        if (reason == InterruptReason.STOP_CASTING) {
            setCanLaunch(true);
        } else {
            for (PortalEntity portal : portals) {
                if (portal != null && portal.isAlive()) portal.discard();
            }
            portals.clear();
        }
    }

    public float getMaxManaCost() {
        return 1000;
    }

    @Override
    public float getRequestedManaCost() {
        return Math.min(1, getLifetime() / chargeTime()) * getMaxManaCost();
    }


    protected void applySpell(float manaCost, float casterMana) {
        SpellUtils.cast(getSpell(), new SpellSource(getCaster(), getCaster() instanceof Player player ? player.getUsedItemHand() : getCaster().swingingArm), target(), t -> new SpellContext(level(), getSpell()), manaCost, getCastingSpellManaCost(), false);
    }

    protected List<SpellTarget> target() {
        var targets = new ArrayList<SpellTarget>();
        level().getEntities(getCaster(), getCaster().getBoundingBox().inflate(radius()), (Entity e) -> e != this && e.position().distanceTo(getCaster().position()) < radius()).stream().map(SpellTarget::new).forEach(targets::add);
        for (int i = -Mth.ceil(radius()); i <= Mth.ceil(radius()); i++) {
            for (int j = -1; j <= Mth.ceil(radius()); j++) {
                for (int k = -Mth.ceil(radius()); k <= Mth.ceil(radius()); k++) {
                    var pos = BlockPos.containing(getCaster().position().add(i, j, k));
                    if (pos.getCenter().distanceTo(getCaster().position()) > radius())
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
        Collections.shuffle(targets);
        return targets;
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
        portals.removeIf(p -> p == null || !p.isAlive());
        portalSpawnDelay++;
        if (portalSpawnDelay >= PORTAL_SPAWN_INTERVAL && portals.size() < MAX_PORTALS) {
            spawnPortal();
            portalSpawnDelay = 0;
        }
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