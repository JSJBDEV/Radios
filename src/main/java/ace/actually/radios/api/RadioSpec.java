package ace.actually.radios.api;

import ace.actually.radios.impl.RadioReceiverModel;
import ace.actually.radios.impl.RadioStorage;
import ace.actually.radios.impl.RadioTransmitterModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Pure radio logic layer.
 * Operates on domain models without touching NBT or persistence directly.
 */
public class RadioSpec {

    private static RadioStorage storage;

    /**
     * Initialize RadioSpec with a storage implementation.
     * This allows injection of mock storage for testing.
     */
    public static void initialize(RadioStorage storageImpl) {
        storage = storageImpl;
    }

    public static List<Consumer<StaticRadioAction>> ACTIONS = new ArrayList<>();

    private static final int CROSS_DIMENSION_MULTIPLIER = 16;

    public static void registerDimensionPhysicalLocation(ServerLevel level, int x, int y, int z) {
        storage.setDimensionLocation(level.dimension().location().toString(), new BlockPos(x, y, z));
    }

    /**
     * Start transmitting a message from a position on a band
     * this method also checks for StaticRadioActions that would be triggered by this message
     * @param level the dimension
     * @param pos the radio transmitter's physical location
     * @param band qed
     * @param message qed
     * @return the index in RADIOS of this radio
     */
    public static int transmit(ServerLevel level, BlockPos pos, int band, String message, String passphrase) {
        String dimensionString = level.dimension().location().toString();

        // Try to find existing transmitter at this location
        RadioTransmitterModel existingTransmitter = storage.findTransmitterAt(dimensionString, pos);

        if (existingTransmitter != null) {
            // Update existing transmitter
            existingTransmitter.setMessage(message);
            existingTransmitter.setPassphrase(passphrase);
            existingTransmitter.setBand(band);

            // Notify all subscribers
            for (RadioReceiverModel subscriber : existingTransmitter.getSubscribers()) {
                ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(subscriber.getDimension()));
                ServerLevel dimension = level.getServer().getLevel(dimKey);
                if (dimension != null) {
                    notifySubscriber(dimension, subscriber.getPos(), message);
                }
            }

            runStaticRadioActions(level, pos, band, message);

            // Return the index of this transmitter
            return storage.getTransmitters().indexOf(existingTransmitter);
        } else {
            // Create new transmitter
            RadioTransmitterModel newTransmitter = new RadioTransmitterModel(
                dimensionString, pos, band, message, passphrase
            );
            storage.addTransmitter(newTransmitter);

            runStaticRadioActions(level, pos, band, message);

            return storage.getTransmitters().size() - 1;
        }
    }

    /**
     * This should check as far as you would expect this band to transmit:
     * - most radio actions will be low band, and thus can just be checked locally, on that dimension or station
     * - some radio actions may be medium band, these should check nearby dimensions and space, and should be used sparingly
     * - some radio actions may be high/ultra-high band, these should not use structure locations unless they are static
     * @param level the dimension we are checking from
     * @param pos the location we are checking from
     * @param band qed
     * @param message qed
     */
    public static void runStaticRadioActions(ServerLevel level, BlockPos pos, int band, String message) {
        for (Consumer<StaticRadioAction> action : ACTIONS) {
            action.accept(new StaticRadioAction(level, pos, band, message));
        }
    }

    public static void notifySubscriber(ServerLevel level, BlockPos pos, String message) {
        if (level.getBlockState(pos).getBlock() instanceof ISubscriberRadio radio) {
            radio.processReceivedMessage(level, pos, message);
        }
    }

    public static List<String> nearbyDimensions(String fromDim, int band) {
        List<String> near = new ArrayList<>();
        BlockPos fromPos = storage.getDimensionLocation(fromDim);

        if (fromPos == null) {
            return near;
        }

        for (Map.Entry<String, BlockPos> entry : storage.getDimensionLocations().entrySet()) {
            String dimension = entry.getKey();
            if (!dimension.equals(fromDim)) {
                BlockPos toPos = entry.getValue();
                if (fromPos.distSqr(toPos) < Math.pow(band, 64)) {
                    near.add(dimension);
                }
            }
        }
        return near;
    }

    /**
     * Checks if two points in the same or different dimensions would be reachable on a given radio band
     * @param dimString1 the first dimension
     * @param pos1 typically, the transmitters position
     * @param dimString2 the second dimension (could be the same as dimString1)
     * @param pos2 typically, the receivers position
     * @param band the band to check on
     * @return qed
     */
    public static boolean inRadioDistance(String dimString1, BlockPos pos1, String dimString2, BlockPos pos2, int band) {
        double maxDistance = getMaxRangeForAnySignal(band);
        return getRadioDistance(dimString1, pos1, dimString2, pos2, band) <= maxDistance;
    }

    private static double getRadioDistance(String dimString1, BlockPos pos1, String dimString2, BlockPos pos2, int band) {
        if (dimString1.equals(dimString2)) {
            return Math.sqrt(pos1.distSqr(pos2));
        } else {
            if (storage.hasDimensionLocation(dimString1) && storage.hasDimensionLocation(dimString2)) {
                BlockPos bp1 = storage.getDimensionLocation(dimString1);
                BlockPos bp2 = storage.getDimensionLocation(dimString2);
                double distance = Math.sqrt(bp1.distSqr(bp2)) * CROSS_DIMENSION_MULTIPLIER;
                return Math.sqrt(pos1.distSqr(pos2)) + distance;
            }
        }
        return Double.MAX_VALUE;
    }

    /**
     * Scan through all radios currently transmitting and see if there message is receivable
     * if `shouldSubscribe` is true, then any radios that have detectable messages will register this receiver
     * as a subscriber, this means they will be directly notified when a transmission is sent
     * @param receiverLevel the dimension
     * @param receiverPos the physical location of the radio receiver;
     * @param band qed
     * @param shouldSubscribe qed
     * @return a list of all the receivable transmissions on this band at this location
     */
    public static List<RadioSignal> receive(ServerLevel receiverLevel, BlockPos receiverPos, int band,
                                           boolean shouldSubscribe, List<String> passphrases) {
        String receiverDimString = receiverLevel.dimension().location().toString();

        List<RadioSignal> messages = new ArrayList<>();

        for (RadioTransmitterModel transmitter : storage.getTransmitters()) {
            double distance = getRadioDistance(receiverDimString, receiverPos, transmitter.getDimension(), transmitter.getPos(), band);
            // Check if on same band and in range
            if (transmitter.getBand() == band &&
                inRadioDistance(receiverDimString, receiverPos,
                              transmitter.getDimension(), transmitter.getPos(), band)) {

                // Skip blank messages
                if (transmitter.getMessage().isBlank()) {
                    continue;
                }

                String passphrase = transmitter.getPassphrase();
                if (passphrase.isEmpty() || passphrases.contains(passphrase)) {
                    // Message is receivable
                    messages.add(new RadioSignal(transmitter.getMessage(), getSignalStrength(band, distance)));

                    // Subscribe if requested
                    if (shouldSubscribe) {
                        RadioReceiverModel receiver = new RadioReceiverModel(receiverDimString, receiverPos);
                        transmitter.addSubscriber(receiver);
                    }
                } else {
                    // Wrong passphrase
                    messages.add(RadioSignal.scrambled(transmitter.getMessage(), getSignalStrength(band, distance)));
                }
            }
        }

        return messages;
    }


    public static double getMaxRangeForClearSignal(int band) {
        return 50 * Math.pow(2, band / 5.0);
    }

    public static double getMaxRangeForAnySignal(int band) {
        return getMaxRangeForClearSignal(band) * 2;
    }

    public static double getSignalStrength(int band, double distance) {
        double maxA = getMaxRangeForClearSignal(band);
        if (distance <= maxA) return 1.0;
        double excess = distance - maxA;
        double excessAllowance = getMaxRangeForAnySignal(band) - maxA;
        return Mth.clamp(1.0 - (excess / excessAllowance), 0.0, 1.0);
    }

    /**
     * Represents an action that can be performed when a radio transmits a message
     * useful for setting up radios that may be in structures
     * @param level
     * @param pos
     * @param band
     * @param message
     */
    public record StaticRadioAction(ServerLevel level, BlockPos pos, int band, String message) {}
}
