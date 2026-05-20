package notsosig.numismaticoverhaul.client.gui;

import io.wispforest.owo.ops.ItemOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import notsosig.numismaticoverhaul.currency.CurrencyConverter;
import notsosig.numismaticoverhaul.item.CurrencyTooltipData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CurrencyTooltipComponent implements ClientTooltipComponent {

    private final CurrencyTooltipData data;
    private final List<Component> text;

    private int widthCache = -1;

    public CurrencyTooltipComponent(CurrencyTooltipData data) {
        this.data = data;
        this.text = new ArrayList<>();

        if (data.original()[0] != -1) {
            CurrencyConverter.getAsItemStackList(data.original()).forEach(stack -> text.add(createPlaceholder(stack.getCount())));
            text.add(Component.nullToEmpty(" "));
        }

        CurrencyConverter.getAsItemStackList(data.value()).forEach(stack -> text.add(createPlaceholder(stack.getCount())));
    }

    @Override
    public int getHeight() {
        return 10 * text.size();
    }

    @Override
    public int getWidth(Font textRenderer) {
        if (widthCache == -1) {
            widthCache = textRenderer.width(text.stream()
                    .max(Comparator.comparingInt(textRenderer::width)).orElse(Component.nullToEmpty("")));
        }
        return widthCache;
    }

    @Override
    public void renderText(Font pFont, int pMouseX, int pMouseY, Matrix4f pMatrix, MultiBufferSource.BufferSource pBufferSource) {
        ClientTooltipComponent.super.renderText(pFont, pMouseX, pMouseY, pMatrix, pBufferSource);
        for (int i = 0; i < text.size(); i++) {
            pFont.drawInBatch(text.get(i), pMouseX, pMouseY + i * 10, -1, true, pMatrix, pBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        }
    }

    @Override
    public void renderImage(Font pFont, int x, int y, GuiGraphics pGuiGraphics) {
        List<ItemStack> originalCoins = data.original()[0] != -1 ? CurrencyConverter.getAsItemStackList(data.original()) : new ArrayList<>();
        List<ItemStack> coins = CurrencyConverter.getAsItemStackList(data.value());

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0, 0, 50);

        for (int i = 0; i < originalCoins.size(); i++) {
            pGuiGraphics.blitSprite(ResourceLocation.fromNamespaceAndPath("minecraft", "container/villager/discount_strikethrough"), x + (originalCoins.get(i).getCount() > 9 ? 14 : 11), y + 3, 9, 2);
            pGuiGraphics.renderItem(ItemOps.singleCopy(originalCoins.get(i)), x - 4, y - 5 + i * 10);
        }

        for (int i = 0; i < coins.size(); i++) {
            pGuiGraphics.renderItem(ItemOps.singleCopy(coins.get(i)), x - 4, y - 5 + i * 10 + (originalCoins.isEmpty() ? 0 : 10 + originalCoins.size() * 10));
        }

        pGuiGraphics.pose().popPose();
    }

    private static Component createPlaceholder(int count) {
        return Component.literal("§7   " + count + " ").withStyle(ChatFormatting.GRAY);
    }
}
