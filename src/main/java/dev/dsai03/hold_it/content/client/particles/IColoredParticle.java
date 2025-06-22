package dev.dsai03.hold_it.content.client.particles;


import java.awt.*;

public interface IColoredParticle extends IParticle {
    Color getColor();
    void setColor(Color color);
}
