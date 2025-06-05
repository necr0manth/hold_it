package my_awesome_mna_addon.init;

import com.mna.Registries;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import my_awesome_mna_addon.MyAwesomeMnaAddon;
import my_awesome_mna_addon.content.spells.ComponentPorkWarrior;
import my_awesome_mna_addon.content.spells.ShapePorkPulse;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeSpells {
	DeferredRegister<Shape> SHAPES = DeferredRegister.create(Registries.Shape.get(), MyAwesomeMnaAddon.MODID);
	DeferredRegister<SpellEffect> EFFECTS = DeferredRegister.create(Registries.SpellEffect.get(), MyAwesomeMnaAddon.MODID);
	RegistryObject<ShapePorkPulse> SHAPE_PORK_PULSE = SHAPES.register("pork_pulse", () -> new ShapePorkPulse(new ResourceLocation(MyAwesomeMnaAddon.MODID, "pork_pulse"), new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/pork_pulse.png")));
	RegistryObject<SpellEffect> PORK_WARRIOR = EFFECTS.register("pork_warrior", () -> new ComponentPorkWarrior(new ResourceLocation(MyAwesomeMnaAddon.MODID, "pork_warrior"), new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/pork_warrior.png")));
}
