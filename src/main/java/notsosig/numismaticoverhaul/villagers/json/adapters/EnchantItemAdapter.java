package notsosig.numismaticoverhaul.villagers.json.adapters;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.component.DataComponents;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import notsosig.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EnchantItemAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public VillagerTrades.ItemListing deserialize(JsonObject json) {

        loadDefaultStats(json, false);
        VillagerJsonHelper.assertInt(json, "level");

        boolean allow_treasure = VillagerJsonHelper.boolean_getOrDefault(json, "allow_treasure", false);

        int level = json.get("level").getAsInt();
        ItemStack item = VillagerJsonHelper.ItemStack_getOrDefault(json, "item", new ItemStack(Items.BOOK));
        int base_price = GsonHelper.getAsInt(json, "base_price", 200);

        return new Factory(item, max_uses, villager_experience, level, allow_treasure, price_multiplier, base_price);
    }

    private static class Factory implements VillagerTrades.ItemListing, NumOTrade {
        private final int experience;
        private final int maxUses;
        private final int level;
        private final boolean allowTreasure;
        private final ItemStack toEnchant;
        private final float multiplier;
        private final int basePrice;

        public Factory(ItemStack item, int maxUses, int experience, int level, boolean allowTreasure, float multiplier, int basePrice) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.level = level;
            this.allowTreasure = allowTreasure;
            this.toEnchant = item;
            this.multiplier = multiplier;
            this.basePrice = basePrice;
        }

        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            var enchantmentRegistry = entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Optional<HolderSet<Enchantment>> possibleEnchants = allowTreasure
                    ? enchantmentRegistry.getTag(EnchantmentTags.TRADEABLE).map(t -> t)
                    : enchantmentRegistry.getTag(EnchantmentTags.NON_TREASURE).map(t -> t);

            ItemStack itemStack = EnchantmentHelper.enchantItem(random, toEnchant.copy(), level,
                    entity.level().registryAccess(), possibleEnchants);

            int price = basePrice;
            ItemEnchantments enchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                var enchantment = entry.getKey();
                boolean isTreasure = enchantment.is(EnchantmentTags.TREASURE);
                price += (int) (price * 0.10f + basePrice * (isTreasure ? 2f : 1f) *
                        entry.getIntValue() * Mth.nextFloat(random, .8f, 1.2f)
                        * (5f / enchantment.value().getWeight()));
            }

            return new MerchantOffer(CurrencyHelper.getClosestItemCost(price),
                    Optional.of(new ItemCost(toEnchant.getItem(), 1)), itemStack, maxUses, this.experience, multiplier);
        }
    }
}
