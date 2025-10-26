package com.github.tommyettinger.digital;

import org.junit.Test;

import static com.github.tommyettinger.digital.BitConversion.longBitsToDouble;

public class DistributorTest {

    public static double probitHighPrecision(double d)
    {
        double x = Distributor.probit(d);
        /* 5.885886107568E-311 was found by exhaustively searching, rejecting any numbers that would
           make this return NaN. It is the lowest input this can accept without producing NaN. */
        if(d > 0.0 && d < 1.0 && d != 0.5) {
            double e = 0.5 * MathTools.erfc(x * -0.7071067811865475) - d; /* -0.7071067811865475 == -1.0 / Math.sqrt(2.0) */
            double u = e * 2.5066282746310002 * Math.exp(0.5 * x * x); /* 2.5066282746310002 == Math.sqrt(2*Math.PI) */
            x = x - u / (1.0 + 0.5 * x * u);
        }
        return x;
    }

    @Test
    public void testLimits() {
        System.out.println("Distributor.probit(0.0) == " + Distributor.probit(0.0));
        System.out.println("Distributor.probit(Double.MIN_VALUE) == " + Distributor.probit(Double.MIN_VALUE));
        System.out.println("Distributor.probit(longBitsToDouble(0x2L)) == " + Distributor.probit(longBitsToDouble(0x2L)));
        System.out.println("Distributor.probit(longBitsToDouble(0x0010000000000000L>>>1)) == " + Distributor.probit(longBitsToDouble(0x0010000000000000L>>>1)));
        System.out.println("Distributor.probit(Double.MIN_NORMAL) == " + Distributor.probit(Double.MIN_NORMAL));
        System.out.println("Distributor.probit(0x1p-53) == " + Distributor.probit(0x1p-53));
        System.out.println("Distributor.probit(0x2p-53) == " + Distributor.probit(0x2p-53));
        System.out.println("Distributor.probit(0.5 - 0x1p-53) == " + Distributor.probit(0.5 - 0x1p-53));
        System.out.println("Distributor.probit(0.5 - 0x1p-54) == " + Distributor.probit(0.5 - 0x1p-54));
        System.out.println("Distributor.probit(0.5) == " + Distributor.probit(0.5));
        System.out.println("Distributor.probit(0.5 + 0x1p-53) == " + Distributor.probit(0.5 + 0x1p-53));
        System.out.println("Distributor.probit(1.0 - 0x2p-53) == " + Distributor.probit(1.0 - 0x2p-53));
        System.out.println("Distributor.probit(1.0 - 0x1p-53) == " + Distributor.probit(1.0 - 0x1p-53));
        System.out.println("Distributor.probit(1.0) == " + Distributor.probit(1.0));

        System.out.println("Distributor.probitHighPrecision(0.0) == " + probitHighPrecision(0.0));
        System.out.println("Distributor.probitHighPrecision(Double.MIN_VALUE) == " + probitHighPrecision(Double.MIN_VALUE));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(0x2L)) == " + probitHighPrecision(longBitsToDouble(0x2L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(0x0010000000000000L>>>1)) == " + probitHighPrecision(longBitsToDouble(0x0010000000000000L>>>1)));
        System.out.println("Distributor.probitHighPrecision(Double.MIN_NORMAL) == " + probitHighPrecision(Double.MIN_NORMAL));
        System.out.println("Distributor.probitHighPrecision(0x1p-53) == " + probitHighPrecision(0x1p-53));
        System.out.println("Distributor.probitHighPrecision(0x2p-53) == " + probitHighPrecision(0x2p-53));
        System.out.println("Distributor.probitHighPrecision(0.5 - 0x1p-53) == " + probitHighPrecision(0.5 - 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(0.5 - 0x1p-54) == " + probitHighPrecision(0.5 - 0x1p-54));
        System.out.println("Distributor.probitHighPrecision(0.5) == " + probitHighPrecision(0.5));
        System.out.println("Distributor.probitHighPrecision(0.5 + 0x1p-53) == " + probitHighPrecision(0.5 + 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(1.0 - 0x2p-53) == " + probitHighPrecision(1.0 - 0x2p-53));
        System.out.println("Distributor.probitHighPrecision(1.0 - 0x1p-53) == " + probitHighPrecision(1.0 - 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(1.0) == " + probitHighPrecision(1.0));

        System.out.println("Distributor.normal(Long.MIN_VALUE) == " + Distributor.normal(Long.MIN_VALUE));
        System.out.println("Distributor.normal(-3L) == " + Distributor.normal(-3L));
        System.out.println("Distributor.normal(-2L) == " + Distributor.normal(-2L));
        System.out.println("Distributor.normal(-1L) == " + Distributor.normal(-1L));
        System.out.println("Distributor.normal(0L) == " + Distributor.normal(0L));
        System.out.println("Distributor.normal(1L) == " + Distributor.normal(1L));
        System.out.println("Distributor.normal(2L) == " + Distributor.normal(2L));
        System.out.println("Distributor.normal(Long.MAX_VALUE) == " + Distributor.normal(Long.MAX_VALUE));

        System.out.println("Distributor.probitL(Long.MIN_VALUE) == " + Distributor.probitL(Long.MIN_VALUE));
        System.out.println("Distributor.probitL(-3) == " + Distributor.probitL(-3));
        System.out.println("Distributor.probitL(-2) == " + Distributor.probitL(-2));
        System.out.println("Distributor.probitL(-1) == " + Distributor.probitL(-1));
        System.out.println("Distributor.probitL(0) == " + Distributor.probitL(0));
        System.out.println("Distributor.probitL(1) == " + Distributor.probitL(1));
        System.out.println("Distributor.probitL(2) == " + Distributor.probitL(2));
        System.out.println("Distributor.probitL(Long.MAX_VALUE) == " + Distributor.probitL(Long.MAX_VALUE));

        System.out.println("Distributor.probitI(Integer.MIN_VALUE) == " + Distributor.probitI(Integer.MIN_VALUE));
        System.out.println("Distributor.probitI(-3) == " + Distributor.probitI(-3));
        System.out.println("Distributor.probitI(-2) == " + Distributor.probitI(-2));
        System.out.println("Distributor.probitI(-1) == " + Distributor.probitI(-1));
        System.out.println("Distributor.probitI(0) == " + Distributor.probitI(0));
        System.out.println("Distributor.probitI(1) == " + Distributor.probitI(1));
        System.out.println("Distributor.probitI(2) == " + Distributor.probitI(2));
        System.out.println("Distributor.probitI(Integer.MAX_VALUE) == " + Distributor.probitI(Integer.MAX_VALUE));

        System.out.println("Distributor.probitD(0.0) == " + Distributor.probitD(0.0));
        System.out.println("Distributor.probitD(Double.MIN_VALUE) == " + Distributor.probitD(Double.MIN_VALUE));
        System.out.println("Distributor.probitD(Double.MIN_NORMAL) == " + Distributor.probitD(Double.MIN_NORMAL));
        System.out.println("Distributor.probitD(0x1p-53) == " + Distributor.probitD(0x1p-53));
        System.out.println("Distributor.probitD(0x2p-53) == " + Distributor.probitD(0x2p-53));
        System.out.println("Distributor.probitD(0.5 - 0x1p-53) == " + Distributor.probitD(0.5 - 0x1p-53));
        System.out.println("Distributor.probitD(0.5 - 0x1p-54) == " + Distributor.probitD(0.5 - 0x1p-54));
        System.out.println("Distributor.probitD(0.5) == " + Distributor.probitD(0.5));
        System.out.println("Distributor.probitD(0.5 + 0x1p-53) == " + Distributor.probitD(0.5 + 0x1p-53));
        System.out.println("Distributor.probitD(1.0 - 0x2p-53) == " + Distributor.probitD(1.0 - 0x2p-53));
        System.out.println("Distributor.probitD(1.0 - 0x1p-53) == " + Distributor.probitD(1.0 - 0x1p-53));
        System.out.println("Distributor.probitD(1.0) == " + Distributor.probitD(1.0));

        System.out.println("Distributor.probitF(0.0f) == " + Distributor.probitF(0.0f));
        System.out.println("Distributor.probitF(Float.MIN_VALUE) == " + Distributor.probitF(Float.MIN_VALUE));
        System.out.println("Distributor.probitF(Float.MIN_NORMAL) == " + Distributor.probitF(Float.MIN_NORMAL));
        System.out.println("Distributor.probitF(0x1p-24f) == " + Distributor.probitF(0x1p-24f));
        System.out.println("Distributor.probitF(0x2p-24f) == " + Distributor.probitF(0x2p-24f));
        System.out.println("Distributor.probitF(0.5f - 0x1p-24f) == " + Distributor.probitF(0.5f - 0x1p-24f));
        System.out.println("Distributor.probitF(0.5f - 0x1p-25f) == " + Distributor.probitF(0.5f - 0x1p-25f));
        System.out.println("Distributor.probitF(0.5f) == " + Distributor.probitF(0.5f));
        System.out.println("Distributor.probitF(0.5f + 0x1p-24f) == " + Distributor.probitF(0.5f + 0x1p-24f));
        System.out.println("Distributor.probitF(1.0f - 0x2p-24f) == " + Distributor.probitF(1.0f - 0x2p-24f));
        System.out.println("Distributor.probitF(1.0f - 0x1p-24f) == " + Distributor.probitF(1.0f - 0x1p-24f));
        System.out.println("Distributor.probitF(1.0f) == " + Distributor.probitF(1.0f));
    }
    @Test
    public void testHighPrecision() {
        for (int i = 1; i < 52; i++) {
            System.out.println("Distributor.probitHighPrecision(longBitsToDouble(1L << " + i + ")) == " + probitHighPrecision(longBitsToDouble(1L << i)));
        }
        System.out.println();
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(10L << 40)) == " + probitHighPrecision(longBitsToDouble(10L << 40)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(11L << 40)) == " + probitHighPrecision(longBitsToDouble(11L << 40)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(12L << 40)) == " + probitHighPrecision(longBitsToDouble(12L << 40)));
        System.out.println();
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(21L << 39)) == " + probitHighPrecision(longBitsToDouble(21L << 39)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble((21L << 39) + 1L)) == " + probitHighPrecision(longBitsToDouble((21L << 39) + 1L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble((22L << 39) - 1L)) == " + probitHighPrecision(longBitsToDouble((22L << 39) - 1L)));
        for (int i = 0; i < 39; i++) {
            System.out.println("Distributor.probitHighPrecision(longBitsToDouble((22L << 39) - (1L<<" + i + "))) == " + probitHighPrecision(longBitsToDouble((22L << 39) - (1L << i))));
        }
        System.out.println();
        long start = (22L << 39) - (1L<<37), end = (22L << 39) - (1L<<38);
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(start)) == " + probitHighPrecision(longBitsToDouble(start)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(start-1L)) == " + probitHighPrecision(longBitsToDouble(start-1L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(end+1L)) == " + probitHighPrecision(longBitsToDouble(end+1L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(end)) == " + probitHighPrecision(longBitsToDouble(end)));
        System.out.println("Starting at " + start + " and working down to " + end + ":");
        long i = end;
        for (int it = 0; it < 256; it++) {
            if(Double.isNaN(probitHighPrecision(longBitsToDouble(i)))) {
                System.out.println(i + " is out of range!");
                if (!Double.isNaN(probitHighPrecision(longBitsToDouble(i+1L)))) {
                    System.out.println(("Double " + longBitsToDouble(i + 1L) + " or long " + (i + 1L) + " (0x" + Base.BASE16.unsigned(i + 1L) + ") is in range!"));
                    break;
                }
                end = i + 1L;
                i = (end >>> 1) + (start >>> 1);
            } else {
                if (Double.isNaN(probitHighPrecision(longBitsToDouble(i-1L)))) {
                    System.out.println((i - 1L) + " is out of range!");
                    System.out.println(("Double " + longBitsToDouble(i) + " or long " + i + " (0x" + Base.BASE16.unsigned(i) + ") is in range!"));
                    break;
                }
                start = i - 1L;
                i = (end >>> 1) + (start >>> 1);
            }
        }
    }

    /**
     * Tested 1 trillion inputs to {@link Distributor#normalF(long)}.
     * <br>
     * min -7.138506412506104 max 7.182311534881592
     * <br>
     * EARLIER: Tested 100 billion inputs to {@link Distributor#normalF(long)}.
     * <br>
     * min -7.132757663726807 max 6.896588325500488
     * @param args ignored
     */
    public static void main(String[] args) {
        float min = 1E10f, max = -1E10f;
        long c = 0L;
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000000000; j++) {
                float r = Distributor.normalF(c += 0x9E3779B97F4A7C15L);
                min = Math.min(min, r);
                max = Math.max(max, r);
            }
            System.out.printf("Iteration %3d/1000: min %3.15f max %3.15f\n", i, min, max);
        }
    }
}
