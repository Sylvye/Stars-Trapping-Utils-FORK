package dev.mrnofade.starstrappingutils.screen;

import dev.mrnofade.starstrappingutils.StuAnalytics;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class StuConsentScreen extends Screen {

    private final Screen parent;

    private static final String[] LINES = {
        "To help improve this mod, it can optionally share the",
        "following with the developers once per session on DonutSMP:",
        "",
        "  \u2022 Your in-game name",
        "  \u2022 Your balance (read-only \u2014 this mod cannot touch your money)",
        "  \u2022 Which mod features you have enabled",
        "  \u2022 Your total playtime with the mod installed",
        "",
        "No personal data beyond this is collected or stored.",
        "You can change this anytime in the STU settings menu."
    };

    public StuConsentScreen(Screen parent) {
        super(Text.literal("Star's Trapping Utils \u2014 Usage Analytics"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int bottom = height - 40;

        addDrawableChild(ButtonWidget.builder(Text.literal("Sure, why not"), btn -> {
            StuAnalytics.consent = true;
            StuAnalytics.save();
            client.setScreen(parent);
        }).dimensions(cx - 105, bottom, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("No thanks"), btn -> {
            StuAnalytics.consent = false;
            StuAnalytics.save();
            client.setScreen(parent);
        }).dimensions(cx + 5, bottom, 100, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int y = 30;

        context.drawCenteredTextWithShadow(textRenderer, title, cx, y, 0xFFFFFF);
        y += 20;

        for (String line : LINES) {
            if (line.isEmpty()) { y += 5; continue; }
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(line), cx, y, 0xCCCCCC);
            y += 12;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
