package ace.actually.radios.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistence coordinator between Minecraft storage and domain models.
 * Owns all interaction with MinecraftServer and CommandStorage.
 * Delegates per-object serialization to model classes.
 */
public class RadioStorage {
    private static final ResourceLocation RADIO_SAVE = ResourceLocation.fromNamespaceAndPath("radios", "radios");

    private final List<RadioTransmitterModel> transmitters;
    private final Map<String, BlockPos> dimensionLocations;

    private static final Logger LOGGER = LoggerFactory.getLogger("Radio Storage");

    public RadioStorage() {
        this.transmitters = new ArrayList<>();
        this.dimensionLocations = new HashMap<>();
    }

    /**
     * Load radio data from server storage
     */
    public void load(MinecraftServer server) {
        CompoundTag savedRadios = server.getCommandStorage().get(RADIO_SAVE);

        // Load transmitters
        ListTag radiosList = savedRadios.getList("radios", ListTag.TAG_COMPOUND);
        transmitters.clear();
        for (int i = 0; i < radiosList.size(); i++) {
            CompoundTag radioTag = radiosList.getCompound(i);
            transmitters.add(RadioTransmitterModel.fromNBT(radioTag));
        }

        // Load dimension locations
        CompoundTag dimensionsTag = savedRadios.getCompound("dimensions");
        dimensionLocations.clear();
        for (String key : dimensionsTag.getAllKeys()) {
            int[] posArray = dimensionsTag.getIntArray(key);
            dimensionLocations.put(key, new BlockPos(posArray[0], posArray[1], posArray[2]));
        }
    }

    /**
     * Save radio data to server storage
     */
    public void save(MinecraftServer server) {
        CompoundTag savedRadios = new CompoundTag();

        // Save transmitters
        ListTag radiosList = new ListTag();
        for (RadioTransmitterModel transmitter : transmitters) {
            radiosList.add(transmitter.toNBT());
        }
        savedRadios.put("radios", radiosList);

        // Save dimension locations
        CompoundTag dimensionsTag = new CompoundTag();
        for (Map.Entry<String, BlockPos> entry : dimensionLocations.entrySet()) {
            BlockPos pos = entry.getValue();
            dimensionsTag.putIntArray(entry.getKey(), new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
        savedRadios.put("dimensions", dimensionsTag);

        server.getCommandStorage().set(RADIO_SAVE, savedRadios);
    }

    /**
     * Get all transmitters
     */
    public List<RadioTransmitterModel> getTransmitters() {
        return transmitters;
    }

    /**
     * Find a transmitter at a specific location, or null if not found
     */
    public RadioTransmitterModel findTransmitterAt(String dimension, BlockPos pos) {
        for (RadioTransmitterModel transmitter : transmitters) {
            if (transmitter.matches(dimension, pos)) {
                return transmitter;
            }
        }
        return null;
    }

    /**
     * Add a new transmitter
     */
    public void addTransmitter(RadioTransmitterModel transmitter) {
        transmitters.add(transmitter);
    }

    /**
     * Get dimension location, or null if not registered
     */
    public BlockPos getDimensionLocation(String dimension) {
        return dimensionLocations.get(dimension);
    }

    /**
     * Get all dimension locations
     */
    public Map<String, BlockPos> getDimensionLocations() {
        return dimensionLocations;
    }

    /**
     * Set a dimension's physical location
     */
    public void setDimensionLocation(String dimension, BlockPos pos) {
        dimensionLocations.put(dimension, pos);
    }

    /**
     * Check if a dimension location is registered
     */
    public boolean hasDimensionLocation(String dimension) {
        return dimensionLocations.containsKey(dimension);
    }
}
