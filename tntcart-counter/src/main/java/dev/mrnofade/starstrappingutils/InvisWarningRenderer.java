package dev.mrnofade.starstrappingutils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class InvisWarningRenderer {

    public static void render(GuiGraphicsExtractor context) {
        if (!StarTrappingUtils.invisWarning) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        MobEffectInstance invis = mc.player.getEffect(MobEffects.INVISIBILITY);
        if (invis == null) return;

        int remaining = invis.getDuration();
        if (remaining > 100) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int borderSize = 20;
        int alpha = 51;
        int color = (alpha << 24) | 0x00CC0000;

        context.fill(0, 0, borderSize, screenH, color);
        context.fill(screenW - borderSize, 0, screenW, screenH, color);
        context.fill(0, 0, screenW, borderSize, color);
        context.fill(0, screenH - borderSize, screenW, screenH, color);
    }
}
