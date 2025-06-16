package dev.dsai03.hold_it.events;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ShitEventHandler {

	@SubscribeEvent
	public static void shit(TickEvent.ServerTickEvent event) {
		//раскомментируйте это, если хотите потестить что-то на очень лагучем сервере
//		int a = 0;
//		for(int i = 0;i<300000000;i++){
//			a+=i;
//		}
	}
}
