package notsosig.numismaticoverhaul.currency;

import notsosig.numismaticoverhaul.init.ItemInit;
import notsosig.numismaticoverhaul.item.CoinItem;
import notsosig.numismaticoverhaul.item.CurrencyItem;
import notsosig.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

import java.util.ArrayList;
import java.util.List;

public class CurrencyHelper {

    public static long getMoneyInInventory(Player player, boolean remove) {
        long value = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isCombined(stack)) continue;
            if (!(stack.getItem() instanceof CurrencyItem currencyItem)) continue;
            value += currencyItem.getValue(stack);
            if (remove) player.getInventory().removeItem(stack);
        }
        return value;
    }

    public static int getValue(List<ItemStack> stacks) {
        return stacks.stream().mapToInt(stack -> {
            if (stack == null) return 0;
            if (isCombined(stack)) return 0;
            if (!(stack.getItem() instanceof CurrencyItem currencyItem)) return 0;
            return (int) currencyItem.getValue(stack);
        }).sum();
    }

    public static void offerAsCoins(Player player, long value) {
        for (ItemStack itemStack : CurrencyConverter.getAsValidStacks(value)) {
            player.getInventory().placeItemBackInInventory(itemStack);
        }
    }

    public static boolean deduceFromInventory(Player player, long value) {
        long presentInInventory = getMoneyInInventory(player, false);
        if (presentInInventory < value) return false;
        getMoneyInInventory(player, true);
        offerAsCoins(player, presentInInventory - value);
        return true;
    }

    public static List<ItemStack> getAsStacks(long value, int maxStacks) {
        List<ItemStack> stacks = new ArrayList<>();
        List<ItemStack> rawStacks = CurrencyConverter.getAsValidStacks(value);
        if (rawStacks.size() <= maxStacks) {
            stacks.addAll(rawStacks);
        } else {
            stacks.add(MoneyBagItem.create(value));
        }
        return stacks;
    }

    public static ItemCost getClosestItemCost(long value) {
        ItemStack stack = getClosest(value);
        return new ItemCost(stack.getItem(), stack.getCount());
    }

    public static ItemStack getClosest(long value) {
        long[] values = CurrencyResolver.splitValues(value);
        for (int i = 0; i < 2; i++) {
            if (values[i + 1] == 0) break;
            // Ceiling ensures the returned denomination always covers the actual cost,
            // so satisfiedBy never rejects a payment that autofill placed.
            values[i + 1] += (long) Math.ceil(values[i] / 100.0);
            values[i] = 0;
        }
        return CurrencyConverter.getAsItemStackList(CurrencyResolver.combineValues(values)).get(0);
    }

    private static boolean isCombined(ItemStack stack) {
        return stack.is(ItemInit.MONEY_BAG.get()) && stack.has(MoneyBagItem.COMBINED_VALUES_COMPONENT.get());
    }
}
