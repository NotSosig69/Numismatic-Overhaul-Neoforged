package notsosig.numismaticoverhaul.mixin;

import com.frikinjay.villagerapi.villagerpack.VillagerPackCodecs;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;

/**
 * Extends VillagerAPI pack trade parsing with two coin-native fields:
 *
 * "numismatic_price": <bronze>  — player pays coins, gets the "sell" item
 * "numismatic_reward": <bronze> — player trades "buy_a" item, gets coins
 *
 * Example (player buys 5 apples for 50 bronze = 0.5 silver):
 * { "sell": "minecraft:apple", "sell_count": 5, "numismatic_price": 50 }
 *
 * Example (player sells 16 wheat, gets 200 bronze = 2 silver):
 * { "buy_a": { "id": "minecraft:wheat", "count": 16 }, "numismatic_reward": 200 }
 *
 * Optional trade control fields: "max_uses" (default 12), "xp" (default 5), "price_mult" (default 0.05)
 */
@Mixin(value = VillagerPackCodecs.class, remap = false)
public class VillagerPackCodecsMixin {

    @Inject(method = "parseTrade", at = @At("HEAD"), cancellable = true, remap = false)
    private static void numismatic$injectCoinTrade(JsonObject trade, CallbackInfoReturnable<VillagerTrades.ItemListing> cir) {
        boolean hasCoinPrice = trade.has("numismatic_price");
        boolean hasCoinReward = trade.has("numismatic_reward");
        if (!hasCoinPrice && !hasCoinReward) return;

        int maxUses = trade.has("max_uses") ? trade.get("max_uses").getAsInt() : 12;
        int xp = trade.has("xp") ? trade.get("xp").getAsInt() : 5;
        float priceMult = trade.has("price_mult") ? trade.get("price_mult").getAsFloat() : 0.05f;

        if (hasCoinPrice && trade.has("sell")) {
            long bronzePrice = trade.get("numismatic_price").getAsLong();
            ItemStack coinStack = CurrencyHelper.getClosest(bronzePrice);
            ItemCost coinCost = new ItemCost(coinStack.getItem(), coinStack.getCount());

            int sellCount = trade.has("sell_count") ? trade.get("sell_count").getAsInt() : 1;
            ItemStack result = numismatic$parseItem(trade.get("sell"), sellCount);
            if (result.isEmpty()) return;

            cir.setReturnValue((entity, random) -> new MerchantOffer(coinCost, result.copy(), maxUses, xp, priceMult));
            cir.cancel();
        } else if (hasCoinReward && trade.has("buy_a")) {
            long bronzeReward = trade.get("numismatic_reward").getAsLong();
            ItemStack coinStack = CurrencyHelper.getClosest(bronzeReward);

            ItemStack payment = numismatic$parseItem(trade.get("buy_a"), 1);
            if (payment.isEmpty()) return;
            ItemCost paymentCost = new ItemCost(payment.getItem(), payment.getCount());

            cir.setReturnValue((entity, random) -> new MerchantOffer(paymentCost, coinStack.copy(), maxUses, xp, priceMult));
            cir.cancel();
        }
    }

    @Unique
    private static ItemStack numismatic$parseItem(JsonElement element, int defaultCount) {
        if (element == null || element.isJsonNull()) return ItemStack.EMPTY;
        String id;
        int count = defaultCount;
        if (element.isJsonPrimitive()) {
            id = element.getAsString();
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            id = obj.has("id") ? obj.get("id").getAsString()
                    : obj.has("item") ? obj.get("item").getAsString() : null;
            if (obj.has("count")) count = obj.get("count").getAsInt();
        } else {
            return ItemStack.EMPTY;
        }
        if (id == null) return ItemStack.EMPTY;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item, Math.max(1, count));
    }
}
