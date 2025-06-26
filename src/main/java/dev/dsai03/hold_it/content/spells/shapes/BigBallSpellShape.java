package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.AwesomeSpellShapeEntity;
import dev.dsai03.hold_it.content.entities.BigBallSpellShapeEntity;
import dev.dsai03.hold_it.content.entities.SpellSevenShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class BigBallSpellShape extends BaseChargeableSpellShape<SpellSevenShapeEntity> {
    public BigBallSpellShape(ResourceLocation guiIcon, AttributeValuePair... attributeValuePairs) {
        super(guiIcon, attributeValuePairs);
    }

    @Override
    public SpellSevenShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition recipe) {
        return new BigBallSpellShapeEntity(source.getCaster(), level, recipe);
    }


    @Override
    public int baselineCooldown() {
        return 70;
    }

    @Override
    public float initialComplexity() {
        return 25;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }
}
