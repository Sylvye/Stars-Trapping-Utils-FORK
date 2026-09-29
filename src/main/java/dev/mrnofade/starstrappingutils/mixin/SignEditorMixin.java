package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StarTrappingUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class SignEditorMixin {

    @Inject(method = "onSignEditorOpen", at = @At("HEAD"), cancellable = true)
    private void stuBlockSignGui(SignEditorOpenS2CPacket packet, CallbackInfo ci) {
        if (StarTrappingUtils.noSignGui) {
            ci.cancel();
        }
    }
}
