package dev.dsai03.hold_it.init.client;

import dev.dsai03.hold_it.content.client.entity_renderers.BallEntityRenderer;
import dev.dsai03.hold_it.content.client.entity_renderers.BigBallEntityRenderer;
import dev.dsai03.hold_it.content.client.entity_renderers.EmptyEntityRenderer;
import dev.dsai03.hold_it.content.client.entity_renderers.PortalEntityRenderer;
import dev.dsai03.hold_it.content.client.entity_renderers.SwordEntityRenderer;
import dev.dsai03.hold_it.init.AwesomeEntityTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AwesomeEntityRenderers {
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerEntityRenderingHandlers(FMLClientSetupEvent event) {
		EntityRenderers.register(AwesomeEntityTypes.COOL_SHAPE.get(), EmptyEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.AWESOME_SHAPE.get(), EmptyEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.BALL_ENTITY_TYPE.get(), BallEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.SPHERE_ENTITY_TYPE.get(), EmptyEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.SEVEN_SHAPE.get(), EmptyEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.BIG_BALL_SPELL_SHAPE.get(), EmptyEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.BIG_BALL_ENTITY_TYPE.get(), BigBallEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.PORTAL_ENTITY_TYPE.get(), PortalEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.PORTAL_SWORD_SHAPE.get(), EmptyEntityRenderer::new);
		EntityRenderers.register(AwesomeEntityTypes.SWORD_ENTITY_TYPE.get(), SwordEntityRenderer::new);
	}
}
