package dev.creationcore.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Temporary carrier used only while an “empty” bucket is rising back from below the world.
 * A normal ItemEntity would be discarded by vanilla's below-world check before it could rise
 * from the Overworld/Nether trigger depth. Once it reaches its target height, gameplay code
 * replaces this entity with an ordinary ItemEntity again.
 */
public final class VoidBucketEntity extends ItemEntity {
    public VoidBucketEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void onBelowWorld() {
        // Deliberately survive below the world's minimum while being reeled back from the void.
    }
}
