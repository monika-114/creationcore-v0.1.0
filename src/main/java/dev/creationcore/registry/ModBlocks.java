package dev.creationcore.registry;

import dev.creationcore.CreativeCoreMod;
import dev.creationcore.block.BaseMatterBlock;
import dev.creationcore.block.CreativeCraftingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreativeCoreMod.MODID);

    public static final DeferredBlock<BaseMatterBlock> BASE_MATTER = BLOCKS.registerBlock(
            "base_matter",
            BaseMatterBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(4.5F, 3_600_000F)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()
                    .noCollission()
    );

    public static final DeferredBlock<CreativeCraftingTableBlock> CREATIVE_CRAFTING_TABLE = BLOCKS.registerBlock(
            "creative_crafting_table",
            CreativeCraftingTableBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.5F, 12.0F)
                    .sound(SoundType.WOOD)
    );

    private ModBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
