package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StarTrappingUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class SignEditorMixin {

    @Inject(method = "handleOpenSignEditor", at = @At("HEAD"), cancellable = true)
    private void stuBlockSignGui(ClientboundOpenSignEditorPacket packet, CallbackInfo ci) {
        if (StarTrappingUtils.noSignGui) {
            ci.cancel();
        }
    }
}
