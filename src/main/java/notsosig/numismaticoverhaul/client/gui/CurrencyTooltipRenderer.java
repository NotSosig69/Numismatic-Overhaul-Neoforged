package notsosig.numismaticoverhaul.client.gui;

import net.minecraft.ChatFormatting;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import notsosig.numismaticoverhaul.currency.CurrencyConverter;

import java.util.ArrayList;
import java.util.List;

public class CurrencyTooltipRenderer {

    public static void renderTooltip(long value, GuiGraphics matrices, Screen screen, Component title, int x, int y) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(title);

        y += 10;

        List<ItemStack> coins = CurrencyConverter.getAsItemStackListRaw(value);

        matrices.pose().translate(0, 0, 500);

        for (int i = 0; i < coins.size(); i++) {
            renderStack(matrices, coins.get(i), tooltip, i, x, y);
        }
        matrices.pose().translate(0, 0, -500);

        if (tooltip.size() == 1) {
            tooltip.add(Component.translatable("numismaticoverhaul.empty").withStyle(ChatFormatting.GRAY));
        }

        matrices.renderComponentTooltip(Minecraft.getInstance().font, tooltip, x, y - 15);
    }

    private static void renderStack(GuiGraphics graphics, ItemStack stack, List<Component> tooltip, int index, int x, int y) {
        tooltip.add(createPlaceholder(String.valueOf(stack.getCount())));

        int localX = x + 8;
        int localY = y - (2 - index) * 10;

        graphics.renderFakeItem(ItemOps.singleCopy(stack), localX, localY);
    }

    private static Component createPlaceholder(String text) {
        return Component.nullToEmpty("§7   " + text + " ");
    }
}
