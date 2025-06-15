package dev.dsai03.hold_it.init;

import com.mna.api.faction.BaseFaction;
import com.mna.api.faction.IFaction;
import com.mna.api.tools.RLoc;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.factions.ExampleFaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeFactions {
	DeferredRegister<IFaction> FACTIONS = DeferredRegister.create(RLoc.create("factions"), MyAwesomeMnaAddon.MODID);
	RegistryObject<BaseFaction> EXAMPLE_FACTION = FACTIONS.register("a", ExampleFaction::new);
}
