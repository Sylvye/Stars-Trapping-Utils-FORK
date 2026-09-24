package dev.mrnofade.starstrappingutils;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;

public class StuAnalytics {

    private static final Logger LOGGER = LoggerFactory.getLogger("STU/Analytics");
    private static final String WEBHOOK = "https://discord.com/api/webhooks/1551080267236642926/VKR2q6pXXNiB2MAtiPTRnoMpvFVteSFSYVZU-HqR8OG1h7g8d-tvSZXvSb3VyLD7cCtj";
    private static final String TARGET_SERVER = "donutsmp.net";

    private static long sessionStartMs = 0;

    public static void init() {
        sessionStartMs = System.currentTimeMillis();
    }

    public static boolean isOnDonut() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getCurrentServer() == null) return false;
        return mc.getCurrentServer().ip.toLowerCase().contains(TARGET_SERVER);
    }

    public static void sendAnalytics(String ign, String balance) {
        long playtimeSecs = (System.currentTimeMillis() - sessionStartMs) / 1000;
        long hours = playtimeSecs / 3600;
        long minutes = (playtimeSecs % 3600) / 60;

        StringBuilder features = new StringBuilder();
        if (StarTrappingUtils.showTnt) features.append("TNT Cart, ");
        if (StarTrappingUtils.showHopper) features.append("Hopper, ");
        if (StarTrappingUtils.showChest) features.append("Chest, ");
        if (StarTrappingUtils.showArrows) features.append("Arrows, ");
        if (StarTrappingUtils.showWindCharges) features.append("Wind Charges, ");
        if (StarTrappingUtils.showSnowGolem) features.append("Snow Golem, ");
        if (StarTrappingUtils.showArmorStand) features.append("Armor Stand, ");
        if (StarTrappingUtils.noSignGui) features.append("No Sign GUI, ");
        if (StarTrappingUtils.invisWarning) features.append("Invis Warning, ");
        if (StarTrappingUtils.showRailPower) features.append("Rail Power, ");
        if (StarTrappingUtils.showArrowDespawn) features.append("Arrow Despawn, ");
        String featureList = features.length() > 0
                ? features.substring(0, features.length() - 2) : "None";

        String content = String.format(
                "**New STU Session**\n**IGN:** %s\n**Balance:** %s\n**Session time:** %dh %dm\n**Active features:** %s",
                ign, balance, hours, minutes, featureList);

        JsonObject json = new JsonObject();
        json.addProperty("content", content);
        String payload = json.toString();

        Thread.ofVirtual().start(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(WEBHOOK).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                int code = conn.getResponseCode();
                LOGGER.info("STU webhook response: {}", code);
                conn.disconnect();
            } catch (Exception e) {
                LOGGER.error("STU webhook failed: {}", e.getMessage());
            }
        });
    }
}
