package dev.dsai03.hold_it.util;

import com.mna.api.spells.base.ISpellDefinition;

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
}
