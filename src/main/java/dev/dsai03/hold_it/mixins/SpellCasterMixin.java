package dev.dsai03.hold_it.mixins;

import com.mna.api.spells.parts.Shape;
import com.mna.spells.SpellCaster;
import dev.dsai03.hold_it.content.spells.shapes.IChargeableSpellShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpellCaster.class)
public class SpellCasterMixin {
	@Redirect(method = "Affect(Lnet/minecraft/world/item/ItemStack;Lcom/mna/api/spells/base/ISpellDefinition;Lnet/minecraft/world/level/Level;Lcom/mna/api/spells/targeting/SpellSource;Lcom/mna/api/spells/targeting/SpellTarget;Lcom/mna/api/spells/targeting/SpellContext;)Lcom/mna/api/spells/SpellCastingResult;", at = @At(value = "INVOKE", target = "Lcom/mna/api/spells/parts/Shape;isChanneled()Z"))
	private static boolean affect(Shape shape) {
		if (shape instanceof IChargeableSpellShape)
			return true;
		return shape.isChanneled();
	}
}
