package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StuBalTracker;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class OverlayHideMixin {

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void stuInterceptOverlay(Text message, boolean tinted, CallbackInfo ci) {
        if (StuBalTracker.tryCapture(message.getString())) {
            ci.cancel();
        }
    }
}
