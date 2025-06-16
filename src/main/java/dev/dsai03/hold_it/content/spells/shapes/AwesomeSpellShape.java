package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.AwesomeSpellShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class AwesomeSpellShape extends BaseChargeableSpellShape{
	public AwesomeSpellShape(ResourceLocation guiIcon, AttributeValuePair... attributeValuePairs) {
		super(guiIcon, attributeValuePairs);
	}

	@Override
	public Entity createEntity(SpellSource source, Level level, ISpellDefinition definition) {
		return new AwesomeSpellShapeEntity(source.getCaster(), level, definition);
	}

	@Override
	public float initialComplexity() {
		return 10;
	}

	@Override
	public int requiredXPForRote() {
		return 100;
	}
}
