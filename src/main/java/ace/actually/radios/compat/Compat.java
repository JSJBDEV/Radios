package ace.actually.radios.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

public class Compat {
    private static boolean isVSLoaded() {
        return ModList.get().isLoaded("valkyrienskies");
    }

    public static BlockPos toWorldPos(ServerLevel level, BlockPos pos) {
        if (isVSLoaded()) {
            Vec3 truePos = VSCompat.shipToWorld(level, pos);
            return new BlockPos((int) truePos.x, (int) truePos.y, (int) truePos.z);
        } else {
            return pos;
        }
    }
}
