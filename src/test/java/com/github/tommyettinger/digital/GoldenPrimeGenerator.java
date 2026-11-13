package com.github.tommyettinger.digital;

import java.math.BigInteger;
import java.util.Locale;

public class GoldenPrimeGenerator {
    /**
     * Generates prime numbers with a specified bit length that are always the next prime after the truncation
     * of a long from {@link MathTools#GOLDEN_LONGS}. Uses the same formatting as GOLDEN_LONGS.
     * @param args ignored
     */
    public static void main(String[] args){
        final int BITS = 21;
        int dim = 1;
        int lineCount = 0;
        for (int i = 0, di = 0; i < MathTools.GOLDEN_LONGS.length; i++) {
            long g = MathTools.GOLDEN_LONGS[i] >>> 64 - BITS;
            System.out.print("0x" + BigInteger.valueOf(g).nextProbablePrime().toString(16).toUpperCase(Locale.ROOT) + ", ");
            if(++di == dim){
                di = 0;
                dim++;
                System.out.println();
                lineCount = 0;
            } else if(++lineCount == 5){
                System.out.println();
                lineCount = 0;
            }
        }
    }
}
