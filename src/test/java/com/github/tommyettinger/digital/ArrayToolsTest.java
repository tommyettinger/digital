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
}
