package notsosig.numismaticoverhaul.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import notsosig.numismaticoverhaul.villagers.json.VillagerTradesHandler;

import java.util.Collections;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    @Inject(method = "addNewPlayer", at = @At("TAIL"))
    public void playerConnect(ServerPlayer player, CallbackInfo ci) {
        VillagerTradesHandler.broadcastErrors(Collections.singletonList(player));
    }
}
