package notsosig.numismaticoverhaul.client.gui.purse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import notsosig.numismaticoverhaul.cap.CurrencyHolder;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.client.gui.CurrencyTooltipRenderer;
import notsosig.numismaticoverhaul.currency.Currency;

public class PurseButton extends Button {

    private static final int U = 62;
    private static final int V_NORMAL = 0;
    private static final int V_HOVERED = 13;

    private final CurrencyHolder currencyStorage;
    private final Screen parent;
    private final Component TOOLTIP_TITLE;

    public PurseButton(int x, int y, OnPress pressAction, Player player, Screen parent) {
        super(x, y, 11, 13, Component.empty(), pressAction, DEFAULT_NARRATION);
        this.currencyStorage = CurrencyHolderAttacher.getCurrencyHolder(player);
        this.parent = parent;
        this.TOOLTIP_TITLE = Component.translatable("gui.numismaticoverhaul.purse_title")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Currency.GOLD.getNameColor())));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Minecraft.getInstance().player.isSpectator()) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int v = this.isHovered() ? V_HOVERED : V_NORMAL;
        pGuiGraphics.blit(PurseWidget.TEXTURE, this.getX(), this.getY(), U, v, this.width, this.height);
        if (this.isHovered())
            CurrencyTooltipRenderer.renderTooltip(
                    currencyStorage.getValue(),
                    pGuiGraphics, parent,
                    TOOLTIP_TITLE,
                    this.getX() + 14, this.getY() + 5);
    }
}
