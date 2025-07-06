package dev.dsai03.hold_it.util;

import com.mna.api.spells.adjusters.SpellAdjustingContext;

public interface ISpellMultiAdjuster {
    default void adjustOnSpellTooltip(SpellAdjustingContext context) {
    }

    default void adjustOnSpellcraftingManaCostEstimate(SpellAdjustingContext context) {
    }

    default void adjustOnCalculatingManaCost(SpellAdjustingContext context) {
    }

    default void adjustOnCasting(SpellAdjustingContext context) {
    }
}
