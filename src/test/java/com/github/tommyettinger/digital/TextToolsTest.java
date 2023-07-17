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
//        for(String s : splat) System.out.println("!!!" + s + "!!!");
        Assert.assertEquals(4, splat.length);
    }
}
