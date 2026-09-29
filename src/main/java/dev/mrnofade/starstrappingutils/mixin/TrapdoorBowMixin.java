package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StarTrappingUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrapdoorBlock.class)
public class TrapdoorBowMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void stuCancelTrapdoorWithBow(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!StarTrappingUtils.bowBlocksTrapdoors) return;
        if (player.getMainHandStack().getItem() instanceof BowItem
                || player.getOffHandStack().getItem() instanceof BowItem) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
