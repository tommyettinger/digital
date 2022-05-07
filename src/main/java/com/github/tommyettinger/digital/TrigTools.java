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

/**
 * Various trigonometric approximations, using a lookup table for sin(), cos(), and tan(), and Taylor series for their
 * inverses. Yes, I know Taylor series are not optimal over large input ranges. But, asin() and acos() do not have large
 * input ranges permissible. This also has an atan2() approximation defined with output in radians.
 */
public class TrigTools {
    public static final float FLOAT_ROUNDING_ERROR = 0.000001f; // 32 bits
    /**
     * Everybody's favorite circle-related irrational number, as a float; the same as casting {@link Math#PI} to float.
     */
    public static final float PI = (float) Math.PI;
    /**
     * 1.0f divided by {@link #PI}.
     */
    public static final float PI_INVERSE = (float) (1.0 / Math.PI);
    /**
     * 2f times {@link #PI}; the same as {@link #TAU}.
     */
    public static final float PI2 = PI * 2f;
    /**
     * 2f times {@link #PI}; the same as {@link #PI2}.
     */
    public static final float TAU = PI2;
    /**
     * {@link #PI} divided by 2f.
     */
    public static final float HALF_PI = PI * 0.5f;
    /**
     * {@link #PI} divided by 4f.
     */
    public static final float QUARTER_PI = PI * 0.25f;

    private static final double QUARTER_PI_D = Math.PI * 0.25;

    /**
     * This is the same as {@link Math#E} cast to a float; that is, it is closest to the base of natural logarithms.
     */
    public static final float E = (float) Math.E;

    private static final int SIN_BITS = 14; // 64KB. Adjust for accuracy.
    /**
     * The size of {@link #SIN_TABLE}, available separately from the table's length for convenience.
     */
    public static final int TABLE_SIZE = (1 << SIN_BITS);
    /**
     * The bitmask that can be used to confine any int to wrap within {@link #TABLE_SIZE}. Any accesses to
     * {@link #SIN_TABLE} with an index that could be out of bounds should probably be wrapped using this, as with
     * {@code SIN_TABLE[index & TABLE_MASK]}.
     */
    public static final int TABLE_MASK = TABLE_SIZE - 1;
    private static final int QUARTER_ROTATION = TABLE_SIZE >>> 2;


    private static final float radFull = PI2;
    private static final float degFull = 360;
    private static final float turnFull = 1;

    private static final float radToIndex = TABLE_SIZE / radFull;
    private static final float degToIndex = TABLE_SIZE / degFull;
    private static final float turnToIndex = TABLE_SIZE;

    /**
     * Multiply by this to convert from radians to degrees.
     */
    public static final float radiansToDegrees = 180f / PI;
    /**
     * Multiply by this to convert from degrees to radians.
     */
    public static final float degreesToRadians = PI / 180;
    /**
     * A precalculated table of 16384 floats, corresponding to the y-value of points on the unit circle, ordered by
     * increasing angle. This should not be mutated, but it can be accessed directly for things like getting random
     * unit vectors, or implementing the "sincos" method (which assigns sin() to one item and cos() to another).
     * <br>
     * A quick way to get a random unit vector is to get a random 14-bit number, as with
     * {@code int angle = random.nextInt() >>> 18;}, look up angle in this table to get y, then look up
     * {@code (angle + 4096) & 16383} to get x.
     */
    public static final float[] SIN_TABLE = new float[TABLE_SIZE];

    static {
        for (int i = 0; i < TABLE_SIZE; i++)
            SIN_TABLE[i] = (float) Math.sin((i + 0.5f) / TABLE_SIZE * radFull);
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE[0] = 0f;
        SIN_TABLE[(int) (90 * degToIndex) & TABLE_MASK] = 1f;
        SIN_TABLE[(int) (180 * degToIndex) & TABLE_MASK] = 0f;
        SIN_TABLE[(int) (270 * degToIndex) & TABLE_MASK] = -1f;
    }

    /**
     * Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).
     */
    public static float sin(float radians) {
        return SIN_TABLE[(int) (radians * radToIndex) & TABLE_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).
     */
    public static float cos(float radians) {
        return SIN_TABLE[(int) ((radians + HALF_PI) * radToIndex) & TABLE_MASK];
    }

    /**
     * Returns the tangent in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).
     */
    public static float tan(float radians) {
        final int idx = (int) (radians * radToIndex) & TABLE_MASK;
        return SIN_TABLE[idx] / SIN_TABLE[idx + QUARTER_ROTATION & TABLE_MASK];
    }

    /**
     * Returns the sine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive).
     */
    public static float sinDeg(float degrees) {
        return SIN_TABLE[(int) (degrees * degToIndex) & TABLE_MASK];
    }

    /**
     * Returns the cosine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive).
     */
    public static float cosDeg(float degrees) {
        return SIN_TABLE[(int) ((degrees + 90) * degToIndex) & TABLE_MASK];
    }

    /**
     * Returns the tangent in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive).
     */
    public static float tanDeg(float degrees) {
        final int idx = (int) (degrees * degToIndex) & TABLE_MASK;
        return SIN_TABLE[idx] / SIN_TABLE[idx + QUARTER_ROTATION & TABLE_MASK];
    }

    /**
     * Returns the sine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive).
     */
    public static float sinTurns(float turns) {
        return SIN_TABLE[(int) (turns * turnToIndex) & TABLE_MASK];
    }

    /**
     * Returns the cosine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive).
     */
    public static float cosTurns(float turns) {
        return SIN_TABLE[(int) ((turns + 0.25f) * turnToIndex) & TABLE_MASK];
    }

    /**
     * Returns the tangent in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive).
     */
    public static float tanTurns(float turns) {
        final int idx = (int) (turns * turnToIndex) & TABLE_MASK;
        return SIN_TABLE[idx] / SIN_TABLE[idx + QUARTER_ROTATION & TABLE_MASK];
    }

    // ---

    /**
     * A variant on {@link #atan(float)} that does not tolerate infinite inputs for speed reasons. This can be given a double
     * parameter, but is otherwise the same as atan(float), and returns a float like that method. It uses the same approximation,
     * from sheet 11 of "Approximations for Digital Computers." This is mostly meant to be used inside
     * {@link #atan2(float, float)}, but it may be a tiny bit faster than atan(float) in other code.
     *
     * @param i any finite double or float, but more commonly a float
     * @return an output from the inverse tangent function, from {@code -HALF_PI} to {@code HALF_PI} inclusive
     */
    public static float atanUnchecked(double i) {
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
        return (float) (Math.signum(i) * (QUARTER_PI_D
                + (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11)));
    }

    /**
     * A variant on {@link #atanDeg(float)} that does not tolerate infinite inputs for speed reasons. This can be given a double
     * parameter, but is otherwise the same as atanDeg(float), and returns a float like that method. It uses the same approximation,
     * from sheet 11 of "Approximations for Digital Computers." This is mostly meant to be used inside
     * {@link #atan2(float, float)}, but it may be a tiny bit faster than atanDeg(float) in other code.
     *
     * @param i any finite double or float, but more commonly a float
     * @return an output from the inverse tangent function in degrees, from {@code -90} to {@code 90} inclusive
     */
    public static float atanUncheckedDeg(double i) {
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
        return (float) (Math.signum(i) * (45.0
                + (57.2944766070562 * c - 19.05792099799635 * c3 + 11.089223410359068 * c5 - 6.6711120475953765 * c7 + 3.016813013351768 * c9 - 0.6715752908287405 * c11)));
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using radians. Average error is
     * 1.057E-6 radians; maximum error is 1.922E-6. Takes y and x (in that unusual order) as
     * floats, and returns the angle from the origin to that point in radians. It is about 4 times faster than
     * {@link Math#atan2(double, double)} (roughly 15 ns instead of roughly 60 ns for Math, on Java 8 HotSpot).
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
     * translates to atan2().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a float; ranges from {@code -PI} to {@code PI}
     */
    public static float atan2(final float y, float x) {
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

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using positive or negative degrees.
     * Average error is ??? degrees; maximum error is ???. Takes y and x (in that unusual order) as
     * floats, and returns the angle from the origin to that point in degrees.
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
     * translates to atan2Deg().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a float; ranges from {@code -180} to {@code 180}
     */
    public static float atan2Deg(final float y, float x) {
        float n = y / x;
        if (n != n)
            n = (y == x ? 1f : -1f); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return atanUncheckedDeg(n);
        else if (x < 0) {
            if (y >= 0) return atanUncheckedDeg(n) + 180f;
            return atanUnchecked(n) - 180f;
        } else if (y > 0)
            return x + 90f;
        else if (y < 0) return x - 90f;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Returns acos in radians; less accurate than Math.acos but may be faster. Average error of 0.00002845 radians (0.0016300649
     * degrees), largest error of 0.000067548 radians (0.0038702153 degrees). This implementation does not return NaN if given an
     * out-of-range input (Math.acos does return NaN), unless the input is NaN.
     *
     * @param a acos is defined only when a is between -1f and 1f, inclusive
     * @return between {@code 0} and {@code PI} when a is in the defined range
     */
    public static float acos(float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return (float) Math.sqrt(1f - a) * (1.5707288f - 0.2121144f * a + 0.0742610f * a2 - 0.0187293f * a3);
        }
        return PI
                - (float) Math.sqrt(1f + a) * (1.5707288f + 0.2121144f * a + 0.0742610f * a2 + 0.0187293f * a3);
    }

    /**
     * Returns asin in radians; less accurate than Math.asin but may be faster. Average error of 0.000028447 radians (0.0016298931
     * degrees), largest error of 0.000067592 radians (0.0038727364 degrees). This implementation does not return NaN if given an
     * out-of-range input (Math.asin does return NaN), unless the input is NaN.
     *
     * @param a asin is defined only when a is between -1f and 1f, inclusive
     * @return between {@code -HALF_PI} and {@code HALF_PI} when a is in the defined range
     */
    public static float asin(float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return HALF_PI
                    - (float) Math.sqrt(1f - a) * (1.5707288f - 0.2121144f * a + 0.0742610f * a2 - 0.0187293f * a3);
        }
        return (float) Math.sqrt(1f + a) * (1.5707288f + 0.2121144f * a + 0.0742610f * a2 + 0.0187293f * a3) - HALF_PI;
    }

    /**
     * Arc tangent approximation with very low error, using an algorithm from the 1955 research study "Approximations for Digital
     * Computers," by RAND Corporation (this is sheet 11's algorithm, which is the fourth-fastest and fourth-least precise). This
     * method is usually about 4x faster than {@link Math#atan(double)}, but is somewhat less precise than Math's implementation.
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUnchecked(double)}, but this method will be correct
     * enough for infinite inputs, and atanUnchecked() will not be.
     *
     * @param i an input to the inverse tangent function; any float is accepted
     * @return an output from the inverse tangent function, from {@code -HALF_PI} to {@code HALF_PI} inclusive
     * @see #atanUnchecked(double) If you know the input will be finite, you can use atanUnchecked() instead.
     */
    public static float atan(float i) {
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
        return (float) (Math.signum(i) * (QUARTER_PI_D
                + (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11)));
    }

    /**
     * Arc tangent approximation returning a value measured in positive or negative degrees, using an algorithm from the
     * 1955 research study "Approximations for Digital Computers," by RAND Corporation (this is sheet 11's algorithm,
     * which is the fourth-fastest and fourth-least precise).
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUncheckedDeg(double)}, but this method will be correct
     * enough for infinite inputs, and atanUnchecked() will not be.
     *
     * @param i an input to the inverse tangent function; any float is accepted
     * @return an output from the inverse tangent function in degrees, from {@code -90} to {@code 90} inclusive
     * @see #atanUncheckedDeg(double) If you know the input will be finite, you can use atanUncheckedDeg() instead.
     */
    public static float atanDeg(float i) {
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
        return (float) (Math.signum(i) * (45.0
                + (57.2944766070562 * c - 19.05792099799635 * c3 + 11.089223410359068 * c5 - 6.6711120475953765 * c7 + 3.016813013351768 * c9 - 0.6715752908287405 * c11)));
    }

}
