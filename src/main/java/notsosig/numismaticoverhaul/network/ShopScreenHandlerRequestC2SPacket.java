package notsosig.numismaticoverhaul.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.block.ShopScreenHandler;

public record ShopScreenHandlerRequestC2SPacket(Action action, long value) implements CustomPacketPayload {

    public static final Type<ShopScreenHandlerRequestC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NumismaticOverhaul.MODID, "shop_screen_request"));

    public static final StreamCodec<FriendlyByteBuf, ShopScreenHandlerRequestC2SPacket> CODEC =
            StreamCodec.ofMember(
                    (pkt, buf) -> { buf.writeEnum(pkt.action); buf.writeLong(pkt.value); },
                    buf -> new ShopScreenHandlerRequestC2SPacket(buf.readEnum(Action.class), buf.readLong())
            );

    public ShopScreenHandlerRequestC2SPacket(Action action) {
        this(action, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShopScreenHandlerRequestC2SPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof ShopScreenHandler shopHandler)) return;

            switch (pkt.action) {
                case LOAD_OFFER -> shopHandler.loadOffer(pkt.value);
                case CREATE_OFFER -> shopHandler.createOffer(pkt.value);
                case DELETE_OFFER -> shopHandler.deleteOffer();
                case EXTRACT_CURRENCY -> shopHandler.extractCurrency();
                case TOGGLE_TRANSFER -> shopHandler.toggleTransfer();
            }
        });
    }

    public enum Action {
        CREATE_OFFER, DELETE_OFFER, LOAD_OFFER, EXTRACT_CURRENCY, TOGGLE_TRANSFER
    }
}
