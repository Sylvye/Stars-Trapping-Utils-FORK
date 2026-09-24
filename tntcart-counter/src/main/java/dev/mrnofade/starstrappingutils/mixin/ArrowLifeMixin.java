package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.ArrowLifeAccessor;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class ArrowLifeMixin implements ArrowLifeAccessor {

    @Shadow
    protected int inGroundTime;

    @Unique
    private int stu_prevInGroundTime = 0;

    @Unique
    private int stu_clientLife = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void stuTrackClientLife(CallbackInfo ci) {
        if (inGroundTime > 0) {
            if (inGroundTime >= stu_prevInGroundTime) {
                stu_clientLife++;
            } else {
                stu_clientLife = 0;
            }
        } else {
            stu_clientLife = 0;
        }
        stu_prevInGroundTime = inGroundTime;
    }

    @Override
    public int stu_getLife() {
        return stu_clientLife;
    }
}
