package dev.dsai03.hold_it.mixins.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor
    ClientLevel getLevel();

    @Accessor
    double getXo();

    @Accessor
    double getYo();

    @Accessor
    double getZo();

    @Accessor
    double getX();

    @Accessor
    double getY();

    @Accessor
    double getZ();

    @Accessor
    double getXd();

    @Accessor
    double getYd();

    @Accessor
    double getZd();

    @Accessor
    AABB getBb();

    @Accessor
    boolean getOnGround();

    @Accessor
    boolean getHasPhysics();

    @Accessor
    boolean getStoppedByCollision();

    @Accessor
    boolean getRemoved();

    @Accessor
    float getBbWidth();

    @Accessor
    float getBbHeight();

    @Accessor
    RandomSource getRandom();

    @Accessor
    int getAge();

    @Accessor
    int getLifetime();

    @Accessor
    float getGravity();

    @Accessor
    float getRCol();

    @Accessor
    float getGCol();

    @Accessor
    float getBCol();

    @Accessor
    float getAlpha();

    @Accessor
    float getRoll();

    @Accessor
    float getORoll();

    @Accessor
    float getFriction();

    @Accessor
    boolean getSpeedUpWhenYMotionIsBlocked();

    @Accessor
    void setLevel(ClientLevel level);

    @Accessor
    void setXo(double xo);

    @Accessor
    void setYo(double yo);

    @Accessor
    void setZo(double zo);

    @Accessor
    void setX(double x);

    @Accessor
    void setY(double y);

    @Accessor
    void setZ(double z);

    @Accessor
    void setXd(double xd);

    @Accessor
    void setYd(double yd);

    @Accessor
    void setZd(double zd);

    @Accessor
    void setBb(AABB bb);

    @Accessor
    void setOnGround(boolean onGround);

    @Accessor
    void setHasPhysics(boolean hasPhysics);

    @Accessor
    void setStoppedByCollision(boolean stoppedByCollision);

    @Accessor
    void setRemoved(boolean removed);

    @Accessor
    void setBbWidth(float bbWidth);

    @Accessor
    void setBbHeight(float bbHeight);

    @Accessor
    void setRandom(RandomSource random);

    @Accessor
    void setAge(int age);

    @Accessor
    void setLifetime(int lifetime);

    @Accessor
    void setGravity(float gravity);

    @Accessor
    void setRCol(float rCol);

    @Accessor
    void setGCol(float gCol);

    @Accessor
    void setBCol(float bCol);

    @Accessor
    void setAlpha(float alpha);

    @Accessor
    void setRoll(float roll);

    @Accessor
    void setORoll(float oRoll);

    @Accessor
    void setFriction(float friction);

    @Accessor
    void setSpeedUpWhenYMotionIsBlocked(boolean speedUpWhenYMotionIsBlocked);

}
