package dev.dsai03.hold_it.util;

import com.ibm.icu.text.MessagePattern;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.world.entity.Entity;

public class ParticleUtils {

	public static MAParticleType getParticleType(Affinity affinity) {
		return switch (affinity) {
			case ARCANE -> ParticleInit.ARCANE.get();
			case EARTH -> ParticleInit.EARTH.get();
			case ENDER -> ParticleInit.ENDER.get();
			case FIRE -> ParticleInit.FLAME.get();
			case WATER -> ParticleInit.WATER.get();
			case WIND -> ParticleInit.AIR_ORBIT.get();
			case HELLFIRE -> ParticleInit.HELLFIRE.get();
			case ICE -> ParticleInit.FROST.get();
			case LIGHTNING -> ParticleInit.LIGHTNING_BOLT.get();
			case UNKNOWN -> null;
			case BLOOD -> ParticleInit.BLOOD.get();
		};
	}
}
