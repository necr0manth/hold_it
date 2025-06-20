package dev.dsai03.hold_it.util;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellDefinition;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import java.util.Random;
import java.util.function.Supplier;

public class LazySpellHolder {
    private final Supplier<ISpellDefinition> spell;
    private ISpellDefinition cachedSpell;

    public LazySpellHolder(Supplier<ISpellDefinition> spell) {
        this.spell = spell;
    }

    public ISpellDefinition getSpell() {
        if (cachedSpell == null) {
            cachedSpell = spell.get();
        }
        return cachedSpell;
    }

    private static final NonNullSupplier<RuntimeException> exception = () -> new RuntimeException("0_o");

    private float[] affinities;
    private float[] normalizedAffinities;
    private final Random random = new Random();

    public float[] getAffinities() {
        if (affinities == null) {
            var spell = getSpell();
            if (spell == null) {
                return null;
            }
            var aff = spell.getAffinity();
            affinities = new float[Affinity.values().length];
            for (var affinity : Affinity.values()) {
                if (aff.containsKey(affinity))
                    affinities[affinity.ordinal()] = aff.get(affinity);
            }
        }
        return affinities;
    }

    public float[] getNormalizedAffinities() {
        if (normalizedAffinities == null) {
            var aff = getAffinities();
            if (aff == null)
                return null;
            float s = 0;
            for (var affinity : Affinity.values()) {
                s += aff[affinity.ordinal()];
            }
            normalizedAffinities = new float[Affinity.values().length];
            for (var affinity : Affinity.values()) {
                normalizedAffinities[affinity.ordinal()] = aff[affinity.ordinal()] / s;
            }
        }
        return normalizedAffinities;
    }

    public Affinity getRandomAffinity() {
        if (getNormalizedAffinities() == null)
            return null;
        float s = 1;
        for (var i = 0; i < Affinity.values().length; i++) {
            var j = getNormalizedAffinities()[i];
            if (random.nextFloat() < j / s)
                return Affinity.values()[i];
            s -= j;
        }
        return null;
    }
}
