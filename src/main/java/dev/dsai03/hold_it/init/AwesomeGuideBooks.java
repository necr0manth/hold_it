package dev.dsai03.hold_it.init;

import com.mna.api.guidebook.RegisterGuidebooksEvent;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AwesomeGuideBooks {
	@SubscribeEvent
	public static void onRegisterGuidebooks(RegisterGuidebooksEvent event) {
		event.getRegistry().addGuidebookPath(new ResourceLocation(MyAwesomeMnaAddon.MODID, "guide"));
	}
}
