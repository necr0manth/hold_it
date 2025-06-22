package dev.dsai03.hold_it.util;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellDefinition;

import java.util.*;

public class AffinityDistribution {
    private volatile float[] distribution;
    private volatile Set<Affinity> affinities;
    private volatile Map<Affinity, Float> map;
    private volatile AffinityDistribution normalized;
    private volatile Random random;

    private Random getRandom() {
        if (random == null)
            random = new Random();
        return random;
    }

    private AffinityDistribution() {
    }

    public float[] asArray() {
        if (distribution == null) {
            var distribution = new float[Affinity.values().length];
            for (var aff : map.entrySet())
                distribution[aff.getKey().ordinal()] = aff.getValue();
            this.distribution = distribution;
        }
        return distribution.clone();
    }

    public Map<Affinity, Float> asMap() {
        if (map == null) {
            var map = new HashMap<Affinity, Float>();
            for (var affinity : Affinity.values()) {
                var value = distribution[affinity.ordinal()];
                if (value != 0)
                    map.put(affinity, value);
            }
            this.map = Collections.unmodifiableMap(map);
        }
        return map;
    }

    public Set<Affinity> getPresentAffinities() {
        if (affinities == null) {
            affinities = Collections.unmodifiableSet(asMap().keySet());
        }
        return affinities;
    }

    public static AffinityDistribution fromArray(float[] distribution) {
        var obj = new AffinityDistribution();
        if (distribution.length != Affinity.values().length)
            throw new RuntimeException(">_<");
        obj.distribution = distribution;
        return obj;
    }

    public static AffinityDistribution fromMap(Map<Affinity, Float> map) {
        var obj = new AffinityDistribution();
        obj.map = map;
        return obj;
    }

    public static AffinityDistribution fromSpell(ISpellDefinition spellDefinition) {
        return fromMap(spellDefinition.getAffinity());
    }

    public AffinityDistribution normalized() {
        if (normalized == null) {
            var aff = asArray();
            float s = 0;
            for (var affinity : Affinity.values()) {
                s += aff[affinity.ordinal()];
            }
            var normalizedAffinities = new float[Affinity.values().length];
            for (var affinity : Affinity.values()) {
                normalizedAffinities[affinity.ordinal()] = aff[affinity.ordinal()] / s;
            }
            this.normalized = fromArray(normalizedAffinities);
        }
        return normalized;
    }

    public Affinity getRandomAffinity() {
        var normalized = normalized().asArray();
        float s = 1;
        for (var i = 0; i < Affinity.values().length; i++) {
            var j = normalized[i];
            if (getRandom().nextFloat() < j / s)
                return Affinity.values()[i];
            s -= j;
        }
        return null;
    }

    public AffinityDistribution without(Affinity... affinities) {
        var distribution = Arrays.copyOf(asArray(), Affinity.values().length);
        for (var affinity : affinities)
            distribution[affinity.ordinal()] = 0;
        return fromArray(distribution);
    }

    public float getAffinity(Affinity affinity) {
        return asArray()[affinity.ordinal()];
    }
}
