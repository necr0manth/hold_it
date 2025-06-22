package dev.dsai03.hold_it.mixins.client;

import net.minecraft.client.particle.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {

    @Accessor
    Map<ResourceLocation, SpriteSet> getSpriteSets();

    @Accessor
    Map<ResourceLocation, ParticleProvider<?>> getProviders();

    @Accessor
    Map<ParticleRenderType, Queue<Particle>> getParticles();
}
