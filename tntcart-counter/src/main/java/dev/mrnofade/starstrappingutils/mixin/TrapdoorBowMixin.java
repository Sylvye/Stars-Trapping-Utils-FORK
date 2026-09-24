package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StarTrappingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrapDoorBlock.class)
public class TrapdoorBowMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void stuCancelTrapdoorWithBow(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!StarTrappingUtils.bowBlocksTrapdoors) return;
        if (player.getMainHandItem().getItem() instanceof BowItem
                || player.getOffhandItem().getItem() instanceof BowItem) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
