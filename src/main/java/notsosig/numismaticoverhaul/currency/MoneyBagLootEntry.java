package notsosig.numismaticoverhaul.currency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import notsosig.numismaticoverhaul.NumismaticOverhaul;
import notsosig.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.function.Consumer;

public class MoneyBagLootEntry extends LootPoolSingletonContainer {

    public static final MapCodec<MoneyBagLootEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.optionalFieldOf("min", 0).forGetter(o -> o.min),
            Codec.INT.fieldOf("max").forGetter(o -> o.max)
    ).and(singletonFields(inst)).apply(inst, MoneyBagLootEntry::new));

    private final int min;
    private final int max;

    private MoneyBagLootEntry(int min, int max, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.min = min;
        this.max = max;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> lootConsumer, LootContext context) {
        int value = Mth.nextInt(context.getRandom(), min, max);
        if (value == 0) return;
        lootConsumer.accept(MoneyBagItem.createCombined(CurrencyResolver.splitValues(value)));
    }

    public static Builder<?> builder(int min, int max) {
        return simpleBuilder((weight, quality, conditions, functions) -> new MoneyBagLootEntry(min, max, weight, quality, conditions, functions));
    }

    @Override
    public LootPoolEntryType getType() {
        return NumismaticOverhaul.MONEY_BAG_ENTRY;
    }
}
