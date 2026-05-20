package notsosig.numismaticoverhaul.villagers.json.adapters;

import com.google.gson.JsonObject;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import notsosig.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SellPotionContainerItemAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public VillagerTrades.ItemListing deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertJsonObject(json, "container_item");
        VillagerJsonHelper.assertJsonObject(json, "buy_item");

        int price = json.get("price").getAsInt();
        ItemStack container_item = VillagerJsonHelper.getItemStackFromJson(json.get("container_item").getAsJsonObject());
        ItemStack buy_item = VillagerJsonHelper.getItemStackFromJson(json.get("buy_item").getAsJsonObject());

        return new Factory(container_item, buy_item, price, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements VillagerTrades.ItemListing, NumOTrade {
        private final ItemStack containerItem;
        private final ItemStack buyItem;

        private final int price;
        private final int maxUses;
        private final int experience;

        private final float priceMultiplier;

        public Factory(ItemStack containerItem, ItemStack buyItem, int price, int maxUses, int experience, float priceMultiplier) {
            this.containerItem = containerItem;
            this.buyItem = buyItem;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.priceMultiplier = priceMultiplier;
        }

        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            var brewing = entity.level().potionBrewing();
            var list = BuiltInRegistries.POTION.holders()
                    .filter(h -> !h.value().getEffects().isEmpty() && brewing.isBrewablePotion(h))
                    .toList();

            if (list.isEmpty()) return null;
            Holder<Potion> potionHolder = list.get(random.nextInt(list.size()));
            ItemStack itemStack2 = PotionContents.createItemStack(containerItem.getItem(), potionHolder);

            return new MerchantOffer(CurrencyHelper.getClosestItemCost(price), Optional.of(new ItemCost(buyItem.getItem(), buyItem.getCount())), itemStack2, this.maxUses, this.experience, this.priceMultiplier);
        }
    }
}
