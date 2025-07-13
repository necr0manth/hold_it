package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dsai03.hold_it.content.entities.SwordEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class SwordEntityRenderer extends EntityRenderer<SwordEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("hold_it", "textures/entity/sword.png");

    public SwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SwordEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Масштабируем меч в зависимости от его силы
        float scale = entity.getPower();
        poseStack.scale(scale, scale, scale);

        // Поворачиваем меч в направлении движения энтити
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 0.001) {
            // Вычисляем углы на основе вектора движения
            float yaw = (float) Math.toDegrees(Math.atan2(-motion.x, motion.z));
            float pitch = (float) Math.toDegrees(-Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z)));

            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        } else {
            // Если энтити не движется, используем его текущий поворот
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        }

        // Поворачиваем меч так, чтобы острие смотрело вперед
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));

        // Создаем ItemStack меча для рендеринга
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);

        // Рендерим меч как предмет
        Minecraft.getInstance().getItemRenderer().renderStatic(
            swordStack,
            ItemDisplayContext.NONE,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            entity.level(),
            entity.getId()
        );

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SwordEntity entity) {
        return TEXTURE;
    }
}
