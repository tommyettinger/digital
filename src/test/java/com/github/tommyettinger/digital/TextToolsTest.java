package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Test;

public class TextToolsTest {
    @Test
    public void testSplit() {
        String csv;
        String[] splat;
        csv = "comma,separated,values,are,my,friend";
        splat = TextTools.split(csv, ",");
        Assert.assertEquals(6, splat.length);
        csv = "comma, separated, values, are, my, friend";
        splat = TextTools.split(csv, ", ");
        Assert.assertEquals(6, splat.length);
        splat = TextTools.split(csv, ", ", 9, csv.length() - 7);
//        for(String s : splat) System.out.println(">>>" + s + "<<<");
        Assert.assertEquals(4, splat.length);
        boolean[] booleans = {true, true, false, false, false, true, true, false, true, true, true, true, false, true};
//        String bd = "10001101";
        String bd = TextTools.joinDense(booleans, 1, 8);
        boolean[] full = TextTools.booleanSplitDense(bd);
        Assert.assertTrue(full[0]);
        Assert.assertFalse(full[1]);
        Assert.assertFalse(full[2]);
        Assert.assertFalse(full[3]);
        Assert.assertTrue(full[4]);
        Assert.assertTrue(full[5]);
        Assert.assertFalse(full[6]);
        Assert.assertTrue(full[7]);
        full = TextTools.booleanSplitDense(bd, '1', 4, 7);
        Assert.assertTrue(full[0]);
        Assert.assertTrue(full[1]);
        Assert.assertFalse(full[2]);

    }
}
