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
import net.minecraft.world.phys.Vec2;

import java.awt.*;

public class InvButtonEditorScreen extends Screen {

    public static final ResourceLocation INVENTORY = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
    private int imageWidth = 176;
    private int imageHeight = 166;
    private int leftPos = (Lwib.mc.getWindow().getGuiScaledWidth() - imageWidth) / 2;
    private int topPos = (Lwib.mc.getWindow().getGuiScaledHeight() - imageHeight) / 2;
    private EditBox commandBox;
    private Button deleteButton;
    private boolean isEditingButton = false;
    private Vec2 editingPos;

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

        isEditingButton = false;
        editingPos = null;
        this.deleteButton = null;
        this.commandBox.setVisible(false);
    }

    private void initDelete() {
        this.deleteButton = Button.builder(Component.empty(), button -> {

            for (InventoryButton btn : Lwib.inventoryButtons) {
                if (btn.getX() == (int) editingPos.x) {
                    btn.markedForDeletion = true;
                }
            }

            this.updateScreen();
        }).bounds((int) editingPos.x + 101 - 16, (int) editingPos.y, 14, 15).build();

        this.deleteButton.setTooltip(Tooltip.create(Component.literal("Delete this button!").withColor(Color.RED.getRGB())));
        this.deleteButton.setMessage(Component.literal("D"));
    }

    private void initEditor() {
        String string = this.commandBox != null ? this.commandBox.getValue() : "";
        this.commandBox = new EditBox(Lwib.mc.font, 0, 0, 100, 20, Component.empty());
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
        }else if (this.commandBox.isFocused() && this.commandBox.isVisible() && keyEvent.input() == 257){

            if (isEditingButton) {
                Lwib.mc.player.displayClientMessage(Component.literal("Edited button"), true);
                var button = Lwib.inventoryButtons.stream().filter(btn -> btn.getX() == (int) editingPos.x).findFirst().orElseThrow();
                button.command = this.commandBox.getValue();
                this.updateScreen();
                return true;
            }

            Lwib.mc.player.displayClientMessage(Component.literal("Saved a new button"), false);
            var button = new InventoryButton((int) (editingPos.x - leftPos), (int) (editingPos.y - topPos), this.commandBox.getValue());
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
    public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean bl) {

        if(this.commandBox != null){
            if(this.commandBox.mouseClicked(mouseEvent, bl)){
                this.commandBox.setFocused(true);
                return true;
            }
            this.commandBox.setFocused(false);
        }

        if (this.deleteButton != null) {
            if (this.deleteButton.mouseClicked(mouseEvent, bl)) {
                return true;
            }
        }

        if(mouseEvent.button() == 0){
            var clickedPos = new Vec2((float) mouseEvent.x(), (float) mouseEvent.y());
            if(this.commandBox == null) return true;

            for (InventoryButton button : Lwib.inventoryButtons) {
                if (button.markedForDeletion) continue;
                var buttonBounds = button.getRectangle();
                if (buttonBounds.containsPoint((int) mouseEvent.x(), (int) mouseEvent.y())){

                    isEditingButton = true;
                    editingPos = new Vec2(button.getX(), button.getY());
                    this.commandBox.moveCursorToStart(false);
                    this.commandBox.setVisible(true);
                    this.commandBox.setPosition(button.getX() - 1, button.getY() + 17);
                    this.commandBox.setValue(button.command);

                    initDelete();

                    return true;
                }
            }

            if (editingPos != null && clickedPos.distanceToSqr(editingPos) > 200){
                isEditingButton = false;
                editingPos = null;
            }

            if (!isEditingButton){
                editingPos = clickedPos;
                this.commandBox.setVisible(true);
                this.commandBox.setPosition((int) editingPos.x - 1, (int) editingPos.y + 17);
                this.commandBox.setValue("/");
                this.deleteButton = null;
            }
        }

        return super.mouseClicked(mouseEvent, bl);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float f) {
        super.render(guiGraphics, x, y, f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        for (InventoryButton button : Lwib.inventoryButtons) {
            if (button.markedForDeletion) continue;
            guiGraphics.fill(RenderPipelines.GUI, button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xFFFF99FF);
        }


        if (editingPos != null) {

            //Background
            guiGraphics.fill((int) editingPos.x - 2, (int) editingPos.y - 2, (int) editingPos.x + 101, (int) editingPos.y + 39, 0xFF222222);

            //Button
            guiGraphics.fill((int) editingPos.x, (int) editingPos.y,  (int) editingPos.x + 15, (int) editingPos.y + 15, 0xFFFFFFFF);

            String str = "Create a button";

            if (isEditingButton) {
                str = "Editing...";
            }

            guiGraphics.drawString(Lwib.mc.font, str, (int) editingPos.x + 18, (int) editingPos.y + 4, 0xFFFFFFFF);

        }

        if (this.deleteButton != null) {
            this.deleteButton.render(guiGraphics, x, y, f);
        }
        this.commandBox.render(guiGraphics, x, y, f);

    }

    @Override
    public void onClose() {
        super.onClose();
        Lwib.inventoryButtons.removeIf(btn -> btn.markedForDeletion);
        Lwib.serializeButtons();
    }
}
