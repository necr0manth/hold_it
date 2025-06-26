package dev.dsai03.hold_it.events;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import dev.dsai03.hold_it.util.SpellUtils;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AbobaEventHandler {
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        event.player.getCapability(PlayerMagicProvider.MAGIC).ifPresent(magic -> {
            if (SpellUtils.getCastingSpellEntity(event.player) == null)
                magic.getCastingResource().removeRegenerationModifier("chargeableSpell");
        });
    }
}
