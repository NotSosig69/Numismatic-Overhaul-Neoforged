package notsosig.numismaticoverhaul.villagers.json.adapters;

import com.google.gson.JsonObject;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SellSingleEnchantmentAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public VillagerTrades.ItemListing deserialize(JsonObject json) {
        loadDefaultStats(json, false);
        return new Factory(max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements VillagerTrades.ItemListing, NumOTrade {
        private final int experience;
        private final int maxUses;
        private final float multiplier;

        public Factory(int maxUses, int experience, float multiplier) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.multiplier = multiplier;
        }

        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            var enchantmentRegistry = entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            List<Holder<Enchantment>> tradeableList = enchantmentRegistry.getTag(EnchantmentTags.TRADEABLE)
                    .map(tag -> tag.stream().toList())
                    .orElse(List.of());

            int cost;
            ItemStack itemStack;

            if (!tradeableList.isEmpty()) {
                var enchantmentEntry = tradeableList.get(random.nextInt(tradeableList.size()));
                var enchantment = enchantmentEntry.value();

                int enchantmentLevel = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
                itemStack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantmentEntry, enchantmentLevel));

                cost = 100 * (10 / enchantment.getWeight()) + (random.nextInt(50) + enchantmentLevel) * enchantmentLevel * enchantmentLevel * (10 / enchantment.getWeight());
                if (enchantmentEntry.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    cost *= 2;
                }
            } else {
                cost = 1;
                itemStack = new ItemStack(Items.BOOK);
            }

            return new MerchantOffer(CurrencyHelper.getClosestItemCost(cost), Optional.of(new ItemCost(Items.BOOK, 1)), itemStack, maxUses, this.experience, multiplier);
        }
    }
}
