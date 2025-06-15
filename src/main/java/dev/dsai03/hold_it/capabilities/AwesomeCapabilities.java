package dev.dsai03.hold_it.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public interface AwesomeCapabilities {
	Capability<IMagicCharge> MAGIC_CHARGE = CapabilityManager.get(new CapabilityToken<>() {});
}
