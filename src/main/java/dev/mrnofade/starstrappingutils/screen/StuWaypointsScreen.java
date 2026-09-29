package dev.mrnofade.starstrappingutils.screen;

import dev.mrnofade.starstrappingutils.Waypoint;
import dev.mrnofade.starstrappingutils.WaypointManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class StuWaypointsScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;
    private static final int ROW_HEIGHT = 22;
    private static final int MAX_VISIBLE = 8;

    public StuWaypointsScreen(Screen parent) {
        super(Text.literal("Waypoints"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildList();
    }

    private void rebuildList() {
        clearChildren();
        List<Waypoint> wps = WaypointManager.getWaypoints();
        int startY = 40;
        int visible = Math.min(MAX_VISIBLE, wps.size() - scrollOffset);
        for (int i = 0; i < visible; i++) {
            int idx = i + scrollOffset;
            Waypoint wp = wps.get(idx);
            String label = wp.name + " [" + wp.pos.getX() + ", " + wp.pos.getY() + ", " + wp.pos.getZ() + "]";
            final int finalIdx = idx;
            addDrawableChild(ButtonWidget.builder(Text.literal("X"), btn -> {
                WaypointManager.removeWaypoint(finalIdx);
                scrollOffset = Math.max(0, scrollOffset - 1);
                rebuildList();
            }).dimensions(width / 2 + 90, startY + i * ROW_HEIGHT, 20, 18).build());
            addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> {})
                    .dimensions(width / 2 - 110, startY + i * ROW_HEIGHT, 198, 18).build());
        }

        if (scrollOffset > 0) {
            addDrawableChild(ButtonWidget.builder(Text.literal("^"), btn -> { scrollOffset--; rebuildList(); })
                    .dimensions(width / 2 + 115, 40, 20, 18).build());
        }
        if (scrollOffset + MAX_VISIBLE < wps.size()) {
            addDrawableChild(ButtonWidget.builder(Text.literal("v"), btn -> { scrollOffset++; rebuildList(); })
                    .dimensions(width / 2 + 115, 40 + (MAX_VISIBLE - 1) * ROW_HEIGHT, 20, 18).build());
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
                .dimensions(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        if (WaypointManager.getWaypoints().isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("No waypoints. Use the keybind to add one."), width / 2, height / 2, 0xAAAAAA);
        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
