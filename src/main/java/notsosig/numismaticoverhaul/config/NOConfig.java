package notsosig.numismaticoverhaul.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class NOConfig {

    public static final ModConfigSpec CONFIG_SPEC;
    public static final NOConfig INSTANCE;

    static {
        Pair<NOConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(NOConfig::new);
        CONFIG_SPEC = pair.getRight();
        INSTANCE = pair.getLeft();
    }

    public ModConfigSpec.DoubleValue moneyDropChancePeaceful;
    public ModConfigSpec.DoubleValue moneyDropChanceEasy;
    public ModConfigSpec.DoubleValue moneyDropChanceNormal;
    public ModConfigSpec.DoubleValue moneyDropChanceHard;
    public ModConfigSpec.BooleanValue wrapModTrades;
    public ModConfigSpec.BooleanValue enableDenominationExchange;
    public ModConfigSpec.BooleanValue enableSmartAutofill;
    public ModConfigSpec.BooleanValue showCurrencyActionBar;
    public ModConfigSpec.LongValue maxPurseValue;
    public ModConfigSpec.ConfigValue<List<? extends String>> structuresToHaveCoins;

    private NOConfig(ModConfigSpec.Builder builder) {
        builder.push("deathPenalty");
        moneyDropChancePeaceful = builder.comment("% of purse dropped on death in Peaceful (default 0)").defineInRange("peaceful", 0.0, 0.0, 100.0);
        moneyDropChanceEasy = builder.comment("% of purse dropped on death in Easy (default 25)").defineInRange("easy", 25.0, 0.0, 100.0);
        moneyDropChanceNormal = builder.comment("% of purse dropped on death in Normal (default 50)").defineInRange("normal", 50.0, 0.0, 100.0);
        moneyDropChanceHard = builder.comment("% of purse dropped on death in Hard (default 75)").defineInRange("hard", 75.0, 0.0, 100.0);
        builder.pop();

        builder.push("trading");
        wrapModTrades = builder
                .comment("When enabled, emerald prices on trades added by other mods (including VillagerAPI custom professions) are automatically converted to coin equivalents. Disable if you are using a separate economy mod that should keep its own emerald prices.")
                .define("wrapModTrades", true);
        enableDenominationExchange = builder
                .comment("Allow paying with a higher denomination coin and receiving change back. E.g. pay 1 silver on a 50 bronze trade and get 50 bronze change.")
                .define("enableDenominationExchange", true);
        enableSmartAutofill = builder
                .comment("Automatically fill trade payment slots from the purse. When the exact coin count would exceed the 99-stack limit, a higher denomination is used automatically.")
                .define("enableSmartAutofill", true);
        showCurrencyActionBar = builder
                .comment("Show an action bar message whenever the purse balance changes (e.g. after a trade or picking up coins).")
                .define("showCurrencyActionBar", true);
        builder.pop();

        builder.push("economy");
        maxPurseValue = builder
                .comment("Maximum value (in bronze) the purse can hold. -1 = unlimited (default). Example: 9999999 = 999 gold, 99 silver, 99 bronze.")
                .defineInRange("maxPurseValue", -1L, -1L, Long.MAX_VALUE);
        builder.pop();

        builder.push("loot");
        structuresToHaveCoins = builder.comment("Structures that have coins, specific rates can be changed via datapacks.")
                .defineListAllowEmpty("structures",
                        ImmutableList.of(
                                BuiltInLootTables.STRONGHOLD_LIBRARY.location().toString(),
                                BuiltInLootTables.BASTION_TREASURE.location().toString(),
                                BuiltInLootTables.STRONGHOLD_CORRIDOR.location().toString(),
                                BuiltInLootTables.PILLAGER_OUTPOST.location().toString(),
                                BuiltInLootTables.BURIED_TREASURE.location().toString(),
                                BuiltInLootTables.SIMPLE_DUNGEON.location().toString(),
                                BuiltInLootTables.ABANDONED_MINESHAFT.location().toString()
                        ),
                        obj -> true);
        builder.pop();
    }
}
