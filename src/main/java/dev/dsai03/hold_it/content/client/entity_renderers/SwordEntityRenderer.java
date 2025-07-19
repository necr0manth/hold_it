package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.entities.SwordEntity;
import dev.dsai03.hold_it.util.AffinityDistribution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SwordEntityRenderer extends EntityRenderer<SwordEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("hold_it", "textures/entity/sword.png");

    public SwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SwordEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var baseColor = new Color(0, 0, 0);
        for (var affinity : AffinityDistribution.fromSpell(entity.getSpell()).normalized().asMap().entrySet()) {
            baseColor = new Color(
                    MathUtils.clamp01((baseColor.getRed() + affinity.getKey().getColor()[0] * affinity.getValue()) / 255f),
                    MathUtils.clamp01((baseColor.getGreen() + affinity.getKey().getColor()[1] * affinity.getValue()) / 255f),
                    MathUtils.clamp01((baseColor.getBlue() + affinity.getKey().getColor()[2] * affinity.getValue()) / 255f));
        }
        var color = new Color(entity.getSpell().getParticleColorOverride());
        if (color.equals(Color.WHITE))
            color = baseColor;
        var baseAlpha = 1f;
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        poseStack.translate(0.5, 0.25, -0.5);


        poseStack.mulPose(Axis.XP.rotationDegrees(45));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        var model = Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(MyAwesomeMnaAddon.MODID, "entity/sword_entity"));
        for (var rendertype : new RenderType[]{RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS)}) {
            var vertexConsumer = buffer.getBuffer(rendertype);
            for (var quad : model.getQuads(null, null, RandomSource.create())) {
                vertexConsumer.putBulkData(poseStack.last(), quad, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, baseAlpha * entity.getAlphaPercentage(), packedLight, OverlayTexture.NO_OVERLAY, true);
            }
        }
//        // Render the model using the ItemRenderer's renderModel method
//        ItemStack dummyStack = new ItemStack(Items.IRON_SWORD); // Used for rendering properties
//        Minecraft.getInstance().getItemRenderer().renderModelLists(
//                model,
//                dummyStack,
//                packedLight,
//                OverlayTexture.NO_OVERLAY,
//                poseStack,
//                buffer.getBuffer(RenderType.translucent())
//        );

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SwordEntity entity) {
        return TEXTURE;
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(MyAwesomeMnaAddon.MODID, "entity/sword_entity"));
    }
}
