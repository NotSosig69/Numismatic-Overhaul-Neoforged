package notsosig.numismaticoverhaul.cap;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import notsosig.numismaticoverhaul.NumismaticOverhaul;

import java.util.function.Supplier;

public class CurrencyHolderAttacher {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, NumismaticOverhaul.MODID);

    public static final Supplier<AttachmentType<CurrencyHolder>> CURRENCY_HOLDER =
            ATTACHMENT_TYPES.register("currency_holder", () ->
                    AttachmentType.builder(CurrencyHolder::new)
                            .serialize(Codec.LONG.xmap(
                                    v -> { CurrencyHolder h = new CurrencyHolder(); h.setValue(v); return h; },
                                    CurrencyHolder::getValue
                            ))
                            .copyOnDeath()
                            .build()
            );

    public static CurrencyHolder getCurrencyHolder(Entity entity) {
        return entity.getData(CURRENCY_HOLDER.get());
    }

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
