package notsosig.numismaticoverhaul.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCurrencyHolderS2CPacket(long value) implements CustomPacketPayload {

    public static final Type<SyncCurrencyHolderS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NumismaticOverhaul.MODID, "sync_currency_holder"));

    public static final StreamCodec<FriendlyByteBuf, SyncCurrencyHolderS2CPacket> CODEC =
            StreamCodec.ofMember(
                    (pkt, buf) -> buf.writeLong(pkt.value),
                    buf -> new SyncCurrencyHolderS2CPacket(buf.readLong())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncCurrencyHolderS2CPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            CurrencyHolderAttacher.getCurrencyHolder(player).setValue(pkt.value);
        });
    }
}
