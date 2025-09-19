package ace.actually.radios;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface ISubscriberRadio {
    public void processReceivedMessage(ServerLevel receiverLevel, BlockPos receiverPos, String message);
}
