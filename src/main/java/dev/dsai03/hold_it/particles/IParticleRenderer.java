package dev.dsai03.hold_it.particles;

import java.util.Set;

public interface IParticleRenderer<P extends IParticle, RENDER_CONTEXT> {
    void renderParticles(RENDER_CONTEXT renderContext, Set<P> renderData);
}
