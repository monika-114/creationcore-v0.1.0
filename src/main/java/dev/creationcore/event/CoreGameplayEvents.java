package dev.creationcore.event;

import dev.creationcore.CreativeCoreMod;
import dev.creationcore.entity.CreativeCoreEntity;
import dev.creationcore.entity.VoidBucketEntity;
import dev.creationcore.data.DragonRitualSavedData;
import dev.creationcore.registry.ModBlocks;
import dev.creationcore.registry.ModItems;
import dev.creationcore.registry.ModEntities;
import dev.creationcore.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Collection;

@EventBusSubscriber(modid = CreativeCoreMod.MODID)
public final class CoreGameplayEvents {
    private static final String WITHER_CHARGED = "creationcore_wither_charged";
    private static final String VOID_THRESHOLD = "creationcore_void_threshold";
    private static final String VOID_RETURN_X = "creationcore_void_return_x";
    private static final String VOID_RETURN_Y = "creationcore_void_return_y";
    private static final String VOID_RETURN_Z = "creationcore_void_return_z";
    private static final String VOID_RETURNING = "creationcore_void_returning";
    private static final String VOID_DELAY = "creationcore_void_delay";

    private CoreGameplayEvents() {}

    @SubscribeEvent
    public static void onWitherBirthExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (!(event.getExplosion().getDirectSourceEntity() instanceof WitherBoss wither)) return;
        if (wither.getPersistentData().getBoolean(WITHER_CHARGED)) return;

        boolean consumedAny = false;
        for (Entity entity : new ArrayList<>(event.getAffectedEntities())) {
            if (entity instanceof ItemEntity item && item.getItem().is(ModItems.BLANK_MATTER.get())) {
                item.discard(); // consume the entire ItemEntity stack
                consumedAny = true;
            }
        }

        if (consumedAny) {
            wither.getPersistentData().putBoolean(WITHER_CHARGED, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWitherDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof WitherBoss wither)) return;
        if (!wither.getPersistentData().getBoolean(WITHER_CHARGED)) return;

        Collection<ItemEntity> drops = event.getDrops();
        // Strip vanilla stars and any Base Matter inserted into the mutable loot collection.
        // The ritual result is spawned directly at LOWEST priority so Looting/drop multipliers
        // which operate on LivingDropsEvent cannot scale the Creative Core progression item.
        drops.removeIf(drop -> drop.getItem().is(Items.NETHER_STAR) || drop.getItem().is(ModItems.BASE_MATTER.get()));
        if (wither.level() instanceof ServerLevel level) {
            ItemEntity baseMatter = new ItemEntity(level, wither.getX(), wither.getY(), wither.getZ(),
                    new ItemStack(ModItems.BASE_MATTER.get()));
            baseMatter.setDefaultPickUpDelay();
            level.addFreshEntity(baseMatter);
        }
    }

    @SubscribeEvent
    public static void onDragonDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        if (!(dragon.level() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.END)) return;

        BlockPos origin = dragon.getFightOrigin();
        int consumed = 0;
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            BlockPos pos = new BlockPos(origin.getX(), y, origin.getZ());
            if (level.getBlockState(pos).is(ModBlocks.BASE_MATTER.get())) {
                level.removeBlock(pos, false);
                consumed++;
            }
        }

        if (consumed > 0) {
            // Persist the pending result. The actual Creative Matter is created only after
            // the return portal has appeared, so the ritual survives a save/reload between stages.
            DragonRitualSavedData.get(level).begin(origin);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.END)) return;

        DragonRitualSavedData data = DragonRitualSavedData.get(level);
        if (!data.isPending()) return;

        BlockPos portal = findExitPortal(level, data);
        if (portal == null) return;

        // Clear the persisted pending flag before spawning the output. In the unlikely event of
        // a crash at this exact point, this favors never duplicating Creative Matter.
        data.complete();
        ItemEntity result = new ItemEntity(level, portal.getX() + 0.5, portal.getY() + 4.0, portal.getZ() + 0.5,
                new ItemStack(ModItems.CREATIVE_MATTER.get()));
        result.setDefaultPickUpDelay();
        result.setDeltaMovement(0, 0.08, 0);
        level.addFreshEntity(result);
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (!(item.level() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.END) || !event.getDimension().equals(Level.OVERWORLD)) return;
        if (!isTouchingEndPortal(level, item)) return;
        if (!isValidCreativeMatterShulker(item.getItem())) return;

        // Cancel the normal item teleport. The entire shulker box (and its one Creative Matter)
        // is the ritual cost; exactly one persistent Creative Core is created at world spawn.
        event.setCanceled(true);
        spawnCreativeCoreAtWorldSpawn(level.getServer());
        item.discard();
    }

    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (!(item.level() instanceof ServerLevel level)) return;
        if (item instanceof CreativeCoreEntity) return;

        CompoundTag data = item.getPersistentData();
        if (data.getBoolean(VOID_RETURNING)) {
            tickReturningVoidBucket(item, data);
            return;
        }

        if (!item.getItem().is(Items.BUCKET)) return;
        if (!isSupportedVoidFishingDimension(level)) return;

        // The first observed position is the fallback return point. While the bucket is still
        // resting on normal terrain, keep refreshing it; once it leaves the ledge, this freezes
        // the last clearly non-void position instead of following the bucket all the way down.
        if (!data.contains(VOID_RETURN_Y)) {
            rememberVoidReturnPosition(item, data);
        }
        if (item.onGround()) {
            rememberVoidReturnPosition(item, data);
        }

        if (!data.contains(VOID_THRESHOLD)) {
            int jitter = level.random.nextInt(11) - 5;
            int threshold = level.dimension().equals(Level.END)
                    ? -40 + jitter
                    : level.getMinBuildHeight() - 10 + jitter;
            data.putInt(VOID_THRESHOLD, threshold);
        }

        int threshold = data.getInt(VOID_THRESHOLD);
        if (item.getY() <= threshold) {
            double targetY = data.getDouble(VOID_RETURN_Y);
            // Rise along the actual void-entry column rather than underneath the ledge from which
            // the bucket was thrown. Only the remembered safe Y is used as the return height.
            double returnX = item.getX();
            double returnZ = item.getZ();
            int bucketCount = item.getItem().getCount();

            // A dedicated temporary ItemEntity subtype is required here: in the Overworld/Nether
            // this spawn point is intentionally below the vanilla world floor and a normal item
            // entity would be discarded before it could rise. Collision is otherwise normal.
            VoidBucketEntity returning = new VoidBucketEntity(ModEntities.VOID_BUCKET_RETURN.get(), level);
            returning.setPos(returnX, threshold - 8.0, returnZ);
            returning.setItem(new ItemStack(ModItems.VOID_BUCKET.get(), bucketCount));
            CompoundTag returningData = returning.getPersistentData();
            returningData.putBoolean(VOID_RETURNING, true);
            returningData.putDouble(VOID_RETURN_X, returnX);
            returningData.putDouble(VOID_RETURN_Y, targetY);
            returningData.putDouble(VOID_RETURN_Z, returnZ);
            returningData.putInt(VOID_DELAY, 60);
            returning.setNoGravity(true);
            returning.setNeverPickUp();
            returning.setUnlimitedLifetime();
            returning.setDeltaMovement(0, 0, 0);
            level.addFreshEntity(returning);
            item.discard();
        }
    }


    private static boolean isSupportedVoidFishingDimension(ServerLevel level) {
        return level.dimension().equals(Level.END)
                || level.dimension().equals(Level.OVERWORLD)
                || level.dimension().equals(Level.NETHER);
    }

    private static void rememberVoidReturnPosition(ItemEntity item, CompoundTag data) {
        data.putDouble(VOID_RETURN_X, item.getX());
        data.putDouble(VOID_RETURN_Y, item.getY());
        data.putDouble(VOID_RETURN_Z, item.getZ());
    }

    private static void tickReturningVoidBucket(ItemEntity item, CompoundTag data) {
        item.setNoGravity(true);
        item.setNeverPickUp();
        item.setUnlimitedLifetime();

        int delay = data.getInt(VOID_DELAY);
        if (delay > 0) {
            data.putInt(VOID_DELAY, delay - 1);
            item.setDeltaMovement(0, 0, 0);
            return;
        }

        double targetY = data.getDouble(VOID_RETURN_Y);
        if (item.getY() < targetY - 0.25) {
            // Normal ItemEntity collision remains active. A block above can stop the ascent.
            item.setDeltaMovement(0, 0.075, 0);
        } else {
            // Surface as a normal ItemEntity again. This restores ordinary lifetime, pickup,
            // collision and below-world behavior after the special ascent has finished.
            if (item.level() instanceof ServerLevel level) {
                ItemEntity surfaced = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), item.getItem().copy());
                surfaced.setDefaultPickUpDelay();
                surfaced.setDeltaMovement(0, 0, 0);
                level.addFreshEntity(surfaced);
                item.discard();
            }
        }
    }

    private static BlockPos findExitPortal(ServerLevel level, DragonRitualSavedData data) {
        // Search a small column around the fight origin. This does not assume vanilla's exact Y,
        // while still requiring a real END_PORTAL block to exist before the ritual finishes.
        BlockPos center = data.originAt(0);
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isTouchingEndPortal(ServerLevel level, ItemEntity item) {
        BlockPos pos = item.blockPosition();
        return level.getBlockState(pos).is(Blocks.END_PORTAL)
                || level.getBlockState(pos.below()).is(Blocks.END_PORTAL);
    }

    private static boolean isValidCreativeMatterShulker(ItemStack stack) {
        if (!stack.is(ModTags.CREATIVE_CORE_CONTAINERS)) return false;
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        contents.copyInto(items);

        int nonEmpty = 0;
        for (ItemStack inside : items) {
            if (inside.isEmpty()) continue;
            nonEmpty++;
            if (!inside.is(ModItems.CREATIVE_MATTER.get()) || inside.getCount() != 1) return false;
        }
        return nonEmpty == 1;
    }

    private static void spawnCreativeCoreAtWorldSpawn(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        CreativeCoreEntity core = new CreativeCoreEntity(overworld, spawn.getX() + 0.5, y + 2.5, spawn.getZ() + 0.5);
        overworld.addFreshEntity(core);
    }
}
