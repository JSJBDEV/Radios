package ace.actually.radios.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single transmitting radio.
 * Responsible for its own NBT serialization/deserialization.
 * Contains all transmitter data including subscribers.
 */
public class RadioTransmitterModel {
    private String dimension;
    private BlockPos pos;
    private int band;
    private String message;
    private String passphrase;
    private List<RadioReceiverModel> subscribers;

    public RadioTransmitterModel(String dimension, BlockPos pos, int band, String message, String passphrase) {
        this.dimension = dimension;
        this.pos = pos;
        this.band = band;
        this.message = message;
        this.passphrase = passphrase;
        this.subscribers = new ArrayList<>();
    }

    // Getters
    public String getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getBand() {
        return band;
    }

    public String getMessage() {
        return message;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public List<RadioReceiverModel> getSubscribers() {
        return subscribers;
    }

    // Setters for radio operations
    public void setBand(int band) {
        this.band = band;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * Adds a subscriber if not already present
     */
    public void addSubscriber(RadioReceiverModel subscriber) {
        // Check if subscriber already exists
        for (RadioReceiverModel existing : subscribers) {
            if (existing.matches(subscriber.getDimension(), subscriber.getPos())) {
                return; // Already subscribed
            }
        }
        subscribers.add(subscriber);
    }

    /**
     * Checks if this transmitter matches a given dimension and position
     */
    public boolean matches(String dimension, BlockPos pos) {
        return this.dimension.equals(dimension) &&
               this.pos.getX() == pos.getX() &&
               this.pos.getY() == pos.getY() &&
               this.pos.getZ() == pos.getZ();
    }

    /**
     * Deserializes a transmitter from NBT
     */
    public static RadioTransmitterModel fromNBT(CompoundTag tag) {
        String dimension = tag.getString("dimension");
        int[] posArray = tag.getIntArray("pos");
        BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        int band = tag.getInt("band");
        String message = tag.getString("message");
        String passphrase = tag.getString("passphrase");

        RadioTransmitterModel transmitter = new RadioTransmitterModel(dimension, pos, band, message, passphrase);

        // Load subscribers if present
        if (tag.contains("subscribers")) {
            ListTag subscribersList = tag.getList("subscribers", ListTag.TAG_COMPOUND);
            for (int i = 0; i < subscribersList.size(); i++) {
                CompoundTag subTag = subscribersList.getCompound(i);
                transmitter.subscribers.add(RadioReceiverModel.fromNBT(subTag));
            }
        }

        return transmitter;
    }

    /**
     * Serializes this transmitter to NBT
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension);
        tag.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        tag.putInt("band", band);
        tag.putString("message", message);
        tag.putString("passphrase", passphrase);

        // Save subscribers
        if (!subscribers.isEmpty()) {
            ListTag subscribersList = new ListTag();
            for (RadioReceiverModel subscriber : subscribers) {
                subscribersList.add(subscriber.toNBT());
            }
            tag.put("subscribers", subscribersList);
        }

        return tag;
    }
}
