package dev.dsai03.hold_it.init;

import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.entities.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public interface AwesomeEntityTypes {
    DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MyAwesomeMnaAddon.MODID);
    RegistryObject<EntityType<CoolShapeEntity>> COOL_SHAPE = register("cool_shape", () -> EntityType.Builder.<CoolShapeEntity>of(CoolShapeEntity::new, MobCategory.MISC).fireImmune().sized(0, 0));
    RegistryObject<EntityType<AwesomeSpellShapeEntity>> AWESOME_SHAPE = register("awesome_shape", () -> EntityType.Builder.<AwesomeSpellShapeEntity>of(AwesomeSpellShapeEntity::new, MobCategory.MISC).fireImmune().sized(0, 0));
    RegistryObject<EntityType<BallEntity>> BALL_ENTITY_TYPE = register("ball", () -> EntityType.Builder.<BallEntity>of(BallEntity::new, MobCategory.MISC).fireImmune().fireImmune());
    RegistryObject<EntityType<SpellSevenShapeEntity>> SEVEN_SHAPE = register("seven_shape", () -> EntityType.Builder.<SpellSevenShapeEntity>of(SpellSevenShapeEntity::new, MobCategory.MISC).fireImmune().sized(0, 0));
    RegistryObject<EntityType<SphereEntity>> SPHERE_ENTITY_TYPE = register("sphere", () -> EntityType.Builder.<SphereEntity>of(SphereEntity::new, MobCategory.MISC).fireImmune());
    RegistryObject<EntityType<BigBallSpellShapeEntity>> BIG_BALL_SPELL_SHAPE = register("big_ball_shape", () -> EntityType.Builder.<BigBallSpellShapeEntity>of(BigBallSpellShapeEntity::new, MobCategory.MISC).fireImmune().sized(0, 0));
    RegistryObject<EntityType<BigBallEntity>> BIG_BALL_ENTITY_TYPE = register("big_ball", () -> EntityType.Builder.<BigBallEntity>of(BigBallEntity::new, MobCategory.MISC).fireImmune());
    RegistryObject<EntityType<PortalEntity>> PORTAL_ENTITY_TYPE = register("portal", () -> EntityType.Builder.<PortalEntity>of(PortalEntity::new, MobCategory.MISC).fireImmune().sized(1, 1));
    RegistryObject<EntityType<SwordEntity>> SWORD_ENTITY_TYPE = register("sword", () -> EntityType.Builder.<SwordEntity>of(SwordEntity::new, MobCategory.MISC).fireImmune().sized(0.5f, 0.5f));
    RegistryObject<EntityType<PortalSwordShapeEntity>> PORTAL_SWORD_SHAPE = register("portal_sword_shape", () -> EntityType.Builder.<PortalSwordShapeEntity>of(PortalSwordShapeEntity::new, MobCategory.MISC).fireImmune().sized(0, 0));

    static <T extends Entity> RegistryObject<EntityType<T>> register(String name, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITY_TYPES.register(name, () -> builderSupplier.get().build(MyAwesomeMnaAddon.MODID + ":" + name));
    }
}
