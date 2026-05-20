package notsosig.numismaticoverhaul.currency;

import notsosig.numismaticoverhaul.init.ItemInit;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurrencyConverter {

    public static ItemStack[] getAsItemStackArray(long value) {
        ItemStack[] output = new ItemStack[]{null, null, null};
        long[] values = CurrencyResolver.splitValues(value);
        output[2] = new ItemStack(ItemInit.GOLD_COIN.get(), asInt(values[2]));
        output[1] = new ItemStack(ItemInit.SILVER_COIN.get(), asInt(values[1]));
        output[0] = new ItemStack(ItemInit.BRONZE_COIN.get(), asInt(values[0]));
        return output;
    }

    public static List<ItemStack> getAsItemStackListRaw(long value) {
        List<ItemStack> list = new ArrayList<>();
        Arrays.stream(getAsItemStackArray(value)).forEach(itemStack -> {
            if (itemStack != null && itemStack.getCount() != 0) {
                list.add(0, itemStack);
            }
        });
        return list;
    }

    public static List<ItemStack> getAsItemStackListRaw(long[] values) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] <= 0) continue;
            list.add(0, new ItemStack(Currency.values()[i], asInt(values[i])));
        }
        return list;
    }

    public static List<ItemStack> getAsItemStackList(long value) {
        return splitAtMaxCount(getAsItemStackListRaw(value));
    }

    public static List<ItemStack> getAsItemStackList(long[] values) {
        return splitAtMaxCount(getAsItemStackListRaw(values));
    }

    public static int getRequiredCurrencyTypes(long value) {
        return getAsItemStackListRaw(value).size();
    }

    public static List<ItemStack> splitAtMaxCount(List<ItemStack> input) {
        List<ItemStack> output = new ArrayList<>();
        for (ItemStack stack : input) {
            if (stack == null || stack.isEmpty()) continue;
            int itemMax = stack.getMaxStackSize();
            int max = itemMax > 0 ? Math.min(itemMax, 64) : 64;
            if (stack.getCount() <= max) {
                output.add(stack);
            } else {
                int fullStacks = stack.getCount() / max;
                int remainder = stack.getCount() % max;
                for (int i = 0; i < fullStacks; i++) {
                    ItemStack copy = stack.copy();
                    copy.setCount(max);
                    output.add(copy);
                }
                if (remainder > 0) {
                    ItemStack copy = stack.copy();
                    copy.setCount(remainder);
                    output.add(copy);
                }
            }
        }
        return output;
    }

    public static List<ItemStack> getAsValidStacks(long value) {
        return splitAtMaxCount(getAsItemStackListRaw(value));
    }

    public static List<ItemStack> getAsValidStacks(long[] values) {
        return splitAtMaxCount(getAsItemStackListRaw(values));
    }

    public static int asInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
