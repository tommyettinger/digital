package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Test;

public class HasherTest {
    @Test
    public void test2D() {
        byte[][] byte2D = new byte[10][10], byte2D2 = new byte[10][10];
        Assert.assertEquals(Hasher.astaroth.hash(byte2D), Hasher.astaroth.hash(byte2D2));
        char[][] char2D = new char[10][10], char2D2 = new char[10][10];
        Assert.assertEquals(Hasher.astaroth.hash(char2D), Hasher.astaroth.hash(char2D2));
        int[][] int2D = new int[10][10], int2D2 = new int[10][10];
        Assert.assertEquals(Hasher.astaroth.hash(int2D), Hasher.astaroth.hash(int2D2));
        long[][] long2D = new long[10][10], long2D2 = new long[10][10];
        Assert.assertEquals(Hasher.astaroth.hash(long2D), Hasher.astaroth.hash(long2D2));
        float[][] float2D = new float[10][10], float2D2 = new float[10][10];
        Assert.assertEquals(Hasher.astaroth.hash(float2D), Hasher.astaroth.hash(float2D2));
        double[][] double2D = new double[10][10], double2D2 = new double[10][10];
        Assert.assertEquals(Hasher.astaroth.hash(double2D), Hasher.astaroth.hash(double2D2));
    }
}