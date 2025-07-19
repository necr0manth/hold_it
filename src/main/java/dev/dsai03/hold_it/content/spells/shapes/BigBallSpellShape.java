package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.AwesomeSpellShapeEntity;
import dev.dsai03.hold_it.content.entities.BigBallSpellShapeEntity;
import dev.dsai03.hold_it.content.entities.SpellSevenShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class BigBallSpellShape extends BaseChargeableSpellShape<BigBallSpellShapeEntity> {
    public BigBallSpellShape(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 10, 1, 100, 1, 1), new AttributeValuePair(Attribute.RADIUS, 1, 1, 4, 0.2f, 0.1f), new AttributeValuePair(Attribute.PRECISION, 0, 0, 2, 1, 0));
    }

    @Override
    public BigBallSpellShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition recipe) {
        return new BigBallSpellShapeEntity(source.getCaster(), level, recipe);
    }


    @Override
    public int baselineCooldown() {
        return 10;
    }

    @Override
    public float initialComplexity() {
        return 25;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    private void adjustOnTooltipAndCrafting(SpellAdjustingContext context) {
        context.spell.setManaCost(context.spell.getShape().getValue(Attribute.MAGNITUDE) * context.spell.getManaCost());
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
