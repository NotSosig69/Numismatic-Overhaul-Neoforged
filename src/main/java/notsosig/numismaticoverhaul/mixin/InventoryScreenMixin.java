package notsosig.numismaticoverhaul.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.client.gui.purse.PurseButton;
import notsosig.numismaticoverhaul.client.gui.purse.PurseWidget;
import notsosig.numismaticoverhaul.config.NOClientConfig;
import notsosig.numismaticoverhaul.network.RequestPurseActionC2SPacket;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    public InventoryScreenMixin(InventoryMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }

    private PurseWidget numismatic$purse;
    private PurseButton numismatic$button;

    @Inject(method = "init", at = @At("TAIL"))
    public void addButton(CallbackInfo ci) {
        int purseX = NOClientConfig.CLIENT.pursePositionX.get();
        int purseY = NOClientConfig.CLIENT.pursePositionY.get();

        numismatic$purse = new PurseWidget(this.leftPos + purseX, this.topPos + purseY, minecraft,
                CurrencyHolderAttacher.getCurrencyHolder(minecraft.player),
                this.minecraft.renderBuffers().bufferSource());

        numismatic$button = new PurseButton(this.leftPos + purseX + 29, this.topPos + purseY - 14, button -> {
            if (Screen.hasShiftDown()) {
                PacketDistributor.sendToServer(RequestPurseActionC2SPacket.storeAll());
            } else {
                numismatic$purse.toggleActive();
            }
        }, minecraft.player, this);

        this.addRenderableWidget(numismatic$button);
    }

    @Inject(method = {"lambda$init$0"}, at = @At("TAIL"))
    private void updateWidgetPosition(Button button, CallbackInfo ci) {
        int purseX = NOClientConfig.CLIENT.pursePositionX.get();
        int purseY = NOClientConfig.CLIENT.pursePositionY.get();
        this.numismatic$button.setPosition(this.leftPos + purseX + 29, this.topPos + purseY - 14);
        this.numismatic$purse = new PurseWidget(this.leftPos + purseX, this.topPos + purseY, minecraft,
                CurrencyHolderAttacher.getCurrencyHolder(minecraft.player),
                minecraft.renderBuffers().bufferSource());
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        if (numismatic$purse != null) numismatic$purse.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouse(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (numismatic$purse != null && numismatic$purse.mouseClicked(mouseX, mouseY, button)) cir.setReturnValue(true);
    }

    @Override
    protected void renderTooltip(GuiGraphics matrices, int x, int y) {
        if (numismatic$purse != null && numismatic$purse.isMouseOver(x, y)) return;
        super.renderTooltip(matrices, x, y);
    }
}
