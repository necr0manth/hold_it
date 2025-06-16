package dev.dsai03.hold_it.init;

import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.entities.AwesomeSpellShapeEntity;
import dev.dsai03.hold_it.content.entities.BallEntity;
import dev.dsai03.hold_it.content.entities.CoolShapeEntity;
import dev.dsai03.hold_it.content.entities.SpellSevenShapeEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public interface AwesomeEntityTypes {
	DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MyAwesomeMnaAddon.MODID);
	RegistryObject<EntityType<CoolShapeEntity>> COOL_SHAPE = register("cool_shape", () -> EntityType.Builder.of(CoolShapeEntity::new, MobCategory.MISC));
	RegistryObject<EntityType<AwesomeSpellShapeEntity>> AWESOME_SHAPE = register("awesome_shape", () -> EntityType.Builder.of(AwesomeSpellShapeEntity::new, MobCategory.MISC));
	RegistryObject<EntityType<BallEntity>> BALL_ENTITY_TYPE = register("ball", () -> EntityType.Builder.of(BallEntity::new, MobCategory.MISC));

	RegistryObject<EntityType<SpellSevenShapeEntity>> SEVEN_SHAPE = register("seven_shape", () -> EntityType.Builder.of(SpellSevenShapeEntity::new, MobCategory.MISC));

	static <T extends Entity> RegistryObject<EntityType<T>> register(String name, Supplier<EntityType.Builder<T>> builderSupplier) {
		return ENTITY_TYPES.register(name, () -> builderSupplier.get().build(MyAwesomeMnaAddon.MODID + ":" + name));
	}
}
