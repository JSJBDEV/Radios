package ace.actually.radios.blocks;

import ace.actually.radios.ISubscriberRadio;
import ace.actually.radios.RadioSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class Band5RadioBlock extends Block implements ISubscriberRadio {
    public Band5RadioBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return Block.box(5,0,2,11,8,14);
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_60508_) {

        if(level instanceof ServerLevel sl && hand==InteractionHand.MAIN_HAND)
        {
            ItemStack stack = player.getMainHandItem();
            player.sendSystemMessage(Component.literal("Tuning... "));
            if(stack.isEmpty())
            {
                List<String> messages = RadioSpec.receive(sl,pos,5,player.isCrouching());
                messages.forEach(a->player.sendSystemMessage(Component.literal(a)));
            }
            else
            {
                RadioSpec.transmit(sl,pos,5,stack.getDisplayName().getString());
            }
        }
        return super.use(p_60503_, level, pos, player, hand, p_60508_);
    }

    @Override
    public void processReceivedMessage(ServerLevel receiverLevel, BlockPos receiverPos, String message) {

    }
}
