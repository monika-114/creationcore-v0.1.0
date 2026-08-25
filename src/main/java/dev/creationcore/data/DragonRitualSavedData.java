package dev.creationcore.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class DragonRitualSavedData extends SavedData {
    private static final String FILE_NAME = "creationcore_dragon_ritual";
    private static final String PENDING = "pending";
    private static final String ORIGIN_X = "origin_x";
    private static final String ORIGIN_Z = "origin_z";

    private boolean pending;
    private int originX;
    private int originZ;

    public static DragonRitualSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(DragonRitualSavedData::new, DragonRitualSavedData::load),
                FILE_NAME
        );
    }

    private static DragonRitualSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DragonRitualSavedData data = new DragonRitualSavedData();
        data.pending = tag.getBoolean(PENDING);
        data.originX = tag.getInt(ORIGIN_X);
        data.originZ = tag.getInt(ORIGIN_Z);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(PENDING, pending);
        tag.putInt(ORIGIN_X, originX);
        tag.putInt(ORIGIN_Z, originZ);
        return tag;
    }

    public void begin(BlockPos origin) {
        pending = true;
        originX = origin.getX();
        originZ = origin.getZ();
        setDirty();
    }

    public boolean isPending() {
        return pending;
    }

    public BlockPos originAt(int y) {
        return new BlockPos(originX, y, originZ);
    }

    public void complete() {
        pending = false;
        setDirty();
    }
}
