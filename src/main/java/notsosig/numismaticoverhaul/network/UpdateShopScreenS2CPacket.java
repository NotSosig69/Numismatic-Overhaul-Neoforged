package notsosig.numismaticoverhaul.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.block.ShopBlockEntity;
import notsosig.numismaticoverhaul.block.ShopOffer;
import notsosig.numismaticoverhaul.client.gui.ShopScreen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

public record UpdateShopScreenS2CPacket(List<ShopOffer> offers, long storedCurrency,
                                        boolean transferEnabled) implements CustomPacketPayload {

    public static final Type<UpdateShopScreenS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NumismaticOverhaul.MODID, "update_shop_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateShopScreenS2CPacket> CODEC =
            StreamCodec.ofMember(
                    (pkt, buf) -> {
                        buf.writeVarInt(pkt.offers.size());
                        for (ShopOffer offer : pkt.offers) {
                            ItemStack.STREAM_CODEC.encode(buf, offer.getSellStack());
                            buf.writeLong(offer.getPrice());
                        }
                        buf.writeLong(pkt.storedCurrency);
                        buf.writeBoolean(pkt.transferEnabled);
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        List<ShopOffer> offers = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            var item = ItemStack.STREAM_CODEC.decode(buf);
                            long price = buf.readLong();
                            if (!item.isEmpty() && price != 0) offers.add(new ShopOffer(item, price));
                        }
                        return new UpdateShopScreenS2CPacket(offers, buf.readLong(), buf.readBoolean());
                    }
            );

    public UpdateShopScreenS2CPacket(ShopBlockEntity shop) {
        this(shop.getOffers(), shop.getStoredCurrency(), shop.isTransferEnabled());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateShopScreenS2CPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(Minecraft.getInstance().screen instanceof ShopScreen screen)) return;
            screen.update(pkt);
        });
    }
}
