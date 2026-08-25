package dev.creationcore.registry;

import dev.creationcore.CreativeCoreMod;
import dev.creationcore.menu.CreativeCraftingMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CreativeCoreMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CreativeCraftingMenu>> CREATIVE_CRAFTING =
            MENUS.register("creative_crafting", () -> new MenuType<>(CreativeCraftingMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
