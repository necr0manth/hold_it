package dev.dsai03.hold_it.init;

import com.mna.api.spells.parts.Shape;
import com.mna.api.tools.RLoc;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.spells.shapes.AwesomeSpellShape;
import dev.dsai03.hold_it.content.spells.shapes.ChargeBallShape;
import dev.dsai03.hold_it.content.spells.shapes.CoolShape;
import dev.dsai03.hold_it.content.spells.shapes.SpellSevenShape;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface AwesomeSpellShapes {
	DeferredRegister<Shape> SHAPES = DeferredRegister.create(RLoc.create("shapes"), MyAwesomeMnaAddon.MODID);
//	RegistryObject<ShapePorkPulse> SHAPE_PORK_PULSE = SHAPES.register("pork_pulse", () -> new ShapePorkPulse(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/pork_pulse.png")));
	RegistryObject<CoolShape> COOL_SHAPE = SHAPES.register("cool_shape", () -> new CoolShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shapes/cool_shape.png")));
	RegistryObject<AwesomeSpellShape> AWESOME_SHAPE = SHAPES.register("awesome_shape", () -> new AwesomeSpellShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shapes/awesome_shape.png")));
	RegistryObject<SpellSevenShape> SEVEN_SHAPE = SHAPES.register("seven_shape", () -> new SpellSevenShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shapes/seven_shape.png")));
	RegistryObject<ChargeBallShape> BALL_SHAPE = SHAPES.register("ball_shape", () -> new ChargeBallShape(new ResourceLocation(MyAwesomeMnaAddon.MODID, "textures/spells/shapes/ball_shape.png")));
}
