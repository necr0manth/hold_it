package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.CoolShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class CoolShape extends BaseChargeableSpellShape<CoolShapeEntity> {
	public CoolShape(ResourceLocation guiIcon, AttributeValuePair... attributeValuePairs) {
		super(guiIcon, attributeValuePairs);
	}

	@Override
	public CoolShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition recipe) {
		return new CoolShapeEntity(source.getCaster(), recipe, level);
	}

	@Override
	public int baselineCooldown() {
		return 100;
	}

	@Override
	public float initialComplexity() {
		return 10;
	}

	@Override
	public int requiredXPForRote() {
		return 10;
	}
}
