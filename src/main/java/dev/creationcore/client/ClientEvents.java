package dev.creationcore.client;

import dev.creationcore.CreativeCoreMod;
import dev.creationcore.registry.ModEntities;
import dev.creationcore.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = CreativeCoreMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CREATIVE_CRAFTING.get(), CreativeCraftingScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CREATIVE_CORE.get(), CreativeCoreEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.VOID_BUCKET_RETURN.get(), VoidBucketEntityRenderer::new);
    }
}
