package dev.dsai03.hold_it.init;

import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.spells.SpellCaster;
import dev.dsai03.hold_it.util.ISpellMultiAdjuster;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AwesomeAdjusters {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(AwesomeAdjusters::registerAdjusters);
    }

    private static void registerAdjusters() {
        SpellCaster.registerAdjuster(ctx -> ctx.stage == SpellCastStage.SPELL_TOOLTIP, createAdjuster((ctx, multiAdjuster) -> multiAdjuster.adjustOnSpellTooltip(ctx)));
        SpellCaster.registerAdjuster(ctx -> ctx.stage == SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE, createAdjuster((ctx, multiAdjuster) -> multiAdjuster.adjustOnSpellcraftingManaCostEstimate(ctx)));
        SpellCaster.registerAdjuster(ctx -> ctx.stage == SpellCastStage.CALCULATING_MANA_COST, createAdjuster((ctx, multiAdjuster) -> multiAdjuster.adjustOnCalculatingManaCost(ctx)));
        SpellCaster.registerAdjuster(ctx -> ctx.stage == SpellCastStage.CASTING, createAdjuster((ctx, multiAdjuster) -> multiAdjuster.adjustOnCasting(ctx)));
    }

    private static Consumer<SpellAdjustingContext> createAdjuster(BiConsumer<SpellAdjustingContext, ISpellMultiAdjuster> adjuster) {
        return context -> {
            if (Optional.ofNullable(context.spell.getShape()).map(IModifiedSpellPart::getPart).orElse(null) instanceof ISpellMultiAdjuster shape) {
                adjuster.accept(context, shape);
            }
            context.spell.iterateComponents(component -> {
                if (Optional.ofNullable(component).map(IModifiedSpellPart::getPart).orElse(null) instanceof ISpellMultiAdjuster effect) {
                    adjuster.accept(context, effect);
                }
            });
        };
    }
}
