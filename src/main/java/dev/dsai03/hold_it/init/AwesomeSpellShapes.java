package dev.dsai03.hold_it.init;

import com.mna.api.spells.parts.Shape;
import com.mna.api.tools.RLoc;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.spells.shapes.AwesomeSpellShape;
import dev.dsai03.hold_it.content.spells.shapes.BigBallSpellShape;
import dev.dsai03.hold_it.content.spells.shapes.CoolShape;
import dev.dsai03.hold_it.content.spells.shapes.SpellSevenShape;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeSpellShapes {
	DeferredRegister<Shape> SHAPES = DeferredRegister.create(RLoc.create("shapes"), MyAwesomeMnaAddon.MODID);
//	RegistryObject<ShapePorkPulse> SHAPE_PORK_PULSE = SHAPES.register("pork_pulse", () -> new ShapePorkPulse(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/pork_pulse.png")));
	RegistryObject<CoolShape> COOL_SHAPE = SHAPES.register("cool_shape", () -> new CoolShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shape/1spelltg.png")));
	RegistryObject<AwesomeSpellShape> AWESOME_SHAPE = SHAPES.register("awesome_shape", () -> new AwesomeSpellShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shape/awesome_shape.png")));
	RegistryObject<SpellSevenShape> SEVEN_SHAPE = SHAPES.register("seven_shape", () -> new SpellSevenShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shape/7spelltg.png")));
	RegistryObject<BigBallSpellShape> BIG_BALL_SPELL_SHAPE = SHAPES.register("big_ball_shape", () -> new BigBallSpellShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shape/seven_shape.png")));
}
