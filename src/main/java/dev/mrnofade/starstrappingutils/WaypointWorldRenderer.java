package dev.mrnofade.starstrappingutils;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class WaypointWorldRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(WaypointWorldRenderer::onRender);
    }

    private static void onRender(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;

        MatrixStack matrices = ctx.matrices();
        if (matrices == null) return;

        String currentDim = mc.world.getRegistryKey().getValue().toString().replace("minecraft:", "");
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getCameraPos();

        VertexConsumerProvider.Immediate lineConsumers = mc.getBufferBuilders().getEffectVertexConsumers();

        for (Waypoint wp : WaypointManager.getWaypoints()) {
            if (!wp.dimension.equals(currentDim)) continue;

            double dx = wp.pos.getX() + 0.5 - camPos.x;
            double dy = wp.pos.getY() - camPos.y;
            double dz = wp.pos.getZ() + 0.5 - camPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 512) continue;

            int r = (wp.color >> 16) & 0xFF;
            int g = (wp.color >> 8) & 0xFF;
            int b = wp.color & 0xFF;
            int argb = wp.color | 0xFF000000;
            float beamHeight = 48f;

            matrices.push();
            matrices.translate(dx, dy, dz);
            MatrixStack.Entry entry = matrices.peek();

            VertexConsumer lines = lineConsumers.getBuffer(RenderLayers.lines());
            lines.vertex(entry, 0, 0, 0).color(r, g, b, 255).normal(entry, 0, 1, 0).lineWidth(6.0f);
            lines.vertex(entry, 0, beamHeight, 0).color(r, g, b, 0).normal(entry, 0, 1, 0).lineWidth(6.0f);
            lineConsumers.draw(RenderLayers.lines());

            matrices.translate(0, beamHeight + 0.5, 0);
            matrices.multiply(camera.getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);

            String label = wp.name + " \u00B7 " + (int) dist + "m";
            TextRenderer tr = mc.textRenderer;
            float lx = -tr.getWidth(label) / 2.0f;
            tr.draw(label, lx, 0, argb, false,
                    matrices.peek().getPositionMatrix(), lineConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0x60000000,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE);
            lineConsumers.draw();

            matrices.pop();
        }
    }
}
