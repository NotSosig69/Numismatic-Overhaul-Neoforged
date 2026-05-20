package notsosig.numismaticoverhaul.init;

import com.mojang.serialization.Codec;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.item.MoneyBagValues;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(NumismaticOverhaul.MODID);

    public static final Supplier<DataComponentType<Long>> ORIGINAL_VALUE =
            DATA_COMPONENT_TYPES.registerComponentType("original_value",
                    b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final Supplier<DataComponentType<MoneyBagValues>> MONEY_BAG_COMBINED_VALUES =
            DATA_COMPONENT_TYPES.registerComponentType("money_bag_combined_values",
                    b -> b.persistent(MoneyBagValues.CODEC).networkSynchronized(MoneyBagValues.STREAM_CODEC));

    public static void register(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }
}
