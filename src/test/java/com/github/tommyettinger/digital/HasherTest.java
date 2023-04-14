package com.github.tommyettinger.digital;

import com.github.tommyettinger.digital.v020.OldHasher;
import org.junit.Assert;
import org.junit.Test;

public class HasherTest {
    @Test
    public void test2D() {
        byte[][] byte2D = new byte[10][10], byte2D2 = new byte[10][10];
        char[][] char2D = new char[10][10], char2D2 = new char[10][10];
        int[][] int2D = new int[10][10], int2D2 = new int[10][10];
        long[][] long2D = new long[10][10], long2D2 = new long[10][10];
        float[][] float2D = new float[10][10], float2D2 = new float[10][10];
        double[][] double2D = new double[10][10], double2D2 = new double[10][10];
        String[][] string2D = new String[][]{
            ArrayTools.stringSpan(0, 10),
            ArrayTools.stringSpan(10, 10),
            ArrayTools.stringSpan(20, 10),
            ArrayTools.stringSpan(30, 10),
            ArrayTools.stringSpan(40, 10),
            ArrayTools.stringSpan(50, 10),
            ArrayTools.stringSpan(60, 10),
            ArrayTools.stringSpan(70, 10),
            ArrayTools.stringSpan(80, 10),
            ArrayTools.stringSpan(90, 10),
        };
        ArrayTools.shuffle2D(string2D, new AlternateRandom(123));
        String[][] string2D2 = new String[10][10];
        for (int i = 0; i < 10; i++) {
            System.arraycopy(string2D[i], 0, string2D2[i], 0, 10);
        }
        Assert.assertEquals(Hasher.astaroth.hash(byte2D), OldHasher.astaroth.hash(byte2D2));
        Assert.assertEquals(Hasher.astaroth.hash(char2D), OldHasher.astaroth.hash(char2D2));
        Assert.assertEquals(Hasher.astaroth.hash(int2D), OldHasher.astaroth.hash(int2D2));
        Assert.assertEquals(Hasher.astaroth.hash(long2D), OldHasher.astaroth.hash(long2D2));
        Assert.assertEquals(Hasher.astaroth.hash(float2D), OldHasher.astaroth.hash(float2D2));
        Assert.assertEquals(Hasher.astaroth.hash(double2D), OldHasher.astaroth.hash(double2D2));
        Assert.assertEquals(Hasher.astaroth.hash(string2D), OldHasher.astaroth.hash(string2D2));

        Assert.assertEquals(Hasher.astaroth.hash64(byte2D), OldHasher.astaroth.hash64(byte2D2));
        Assert.assertEquals(Hasher.astaroth.hash64(char2D), OldHasher.astaroth.hash64(char2D2));
        Assert.assertEquals(Hasher.astaroth.hash64(int2D), OldHasher.astaroth.hash64(int2D2));
        Assert.assertEquals(Hasher.astaroth.hash64(long2D), OldHasher.astaroth.hash64(long2D2));
        Assert.assertEquals(Hasher.astaroth.hash64(float2D), OldHasher.astaroth.hash64(float2D2));
        Assert.assertEquals(Hasher.astaroth.hash64(double2D), OldHasher.astaroth.hash64(double2D2));
        Assert.assertEquals(Hasher.astaroth.hash64(string2D), OldHasher.astaroth.hash64(string2D2));

    }
}