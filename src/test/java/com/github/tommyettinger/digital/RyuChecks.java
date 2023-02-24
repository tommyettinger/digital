package com.github.tommyettinger.digital;

public class RyuChecks {
    public static double nextExclusiveDouble (AlternateRandom random) {
        final long bits = random.nextLong();
        return BitConversion.longBitsToDouble(1022L - Long.numberOfTrailingZeros(bits) << 52 | bits >>> 12);
    }
    public static float nextExclusiveFloat (AlternateRandom random) {
        final long bits = random.nextLong();
        return BitConversion.intBitsToFloat(126 - Long.numberOfTrailingZeros(bits) << 23 | (int)(bits >>> 41));
    }

    /**
     * Some interesting results:
     * It should be obvious when you run this that Ryu prints a lot more digits. I'm not sure why this is.
     * The decimal number Ryu prints as {@code 0.00039954273149628435} gets printed differently by Java depending on
     * whether it is using {@code %g} or {@code %f}, with {@code 0.000399543} for the former and {@code 0.000400} for
     * the latter.
     * @param args unused
     */
    public static void main(String[] args) {
        AlternateRandom random = new AlternateRandom(1234567890L);
        for (int i = 0; i < 100; i++) {
            double d = (nextExclusiveDouble(random) / nextExclusiveDouble(random)) * (nextExclusiveDouble(random) - 0.5);
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s\n", RyuDouble.general(d), RyuDouble.decimal(d), RyuDouble.scientific(d));
        }
        random.setSeed(1234567890L);
        for (int i = 0; i < 100; i++) {
            float d = (nextExclusiveFloat(random) / nextExclusiveFloat(random)) * (nextExclusiveFloat(random) - 0.5f);
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s\n", RyuFloat.general(d), RyuFloat.decimal(d), RyuFloat.scientific(d));
        }

        System.out.println("\nDOUBLE SPECIALS:\n");
        for (double d : new double[]{Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NaN, 0.0, -0.0}) {
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s\n", RyuDouble.general(d), RyuDouble.decimal(d), RyuDouble.scientific(d));
        }

        System.out.println("\nFLOAT SPECIALS:\n");
        for (float d : new float[]{Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NaN, 0.0f, -0.0f}) {
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s\n", RyuFloat.general(d), RyuFloat.decimal(d), RyuFloat.scientific(d));
        }
        {
            String sb = RyuDouble.appendDecimal(new StringBuilder("junk: "), Double.MIN_NORMAL).append(", and more").toString();
            double parsed = Base.readDouble(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Double.MIN_NORMAL);
        }
        System.out.println();
        {
            String sb = RyuFloat.appendDecimal(new StringBuilder("junk: "), Float.MIN_NORMAL).append(", and more").toString();
            float parsed = Base.readFloat(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Float.MIN_NORMAL);
        }
        System.out.println();

        System.out.println(Base.isJavaNumber("0009E0")); // incorrect
        System.out.println(Base.isValidFloatingPoint("0009E0")); // correct!

//Well, now you know why we use scientific notation -- so we don't print Double.MIN_NORMAL as 0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000022250738585072014
    }
}
