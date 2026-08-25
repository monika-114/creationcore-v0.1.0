package dev.creationcore;

import dev.creationcore.registry.ModBlocks;
import dev.creationcore.registry.ModItems;
import dev.creationcore.registry.ModEntities;
import dev.creationcore.registry.ModMenus;
import dev.creationcore.registry.ModRecipes;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(CreativeCoreMod.MODID)
public final class CreativeCoreMod {
    public static final String MODID = "creationcore";

    public CreativeCoreMod(IEventBus modBus) {
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModMenus.register(modBus);
        ModRecipes.register(modBus);
        modBus.addListener(this::addCreativeTabContents);
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BLANK_MATTER.get());
            event.accept(ModItems.BASE_MATTER.get());
            event.accept(ModItems.CREATIVE_MATTER.get());
            event.accept(ModItems.CREATIVE_CORE.get());
            event.accept(ModItems.VOID_BUCKET.get());
            event.accept(ModItems.BOTTLED_NOTHING.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.CREATIVE_CRAFTING_TABLE.get());
        }
    }
}
