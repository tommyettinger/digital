package com.github.tommyettinger.digital;

import org.junit.Test;

import static com.github.tommyettinger.digital.BitConversion.longBitsToDouble;

public class DistributorTest {
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

        System.out.println("Distributor.probitHighPrecision(0.0) == " + Distributor.probitHighPrecision(0.0));
        System.out.println("Distributor.probitHighPrecision(Double.MIN_VALUE) == " + Distributor.probitHighPrecision(Double.MIN_VALUE));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(0x2L)) == " + Distributor.probitHighPrecision(longBitsToDouble(0x2L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(0x0010000000000000L>>>1)) == " + Distributor.probitHighPrecision(longBitsToDouble(0x0010000000000000L>>>1)));
        System.out.println("Distributor.probitHighPrecision(Double.MIN_NORMAL) == " + Distributor.probitHighPrecision(Double.MIN_NORMAL));
        System.out.println("Distributor.probitHighPrecision(0x1p-53) == " + Distributor.probitHighPrecision(0x1p-53));
        System.out.println("Distributor.probitHighPrecision(0x2p-53) == " + Distributor.probitHighPrecision(0x2p-53));
        System.out.println("Distributor.probitHighPrecision(0.5 - 0x1p-53) == " + Distributor.probitHighPrecision(0.5 - 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(0.5 - 0x1p-54) == " + Distributor.probitHighPrecision(0.5 - 0x1p-54));
        System.out.println("Distributor.probitHighPrecision(0.5) == " + Distributor.probitHighPrecision(0.5));
        System.out.println("Distributor.probitHighPrecision(0.5 + 0x1p-53) == " + Distributor.probitHighPrecision(0.5 + 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(1.0 - 0x2p-53) == " + Distributor.probitHighPrecision(1.0 - 0x2p-53));
        System.out.println("Distributor.probitHighPrecision(1.0 - 0x1p-53) == " + Distributor.probitHighPrecision(1.0 - 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(1.0) == " + Distributor.probitHighPrecision(1.0));

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
            System.out.println("Distributor.probitHighPrecision(longBitsToDouble(1L << " + i + ")) == " + Distributor.probitHighPrecision(longBitsToDouble(1L << i)));
        }
        System.out.println();
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(10L << 40)) == " + Distributor.probitHighPrecision(longBitsToDouble(10L << 40)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(11L << 40)) == " + Distributor.probitHighPrecision(longBitsToDouble(11L << 40)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(12L << 40)) == " + Distributor.probitHighPrecision(longBitsToDouble(12L << 40)));
        System.out.println();
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(21L << 39)) == " + Distributor.probitHighPrecision(longBitsToDouble(21L << 39)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble((21L << 39) + 1L)) == " + Distributor.probitHighPrecision(longBitsToDouble((21L << 39) + 1L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble((22L << 39) - 1L)) == " + Distributor.probitHighPrecision(longBitsToDouble((22L << 39) - 1L)));
        for (int i = 0; i < 39; i++) {
            System.out.println("Distributor.probitHighPrecision(longBitsToDouble((22L << 39) - (1L<<" + i + "))) == " + Distributor.probitHighPrecision(longBitsToDouble((22L << 39) - (1L << i))));
        }
        System.out.println();
        long start = (22L << 39) - (1L<<37), end = (22L << 39) - (1L<<38);
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(start)) == " + Distributor.probitHighPrecision(longBitsToDouble(start)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(start-1L)) == " + Distributor.probitHighPrecision(longBitsToDouble(start-1L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(end+1L)) == " + Distributor.probitHighPrecision(longBitsToDouble(end+1L)));
        System.out.println("Distributor.probitHighPrecision(longBitsToDouble(end)) == " + Distributor.probitHighPrecision(longBitsToDouble(end)));
        System.out.println("Starting at " + start + " and working down to " + end + ":");
        for (long i = start; i > end; i--) {
            if(Double.isNaN(Distributor.probitHighPrecision(longBitsToDouble(i)))){
                System.out.println(i + " is out of range!");
                System.out.println(("Double " + longBitsToDouble(i+1L) + " is in range!"));
                break;
            }
        }
    }
}
