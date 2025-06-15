package dev.dsai03.hold_it.init;

import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeCreativeTabs {
	DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MyAwesomeMnaAddon.MODID);
	RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("tab1",
			() -> CreativeModeTab.builder().icon(() -> new ItemStack(AwesomeItems.EXAMPLE_ITEM.get()))
					      .title(Component.literal("Awesome creative tab"))
					      .displayItems((pParameters, pOutput) -> {
						      pOutput.accept(AwesomeItems.EXAMPLE_ITEM.get());
						      pOutput.accept(AwesomeItems.EXAMPLE_BLOCK_ITEM.get());
					      })
					      .withTabsBefore(CreativeModeTabs.COMBAT)
					      .build());
}
