package me.starzebra.lwib.buttons;

import me.starzebra.lwib.Lwib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class InvButtonEditorScreen extends Screen {

    public static final ResourceLocation INVENTORY = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
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
    private final int editorWidth = 107;

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
            button.setX(leftPos + button.offsetX);
            button.setY(topPos + button.offsetY);
        }

        selectedButton = null;
        newBtnPos = null;
        this.deleteButton = null;
        this.selectItemButton = null;
        this.selectColorButton = null;
        this.commandBox.setVisible(false);
    }

    private void initColorPicker() {
        int currentColor = selectedButton != null ? selectedButton.color : 0xFFFFFFFF;
        if (newBtnPos == null && selectedButton == null) return;

        int x = (newBtnPos != null) ? (int) newBtnPos.x : selectedButton.getX();
        int y = (newBtnPos != null) ? (int) newBtnPos.y : selectedButton.getY();

        this.selectColorButton = Button.builder(Component.literal("C"), button -> {
            // Open the color picker screen
            ColorPickerScreen colorScreen = new ColorPickerScreen(this, currentColor, (selectedColor) -> {
                // This callback is called when a color is selected
                if (selectedButton != null) {
                    selectedButton.color = selectedColor;
                }
            });
            Lwib.mc.setScreen(colorScreen);
        }).bounds(x + editorWidth - 16 - 15 - 15, y, 14, 15).build();

        this.selectColorButton.setTooltip(Tooltip.create(Component.literal("Change button color")));
    }

    private void initItemSelector() {
        this.selectItemButton = Button.builder(Component.literal("I"), button -> {
            ItemSelectionScreen itemScreen = new ItemSelectionScreen(this, (item) -> {
                if (item != null) {
                    selectedButton.icon = item;
                }
            });
            Lwib.setScreen(itemScreen);
        }).bounds(selectedButton.getX() + editorWidth - 16 - 15, selectedButton.getY(), 14, 15).build();

        this.selectItemButton.setTooltip(Tooltip.create(Component.literal("Select display item").withColor(Color.WHITE.getRGB())));
    }

    private void initDelete() {
        this.deleteButton = Button.builder(Component.empty(), button -> {
            for (InventoryButton btn : Lwib.inventoryButtons) {
                if (btn == selectedButton) {
                    btn.markedForDeletion = true;
                }
            }
            this.updateScreen();
        }).bounds(selectedButton.getX() + editorWidth - 16, selectedButton.getY(), 14, 15).build();

        this.deleteButton.setTooltip(Tooltip.create(Component.literal("Delete this button").withColor(Color.RED.getRGB())));
        this.deleteButton.setMessage(Component.literal("D").withColor(Color.RED.getRGB()));

        initItemSelector();

        initColorPicker();
    }

    private void initEditor() {
        String string = this.commandBox != null ? this.commandBox.getValue() : "";
        this.commandBox = new EditBox(Lwib.mc.font, 0, 0, editorWidth - 1, 20, Component.empty());
        this.commandBox.setVisible(false);
        this.commandBox.setMaxLength(50);
        this.commandBox.setValue(string);
        this.commandBox.setHint(Component.literal("/visit iCop"));
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics context){
        //don't apply blur effect
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if(this.commandBox.keyPressed(keyEvent)){
            return true;
        } else if (this.commandBox.isFocused() && this.commandBox.isVisible() && keyEvent.input() == GLFW.GLFW_KEY_ENTER) {

            if (selectedButton != null) {
                selectedButton.command = this.commandBox.getValue();
                this.updateScreen();
                return true;
            }

            if (this.commandBox.getValue().trim().replace("/", "").isEmpty()) {
                return true;
            }

            var button = new InventoryButton((int) (newBtnPos.x - leftPos), (int) (newBtnPos.y - topPos), this.commandBox.getValue().trim());
            Lwib.inventoryButtons.add(button);
            this.updateScreen();

            return true;
        }else if (this.commandBox.isFocused() && this.commandBox.isVisible() && !keyEvent.isEscape()){
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        if(this.commandBox.charTyped(characterEvent)){
            return true;
        }
        return super.charTyped(characterEvent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean isDoubleClick) {

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
                if (button.markedForDeletion) continue;
                var buttonBounds = button.getRectangle();
                if (buttonBounds.containsPoint((int) mouseEvent.x(), (int) mouseEvent.y())) {

                    newBtnPos = null;
                    selectedButton = button;
                    this.commandBox.moveCursorToStart(false);
                    this.commandBox.setVisible(true);
                    this.commandBox.setPosition(button.getX() - 1, button.getY() + 17);
                    this.commandBox.setValue(button.command);

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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(Lwib.mc.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        for (InventoryButton button : Lwib.inventoryButtons) {
            if (button.markedForDeletion) continue;
            guiGraphics.fill(RenderPipelines.GUI, button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), button.color);
            guiGraphics.renderItem(button.icon, button.getX(), button.getY());
        }

        // Editor
        if (newBtnPos != null || selectedButton != null) {
            int x = (newBtnPos != null) ? (int) newBtnPos.x : selectedButton.getX();
            int y = (newBtnPos != null) ? (int) newBtnPos.y : selectedButton.getY();

            renderEditor(guiGraphics, x, y);
        }

        if (this.deleteButton != null) {
            this.deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.selectItemButton != null) {
            this.selectItemButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.selectColorButton != null) {
            this.selectColorButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        this.commandBox.render(guiGraphics, mouseX, mouseY, partialTick);

    }

    private void renderEditor(GuiGraphics guiGraphics, int x, int y) {
        // Background
        guiGraphics.fill(x - 2, y - 2, x + editorWidth, y + 39, 0xFF222222);

        int color = 0xFFFFFFFF;
        String str = "Create a button";
        ItemStack icon = Items.GRAY_DYE.getDefaultInstance();

        if (selectedButton != null) {
            str = "Editing...";
            color = selectedButton.color;
            icon = selectedButton.icon;
        }

        guiGraphics.drawString(Lwib.mc.font, str, x + 19, y + 4, 0xFFFFFFFF);

        // Button
        guiGraphics.fill(x, y, x + 16, y + 16, color);

        // Icon
        guiGraphics.renderItem(icon, x, y);

    }

    @Override
    public void onClose() {
        super.onClose();
        Lwib.inventoryButtons.removeIf(btn -> btn.markedForDeletion);
        Lwib.serializeButtons();
    }
}
