package dev.dsai03.hold_it.util;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.content.entities.ChargeableSpellEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

public class SpellUtils {
    public static void addRote(HashMap<SpellEffect, ComponentApplicationResult> results, LivingEntity caster) {
        if (caster instanceof Player player) {
            results.forEach((key, value) -> {
                if (value.is_success) {
                    SpellCaster.addComponentRoteProgress(player, key);
                }
            });
        }
    }

    public static HashMap<SpellEffect, ComponentApplicationResult> cast(ISpellDefinition spell, SpellSource source, SpellTarget target, SpellContext context) {
        var results = SpellCaster.ApplyComponents(spell, source, target, context);
        addRote(results, source.getCaster());
        return results;
    }

    public static void applyAdjusters(ISpellDefinition spell, LivingEntity caster, boolean curioCast, SpellCastStage stage) {
        SpellCaster.applyAdjusters(caster.getUseItem(), caster, caster.getUsedItemHand(), curioCast, spell, stage);
    }

    public static void cast(ISpellDefinition spell, SpellSource source, Collection<SpellTarget> targets, Function<SpellTarget, SpellContext> context, float manaCost, float spellManaCost, boolean consume) {
        Runnable[] task = new Runnable[1];
        var manaCostCounter = new float[]{manaCost};
        task[0] = () -> {
            if (manaCostCounter[0] > spellManaCost) {
                boolean success = false;
                for (var target : targets) {
                    SpellCaster.applyAdjusters(source.getCaster().getUseItem(), source.getCaster(), source.getHand(), false, spell, SpellCastStage.CASTING);
                    if (manaCostCounter[0] > spellManaCost) {
                        var results = cast(spell, source, target, context.apply(target));
                        if (results.values().stream().anyMatch(r -> r.is_success)) {
                            manaCostCounter[0] -= spellManaCost;
                            success = true;
                            if (consume)
                                consumeMana(source.getCaster(), spellManaCost);
                        }
                    } else
                        return;
                }
                if (success)
                    ServerScheduler.schedule(1, task[0]);
            }
        };
        task[0].run();
    }

    public static void consumeMana(LivingEntity caster, float mana) {
        caster.getCapability(PlayerMagicProvider.MAGIC).ifPresent(magic -> magic.getCastingResource().consume(caster, mana));
    }

    public static ChargeableSpellEntity getCastingSpellEntity(LivingEntity caster) {
        var spellEntities = caster.level().getEntitiesOfClass(ChargeableSpellEntity.class, caster.getBoundingBox().inflate(2), e -> e.getCaster() == caster);
        if (spellEntities.isEmpty())
            return null;
        return spellEntities.get(0);
    }
}
