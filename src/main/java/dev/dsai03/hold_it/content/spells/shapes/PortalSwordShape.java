package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.PortalSwordShapeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PortalSwordShape extends BaseChargeableSpellShape<PortalSwordShapeEntity> {
    public PortalSwordShape(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 10, 1, 30, 1, 10), new AttributeValuePair(Attribute.DURATION, 10, 1, 30, 1, 10), new AttributeValuePair(Attribute.SPEED, 1, 0.1f, 5, 0.1f, 10));
    }

    @Override
    public PortalSwordShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition recipe) {
        return new PortalSwordShapeEntity(source.getCaster(), level, recipe);
    }

    @Override
    public int baselineCooldown() {
        return 40;
    }

    @Override
    public float initialComplexity() {
        return 15;
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    private void adjustSpell(SpellAdjustingContext context) {
        var manaCost = (float) (0.5 * context.spell.getShape().getValue(Attribute.MAGNITUDE) * context.spell.getShape().getValue(Attribute.SPEED) * Math.sqrt(context.spell.getShape().getValue(Attribute.DURATION)) * context.spell.getManaCost());
        context.spell.setManaCost(manaCost);
    }

    @Override
    public void adjustOnSpellTooltip(SpellAdjustingContext context) {
        super.adjustOnSpellTooltip(context);
        adjustSpell(context);
    }

    @Override
    public void adjustOnSpellcraftingManaCostEstimate(SpellAdjustingContext context) {
        super.adjustOnSpellcraftingManaCostEstimate(context);
        adjustSpell(context);
    }
}