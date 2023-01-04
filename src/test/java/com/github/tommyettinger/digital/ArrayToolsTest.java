package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ArrayToolsTest {
    @Test
    public void testCharSpan() {
        char[] span;
        span = ArrayTools.charSpan(-1);
        Assert.assertEquals(0, span.length);
        span = ArrayTools.charSpan(0);
        Assert.assertEquals(1, span.length);
        span = ArrayTools.charSpan(1);
        Assert.assertEquals(2, span.length);
        span = ArrayTools.charSpan(2);
        Assert.assertEquals(3, span.length);
        span = ArrayTools.charSpan(0x10000);
        Assert.assertEquals(0x10001, span.length);

        span = ArrayTools.charSpan(0xFFFF);
        Assert.assertEquals(0x10000, span.length);

        Arrays.fill(span, 'a');
        span = ArrayTools.charSpan(span);
        Assert.assertEquals('\uFFFF', span[0xFFFF]);
        Assert.assertEquals('\u0000', span[0]);
    }

    @Test
    public void testCharSection() {
//        char[][] letters = new char[][] {
//                ArrayTools.letterSpan(0, 16),
//                ArrayTools.letterSpan(16, 16),
//                ArrayTools.letterSpan(32, 16),
//                ArrayTools.letterSpan(48, 16),
//                ArrayTools.letterSpan(64, 16),
//                ArrayTools.letterSpan(80, 16),
//                ArrayTools.letterSpan(96, 16),
//                ArrayTools.letterSpan(112, 16),
//                ArrayTools.letterSpan(128, 16),
//        };
        char[][] letters = new char[9][16];
        ArrayTools.sequentialFill(letters, ArrayTools.letterSpan(144));
        for (int i = 0; i < letters.length; i++) {
            System.out.println(letters[i]);
        }
        System.out.println();
        char[][] sub = ArrayTools.section(letters, 4, 5, 8, 6);
        for (int i = 0; i < sub.length; i++) {
            System.out.println(sub[i]);
        }
        System.out.println();

        sub = ArrayTools.section(letters, 5, 10, 6, 8);
        for (int i = 0; i < sub.length; i++) {
            System.out.println(sub[i]);
        }
        System.out.println();
    }


    @Test
    public void testStringSection() {
        String[][] words = new String[9][16];
        ArrayTools.sequentialFill(words, ArrayTools.stringSpan(48 + 144, 144));
        for (int i = 0; i < words.length; i++) {
            System.out.println(String.join(", ", words[i]));
        }
        System.out.println();
        String[][] sub = ArrayTools.section(words, 4, 5, 8, 6);
        for (int i = 0; i < sub.length; i++) {
            System.out.println(String.join(", ", sub[i]));
        }
        System.out.println();

        sub = ArrayTools.section(words, 5, 10, 6, 8);
        for (int i = 0; i < sub.length; i++) {
            System.out.println(String.join(", ", sub[i]));
        }
        System.out.println();
    }

    @Test
    public void testCharSequentialFill() {
        char[] contents = ArrayTools.letterSpan(100);
        char[][] large = new char[12][11], small = new char[6][6];
        ArrayTools.sequentialFill(large, contents);
        Assert.assertEquals(contents[0], large[0][0]);
        Assert.assertEquals(contents[1], large[0][1]);
        Assert.assertEquals(contents[2], large[0][2]);
        Assert.assertEquals(contents[11], large[1][0]);
        Assert.assertEquals(contents[12], large[1][1]);
        Assert.assertEquals(contents[13], large[1][2]);
        Assert.assertEquals(contents[0], large[9][1]);
        ArrayTools.sequentialFill(small, contents);
        Assert.assertEquals(contents[0], small[0][0]);
        Assert.assertEquals(contents[1], small[0][1]);
        Assert.assertEquals(contents[2], small[0][2]);
        Assert.assertEquals(contents[6], small[1][0]);
        Assert.assertEquals(contents[7], small[1][1]);
        Assert.assertEquals(contents[8], small[1][2]);
    }

    @Test
    public void testObjectSequentialFill() {
        String[] contents = ArrayTools.stringSpan(100);
        String[][] large = new String[12][11], small = new String[6][6];
        ArrayTools.sequentialFill(large, contents);
        Assert.assertEquals(contents[0], large[0][0]);
        Assert.assertEquals(contents[1], large[0][1]);
        Assert.assertEquals(contents[2], large[0][2]);
        Assert.assertEquals(contents[11], large[1][0]);
        Assert.assertEquals(contents[12], large[1][1]);
        Assert.assertEquals(contents[13], large[1][2]);
        Assert.assertEquals(contents[0], large[9][1]);
        ArrayTools.sequentialFill(small, contents);
        Assert.assertEquals(contents[0], small[0][0]);
        Assert.assertEquals(contents[1], small[0][1]);
        Assert.assertEquals(contents[2], small[0][2]);
        Assert.assertEquals(contents[6], small[1][0]);
        Assert.assertEquals(contents[7], small[1][1]);
        Assert.assertEquals(contents[8], small[1][2]);
    }

    public static void main(String[] args) {
        char[][] letters = new char[][] {
                ArrayTools.letterSpan(0, 16),
                ArrayTools.letterSpan(16, 16),
                ArrayTools.letterSpan(32, 16),
                ArrayTools.letterSpan(48, 16),
                ArrayTools.letterSpan(64, 16),
                ArrayTools.letterSpan(80, 16),
                ArrayTools.letterSpan(96, 16),
                ArrayTools.letterSpan(112, 16),
                ArrayTools.letterSpan(128, 16),
        };
        for (int i = 0; i < letters.length; i++) {
            System.out.println(letters[i]);
        }
        System.out.println();
        ArrayTools.shuffle2D(letters);
        for (int i = 0; i < letters.length; i++) {
            System.out.println(letters[i]);
        }
        // This is just some quick code for testing where in the GOLDEN_LONGS array a particular constant is.
//        for (int i = 0; i < 128; i++) {
//            if(MathTools.GOLDEN_LONGS[i] == 0xAB273EB15C029011L)
//            {
//                System.out.printf("%d: %X", i, i);
//                return;
//            }
//        }
    }
}
