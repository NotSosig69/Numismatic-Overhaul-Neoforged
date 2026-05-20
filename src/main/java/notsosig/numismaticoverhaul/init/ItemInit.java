package notsosig.numismaticoverhaul.init;

import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.currency.Currency;
import notsosig.numismaticoverhaul.item.CoinItem;
import notsosig.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static notsosig.numismaticoverhaul.init.CreativeTabInit.addToTab;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, NumismaticOverhaul.MODID);

    public static final DeferredHolder<Item, CoinItem> BRONZE_COIN = addToTab(
            ITEMS.register("bronze_coin", () -> new CoinItem(Currency.BRONZE)));
    public static final DeferredHolder<Item, CoinItem> SILVER_COIN = addToTab(
            ITEMS.register("silver_coin", () -> new CoinItem(Currency.SILVER)));
    public static final DeferredHolder<Item, CoinItem> GOLD_COIN = addToTab(
            ITEMS.register("gold_coin", () -> new CoinItem(Currency.GOLD)));
    public static final DeferredHolder<Item, MoneyBagItem> MONEY_BAG = addToTab(
            ITEMS.register("money_bag", MoneyBagItem::new));
}
