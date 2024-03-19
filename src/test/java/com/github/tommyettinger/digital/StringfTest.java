package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringfTest {
    @Test
    public void testStringConversion() {
        String fmt = "Hello, %s! How's your %s?";
        LinkedHashMap<String, String> pairs = new LinkedHashMap<>();
        pairs.put("Jim", "cat");
        pairs.put("Beth", "dog");
        pairs.put("Mike", "back");
        for(Map.Entry<String, String> ent : pairs.entrySet()) {
            Assert.assertEquals(String.format(fmt, ent.getKey(), ent.getValue()),
                    Stringf.format(fmt, ent.getKey(), ent.getValue()));
        }
    }

    @Test
    public void testIntConversion() {
        String fmt = "UNIT #%08X, HAVE YOU RECOVERED ITEM %d?";
        LinkedHashMap<Integer, Integer> pairs = new LinkedHashMap<>();
        pairs.put(0x12345678, 1);
        pairs.put(0x1234, 2);
        pairs.put(0, -3);
        for(Map.Entry<Integer, Integer> ent : pairs.entrySet()) {
            Assert.assertEquals(String.format(fmt, ent.getKey(), ent.getValue()),
                    Stringf.format(fmt, ent.getKey(), ent.getValue()));
        }
    }

    @Test
    public void testFloatConversion() {
        String fmt = "Sensor readings: %.2f%% oxygen, %.4f%% helium.";
        LinkedHashMap<Double, Double> pairs = new LinkedHashMap<>();
        pairs.put(10.3618, 0.0014);
        pairs.put(5.4, 2.3);
        pairs.put(0.0, 70.9830);
        for(Map.Entry<Double, Double> ent : pairs.entrySet()) {
//            Assert.assertEquals(String.format(fmt, ent.getKey(), ent.getValue()),
//                    Stringf.format(fmt, ent.getKey(), ent.getValue()));
            System.out.println("STRING  : " + String.format(fmt, ent.getKey(), ent.getValue()));
            System.out.println("STRINGF : " + Stringf.format(fmt, ent.getKey(), ent.getValue()));
        }
    }
}
