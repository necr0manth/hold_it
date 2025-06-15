package dev.dsai03.hold_it.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class MagicChargeImpl implements IMagicCharge, INBTSerializable<CompoundTag> {
	private int charge = 0;

	@Override public int getCharge() { return charge; }
	@Override public void setCharge(int amount) { this.charge = amount; }

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Charge", charge);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		charge = tag.getInt("Charge");
	}
}
