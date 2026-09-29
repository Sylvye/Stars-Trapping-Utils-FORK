package dev.mrnofade.starstrappingutils;

import net.minecraft.client.MinecraftClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class StuConfig {

    private static Path configPath;

    public static void init() {
        configPath = MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config").resolve("starstrappingutils.properties");
        load();
    }

    public static void save() {
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            Properties p = new Properties();
            p.setProperty("showTnt",          String.valueOf(StarTrappingUtils.showTnt));
            p.setProperty("showHopper",       String.valueOf(StarTrappingUtils.showHopper));
            p.setProperty("showChest",        String.valueOf(StarTrappingUtils.showChest));
            p.setProperty("showFurnace",      String.valueOf(StarTrappingUtils.showFurnace));
            p.setProperty("showEmpty",        String.valueOf(StarTrappingUtils.showEmpty));
            p.setProperty("showSnowGolem",    String.valueOf(StarTrappingUtils.showSnowGolem));
            p.setProperty("showArmorStand",   String.valueOf(StarTrappingUtils.showArmorStand));
            p.setProperty("showArrows",       String.valueOf(StarTrappingUtils.showArrows));
            p.setProperty("showWindCharges",  String.valueOf(StarTrappingUtils.showWindCharges));
            p.setProperty("showRailPower",    String.valueOf(StarTrappingUtils.showRailPower));
            p.setProperty("showArrowDespawn", String.valueOf(StarTrappingUtils.showArrowDespawn));
            p.setProperty("noSignGui",        String.valueOf(StarTrappingUtils.noSignGui));
            p.setProperty("invisWarning",     String.valueOf(StarTrappingUtils.invisWarning));
            p.setProperty("bowBlocksTrapdoors", String.valueOf(StarTrappingUtils.bowBlocksTrapdoors));
            p.setProperty("hudPosition",      StarTrappingUtils.hudPosition.name());
            p.setProperty("arrowVolume",      String.valueOf(StarTrappingUtils.arrowVolume));
            p.setProperty("minecartVolume",   String.valueOf(StarTrappingUtils.minecartVolume));
            try (OutputStream out = Files.newOutputStream(configPath)) {
                p.store(out, "Star's Trapping Utils config");
            }
        } catch (Exception ignored) {}
    }

    private static void load() {
        if (configPath == null || !Files.exists(configPath)) return;
        try (InputStream in = Files.newInputStream(configPath)) {
            Properties p = new Properties();
            p.load(in);
            StarTrappingUtils.showTnt          = bool(p, "showTnt", true);
            StarTrappingUtils.showHopper       = bool(p, "showHopper", true);
            StarTrappingUtils.showChest        = bool(p, "showChest", true);
            StarTrappingUtils.showFurnace      = bool(p, "showFurnace", true);
            StarTrappingUtils.showEmpty        = bool(p, "showEmpty", true);
            StarTrappingUtils.showSnowGolem    = bool(p, "showSnowGolem", true);
            StarTrappingUtils.showArmorStand   = bool(p, "showArmorStand", true);
            StarTrappingUtils.showArrows       = bool(p, "showArrows", true);
            StarTrappingUtils.showWindCharges  = bool(p, "showWindCharges", true);
            StarTrappingUtils.showRailPower    = bool(p, "showRailPower", false);
            StarTrappingUtils.showArrowDespawn = bool(p, "showArrowDespawn", false);
            StarTrappingUtils.noSignGui        = bool(p, "noSignGui", false);
            StarTrappingUtils.invisWarning     = bool(p, "invisWarning", false);
            StarTrappingUtils.bowBlocksTrapdoors = bool(p, "bowBlocksTrapdoors", false);
            StarTrappingUtils.arrowVolume      = doub(p, "arrowVolume", 1.0);
            StarTrappingUtils.minecartVolume   = doub(p, "minecartVolume", 1.0);
            String pos = p.getProperty("hudPosition");
            if (pos != null) {
                try { StarTrappingUtils.hudPosition = HudPosition.valueOf(pos); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v != null ? Boolean.parseBoolean(v) : def;
    }

    private static double doub(Properties p, String key, double def) {
        try { return Double.parseDouble(p.getProperty(key, String.valueOf(def))); }
        catch (Exception e) { return def; }
    }
}
