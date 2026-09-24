package dev.mrnofade.starstrappingutils.screen;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class VolumeSlider extends AbstractSliderButton {

    private final String label;
    private final Consumer<Double> onChange;

    public VolumeSlider(int x, int y, int width, int height, String label, double initialValue, Consumer<Double> onChange) {
        super(x, y, width, height, Component.literal(label + ": " + format(initialValue)), initialValue);
        this.label = label;
        this.onChange = onChange;
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(label + ": " + format(value)));
    }

    @Override
    protected void applyValue() {
        onChange.accept(value);
    }

    private static String format(double v) {
        if (v <= 0.0) return "Muted";
        if (v >= 1.0) return "100%";
        return (int) (v * 100) + "%";
    }
}
