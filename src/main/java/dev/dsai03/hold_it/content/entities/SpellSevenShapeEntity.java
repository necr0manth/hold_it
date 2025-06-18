package dev.dsai03.hold_it.content.entities;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import dev.dsai03.hold_it.util.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpellSevenShapeEntity extends ChargeableSpellEntity{
    Random random = new Random();
    public SpellSevenShapeEntity(EntityType<? extends SpellSevenShapeEntity> entityType, Level world) {
        super(entityType, world);
    }

    public SpellSevenShapeEntity(EntityType<? extends SpellSevenShapeEntity> entityType, LivingEntity caster, ISpellDefinition spell, Level world) {
        super(entityType, caster, spell, world);
    }

    public SpellSevenShapeEntity(LivingEntity caster, Level world, ISpellDefinition spell) {
        super(AwesomeEntityTypes.SEVEN_SHAPE.get(), caster, spell, world);
    }
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
    protected void chargeTick() {
        if (level().isClientSide)
            clientTick();
        System.out.println("Charging: " + getLifetime());
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
        return getLifetime() >= maxChargeTime();
    }

    @Override
    protected void onInterrupt() {
        System.out.println("Interrupted");
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
}