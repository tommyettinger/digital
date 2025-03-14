package com.github.tommyettinger.digital;

import org.junit.Test;

public class DistributorTest {
    @Test
    public void testLimits() {
        System.out.println("Distributor.probit(Double.MIN_NORMAL) == " + Distributor.probit(Double.MIN_NORMAL));
        System.out.println("Distributor.probit(0x1p-53) == " + Distributor.probit(0x1p-53));
        System.out.println("Distributor.probit(0x2p-53) == " + Distributor.probit(0x2p-53));
        System.out.println("Distributor.probit(0.5 - 0x1p-53) == " + Distributor.probit(0.5 - 0x1p-53));
        System.out.println("Distributor.probit(0.5 - 0x1p-54) == " + Distributor.probit(0.5 - 0x1p-54));
        System.out.println("Distributor.probit(0.5) == " + Distributor.probit(0.5));
        System.out.println("Distributor.probit(0.5 + 0x1p-53) == " + Distributor.probit(0.5 + 0x1p-53));
        System.out.println("Distributor.probit(1.0 - 0x2p-53) == " + Distributor.probit(1.0 - 0x2p-53));
        System.out.println("Distributor.probit(1.0 - 0x1p-53) == " + Distributor.probit(1.0 - 0x1p-53));

        System.out.println("Distributor.probitHighPrecision(Double.MIN_NORMAL) == " + Distributor.probitHighPrecision(Double.MIN_NORMAL));
        System.out.println("Distributor.probitHighPrecision(0x1p-53) == " + Distributor.probitHighPrecision(0x1p-53));
        System.out.println("Distributor.probitHighPrecision(0x2p-53) == " + Distributor.probitHighPrecision(0x2p-53));
        System.out.println("Distributor.probitHighPrecision(0.5 - 0x1p-53) == " + Distributor.probitHighPrecision(0.5 - 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(0.5 - 0x1p-54) == " + Distributor.probitHighPrecision(0.5 - 0x1p-54));
        System.out.println("Distributor.probitHighPrecision(0.5) == " + Distributor.probitHighPrecision(0.5));
        System.out.println("Distributor.probitHighPrecision(0.5 + 0x1p-53) == " + Distributor.probitHighPrecision(0.5 + 0x1p-53));
        System.out.println("Distributor.probitHighPrecision(1.0 - 0x2p-53) == " + Distributor.probitHighPrecision(1.0 - 0x2p-53));
        System.out.println("Distributor.probitHighPrecision(1.0 - 0x1p-53) == " + Distributor.probitHighPrecision(1.0 - 0x1p-53));

        System.out.println("Distributor.normal(Long.MIN_VALUE) == " + Distributor.normal(Long.MIN_VALUE));
        System.out.println("Distributor.normal(-3L) == " + Distributor.normal(-3L));
        System.out.println("Distributor.normal(-2L) == " + Distributor.normal(-2L));
        System.out.println("Distributor.normal(-1L) == " + Distributor.normal(-1L));
        System.out.println("Distributor.normal(0L) == " + Distributor.normal(0L));
        System.out.println("Distributor.normal(1L) == " + Distributor.normal(1L));
        System.out.println("Distributor.normal(2L) == " + Distributor.normal(2L));
        System.out.println("Distributor.normal(Long.MAX_VALUE) == " + Distributor.normal(Long.MAX_VALUE));

        System.out.println("Distributor.linearNormal(Integer.MIN_VALUE) == " + Distributor.linearNormal(Long.MIN_VALUE));
        System.out.println("Distributor.linearNormal(-3) == " + Distributor.linearNormal(-3));
        System.out.println("Distributor.linearNormal(-2) == " + Distributor.linearNormal(-2));
        System.out.println("Distributor.linearNormal(-1) == " + Distributor.linearNormal(-1));
        System.out.println("Distributor.linearNormal(0) == " + Distributor.linearNormal(0));
        System.out.println("Distributor.linearNormal(1) == " + Distributor.linearNormal(1));
        System.out.println("Distributor.linearNormal(2) == " + Distributor.linearNormal(2));
        System.out.println("Distributor.linearNormal(Integer.MAX_VALUE) == " + Distributor.linearNormal(Long.MAX_VALUE));

        System.out.println("Distributor.linearNormalF(Integer.MIN_VALUE) == " + Distributor.linearNormalF(Integer.MIN_VALUE));
        System.out.println("Distributor.linearNormalF(-3) == " + Distributor.linearNormalF(-3));
        System.out.println("Distributor.linearNormalF(-2) == " + Distributor.linearNormalF(-2));
        System.out.println("Distributor.linearNormalF(-1) == " + Distributor.linearNormalF(-1));
        System.out.println("Distributor.linearNormalF(0) == " + Distributor.linearNormalF(0));
        System.out.println("Distributor.linearNormalF(1) == " + Distributor.linearNormalF(1));
        System.out.println("Distributor.linearNormalF(2) == " + Distributor.linearNormalF(2));
        System.out.println("Distributor.linearNormalF(Integer.MAX_VALUE) == " + Distributor.linearNormalF(Integer.MAX_VALUE));
    }
}
