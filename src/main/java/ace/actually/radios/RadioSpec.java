package ace.actually.radios;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class RadioSpec {

    private static final ResourceLocation RADIO_SAVE = ResourceLocation.fromNamespaceAndPath("radios","radios");

    private static ListTag RADIOS = new ListTag();
    private static CompoundTag DIMENSIONS = new CompoundTag();

    public static List<Consumer<StaticRadioAction>> ACTIONS = new ArrayList<>();

    private static final int CROSS_DIMENSION_MULTIPLIER = 16;

    public static void save(MinecraftServer server)
    {
        CompoundTag savedRadios = server.getCommandStorage().get(RADIO_SAVE);
        savedRadios.put("radios",RADIOS);
        savedRadios.put("dimensions",DIMENSIONS);
        server.getCommandStorage().set(RADIO_SAVE,savedRadios);
    }
    public static void load(MinecraftServer server)
    {
        CompoundTag savedRadios = server.getCommandStorage().get(RADIO_SAVE);
        RADIOS = savedRadios.getList("radios",ListTag.TAG_COMPOUND);
        DIMENSIONS = savedRadios.getCompound("dimensions");
    }


    public static void registerDimensionPhysicalLocation(ServerLevel level, int x, int y, int z)
    {
        load(level.getServer());
        DIMENSIONS.putIntArray(level.dimension().location().toString(),new int[]{x,y,z});
        save(level.getServer());
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
    public static int transmit(ServerLevel level, BlockPos pos, int band,String message,String passphrase)
    {
        load(level.getServer());
        for(int i = 0; i < RADIOS.size(); i++) {
            CompoundTag radio = RADIOS.getCompound(i);
            if(level.dimension().location().toString().equals(radio.getString("dimension")))
            {
                int[] v = radio.getIntArray("pos");
                if(v[0]==pos.getX() && v[1]==pos.getY() && v[2]==pos.getZ())
                {
                    radio.putString("message",message);
                    radio.putString("passphrase",passphrase);
                    RADIOS.set(i,radio);
                    if(radio.contains("subscribers"))
                    {
                        ListTag subs = radio.getList("subscribers",ListTag.TAG_COMPOUND);
                        for (int j = 0; j < subs.size(); j++) {
                            CompoundTag sub = subs.getCompound(j);
                            int[] p = sub.getIntArray("pos");
                            ResourceKey<Level> a = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(sub.getString("dimension")));
                            ServerLevel dimension = level.getServer().getLevel(a);
                            notifySubscriber(dimension,new BlockPos(p[0],p[1],p[2]),message);
                        }
                    }
                    runStaticRadioActions(level, pos, band, message);
                    save(level.getServer());
                    return i;
                }
            }
        }
        CompoundTag radio = new CompoundTag();
        radio.putString("dimension",level.dimension().location().toString());
        radio.putIntArray("pos",new int[]{pos.getX(),pos.getY(),pos.getZ()});
        radio.putInt("band",band);
        radio.putString("message",message);
        radio.putString("passphrase",passphrase);
        RADIOS.add(radio);
        runStaticRadioActions(level, pos, band, message);
        save(level.getServer());
        return RADIOS.size()-1;
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
    public static void runStaticRadioActions(ServerLevel level, BlockPos pos, int band, String message)
    {
        for(Consumer<StaticRadioAction> action: ACTIONS)
        {
            action.accept(new StaticRadioAction(level,pos,band,message));
        }
    }

    public static void notifySubscriber(ServerLevel level, BlockPos pos, String message)
    {
        if(level.getBlockState(pos).getBlock() instanceof ISubscriberRadio radio)
        {
            radio.processReceivedMessage(level,pos,message);
        }
    }

    public static List<String> nearbyDimensions(String fromDim, int band)
    {
        List<String> near = new ArrayList<>();
        int[] ia = DIMENSIONS.getIntArray(fromDim);
        BlockPos fromPos = new BlockPos(ia[0],ia[1],ia[2]);
        for(String dimension: DIMENSIONS.getAllKeys())
        {
            if(!dimension.equals(fromDim))
            {
                int[] ia2 = DIMENSIONS.getIntArray(dimension);
                BlockPos toPos = new BlockPos(ia2[0],ia2[1],ia2[2]);
                if(fromPos.distSqr(toPos)<Math.pow(band,64))
                {
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
    public static boolean inRadioDistance(String dimString1, BlockPos pos1, String dimString2, BlockPos pos2, int band)
    {

        if(dimString1.equals(dimString2))
        {
            return pos1.distSqr(pos2)<Math.pow(band,8);
        }
        else
        {
            if(DIMENSIONS.contains(dimString1) && DIMENSIONS.contains(dimString2))
            {
                int[] ia1 = DIMENSIONS.getIntArray(dimString1);
                BlockPos bp1 = new BlockPos(ia1[0],ia1[1],ia1[2]);
                int[] ia2 = DIMENSIONS.getIntArray(dimString2);
                BlockPos bp2 = new BlockPos(ia2[0],ia2[1],ia2[2]);
                double distance = bp1.distSqr(bp2)*CROSS_DIMENSION_MULTIPLIER;
                return (pos1.distSqr(pos2)+distance)<Math.pow(band,8);
            }
        }
        return false;
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
    public static List<String> receive(ServerLevel receiverLevel, BlockPos receiverPos, int band, boolean shouldSubscribe, List<String> passphrases)
    {
        load(receiverLevel.getServer());
        String receiverDimString = receiverLevel.dimension().location().toString();

        List<String> messages = new ArrayList<>();
        for (int i = 0; i < RADIOS.size(); i++) {
            CompoundTag radio = RADIOS.getCompound(i);
            int[] ia = radio.getIntArray("pos");
            BlockPos bp = new BlockPos(ia[0],ia[1],ia[2]);
            if(inRadioDistance(receiverDimString,receiverPos,radio.getString("dimension"),bp,band))
            {
                String passphrase = radio.getString("passphrase");
                if(passphrase.isEmpty() && passphrases.contains(passphrase))
                {
                    messages.add(radio.getString("message"));
                    if(shouldSubscribe)
                    {
                        ListTag subs = radio.getList("subscribers",ListTag.TAG_COMPOUND);
                        boolean exists = false;

                        //we first check if this block exists as a subscriber already
                        for (int j = 0; j < subs.size(); j++) {
                            CompoundTag sub = subs.getCompound(i);
                            if(sub.getString("dimension").equals(receiverDimString))
                            {
                                int[] w = sub.getIntArray("pos");
                                if(w[0]==receiverPos.getX() && w[1]==receiverPos.getY() && w[2]==receiverPos.getZ())
                                {
                                    exists = true;
                                    break;
                                }
                            }
                        }
                        if(!exists)
                        {
                            CompoundTag subscriber = new CompoundTag();
                            subscriber.putString("dimension",receiverLevel.dimension().location().toString());
                            subscriber.putIntArray("pos", new int[]{receiverPos.getX(),receiverPos.getY(),receiverPos.getZ()});
                            subs.add(subscriber);
                            radio.put("subscribers",subs);
                            RADIOS.set(i,radio);
                        }
                    }
                }
                else
                {
                    messages.add("[You receive an incomprehensible signal]");
                }

            }
        }
        save(receiverLevel.getServer());
        return messages;
    }

    /**
     * Represents an action that can be performed when a radio transmits a message
     * useful for setting up radios that may be in structures
     * @param level
     * @param pos
     * @param band
     * @param message
     */
    public record StaticRadioAction(ServerLevel level, BlockPos pos, int band, String message){}
}
