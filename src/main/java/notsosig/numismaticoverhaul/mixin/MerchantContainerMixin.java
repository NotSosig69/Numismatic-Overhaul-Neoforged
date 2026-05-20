package notsosig.numismaticoverhaul.mixin;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MerchantContainer.class)
public class MerchantContainerMixin {

    @Redirect(method = "setItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantContainer;getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I"))
    private int redirectGetMaxStackSize(MerchantContainer merchantContainer, ItemStack stack) {
        return 99;
    }
}
