package notsosig.numismaticoverhaul.init;

import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.block.PiggyBankScreenHandler;
import notsosig.numismaticoverhaul.block.ShopScreenHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuInit {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, NumismaticOverhaul.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ShopScreenHandler>> SHOP =
            MENU_TYPES.register("shop", () -> IMenuTypeExtension.create(ShopScreenHandler::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PiggyBankScreenHandler>> PIGGY_BANK =
            MENU_TYPES.register("piggy_bank", () -> new MenuType<>(PiggyBankScreenHandler::new, FeatureFlags.DEFAULT_FLAGS));
}
