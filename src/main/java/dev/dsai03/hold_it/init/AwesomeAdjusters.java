package dev.dsai03.hold_it.init;

import com.mna.api.spells.adjusters.DefaultAdjusters;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.spells.SpellCaster;
import dev.dsai03.hold_it.content.spells.shapes.IChargeableSpellShape;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AwesomeAdjusters {
    private static boolean adjusterRegistered = false;

    // Попробуем зарегистрироваться на FMLLoadCompleteEvent с самым низким приоритетом
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        // Используем отложенную регистрацию через enqueueWork для выполнения в основном потоке
        event.enqueueWork(() -> {
            if (!adjusterRegistered) {
                registerAdjuster();
            }
        });
    }

    private static void registerAdjuster() {
        try {
            SpellCaster.registerAdjuster(ctx -> ctx.stage == SpellCastStage.CALCULATING_MANA_COST, AwesomeAdjusters::adjustChargeableSpell);
            adjusterRegistered = true;
            System.out.println("[Hold-it!] Successfully registered spell adjuster");
        } catch (Exception e) {
            System.err.println("[Hold-it!] Failed to register spell adjuster: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void adjustChargeableSpell(SpellAdjustingContext context) {
        if (Optional.ofNullable(context.spell.getShape()).map(IModifiedSpellPart::getPart).orElse(null) instanceof IChargeableSpellShape shape) {
            shape.adjustSpell(context);
        }
    }
}
