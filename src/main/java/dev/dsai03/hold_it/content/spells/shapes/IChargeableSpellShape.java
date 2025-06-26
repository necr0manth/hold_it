package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.adjusters.SpellAdjustingContext;

public interface IChargeableSpellShape {
    default void adjustSpell(SpellAdjustingContext context) {
    }
}
