package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StarTrappingUtils;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSoundInstance.class)
public class SoundInstanceMixin {

    @Shadow protected net.minecraft.resources.Identifier identifier;

    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void stuVolumeScale(CallbackInfoReturnable<Float> cir) {
        String path = identifier.getPath();
        if (isArrowSound(path)) {
            cir.setReturnValue(cir.getReturnValue() * (float) StarTrappingUtils.arrowVolume);
        } else if (isMinecartSound(path)) {
            cir.setReturnValue(cir.getReturnValue() * (float) StarTrappingUtils.minecartVolume);
        }
    }

    private static boolean isArrowSound(String path) {
        return path.contains("arrow") || path.contains("bow/hit") || path.contains("bow/shoot");
    }

    private static boolean isMinecartSound(String path) {
        return path.contains("minecart") || path.contains("cart");
    }
}
