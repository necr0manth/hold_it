package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import dev.dsai03.hold_it.content.entities.ChargeableSpellEntity;
import dev.dsai03.hold_it.util.SpellUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class BaseChargeableSpellShape<T extends ChargeableSpellEntity> extends Shape implements IChargeableSpellShape {

    public BaseChargeableSpellShape(ResourceLocation guiIcon, AttributeValuePair... attributeValuePairs) {
        super(guiIcon, attributeValuePairs);
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level level, IModifiedSpellPart<Shape> part, ISpellDefinition recipe) {
        if (!level.isClientSide)
            level.addFreshEntity(createEntity(source, level, recipe));
        return List.of(new SpellTarget(source.getCaster()));
    }

    public abstract T createEntity(SpellSource source, Level level, ISpellDefinition definition);

    public boolean spawnsTargetEntity() {
        return true;
    }

    @Override
    public int maxChannelTime(IModifiedSpellPart<Shape> shape) {
        return 100000000;
    }

    public float castComplexity() {
        return 0;
    }

    @SafeVarargs
    private Class<T> getEntityClass(T... huy) {
        return (Class<T>) huy.getClass().componentType();
    }

    @Override
    public void adjustSpell(SpellAdjustingContext context) {
        var entity = SpellUtils.getCastingSpellEntity(context.caster);
        if (getEntityClass().isInstance(entity))
            entity.adjustSpell(context);
        else
            context.spell.setManaCost(0);
    }
}
