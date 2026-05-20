package notsosig.numismaticoverhaul.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import notsosig.numismaticoverhaul.cap.CurrencyHolder;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.config.NOConfig;
import notsosig.numismaticoverhaul.currency.CurrencyConverter;

@Mixin(Player.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "destroyVanishingCursedItems", at = @At("TAIL"))
    public void onServerDeath(CallbackInfo ci) {
        var player = (Player) (Object) this;

        final var world = player.level();
        if (world.isClientSide) return;

        final CurrencyHolder component = CurrencyHolderAttacher.getCurrencyHolder(player);

        Difficulty difficulty = world.getDifficulty();
        double dropPct = switch (difficulty) {
            case PEACEFUL -> NOConfig.INSTANCE.moneyDropChancePeaceful.get();
            case EASY -> NOConfig.INSTANCE.moneyDropChanceEasy.get();
            case NORMAL -> NOConfig.INSTANCE.moneyDropChanceNormal.get();
            case HARD -> NOConfig.INSTANCE.moneyDropChanceHard.get();
        };
        int dropped = (int) (component.getValue() * (dropPct * 0.01));

        for (var drop : CurrencyConverter.getAsValidStacks(dropped)) {
            player.drop(drop, true, false);
        }

        component.silentModify(-dropped);
    }
}
