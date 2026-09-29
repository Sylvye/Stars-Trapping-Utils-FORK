package dev.mrnofade.starstrappingutils.screen;

import dev.mrnofade.starstrappingutils.HudPosition;
import dev.mrnofade.starstrappingutils.StarTrappingUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class StuSettingsScreen extends Screen {

    private final Screen parent;

    public StuSettingsScreen(Screen parent) {
        super(Text.literal("Star's Trapping Utils"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int col1 = width / 2 - 305;
        int col2 = width / 2 - 100;
        int col3 = width / 2 + 110;
        int startY = 40;
        int rowH = 24;
        int btnW = 190;

        addToggle(col1, startY,            btnW, "TNT Cart",            StarTrappingUtils.showTnt,          v -> StarTrappingUtils.showTnt = v);
        addToggle(col1, startY + rowH,     btnW, "Hopper Cart",         StarTrappingUtils.showHopper,       v -> StarTrappingUtils.showHopper = v);
        addToggle(col1, startY + rowH * 2, btnW, "Chest Cart",          StarTrappingUtils.showChest,        v -> StarTrappingUtils.showChest = v);
        addToggle(col1, startY + rowH * 3, btnW, "Furnace Cart",        StarTrappingUtils.showFurnace,      v -> StarTrappingUtils.showFurnace = v);
        addToggle(col1, startY + rowH * 4, btnW, "Empty Cart",          StarTrappingUtils.showEmpty,        v -> StarTrappingUtils.showEmpty = v);

        addToggle(col2, startY,            btnW, "Snow Golem",          StarTrappingUtils.showSnowGolem,    v -> StarTrappingUtils.showSnowGolem = v);
        addToggle(col2, startY + rowH,     btnW, "Armor Stand",         StarTrappingUtils.showArmorStand,   v -> StarTrappingUtils.showArmorStand = v);
        addToggle(col2, startY + rowH * 2, btnW, "Arrows",              StarTrappingUtils.showArrows,       v -> StarTrappingUtils.showArrows = v);
        addToggle(col2, startY + rowH * 3, btnW, "Wind Charges",        StarTrappingUtils.showWindCharges,  v -> StarTrappingUtils.showWindCharges = v);
        addToggle(col2, startY + rowH * 4, btnW, "Rail Power",          StarTrappingUtils.showRailPower,    v -> StarTrappingUtils.showRailPower = v);
        addToggle(col2, startY + rowH * 5, btnW, "Arrow Despawn Timer", StarTrappingUtils.showArrowDespawn, v -> StarTrappingUtils.showArrowDespawn = v);
        addToggle(col2, startY + rowH * 6, btnW, "No Sign GUI",         StarTrappingUtils.noSignGui,        v -> StarTrappingUtils.noSignGui = v);
        addToggle(col2, startY + rowH * 7, btnW, "Invis Warning",       StarTrappingUtils.invisWarning,        v -> StarTrappingUtils.invisWarning = v);
        addToggle(col2, startY + rowH * 8, btnW, "Bow Blocks Trapdoors", StarTrappingUtils.bowBlocksTrapdoors,  v -> StarTrappingUtils.bowBlocksTrapdoors = v);

        addDrawableChild(ButtonWidget.builder(Text.literal("Open Calculator"), btn ->
                client.setScreen(new StuCalculatorScreen(this)))
                .dimensions(col3, startY + rowH * 3, btnW, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Waypoints"), btn ->
                client.setScreen(new StuWaypointsScreen(this)))
                .dimensions(col3, startY + rowH * 4, btnW, 20).build());

        addDrawableChild(new VolumeSlider(col3, startY, btnW, 20,
                "Arrow Volume", StarTrappingUtils.arrowVolume,
                v -> StarTrappingUtils.arrowVolume = v));

        addDrawableChild(new VolumeSlider(col3, startY + rowH, btnW, 20,
                "Minecart Volume", StarTrappingUtils.minecartVolume,
                v -> StarTrappingUtils.minecartVolume = v));

        addDrawableChild(ButtonWidget.builder(
                Text.literal("HUD Pos: " + StarTrappingUtils.hudPosition.displayName()),
                btn -> {
                    HudPosition[] vals = HudPosition.values();
                    int next = (StarTrappingUtils.hudPosition.ordinal() + 1) % vals.length;
                    StarTrappingUtils.hudPosition = vals[next];
                    btn.setMessage(Text.literal("HUD Pos: " + StarTrappingUtils.hudPosition.displayName()));
                }
        ).dimensions(col3, startY + rowH * 2, btnW, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
                .dimensions(width / 2 - 50, height - 30, 100, 20)
                .build());
    }

    private void addToggle(int x, int y, int w, String label, boolean initial, java.util.function.Consumer<Boolean> setter) {
        addDrawableChild(ButtonWidget.builder(
                Text.literal(label + ": " + (initial ? "ON" : "OFF")),
                btn -> {
                    boolean next = !btn.getMessage().getString().endsWith("ON");
                    setter.accept(next);
                    btn.setMessage(Text.literal(label + ": " + (next ? "ON" : "OFF")));
                }
        ).dimensions(x, y, w, 20).build());
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
        context.drawTextWithShadow(textRenderer, Text.literal("Minecarts"), width / 2 - 305, 28, 0xAAAAAA);
        context.drawTextWithShadow(textRenderer, Text.literal("Features"), width / 2 - 100, 28, 0xAAAAAA);
        context.drawTextWithShadow(textRenderer, Text.literal("Volume"), width / 2 + 110, 28, 0xAAAAAA);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
