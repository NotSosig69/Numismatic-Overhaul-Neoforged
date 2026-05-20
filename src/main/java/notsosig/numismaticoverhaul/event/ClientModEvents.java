package notsosig.numismaticoverhaul.event;

import io.wispforest.owo.ui.parsing.UIModelLoader;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import notsosig.numismaticoverhaul.client.gui.CurrencyTooltipComponent;
import notsosig.numismaticoverhaul.item.CurrencyTooltipData;

public class ClientModEvents {

    public static void onTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CurrencyTooltipData.class, CurrencyTooltipComponent::new);
    }

    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new UIModelLoader());
    }
}
