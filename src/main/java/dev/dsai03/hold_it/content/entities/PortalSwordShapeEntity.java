package dev.dsai03.hold_it.content.entities;

import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.util.AffinityDistribution;
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

    public PortalSwordShapeEntity(EntityType<? extends ChargeableSpellEntity> entityType, Level world) {
        super(entityType, world);
    }

    public PortalSwordShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.PORTAL_SWORD_SHAPE.get(), caster, spell, world);
    }

    private void spawnPortal() {
        if (level().isClientSide) return;
        if (getCaster() == null) return;
        Vec3 offset;
        int attempts = 0;
        double minY = getCaster().getEyePosition().y - getCaster().position().y;
        Vec3 look = getCaster().getLookAngle().normalize();
        do {
            double angle = random.nextDouble() * (2.0 / 3.0 * Math.PI) - (1.0 / 3.0 * Math.PI); // -60°..+60°
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
                if (distance < 2.0) {
                    tooClose = true;
                    break;
                }
            }
        }
        if (tooClose) return;

        PortalEntity portal = new PortalEntity(level(), getCaster(), getSpell(), portalPos, (int) ((duration() + Math.random()) * 20), speed() / 20, 0.04f, (int) (20 * duration() * 0.1f), 20, 0.5f, 0.5f, 0.06f);
        portal.lookAt(EntityAnchorArgument.Anchor.FEET, portalPos.add(getCaster().getLookAngle()));
        level().addFreshEntity(portal);
        portals.add(portal);
        level().playSound(null, portalPos.x, portalPos.y, portalPos.z, SFX.Spell.Cast.ForAffinity(AffinityDistribution.fromSpell(getSpell()).getMaxAffinity()), SoundSource.PLAYERS, 0.4f, 1.0F);
    }

    @Override
    protected void onInterrupt(InterruptReason reason) {
        for (PortalEntity portal : portals) {
            if (portal != null && portal.isAlive()) portal.discard();
        }
        discard();
    }

    @Override
    public float getRequestedManaCost() {
        return (float) (0.5 * Math.min(magnitude(), getLifetime() / portalSpawnDelay()) * speed() * Math.sqrt(duration()) * getBaseSpellManaCost());
    }

    protected void applySpell(float manaCost, float casterMana) {
        activatePortals();
    }

    @Override
    protected void spellTick() {
        if (level().isClientSide) return;
        int targetPortalCount = (int) (getManaCost() / speed() / Math.sqrt(duration()) / getBaseSpellManaCost() / 0.5f + 0.0001f);
        if (targetPortalCount > portals.size()) {
            spawnPortal();
        }
    }

    public void activatePortals() {
        for (PortalEntity portal : portals) {
            portal.activate();
        }
    }

    public float portalSpawnDelay() {
        return 0.5f;
    }

    public int magnitude() {
        return (int) getSpell().getShape().getValue(Attribute.MAGNITUDE);
    }

    public float speed() {
        return getSpell().getShape().getValue(Attribute.SPEED);
    }

    public float duration() {
        return getSpell().getShape().getValue(Attribute.DURATION);
    }

    @Override
    public boolean allowCastWhenNotEnoughMana() {
        return true;
    }
}