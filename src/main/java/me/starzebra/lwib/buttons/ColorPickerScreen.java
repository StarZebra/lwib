package me.starzebra.lwib.buttons;

import me.starzebra.lwib.Lwib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class ColorPickerScreen extends Screen {

    private final Screen parent;
    private final ColorSelectionCallback callback;
    private final int initialColor;

    private int red;
    private int green;
    private int blue;
    private int alpha;

    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;
    private ColorSlider alphaSlider;

    private Button confirmButton;
    private Button cancelButton;

    private static final int SLIDER_WIDTH = 200;
    private static final int SLIDER_HEIGHT = 20;
    private int previewSize;
    private int previewX;
    private int previewY;

    public ColorPickerScreen(Screen parent, int initialColor, ColorSelectionCallback callback) {
        super(Component.literal("Choose Color"));
        this.parent = parent;
        this.callback = callback;
        this.initialColor = initialColor;

        // Extract ARGB components
        this.alpha = (initialColor >> 24) & 0xFF;
        this.red = (initialColor >> 16) & 0xFF;
        this.green = (initialColor >> 8) & 0xFF;
        this.blue = initialColor & 0xFF;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 60;
        int spacing = 30;

        // Calculate preview size based on available height
        int slidersHeight = spacing * 4;
        int buttonsHeight = 40;
        int textHeight = 30;
        int availableHeight = this.height - startY - slidersHeight - buttonsHeight - textHeight;
        this.previewSize = Math.min(Math.max(50, availableHeight), 150);

        // Red slider
        this.redSlider = new ColorSlider(
                centerX - SLIDER_WIDTH / 2,
                startY,
                SLIDER_WIDTH,
                SLIDER_HEIGHT,
                Component.literal("Red: " + red),
                red / 255.0,
                value -> {
                    this.red = (int) (value * 255);
                    this.redSlider.setMessage(Component.literal("Red: " + red));
                }
        );

        // Green slider
        this.greenSlider = new ColorSlider(
                centerX - SLIDER_WIDTH / 2,
                startY + spacing,
                SLIDER_WIDTH,
                SLIDER_HEIGHT,
                Component.literal("Green: " + green),
                green / 255.0,
                value -> {
                    this.green = (int) (value * 255);
                    this.greenSlider.setMessage(Component.literal("Green: " + green));
                }
        );

        // Blue slider
        this.blueSlider = new ColorSlider(
                centerX - SLIDER_WIDTH / 2,
                startY + spacing * 2,
                SLIDER_WIDTH,
                SLIDER_HEIGHT,
                Component.literal("Blue: " + blue),
                blue / 255.0,
                value -> {
                    this.blue = (int) (value * 255);
                    this.blueSlider.setMessage(Component.literal("Blue: " + blue));
                }
        );

        // Alpha slider
        this.alphaSlider = new ColorSlider(
                centerX - SLIDER_WIDTH / 2,
                startY + spacing * 3,
                SLIDER_WIDTH,
                SLIDER_HEIGHT,
                Component.literal("Opacity: " + alpha),
                alpha / 255.0,
                value -> {
                    this.alpha = (int) (value * 255);
                    this.alphaSlider.setMessage(Component.literal("Opacity: " + alpha));
                }
        );

        // Calculate preview position
        this.previewX = centerX - previewSize / 2;
        this.previewY = startY + spacing * 4 + 5;

        // Position buttons below preview
        int buttonY = previewY + previewSize + 15;

        // Confirm button
        this.confirmButton = Button.builder(
                Component.literal("Apply"),
                button -> {
                    int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    callback.onColorSelected(color);
                    this.onClose();
                }
        ).bounds(centerX - 105, buttonY, 100, 20).build();

        // Cancel button
        this.cancelButton = Button.builder(
                Component.literal("Cancel"),
                button -> this.onClose()
        ).bounds(centerX + 5, buttonY, 100, 20).build();

        this.addRenderableWidget(redSlider);
        this.addRenderableWidget(greenSlider);
        this.addRenderableWidget(blueSlider);
        this.addRenderableWidget(alphaSlider);
        this.addRenderableWidget(confirmButton);
        this.addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Title
        guiGraphics.drawCenteredString(Lwib.mc.font, this.title, this.width / 2, 20, 0xFFFFFFFF);

        // Checkerboard background for transparency visualization
        drawCheckerboard(guiGraphics, previewX, previewY, previewSize, previewSize);

        // Current color preview
        int currentColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
        guiGraphics.fill(RenderPipelines.GUI, previewX, previewY, previewX + previewSize, previewY + previewSize, currentColor);

        // Border around preview
        guiGraphics.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY, 0xFFFFFFFF);
        guiGraphics.fill(previewX - 1, previewY + previewSize, previewX + previewSize + 1, previewY + previewSize + 1, 0xFFFFFFFF);
        guiGraphics.fill(previewX - 1, previewY, previewX, previewY + previewSize, 0xFFFFFFFF);
        guiGraphics.fill(previewX + previewSize, previewY, previewX + previewSize + 1, previewY + previewSize, 0xFFFFFFFF);

        // Hex color code
        String hexColor = String.format("#%02X%02X%02X%02X", alpha, red, green, blue);
        guiGraphics.drawCenteredString(Lwib.mc.font, hexColor, this.width / 2, previewY + previewSize + 5, 0xFFAAAAAA);
    }

    private void drawCheckerboard(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int squareSize = 8;
        for (int i = 0; i < width; i += squareSize) {
            for (int j = 0; j < height; j += squareSize) {
                boolean isLight = ((i / squareSize) + (j / squareSize)) % 2 == 0;
                int color = isLight ? 0xFFCCCCCC : 0xFF999999;
                guiGraphics.fill(
                        x + i,
                        y + j,
                        Math.min(x + i + squareSize, x + width),
                        Math.min(y + j + squareSize, y + height),
                        color
                );
            }
        }
    }

    @Override
    public void onClose() {
        if (Lwib.mc != null) {
            Lwib.mc.setScreen(parent);
        }
    }

    // Custom slider class
    private static class ColorSlider extends AbstractSliderButton {
        private final SliderCallback callback;

        public ColorSlider(int x, int y, int width, int height, Component message, double value, SliderCallback callback) {
            super(x, y, width, height, message, value);
            this.callback = callback;
        }

        @Override
        protected void updateMessage() {

        }

        @Override
        protected void applyValue() {
            callback.onValueChanged(this.value);
        }

        @FunctionalInterface
        interface SliderCallback {
            void onValueChanged(double value);
        }
    }

    @FunctionalInterface
    public interface ColorSelectionCallback {
        void onColorSelected(int color);
    }
}