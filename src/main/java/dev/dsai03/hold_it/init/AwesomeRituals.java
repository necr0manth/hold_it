package dev.dsai03.hold_it.init;

import com.mna.api.rituals.RitualEffect;
import com.mna.api.tools.RLoc;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.rituals.RitualEffectPorcine;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeRituals {
	DeferredRegister<RitualEffect> RITUALS = DeferredRegister.create(RLoc.create("ritual-effects"), MyAwesomeMnaAddon.MODID);
	RegistryObject<RitualEffectPorcine> RITUAL_PORCINE = RITUALS.register("ritual_govna", () -> new RitualEffectPorcine(new ResourceLocation(MyAwesomeMnaAddon.MODID, "ritual_govna")));
}
