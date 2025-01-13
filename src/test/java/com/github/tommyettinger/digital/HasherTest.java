package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;

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
        Assert.assertNotEquals(Hasher.astaroth.hash(byte2D), Hasher.astaroth_.hash(byte2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash(char2D), Hasher.astaroth_.hash(char2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash(int2D), Hasher.astaroth_.hash(int2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash(long2D), Hasher.astaroth_.hash(long2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash(float2D), Hasher.astaroth_.hash(float2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash(double2D), Hasher.astaroth_.hash(double2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash(string2D), Hasher.astaroth_.hash(string2D2));

        Assert.assertNotEquals(Hasher.astaroth.hash64(byte2D), Hasher.astaroth_.hash64(byte2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(char2D), Hasher.astaroth_.hash64(char2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(int2D), Hasher.astaroth_.hash64(int2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(long2D), Hasher.astaroth_.hash64(long2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(float2D), Hasher.astaroth_.hash64(float2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(double2D), Hasher.astaroth_.hash64(double2D2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(string2D), Hasher.astaroth_.hash64(string2D2));

        Assert.assertNotEquals(
                Hasher.astaroth.hashBulk64((Hasher.HashFunction64<long[]>)Hasher.astaroth::hashBulk64, long2D2),
                Hasher.astaroth_.hashBulk64((Hasher.HashFunction64<long[]>)Hasher.astaroth_::hashBulk64, long2D2));
        Assert.assertNotEquals(
                Hasher.astaroth.hashBulk64((Hasher.HashFunction<long[]>)Hasher.astaroth::hashBulk, long2D2),
                Hasher.astaroth_.hashBulk64((Hasher.HashFunction<long[]>)Hasher.astaroth_::hashBulk, long2D2));
        Assert.assertNotEquals(
                Hasher.astaroth.hashBulk((Hasher.HashFunction64<long[]>)Hasher.astaroth::hashBulk64, long2D2),
                Hasher.astaroth_.hashBulk((Hasher.HashFunction64<long[]>)Hasher.astaroth_::hashBulk64, long2D2));
        Assert.assertNotEquals(
                Hasher.astaroth.hashBulk((Hasher.HashFunction<long[]>)Hasher.astaroth::hashBulk, long2D2),
                Hasher.astaroth_.hashBulk((Hasher.HashFunction<long[]>)Hasher.astaroth_::hashBulk, long2D2));

        Assert.assertNotEquals(
                Hasher.hashBulk64(1L, Hasher.longArrayHashBulk64, long2D2),
                Hasher.hashBulk64(2L, Hasher.longArrayHashBulk64, long2D2));
        Assert.assertNotEquals(
                Hasher.hashBulk64(1L, Hasher.longArrayHashBulk, long2D2),
                Hasher.hashBulk64(2L, Hasher.longArrayHashBulk, long2D2));
        Assert.assertNotEquals(
                Hasher.hashBulk(1L, Hasher.longArrayHashBulk64, long2D2),
                Hasher.hashBulk(2L, Hasher.longArrayHashBulk64, long2D2));
        Assert.assertNotEquals(
                Hasher.hashBulk(1L, Hasher.longArrayHashBulk, long2D2),
                Hasher.hashBulk(2L, Hasher.longArrayHashBulk, long2D2));
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

        Assert.assertNotEquals(Hasher.astaroth.hash(bytes, 0, bytes.length-1), Hasher.astaroth_.hash(bytes2));
        Assert.assertNotEquals(Hasher.astaroth.hash(chars, 0, chars.length-1), Hasher.astaroth_.hash(chars2));
        Assert.assertNotEquals(Hasher.astaroth.hash(ints, 0, ints.length-1), Hasher.astaroth_.hash(ints2));
        Assert.assertNotEquals(Hasher.astaroth.hash(longs, 0, longs.length-1), Hasher.astaroth_.hash(longs2));
        Assert.assertNotEquals(Hasher.astaroth.hash(floats, 0, floats.length-1), Hasher.astaroth_.hash(floats2));
        Assert.assertNotEquals(Hasher.astaroth.hash(doubles, 0, doubles.length-1), Hasher.astaroth_.hash(doubles2));
        Assert.assertNotEquals(Hasher.astaroth.hash(strings, 0, strings.length-1), Hasher.astaroth_.hash(strings2));

        Assert.assertNotEquals(Hasher.astaroth.hash64(bytes, 0, bytes.length-1), Hasher.astaroth_.hash64(bytes2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(chars, 0, chars.length-1), Hasher.astaroth_.hash64(chars2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(ints, 0, ints.length-1), Hasher.astaroth_.hash64(ints2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(longs, 0, longs.length-1), Hasher.astaroth_.hash64(longs2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(floats, 0, floats.length-1), Hasher.astaroth_.hash64(floats2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(doubles, 0, doubles.length-1), Hasher.astaroth_.hash64(doubles2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(strings, 0, strings.length-1), Hasher.astaroth_.hash64(strings2));

        System.arraycopy(bytes2, 0, bytes, 1, bytes.length-1);
        System.arraycopy(chars2, 0, chars, 1, chars.length-1);
        System.arraycopy(ints2, 0, ints, 1, ints.length-1);
        System.arraycopy(longs2, 0, longs, 1, longs.length-1);
        System.arraycopy(floats2, 0, floats, 1, floats.length-1);
        System.arraycopy(doubles2, 0, doubles, 1, doubles.length-1);
        System.arraycopy(strings2, 0, strings, 1, strings.length-1);

        Assert.assertEquals(Hasher.astaroth.hash(bytes, 1, bytes.length-1), Hasher.astaroth.hash(bytes2));
        Assert.assertEquals(Hasher.astaroth.hash(chars, 1, chars.length-1), Hasher.astaroth.hash(chars2));
        Assert.assertEquals(Hasher.astaroth.hash(ints, 1, ints.length-1), Hasher.astaroth.hash(ints2));
        Assert.assertEquals(Hasher.astaroth.hash(longs, 1, longs.length-1), Hasher.astaroth.hash(longs2));
        Assert.assertEquals(Hasher.astaroth.hash(floats, 1, floats.length-1), Hasher.astaroth.hash(floats2));
        Assert.assertEquals(Hasher.astaroth.hash(doubles, 1, doubles.length-1), Hasher.astaroth.hash(doubles2));
        Assert.assertEquals(Hasher.astaroth.hash(strings, 1, strings.length-1), Hasher.astaroth.hash(strings2));

        Assert.assertEquals(Hasher.astaroth.hash64(bytes, 1, bytes.length-1), Hasher.astaroth.hash64(bytes2));
        Assert.assertEquals(Hasher.astaroth.hash64(chars, 1, chars.length-1), Hasher.astaroth.hash64(chars2));
        Assert.assertEquals(Hasher.astaroth.hash64(ints, 1, ints.length-1), Hasher.astaroth.hash64(ints2));
        Assert.assertEquals(Hasher.astaroth.hash64(longs, 1, longs.length-1), Hasher.astaroth.hash64(longs2));
        Assert.assertEquals(Hasher.astaroth.hash64(floats, 1, floats.length-1), Hasher.astaroth.hash64(floats2));
        Assert.assertEquals(Hasher.astaroth.hash64(doubles, 1, doubles.length-1), Hasher.astaroth.hash64(doubles2));
        Assert.assertEquals(Hasher.astaroth.hash64(strings, 1, strings.length-1), Hasher.astaroth.hash64(strings2));

        Assert.assertNotEquals(Hasher.astaroth.hash(bytes, 1, bytes.length-1), Hasher.astaroth_.hash(bytes2));
        Assert.assertNotEquals(Hasher.astaroth.hash(chars, 1, chars.length-1), Hasher.astaroth_.hash(chars2));
        Assert.assertNotEquals(Hasher.astaroth.hash(ints, 1, ints.length-1), Hasher.astaroth_.hash(ints2));
        Assert.assertNotEquals(Hasher.astaroth.hash(longs, 1, longs.length-1), Hasher.astaroth_.hash(longs2));
        Assert.assertNotEquals(Hasher.astaroth.hash(floats, 1, floats.length-1), Hasher.astaroth_.hash(floats2));
        Assert.assertNotEquals(Hasher.astaroth.hash(doubles, 1, doubles.length-1), Hasher.astaroth_.hash(doubles2));
        Assert.assertNotEquals(Hasher.astaroth.hash(strings, 1, strings.length-1), Hasher.astaroth_.hash(strings2));

        Assert.assertNotEquals(Hasher.astaroth.hash64(bytes, 1, bytes.length-1), Hasher.astaroth_.hash64(bytes2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(chars, 1, chars.length-1), Hasher.astaroth_.hash64(chars2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(ints, 1, ints.length-1), Hasher.astaroth_.hash64(ints2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(longs, 1, longs.length-1), Hasher.astaroth_.hash64(longs2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(floats, 1, floats.length-1), Hasher.astaroth_.hash64(floats2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(doubles, 1, doubles.length-1), Hasher.astaroth_.hash64(doubles2));
        Assert.assertNotEquals(Hasher.astaroth.hash64(strings, 1, strings.length-1), Hasher.astaroth_.hash64(strings2));
    }

    @Test
    public void testUniqueness() {
        final int targetLength = Hasher.predefined.length;
        LinkedHashSet<Integer> hashes32 = new LinkedHashSet<>(targetLength);
        for(Hasher h : Hasher.predefined) {
            hashes32.add(h.hash("What a cute kitty!"));
        }
        Assert.assertEquals(targetLength, hashes32.size());
        LinkedHashSet<Long> hashes64 = new LinkedHashSet<>(targetLength);
        for(Hasher h : Hasher.predefined) {
            hashes64.add(h.hash64("What a cute kitty!"));
        }
        Assert.assertEquals(targetLength, hashes64.size());
    }

    @Test
    public void testByteBuffer() {
        byte[] bytes = "Satchmo, my big cute baby cat".getBytes(StandardCharsets.UTF_8);
        byte[] bytes2 = "my big cute baby".getBytes(StandardCharsets.UTF_8); // start 9, length 16
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ByteBuffer buffer2 = ByteBuffer.wrap(bytes2);
        Assert.assertEquals(Hasher.asmoday.hashBulk64(buffer, 9, 16), Hasher.asmoday.hashBulk64(buffer2));
        Assert.assertNotEquals(Hasher.asmoday.hashBulk64(buffer, 9, 16), Hasher.asmoday_.hashBulk64(buffer2));
        Assert.assertEquals(Hasher.asmoday.hashBulk(buffer, 9, 16), Hasher.asmoday.hashBulk(buffer2));
        Assert.assertNotEquals(Hasher.asmoday.hashBulk(buffer, 9, 16), Hasher.asmoday_.hashBulk(buffer2));
    }

    @Test
    public void testBadSeeds() {
        long all127, all128;
        all127 = Hasher.hashBulk64(0L, new long[]{
                0x7F7F7F7F7F7F7F7FL, 0x7F7F7F7F7F7F7F7FL, 0x7F7F7F7F7F7F7F7FL, 0x7F7F7F7F7F7F7F7FL,
                0x7F7F7F7F7F7F7F7FL, 0x7F7F7F7F7F7F7F7FL, 0x7F7F7F7F7F7F7F7FL, 0x7F7F7F7F7F7F7F7FL,
        });
        all128 = Hasher.hashBulk64(0L, new long[]{
                0x0808080808080808L, 0x0808080808080808L, 0x0808080808080808L, 0x0808080808080808L,
                0x0808080808080808L, 0x0808080808080808L, 0x0808080808080808L, 0x0808080808080808L,
        });
        System.out.println("8-bit blocks  :");
        System.out.println("all127: " + Base.BASE16.unsigned(all127));
        System.out.println("all128: " + Base.BASE16.unsigned(all128));
        all127 = Hasher.hashBulk64(0L, new long[]{
                0x007F007F007F007FL, 0x007F007F007F007FL, 0x007F007F007F007FL, 0x007F007F007F007FL,
                0x007F007F007F007FL, 0x007F007F007F007FL, 0x007F007F007F007FL, 0x007F007F007F007FL,
        });
        all128 = Hasher.hashBulk64(0L, new long[]{
                0x0008000800080008L, 0x0008000800080008L, 0x0008000800080008L, 0x0008000800080008L,
                0x0008000800080008L, 0x0008000800080008L, 0x0008000800080008L, 0x0008000800080008L,
        });
        System.out.println("16-bit blocks :");
        System.out.println("all127: " + Base.BASE16.unsigned(all127));
        System.out.println("all128: " + Base.BASE16.unsigned(all128));
        all127 = Hasher.hashBulk64(0L, new long[]{
                0x0000007F0000007FL, 0x0000007F0000007FL, 0x0000007F0000007FL, 0x0000007F0000007FL,
                0x0000007F0000007FL, 0x0000007F0000007FL, 0x0000007F0000007FL, 0x0000007F0000007FL,
        });
        all128 = Hasher.hashBulk64(0L, new long[]{
                0x0000000800000008L, 0x0000000800000008L, 0x0000000800000008L, 0x0000000800000008L,
                0x0000000800000008L, 0x0000000800000008L, 0x0000000800000008L, 0x0000000800000008L,
        });
        System.out.println("32-bit blocks :");
        System.out.println("all127: " + Base.BASE16.unsigned(all127));
        System.out.println("all128: " + Base.BASE16.unsigned(all128));
        all127 = Hasher.hashBulk64(0L, new long[]{
                0x7FL, 0x7FL, 0x7FL, 0x7FL,
                0x7FL, 0x7FL, 0x7FL, 0x7FL,
        });
        all128 = Hasher.hashBulk64(0L, new long[]{
                0x08L, 0x08L, 0x08L, 0x08L,
                0x08L, 0x08L, 0x08L, 0x08L,
        });
        System.out.println("64-bit blocks :");
        System.out.println("all127: " + Base.BASE16.unsigned(all127));
        System.out.println("all128: " + Base.BASE16.unsigned(all128));
        all127 = Hasher.hashBulk64(0L, new long[]{
                0x00L, 0x7FL, 0x00L, 0x7FL,
                0x00L, 0x7FL, 0x00L, 0x7FL,
        });
        all128 = Hasher.hashBulk64(0L, new long[]{
                0x00L, 0x08L, 0x00L, 0x08L,
                0x00L, 0x08L, 0x00L, 0x08L,
        });
        System.out.println("128-bit blocks:");
        System.out.println("all127: " + Base.BASE16.unsigned(all127));
        System.out.println("all128: " + Base.BASE16.unsigned(all128));
        all127 = Hasher.hashBulk64(0L, new long[]{
                0x00L, 0x00L, 0x00L, 0x7FL,
                0x00L, 0x00L, 0x00L, 0x7FL,
        });
        all128 = Hasher.hashBulk64(0L, new long[]{
                0x00L, 0x00L, 0x00L, 0x08L,
                0x00L, 0x00L, 0x00L, 0x08L,
        });
        System.out.println("256-bit blocks:");
        System.out.println("all127: " + Base.BASE16.unsigned(all127));
        System.out.println("all128: " + Base.BASE16.unsigned(all128));
        all127 = Hasher.hashBulk64(0L, new long[]{
                0x00L, 0x00L, 0x00L, 0x00L,
                0x00L, 0x00L, 0x00L, 0x7FL,
        });
        all128 = Hasher.hashBulk64(0L, new long[]{
                0x00L, 0x00L, 0x00L, 0x00L,
                0x00L, 0x00L, 0x00L, 0x08L,
        });
        System.out.println("512-bit blocks:");
        System.out.println("all127: " + Base.BASE16.unsigned(all127));
        System.out.println("all128: " + Base.BASE16.unsigned(all128));
    }
}