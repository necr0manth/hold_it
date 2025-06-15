package dev.dsai03.hold_it.events;


import com.mna.api.events.CastingResourceGuiRegistrationEvent;
import com.mna.api.events.CastingResourceRegistrationEvent;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.factions.FactionRIDs;
import dev.dsai03.hold_it.content.factions.castingresources.ExampleMana;
import dev.dsai03.hold_it.content.factions.castingresources.ExampleManaGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class RegistrationEventHandler {
    @Mod.EventBusSubscriber(modid = MyAwesomeMnaAddon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class RegistrationEventHandlerClient {
        @SubscribeEvent
        public static void onCastingResourceRegistrationEvent(CastingResourceGuiRegistrationEvent event){
            event.getRegistry().registerResourceGui(FactionRIDs.EXAMPLE_MANA, new ExampleManaGui());
        }
    }

    @Mod.EventBusSubscriber(modid = MyAwesomeMnaAddon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistrationEventHandlerCommon {
        @SubscribeEvent
        public static void onCastingResourceRegistrationEvent(CastingResourceRegistrationEvent event){
            event.getRegistry().register(FactionRIDs.EXAMPLE_MANA, ExampleMana.class);
        }
    }
}
