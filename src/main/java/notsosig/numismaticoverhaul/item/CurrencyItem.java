package notsosig.numismaticoverhaul.item;

import notsosig.numismaticoverhaul.init.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public interface CurrencyItem {

    static void setOriginalValue(ItemStack stack, long value) {
        stack.set(ModDataComponents.ORIGINAL_VALUE.get(), value);
    }

    static long getOriginalValue(ItemStack stack) {
        Long val = stack.get(ModDataComponents.ORIGINAL_VALUE.get());
        return val != null ? val : 0L;
    }

    static boolean hasOriginalValue(ItemStack stack) {
        return stack.has(ModDataComponents.ORIGINAL_VALUE.get());
    }

    boolean wasAdjusted(ItemStack other);

    long getValue(ItemStack stack);

    long[] getCombinedValue(ItemStack stack);
}
