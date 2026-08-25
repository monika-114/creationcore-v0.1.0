package dev.creationcore.registry;

import dev.creationcore.CreativeCoreMod;
import dev.creationcore.entity.CreativeCoreEntity;
import dev.creationcore.entity.VoidBucketEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, CreativeCoreMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CreativeCoreEntity>> CREATIVE_CORE = ENTITIES.register(
            "creative_core",
            () -> EntityType.Builder.<CreativeCoreEntity>of(CreativeCoreEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(CreativeCoreMod.MODID + ":creative_core")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<VoidBucketEntity>> VOID_BUCKET_RETURN = ENTITIES.register(
            "void_bucket_return",
            () -> EntityType.Builder.<VoidBucketEntity>of(VoidBucketEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build(CreativeCoreMod.MODID + ":void_bucket_return")
    );

    private ModEntities() {}

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
