package dev.dsai03.hold_it.capabilities;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IMagicCharge {
	int getCharge();
	void setCharge(int amount);
}
