package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.AwesomeSpellShapeEntity;
import dev.dsai03.hold_it.content.entities.SpellSevenShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SpellSevenShape extends BaseChargeableSpellShape<SpellSevenShapeEntity> {
    public SpellSevenShape(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 10, 1, 100, 5, 1),
                new AttributeValuePair(Attribute.RADIUS, 2, 1, 4, 0.1f, 1), new AttributeValuePair(Attribute.DELAY, 1, 1, 10, 1, 1));
    }

    @Override
    public SpellSevenShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition recipe) {
        return new SpellSevenShapeEntity(source.getCaster(), level, recipe);
    }


    @Override
    public int baselineCooldown() {
        return 40;
    }

    @Override
    public float initialComplexity() {
        return 20;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    private void adjustOnTooltipAndCrafting(SpellAdjustingContext context) {
        context.spell.setManaCost((context.spell.getShape().getValue(Attribute.MAGNITUDE) + context.spell.getShape().getValue(Attribute.RADIUS)) * context.spell.getManaCost());
    }

    @Override
    public void adjustOnSpellcraftingManaCostEstimate(SpellAdjustingContext context) {
        super.adjustOnSpellcraftingManaCostEstimate(context);
        adjustOnTooltipAndCrafting(context);
    }

    @Override
    public void adjustOnSpellTooltip(SpellAdjustingContext context) {
        super.adjustOnSpellTooltip(context);
        adjustOnTooltipAndCrafting(context);
    }
}
