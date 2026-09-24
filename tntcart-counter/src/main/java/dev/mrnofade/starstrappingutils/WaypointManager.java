package dev.mrnofade.starstrappingutils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class WaypointManager {

    private static final List<Waypoint> waypoints = new ArrayList<>();
    private static Path savePath;

    private static final int[] COLORS = {
        0xFFFF4444, 0xFF44FF44, 0xFF4444FF, 0xFFFFFF44,
        0xFFFF44FF, 0xFF44FFFF, 0xFFFF8844, 0xFFFFFFFF
    };
    private static int nextColorIndex = 0;

    public static void init() {
        Minecraft mc = Minecraft.getInstance();
        savePath = mc.gameDirectory.toPath().resolve("config").resolve("starstrappingutils_waypoints.txt");
        load();
    }

    public static void addWaypoint(String name, BlockPos pos, String dimension) {
        int color = COLORS[nextColorIndex % COLORS.length];
        nextColorIndex++;
        waypoints.add(new Waypoint(name, pos, color, dimension));
        save();
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

    public static void renderHud(GuiGraphicsExtractor context, DeltaTracker tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gui.hud.isHidden()) return;

        Vec3 playerPos = mc.player.position();
        String currentDim = mc.level.dimension().identifier().toString();

        int y = 5;
        for (Waypoint wp : waypoints) {
            if (!wp.dimension.equals(currentDim)) continue;

            int dx = wp.pos.getX() - (int) playerPos.x;
            int dy = wp.pos.getY() - (int) playerPos.y;
            int dz = wp.pos.getZ() - (int) playerPos.z;
            int dist = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);

            String text = String.format("%s [%d, %d, %d] (%dm)", wp.name, wp.pos.getX(), wp.pos.getY(), wp.pos.getZ(), dist);
            int x = mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(text) / 2;
            context.text(mc.font, text, x, y, wp.color);
            y += 11;
        }
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
