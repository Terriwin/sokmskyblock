package org.terriwin.sokmskyblock;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class SkyblockSavedData extends SavedData {

    private static final String DATA_NAME = "sokmskyblock_data";

    private boolean islandPlaced = false;
    private int restartCount = 0;

    public SkyblockSavedData() {}

    private SkyblockSavedData(CompoundTag tag) {
        this.islandPlaced = tag.getBoolean("islandPlaced");
        this.restartCount = tag.getInt("restartCount");
    }

    public static SkyblockSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        SkyblockSavedData::new,
                        (nbt, provider) -> new SkyblockSavedData(nbt),
                        DataFixTypes.LEVEL
                ),
                DATA_NAME
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("islandPlaced", this.islandPlaced);
        tag.putInt("restartCount", this.restartCount);
        return tag;
    }

    public boolean isIslandPlaced() {
        return islandPlaced;
    }

    public void setIslandPlaced(boolean placed) {
        this.islandPlaced = placed;
        setDirty();
    }

    public int getRestartCount() {
        return restartCount;
    }

    public void incrementRestartCount() {
        this.restartCount++;
        setDirty();
    }
}
