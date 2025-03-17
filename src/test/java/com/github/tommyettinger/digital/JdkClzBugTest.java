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
 * {@link Integer#numberOfTrailingZeros(int)} and {@link BitConversion#countTrailingZeros(int)} are correct.
 */
public class JdkClzBugTest {
    public static void main(String[] args) {
        System.out.println("Not in array: " + Integer.numberOfLeadingZeros(0x1FFFFFF));
        System.out.println("BitConversion: " + BitConversion.countLeadingZeros(0x1FFFFFF));
        int[] out = new int[0x20000004];
        for (int i = 0; i < out.length; i++) {
//            out[i] = BitConversion.countLeadingZeros(i);
            out[i] = Integer.numberOfLeadingZeros(i);
        }
        System.out.println("Calculated in loop on array: " + out[0x1FFFFFF]);
        for (int i = 0; i < out.length; i++) {
            if(out[i] != BitConversion.countLeadingZeros(i)) System.out.printf("0x%08X\n", i);
        }
        System.out.println("Checking if any Integer.numberOfTrailingZeros() results are incorrect...");
        for (int i = 0; i < out.length; i++) {
//            out[i] = BitConversion.countLeadingZeros(i);
            out[i] = Integer.numberOfTrailingZeros(i);
        }
//        System.out.println("Calculated in loop on array: " + out[0x1FFFFFF]);
        for (int i = 0; i < out.length; i++) {
            if(out[i] != BitConversion.countTrailingZeros(i|0xFFFFFFFF00000000L)) System.out.printf("0x%08X\n", i);
        }
    }
}
