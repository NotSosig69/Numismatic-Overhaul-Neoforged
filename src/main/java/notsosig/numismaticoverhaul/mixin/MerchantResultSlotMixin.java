package notsosig.numismaticoverhaul.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {

    @Shadow private MerchantContainer slots;

    // Must inject after notifyTrade but before setItem(0/1): setItem triggers updateSellItem()
    // which nulls activeOffer, so getActiveOffer() returns null at TAIL.
    @Inject(method = "onTake", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/trading/Merchant;notifyTrade(Lnet/minecraft/world/item/trading/MerchantOffer;)V",
            shift = At.Shift.AFTER))
    private void numismatic$giveChange(Player player, ItemStack taken, CallbackInfo ci) {
        MerchantOffer offer = this.slots.getActiveOffer();
        if (offer == null) return;
        long change = ((NumismaticTradeOfferExtensions) offer).numismatic$getChangeOwed();
        if (change <= 0) return;
        ((NumismaticTradeOfferExtensions) offer).numismatic$setChangeOwed(0L);
        CurrencyHolderAttacher.getCurrencyHolder(player).modify(change, player);
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.4f, 1.6f);
    }
}
