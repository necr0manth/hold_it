package dev.dsai03.hold_it.init;

import com.mna.api.items.TieredItem;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static dev.dsai03.hold_it.init.AwesomeBlocks.EXAMPLE_BLOCK;

public interface AwesomeItems {
	DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MyAwesomeMnaAddon.MODID);
	RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));
	RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));
	RegistryObject<Item> MARK_OF_THE_EXAMPLE_FACTION = ITEMS.register("mark_of_the_example_faction", () -> new TieredItem(new Item.Properties()));
}
