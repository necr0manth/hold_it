package dev.dsai03.hold_it.content.spells.shapes;

import dev.dsai03.hold_it.util.ISpellMultiAdjuster;

public interface IChargeableSpellShape extends ISpellMultiAdjuster {
    default float castComplexity() {
        return 0;
    }
}
