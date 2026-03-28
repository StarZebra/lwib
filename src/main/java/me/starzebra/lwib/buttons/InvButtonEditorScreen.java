package me.starzebra.lwib.buttons;

import me.starzebra.lwib.Lwib;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class InvButtonEditorScreen extends Screen {

    public static final Identifier INVENTORY = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");
    private final int imageWidth = 176;
    private final int imageHeight = 166;
    private int leftPos = (Lwib.mc.getWindow().getGuiScaledWidth() - imageWidth) / 2;
    private int topPos = (Lwib.mc.getWindow().getGuiScaledHeight() - imageHeight) / 2;
    private EditBox commandBox;
    private Button deleteButton;
    private Button selectItemButton;
    private Button selectColorButton;
    private Vec2 newBtnPos;
    private InventoryButton selectedButton;
    private final int EDITOR_WIDTH = 120;

    public InvButtonEditorScreen() {
        super(Component.literal("Edit inventory buttons"));
    }

    @Override
    protected void init() {
        super.init();
        initEditor();
        updateScreen();
    }

    private void updateScreen(){
        leftPos = (Lwib.mc.getWindow().getGuiScaledWidth() - imageWidth) / 2;
        topPos = (Lwib.mc.getWindow().getGuiScaledHeight() - imageHeight) / 2;

        for (InventoryButton button : Lwib.inventoryButtons) {

            if (button.isBottomAnchored()) {
                button.setY(topPos + imageHeight + button.getOffsetY());
            } else {
                button.setY(topPos + button.getOffsetY());
            }
            button.setX(leftPos + button.getOffsetX());
        }

        selectedButton = null;
        newBtnPos = null;
        this.deleteButton = null;
        this.selectItemButton = null;
        this.selectColorButton = null;
        this.commandBox.setVisible(false);
    }

    private void initColorPicker() {
        int currentColor = selectedButton != null ? selectedButton.getColor() : 0xFFFFFFFF;
        if (newBtnPos == null && selectedButton == null) return;

        this.selectColorButton = Button.builder(Component.literal("C"), _ -> {
            // Open the color picker screen
            ColorPickerScreen colorScreen = new ColorPickerScreen(this, currentColor, (selectedColor) -> {
                // This callback is called when a color is selected
                if (selectedButton != null) {
                    selectedButton.setColor(selectedColor);
                }
            });
            Lwib.mc.setScreen(colorScreen);
        }).bounds(0, 0, 14, 15).build();

        this.selectColorButton.setTooltip(Tooltip.create(Component.literal("Change button color")));
    }

    private void initItemSelector() {
        this.selectItemButton = Button.builder(Component.literal("I"), _ -> {
            ItemSelectionScreen itemScreen = new ItemSelectionScreen(this, (item) -> {
                if (item != null) {
                    selectedButton.setIcon(item);
                }
            });
            Lwib.setScreen(itemScreen);
        }).bounds(0, 0, 14, 15).build();

        this.selectItemButton.setTooltip(Tooltip.create(Component.literal("Select display item").withColor(Color.WHITE.getRGB())));
    }

    private void initDelete() {
        this.deleteButton = Button.builder(Component.empty(), _ -> {
            for (InventoryButton btn : Lwib.inventoryButtons) {
                if (btn == selectedButton) {
                    btn.setMarkedForDeletion(true);
                }
            }
            this.updateScreen();
        }).bounds(0, 0, 14, 15).build();

        this.deleteButton.setTooltip(Tooltip.create(Component.literal("Delete this button").withColor(Color.RED.getRGB())));
        this.deleteButton.setMessage(Component.literal("D").withColor(Color.RED.getRGB()));

        initItemSelector();

        initColorPicker();
    }

    private void initEditor() {
        String string = this.commandBox != null ? this.commandBox.getValue() : "";
        this.commandBox = new EditBox(Lwib.mc.font, 0, 0, EDITOR_WIDTH - 4, 20, Component.empty());
        this.commandBox.setVisible(false);
        this.commandBox.setMaxLength(50);
        this.commandBox.setValue(string);
        this.commandBox.setHint(Component.literal("/visit iCop"));
    }

    @Override
    protected void extractBlurredBackground(@NonNull GuiGraphicsExtractor graphics) {
        //don't apply blur effect
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent) {
        if(this.commandBox.keyPressed(keyEvent)){
            return true;
        } else if (this.commandBox.isFocused() && this.commandBox.isVisible() && keyEvent.input() == GLFW.GLFW_KEY_ENTER) {

            if (selectedButton != null) {
                selectedButton.setCommand(this.commandBox.getValue());
                this.updateScreen();
                return true;
            }

            if (this.commandBox.getValue().trim().replace("/", "").isEmpty()) {
                return true;
            }

            InventoryButton button = getInventoryButton();

            Lwib.inventoryButtons.add(button);
            this.updateScreen();

            return true;
        }else if (this.commandBox.isFocused() && this.commandBox.isVisible() && !keyEvent.isEscape()){
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    private @NonNull InventoryButton getInventoryButton() {
        boolean isBottomHalf = newBtnPos.y > (topPos + (imageHeight / 2f));

        InventoryButton button;

        if (isBottomHalf) {
            button = new InventoryButton((int) (newBtnPos.x - leftPos), (int) (newBtnPos.y - (topPos + imageHeight)), this.commandBox.getValue().trim(), true);
        } else {
            button = new InventoryButton((int) (newBtnPos.x - leftPos), (int) (newBtnPos.y - topPos), this.commandBox.getValue().trim(), false);
        }
        return button;
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent characterEvent) {
        if(this.commandBox.charTyped(characterEvent)){
            return true;
        }
        return super.charTyped(characterEvent);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseEvent, boolean isDoubleClick) {

        if(this.commandBox != null){
            if (this.commandBox.mouseClicked(mouseEvent, isDoubleClick)) {
                this.commandBox.setFocused(true);
                return true;
            }
            this.commandBox.setFocused(false);
        }

        if (this.deleteButton != null) {
            if (this.deleteButton.mouseClicked(mouseEvent, isDoubleClick)) {
                return true;
            }
        }

        if (this.selectItemButton != null) {
            if (this.selectItemButton.mouseClicked(mouseEvent, isDoubleClick)) {
                return true;
            }
        }

        if (this.selectColorButton != null) {
            if (this.selectColorButton.mouseClicked(mouseEvent, isDoubleClick)) {
                return true;
            }
        }

        if(mouseEvent.button() == 0){
            var clickedPos = new Vec2((int) mouseEvent.x(), (int) mouseEvent.y());
            if(this.commandBox == null) return true;

            if (selectedButton != null && !selectedButton.getRectangle().containsPoint((int) clickedPos.x, (int) clickedPos.y)) {
                selectedButton = null;
                newBtnPos = null;
            }

            // Editing a button
            for (InventoryButton button : Lwib.inventoryButtons) {
                if (button.isMarkedForDeletion()) continue;
                var buttonBounds = button.getRectangle();
                if (buttonBounds.containsPoint((int) mouseEvent.x(), (int) mouseEvent.y())) {

                    newBtnPos = null;
                    selectedButton = button;
                    this.commandBox.moveCursorToStart(false);
                    this.commandBox.setVisible(true);
                    this.commandBox.setPosition(button.getX() - 42, button.getY() + (int) (button.getSize() * 16));
                    this.commandBox.setValue(button.getCommand());

                    initDelete();

                    return true;
                }
            }

            // Creating a new button
            this.commandBox.setVisible(true);
            this.commandBox.setPosition((int) clickedPos.x - 1, (int) clickedPos.y + 17);
            this.commandBox.setValue("/");
            this.deleteButton = null;
            this.selectItemButton = null;
            this.selectColorButton = null;
            newBtnPos = clickedPos;

        }

        return super.mouseClicked(mouseEvent, isDoubleClick);

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selectedButton == null) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        float prevSize = selectedButton.getSize();
        float step = (float) (0.1 * scrollY);
        selectedButton.setButtonSize(prevSize + step);
        return true;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(Lwib.mc.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        for (InventoryButton button : Lwib.inventoryButtons) {
            if (button.isMarkedForDeletion()) continue;
            graphics.fill(RenderPipelines.GUI, button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), button.getColor());
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(16 * button.getSize() / 2 - 8, 16 * button.getSize() / 2 - 8);
            graphics.item(button.getIcon().create(), button.getX(), button.getY());
            pose.popMatrix();
        }

        // Editor
        if (newBtnPos != null || selectedButton != null) {
            int x = (newBtnPos != null) ? (int) newBtnPos.x : selectedButton.getX();
            int y = (newBtnPos != null) ? (int) newBtnPos.y : selectedButton.getY();

            renderEditor(graphics, x, y);

            graphics.centeredText(Lwib.mc.font, Component.literal("Hint: You can resize a button with scroll wheel while editing it."), this.width / 2, 20, 0xFFAAAAAA);
        }

        if (this.deleteButton != null) {
            this.deleteButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        if (this.selectItemButton != null) {
            this.selectItemButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        if (this.selectColorButton != null) {
            this.selectColorButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        this.commandBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }


    private void renderEditor(GuiGraphicsExtractor graphics, int x, int y) {
        int color = 0xFFFFFFFF;
        String str = "Create a button";
        ItemStack icon = Items.GRAY_DYE.getDefaultInstance();
        float size = 1f;

        if (selectedButton != null) {
            str = "Editing...";
            color = selectedButton.getColor();
            icon = selectedButton.getIcon().create();
            size = selectedButton.getSize();
        }

        int iconOffset = (int) (size * 8);

        final int topPadding = 20;
        final int editorH = (int) (size * 16) + this.commandBox.getHeight() + 4;

        final int startX = x - EDITOR_WIDTH / 2 + iconOffset;
        final int startY = y - topPadding;

        final int endX = x + EDITOR_WIDTH / 2 + iconOffset;

        // Background
        graphics.fill(startX, startY, x + EDITOR_WIDTH / 2 + iconOffset, y + editorH, 0xFF222222);

        this.commandBox.setPosition(startX + 2, y + 2 + (int) (size * 16));

        if (selectedButton != null) {
            this.selectColorButton.setPosition(endX - 15 - 15, y - 8 - topPadding / 2);
            this.deleteButton.setPosition(endX - 15, y - 8 - topPadding / 2);
            this.selectItemButton.setPosition(endX - 15 - 30, y - 8 - topPadding / 2);
        }

        // Text
        graphics.text(Lwib.mc.font, str, startX + 4, y - 4 - topPadding / 2, 0xFFFFFFFF);

        // Button
        graphics.fill(x, y, (int) (x + 16 * size), (int) (y + 16 * size), color);

        // Icon
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(16 * size / 2 - 8, 16 * size / 2 - 8);
        graphics.item(icon, x, y);
        pose.popMatrix();

    }

    @Override
    public void onClose() {
        super.onClose();
        Lwib.inventoryButtons.removeIf(InventoryButton::isMarkedForDeletion);
        Lwib.serializeButtons();
    }
}
