package notsosig.numismaticoverhaul.block;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import notsosig.numismaticoverhaul.currency.CurrencyConverter;
import notsosig.numismaticoverhaul.item.MoneyBagItem;
import notsosig.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;

import java.util.List;

public class ShopOffer {

    private final ItemStack sell;
    private final long price;

    public ShopOffer(ItemStack sell, long price) {
        if (sell.isEmpty()) throw new IllegalArgumentException("Sell Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be zero");

        this.sell = sell;
        this.price = price;
    }

    @SuppressWarnings("ConstantConditions")
    public MerchantOffer toTradeOffer(ShopBlockEntity shop, boolean inexhaustible) {
        var buy = CurrencyConverter.getRequiredCurrencyTypes(price) == 1 ? CurrencyConverter.getAsItemStackList(price).get(0) : MoneyBagItem.create(price);
        int maxUses = inexhaustible ? Integer.MAX_VALUE : count(shop.getItems(), sell) / sell.getCount();

        final var tradeOffer = new MerchantOffer(new ItemCost(buy.getItem(), buy.getCount()), sell, maxUses, 0, 0);
        ((NumismaticTradeOfferExtensions) tradeOffer).numismatic$setReputation(-69420);
        return tradeOffer;
    }

    public long getPrice() {
        return price;
    }

    public ItemStack getSellStack() {
        return sell.copy();
    }

    public static CompoundTag writeAll(CompoundTag tag, List<ShopOffer> offers, HolderLookup.Provider registries) {
        ListTag offerList = new ListTag();
        for (ShopOffer offer : offers) {
            offerList.add(offer.toNbt(registries));
        }
        tag.put("Offers", offerList);
        return tag;
    }

    public static void readAll(CompoundTag tag, List<ShopOffer> offers, HolderLookup.Provider registries) {
        offers.clear();
        ListTag offerList = tag.getList("Offers", Tag.TAG_COMPOUND);
        for (Tag offerTag : offerList) {
            ShopOffer offer = fromNbt((CompoundTag) offerTag, registries);
            if (offer != null) offers.add(offer);
        }
    }

    public CompoundTag toNbt(HolderLookup.Provider registries) {
        var nbt = new CompoundTag();
        nbt.putLong("Price", this.price);
        ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.sell)
                .result().ifPresent(tag -> nbt.put("Item", tag));
        return nbt;
    }

    public static ShopOffer fromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        if (!nbt.contains("Item")) return null;
        var itemResult = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), nbt.get("Item"));
        ItemStack item = itemResult.result().orElse(ItemStack.EMPTY);
        if (item.isEmpty()) return null;
        return new ShopOffer(item, nbt.getLong("Price"));
    }

    public static int count(NonNullList<ItemStack> stacks, ItemStack testStack) {
        int count = 0;
        for (var stack : stacks) {
            if (!ItemStack.isSameItemSameComponents(stack, testStack)) continue;
            count += stack.getCount();
        }
        return count;
    }

    public static int remove(NonNullList<ItemStack> stacks, ItemStack removeStack) {
        int toRemove = removeStack.getCount();
        for (var stack : stacks) {
            if (!ItemStack.isSameItemSameComponents(stack, removeStack)) continue;

            int removed = stack.getCount();
            stack.shrink(toRemove);

            toRemove -= removed;
            if (toRemove < 1) break;
        }
        return removeStack.getCount() - toRemove;
    }

    @Override
    public String toString() {
        return this.sell + "@" + this.price + "coins";
    }
}
