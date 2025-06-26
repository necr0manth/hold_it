package dev.dsai03.hold_it.init;

import com.mna.api.spells.adjusters.DefaultAdjusters;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.spells.SpellCaster;
import dev.dsai03.hold_it.content.spells.shapes.IChargeableSpellShape;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AwesomeAdjusters {
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        SpellCaster.registerAdjuster(ctx -> ctx.stage == SpellCastStage.CALCULATING_MANA_COST, AwesomeAdjusters::adjustChargeableSpell);
    }

    public static void adjustChargeableSpell(SpellAdjustingContext context) {
        if (Optional.ofNullable(context.spell.getShape()).map(IModifiedSpellPart::getPart).orElse(null) instanceof IChargeableSpellShape shape) {
            shape.adjustSpell(context);
        }
    }
}
