package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Test;

import static com.github.tommyettinger.digital.TrigTools.*;

public class TrigTest {
    @Test
    public void testTableIndex(){
        for (float i = -10; i <= 10; i+=0.25f) {
            System.out.printf("Math % 6.2f:          , sin %.10f, cos %.10f\n", i,
                    Math.sin(i),
                    Math.cos(i));
            System.out.printf("Plus % 6.2f: idx %05d, sin %.10f, cos %.10f\n", i, TrigTools.radiansToTableIndex(i),
                    SIN_TABLE[TrigTools.radiansToTableIndex(i)],
                    SIN_TABLE[TrigTools.radiansToTableIndex(i) + TrigTools.SIN_TO_COS & TABLE_MASK]);
            System.out.printf("Tabs % 6.2f: idx %05d, sin %.10f, cos %.10f\n", i, TrigTools.radiansToTableIndex(i),
                    SIN_TABLE[TrigTools.radiansToTableIndex(i)],
                    COS_TABLE[TrigTools.radiansToTableIndex(i)]);

            Assert.assertEquals("Failed with i="+i, Math.sin(i), SIN_TABLE_D[TrigTools.radiansToTableIndex(i)], 0.001);
            Assert.assertEquals("Failed with i="+i, Math.cos(i), COS_TABLE_D[TrigTools.radiansToTableIndex(i)], 0.001);
        }
    }
    @Test
    public void testRadians(){
        for (float i = -6.5f; i <= 6.5f; i+=0.25f) {
            Assert.assertEquals(Math.sin(i), TrigTools.sin(i), 0.001f);
            Assert.assertEquals(Math.cos(i), TrigTools.cos(i), 0.001f);
            Assert.assertEquals(Math.sin(i), TrigTools.sinSmooth(i), 0.0005f);
            Assert.assertEquals(Math.cos(i), TrigTools.cosSmooth(i), 0.0005f);
            Assert.assertEquals(Math.sin(i), TrigTools.sinSmoother(i), 0.000001f);
            Assert.assertEquals(Math.cos(i), TrigTools.cosSmoother(i), 0.000001f);

            if (Math.abs(((i) % PI_D + PI_D) % PI_D - PI_D * 0.5) > 0.1) {
                Assert.assertEquals("with i = " + i, Math.tan(i), TrigTools.tan(i), 0.001f);
                Assert.assertEquals("with i = " + i, Math.tan(i), TrigTools.tanSmoother(i), 0.0001f);
            }
        }
    }
    @Test
    public void testDegrees(){
        for (float i = -360.5f; i <= 360.5f; i+=0.25f) {
            Assert.assertEquals(Math.sin(Math.toRadians(i)), TrigTools.sinDeg(i), 0.001f);
            Assert.assertEquals(Math.cos(Math.toRadians(i)), TrigTools.cosDeg(i), 0.001f);
            Assert.assertEquals(Math.sin(Math.toRadians(i)), TrigTools.sinSmoothDeg(i), 0.0005f);
            Assert.assertEquals(Math.cos(Math.toRadians(i)), TrigTools.cosSmoothDeg(i), 0.0005f);
            Assert.assertEquals(Math.sin(Math.toRadians(i)), TrigTools.sinSmootherDeg(i), 0.000001f);
            Assert.assertEquals(Math.cos(Math.toRadians(i)), TrigTools.cosSmootherDeg(i), 0.000001f);
            double limit = Math.abs(((i) % 180.0 + 180.0) % 180.0 - 180.0 * 0.5);
            if (limit > 1.0) {
                Assert.assertEquals("with i = " + i + ", limit = " + limit, Math.tan(Math.toRadians(i)), TrigTools.tanDeg(i), 0.05f);
                Assert.assertEquals("with i = " + i + ", limit = " + limit, Math.tan(Math.toRadians(i)), TrigTools.tanSmootherDeg(i), 0.001f);
            }
        }
    }
    @Test
    public void testTurns(){
        for (float i = -1.125f; i <= 1.125f; i+=0x1p-5f) {
            Assert.assertEquals(Math.sin((i * PI2_D)), TrigTools.sinTurns(i), 0.001f);
            Assert.assertEquals(Math.cos((i * PI2_D)), TrigTools.cosTurns(i), 0.001f);
            Assert.assertEquals(Math.sin((i * PI2_D)), TrigTools.sinSmoothTurns(i), 0.0005f);
            Assert.assertEquals(Math.cos((i * PI2_D)), TrigTools.cosSmoothTurns(i), 0.0005f);
            Assert.assertEquals(Math.sin((i * PI2_D)), TrigTools.sinSmootherTurns(i), 0.000001f);
            Assert.assertEquals(Math.cos((i * PI2_D)), TrigTools.cosSmootherTurns(i), 0.000001f);
            double limit = Math.abs(((i) % 0.50 + 0.50) % 0.50 - 0.50 * 0.5);
            if (limit > 0.01) {
                Assert.assertEquals("with i = " + i + ", limit = " + limit, Math.tan((i * PI2_D)), TrigTools.tanTurns(i), 0.05f);
                Assert.assertEquals("with i = " + i + ", limit = " + limit, Math.tan((i * PI2_D)), TrigTools.tanSmootherTurns(i), 0.001f);
            }
        }
    }

    @Test
    public void testInverse() {
        for (double i = -1.0; i <= 1.0; i+=0x1p-5) {
            Assert.assertEquals(Math.asin(i), TrigTools.asin(i), 0.0001f);
            Assert.assertEquals(Math.acos(i), TrigTools.acos(i), 0.0001f);
            Assert.assertEquals(Math.asin(i), TrigTools.asinDeg(i) * degreesToRadiansD, 0.0001f);
            Assert.assertEquals(Math.acos(i), TrigTools.acosDeg(i) * degreesToRadiansD, 0.0001f);
            Assert.assertEquals(Math.asin(i), TrigTools.asinTurns(i) * PI2_D, 0.0001f);
            Assert.assertEquals(Math.acos(i), TrigTools.acosTurns(i) * PI2_D, 0.0001f);
        }
        for (double i = -10.0; i <= 10.0; i+= 0x1p-5) {
            Assert.assertEquals(Math.atan(i), TrigTools.atan(i), 0.00001f);
            Assert.assertEquals(Math.atan(i), TrigTools.atanDeg(i) * degreesToRadiansD, 0.00001f);
            Assert.assertEquals(Math.atan(i), TrigTools.atanTurns(i) * PI2_D, 0.00001f);
        }
    }
}
