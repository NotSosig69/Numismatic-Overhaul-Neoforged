package notsosig.numismaticoverhaul.villagers.data;

import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RemappingTradeWrapper implements VillagerTrades.ItemListing {

    private final VillagerTrades.ItemListing delegate;

    private RemappingTradeWrapper(VillagerTrades.ItemListing delegate) {
        this.delegate = delegate;
    }

    public static RemappingTradeWrapper wrap(VillagerTrades.ItemListing delegate) {
        return new RemappingTradeWrapper(delegate);
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource random) {
        final var tempOffer = delegate.getOffer(entity, random);

        if (tempOffer == null) return null;

        final var firstBuyRemapped = remapCost(tempOffer.getItemCostA());
        final var secondBuyRemapped = tempOffer.getItemCostB().map(RemappingTradeWrapper::remapCost);
        final var sellRemapped = remapStack(tempOffer.getResult());

        return new MerchantOffer(firstBuyRemapped, secondBuyRemapped, sellRemapped,
                tempOffer.getUses(), tempOffer.getMaxUses(), tempOffer.getXp(),
                tempOffer.getPriceMultiplier(), tempOffer.getDemand());
    }

    private static ItemCost remapCost(ItemCost cost) {
        if (cost.item().value() != Items.EMERALD) return cost;
        var currency = CurrencyHelper.getClosest((long) cost.count() * 125);
        return new ItemCost(currency.getItem(), currency.getCount());
    }

    private static ItemStack remapStack(ItemStack stack) {
        if (stack.getItem() != Items.EMERALD) return stack;
        return CurrencyHelper.getClosest((long) stack.getCount() * 125);
    }
}
