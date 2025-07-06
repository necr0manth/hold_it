package dev.dsai03.hold_it.content.spells.shapes;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.content.entities.ChargeableSpellEntity;
import dev.dsai03.hold_it.util.SpellUtils;
import net.minecraft.nbt.CompoundTag;
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

    public float getCastComplexity() {
        return 0;
    }

    @SafeVarargs
    private Class<T> getEntityClass(T... huy) {
        return (Class<T>) huy.getClass().componentType();
    }

    private void commonAdjust(SpellAdjustingContext context) {
        var recipe = context.spell;
        var nbt = new CompoundTag();
        recipe.writeToNBT(nbt);
        var newRecipe = SpellRecipe.fromNBT(nbt);
        var modifiedShape = newRecipe.getShape();
        if (modifiedShape == null)
            throw new RuntimeException("0_o");
        if (modifiedShape.getPart() instanceof IChargeableSpellShape shape) {
            newRecipe.setShape(new Shape(null) {
                @Override
                public List<SpellTarget> Target(SpellSource var1, Level var2, IModifiedSpellPart<Shape> var3, ISpellDefinition var4) {
                    throw new RuntimeException("0_o");
                }

                @Override
                public float initialComplexity() {
                    return shape.castComplexity();
                }

                @Override
                public int requiredXPForRote() {
                    throw new RuntimeException("0_o");
                }
            });
            for (var attribute : modifiedShape.getContainedAttributes())
                newRecipe.changeShapeAttributeValue(attribute, modifiedShape.getValue(attribute));
            newRecipe.calculateManaCost();
            SpellUtils.applyAdjusters(newRecipe, context.caster, false, SpellCastStage.CALCULATING_MANA_COST);
        }
        context.spell.setManaCost(newRecipe.getManaCost());

    }

    public void adjustOnSpellTooltip(SpellAdjustingContext context) {
        commonAdjust(context);
    }

    @Override
    public void adjustOnCalculatingManaCost(SpellAdjustingContext context) {
        commonAdjust(context);
        var entity = SpellUtils.getCastingSpellEntity(context.caster);
        if (getEntityClass().isInstance(entity))
            entity.adjustSpell(context);
        else
            context.spell.setManaCost(0);
    }


    @Override
    public void adjustOnSpellcraftingManaCostEstimate(SpellAdjustingContext context) {
        commonAdjust(context);
    }
}