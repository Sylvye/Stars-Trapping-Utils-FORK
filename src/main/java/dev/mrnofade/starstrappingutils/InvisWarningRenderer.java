package dev.mrnofade.starstrappingutils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;

public class InvisWarningRenderer {

    public static void render(DrawContext context) {
        if (!StarTrappingUtils.invisWarning) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        StatusEffectInstance invis = mc.player.getStatusEffect(StatusEffects.INVISIBILITY);
        if (invis == null) return;

        int remaining = invis.getDuration();
        if (remaining > 100) return;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int borderSize = 20;
        int alpha = 51;
        int color = (alpha << 24) | 0x00CC0000;

        context.fill(0, 0, borderSize, screenH, color);
        context.fill(screenW - borderSize, 0, screenW, screenH, color);
        context.fill(0, 0, screenW, borderSize, color);
        context.fill(0, screenH - borderSize, screenW, screenH, color);
    }
}
