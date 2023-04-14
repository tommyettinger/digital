package com.github.tommyettinger.digital;

import com.github.tommyettinger.digital.v020.OldHasher;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
    @Test
    public void testRanges() {
        byte[] bytes = "Satchmo, my baby cat".getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;
        byte[] bytes2 = Arrays.copyOf(bytes, len-1);
        char[] chars = "Satchmo, my baby cat".toCharArray(), chars2 = Arrays.copyOf(chars, chars.length-1);
        int[] ints = ArrayTools.range(len), ints2 = Arrays.copyOf(ints, ints.length-1);
        AlternateRandom rng = new AlternateRandom(123);
        long[] longs = new long[len];
        for (int i = 0; i < len; i++) {
            longs[i] = rng.nextLong();
        }
        long[] longs2 = Arrays.copyOf(longs, longs.length-1);
        float[] floats = new float[len];
        for (int i = 0; i < len; i++) {
            floats[i] = rng.nextFloat();
        }
        float[] floats2 = Arrays.copyOf(floats, floats.length-1);
        double[] doubles = new double[len];
        for (int i = 0; i < len; i++) {
            doubles[i] = rng.nextDouble();
        }
        double[] doubles2 = Arrays.copyOf(doubles, doubles.length-1);
        String[] strings = ArrayTools.stringSpan(30, len), strings2 = Arrays.copyOf(strings, strings.length-1);
        Assert.assertEquals(Hasher.astaroth.hash(bytes, 0, bytes.length-1), OldHasher.astaroth.hash(bytes2));
        Assert.assertEquals(Hasher.astaroth.hash(chars, 0, chars.length-1), OldHasher.astaroth.hash(chars2));
        Assert.assertEquals(Hasher.astaroth.hash(ints, 0, ints.length-1), OldHasher.astaroth.hash(ints2));
        Assert.assertEquals(Hasher.astaroth.hash(longs, 0, longs.length-1), OldHasher.astaroth.hash(longs2));
        Assert.assertEquals(Hasher.astaroth.hash(floats, 0, floats.length-1), OldHasher.astaroth.hash(floats2));
        Assert.assertEquals(Hasher.astaroth.hash(doubles, 0, doubles.length-1), OldHasher.astaroth.hash(doubles2));
        Assert.assertEquals(Hasher.astaroth.hash(strings, 0, strings.length-1), OldHasher.astaroth.hash(strings2));

        Assert.assertEquals(Hasher.astaroth.hash64(bytes, 0, bytes.length-1), OldHasher.astaroth.hash64(bytes2));
        Assert.assertEquals(Hasher.astaroth.hash64(chars, 0, chars.length-1), OldHasher.astaroth.hash64(chars2));
        Assert.assertEquals(Hasher.astaroth.hash64(ints, 0, ints.length-1), OldHasher.astaroth.hash64(ints2));
        Assert.assertEquals(Hasher.astaroth.hash64(longs, 0, longs.length-1), OldHasher.astaroth.hash64(longs2));
        Assert.assertEquals(Hasher.astaroth.hash64(floats, 0, floats.length-1), OldHasher.astaroth.hash64(floats2));
        Assert.assertEquals(Hasher.astaroth.hash64(doubles, 0, doubles.length-1), OldHasher.astaroth.hash64(doubles2));
        Assert.assertEquals(Hasher.astaroth.hash64(strings, 0, strings.length-1), OldHasher.astaroth.hash64(strings2));
    }
}