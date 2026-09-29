package dev.mrnofade.starstrappingutils.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHideMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void stuFilterCommandFeedback(Text message, CallbackInfo ci) {
        String text = message.getString();
        if (text.startsWith("Replaced the item") ||
            text.startsWith("Summoned new") ||
            text.startsWith("Killed ") ||
            text.startsWith("Teleported ") ||
            text.startsWith("Set the slot") ||
            text.startsWith("No entity was found")) {
            ci.cancel();
        }
    }
}
