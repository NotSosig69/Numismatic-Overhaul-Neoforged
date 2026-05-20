package notsosig.numismaticoverhaul.client.gui.purse;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AlwaysOnTopTexturedButtonWidget extends Button {

    private final int u;
    private final int v;
    private final int hoveredVOffset;
    private final ResourceLocation texture;

    public AlwaysOnTopTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, ResourceLocation texture, OnPress pressAction) {
        super(x, y, width, height, Component.empty(), pressAction, DEFAULT_NARRATION);

        this.u = u;
        this.v = v;
        this.hoveredVOffset = hoveredVOffset;
        this.texture = texture;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.v;
        if (this.isHoveredOrFocused()) {
            i += this.hoveredVOffset;
        }
        RenderSystem.disableDepthTest();
        pGuiGraphics.blit(texture, this.getX(), this.getY(), this.u, i, this.width, this.height);
        RenderSystem.enableDepthTest();
    }
}
