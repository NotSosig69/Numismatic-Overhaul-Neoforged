package notsosig.numismaticoverhaul.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.config.NOConfig;
import notsosig.numismaticoverhaul.villagers.data.NumismaticVillagerTradesRegistry;
import notsosig.numismaticoverhaul.villagers.data.VillagerTradesResourceListener;

import java.util.HashMap;

@EventBusSubscriber(modid = NumismaticOverhaul.MODID)
public class CommonForgeEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.NONE) return;
        if (!NOConfig.INSTANCE.wrapModTrades.get()) return;
        event.getTrades().forEach((level, trades) -> {
            if (trades != null && !trades.isEmpty()) {
                NumismaticVillagerTradesRegistry.registerFabricVillagerTrades(event.getType(), level, trades);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWanderingTrades(WandererTradesEvent event) {
        if (!NOConfig.INSTANCE.wrapModTrades.get()) return;
        event.getGenericTrades().forEach(trade -> NumismaticVillagerTradesRegistry.registerWanderingTraderTrade(1, trade));
        event.getRareTrades().forEach(trade -> NumismaticVillagerTradesRegistry.registerWanderingTraderTrade(2, trade));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CurrencyHolderAttacher.getCurrencyHolder(serverPlayer).sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CurrencyHolderAttacher.getCurrencyHolder(serverPlayer).sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void reloadListener(AddReloadListenerEvent event) {
        event.addListener(new VillagerTradesResourceListener());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void doCommonVillagerTrades(ServerStartedEvent event) {
        // All mods (including VillagerAPI) have registered their trades by now.
        // Reset and re-run wrapping so VillagerAPI professions added after the resource
        // listener phase are also caught.
        NumismaticVillagerTradesRegistry.resetWrapped();
        NumismaticVillagerTradesRegistry.wrapModVillagers();

        final Tuple<HashMap<VillagerProfession, Int2ObjectOpenHashMap<VillagerTrades.ItemListing[]>>, Int2ObjectOpenHashMap<VillagerTrades.ItemListing[]>> registry =
                NumismaticVillagerTradesRegistry.getRegistryForLoading();
        VillagerTrades.TRADES.putAll(registry.getA());
    }
}
