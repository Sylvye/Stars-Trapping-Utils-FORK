package dev.mrnofade.starstrappingutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;

public class StuBalTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger("STU/BalTracker");

    private static boolean waitingForBal = false;
    private static boolean captured = false;
    private static long requestTimeMs = 0;
    private static int retryCount = 0;

    private static final long RETRY_INTERVAL_MS = 5_000;
    private static final int MAX_RETRIES = 5;

    private static final Pattern BAL_PATTERN = Pattern.compile(
            "^you have\\s+\\$\\s*([\\d,]+(?:\\.[\\d]+)?)$",
            Pattern.CASE_INSENSITIVE
    );

    public static void startCapture() {
        LOGGER.info("STU: Starting bal capture");
        waitingForBal = true;
        captured = false;
        retryCount = 0;
        requestTimeMs = System.currentTimeMillis();
        sendBalCommand();
    }

    private static void sendBalCommand() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        LOGGER.info("STU: Sending /bal (attempt {})", retryCount + 1);
        mc.player.connection.sendCommand("bal");
    }

    public static void tick() {
        if (!waitingForBal || captured) return;

        long elapsed = System.currentTimeMillis() - requestTimeMs;
        if (elapsed > RETRY_INTERVAL_MS * (retryCount + 1)) {
            if (retryCount >= MAX_RETRIES) {
                LOGGER.warn("STU: Bal capture timed out after {} retries", MAX_RETRIES);
                waitingForBal = false;
                return;
            }
            retryCount++;
            sendBalCommand();
        }
    }

    public static boolean tryCapture(String message) {
        if (captured) return BAL_PATTERN.matcher(message.trim()).matches();
        if (!waitingForBal) return false;

        Matcher m = BAL_PATTERN.matcher(message.trim());
        if (m.matches()) {
            String balance = "$" + m.group(1);
            LOGGER.info("STU: Captured balance: {}", balance);
            captured = true;
            waitingForBal = false;
            Minecraft mc = Minecraft.getInstance();
            String ign = mc.player != null ? mc.player.getName().getString() : "Unknown";
            StuAnalytics.sendAnalytics(ign, balance);
            return true;
        }
        return false;
    }
}
