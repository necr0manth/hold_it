package my_awesome_mna_addon.init;

import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.RLoc;
import my_awesome_mna_addon.MyAwesomeMnaAddon;
import my_awesome_mna_addon.content.spells.ComponentPorkWarrior;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeSpellComponents {
	DeferredRegister<SpellEffect> EFFECTS = DeferredRegister.create(RLoc.create("components"), MyAwesomeMnaAddon.MODID);
	RegistryObject<ComponentPorkWarrior> PORK_WARRIOR = EFFECTS.register("pork_warrior", () -> new ComponentPorkWarrior(new ResourceLocation(MyAwesomeMnaAddon.MODID, "pork_warrior"), new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/pork_warrior.png")));

}
