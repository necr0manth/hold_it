package dev.dsai03.hold_it.util;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.SpellCaster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;

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

    public static void cast(ISpellDefinition spell, SpellSource source, SpellTarget target, SpellContext context) {
        addRote(SpellCaster.ApplyComponents(spell, source, target, context), source.getCaster());
    }
}
