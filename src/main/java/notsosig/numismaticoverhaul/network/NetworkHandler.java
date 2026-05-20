package notsosig.numismaticoverhaul.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToClient(
                SyncCurrencyHolderS2CPacket.TYPE,
                SyncCurrencyHolderS2CPacket.CODEC,
                SyncCurrencyHolderS2CPacket::handle
        );
        registrar.playToClient(
                UpdateShopScreenS2CPacket.TYPE,
                UpdateShopScreenS2CPacket.CODEC,
                UpdateShopScreenS2CPacket::handle
        );
        registrar.playToServer(
                RequestPurseActionC2SPacket.TYPE,
                RequestPurseActionC2SPacket.CODEC,
                RequestPurseActionC2SPacket::handle
        );
        registrar.playToServer(
                ShopScreenHandlerRequestC2SPacket.TYPE,
                ShopScreenHandlerRequestC2SPacket.CODEC,
                ShopScreenHandlerRequestC2SPacket::handle
        );
    }
}
