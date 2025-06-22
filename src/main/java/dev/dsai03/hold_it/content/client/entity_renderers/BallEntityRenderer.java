package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mna.api.affinity.Affinity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.dsai03.hold_it.content.client.particles.LightningBall;
import dev.dsai03.hold_it.content.entities.BallEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class BallEntityRenderer extends EntityRenderer<BallEntity> {
    public BallEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    private final WeakHashMap<BallEntity, WeakReference<LightningBall>> balls = new WeakHashMap<>();

    @Override
    public ResourceLocation getTextureLocation(BallEntity pEntity) {
        return null;
    }

    @Override
    public void render(BallEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pEntity.calculateRenderBallData();
        var affinities = pEntity.spell.getAffinities();
        if (affinities != null && affinities[Affinity.LIGHTNING.ordinal()] != 0) {
            if (!balls.containsKey(pEntity) || balls.get(pEntity) == null || balls.get(pEntity).get() == null) {
                var baseColor = new Color(100, 67, 255);
                var ball = new LightningBall(Minecraft.getInstance().level, 10, baseColor, new Color(255, 67, 255), pEntity::getRenderRadius, pEntity::getRenderPosition);
                ball.spawn(0.3f);
                balls.put(pEntity, new WeakReference<>(ball));
            }
        }
    }
}
