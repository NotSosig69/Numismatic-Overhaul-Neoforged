package notsosig.numismaticoverhaul.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import notsosig.numismaticoverhaul.client.gui.NOConfigScreen;
import notsosig.numismaticoverhaul.client.gui.PiggyBankScreen;
import notsosig.numismaticoverhaul.client.gui.ShopScreen;
import notsosig.numismaticoverhaul.init.BlockInit;
import notsosig.numismaticoverhaul.init.ItemInit;
import notsosig.numismaticoverhaul.init.MenuInit;
import notsosig.numismaticoverhaul.item.MoneyBagItem;

public class NumismaticOverhaulClient {

    public static void onClientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (container, parent) -> NOConfigScreen.create(parent));

        event.enqueueWork(() -> {
            ItemProperties.register(ItemInit.BRONZE_COIN.get(), ResourceLocation.parse("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
            ItemProperties.register(ItemInit.SILVER_COIN.get(), ResourceLocation.parse("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
            ItemProperties.register(ItemInit.GOLD_COIN.get(), ResourceLocation.parse("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);

            ItemProperties.register(ItemInit.MONEY_BAG.get(), ResourceLocation.parse("size"), (stack, world, entity, seed) -> {
                long[] values = ((MoneyBagItem) ItemInit.MONEY_BAG.get()).getCombinedValue(stack);
                if (values.length < 3) return 0;
                if (values[2] > 0) return 1;
                if (values[1] > 0) return .5f;
                return 0;
            });
        });
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuInit.SHOP.get(), ShopScreen::new);
        event.register(MenuInit.PIGGY_BANK.get(), PiggyBankScreen::new);
    }

    public static void registerBlockEntity(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockInit.SHOP_BE.get(), ShopBlockEntityRender::new);
    }
}
