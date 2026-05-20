package notsosig.numismaticoverhaul.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MoneyBagValues(long bronze, long silver, long gold) {

    public static final Codec<MoneyBagValues> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("bronze").forGetter(MoneyBagValues::bronze),
            Codec.LONG.fieldOf("silver").forGetter(MoneyBagValues::silver),
            Codec.LONG.fieldOf("gold").forGetter(MoneyBagValues::gold)
        ).apply(instance, MoneyBagValues::new)
    );

    public static final StreamCodec<ByteBuf, MoneyBagValues> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, MoneyBagValues::bronze,
            ByteBufCodecs.VAR_LONG, MoneyBagValues::silver,
            ByteBufCodecs.VAR_LONG, MoneyBagValues::gold,
            MoneyBagValues::new
        );

    public static MoneyBagValues of(long[] arr) {
        return new MoneyBagValues(arr[0], arr[1], arr[2]);
    }

    public long[] toArray() {
        return new long[]{bronze, silver, gold};
    }
}
