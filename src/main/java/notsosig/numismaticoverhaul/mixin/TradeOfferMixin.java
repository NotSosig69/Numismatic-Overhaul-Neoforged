package notsosig.numismaticoverhaul.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import notsosig.numismaticoverhaul.config.NOConfig;
import notsosig.numismaticoverhaul.currency.CurrencyHelper;
import notsosig.numismaticoverhaul.item.CoinItem;
import notsosig.numismaticoverhaul.item.CurrencyItem;
import notsosig.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;

import java.util.Collections;
import java.util.Optional;

@Mixin(MerchantOffer.class)
public class TradeOfferMixin implements NumismaticTradeOfferExtensions {

    @Shadow @Final private ItemCost baseCostA;
    @Shadow @Final private Optional<ItemCost> costB;

    private int numismatic$reputation = 0;
    private long numismatic$changeOwed = 0L;

    @Override
    public void numismatic$setReputation(int reputation) {
        this.numismatic$reputation = reputation;
    }

    @Override
    public int numismatic$getReputation() {
        return numismatic$reputation;
    }

    @Override
    public long numismatic$getChangeOwed() {
        return numismatic$changeOwed;
    }

    @Override
    public void numismatic$setChangeOwed(long change) {
        this.numismatic$changeOwed = change;
    }

    @Inject(method = "getCostA", at = @At("HEAD"), cancellable = true)
    private void adjustFirstStack(CallbackInfoReturnable<ItemStack> cir) {
        if (this.numismatic$reputation == -69420) return;

        if (!(this.baseCostA.item().value() instanceof CurrencyItem currencyItem)) return;

        long originalValue = currencyItem.getValue(this.baseCostA.itemStack());
        long adjustedValue = numismatic$reputation < 0
                ? (long) (originalValue + Math.abs(numismatic$reputation) * (Math.abs(originalValue) * .02))
                : (long) Math.max(1, originalValue - Math.abs(originalValue) * (numismatic$reputation / (numismatic$reputation + 100f)));

        adjustedValue = Math.min(adjustedValue, 990000);

        final var roundedStack = CurrencyHelper.getClosest(adjustedValue);
        if (originalValue != CurrencyHelper.getValue(Collections.singletonList(roundedStack)) && !roundedStack.is(this.baseCostA.item().value())) {
            CurrencyItem.setOriginalValue(roundedStack, originalValue);
        }
        cir.setReturnValue(roundedStack);
    }

    @Inject(method = "satisfiedBy", at = @At("HEAD"), cancellable = true)
    private void numismatic$crossDenomSatisfied(ItemStack offered1, ItemStack offered2, CallbackInfoReturnable<Boolean> cir) {
        if (!NOConfig.INSTANCE.enableDenominationExchange.get()) return;
        if (!(baseCostA.item().value() instanceof CoinItem costCoin)) return;
        if (!(offered1.getItem() instanceof CoinItem offeredCoin)) return;
        if (offeredCoin == costCoin) return;

        long required = costCoin.currency.getRawValue(baseCostA.count());
        long provided = offeredCoin.currency.getRawValue(offered1.getCount());

        if (provided < required) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        boolean slot2ok = costB.isEmpty()
                || (costB.get().test(offered2) && offered2.getCount() >= costB.get().count());
        cir.setReturnValue(slot2ok);
        cir.cancel();
    }

    @Inject(method = "take", at = @At("HEAD"), cancellable = true)
    private void numismatic$crossDenomTake(ItemStack offered1, ItemStack offered2, CallbackInfoReturnable<Boolean> cir) {
        if (!NOConfig.INSTANCE.enableDenominationExchange.get()) return;
        if (!(baseCostA.item().value() instanceof CoinItem costCoin)) return;
        if (!(offered1.getItem() instanceof CoinItem offeredCoin)) return;
        if (offeredCoin == costCoin) return;

        long required = costCoin.currency.getRawValue(baseCostA.count());
        long valuePerCoin = offeredCoin.currency.getRawValue(1);
        int coinsNeeded = (int) Math.ceil((double) required / valuePerCoin);

        if (offered1.getCount() < coinsNeeded) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        offered1.shrink(coinsNeeded);
        costB.ifPresent(b -> offered2.shrink(b.count()));
        this.numismatic$changeOwed = valuePerCoin * coinsNeeded - required;
        cir.setReturnValue(true);
        cir.cancel();
    }
}
