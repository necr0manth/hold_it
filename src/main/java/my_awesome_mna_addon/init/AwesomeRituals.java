package my_awesome_mna_addon.init;

import com.mna.api.rituals.RitualEffect;
import com.mna.api.tools.RLoc;
import my_awesome_mna_addon.MyAwesomeMnaAddon;
import my_awesome_mna_addon.content.rituals.RitualEffectPorcine;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeRituals {
	DeferredRegister<RitualEffect> RITUALS = DeferredRegister.create(RLoc.create("ritual-effects"), MyAwesomeMnaAddon.MODID);
	RegistryObject<RitualEffectPorcine> RITUAL_PORCINE = RITUALS.register("ritual_govna", () -> new RitualEffectPorcine(new ResourceLocation(MyAwesomeMnaAddon.MODID, "ritual_govna")));
}
