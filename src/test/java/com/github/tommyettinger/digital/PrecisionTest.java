package com.github.tommyettinger.digital;

import org.junit.Test;

public class PrecisionTest {
    @Test
    public void testAtan2(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double err = TrigTools.atan2(y, x) - Math.atan2(y, x),
                        ae = Math.abs(err);
                relError += err;
                absError += ae;
                if(maxError != (maxError = Math.max(maxError, ae))){
                    worstX = x;
                    worstY = y;
                }
                counter++;
            }
        }
        System.out.printf("Absolute error:   %3.8f\n" +
                          "Relative error:   %3.8f\n" +
                          "Maximum error:    %3.8f\n" +
                          "Worst position:   %3.8f,%3.8f\n", absError / counter, relError / counter, maxError, worstX, worstY);
    }
    @Test
    public void testAtan2Deg(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double err = TrigTools.atan2Deg(y, x) - Math.toDegrees(Math.atan2(y, x)),
                        ae = Math.abs(err);
                relError += err;
                absError += ae;
                if(maxError != (maxError = Math.max(maxError, ae))){
                    worstX = x;
                    worstY = y;
                }
                counter++;
            }
        }
        System.out.printf("Absolute error:   %3.8f\n" +
                          "Relative error:   %3.8f\n" +
                          "Maximum error:    %3.8f\n" +
                          "Worst position:   %3.8f,%3.8f\n", absError / counter, relError / counter, maxError, worstX, worstY);
    }
    @Test
    public void testAtan2Turns(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double m = (Math.atan2(y, x) / 2.0 / Math.PI);
                if(m < 0.0) m += 1.0;
                double err = TrigTools.atan2Turns(y, x) - m,
                        ae = Math.abs(err);
                relError += err;
                absError += ae;
                if(maxError != (maxError = Math.max(maxError, ae))){
                    worstX = x;
                    worstY = y;
                }
                counter++;
            }
        }
        System.out.printf("Absolute error:   %3.8f\n" +
                          "Relative error:   %3.8f\n" +
                          "Maximum error:    %3.8f\n" +
                          "Worst position:   %3.8f,%3.8f\n", absError / counter, relError / counter, maxError, worstX, worstY);
    }
    @Test
    public void testAtan2Deg360(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double m = Math.toDegrees(Math.atan2(y, x));
                if(m < 0.0) m += 360.0;
                double err = TrigTools.atan2Deg360(y, x) - m,
                        ae = Math.abs(err);
                relError += err;
                absError += ae;
                if(maxError != (maxError = Math.max(maxError, ae))){
                    worstX = x;
                    worstY = y;
                }
                counter++;
            }
        }
        System.out.printf("Absolute error:   %3.8f\n" +
                          "Relative error:   %3.8f\n" +
                          "Maximum error:    %3.8f\n" +
                          "Worst position:   %3.8f,%3.8f\n", absError / counter, relError / counter, maxError, worstX, worstY);
    }
    public static long fibonacciRound(int n) {
        return (long) ((Math.pow(1.618033988749895, n)) / 2.236067977499795 + 0.49999999999999917); // used 2.236067977499794 before
//        return (long) ((Math.pow(MathTools.PHI_D, n) - Math.pow(MathTools.PSI_D, n)) / MathTools.ROOT5_D);
    }
    public static double fibonacciBase(long n) {
        return Math.pow(MathTools.PHI_D, n) / MathTools.ROOT5_D;
    }

    @Test
    public void fibonacciTest() {
        {
            int idx = 2;
            int old = MathTools.fibonacci(1), ancient = MathTools.fibonacci(0), t;
            while (old + ancient == (t = MathTools.fibonacci(idx))) {
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Int failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
        {
            long idx = 2;
            long old = MathTools.fibonacci(1L), ancient = MathTools.fibonacci(0L), t;
            while (old + ancient == (t = MathTools.fibonacci(idx))) {
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Long failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
        {
            int idx = 2;
            long old = MathTools.fibonacci(1L), ancient = MathTools.fibonacci(0L), t;
            while (old + ancient == (t = fibonacciRound(idx))) {
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Round failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
        {
            long idx = 2;
            long old = MathTools.fibonacci(1L), ancient = MathTools.fibonacci(0L), t;
            while (old + ancient == (t = MathTools.fibonacci(idx))) {
                System.out.printf("At index %d, Floored %d , Unfloored %20.20f\n", idx, t, fibonacciBase(idx));
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Long failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
    }
    public static long fibonacciFuzz(int n, double phi, double root) {
        return (long) ((Math.pow(phi, n)) / root + 0.5);
    }

    @Test
    public void testFibonacciFuzz() {
        double phi = MathTools.PHI_D, root = 2.236067977499794, bestPhi = phi, bestRoot = root;
        int furthest = 0;
        System.out.printf("Starting with phi = %22.20f, root = %22.20f\n", bestPhi, bestRoot);
        long seed = Hasher.randomize3(System.nanoTime()); // this can use a fixed seed as well.
        System.out.println("Using seed " + seed);
        for (int i = 0; i < 1000; i++) {
            int idx = 2;
            long old = 1L, ancient = 0L, t;
            while (old + ancient == (t = fibonacciFuzz(idx, phi, root))) {
                idx++;
                ancient = old;
                old = t;
            }
            if(furthest < (furthest = Math.max(idx, furthest))){
                bestPhi = phi;
                bestRoot = root;
            }
            long r = Hasher.randomize1(seed++);
            if(r < 0x1000000000000000L){
                if(t > old + ancient) {
                    root = Math.nextUp(root);
//                    System.out.println("Root up: " + root);
                }
                else {
                    root = Math.nextDown(root);
//                    System.out.println("Root down: " + root);
                }
            } else {
                if(t > old + ancient) {
                    phi = Math.nextDown(phi);
//                    System.out.println("Phi down: " + phi);
                }
                else
                {
                    phi = Math.nextUp(phi);
//                    System.out.println("Phi up: " + phi);
                }
            }
            // an attempt to break out of locally optimal, globally sub-optimal values.
            if((r & 127L) == 0L){
                if((r & 128L) == 0)
                    root = Math.nextUp(Math.nextUp(root));
                else
                    root = Math.nextDown(Math.nextDown(root));
            }
        }
        System.out.printf("Got up to index %d with phi = %22.20f, root = %22.20f\n", furthest, bestPhi, bestRoot);
        //Got up to index 77 with phi = 1.61803398874989500000, root = 2.23606797749979500000
    }
    public static long fibonacciFuzz2(int n, double phi, double half, double root) {
        return (long) ((Math.pow(phi, n)) / root + half);
    }

    @Test
    public void testFibonacciFuzz2() {
        double phi = MathTools.PHI_D, half = 0.5, root = 2.236067977499794, bestPhi = phi, bestHalf = half, bestRoot = root;
        int furthest = 0;
        System.out.printf("Starting with phi = %22.20f, half = %22.20f, root = %22.20f\n", bestPhi, bestHalf, bestRoot);
        long seed = Hasher.randomize3(System.nanoTime()); // this can use a fixed seed as well.
        System.out.println("Using seed " + seed);
        for (int i = 0; i < 1000000; i++) {
            int idx = 2;
            long old = 1L, ancient = 0L, t;
            while (old + ancient == (t = fibonacciFuzz2(idx, phi, half, root))) {
                idx++;
                ancient = old;
                old = t;
            }
            if(furthest < (furthest = Math.max(idx, furthest))){
                bestPhi = phi;
                bestHalf = half;
                bestRoot = root;
            }
            long r = Hasher.randomize1(seed++);
//            if((r & 512) == 0) {
//                if ((r & 256) == 0)
//                    phi = Math.nextUp(phi);
//                else
//                    phi = Math.nextDown(phi);
//            }
            if(r > 0x3000000000000000L){
                if(t > old + ancient) {
                    root = Math.nextUp(root);
//                    System.out.println("Root up: " + root);
                }
                else {
                    root = Math.nextDown(root);
//                    System.out.println("Root down: " + root);
                }
            } else {
                if(t > old + ancient) {
                    half = Math.nextDown(half);
//                    System.out.println("Phi down: " + phi);
                }
                else
                {
                    half = Math.nextUp(half);
//                    System.out.println("Phi up: " + phi);
                }
            }
            // an attempt to break out of locally optimal, globally sub-optimal values.
            if((r & 127L) == 0L){
                if((r & 128L) == 0)
                    root = Math.nextUp(Math.nextUp(root));
                else
                    root = Math.nextDown(Math.nextDown(root));
            }
        }
        System.out.printf("Got up to index %d with phi = %22.20f, half = %22.20f, root = %22.20f\n",
                furthest, bestPhi, bestHalf, bestRoot);
        // Got up to index 78 with half = 0.49999999999999920000, root = 2.23606797749979500000
    }
}
