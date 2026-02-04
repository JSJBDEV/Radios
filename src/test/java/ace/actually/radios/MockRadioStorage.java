package ace.actually.radios;

import ace.actually.radios.impl.RadioStorage;
import ace.actually.radios.impl.RadioTransmitterModel;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of RadioStorage for unit testing.
 * Does not require MinecraftServer or NBT serialization.
 */
public class MockRadioStorage implements RadioStorage {

    private final List<RadioTransmitterModel> transmitters = new ArrayList<>();
    private final Map<String, BlockPos> dimensionLocations = new HashMap<>();

    @Override
    public List<RadioTransmitterModel> getTransmitters() {
        return transmitters;
    }

    @Override
    public RadioTransmitterModel findTransmitterAt(String dimension, BlockPos pos) {
        for (RadioTransmitterModel transmitter : transmitters) {
            if (transmitter.matches(dimension, pos)) {
                return transmitter;
            }
        }
        return null;
    }

    @Override
    public void addTransmitter(RadioTransmitterModel transmitter) {
        transmitters.add(transmitter);
    }

    @Override
    public BlockPos getDimensionLocation(String dimension) {
        return dimensionLocations.get(dimension);
    }

    @Override
    public Map<String, BlockPos> getDimensionLocations() {
        return dimensionLocations;
    }

    @Override
    public void setDimensionLocation(String dimension, BlockPos pos) {
        dimensionLocations.put(dimension, pos);
    }

    @Override
    public boolean hasDimensionLocation(String dimension) {
        return dimensionLocations.containsKey(dimension);
    }

    /**
     * Clear all data - useful for test isolation
     */
    public void clear() {
        transmitters.clear();
        dimensionLocations.clear();
    }
}
