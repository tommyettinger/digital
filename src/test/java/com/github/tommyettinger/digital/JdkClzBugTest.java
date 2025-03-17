package com.github.tommyettinger.digital;

/**
 * Checking to see how <a href="https://bugs.openjdk.org/browse/JDK-8349637">this bug</a> can be reproduced.
 * <br>
 * <pre>
 * With JDK17:
 * Not in array: 7
 * BitConversion: 7
 * Calculated in loop on array: 7
 * With JDK21:
 * Not in array: 7
 * BitConversion: 7
 * Calculated in loop on array: 6
 * (Also affects loop and array when using BitConversion...)
 * With JDK23:
 * Not in array: 7
 * BitConversion: 7
 * Calculated in loop on array: 6
 * With JDK22:
 * Not in array: 7
 * BitConversion: 7
 * Calculated in loop on array: 6
 * </pre>
 * BUT:
 * <pre>
 * With Graal GDK22:
 * Not in array: 7
 * BitConversion: 7
 * Calculated in loop on array: 7
 * </pre>
 */
public class JdkClzBugTest {
    public static void main(String[] args) {
        System.out.println("Not in array: " + Integer.numberOfLeadingZeros(0x1FFFFFF));
        System.out.println("BitConversion: " + BitConversion.countLeadingZeros(0x1FFFFFF));
        int[] out = new int[0x2000004];
        for (int i = 0; i < out.length; i++)
//            out[i] = BitConversion.countLeadingZeros(i);
            out[i] = Integer.numberOfLeadingZeros(i);
        System.out.println("Calculated in loop on array: " + out[0x1FFFFFF]);
    }
}
