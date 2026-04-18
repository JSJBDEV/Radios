package ace.actually.radios.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;

public class Compat {
    private static boolean isVSLoaded() {
        return ModList.get().isLoaded("valkyrienskies");
    }

    public static BlockPos toWorldPos(ServerLevel level, BlockPos pos) {
        BlockPos worldPos = SableCompat.plotToWorld(level, pos);
        if (isVSLoaded()) {
            return BlockPos.containing(VSCompat.shipToWorld(level, worldPos));
        } else {
            return worldPos;
        }
    }
}
