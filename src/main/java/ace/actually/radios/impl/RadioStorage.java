package ace.actually.radios.impl;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

/**
 * Interface for radio data storage operations.
 * Abstracts persistence to enable testing without MinecraftServer.
 */
public interface RadioStorage {

    /**
     * Get all transmitters
     */
    List<RadioTransmitterModel> getTransmitters();

    /**
     * Find a transmitter at a specific location, or null if not found
     */
    RadioTransmitterModel findTransmitterAt(String dimension, BlockPos pos);

    /**
     * Add a new transmitter
     */
    void addTransmitter(RadioTransmitterModel transmitter);

    /**
     * Get dimension location, or null if not registered
     */
    BlockPos getDimensionLocation(String dimension);

    /**
     * Get all dimension locations
     */
    Map<String, BlockPos> getDimensionLocations();

    /**
     * Set a dimension's physical location
     */
    void setDimensionLocation(String dimension, BlockPos pos);

    /**
     * Check if a dimension location is registered
     */
    boolean hasDimensionLocation(String dimension);
}
