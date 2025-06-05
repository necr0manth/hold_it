package my_awesome_mna_addon.init;

import com.mna.Registries;
import com.mna.api.faction.BaseFaction;
import com.mna.api.faction.IFaction;
import my_awesome_mna_addon.MyAwesomeMnaAddon;
import my_awesome_mna_addon.content.factions.ExampleFaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public interface AwesomeFactions {
	DeferredRegister<IFaction> FACTIONS = DeferredRegister.create(Registries.Factions.get(), MyAwesomeMnaAddon.MODID);
	RegistryObject<BaseFaction> EXAMPLE_FACTION = FACTIONS.register("a", ExampleFaction::new);

}
