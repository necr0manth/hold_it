package dev.dsai03.hold_it.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

import static dev.dsai03.hold_it.MyAwesomeMnaAddon.MODID;
import static dev.dsai03.hold_it.capabilities.AwesomeCapabilities.MAGIC_CHARGE;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityAttachHandler {

	@SubscribeEvent
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {

			event.addCapability(new ResourceLocation(MODID, "magic_charge"),
					new ICapabilitySerializable<CompoundTag>() {
						MagicChargeImpl impl = new MagicChargeImpl();
						LazyOptional<IMagicCharge> opt = LazyOptional.of(() -> impl);
						@Override
						public CompoundTag serializeNBT() {
							return impl.serializeNBT();
						}

						@Override
						public void deserializeNBT(CompoundTag nbt) {
							impl.deserializeNBT(nbt);
						}

						@Nonnull
						@Override
						public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
							return MAGIC_CHARGE.orEmpty(cap, opt);
						}
					});
		}
	}
}
