package dev.mrnofade.starstrappingutils.mixin;

import dev.mrnofade.starstrappingutils.StuBalTracker;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatHideMixin {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void stuInterceptChat(Component message, CallbackInfo ci) {
        if (StuBalTracker.tryCapture(message.getString())) {
            ci.cancel();
        }
    }
}
