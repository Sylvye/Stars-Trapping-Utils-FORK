package dev.mrnofade.starstrappingutils.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class StuCalculatorScreen extends Screen {

    private final Screen parent;
    private String display = "0";
    private String pending = "";
    private String operator = "";
    private boolean freshInput = true;

    private static final String[][] KEYS = {
        {"7", "8", "9", "/"},
        {"4", "5", "6", "*"},
        {"1", "2", "3", "-"},
        {"0", ".", "=", "+"},
        {"C", "+/-", "\u2190", ""}
    };

    public StuCalculatorScreen(Screen parent) {
        super(Text.literal("Calculator"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;
        int bw = 32;
        int bh = 22;
        int gap = 3;
        int gridW = 4 * bw + 3 * gap;
        int startX = cx - gridW / 2;
        int startY = cy - 60;

        for (int row = 0; row < KEYS.length; row++) {
            for (int col = 0; col < KEYS[row].length; col++) {
                String label = KEYS[row][col];
                if (label.isEmpty()) continue;
                int bx = startX + col * (bw + gap);
                int by = startY + row * (bh + gap);
                final String key = label;
                addDrawableChild(ButtonWidget.builder(Text.literal(label),
                        btn -> handleKey(key))
                        .dimensions(bx, by, bw, bh).build());
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
                .dimensions(cx - 50, cy + 75, 100, 20).build());
    }

    private void handleKey(String key) {
        switch (key) {
            case "C" -> { display = "0"; pending = ""; operator = ""; freshInput = true; }
            case "+/-" -> {
                if (!display.equals("0") && !display.equals("Error"))
                    display = display.startsWith("-") ? display.substring(1) : "-" + display;
            }
            case "\u2190" -> {
                if (display.equals("Error")) { display = "0"; freshInput = true; }
                else if (display.length() > 1) display = display.substring(0, display.length() - 1);
                else { display = "0"; freshInput = true; }
            }
            case "=" -> {
                if (!operator.isEmpty() && !pending.isEmpty()) {
                    display = calculate(pending, display, operator);
                    pending = ""; operator = ""; freshInput = true;
                }
            }
            case "+", "-", "*", "/" -> {
                if (!operator.isEmpty() && !freshInput) display = calculate(pending, display, operator);
                pending = display; operator = key; display = "0"; freshInput = true;
            }
            default -> {
                if (display.equals("Error")) { display = key; freshInput = false; return; }
                if (key.equals(".") && display.contains(".")) return;
                if (freshInput) { display = key.equals(".") ? "0." : key; freshInput = false; }
                else display = (display.equals("0") && !key.equals(".")) ? key : display + key;
                if (display.length() > 12) display = display.substring(0, 12);
            }
        }
    }

    private String calculate(String a, String b, String op) {
        try {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            double result = switch (op) {
                case "+" -> da + db;
                case "-" -> da - db;
                case "*" -> da * db;
                case "/" -> db == 0 ? Double.NaN : da / db;
                default -> db;
            };
            if (Double.isNaN(result) || Double.isInfinite(result)) return "Error";
            if (result == Math.floor(result)) return String.valueOf((long) result);
            return String.format("%.6f", result).replaceAll("0+$", "").replaceAll("\\.$", "");
        } catch (Exception e) {
            return "Error";
        }
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
        int cy = height / 2;
        int bw = 32;
        int bh = 22;
        int gap = 3;
        int gridW = 4 * bw + 3 * gap;
        int startX = cx - gridW / 2;
        int displayY = cy - 85;

        context.fill(startX - 2, displayY, startX + gridW + 2, displayY + 22, 0xFF222222);

        String expr = operator.isEmpty() ? display
                : freshInput ? pending + " " + operator
                : pending + " " + operator + " " + display;
        int textColor = display.equals("Error") ? 0xFFFF4444 : 0xFFFFFFFF;
        int textX = startX + gridW - textRenderer.getWidth(expr);
        context.drawTextWithShadow(textRenderer, Text.literal(expr),
                Math.max(startX + 2, textX), displayY + 7, textColor);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
