package dev.dsai03.hold_it.init;

import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;

public interface AwesomeCreativeTabs {
	DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MyAwesomeMnaAddon.MODID);
//	RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("hold_it",
//			() -> CreativeModeTab.builder().icon(() -> new ItemStack(AwesomeItems.EXAMPLE_ITEM.get()))
//					      .title(Component.literal("Hold it!"))
//					      .displayItems((pParameters, pOutput) -> {
////						      pOutput.accept(AwesomeItems.EXAMPLE_ITEM.get());
////						      pOutput.accept(AwesomeItems.EXAMPLE_BLOCK_ITEM.get());
//					      })
//					      .withTabsBefore(CreativeModeTabs.COMBAT)
//					      .build());
}
