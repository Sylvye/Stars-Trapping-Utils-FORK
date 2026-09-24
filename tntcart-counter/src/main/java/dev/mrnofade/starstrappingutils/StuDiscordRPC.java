package dev.mrnofade.starstrappingutils;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import net.minecraft.client.Minecraft;

public class StuDiscordRPC {

    private static final long APP_ID = 1399393506867613739L;
    private static final String MODRINTH_URL = "https://modrinth.com/mod/stars-trapping-utils";

    private static IPCClient ipcClient;
    private static final OffsetDateTime sessionStart = OffsetDateTime.now();
    private static boolean connected = false;

    public static void init() {
        Thread.ofVirtual().start(() -> {
            try {
                ipcClient = new IPCClient(APP_ID);
                ipcClient.setListener(new IPCListener() {});
                ipcClient.connect();
                connected = true;
                update();
            } catch (Exception ignored) {}
        });
    }

    public static void update() {
        if (!connected || ipcClient == null || ipcClient.getStatus() != PipeStatus.CONNECTED) return;

        Minecraft mc = Minecraft.getInstance();
        String details = "Star's Trapping Utils";
        String state;

        if (mc != null && mc.getCurrentServer() != null) {
            state = "On " + mc.getCurrentServer().ip;
        } else if (mc != null && mc.hasSingleplayerServer()) {
            state = "Singleplayer";
        } else {
            state = "In Menu";
        }

        RichPresence presence = new RichPresence.Builder()
                .setDetails(details)
                .setState(state)
                .setStartTimestamp(sessionStart)
                .build();

        Thread.ofVirtual().start(() -> {
            try {
                JSONObject activity = presence.toJson();
                JSONArray buttons = new JSONArray();
                JSONObject button = new JSONObject();
                button.put("label", "View on Modrinth");
                button.put("url", MODRINTH_URL);
                buttons.put(button);
                activity.put("buttons", buttons);

                JSONObject args = new JSONObject();
                args.put("pid", ProcessHandle.current().pid());
                args.put("activity", activity);

                JSONObject payload = new JSONObject();
                payload.put("cmd", "SET_ACTIVITY");
                payload.put("args", args);

                Field pipeField = IPCClient.class.getDeclaredField("pipe");
                pipeField.setAccessible(true);
                Object pipe = pipeField.get(ipcClient);
                if (pipe != null) {
                    pipe.getClass().getMethod("send", Packet.OpCode.class, JSONObject.class, com.jagrosh.discordipc.entities.Callback.class)
                            .invoke(pipe, Packet.OpCode.FRAME, payload, null);
                }
            } catch (Exception ignored) {}
        });
    }

    public static void shutdown() {
        if (ipcClient != null) {
            try { ipcClient.close(); } catch (Exception ignored) {}
            ipcClient = null;
            connected = false;
        }
    }
}
