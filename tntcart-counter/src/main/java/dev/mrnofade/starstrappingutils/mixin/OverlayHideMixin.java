package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StuBalTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class OverlayHideMixin {

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void stuInterceptOverlay(Component message, boolean tinted, CallbackInfo ci) {
        if (StuBalTracker.tryCapture(message.getString())) {
            ci.cancel();
        }
    }
}
