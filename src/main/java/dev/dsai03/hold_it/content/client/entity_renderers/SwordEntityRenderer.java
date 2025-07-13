package dev.dsai03.hold_it.content.client.entity_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dsai03.hold_it.MyAwesomeMnaAddon;
import dev.dsai03.hold_it.content.entities.SwordEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SwordEntityRenderer extends EntityRenderer<SwordEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("hold_it", "textures/entity/sword.png");

    public SwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SwordEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        poseStack.translate(0.5, 0.25, -0.5);


        poseStack.mulPose(Axis.XP.rotationDegrees(45));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));

        var model = Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(MyAwesomeMnaAddon.MODID, "entity/sword_entity"));

        // Render the model using the ItemRenderer's renderModel method
        ItemStack dummyStack = new ItemStack(Items.IRON_SWORD); // Used for rendering properties
        Minecraft.getInstance().getItemRenderer().renderModelLists(
                model,
                dummyStack,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer.getBuffer(RenderType.cutout())
        );

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
