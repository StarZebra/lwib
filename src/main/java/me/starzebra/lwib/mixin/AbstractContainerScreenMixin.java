package me.starzebra.lwib.mixin;

import me.starzebra.lwib.Lwib;
import me.starzebra.lwib.buttons.InventoryButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

    @Shadow protected int leftPos;

    @Shadow protected int imageWidth;

    @Shadow protected int topPos;

    @Shadow protected int imageHeight;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void lwib$initInvButtons(CallbackInfo io){
        AbstractContainerScreen<?> screenType = (AbstractContainerScreen<?>) (Object) this;
        if(Lwib.mc == null || Lwib.mc.player == null || Lwib.mc.player.isCreative()) return;

        for(InventoryButton invButton : Lwib.inventoryButtons){
            if(!(screenType instanceof InventoryScreen)){
                if(invButton.getX() >= this.leftPos && invButton.getX() <= this.leftPos + this.imageWidth
                        && invButton.getY() >= this.topPos && invButton.getY() <= this.topPos + this.imageHeight){
                    continue; // Don't render buttons inside the player inventory when the screen isn't a player inventory
                }
            }

            this.addRenderableWidget(invButton);

        }
    }

}
