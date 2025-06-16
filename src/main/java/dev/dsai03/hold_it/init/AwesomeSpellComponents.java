package dev.dsai03.hold_it.init;

import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.RLoc;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.spells.ComponentPorkWarrior;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeSpellComponents {
	DeferredRegister<SpellEffect> EFFECTS = DeferredRegister.create(RLoc.create("components"), MyAwesomeMnaAddon.MODID);
//	RegistryObject<ComponentPorkWarrior> PORK_WARRIOR = EFFECTS.register("pork_warrior", () -> new ComponentPorkWarrior(new ResourceLocation(MyAwesomeMnaAddon.MODID, "pork_warrior"), new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/pork_warrior.png")));
}
