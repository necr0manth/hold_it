package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.AwesomeSpellShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class AwesomeSpellShape extends BaseChargeableSpellShape<AwesomeSpellShapeEntity> {
    public AwesomeSpellShape(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 2, 1, 10, 1f, 20), new AttributeValuePair(Attribute.PRECISION, 0, 0, 2, 1, 10));
    }

    @Override
    public AwesomeSpellShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition definition) {
        return new AwesomeSpellShapeEntity(source.getCaster(), level, definition);
    }

    @Override
    public float initialComplexity() {
        return 20;
    }

    @Override
    public int requiredXPForRote() {
        return 1000;
    }
}
