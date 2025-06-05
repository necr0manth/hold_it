package my_awesome_mna_addon;

import com.mojang.logging.LogUtils;
import my_awesome_mna_addon.init.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MyAwesomeMnaAddon.MODID)
public class MyAwesomeMnaAddon {
	public static final String MODID = "my_awesome_mna_addon";
	public static final Logger LOGGER = LogUtils.getLogger();

	public MyAwesomeMnaAddon() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);

		AwesomeBlocks.BLOCKS.register(modEventBus);
		AwesomeItems.ITEMS.register(modEventBus);
		AwesomeCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
//		AwesomeFactions.FACTIONS.register(modEventBus);
//		AwesomeSpells.SHAPES.register(modEventBus);
//		AwesomeSpells.EFFECTS.register(modEventBus);
		MinecraftForge.EVENT_BUS.register(this);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
	}

}
