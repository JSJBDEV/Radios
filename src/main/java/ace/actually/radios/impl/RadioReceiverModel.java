package ace.actually.radios.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * Represents a persistent reference to a radio receiver.
 * Responsible for its own NBT serialization/deserialization.
 */
public class RadioReceiverModel {
    private String dimension;
    private BlockPos pos;

    public RadioReceiverModel(String dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    public String getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    /**
     * Deserializes a receiver from NBT
     */
    public static RadioReceiverModel fromNBT(CompoundTag tag) {
        String dimension = tag.getString("dimension");
        int[] posArray = tag.getIntArray("pos");
        BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        return new RadioReceiverModel(dimension, pos);
    }

    /**
     * Serializes this receiver to NBT
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension);
        tag.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        return tag;
    }

    /**
     * Checks if this receiver matches a given dimension and position
     */
    public boolean matches(String dimension, BlockPos pos) {
        return this.dimension.equals(dimension) &&
               this.pos.getX() == pos.getX() &&
               this.pos.getY() == pos.getY() &&
               this.pos.getZ() == pos.getZ();
    }
}
