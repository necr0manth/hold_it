package dev.dsai03.hold_it.util;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellDefinition;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

public class LazySpellHolder {
    private final Supplier<ISpellDefinition> spell;
    private ISpellDefinition cachedSpell;
    private final Object lock = new Object();

    public LazySpellHolder(Supplier<ISpellDefinition> spell) {
        this.spell = spell;
    }

    public ISpellDefinition getSpell() {
        synchronized (lock) {
            if (cachedSpell == null)
                cachedSpell = spell.get();
            return cachedSpell;
        }
    }

    private float[] affinities;
    private float[] normalizedAffinities;
    private final Random random = new Random();

    public float[] getAffinities() {
        synchronized (lock) {
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
            return Arrays.copyOf(affinities, affinities.length);
        }
    }

    public float[] getNormalizedAffinities() {
        synchronized (lock) {
            if (normalizedAffinities == null) {
                var aff = getAffinities();
                if (aff == null) {
                    return null;
                }
                float s = 0;
                for (var affinity : Affinity.values()) {
                    s += aff[affinity.ordinal()];
                }
                normalizedAffinities = new float[Affinity.values().length];
                for (var affinity : Affinity.values()) {
                    normalizedAffinities[affinity.ordinal()] = aff[affinity.ordinal()] / s;
                }
            }
            return Arrays.copyOf(normalizedAffinities, normalizedAffinities.length);
        }
    }

    public float[] getNormalizedAffinitiesExcept(Affinity... affinities) {
        var aff = getAffinities();
        if (aff == null)
            return null;
        float s = 0;
        for (var affinity : Affinity.values()) {
            for (var a : affinities)
                if (a == affinity) {
                    aff[affinity.ordinal()] = 0;
                    break;
                }
            s += aff[affinity.ordinal()];
        }
        var ans = new float[Affinity.values().length];
        if (s == 0)
            return ans;
        for (var affinity : Affinity.values()) {
            ans[affinity.ordinal()] = aff[affinity.ordinal()] / s;
        }
        return ans;
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

    public Affinity getRandomAffinityExcept(Affinity... affinities) {
        var a = getNormalizedAffinitiesExcept(affinities);
        if (a == null)
            return null;
        float s = 1;
        for (var i = 0; i < Affinity.values().length; i++) {
            var j = a[i];
            if (random.nextFloat() < j / s)
                return Affinity.values()[i];
            s -= j;
        }
        return null;
    }
}
