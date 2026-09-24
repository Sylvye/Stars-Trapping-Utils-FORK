package dev.mrnofade.starstrappingutils;

import net.minecraft.core.BlockPos;

public class Waypoint {

    public final String name;
    public final BlockPos pos;
    public final int color;
    public final String dimension;

    public Waypoint(String name, BlockPos pos, int color, String dimension) {
        this.name = name;
        this.pos = pos;
        this.color = color;
        this.dimension = dimension;
    }

    public String toSaveString() {
        return name + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":" + color + ":" + dimension;
    }

    public static Waypoint fromSaveString(String s) {
        String[] parts = s.split(":");
        if (parts.length < 6) return null;
        try {
            return new Waypoint(
                    parts[0],
                    new BlockPos(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])),
                    Integer.parseInt(parts[4]),
                    parts[5]
            );
        } catch (Exception e) {
            return null;
        }
    }
}
