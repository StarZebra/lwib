package me.starzebra.lwib.buttons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.starzebra.lwib.Lwib;
import me.starzebra.lwib.mixin.accessor.AbstractContainerScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InventoryButton extends AbstractWidget {

    public int offsetX;
    public int offsetY;
    public String command;
    private long lastClicked = 0L;
    public boolean markedForDeletion = false;
    public int color;
    public ItemStack icon;

    public static final Codec<InventoryButton> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("offsetX").forGetter(InventoryButton::getOffsetX),
            Codec.INT.fieldOf("offsetY").forGetter(InventoryButton::getOffsetY),
            Codec.STRING.fieldOf("command").forGetter(InventoryButton::getCommand),
            Codec.INT.fieldOf("bgColor").forGetter(InventoryButton::getColor),
            ItemStack.CODEC.fieldOf("icon").forGetter(InventoryButton::getIcon)
    ).apply(instance, InventoryButton::new));

    public InventoryButton(int x, int y, String command, int color, ItemStack icon) {
        super(-50, -50, 16, 16, Component.empty());
        this.offsetX = x;
        this.offsetY = y;
        this.command = command;
        this.color = color;
        this.icon = icon;
    }

    public InventoryButton(int x, int y, String command){
        super(-50, -50, 16, 16, Component.empty());
        this.offsetX = x;
        this.offsetY = y;
        this.command = command;
        this.color = 0xFFFFFFFF;
        this.icon = Items.GRAY_DYE.getDefaultInstance();
    }

    private void updateScreenPos(){
        Screen screen = Lwib.mc.screen;

        if (screen instanceof AbstractContainerScreen<?> container) {
            var handledScreen = (AbstractContainerScreenAccessor) container;
            int leftPos = handledScreen.getX();
            int topPos = handledScreen.getY();

            this.setX(leftPos + offsetX);
            this.setY(topPos + offsetY);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        //super.onClick(event, isDoubleClick);
        if (System.currentTimeMillis() - lastClicked < 100)
            return; // Add a small delay to not get banned for spamming commands
        this.lastClicked = System.currentTimeMillis();
        if(isActive()){
            if (Lwib.mc.getConnection() == null) return; // IntelliJ required null check :D
            Lwib.mc.getConnection().sendChat(command);
        }

    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(!isActive()) return;
        this.updateScreenPos();

        guiGraphics.fill(RenderPipelines.GUI, getX(), getY(), getX() + this.width, getY() + this.height, this.color);

        if (this.icon != null) {
            guiGraphics.renderItem(this.icon, getX(), getY());
        }

    }

    public boolean isActive() {
        return !command.trim().isEmpty();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // ignored
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

    public int getColor() {
        return color;
    }

    public ItemStack getIcon() {
        return icon;
    }

}
