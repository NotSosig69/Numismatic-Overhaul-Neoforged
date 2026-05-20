package notsosig.numismaticoverhaul.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class NOClientConfig {

    public static final ModConfigSpec CLIENT_SPEC;
    public static final NOClientConfig CLIENT;

    static {
        Pair<NOClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(NOClientConfig::new);
        CLIENT_SPEC = pair.getRight();
        CLIENT = pair.getLeft();
    }

    public ModConfigSpec.IntValue pursePositionX;
    public ModConfigSpec.IntValue pursePositionY;

    public NOClientConfig(ModConfigSpec.Builder builder) {
        builder.push("purseHud");
        pursePositionX = builder.defineInRange("pursePositionX", 129, 0, 1000);
        pursePositionY = builder.defineInRange("pursePositionY", 20, 0, 2000);
        builder.pop();
    }
}
