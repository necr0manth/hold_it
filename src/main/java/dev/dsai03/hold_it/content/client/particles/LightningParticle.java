package dev.dsai03.hold_it.content.client.particles;

import com.mna.api.particles.ParticleInit;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dsai03.hold_it.mixins.client.ParticleEngineAccessor;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class LightningParticle extends BaseParticle<LightningParticle> implements IColoredParticle {
    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    public interface ILightningDataFactory {
        List<Vec3> create(Vec3 start, Vec3 end);
    }

    public ILightningDataFactory lightningBuilder = (start, end) -> ParticleUtils.getJagged(start, end, 3, 0.3);
    public float quadSize;
    public Color color;
    private Vec3 startPos = Vec3.ZERO;
    public Vec3 relativeEndPos = Vec3.ZERO;
    private final Random random = new Random();
    private List<Vec3> points;
    private float lastLength = -1;
    @Getter
    private long seed = random.nextLong();

    public void regenerate() {
        points = lightningBuilder.create(startPos, getEndPos());
    }

    public LightningParticle(Color color, float quadSize) {
        this.color = color;
        this.quadSize = quadSize;
    }

    @Override
    public void setPos(Vec3 pos) {
        startPos = pos;
    }

    public void setEndPos(Vec3 pos) {
        relativeEndPos = pos.subtract(startPos);
    }

    public Vec3 getEndPos() {
        return getPos().add(relativeEndPos);
    }

    @Override
    public Vec3 getPos() {
        return startPos;
    }

    @Override
    public void setSpeed(Vec3 speed) {
        throw new RuntimeException(">_<");
    }

    @Override
    public Vec3 getSpeed() {
        return Vec3.ZERO;
    }

    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        var length = (float) relativeEndPos.length();

        if (lastLength == -1 || Math.abs(length - lastLength) > 0.01) {
            regenerate();
            lastLength = length;
        } else {
            var lastStartPos = points.get(0);
            var lastRelativeEnd = points.get(points.size() - 1).subtract(lastStartPos);
            var r = lastRelativeEnd.toVector3f().rotationTo(relativeEndPos.toVector3f(), new Quaternionf());
            points = points.stream().map(v -> startPos.add(new Vec3(v.subtract(lastStartPos).toVector3f().rotate(r)))).toList();
        }

        var spriteSet = ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).getSpriteSets().get(ParticleInit.LIGHTNING_BOLT.getId());
        var sprite = spriteSet.get(Mth.floor(20 * Math.min(lifetime, maxLifetime)), Mth.floor(20 * maxLifetime));
        Vector3 posOffset = new Vector3(getPos().subtract(renderInfo.getPosition()));
        Vector3 particleOrigin = new Vector3(getPos());
        int count = 0;
        int maxIndex = points.size();
        Vector3 lastEnd1 = null;
        Vector3 lastEnd2 = null;
        quadSize = 1;
        for (int i = 1; i < points.size(); i++) {
            if (count > maxIndex) {
                break;
            }

            float width = Math.min(0.05F * length, quadSize);
            Vector3 start = new Vector3(points.get(i - 1)).sub(particleOrigin);
            Vector3 end = new Vector3(points.get(i)).sub(particleOrigin);
            Vector3 dir = end.sub(start).normalize().scale(length * 3.0E-4F);
            Vector3f[] avector3f = new Vector3f[]{(lastEnd1 == null ? start.add(new Vector3((-width), 0.0F, (-width))) : lastEnd1.sub(dir)).toVector3f(), (lastEnd2 == null ? start.add(new Vector3((-width), 0.0F, width)) : lastEnd2.sub(dir)).toVector3f(), end.add(new Vector3(width, 0.0F, width)).toVector3f(), end.add(new Vector3(width, 0.0F, (-width))).toVector3f()};
            lastEnd1 = new Vector3(avector3f[2].x(), avector3f[2].y(), avector3f[2].z());
            lastEnd2 = new Vector3(avector3f[3].x(), avector3f[3].y(), avector3f[3].z());

            for (int j = 0; j < 4; ++j) {
                avector3f[j].add(posOffset.x, posOffset.y, posOffset.z);
            }

            float minU = sprite.getU0();
            float maxU = sprite.getU1();
            float minV = sprite.getV0();
            float maxV = sprite.getV1();
            int j = 15728880;
            buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(maxU, maxV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(maxU, minV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(minU, minV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(minU, maxV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(maxU, maxV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(maxU, minV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(minU, minV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(minU, maxV).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f).uv2(j).endVertex();
            ++count;
        }
    }

}
