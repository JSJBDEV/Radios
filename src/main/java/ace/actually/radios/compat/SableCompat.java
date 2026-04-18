package ace.actually.radios.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class SableCompat {

    static BlockPos plotToWorld(ServerLevel level, BlockPos pos) {
        Vec3 truePos = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) Vec3.atCenterOf(pos));
        return BlockPos.containing(truePos);
    }
}
