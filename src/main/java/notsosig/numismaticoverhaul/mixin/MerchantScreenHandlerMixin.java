package notsosig.numismaticoverhaul.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import notsosig.numismaticoverhaul.cap.CurrencyHolder;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.config.NOConfig;
import notsosig.numismaticoverhaul.currency.Currency;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.init.ItemInit;
import notsosig.numismaticoverhaul.item.CoinItem;
import notsosig.numismaticoverhaul.item.MoneyBagItem;

@Mixin(MerchantMenu.class)
public class MerchantScreenHandlerMixin {

    @Shadow
    @Final
    private Merchant trader;

    @Redirect(method = "moveFromInventoryToPaymentSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean isSameItemSameComponents(ItemStack stack1, ItemStack stack2) {
        if (NOConfig.INSTANCE.enableDenominationExchange.get() && stack1.getItem() instanceof CoinItem) {
            return stack1.getItem() == stack2.getItem();
        }
        return ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    @Inject(method = "moveFromInventoryToPaymentSlot", at = @At("TAIL"))
    public void autofillOverride(int slot, ItemCost cost, CallbackInfo ci) {
        MerchantMenu handler = (MerchantMenu) (Object) this;
        CurrencyHolder playerBalance = CurrencyHolderAttacher.getCurrencyHolder(((Inventory) handler.getSlot(3).container).player);

        if (NOConfig.INSTANCE.enableSmartAutofill.get()) {
            Item item = cost.item().value();
            if (item instanceof CoinItem) {
                numismatic$autofillWithCoins(slot, cost, handler, playerBalance);
            } else if (item == ItemInit.MONEY_BAG.get()) {
                autofillWithMoneyBag(slot, cost.itemStack(), handler, playerBalance);
            }
        }

        playerBalance.commitTransactions(((Inventory) handler.getSlot(3).container).player);
    }

    private static void numismatic$autofillWithCoins(int slot, ItemCost cost, MerchantMenu handler, CurrencyHolder playerBalance) {
        CoinItem costCoin = (CoinItem) cost.item().value();
        long requiredValue = costCoin.currency.getRawValue(cost.count());

        ItemStack slotStack = handler.getSlot(slot).getItem();

        // Slot already has a cross-denomination coin with sufficient value → denomination exchange handles it
        if (slotStack.getItem() instanceof CoinItem slotCoin && slotCoin != costCoin) {
            if (slotCoin.currency.getRawValue(slotStack.getCount()) >= requiredValue) return;
        }

        long presentValue = (slotStack.getItem() == costCoin)
                ? costCoin.currency.getRawValue(slotStack.getCount())
                : 0L;
        if (requiredValue <= presentValue) return;

        long neededValue = requiredValue - presentValue;
        int presentCount = (slotStack.getItem() == costCoin) ? slotStack.getCount() : 0;
        int totalCount = presentCount + (int)(neededValue / costCoin.currency.getRawValue(1));

        // Exact denomination fits within the 99-stack limit
        if (totalCount <= 99 && neededValue <= playerBalance.getValue()) {
            playerBalance.pushTransaction(-neededValue);
            handler.slots.get(slot).set(new ItemStack(cost.item(), cost.count()));
            return;
        }

        // Trade needs more than 99 coins — try a higher denomination so denomination exchange gives change.
        // If vanilla autofill already partially filled with the base denomination, return those coins to inventory
        // before replacing with a higher-denomination stack.
        if (!slotStack.isEmpty() && slotStack.getItem() == costCoin) {
            Player player = ((Inventory) handler.getSlot(3).container).player;
            player.getInventory().placeItemBackInInventory(slotStack.copy());
            handler.slots.get(slot).set(ItemStack.EMPTY);
        }
        for (Currency higherDenom : numismatic$higherDenominations(costCoin.currency)) {
            long denomValue = higherDenom.getRawValue(1);
            int coinsNeeded = (int) Math.ceil((double) requiredValue / denomValue);
            if (coinsNeeded > 99) continue;
            long totalCost = denomValue * coinsNeeded;
            if (totalCost > playerBalance.getValue()) continue;
            playerBalance.pushTransaction(-totalCost);
            handler.slots.get(slot).set(new ItemStack(higherDenom, coinsNeeded));
            return;
        }
    }

    @Unique
    private static Currency[] numismatic$higherDenominations(Currency base) {
        return switch (base) {
            case BRONZE -> new Currency[]{ Currency.SILVER, Currency.GOLD };
            case SILVER -> new Currency[]{ Currency.GOLD };
            case GOLD -> new Currency[0];
        };
    }

    private static void autofillWithMoneyBag(int slot, ItemStack stack, MerchantMenu handler, CurrencyHolder playerBalance) {
        if (ItemStack.isSameItemSameComponents(stack, handler.getSlot(slot).getItem())) return;
        Player player = ((Inventory) handler.getSlot(3).container).player;

        long requiredCurrency = ((MoneyBagItem) ItemInit.MONEY_BAG.get()).getValue(stack);
        long availableCurrencyInPlayerInventory = CurrencyHelper.getMoneyInInventory(player, false);

        long neededCurrency = requiredCurrency - availableCurrencyInPlayerInventory;

        if (neededCurrency > playerBalance.getValue()) return;

        if (neededCurrency <= 0) {
            CurrencyHelper.deduceFromInventory(player, requiredCurrency);
        } else {
            CurrencyHelper.deduceFromInventory(player, availableCurrencyInPlayerInventory);
            playerBalance.pushTransaction(-neededCurrency);
        }

        handler.slots.get(slot).set(stack.copy());
    }

    @Inject(method = "playTradeSound", at = @At("HEAD"), cancellable = true)
    public void checkForEntityOnYes(CallbackInfo ci) {
        if (!(trader instanceof Entity)) ci.cancel();
    }
}
