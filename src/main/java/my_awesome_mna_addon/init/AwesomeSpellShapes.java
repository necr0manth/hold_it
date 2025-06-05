package my_awesome_mna_addon.init;

import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.RLoc;
import my_awesome_mna_addon.MyAwesomeMnaAddon;
import my_awesome_mna_addon.content.spells.ComponentPorkWarrior;
import my_awesome_mna_addon.content.spells.ShapePorkPulse;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeSpellShapes {
	DeferredRegister<Shape> SHAPES = DeferredRegister.create(RLoc.create("shapes"), MyAwesomeMnaAddon.MODID);
	RegistryObject<ShapePorkPulse> SHAPE_PORK_PULSE = SHAPES.register("pork_pulse", () -> new ShapePorkPulse(new ResourceLocation(MyAwesomeMnaAddon.MODID, "pork_pulse"), new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/pork_pulse.png")));
}
