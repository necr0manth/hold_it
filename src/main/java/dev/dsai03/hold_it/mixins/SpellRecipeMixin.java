package dev.dsai03.hold_it.mixins;

import com.mna.api.spells.parts.Shape;
import com.mna.spells.crafting.SpellRecipe;
import dev.dsai03.hold_it.content.spells.shapes.IChargeableSpellShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpellRecipe.class)
public class SpellRecipeMixin {
	@Redirect(method = "getMaxChannelTime", at = @At(value = "INVOKE", target = "Lcom/mna/api/spells/parts/Shape;isChanneled()Z"))
	boolean getMaxChannelTime(Shape shape) {
		if (shape instanceof IChargeableSpellShape)
			return true;
		return shape.isChanneled();
	}
}
