package notsosig.numismaticoverhaul;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import notsosig.numismaticoverhaul.cap.CurrencyHolderAttacher;
import notsosig.numismaticoverhaul.client.NumismaticOverhaulClient;
import notsosig.numismaticoverhaul.config.NOClientConfig;
import notsosig.numismaticoverhaul.config.NOConfig;
import notsosig.numismaticoverhaul.event.ClientModEvents;
import notsosig.numismaticoverhaul.currency.MoneyBagLootEntry;
import notsosig.numismaticoverhaul.init.*;
import notsosig.numismaticoverhaul.loot_stuff.AddItemModifier;
import notsosig.numismaticoverhaul.loot_stuff.MoneyBagLootModifier;
import notsosig.numismaticoverhaul.network.NetworkHandler;
import notsosig.numismaticoverhaul.villagers.json.VillagerTradesHandler;

@Mod(NumismaticOverhaul.MODID)
public class NumismaticOverhaul {

    public static final String MODID = "numismaticoverhaul";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final LootPoolEntryType MONEY_BAG_ENTRY = new LootPoolEntryType(MoneyBagLootEntry.CODEC);

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddItemModifier>> ADD_ITEM =
            LOOT_MODIFIER_SERIALIZERS.register("add_item", AddItemModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<MoneyBagLootModifier>> MONEY_BAG =
            LOOT_MODIFIER_SERIALIZERS.register("money_bag", MoneyBagLootModifier.CODEC);

    public NumismaticOverhaul(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NOConfig.CONFIG_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, NOClientConfig.CLIENT_SPEC);

        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ItemInit.ITEMS.register(modEventBus);
        EntityInit.ENTITIES.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        BlockInit.BLOCK_ENTITIES.register(modEventBus);
        CreativeTabInit.TABS.register(modEventBus);
        CurrencyHolderAttacher.register(modEventBus);
        MenuInit.MENU_TYPES.register(modEventBus);
        ModDataComponents.register(modEventBus);
        SoundInit.SOUNDS.register(modEventBus);
        modEventBus.addListener(NetworkHandler::register);
        modEventBus.addListener(EntityInit::attribs);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(NumismaticOverhaulClient::onClientSetup);
            modEventBus.addListener(NumismaticOverhaulClient::registerScreens);
            modEventBus.addListener(NumismaticOverhaulClient::registerBlockEntity);
            modEventBus.addListener(ClientModEvents::onTooltip);
            modEventBus.addListener(ClientModEvents::onRegisterReloadListeners);
        }

        VillagerTradesHandler.registerDefaultAdapters();
    }

    public static final Component PREFIX = Component.empty().withStyle(ChatFormatting.GRAY)
            .append(withColor("o", 0x3955e5))
            .append(withColor("ω", 0x13a6f0))
            .append(withColor("o", 0x3955e5))
            .append(Component.literal(" > ").withStyle(ChatFormatting.GRAY));

    public static MutableComponent withColor(String text, int color) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(color));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
