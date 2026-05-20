package notsosig.numismaticoverhaul.currency;

public class CurrencyResolver {

    public static long[] splitValues(long rawValue) {
        long[] output = new long[]{0, 0, 0};

        if (rawValue / 10000 != 0) {
            output[2] = rawValue / 10000;
            rawValue -= 10000 * output[2];
        }

        if (rawValue / 100 != 0) {
            output[1] = rawValue / 100;
            rawValue -= 100 * output[1];
        }

        if (rawValue > 0) {
            output[0] = rawValue;
        }

        return output;
    }

    public static long combineValues(long[] values) {
        if (values.length != 3) throw new IllegalArgumentException("Input array has to have 3 elements");
        return values[0] + values[1] * 100 + values[2] * 10000;
    }
}
