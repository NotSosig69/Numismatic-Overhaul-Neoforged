package notsosig.numismaticoverhaul.init;

import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NumismaticOverhaul.MODID);
    public static final List<Supplier<? extends ItemLike>> TAB_ITEMS = new ArrayList<>();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NUMISMATIC_GROUP = TABS.register("numismatic_group",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.numismaticoverhaul"))
                    .icon(() -> MoneyBagItem.createCombined(new long[]{0, 1, 0}))
                    .displayItems((displayParams, output) ->
                            TAB_ITEMS.forEach(itemLike -> output.accept(itemLike.get())))
                    .build()
    );

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <R, T extends R> DeferredHolder<R, T> addToTab(DeferredHolder<R, T> itemLike) {
        TAB_ITEMS.add((Supplier<? extends ItemLike>)(Supplier) itemLike);
        return itemLike;
    }
}
