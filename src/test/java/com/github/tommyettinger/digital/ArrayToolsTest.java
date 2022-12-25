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
    }
}
