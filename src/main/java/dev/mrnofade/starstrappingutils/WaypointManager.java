package dev.mrnofade.starstrappingutils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WaypointManager {

    private static final List<Waypoint> waypoints = new ArrayList<>();
    private static Path savePath;

    private static final int[] COLORS = {
        0xFFFF4444, 0xFF44FF44, 0xFF4444FF, 0xFFFFFF44,
        0xFFFF44FF, 0xFF44FFFF, 0xFFFF8844, 0xFFFFFFFF
    };
    private static int nextColorIndex = 0;

    public static void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        savePath = mc.runDirectory.toPath().resolve("config").resolve("starstrappingutils_waypoints.txt");
        load();
    }

    public static void addWaypoint(String name, BlockPos pos, String dimension) {
        int color = COLORS[nextColorIndex % COLORS.length];
        nextColorIndex++;
        String dim = dimension.replace("minecraft:", "");
        waypoints.add(new Waypoint(name, pos, color, dim));
        save();
    }

    public static void removeNearest(Vec3d playerPos, String dimension) {
        String dim = dimension.replace("minecraft:", "");
        Waypoint nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Waypoint wp : waypoints) {
            if (!wp.dimension.equals(dim)) continue;
            double d = playerPos.squaredDistanceTo(wp.pos.getX() + 0.5, wp.pos.getY(), wp.pos.getZ() + 0.5);
            if (d < nearestDist) { nearestDist = d; nearest = wp; }
        }
        if (nearest != null) {
            waypoints.remove(nearest);
            save();
        }
    }

    public static void removeWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
            save();
        }
    }

    public static List<Waypoint> getWaypoints() {
        return waypoints;
    }

    private static void save() {
        try {
            Files.createDirectories(savePath.getParent());
            try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(savePath))) {
                for (Waypoint wp : waypoints) {
                    pw.println(wp.toSaveString());
                }
            }
        } catch (Exception ignored) {}
    }

    private static void load() {
        if (!Files.exists(savePath)) return;
        try (BufferedReader br = Files.newBufferedReader(savePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                Waypoint wp = Waypoint.fromSaveString(line.trim());
                if (wp != null) waypoints.add(wp);
            }
        } catch (Exception ignored) {}
    }
}
