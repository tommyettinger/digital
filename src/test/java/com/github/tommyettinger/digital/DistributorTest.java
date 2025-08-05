package com.github.tommyettinger.digital;

import org.junit.Test;

public class DistributorTest {
    @Test
    public void testLimits() {
        System.out.println("Distributor.probit(0.0) == " + Distributor.probit(0.0));
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
}
