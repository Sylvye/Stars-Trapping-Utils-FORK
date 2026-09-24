package dev.mrnofade.starstrappingutils.screen;

import dev.mrnofade.starstrappingutils.Waypoint;
import dev.mrnofade.starstrappingutils.WaypointManager;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StuWaypointsScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;
    private static final int ROW_HEIGHT = 22;
    private static final int MAX_VISIBLE = 8;

    public StuWaypointsScreen(Screen parent) {
        super(Component.literal("Waypoints"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildList();
        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    private void rebuildList() {
        clearWidgets();
        List<Waypoint> wps = WaypointManager.getWaypoints();
        int startY = 40;
        int visible = Math.min(MAX_VISIBLE, wps.size() - scrollOffset);
        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffset;
            Waypoint wp = wps.get(idx);
            String label = wp.name + " [" + wp.pos.getX() + ", " + wp.pos.getY() + ", " + wp.pos.getZ() + "]";
            final int finalIdx = idx;
            addRenderableWidget(Button.builder(Component.literal("X"), btn -> {
                WaypointManager.removeWaypoint(finalIdx);
                rebuildList();
            }).bounds(width / 2 + 90, startY + i * ROW_HEIGHT, 20, 18).build());
            addRenderableWidget(Button.builder(Component.literal(label), btn -> {})
                    .bounds(width / 2 - 110, startY + i * ROW_HEIGHT, 198, 18).build());
        }

        if (scrollOffset > 0) {
            addRenderableWidget(Button.builder(Component.literal("^"), btn -> { scrollOffset--; rebuildList(); })
                    .bounds(width / 2 + 115, 40, 20, 18).build());
        }
        if (scrollOffset + MAX_VISIBLE < wps.size()) {
            addRenderableWidget(Button.builder(Component.literal("v"), btn -> { scrollOffset++; rebuildList(); })
                    .bounds(width / 2 + 115, 40 + (MAX_VISIBLE - 1) * ROW_HEIGHT, 20, 18).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractBackground(context, mouseX, mouseY, delta);
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, title, width / 2, 15, 0xFFFFFF);
        if (WaypointManager.getWaypoints().isEmpty()) {
            context.centeredText(font,
                    Component.literal("No waypoints. Use the keybind to add one."), width / 2, height / 2, 0xAAAAAA);
        }
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
