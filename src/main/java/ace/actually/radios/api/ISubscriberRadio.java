package ace.actually.radios.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
/**
 * To be implemented by a Block that subscribes to radio signal updates
 **/
public interface ISubscriberRadio {
    void processReceivedMessage(ServerLevel receiverLevel, BlockPos receiverPos, String message);
}
