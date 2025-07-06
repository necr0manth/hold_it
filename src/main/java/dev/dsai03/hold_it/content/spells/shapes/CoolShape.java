package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellSource;
import dev.dsai03.hold_it.content.entities.CoolShapeEntity;
import dev.dsai03.hold_it.util.SpellUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CoolShape extends BaseChargeableSpellShape<CoolShapeEntity> {
    public CoolShape(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 0.5f, 0, 1, 0.02f, 1),
                new AttributeValuePair(Attribute.RADIUS, 10, 5, 20, 1, 2));
    }

    @Override
    public CoolShapeEntity createEntity(SpellSource source, Level level, ISpellDefinition recipe) {
        return new CoolShapeEntity(source.getCaster(), recipe, level);
    }

    @Override
    public int baselineCooldown() {
        return 40;
    }

    @Override
    public float initialComplexity() {
        return 10;
    }

    @Override
    public int requiredXPForRote() {
        return 10;
    }

    public void adjustOnTooltipAndCrafting(SpellAdjustingContext context) {
        if (context.caster instanceof Player)
            context.spell.setManaCost(SpellUtils.getPlayerMagic(context.caster).getCastingResource().getMaxAmount() * context.spell.getShape().getValue(Attribute.MAGNITUDE));

    }

    @Override
    public void adjustOnSpellcraftingManaCostEstimate(SpellAdjustingContext context) {
        super.adjustOnSpellcraftingManaCostEstimate(context);
    }

    @Override
    public void adjustOnSpellTooltip(SpellAdjustingContext context) {
        super.adjustOnSpellcraftingManaCostEstimate(context);
        adjustOnTooltipAndCrafting(context);
    }
}
