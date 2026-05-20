package notsosig.numismaticoverhaul.client.gui.purse;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableInt;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.cap.CurrencyHolder;
import notsosig.numismaticoverhaul.currency.Currency;
import notsosig.numismaticoverhaul.currency.CurrencyConverter;
import notsosig.numismaticoverhaul.currency.CurrencyResolver;
import notsosig.numismaticoverhaul.network.RequestPurseActionC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class PurseWidget extends GuiGraphics implements Renderable, GuiEventListener, NarratableEntry {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NumismaticOverhaul.MODID, "textures/gui/purse_widget.png");
    private final Minecraft client;
    private final int x;
    private final int y;

    private boolean active = false;
    private final List<Button> buttons = new ArrayList<>();

    private final MutableInt goldAmount = new MutableInt(0);
    private final MutableInt silverAmount = new MutableInt(0);
    private final MutableInt bronzeAmount = new MutableInt(0);
    private final CurrencyHolder currencyStorage;

    public PurseWidget(int x, int y, Minecraft client, CurrencyHolder currencyStorage, MultiBufferSource.BufferSource pBufferSource) {
        super(client, pBufferSource);
        this.client = client;
        this.x = x;
        this.y = y;

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 10, button -> modifyInBounds(goldAmount, true, Currency.GOLD), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 16, button -> modifyInBounds(goldAmount, false, Currency.GOLD), false));

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 22, button -> modifyInBounds(silverAmount, true, Currency.SILVER), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 28, button -> modifyInBounds(silverAmount, false, Currency.SILVER), false));

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 34, button -> modifyInBounds(bronzeAmount, true, Currency.BRONZE), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 40, button -> modifyInBounds(bronzeAmount, false, Currency.BRONZE), false));

        buttons.add(new AlwaysOnTopTexturedButtonWidget(x + 3, y + 46, 24, 8, 37, 0, 16, TEXTURE, button -> {
            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
                PacketDistributor.sendToServer(RequestPurseActionC2SPacket.extractAll());
            } else if (selectedValue() > 0) {
                PacketDistributor.sendToServer(RequestPurseActionC2SPacket.extract(selectedValue()));
                resetSelectedValue();
            }
        }));

        this.currencyStorage = currencyStorage;
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        if (!active) return;

        RenderSystem.disableDepthTest();
        blit(TEXTURE, x, y, 0, 0, 37, 60);

        for (Button button : buttons) {
            button.render(matrices, mouseX, mouseY, delta);
        }

        matrices.drawString(client.font, Component.literal("" + goldAmount), x + 5, y + 12, 16777215);
        matrices.drawString(client.font, Component.literal("" + silverAmount), x + 5, y + 24, 16777215);
        matrices.drawString(client.font, Component.literal("" + bronzeAmount), x + 5, y + 36, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || client.player.isSpectator()) return false;

        for (Button buttonWidget : buttons) {
            if (buttonWidget.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + 37 && mouseY >= y && mouseY <= y + 57 && active;
    }

    @Override
    public void setFocused(boolean pFocused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    public void toggleActive() {
        active = !active;
    }

    private void modifyInBounds(MutableInt value, boolean add, Currency currency) {
        modifyInBounds(value, Screen.hasShiftDown() ? 10 : 1, add, currency);
    }

    private void modifyInBounds(MutableInt value, int modifyBy, boolean add, Currency currency) {
        long stepSize = currency.getRawValue(1);
        long possibleSteps = (currencyStorage.getValue() - selectedValue()) / stepSize;
        int upperBound = CurrencyConverter.asInt(Math.min(value.intValue() + possibleSteps, 99));

        if (add) value.add(modifyBy);
        else value.subtract(modifyBy);

        if (value.intValue() < 0) value.setValue(0);
        if (value.intValue() > upperBound) value.setValue(upperBound);
    }

    private long selectedValue() {
        return CurrencyResolver.combineValues(new long[]{bronzeAmount.getValue(), silverAmount.getValue(), goldAmount.getValue()});
    }

    private void resetSelectedValue() {
        currencyStorage.silentModify(-selectedValue());

        int oldGoldAmount = goldAmount.intValue();
        int oldSilverAmount = silverAmount.intValue();
        int oldBronzeAmount = bronzeAmount.intValue();

        goldAmount.setValue(0);
        bronzeAmount.setValue(0);
        silverAmount.setValue(0);

        modifyInBounds(goldAmount, oldGoldAmount, true, Currency.GOLD);
        modifyInBounds(silverAmount, oldSilverAmount, true, Currency.SILVER);
        modifyInBounds(bronzeAmount, oldBronzeAmount, true, Currency.BRONZE);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.FOCUSED;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {}

    public static class SmallPurseAdjustButton extends AlwaysOnTopTexturedButtonWidget {
        public SmallPurseAdjustButton(int x, int y, OnPress pressAction, boolean add) {
            super(x, y, 9, 5, add ? 37 : 46, 24, 10, PurseWidget.TEXTURE, pressAction);
        }
    }
}
