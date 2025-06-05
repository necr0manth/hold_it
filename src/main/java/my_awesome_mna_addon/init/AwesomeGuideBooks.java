package my_awesome_mna_addon.init;

import com.mna.api.guidebook.RegisterGuidebooksEvent;
import my_awesome_mna_addon.MyAwesomeMnaAddon;
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
