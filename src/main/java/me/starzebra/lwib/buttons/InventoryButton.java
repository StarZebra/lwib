package me.starzebra.lwib.buttons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.starzebra.lwib.Lwib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class InventoryButton extends AbstractWidget {

    public int offsetX;
    public int offsetY;
    public String command;
    private long lastClicked = 0L;
    public boolean markedForDeletion = false;

    public static final Codec<InventoryButton> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("offsetX").forGetter(InventoryButton::getOffsetX),
            Codec.INT.fieldOf("offsetY").forGetter(InventoryButton::getOffsetY),
            Codec.STRING.fieldOf("command").forGetter(InventoryButton::getCommand)
    ).apply(instance, InventoryButton::new));

    public InventoryButton(int x, int y, String command){
        super(-50, -50, 15, 15, Component.empty());
        this.offsetX = x;
        this.offsetY = y;
        this.command = command;
    }

    private void updateScreenPos(){
        Screen screen = Lwib.mc.screen;

        var leftPos = (Lwib.mc.getWindow().getGuiScaledWidth() - 176) / 2;
        var topPos = (Lwib.mc.getWindow().getGuiScaledHeight() - 166) / 2;

        if (screen instanceof AbstractContainerScreen<?>) {
            this.setX(leftPos + offsetX);
            this.setY(topPos + offsetY);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        //super.onClick(event, isDoubleClick);
        if(System.currentTimeMillis() - lastClicked < 100) return; //Add a small delay to not get banned for spamming commands
        this.lastClicked = System.currentTimeMillis();
        if(isActive()){
            if(Lwib.mc.getConnection() == null) return; //IntelliJ required null check :D
            Lwib.mc.getConnection().sendCommand(command.replace("/",""));
        }

    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(!isActive()) return;
        this.updateScreenPos();

        guiGraphics.fill(RenderPipelines.GUI, getX(), getY(), getX() + this.width, getY() + this.height, ARGB.color(255, 0xFF99FF));

    }

    public boolean isActive() {
        return !command.trim().isEmpty();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        //ignored
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public String getCommand() {
        return command;
    }

}
