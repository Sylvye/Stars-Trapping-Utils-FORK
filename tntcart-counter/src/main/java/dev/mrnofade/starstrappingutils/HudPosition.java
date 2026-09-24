package dev.mrnofade.starstrappingutils;

import net.minecraft.client.Minecraft;

public enum HudPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    BOSSBAR,
    ABOVE_XP;

    public int getX(Minecraft mc, int contentWidth) {
        int w = mc.getWindow().getGuiScaledWidth();
        return switch (this) {
            case TOP_LEFT, BOTTOM_LEFT -> 10;
            case TOP_RIGHT, BOTTOM_RIGHT -> w - 10 - contentWidth;
            case BOSSBAR, ABOVE_XP -> w / 2 - contentWidth / 2;
        };
    }

    public int getY(Minecraft mc) {
        int h = mc.getWindow().getGuiScaledHeight();
        return switch (this) {
            case TOP_LEFT, TOP_RIGHT -> 10;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> h - 35;
            case BOSSBAR -> 14;
            case ABOVE_XP -> h - 49;
        };
    }

    public String displayName() {
        return switch (this) {
            case TOP_LEFT -> "Top Left";
            case TOP_RIGHT -> "Top Right";
            case BOTTOM_LEFT -> "Bottom Left";
            case BOTTOM_RIGHT -> "Bottom Right";
            case BOSSBAR -> "Bossbar Area";
            case ABOVE_XP -> "Above XP Bar";
        };
    }
}
