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
        String fmt = "Sensor readings: %.4f%% oxygen, %.3f%% helium. Probability of colonization: %e";
        LinkedHashMap<Float, Float> pairs = new LinkedHashMap<>();
        pairs.put(10.3618f, 0.014f);
        pairs.put(5.4f, 2.2f);
        pairs.put(0.0f, 70.9830f);
        pairs.put(0.017f, 0.0640f);
        for(Map.Entry<Float, Float> ent : pairs.entrySet()) {
//            Assert.assertEquals(String.format(fmt, ent.getKey(), ent.getValue()),
//                    Stringf.format(fmt, ent.getKey(), ent.getValue()));
            double rand = (float)Math.random() * 0x1p-10f;
            System.out.println("STRING  : " + String.format(fmt, ent.getKey(), ent.getValue(), rand));
            System.out.println("STRINGF : " + Stringf.format(fmt, ent.getKey(), ent.getValue(), rand));
        }
    }

    @Test
    public void testDoubleConversion() {
        String fmt = "Sensor readings: %.4f%% oxygen, %.3f%% helium. Probability of colonization: %e";
        LinkedHashMap<Double, Double> pairs = new LinkedHashMap<>();
        pairs.put(10.3618, 0.014);
        pairs.put(5.4, 2.2);
        pairs.put(0.0, 70.9830);
        pairs.put(0.017, 0.0640);
        for(Map.Entry<Double, Double> ent : pairs.entrySet()) {
//            Assert.assertEquals(String.format(fmt, ent.getKey(), ent.getValue()),
//                    Stringf.format(fmt, ent.getKey(), ent.getValue()));
            double rand = Math.random() * 0.001;
            System.out.println("STRING  : " + String.format(fmt, ent.getKey(), ent.getValue(), rand));
            System.out.println("STRINGF : " + Stringf.format(fmt, ent.getKey(), ent.getValue(), rand));
        }
    }
}
