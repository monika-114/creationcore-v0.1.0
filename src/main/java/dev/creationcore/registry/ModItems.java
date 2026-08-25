package dev.creationcore.registry;

import dev.creationcore.CreativeCoreMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreativeCoreMod.MODID);

    public static final DeferredItem<Item> BLANK_MATTER = ITEMS.registerSimpleItem("blank_matter", new Item.Properties());

    public static final DeferredItem<BlockItem> BASE_MATTER = ITEMS.register("base_matter",
            () -> new BlockItem(ModBlocks.BASE_MATTER.get(), new Item.Properties()));

    public static final DeferredItem<Item> CREATIVE_MATTER = ITEMS.registerSimpleItem("creative_matter", new Item.Properties());
    public static final DeferredItem<Item> CREATIVE_CORE = ITEMS.registerSimpleItem("creative_core", new Item.Properties().stacksTo(16));

    // Intentionally a plain Item rather than BucketItem: it has no liquid/cauldron behavior.
    public static final DeferredItem<Item> VOID_BUCKET = ITEMS.registerSimpleItem("void_bucket", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> BOTTLED_NOTHING = ITEMS.registerSimpleItem("bottled_nothing", new Item.Properties());

    public static final DeferredItem<BlockItem> CREATIVE_CRAFTING_TABLE = ITEMS.register("creative_crafting_table",
            () -> new BlockItem(ModBlocks.CREATIVE_CRAFTING_TABLE.get(), new Item.Properties()));

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
