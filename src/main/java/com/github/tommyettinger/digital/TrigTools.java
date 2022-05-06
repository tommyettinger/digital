/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.github.tommyettinger.digital;

public class TrigTools {
    static public final float FLOAT_ROUNDING_ERROR = 0.000001f; // 32 bits
    static public final float PI = (float)Math.PI;
    static public final float PI_INVERSE = (float)(1.0/Math.PI);
    static public final float PI2 = PI * 2;
    static public final float HALF_PI = PI / 2;

    static public final float E = (float)Math.E;

    static private final int SIN_BITS = 14; // 16KB. Adjust for accuracy.
    static private final int SIN_COUNT = (1 << SIN_BITS);
    static private final int QUARTER_ROTATION = SIN_COUNT >>> 2;
    static private final int SIN_MASK = SIN_COUNT - 1;


    static private final float radFull = PI2;
    static private final float degFull = 360;
    static private final float turnFull = 1;

    static private final float radToIndex = SIN_COUNT / radFull;
    static private final float degToIndex = SIN_COUNT / degFull;
    static private final float turnToIndex = SIN_COUNT;

    /**
     * Multiply by this to convert from radians to degrees.
     */
    public static final float radiansToDegrees = 180f / PI;
    /**
     * Multiply by this to convert from degrees to radians.
     */
    public static final float degreesToRadians = PI / 180;
    public static final float[] sinTable = new float[SIN_COUNT];

    static {
        for (int i = 0; i < SIN_COUNT; i++)
            sinTable[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        sinTable[0] = 0f;
        sinTable[(int) (90 * degToIndex) & SIN_MASK] = 1f;
        sinTable[(int) (180 * degToIndex) & SIN_MASK] = 0f;
        sinTable[(int) (270 * degToIndex) & SIN_MASK] = -1f;
    }

    /** Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive). */
    static public float sin (float radians) {
        return sinTable[(int)(radians * radToIndex) & SIN_MASK];
    }

    /** Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive). */
    static public float cos (float radians) {
        return sinTable[(int)((radians + HALF_PI) * radToIndex) & SIN_MASK];
    }

    /** Returns the tangent in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive). */
    static public float tan (float radians) {
        final int idx = (int)(radians * radToIndex) & SIN_MASK;
        return sinTable[idx] / sinTable[idx + QUARTER_ROTATION & SIN_MASK];
    }

    /** Returns the sine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive). */
    static public float sinDeg (float degrees) {
        return sinTable[(int)(degrees * degToIndex) & SIN_MASK];
    }

    /** Returns the cosine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive). */
    static public float cosDeg (float degrees) {
        return sinTable[(int)((degrees + 90) * degToIndex) & SIN_MASK];
    }

    /** Returns the tangent in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive). */
    static public float tanDeg (float degrees) {
        final int idx = (int)(degrees * degToIndex) & SIN_MASK;
        return sinTable[idx] / sinTable[idx + QUARTER_ROTATION & SIN_MASK];
    }

    /** Returns the sine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive). */
    static public float sinTurns (float turns) {
        return sinTable[(int)(turns * turnToIndex) & SIN_MASK];
    }

    /** Returns the cosine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive). */
    static public float cosTurns (float turns) {
        return sinTable[(int)((turns + 0.25f) * turnToIndex) & SIN_MASK];
    }

    /** Returns the tangent in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive). */
    static public float tanTurns (float turns) {
        final int idx = (int)(turns * turnToIndex) & SIN_MASK;
        return sinTable[idx] / sinTable[idx + QUARTER_ROTATION & SIN_MASK];
    }

    // ---

    /** A variant on {@link #atan(float)} that does not tolerate infinite inputs for speed reasons. This can be given a double
     * parameter, but is otherwise the same as atan(float), and returns a float like that method. It uses the same approximation,
     * from sheet 11 of "Approximations for Digital Computers." This is mostly meant to be used inside
     * {@link #atan2(float, float)}, but it may be a tiny bit faster than atan(float) in other code.
     * @param i any finite double or float, but more commonly a float
     * @return an output from the inverse tangent function, from {@code -HALF_PI} to {@code HALF_PI} inclusive */
    public static float atanUnchecked (double i) {
        // We use double precision internally, because some constants need double precision.
        double n = Math.abs(i);
        // c uses the "equally-good" formulation that permits n to be from 0 to almost infinity.
        double c = (n - 1.0) / (n + 1.0);
        // The approximation needs 6 odd powers of c.
        double c2 = c * c;
        double c3 = c * c2;
        double c5 = c3 * c2;
        double c7 = c5 * c2;
        double c9 = c7 * c2;
        double c11 = c9 * c2;
        return (float)Math.copySign((Math.PI * 0.25)
                + (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11), i);
    }

    /** Close approximation of the frequently-used trigonometric method atan2, with higher precision than libGDX's atan2
     * approximation. Average error is 1.057E-6 radians; maximum error is 1.922E-6. Takes y and x (in that unusual order) as
     * floats, and returns the angle from the origin to that point in radians. It is about 4 times faster than
     * {@link Math#atan2(double, double)} (roughly 15 ns instead of roughly 60 ns for Math, on Java 8 HotSpot). <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
     * translates to atan2().
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a float; ranges from {@code -PI} to {@code PI} */
    public static float atan2 (final float y, float x) {
        float n = y / x;
        if (n != n)
            n = (y == x ? 1f : -1f); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return atanUnchecked(n);
        else if (x < 0) {
            if (y >= 0) return atanUnchecked(n) + PI;
            return atanUnchecked(n) - PI;
        } else if (y > 0)
            return x + HALF_PI;
        else if (y < 0) return x - HALF_PI;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /** Returns acos in radians; less accurate than Math.acos but may be faster. Average error of 0.00002845 radians (0.0016300649
     * degrees), largest error of 0.000067548 radians (0.0038702153 degrees). This implementation does not return NaN if given an
     * out-of-range input (Math.acos does return NaN), unless the input is NaN.
     * @param a acos is defined only when a is between -1f and 1f, inclusive
     * @return between {@code 0} and {@code PI} when a is in the defined range */
    static public float acos (float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return (float)Math.sqrt(1f - a) * (1.5707288f - 0.2121144f * a + 0.0742610f * a2 - 0.0187293f * a3);
        }
        return 3.14159265358979323846f
                - (float)Math.sqrt(1f + a) * (1.5707288f + 0.2121144f * a + 0.0742610f * a2 + 0.0187293f * a3);
    }

    /** Returns asin in radians; less accurate than Math.asin but may be faster. Average error of 0.000028447 radians (0.0016298931
     * degrees), largest error of 0.000067592 radians (0.0038727364 degrees). This implementation does not return NaN if given an
     * out-of-range input (Math.asin does return NaN), unless the input is NaN.
     * @param a asin is defined only when a is between -1f and 1f, inclusive
     * @return between {@code -HALF_PI} and {@code HALF_PI} when a is in the defined range */
    static public float asin (float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return 1.5707963267948966f
                    - (float)Math.sqrt(1f - a) * (1.5707288f - 0.2121144f * a + 0.0742610f * a2 - 0.0187293f * a3);
        }
        return -1.5707963267948966f + (float)Math.sqrt(1f + a) * (1.5707288f + 0.2121144f * a + 0.0742610f * a2 + 0.0187293f * a3);
    }

    /** Arc tangent approximation with very low error, using an algorithm from the 1955 research study "Approximations for Digital
     * Computers," by RAND Corporation (this is sheet 11's algorithm, which is the fourth-fastest and fourth-least precise). This
     * method is usually about 4x faster than {@link Math#atan(double)}, but is somewhat less precise than Math's implementation.
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUnchecked(double)}, but this method will be correct
     * enough for infinite inputs, and atanUnchecked() will not be.
     * @param i an input to the inverse tangent function; any float is accepted
     * @return an output from the inverse tangent function, from {@code -HALF_PI} to {@code HALF_PI} inclusive
     * @see #atanUnchecked(double) If you know the input will be finite, you can use atanUnchecked() instead. */
    public static float atan (float i) {
        // We use double precision internally, because some constants need double precision.
        // This clips infinite inputs at Double.MAX_VALUE, which still probably becomes infinite
        // again when converted back to float.
        double n = Math.min(Math.abs(i), Double.MAX_VALUE);
        // c uses the "equally-good" formulation that permits n to be from 0 to almost infinity.
        double c = (n - 1.0) / (n + 1.0);
        // The approximation needs 6 odd powers of c.
        double c2 = c * c;
        double c3 = c * c2;
        double c5 = c3 * c2;
        double c7 = c5 * c2;
        double c9 = c7 * c2;
        double c11 = c9 * c2;
        return (float)Math.copySign((Math.PI * 0.25)
                + (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11), i);
    }

}
