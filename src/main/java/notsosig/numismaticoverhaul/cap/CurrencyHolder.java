package notsosig.numismaticoverhaul.cap;

import notsosig.numismaticoverhaul.config.NOConfig;
import notsosig.numismaticoverhaul.currency.CurrencyConverter;
import notsosig.numismaticoverhaul.item.CoinItem;
import notsosig.numismaticoverhaul.network.SyncCurrencyHolderS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CurrencyHolder {

    private long value;
    private final List<Long> transactions = new ArrayList<>();

    public CurrencyHolder() {}

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void silentModify(long delta) {
        this.value += delta;
    }

    public Long popTransaction() {
        return this.transactions.remove(this.transactions.size() - 1);
    }

    public void pushTransaction(long value) {
        this.transactions.add(value);
    }

    public void modify(long delta, Player player) {
        this.value += delta;
        if (delta > 0) {
            long max = NOConfig.INSTANCE.maxPurseValue.get();
            if (max >= 0) this.value = Math.min(this.value, max);
        }

        sync(player);

        if (!NOConfig.INSTANCE.showCurrencyActionBar.get()) return;

        long tempValue = delta < 0 ? -delta : delta;
        List<ItemStack> transactionStacks = CurrencyConverter.getAsItemStackList(tempValue);
        if (transactionStacks.isEmpty()) return;

        MutableComponent message = delta < 0
                ? Component.literal("§c- ")
                : Component.literal("§a+ ");
        message.append(Component.literal("§7["));
        for (ItemStack stack : transactionStacks) {
            message.append(Component.literal("§b" + stack.getCount() + " "));
            message.append(Component.translatable("currency.numismaticoverhaul." + ((CoinItem) stack.getItem()).currency.name().toLowerCase()));
            if (transactionStacks.indexOf(stack) != transactionStacks.size() - 1)
                message.append(Component.literal(", "));
        }
        message.append(Component.literal("§7]"));

        player.displayClientMessage(message, true);
    }

    public void commitTransactions(Player player) {
        if (this.transactions.isEmpty()) return;
        this.modify(this.transactions.stream().mapToLong(Long::longValue).sum(), player);
        this.transactions.clear();
    }

    public void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncCurrencyHolderS2CPacket(this.value));
        }
    }
}
