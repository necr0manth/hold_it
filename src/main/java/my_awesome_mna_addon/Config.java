package my_awesome_mna_addon;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


@Mod.EventBusSubscriber(modid = MyAwesomeMnaAddon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	//	private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
//			                                                                   .comment("Sample config comment")
//			                                                                   .define("sampleConfigValue", true);
	static final ForgeConfigSpec SPEC = BUILDER.build();

	public static boolean sampleConfigValue;

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
//		sampleConfigValue = LOG_DIRT_BLOCK.get();
	}
}
