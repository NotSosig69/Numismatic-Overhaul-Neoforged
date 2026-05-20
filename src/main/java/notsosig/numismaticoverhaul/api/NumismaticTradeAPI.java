package notsosig.numismaticoverhaul.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.villagers.data.RemappingTradeWrapper;
import notsosig.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import notsosig.numismaticoverhaul.villagers.json.VillagerTradesHandler;

/**
 * Public API for integrating with Numismatic Overhaul's coin economy.
 *
 * Coin denominations (all values in bronze):
 *   1 bronze coin  =       1
 *   1 silver coin  =     100
 *   1 gold coin    =  10,000
 *
 * --- DATAPACK WORKFLOW (no Java code required) ---
 *
 * VillagerAPI pack trades (in villagerpacks/<pack>/trades/*.json) can use two new fields:
 *
 *   "numismatic_price": <bronze>  — player pays coins, gets the "sell" item
 *   "numismatic_reward": <bronze> — player pays "buy_a" item, receives coins
 *
 * Example — player buys 5 apples for 50 bronze (half a silver):
 * {
 *   "sell": "minecraft:apple", "sell_count": 5,
 *   "numismatic_price": 50,
 *   "max_uses": 8, "xp": 2, "price_mult": 0.05
 * }
 *
 * Example — player sells 16 wheat and earns 200 bronze:
 * {
 *   "buy_a": { "id": "minecraft:wheat", "count": 16 },
 *   "numismatic_reward": 200
 * }
 *
 * Players can pay with any coin denomination (e.g., one gold coin for a silver-priced trade)
 * and receive change in their purse automatically.
 *
 * Alternatively, place trade files under data/<namespace>/villager_trades/<profession_key>.json
 * using the Numismatic trade format (see existing JSON files for examples).
 *
 * --- JAVA API ---
 *
 * Call registerTradeType() during mod initialization (FMLCommonSetupEvent or earlier).
 * Custom types become available in any datapack using the numismaticoverhaul:villager_trades/ path.
 */
public final class NumismaticTradeAPI {

    private NumismaticTradeAPI() {}

    /**
     * Registers a custom trade type for use in Numismatic villager trade datapacks.
     * Once registered, pack authors can reference it as {@code "type": "yourmod:your_type"}
     * inside any {@code data/<ns>/villager_trades/<profession>.json} file.
     *
     * <pre>{@code
     * NumismaticTradeAPI.registerTradeType(
     *     ResourceLocation.fromNamespaceAndPath("mymod", "sell_special"),
     *     new MySpecialTradeAdapter()
     * );
     * }</pre>
     */
    public static void registerTradeType(ResourceLocation id, TradeJsonAdapter adapter) {
        VillagerTradesHandler.tradeTypesRegistry.put(id, adapter);
    }

    /**
     * Returns the ItemCost for a given bronze value using the closest single coin denomination.
     * Example: 500 → 5 silver coins (ItemCost); 10000 → 1 gold coin (ItemCost).
     */
    public static ItemCost getCoinCost(long bronzeValue) {
        return CurrencyHelper.getClosestItemCost(bronzeValue);
    }

    /**
     * Returns a coin ItemStack for a given bronze value.
     * Example: 500 → ItemStack(silver_coin, 5); 10000 → ItemStack(gold_coin, 1).
     */
    public static ItemStack getCoins(long bronzeValue) {
        return CurrencyHelper.getClosest(bronzeValue);
    }

    /**
     * Creates an ItemListing (trade) where the player pays coins and receives the given result.
     * The cost is rounded to the nearest single coin denomination.
     * Registered using VillagerTrades.TRADES or VillagerAPI's trade registration.
     */
    public static VillagerTrades.ItemListing coinBuyTrade(ItemStack result, long bronzePrice, int maxUses, int xp) {
        ItemCost cost = CurrencyHelper.getClosestItemCost(bronzePrice);
        return (entity, random) -> new MerchantOffer(cost, result.copy(), maxUses, xp, 0.05f);
    }

    /**
     * Creates an ItemListing (trade) where the player sells the given item and receives coins.
     */
    public static VillagerTrades.ItemListing coinSellTrade(ItemStack payment, long bronzeReward, int maxUses, int xp) {
        ItemCost cost = new ItemCost(payment.getItem(), payment.getCount());
        ItemStack coins = CurrencyHelper.getClosest(bronzeReward);
        return (entity, random) -> new MerchantOffer(cost, coins.copy(), maxUses, xp, 0.05f);
    }

    /**
     * Wraps any VillagerTrades.ItemListing so its emerald buy/sell items are automatically
     * converted to equivalent coin values at trade generation time.
     * Coin items already present in the trade pass through unchanged.
     * This is called automatically for all VillagerAPI professions on server start.
     */
    public static VillagerTrades.ItemListing remapToCoin(VillagerTrades.ItemListing trade) {
        return RemappingTradeWrapper.wrap(trade);
    }
}
