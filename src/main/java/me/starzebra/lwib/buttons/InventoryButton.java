package me.starzebra.lwib.buttons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.starzebra.lwib.Lwib;
import me.starzebra.lwib.mixin.accessor.AbstractContainerScreenAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;

public class InventoryButton extends AbstractWidget {

    private final int offsetX;
    private final int offsetY;
    private String command;
    private long lastClicked = 0L;
    private boolean markedForDeletion = false;
    private boolean isBottomAnchored;
    private int color;
    private ItemStackTemplate icon;
    private float size;

    public static final Codec<InventoryButton> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("offsetX").forGetter(InventoryButton::getOffsetX),
            Codec.INT.fieldOf("offsetY").forGetter(InventoryButton::getOffsetY),
            Codec.STRING.fieldOf("command").forGetter(InventoryButton::getCommand),
            Codec.INT.fieldOf("bgColor").forGetter(InventoryButton::getColor),
            ItemStackTemplate.CODEC.fieldOf("icon").forGetter(InventoryButton::getIcon),
            Codec.FLOAT.fieldOf("size").forGetter(InventoryButton::getSize),
            Codec.BOOL.fieldOf("bottomAnchored").forGetter(InventoryButton::isBottomAnchored)
    ).apply(instance, InventoryButton::new));

    public InventoryButton(int x, int y, String command, int color, ItemStackTemplate icon, float size, boolean bottomAnchored) {
        super(-50, -50, 16, 16, Component.empty());
        this.offsetX = x;
        this.offsetY = y;
        this.command = command;
        this.color = color;
        this.icon = icon;
        this.size = size;
        this.isBottomAnchored = bottomAnchored;
        setWidth((int) (width * size));
        setHeight((int) (height * size));
    }

    public InventoryButton(int x, int y, String command, boolean bottomAnchored) {
        super(-50, -50, 16, 16, Component.empty());
        this.offsetX = x;
        this.offsetY = y;
        this.command = command;
        this.color = 0xFFFFFFFF;
        this.icon = ItemStackTemplate.fromNonEmptyStack(Items.GRAY_DYE.getDefaultInstance());
        this.size = 1f;
        this.isBottomAnchored = bottomAnchored;
    }

    private void updateScreenPos(){
        Screen screen = Lwib.mc.screen;

        if (screen instanceof AbstractContainerScreen<?> container) {
            var handledScreen = (AbstractContainerScreenAccessor) container;
            int leftPos = handledScreen.getX();
            int topPos = handledScreen.getY();
            int imageHeight = handledScreen.getHeight();

            if (isBottomAnchored) {
                this.setY(topPos + imageHeight + offsetY);
            } else {
                this.setY(topPos + offsetY);
            }
            this.setX(leftPos + offsetX);

        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (!isActive()) return;
        this.updateScreenPos();

        graphics.fill(RenderPipelines.GUI, getX(), getY(), getX() + this.width, getY() + this.height, this.color);

        if (this.icon != null) {

            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate((float) width / 2 - 8, (float) width / 2 - 8); // Center the icon
            graphics.item(this.icon.create(), getX(), getY());
            pose.popMatrix();
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        //super.onClick(event, isDoubleClick);
        if (System.currentTimeMillis() - lastClicked < 100)
            return; // Add a small delay to not get banned for spamming commands
        this.lastClicked = System.currentTimeMillis();
        if(isActive()){
            if (Lwib.mc.getConnection() == null) return;
            if (isCommand()) {
                Lwib.mc.getConnection().sendCommand(command.substring(1));
            } else {
                Lwib.mc.getConnection().sendChat(command);
            }
        }
    }

    public boolean isActive() {
        return !command.trim().isEmpty();
    }

    private boolean isCommand() {
        return command.trim().startsWith("/");
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // ignored
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // Remove the click sound
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

    public ItemStackTemplate getIcon() {
        return icon;
    }

    public float getSize() {
        return size != 0f ? size : 1f;
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    public void setMarkedForDeletion(boolean markedForDeletion) {
        this.markedForDeletion = markedForDeletion;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setIcon(ItemStackTemplate icon) {
        this.icon = icon;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setButtonSize(float size) {
        if ((int) size > 4 || (int) size < 1) return;
        this.size = size;
        setWidth((int) (16 * size));
        setHeight((int) (16 * size));
    }

    public boolean isBottomAnchored() {
        return isBottomAnchored;
    }

    public void setBottomAnchored(boolean bottomAnchored) {
        isBottomAnchored = bottomAnchored;
    }
}
