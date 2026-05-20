package notsosig.numismaticoverhaul.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.currency.CurrencyConverter;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;

public record RequestPurseActionC2SPacket(Action action, long value) implements CustomPacketPayload {

    public static final Type<RequestPurseActionC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NumismaticOverhaul.MODID, "request_purse_action"));

    public static final StreamCodec<FriendlyByteBuf, RequestPurseActionC2SPacket> CODEC =
            StreamCodec.ofMember(
                    (pkt, buf) -> { buf.writeEnum(pkt.action); buf.writeLong(pkt.value); },
                    buf -> new RequestPurseActionC2SPacket(buf.readEnum(Action.class), buf.readLong())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestPurseActionC2SPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof InventoryMenu)) return;

            var holder = CurrencyHolderAttacher.getCurrencyHolder(player);
            switch (pkt.action) {
                case STORE_ALL -> holder.modify(CurrencyHelper.getMoneyInInventory(player, true), player);
                case EXTRACT -> {
                    if (holder.getValue() < pkt.value) return;
                    CurrencyConverter.getAsItemStackList(pkt.value).forEach(stack -> player.getInventory().placeItemBackInInventory(stack));
                    holder.modify(-pkt.value, player);
                }
                case EXTRACT_ALL -> {
                    long total = holder.getValue();
                    CurrencyConverter.getAsValidStacks(total).forEach(stack -> player.getInventory().placeItemBackInInventory(stack));
                    holder.modify(-total, player);
                }
            }
        });
    }

    public static RequestPurseActionC2SPacket storeAll() {
        return new RequestPurseActionC2SPacket(Action.STORE_ALL, 0);
    }

    public static RequestPurseActionC2SPacket extractAll() {
        return new RequestPurseActionC2SPacket(Action.EXTRACT_ALL, 0);
    }

    public static RequestPurseActionC2SPacket extract(long amount) {
        return new RequestPurseActionC2SPacket(Action.EXTRACT, amount);
    }

    public enum Action {
        STORE_ALL, EXTRACT, EXTRACT_ALL
    }
}
