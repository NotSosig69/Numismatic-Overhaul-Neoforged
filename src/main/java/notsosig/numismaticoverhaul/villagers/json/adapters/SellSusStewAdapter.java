package notsosig.numismaticoverhaul.villagers.json.adapters;

import com.google.gson.JsonObject;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.villagers.exceptions.DeserializationException;
import notsosig.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class SellSusStewAdapter extends TradeJsonAdapter {

    @Override
    public @NotNull VillagerTrades.ItemListing deserialize(JsonObject json) {
        this.loadDefaultStats(json, true);

        final int price = json.get("price").getAsInt();
        final int duration = GsonHelper.getAsInt(json, "duration", 100);

        final String effectIdStr = GsonHelper.getAsString(json, "effect_id");
        final ResourceLocation effectId = ResourceLocation.tryParse(effectIdStr);
        if (effectId == null) throw new DeserializationException("Invalid effect_id resource location: " + effectIdStr);
        final var effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(effectId)
                .orElseThrow(() -> new DeserializationException("Unknown status effect '" + effectId + "'"));

        return new Factory(effectHolder, price, duration, villager_experience, price_multiplier, max_uses);
    }

    static class Factory implements VillagerTrades.ItemListing, NumOTrade {
        private final Holder<MobEffect> effect;
        private final int price;
        private final int duration;
        private final int experience;
        private final int maxUses;
        private final float multiplier;

        public Factory(Holder<MobEffect> effect, int price, int duration, int experience, float multiplier, int maxUses) {
            this.effect = effect;
            this.price = price;
            this.duration = duration;
            this.experience = experience;
            this.multiplier = multiplier;
            this.maxUses = maxUses;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            ItemStack susStew = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            susStew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS,
                    new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(effect, duration))));

            return new MerchantOffer(CurrencyHelper.getClosestItemCost(price), susStew, this.maxUses, this.experience, this.multiplier);
        }
    }
}
