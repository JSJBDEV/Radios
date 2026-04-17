package ace.actually.radios.blocks;

import ace.actually.radios.api.ISubscriberRadio;
import ace.actually.radios.api.RadioSignal;
import ace.actually.radios.api.RadioSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return Block.box(5,0,2,11,8,14);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel sl) {
            player.sendSystemMessage(Component.literal("Tuning... "));
            List<RadioSignal> messages = RadioSpec.receive(sl, pos, 5, player.isCrouching(), List.of());
            if (messages.isEmpty()) {
                player.sendSystemMessage(Component.literal("No signals found."));
            } else {
                messages.forEach(signal -> player.sendSystemMessage(Component.literal(signal.message())));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (hand == InteractionHand.MAIN_HAND && level instanceof ServerLevel sl) {
            RadioSpec.transmit(sl, pos, 5, stack.getDisplayName().getString(), "");
            player.sendSystemMessage(Component.literal("Broadcasting: " + stack.getDisplayName().getString()));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if(level instanceof ServerLevel sl) {
            // clear message on remove
            RadioSpec.transmit(sl, pos, 5, "", "");
        }
    }

    @Override
    public void processReceivedMessage(ServerLevel receiverLevel, BlockPos receiverPos, String message) {

    }
}
