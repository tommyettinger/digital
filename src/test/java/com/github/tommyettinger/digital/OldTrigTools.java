/*
 * Copyright (c) 2022 See AUTHORS file.
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
 *
 */

package com.github.tommyettinger.digital;

import com.badlogic.gdx.utils.NumberUtils;

/**
 * Various trigonometric approximations, using a lookup table for sin(), cos(), and tan(), and Taylor series for their
 * inverses. This supplies variants for radians, degrees, and turns. This also has an atan2() approximation defined with
 * output in radians, degrees, and turns.
 * <br>
 * This is primarily derived from libGDX's MathUtils class. The main new functionalities are the variants that take or
 * return measurements in turns, the now-available {@link #SIN_TABLE} and {@link #SIN_TABLE_D}, and double variants in
 * general. Using the sin table directly allows {@link #tan(float)} to be implemented in a straightforward way, and it
 * has other uses mentioned in its docs (in particular, uniform random unit vectors).
 * <br>
 * MathUtils had its sin and cos methods created by Riven on JavaGaming.org . The asin(), acos(), and atan() methods all
 * use Taylor series approximations from the 1955 research study "Approximations for Digital Computers," by RAND
 * Corporation; though one might think such code would be obsolete over 60 years later, the approximations from that
 * study seem to have higher accuracy and speed than most attempts in later decades, often those aimed at DSP usage.
 */
public final class OldTrigTools {

    /**
     * Not meant to be instantiated.
     */
    private OldTrigTools() {
    }

    /**
     * The {@code float} value that is closer than any other to
     * <i>pi</i>, the ratio of the circumference of a circle to its
     * diameter.
     */
    public static final float PI = (float) Math.PI;
    /**
     * The {@code double} value that is closer than any other to
     * <i>pi</i>, the ratio of the circumference of a circle to its
     * diameter.
     */
    public static final double PI_D = Math.PI;
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
     * {@link #PI} divided by 2f; the same as {@link #ETA}.
     */
    public static final float HALF_PI = PI * 0.5f;
    /**
     * {@link #PI} divided by 2f; the same as {@link #HALF_PI}.
     */
    public static final float ETA = HALF_PI;
    /**
     * 1.0 divided by {@link #PI}.
     */
    public static final double PI_INVERSE_D = (1.0 / Math.PI);
    /**
     * 2.0 times {@link Math#PI}; the same as {@link #TAU_D}.
     */
    public static final double PI2_D = Math.PI * 2.0;
    /**
     * 2.0 times {@link Math#PI}; the same as {@link #PI2_D}.
     */
    public static final double TAU_D = PI2_D;
    /**
     * {@link Math#PI} divided by 2.0; the same as {@link #ETA_D}.
     */
    public static final double HALF_PI_D = Math.PI * 0.5;
    /**
     * {@link Math#PI} divided by 2.0; the same as {@link #HALF_PI_D}.
     */
    public static final double ETA_D = HALF_PI_D;
    /**
     * {@link #PI} divided by 4f.
     */
    public static final float QUARTER_PI = PI * 0.25f;

    /**
     * {@link Math#PI} divided by 4.0.
     */
    public static final double QUARTER_PI_D = Math.PI * 0.25;

    private static final int SIN_BITS = 14; // 64KB. Adjust for accuracy.
    /**
     * The size of {@link #SIN_TABLE}, available separately from the table's length for convenience.
     */
    public static final int TABLE_SIZE = (1 << SIN_BITS);

    /**
     * If you add this to an index used in {@link #SIN_TABLE}, you get the result of the cosine instead of the sine.
     */
    public static final int SIN_TO_COS = TABLE_SIZE >>> 2;
    /**
     * The bitmask that can be used to confine any int to wrap within {@link #TABLE_SIZE}. Any accesses to
     * {@link #SIN_TABLE} with an index that could be out of bounds should probably be wrapped using this, as with
     * {@code SIN_TABLE[index & TABLE_MASK]}.
     */
    public static final int TABLE_MASK = TABLE_SIZE - 1;

    static final float radToIndex = TABLE_SIZE / PI2;
    static final float degToIndex = TABLE_SIZE / 360f;
    static final float turnToIndex = TABLE_SIZE;

    static final double radToIndexD = TABLE_SIZE / PI2_D;
    static final double degToIndexD = TABLE_SIZE / 360.0;
    static final double turnToIndexD = TABLE_SIZE;

    /**
     * Multiply by this to convert from radians to degrees.
     */
    public static final float radiansToDegrees = 180f / PI;
    /**
     * Multiply by this to convert from degrees to radians.
     */
    public static final float degreesToRadians = PI / 180f;
    /**
     * A precalculated table of 16385 floats, corresponding to the y-value of points on the unit circle, ordered by
     * increasing angle. This should not be mutated, but it can be accessed directly for things like getting random
     * unit vectors, or implementing the "sincos" method (which assigns sin() to one item and cos() to another).
     * <br>
     * A quick way to get a random unit vector is to get a random 14-bit number, as with
     * {@code int angle = random.nextInt() >>> 18;}, look up angle in this table to get y, then look up
     * {@code (angle + TrigTools.SIN_TO_COS) & TrigTools.TABLE_MASK} (or {@code (angle + 4096) & 16383}) to get x.
     */
    public static final float[] SIN_TABLE = new float[TABLE_SIZE+1];

    /**
     * Multiply by this to convert from radians to degrees.
     */
    public static final double radiansToDegreesD = 180.0 / Math.PI;
    /**
     * Multiply by this to convert from degrees to radians.
     */
    public static final double degreesToRadiansD = Math.PI / 180.0;
    /**
     * A precalculated table of 16385 doubles, corresponding to the y-value of points on the unit circle, ordered by
     * increasing angle. This should not be mutated, but it can be accessed directly for things like getting random
     * unit vectors, or implementing the "sincos" method (which assigns sin() to one item and cos() to another).
     * <br>
     * A quick way to get a random unit vector is to get a random 14-bit number, as with
     * {@code int angle = random.nextInt() >>> 18;}, look up angle in this table to get y, then look up
     * {@code (angle + TrigTools.SIN_TO_COS) & TrigTools.TABLE_MASK} (or {@code (angle + 4096) & 16383}) to get x.
     */
    public static final double[] SIN_TABLE_D = new double[TABLE_SIZE+1];

    static {
        for (int i = 0; i < TABLE_SIZE; i++)
            SIN_TABLE[i] = (float) (SIN_TABLE_D[i] = Math.sin((i + 0.5f) / TABLE_SIZE * PI2));
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE[0] = 0f;
        SIN_TABLE[(int) (90 * degToIndex) & TABLE_MASK] = 1f;
        SIN_TABLE[(int) (180 * degToIndex) & TABLE_MASK] = 0f;
        SIN_TABLE[(int) (270 * degToIndex) & TABLE_MASK] = -1.0f;
        SIN_TABLE_D[0] = 0.0;
        SIN_TABLE_D[(int) (90 * degToIndex) & TABLE_MASK] = 1.0;
        SIN_TABLE_D[(int) (180 * degToIndex) & TABLE_MASK] = 0.0;
        SIN_TABLE_D[(int) (270 * degToIndex) & TABLE_MASK] = -1.0;
    }

    /**
     * Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).
     *
     * @param radians an angle in radians, where 0 to {@link #PI2} is one rotation
     */
    public static float sin(float radians) {
        return SIN_TABLE[(int) (radians * radToIndex) & TABLE_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).
     *
     * @param radians an angle in radians, where 0 to {@link #PI2} is one rotation
     */
    public static float cos(float radians) {
        return SIN_TABLE[(int) (radians * radToIndex) + SIN_TO_COS & TABLE_MASK];
    }

    /**
     * Returns the tangent in radians, using a Padé approximant.
     * Padé approximants tend to be most accurate when they aren't producing results of extreme magnitude; in the tan()
     * function, those results occur on and near odd multiples of {@code PI/2}, and this method is least accurate when
     * given inputs near those multiples.
     * <br> For inputs between -1.57 to 1.57 (just inside half-pi), separated by 0x1p-20f,
     * absolute error is 0.00890192, relative error is 0.00000090, and the maximum error is 17.98901367 when given
     * 1.56999838. The maximum error might seem concerning, but it's the difference between the correct 1253.22167969
     * and the 1235.23266602 this returns, so for many purposes the difference won't be noticeable.
     * <br> For inputs between -1.55 to 1.55 (getting less close to half-pi), separated by 0x1p-20f, absolute error is
     * 0.00023368, relative error is -0.00000009, and the maximum error is 0.02355957 when given -1.54996467. The
     * maximum error is the difference between the correct -47.99691010 and the -47.97335052 this returns.
     * <br> While you don't have to use a dedicated method for tan(), and you can use {@code sin(x)/cos(x)},
     * approximating tan() in this way is very susceptible to error building up from any of sin(), cos() or the
     * division. Where this tan() has a maximum error in the -1.55 to 1.55 range of 0.02355957, the simpler division
     * technique on the same range has a maximum error of 1.25724030 (about 50 times worse), as well as larger absolute
     * and relative errors. Casting the double result of {@link Math#tan(double)} to float will get the highest
     * precision, but can be anywhere from 2.5x to nearly 4x slower than this, depending on JVM.
     * <br> Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer by Soonts</a>.
     *
     * @param radians a float angle in radians, where 0 to {@link #PI2} is one rotation
     * @return a float approximation of tan()
     */
    public static float tan(float radians) {
        radians *= OldTrigTools.PI_INVERSE;
        radians += 0.5f;
        radians -= Math.floor(radians);
        radians -= 0.5f;
        radians *= OldTrigTools.PI;
        final float x2 = radians * radians, x4 = x2 * x2;
        return radians * ((0.0010582010582010583f) * x4 - (0.1111111111111111f) * x2 + 1f)
                / ((0.015873015873015872f) * x4 - (0.4444444444444444f) * x2 + 1f);
        // How we calculated those long constants above (from Stack Exchange, by Soonts):
//        return x * ((1.0/945.0) * x4 - (1.0/9.0) * x2 + 1.0) / ((1.0/63.0) * x4 - (4.0/9.0) * x2 + 1.0);
        // Normally, it would be best to show the division steps, but if GWT isn't computing mathematical constants at
        // compile-time, which I don't know if it does, that would make the shown-division way slower by 4 divisions.
    }

    /**
     * Returns the sine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive).
     *
     * @param degrees an angle in degrees, where 0 to 360 is one rotation
     */
    public static float sinDeg(float degrees) {
        return SIN_TABLE[(int) (degrees * degToIndex) & TABLE_MASK];
    }

    /**
     * Returns the cosine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive).
     *
     * @param degrees an angle in degrees, where 0 to 360 is one rotation
     */
    public static float cosDeg(float degrees) {
        return SIN_TABLE[(int) (degrees * degToIndex) + SIN_TO_COS & TABLE_MASK];
    }

    /**
     * Returns the tangent in degrees, using a Padé approximant.
     * Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer</a>.
     *
     * @param degrees an angle in degrees, where 0 to 360 is one rotation
     * @return a float approximation of tan()
     */
    public static float tanDeg(float degrees) {
        degrees *= 1f/180f;
        degrees += 0.5f;
        degrees -= Math.floor(degrees);
        degrees -= 0.5f;
        degrees *= OldTrigTools.PI;
        final float x2 = degrees * degrees, x4 = x2 * x2;
        return degrees * ((0.0010582010582010583f) * x4 - (0.1111111111111111f) * x2 + 1f)
                / ((0.015873015873015872f) * x4 - (0.4444444444444444f) * x2 + 1f);
    }

    /**
     * Returns the sine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive).
     *
     * @param turns an angle in turns, where 0 to 1 is one rotation
     */
    public static float sinTurns(float turns) {
        return SIN_TABLE[(int) (turns * turnToIndex) & TABLE_MASK];
    }

    /**
     * Returns the cosine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive).
     *
     * @param turns an angle in turns, where 0 to 1 is one rotation
     */
    public static float cosTurns(float turns) {
        return SIN_TABLE[(int) (turns * turnToIndex) + SIN_TO_COS & TABLE_MASK];
    }

    /**
     * Returns the tangent in turns, using a Padé approximant.
     * Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer</a>.
     *
     * @param turns an angle in turns, where 0 to 1 is one rotation
     * @return a float approximation of tan()
     */
    public static float tanTurns(float turns) {
        turns += turns;
        turns += 0.5f;
        turns -= Math.floor(turns);
        turns -= 0.5f;
        turns *= OldTrigTools.PI;
        final float x2 = turns * turns, x4 = x2 * x2;
        return turns * ((0.0010582010582010583f) * x4 - (0.1111111111111111f) * x2 + 1f)
                / ((0.015873015873015872f) * x4 - (0.4444444444444444f) * x2 + 1f);
    }

    /**
     * Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).
     *
     * @param radians an angle in radians, where 0 to {@link #PI2_D} is one rotation
     */
    public static double sin(double radians) {
        return SIN_TABLE_D[(int) (radians * radToIndexD) & TABLE_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).
     *
     * @param radians an angle in radians, where 0 to {@link #PI2_D} is one rotation
     */
    public static double cos(double radians) {
        return SIN_TABLE_D[(int) (radians * radToIndexD) + SIN_TO_COS & TABLE_MASK];
    }

    /**
     * Returns the tangent in radians, using a Padé approximant.
     * Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer</a>.
     * @param radians a double angle in radians, where 0 to {@link #PI2} is one rotation
     * @return a double approximation of tan()
     */
    public static double tan(double radians) {
        radians *= OldTrigTools.PI_INVERSE_D;
        radians += 0.5;
        radians -= Math.floor(radians);
        radians -= 0.5;
        radians *= OldTrigTools.PI_D;
        final double x2 = radians * radians, x4 = x2 * x2;
        return radians * ((0.0010582010582010583) * x4 - (0.1111111111111111) * x2 + 1.0)
                / ((0.015873015873015872) * x4 - (0.4444444444444444) * x2 + 1.0);
        // how we calculated those large constants above:
//        return x * ((1.0/945.0) * x4 - (1.0/9.0) * x2 + 1.0) / ((1.0/63.0) * x4 - (4.0/9.0) * x2 + 1.0);
    }

    /**
     * Returns the sine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive).
     *
     * @param degrees an angle in degrees, where 0 to 360 is one rotation
     */
    public static double sinDeg(double degrees) {
        return SIN_TABLE_D[(int) (degrees * degToIndexD) & TABLE_MASK];
    }

    /**
     * Returns the cosine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
     * inclusive).
     *
     * @param degrees an angle in degrees, where 0 to 360 is one rotation
     */
    public static double cosDeg(double degrees) {
        return SIN_TABLE_D[(int) (degrees * degToIndexD) + SIN_TO_COS & TABLE_MASK];
    }

    /**
     * Returns the tangent in degrees, using a Padé approximant.
     * Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer</a>.
     *
     * @param degrees an angle in degrees, where 0 to 360 is one rotation
     * @return a double approximation of tan()
     */
    public static double tanDeg(double degrees) {
        degrees *= 1.0/180.0;
        degrees += 0.5;
        degrees -= Math.floor(degrees);
        degrees -= 0.5;
        degrees *= OldTrigTools.PI_D;
        final double x2 = degrees * degrees, x4 = x2 * x2;
        return degrees * ((0.0010582010582010583) * x4 - (0.1111111111111111) * x2 + 1.0)
                / ((0.015873015873015872) * x4 - (0.4444444444444444) * x2 + 1.0);
    }

    /**
     * Returns the sine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive).
     *
     * @param turns an angle in turns, where 0 to 1 is one rotation
     */
    public static double sinTurns(double turns) {
        return SIN_TABLE_D[(int) (turns * turnToIndexD) & TABLE_MASK];
    }

    /**
     * Returns the cosine in turns from a lookup table. For optimal precision, use turns between -1 and 1 (both
     * inclusive).
     *
     * @param turns an angle in turns, where 0 to 1 is one rotation
     */
    public static double cosTurns(double turns) {
        return SIN_TABLE_D[(int) (turns * turnToIndexD) + SIN_TO_COS & TABLE_MASK];
    }

    /**
     * Returns the tangent in turns, using a Padé approximant.
     * Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer</a>.
     *
     * @param turns an angle in turns, where 0 to 1 is one rotation
     * @return a double approximation of tan()
     */
    public static double tanTurns(double turns) {
        turns += turns;
        turns += 0.5;
        turns -= Math.floor(turns);
        turns -= 0.5;
        turns *= OldTrigTools.PI_D;
        final double x2 = turns * turns, x4 = x2 * x2;
        return turns * ((0.0010582010582010583) * x4 - (0.1111111111111111) * x2 + 1.0)
                / ((0.015873015873015872) * x4 - (0.4444444444444444) * x2 + 1.0);
    }

    /**
     * Gets an approximation of the sine of {@code radians} that is usually much more accurate than
     * {@link #sin(float)}, but that is somewhat slower. This still offers about 2x to
     * 4x the throughput of {@link Math#sin(double)} (cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #sin(float)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param radians an angle in radians; optimally between {@code -PI2} and {@code PI2}
     * @return the approximate sine of the given angle, from -1 to 1 inclusive
     */
    public static float sinSmoother(float radians) {
//        //Mean absolute error:     0.0000001205
//        //Mean relative error:     0.0007681165
//        //Maximum abs. error:      0.0002874465
//        //Maximum rel. error:   1644.0024414063
        radians = radians * radToIndex - 0.5f;
        final int floor = (int)(radians + 16384.0) - 16384;
        final float from = SIN_TABLE[floor & TABLE_MASK], to = SIN_TABLE[(floor + 1) & TABLE_MASK];
        return from + (to - from) * (radians - floor);
//        //Mean absolute error:     0.0001210357
//        //Mean relative error:     0.0010268957
//        //Maximum abs. error:      0.0001922157
//        //Maximum rel. error:      1.1638665199
//        radians = radians * radToIndex;
//        final int floor = (int)(radians + 16384.0) - 16384;
//        final int masked = floor & TABLE_MASK;
//        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
//        return from + (to - from) * (radians - floor);
//        //Mean absolute error:     0.0000902124
//        //Mean relative error:     0.0008568958
//        //Maximum abs. error:      0.0002871510
//        //Maximum rel. error:      1.7204090357
//        radians = radians * radToIndex;
//        final int floor = (int)(radians + 16384.0) - 16384;
//        final float rem = (radians - floor);
//        final float from = SIN_TABLE[floor & TABLE_MASK], to = SIN_TABLE[floor + (NumberUtils.floatToIntBits(rem - 0.5f) >> 31 | 1) & TABLE_MASK];
//        return from + (to - from) * rem;
        //Mean absolute error:     0.0000001323
        //Mean relative error:     0.0011963437
        //Maximum abs. error:      0.0002878467
        //Maximum rel. error:   1906.3225097656
//        radians = radians * radToIndex - 0.5f;
//        final int floor = (int)(radians + 16384.5) - 16384;
//        final int masked = floor & TABLE_MASK;
//        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
//        return from + (to - from) * (radians - floor);
//        //Mean absolute error:     0.0000001249
//        //Mean relative error:     0.0008269644
//        //Maximum abs. error:      0.0003832272
//        //Maximum rel. error:    635.4584350586
//        radians = radians * radToIndex - 0.5f;
//        final int floor = (int)radians + ((int)radians>>31);
//        final int masked = floor & TABLE_MASK;
//        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
//        return from + (to - from) * (radians - floor);
//        //Mean absolute error:     0.0000000960
//        //Mean relative error:     0.0007683055
//        //Maximum abs. error:      0.0002874465
//        //Maximum rel. error:   1644.0024414063
//        radians = (float) (radians * radToIndexD - 0.5);
//        final int floor = (int)(radians + 16384.0) - 16384;
//        final int masked = floor & TABLE_MASK;
//        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
//        return from + (to - from) * (radians - floor);
        //Mean absolute error:     0.0000000783
        //Mean relative error:     0.0006054470
        //Maximum abs. error:      0.0001917247
        //Maximum rel. error:    634.0779418945
//        if(radians < 0f) {
//            radians = -radians * radToIndex - 0.5f;
//            final int floor = (int) (radians + 1) - 1;
//            final int masked = floor & TABLE_MASK;
//            final float from = SIN_TABLE[masked], to = SIN_TABLE[masked + 1];
//            return -from - (to - from) * (radians - floor);
//        }
//        radians = radians * radToIndex - 0.5f;
//        final int floor = (int) (radians + 1) - 1;
//        final int masked = floor & TABLE_MASK;
//        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked + 1];
//        return from + (to - from) * (radians - floor);

    }
    public static float sinShifty(final float radians) {
        //Mean absolute error:     0.0000602018
        //Mean relative error:     0.0010084792
        //Maximum abs. error:      0.0003832261
        //Maximum rel. error:   1267.9168701172
        final int idx = (int)(radians * radToIndex);
        return SIN_TABLE[(idx + (idx >> 31)) & TABLE_MASK];
    }

    /**
     * Gets an approximation of the sine of {@code radians} that is usually much more accurate than
     * {@link #sin(double)}, but that is somewhat slower. This still offers better
     * throughput than {@link Math#sin(double)}.
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #sin(double)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param radians an angle in radians; optimally between {@code -PI2_D} and {@code PI2_D}
     * @return the approximate sine of the given angle, from -1 to 1 inclusive
     */
    public static double sinSmoother(double radians) {
        // 14 bits
        //Mean absolute error:     0.0000000078
        //Mean relative error:     0.0000001134
        //Maximum abs. error:      0.0000000184
        //Maximum rel. error:      1.0000000000
        radians = radians * radToIndexD - 0.5;
        final int floor = (int) Math.floor(radians);
        final int masked = floor & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }

    /**
     * Gets an approximation of the cosine of {@code radians} that is usually much more accurate than
     * {@link #cos(float)}, but that is somewhat slower. This still offers about 2x to
     * 4x the throughput of {@link Math#cos(double)} (cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #cos(float)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param radians an angle in radians; optimally between {@code -PI2} and {@code PI2}
     * @return the approximate cosine of the given angle, from -1 to 1 inclusive
     */
    public static float cosSmoother(float radians) {
        // 14 bits
        //Mean absolute error:     0.0000000719
        //Mean relative error:     0.0000011134
        //Maximum abs. error:      0.0000004172
        //Maximum rel. error:      0.9999999404
        radians = radians * radToIndex - 0.5f;
        final int floor = (int)(radians + 16384.0) - 16384;
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (radians - floor);
    }

    /**
     * Gets an approximation of the cosine of {@code radians} that is usually much more accurate than
     * {@link #cos(double)}, but that is somewhat slower. This still offers better
     * throughput than {@link Math#cos(double)}.
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #cos(double)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param radians an angle in radians; optimally between {@code -PI2_D} and {@code PI2_D}
     * @return the approximate cosine of the given angle, from -1 to 1 inclusive
     */
    public static double cosSmoother(double radians) {
        radians = radians * radToIndexD - 0.5;
        final int floor = (int) Math.floor(radians);
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }

    /**
     * Gets an approximation of the tangent of {@code radians} that is usually much more accurate than
     * {@link #tan(float)}, and can be slightly faster on recent JDKs (or slower on JDK 8). This still offers much
     * higher throughput than {@link Math#tan(double)} (cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #sin(float)} and {@link #cos(float)} use, but
     * interpolates between adjacent entries in the table, rather than just using one entry for each unmodified. It
     * simply gets the sine and cosine at about the same time, then divides sine by cosine. This is different from how
     * {@link #tan(float)} works, and tends to be much more precise.
     * @param radians a float angle in radians, where 0 to {@link #PI2} is one rotation
     * @return a float approximation of tan()
     */
    public static float tanSmoother(float radians) {
        // on the -1.57 to 1.57 range:
        //Mean absolute error:     0.0000502852
        //Mean relative error:     0.0000002945
        //Maximum abs. error:      0.1672363281
        //Maximum rel. error:      0.0001353590
        radians = radians * radToIndex - 0.5f;
        final int floor = (int)(radians + 16384.0) - 16384;
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final float fromS = SIN_TABLE[maskedS], toS = SIN_TABLE[maskedS+1];
        final float fromC = SIN_TABLE[maskedC], toC = SIN_TABLE[maskedC+1];
        radians -= floor;
        return (fromS + (toS - fromS) * radians)/(fromC + (toC - fromC) * radians);
    }

    /**
     * Gets an approximation of the tangent of {@code radians} that is usually much more accurate than
     * {@link #tan(double)}, and can be slightly faster on recent JDKs (or slower on JDK 8). This still offers much
     * higher throughput than {@link Math#tan(double)}.
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #sin(double)} and {@link #cos(double)} use, but
     * interpolates between adjacent entries in the table, rather than just using one entry for each unmodified. It
     * simply gets the sine and cosine at about the same time, then divides sine by cosine. This is different from how
     * {@link #tan(double)} works, and tends to be much more precise.
     * @param radians a double angle in radians, where 0 to {@link #PI2} is one rotation
     * @return an approximation of tan()
     */
    public static double tanSmoother(double radians) {
        radians = radians * radToIndexD - 0.5;
        final int floor = (int)Math.floor(radians);
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final double fromS = SIN_TABLE_D[maskedS], toS = SIN_TABLE_D[maskedS+1];
        final double fromC = SIN_TABLE_D[maskedC], toC = SIN_TABLE_D[maskedC+1];
        radians -= floor;
        return (fromS + (toS - fromS) * radians)/(fromC + (toC - fromC) * radians);
    }

    /**
     * Gets an approximation of the sine of {@code degrees} that is usually much more accurate than
     * {@link #sinDeg(float)}, but that is somewhat slower. This still offers about 2x
     * to 4x the throughput of {@link Math#sin(double)} (converted from degrees and cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #sinDeg(float)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param degrees an angle in degrees; optimally between -360 and 360
     * @return the approximate sine of the given angle, from -1 to 1 inclusive
     */
    public static float sinSmootherDeg(float degrees) {
        //Mean absolute error:     0.0000000590
        //Mean relative error:     0.0000008347
        //Maximum abs. error:      0.0000003576
        //Maximum rel. error:      0.2968730032
        degrees *= degToIndex;
        final int floor = (int)(degrees + 16384.0) - 16384;
        final int masked = floor & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (degrees - floor);
    }

    /**
     * Gets an approximation of the sine of {@code degrees} that is usually much more accurate than
     * {@link #sinDeg(double)}, but that is somewhat slower. This still offers better
     * throughput than {@link Math#sin(double)} (converted from degrees).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #sinDeg(double)} uses, but interpolates between
     * two adjacent entries in the table, rather than just using one entry unmodified.
     * @param degrees an angle in degrees; optimally between -360 and 360
     * @return the approximate sine of the given angle, from -1 to 1 inclusive
     */
    public static double sinSmootherDeg(double degrees) {
        degrees *= degToIndexD;
        final int floor = (int) Math.floor(degrees);
        final int masked = floor & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (degrees - floor);
    }

    /**
     * Gets an approximation of the cosine of {@code degrees} that is usually much more accurate than
     * {@link #cosDeg(float)}, but that is somewhat slower. This still offers about 2x to
     * 4x the throughput of {@link Math#cos(double)} (converted from degrees and cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #cosDeg(float)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param degrees an angle in degrees; optimally between -360 and 360
     * @return the approximate cosine of the given angle, from -1 to 1 inclusive
     */
    public static float cosSmootherDeg(float degrees) {
        degrees *= degToIndex;
        final int floor = (int)(degrees + 16384.0) - 16384;
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (degrees - floor);
    }

    /**
     * Gets an approximation of the cosine of {@code degrees} that is usually much more accurate than
     * {@link #cosDeg(double)}, but that is somewhat slower. This still offers better
     * throughput than {@link Math#cos(double)} (converted from degrees).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #cosDeg(double)} uses, but interpolates between
     * two adjacent entries in the table, rather than just using one entry unmodified.
     * @param degrees an angle in degrees; optimally between -360 and 360
     * @return the approximate cosine of the given angle, from -1 to 1 inclusive
     */
    public static double cosSmootherDeg(double degrees) {
        degrees *= degToIndexD;
        final int floor = (int) Math.floor(degrees);
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (degrees - floor);
    }

    /**
     * Gets an approximation of the tangent of {@code degrees} that is usually much more accurate than
     * {@link #tanDeg(float)}, and can be slightly faster on recent JDKs (or slower on JDK 8). This still offers much
     * higher throughput than {@link Math#tan(double)} (converted from degrees and cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #sinDeg(float)} and {@link #cosDeg(float)} use, but
     * interpolates between adjacent entries in the table, rather than just using one entry for each unmodified. It
     * simply gets the sine and cosine at about the same time, then divides sine by cosine. This is different from how
     * {@link #tanDeg(float)} works, and tends to be much more precise.
     * @param degrees a float angle in degrees, where 0 to 360 is one rotation
     * @return a float approximation of tan()
     */
    public static float tanSmootherDeg(float degrees) {
        degrees *= degToIndex;
        final int floor = (int)(degrees + 16384.0) - 16384;
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final float fromS = SIN_TABLE[maskedS], toS = SIN_TABLE[maskedS+1];
        final float fromC = SIN_TABLE[maskedC], toC = SIN_TABLE[maskedC+1];
        return (fromS + (toS - fromS) * (degrees - floor))/(fromC + (toC - fromC) * (degrees - floor));
    }

    /**
     * Gets an approximation of the tangent of {@code degrees} that is usually much more accurate than
     * {@link #tanDeg(double)}, and can be slightly faster on recent JDKs (or slower on JDK 8). This still offers much
     * higher throughput than {@link Math#tan(double)} (converted from degrees).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #sinDeg(double)} and {@link #cosDeg(double)} use,
     * but interpolates between adjacent entries in the table, rather than just using one entry for each unmodified. It
     * simply gets the sine and cosine at about the same time, then divides sine by cosine. This is different from how
     * {@link #tanDeg(double)} works, and tends to be much more precise.
     * @param degrees a double angle in degrees, where 0 to 360 is one rotation
     * @return an approximation of tan()
     */
    public static double tanSmootherDeg(double degrees) {
        degrees *= degToIndexD;
        final int floor = (int)Math.floor(degrees);
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final double fromS = SIN_TABLE_D[maskedS], toS = SIN_TABLE_D[maskedS+1];
        final double fromC = SIN_TABLE_D[maskedC], toC = SIN_TABLE_D[maskedC+1];
        return (fromS + (toS - fromS) * (degrees - floor))/(fromC + (toC - fromC) * (degrees - floor));
    }

    /**
     * Gets an approximation of the sine of {@code turns} that is usually much more accurate than
     * {@link #sinTurns(float)}, but that is somewhat slower. This still offers about 2x
     * to 4x the throughput of {@link Math#sin(double)} (converted from turns and cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #sinTurns(float)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param turns an angle in turns; optimally between -1 and 1
     * @return the approximate sine of the given angle, from -1 to 1 inclusive
     */
    public static float sinSmootherTurns(float turns) {
        turns *= turnToIndex;
        final int floor = (int)(turns + 16384.0) - 16384;
        final int masked = floor & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (turns - floor);
    }

    /**
     * Gets an approximation of the sine of {@code turns} that is usually much more accurate than
     * {@link #sinTurns(double)}, but that is somewhat slower. This still offers better
     * throughput than {@link Math#sin(double)} (converted from turns).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #sinTurns(double)} uses, but interpolates between
     * two adjacent entries in the table, rather than just using one entry unmodified.
     * @param turns an angle in turns; optimally between -1 and 1
     * @return the approximate sine of the given angle, from -1 to 1 inclusive
     */
    public static double sinSmootherTurns(double turns) {
        turns *= turnToIndexD;
        final int floor = (int) Math.floor(turns);
        final int masked = floor & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (turns - floor);
    }

    /**
     * Gets an approximation of the cosine of {@code turns} that is usually much more accurate than
     * {@link #cosTurns(float)}, but that is somewhat slower. This still offers about 2x to
     * 4x the throughput of {@link Math#cos(double)} (converted from turns and cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #cosTurns(float)} uses, but interpolates between two
     * adjacent entries in the table, rather than just using one entry unmodified.
     * @param turns an angle in turns; optimally between -1 and 1
     * @return the approximate cosine of the given angle, from -1 to 1 inclusive
     */
    public static float cosSmootherTurns(float turns) {
        turns *= turnToIndex;
        final int floor = (int)(turns + 16384.0) - 16384;
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (turns - floor);
    }

    /**
     * Gets an approximation of the cosine of {@code turns} that is usually much more accurate than
     * {@link #cosTurns(double)}, but that is somewhat slower. This still offers better
     * throughput than {@link Math#cos(double)} (converted from turns).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #cosTurns(double)} uses, but interpolates between
     * two adjacent entries in the table, rather than just using one entry unmodified.
     * @param turns an angle in turns; optimally between -1 and 1
     * @return the approximate cosine of the given angle, from -1 to 1 inclusive
     */
    public static double cosSmootherTurns(double turns) {
        turns *= turnToIndexD;
        final int floor = (int) Math.floor(turns);
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (turns - floor);
    }

    /**
     * Gets an approximation of the tangent of {@code turns} that is usually much more accurate than
     * {@link #tanTurns(float)}, and can be slightly faster on recent JDKs (or slower on JDK 8). This still offers much
     * higher throughput than {@link Math#tan(double)} (converted from turns and cast to float).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE} that {@link #sinTurns(float)} and {@link #cosTurns(float)} use, but
     * interpolates between adjacent entries in the table, rather than just using one entry for each unmodified. It
     * simply gets the sine and cosine at about the same time, then divides sine by cosine. This is different from how
     * {@link #tanTurns(float)} works, and tends to be much more precise.
     * @param turns a float angle in turns, where 0 to 1 is one rotation
     * @return a float approximation of tan()
     */
    public static float tanSmootherTurns(float turns) {
        turns *= turnToIndex;
        final int floor = (int)(turns + 16384.0) - 16384;
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final float fromS = SIN_TABLE[maskedS], toS = SIN_TABLE[maskedS+1];
        final float fromC = SIN_TABLE[maskedC], toC = SIN_TABLE[maskedC+1];
        return (fromS + (toS - fromS) * (turns - floor))/(fromC + (toC - fromC) * (turns - floor));
    }

    /**
     * Gets an approximation of the tangent of {@code turns} that is usually much more accurate than
     * {@link #tanTurns(double)}, and can be slightly faster on recent JDKs (or slower on JDK 8). This still offers much
     * higher throughput than {@link Math#tan(double)} (converted from turns).
     * <br>
     * Internally, this uses the same {@link #SIN_TABLE_D} that {@link #sinTurns(double)} and {@link #cosTurns(double)} use,
     * but interpolates between adjacent entries in the table, rather than just using one entry for each unmodified. It
     * simply gets the sine and cosine at about the same time, then divides sine by cosine. This is different from how
     * {@link #tanTurns(double)} works, and tends to be much more precise.
     * @param turns a double angle in turns, where 0 to 1 is one rotation
     * @return an approximation of tan()
     */
    public static double tanSmootherTurns(double turns) {
        turns *= turnToIndexD;
        final int floor = (int)Math.floor(turns);
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final double fromS = SIN_TABLE_D[maskedS], toS = SIN_TABLE_D[maskedS+1];
        final double fromC = SIN_TABLE_D[maskedC], toC = SIN_TABLE_D[maskedC+1];
        return (fromS + (toS - fromS) * (turns - floor))/(fromC + (toC - fromC) * (turns - floor));
    }

    // ---

    /**
     * A variant on {@link #atan(float)} that does not tolerate infinite inputs for speed reasons. This can be given a double
     * parameter, but is otherwise the same as atan(float), and returns a float like that method. It uses the same approximation,
     * from sheet 11 of "Approximations for Digital Computers." This is mostly meant to be used inside
     * {@link #atan2(float, float)}, but it may be a tiny bit faster than atan(float) in other code.
     *
     * @param i any finite double or float, but more commonly a float
     * @return an output from the inverse tangent function in radians, from {@code -HALF_PI} to {@code HALF_PI} inclusive
     */
    public static double atanUnchecked(double i) {
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
        return Math.signum(i) * (QUARTER_PI_D
                + (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11));
    }

    /**
     * A variant on {@link #atanTurns(float)} that does not tolerate infinite inputs for speed reasons. This can be given a double
     * parameter, but is otherwise the same as atanTurns(float), but returns a double in case external code needs higher precision.
     * It uses the same approximation, from sheet 11 of "Approximations for Digital Computers." This is mostly meant to be used inside
     * {@link #atan2Turns(float, float)}, but it may be a tiny bit faster than atanTurns(float) in other code.
     *
     * @param i any finite double or float, but more commonly a float
     * @return an output from the inverse tangent function in turns, from {@code -0.25} to {@code 0.25} inclusive
     */
    public static double atanUncheckedTurns(double i) {
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
        return (Math.signum(i) * (0.125
                + (0.15915132390848943 * c - 0.052938669438878753 * c3 + 0.030803398362108523 * c5
                - 0.01853086679887605 * c7 + 0.008380036148199356 * c9 - 0.0018654869189687236 * c11)));
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
    public static double atanUncheckedDeg(double i) {
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
        return (Math.signum(i) * (45.0
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
            n = (y == x ? 1f : -1.0f); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return (float) atanUnchecked(n);
        else if (x < 0) {
            if (y >= 0) return (float) (atanUnchecked(n) + Math.PI);
            return (float) (atanUnchecked(n) - Math.PI);
        } else if (y > 0)
            return x + HALF_PI;
        else if (y < 0) return x - HALF_PI;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using positive or negative degrees.
     * Average absolute error is 0.00006037 degrees; relative error is 0 degrees, maximum error is 0.00010396 degrees.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in degrees.
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
            n = (y == x ? 1f : -1.0f); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return (float) atanUncheckedDeg(n);
        else if (x < 0) {
            if (y >= 0) return (float) (atanUncheckedDeg(n) + 180.0);
            return (float) (atanUncheckedDeg(n) - 180.0);
        } else if (y > 0)
            return x + 90f;
        else if (y < 0) return x - 90f;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using non-negative degrees only.
     * Average absolute error is 0.00006045 degrees; relative error is 0 degrees; maximum error is 0.00011178 degrees.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in degrees.
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
     * translates to atan2Deg360().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a float; ranges from {@code 0} to {@code 360}
     */
    public static float atan2Deg360(final float y, float x) {
        float n = y / x;
        if (n != n)
            n = (y == x ? 1f : -1.0f); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if (x > 0) {
            if (y >= 0)
                return (float) atanUncheckedDeg(n);
            else
                return (float) (atanUncheckedDeg(n) + 360.0);
        } else if (x < 0) {
            return (float) (atanUncheckedDeg(n) + 180.0);
        } else if (y > 0) return x + 90f;
        else if (y < 0) return x + 270f;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using non-negative turns only.
     * Average absolute error is 0.00000030 turns; relative error is 0 turns; maximum error is 0.00000017 turns.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in turns.
     * Because this always returns a float between 0.0 (inclusive) and 1.0 (exclusive), it can be useful for various
     * kinds of calculations that must store angles as a small fraction, such as packing a hue angle into a byte.
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
     * translates to atan2Turns().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in turns as a float; ranges from {@code 0.0f} to {@code 1.0f}
     */
    public static float atan2Turns(final float y, float x) {
        float n = y / x;
        if (n != n)
            n = (y == x ? 1f : -1.0f); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if (x > 0) {
            if (y >= 0)
                return (float) atanUncheckedTurns(n);
            else
                return (float) (atanUncheckedTurns(n) + 1.0);
        } else if (x < 0) {
            return (float) (atanUncheckedTurns(n) + 0.5);
        } else if (y > 0) return x + 0.25f;
        else if (y < 0) return x + 0.75f;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using radians. Average error is
     * 1.057E-6 radians; maximum error is 1.922E-6. Takes y and x (in that unusual order) as
     * doubles, and returns the angle from the origin to that point in radians. It is about 4 times faster than
     * {@link Math#atan2(double, double)} (roughly 15 ns instead of roughly 60 ns for Math, on Java 8 HotSpot).
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(double)} method, and that cleanly
     * translates to atan2().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a double; ranges from {@code -PI} to {@code PI}
     */
    public static double atan2(final double y, double x) {
        double n = y / x;
        if (n != n)
            n = (y == x ? 1.0 : -1.0); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0.0; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return atanUnchecked(n);
        else if (x < 0) {
            if (y >= 0) return atanUnchecked(n) + Math.PI;
            return atanUnchecked(n) - Math.PI;
        } else if (y > 0)
            return x + HALF_PI_D;
        else if (y < 0) return x - HALF_PI_D;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using positive or negative degrees.
     * Average absolute error is 0.00006037 degrees; relative error is 0 degrees, maximum error is 0.00010396 degrees.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in degrees.
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(double)} method, and that cleanly
     * translates to atan2Deg().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a double; ranges from {@code -180} to {@code 180}
     */
    public static double atan2Deg(final double y, double x) {
        double n = y / x;
        if (n != n)
            n = (y == x ? 1.0 : -1.0); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0.0; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return atanUncheckedDeg(n);
        else if (x < 0) {
            if (y >= 0) return atanUncheckedDeg(n) + 180.0;
            return atanUncheckedDeg(n) - 180.0;
        } else if (y > 0)
            return x + 90.0;
        else if (y < 0) return x - 90.0;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using non-negative degrees only.
     * Average absolute error is 0.00006045 degrees; relative error is 0 degrees; maximum error is 0.00011178 degrees.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in degrees.
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(double)} method, and that cleanly
     * translates to atan2Deg360().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a double; ranges from {@code 0} to {@code 360}
     */
    public static double atan2Deg360(final double y, double x) {
        double n = y / x;
        if (n != n)
            n = (y == x ? 1.0 : -1.0); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0.0; // if n is infinite, y is infinitely larger than x.
        if (x > 0) {
            if (y >= 0)
                return atanUncheckedDeg(n);
            else
                return atanUncheckedDeg(n) + 360.0;
        } else if (x < 0) {
            return atanUncheckedDeg(n) + 180.0;
        } else if (y > 0) return x + 90.0;
        else if (y < 0) return x + 270.0;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, using non-negative turns only.
     * Average absolute error is 0.00000030 turns; relative error is 0 turns; maximum error is 0.00000017 turns.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in turns.
     * Because this always returns a double between 0.0 (inclusive) and 1.0 (exclusive), it can be useful for various
     * kinds of calculations that must store angles as a small fraction, such as packing a hue angle into a byte.
     * <br>
     * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
     * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
     * a very small degree, and are considerably less precise. That study provides an {@link #atan(double)} method, and that cleanly
     * translates to atan2Turns().
     *
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in turns as a double; ranges from {@code 0.0} to {@code 1.0}
     */
    public static double atan2Turns(final double y, double x) {
        double n = y / x;
        if (n != n)
            n = (y == x ? 1.0 : -1.0); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0.0; // if n is infinite, y is infinitely larger than x.
        if (x > 0) {
            if (y >= 0)
                return atanUncheckedTurns(n);
            else
                return atanUncheckedTurns(n) + 1.0;
        } else if (x < 0) {
            return atanUncheckedTurns(n) + 0.5;
        } else if (y > 0) return x + 0.25;
        else if (y < 0) return x + 0.75;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    /**
     * Returns arcsine in radians; less accurate than Math.asin but may be faster. Average error of 0.000028447 radians (0.0016298931
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
     * Returns arcsine in degrees. This implementation does not return NaN if given an
     * out-of-range input (Math.asin does return NaN), unless the input is NaN.
     *
     * @param a asin is defined only when a is between -1f and 1f, inclusive
     * @return between {@code -90} and {@code 90} when a is in the defined range
     */
    public static float asinDeg(float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return 90f
                    - (float) Math.sqrt(1f - a) * (89.99613099964837f - 12.153259893949748f * a + 4.2548418824210055f * a2 - 1.0731098432343729f * a3);
        }
        return (float) Math.sqrt(1f + a) * (89.99613099964837f + 12.153259893949748f * a + 4.2548418824210055f * a2 + 1.0731098432343729f * a3) - 90f;
    }

    /**
     * Returns arcsine in turns. This implementation does not return NaN if given an
     * out-of-range input (Math.asin does return NaN), unless the input is NaN.
     * Note that unlike {@link #atan2Turns(float, float)}, this can return negative turn values.
     *
     * @param a asin is defined only when a is between -1f and 1f, inclusive
     * @return between {@code -0.25} and {@code 0.25} when a is in the defined range
     */
    public static float asinTurns(float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return (float) (0.25 - Math.sqrt(1.0 - a) * (0.24998925277680104 - 0.033759055260971525 * a + 0.011819005228947238 * a2 - 0.0029808606756510357 * a3));
        }
        return (float) (Math.sqrt(1.0 + a) * (0.24998925277680104 + 0.033759055260971525 * a + 0.011819005228947238 * a2 + 0.0029808606756510357 * a3) - 0.25);
    }

    /**
     * Returns arccosine in radians; less accurate than Math.acos but may be faster. Average error of 0.00002845 radians (0.0016300649
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
     * Returns arccosine in degrees. This implementation does not return NaN if given an
     * out-of-range input (Math.acos does return NaN), unless the input is NaN.
     *
     * @param a acos is defined only when a is between -1f and 1f, inclusive
     * @return between {@code 0} and {@code 180} when a is in the defined range
     */
    public static float acosDeg(float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return (float) Math.sqrt(1f - a) * (89.99613099964837f - 12.153259533621753f * a + 4.254842010910525f * a2 - 1.0731098035209208f * a3);
        }
        return 180f
                - (float) Math.sqrt(1f + a) * (89.99613099964837f + 12.153259533621753f * a + 4.254842010910525f * a2 + 1.0731098035209208f * a3);
    }

    /**
     * Returns arccosine in turns. This implementation does not return NaN if given an
     * out-of-range input (Math.acos does return NaN), unless the input is NaN.
     *
     * @param a acos is defined only when a is between -1f and 1f, inclusive
     * @return between {@code 0} and {@code 0.5} when a is in the defined range
     */
    public static float acosTurns(float a) {
        float a2 = a * a; // a squared
        float a3 = a * a2; // a cubed
        if (a >= 0f) {
            return (float) (Math.sqrt(1.0 - a) * (0.24998925277680104 - 0.033759055260971525 * a + 0.011819005228947238 * a2 - 0.0029808606756510357 * a3));
        }
        return (float) (0.5 - Math.sqrt(1.0 + a) * (0.24998925277680104 + 0.033759055260971525 * a + 0.011819005228947238 * a2 + 0.0029808606756510357 * a3));
    }

    /**
     * Returns arcsine in radians; less accurate than Math.asin but may be faster. Average error of 0.000028447 radians (0.0016298931
     * degrees), largest error of 0.000067592 radians (0.0038727364 degrees). This implementation does not return NaN if given an
     * out-of-range input (Math.asin does return NaN), unless the input is NaN.
     *
     * @param a asin is defined only when a is between -1.0 and 1.0, inclusive
     * @return between {@code -HALF_PI} and {@code HALF_PI} when a is in the defined range
     */
    public static double asin(double a) {
        double a2 = a * a; // a squared
        double a3 = a * a2; // a cubed
        if (a >= 0.0) {
            return HALF_PI_D
                    - Math.sqrt(1.0 - a) * (1.5707288 - 0.2121144 * a + 0.0742610 * a2 - 0.0187293 * a3);
        }
        return Math.sqrt(1.0 + a) * (1.5707288 + 0.2121144 * a + 0.0742610 * a2 + 0.0187293 * a3) - HALF_PI_D;
    }

    /**
     * Returns arcsine in degrees. This implementation does not return NaN if given an
     * out-of-range input (Math.asin does return NaN), unless the input is NaN.
     *
     * @param a asin is defined only when a is between -1.0 and 1.0, inclusive
     * @return between {@code -90} and {@code 90} when a is in the defined range
     */
    public static double asinDeg(double a) {
        double a2 = a * a; // a squared
        double a3 = a * a2; // a cubed
        if (a >= 0.0) {
            return 90.0
                    - Math.sqrt(1.0 - a) * (89.99613099964837 - 12.153259893949748 * a + 4.2548418824210055 * a2 - 1.0731098432343729 * a3);
        }
        return Math.sqrt(1.0 + a) * (89.99613099964837 + 12.153259893949748 * a + 4.2548418824210055 * a2 + 1.0731098432343729 * a3) - 90.0;
    }

    /**
     * Returns arcsine in turns. This implementation does not return NaN if given an
     * out-of-range input (Math.asin does return NaN), unless the input is NaN.
     * Note that unlike {@link #atan2Turns(double, double)}, this can return negative turn values.
     *
     * @param a asin is defined only when a is between -1.0 and 1.0, inclusive
     * @return between {@code -0.25} and {@code 0.25} when a is in the defined range
     */
    public static double asinTurns(double a) {
        double a2 = a * a; // a squared
        double a3 = a * a2; // a cubed
        if (a >= 0.0) {
            return 0.25 - Math.sqrt(1.0 - a) * (0.24998925277680104 - 0.033759055260971525 * a + 0.011819005228947238 * a2 - 0.0029808606756510357 * a3);
        }
        return Math.sqrt(1.0 + a) * (0.24998925277680104 + 0.033759055260971525 * a + 0.011819005228947238 * a2 + 0.0029808606756510357 * a3) - 0.25;
    }

    /**
     * Returns arccosine in radians; less accurate than Math.acos but may be faster. Average error of 0.00002845 radians (0.0016300649
     * degrees), largest error of 0.000067548 radians (0.0038702153 degrees). This implementation does not return NaN if given an
     * out-of-range input (Math.acos does return NaN), unless the input is NaN.
     *
     * @param a acos is defined only when a is between -1.0 and 1.0, inclusive
     * @return between {@code 0} and {@code PI} when a is in the defined range
     */
    public static double acos(double a) {
        double a2 = a * a; // a squared
        double a3 = a * a2; // a cubed
        if (a >= 0.0) {
            return Math.sqrt(1.0 - a) * (1.5707288 - 0.2121144 * a + 0.0742610 * a2 - 0.0187293 * a3);
        }
        return Math.PI
                - Math.sqrt(1.0 + a) * (1.5707288 + 0.2121144 * a + 0.0742610 * a2 + 0.0187293 * a3);
    }

    /**
     * Returns arccosine in degrees. This implementation does not return NaN if given an
     * out-of-range input (Math.acos does return NaN), unless the input is NaN.
     *
     * @param a acos is defined only when a is between -1.0 and 1.0, inclusive
     * @return between {@code 0} and {@code 180} when a is in the defined range
     */
    public static double acosDeg(double a) {
        double a2 = a * a; // a squared
        double a3 = a * a2; // a cubed
        if (a >= 0.0) {
            return Math.sqrt(1.0 - a) * (89.99613099964837 - 12.153259533621753 * a + 4.254842010910525 * a2 - 1.0731098035209208 * a3);
        }
        return 180.0
                - Math.sqrt(1.0 + a) * (89.99613099964837 + 12.153259533621753 * a + 4.254842010910525 * a2 + 1.0731098035209208 * a3);
    }

    /**
     * Returns arccosine in turns. This implementation does not return NaN if given an
     * out-of-range input (Math.acos does return NaN), unless the input is NaN.
     *
     * @param a acos is defined only when a is between -1.0 and 1.0, inclusive
     * @return between {@code 0} and {@code 0.5} when a is in the defined range
     */
    public static double acosTurns(double a) {
        double a2 = a * a; // a squared
        double a3 = a * a2; // a cubed
        if (a >= 0.0) {
            return Math.sqrt(1.0 - a) * (0.24998925277680104 - 0.033759055260971525 * a + 0.011819005228947238 * a2 - 0.0029808606756510357 * a3);
        }
        return 0.5 - Math.sqrt(1.0 + a) * (0.24998925277680104 + 0.033759055260971525 * a + 0.011819005228947238 * a2 + 0.0029808606756510357 * a3);
    }

    /**
     * Arc tangent approximation with very low error, using an algorithm from the 1955 research study "Approximations for Digital
     * Computers," by RAND Corporation (this is sheet 11's algorithm, which is the fourth-fastest and fourth-least precise). This
     * method is usually about 4x faster than {@link Math#atan(double)}, but is somewhat less precise than Math's implementation.
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUnchecked(double)}, but this method will be correct
     * enough for infinite inputs, and atanUnchecked() will not be.
     *
     * @param i an input to the inverse tangent function; any float is accepted
     * @return an output from the inverse tangent function in radians, from {@code -HALF_PI} to {@code HALF_PI} inclusive
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

    /**
     * Arc tangent approximation with very low error, using an algorithm from the 1955 research study "Approximations for Digital
     * Computers," by RAND Corporation (this is sheet 11's algorithm, which is the fourth-fastest and fourth-least precise).
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUncheckedTurns(double)}, but this method will be correct
     * enough for infinite inputs, and atanUncheckedTurns() will not be.
     *
     * @param i an input to the inverse tangent function; any float is accepted
     * @return an output from the inverse tangent function in turns, from {@code -0.25} to {@code 0.25} inclusive
     * @see #atanUnchecked(double) If you know the input will be finite, you can use atanUnchecked() instead.
     */
    public static float atanTurns(float i) {
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
        return (float) (Math.signum(i) * (0.125
                + (0.15915132390848943 * c - 0.052938669438878753 * c3 + 0.030803398362108523 * c5
                - 0.01853086679887605 * c7 + 0.008380036148199356 * c9 - 0.0018654869189687236 * c11)));
    }


    /**
     * Arc tangent approximation with very low error, using an algorithm from the 1955 research study "Approximations for Digital
     * Computers," by RAND Corporation (this is sheet 11's algorithm, which is the fourth-fastest and fourth-least precise). This
     * method is usually about 4x faster than {@link Math#atan(double)}, but is somewhat less precise than Math's implementation.
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUnchecked(double)}, but this method will be correct
     * enough for infinite inputs, and atanUnchecked() will not be.
     *
     * @param i an input to the inverse tangent function; any double is accepted
     * @return an output from the inverse tangent function in radians, from {@code -HALF_PI} to {@code HALF_PI} inclusive
     * @see #atanUnchecked(double) If you know the input will be finite, you can use atanUnchecked() instead.
     */
    public static double atan(double i) {
        // We use double precision internally, because some constants need double precision.
        // This clips infinite inputs at Double.MAX_VALUE, which still probably becomes infinite
        // again when converted back to double.
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
        return Math.signum(i) * (QUARTER_PI_D
                + (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11));
    }

    /**
     * Arc tangent approximation returning a value measured in positive or negative degrees, using an algorithm from the
     * 1955 research study "Approximations for Digital Computers," by RAND Corporation (this is sheet 11's algorithm,
     * which is the fourth-fastest and fourth-least precise).
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUncheckedDeg(double)}, but this method will be correct
     * enough for infinite inputs, and atanUnchecked() will not be.
     *
     * @param i an input to the inverse tangent function; any double is accepted
     * @return an output from the inverse tangent function in degrees, from {@code -90} to {@code 90} inclusive
     * @see #atanUncheckedDeg(double) If you know the input will be finite, you can use atanUncheckedDeg() instead.
     */
    public static double atanDeg(double i) {
        // We use double precision internally, because some constants need double precision.
        // This clips infinite inputs at Double.MAX_VALUE, which still probably becomes infinite
        // again when converted back to double.
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
        return Math.signum(i) * (45.0
                + (57.2944766070562 * c - 19.05792099799635 * c3 + 11.089223410359068 * c5 - 6.6711120475953765 * c7 + 3.016813013351768 * c9 - 0.6715752908287405 * c11));
    }

    /**
     * Arc tangent approximation with very low error, using an algorithm from the 1955 research study "Approximations for Digital
     * Computers," by RAND Corporation (this is sheet 11's algorithm, which is the fourth-fastest and fourth-least precise).
     * For finite inputs only, you may get a tiny speedup by using {@link #atanUncheckedTurns(double)}, but this method will be correct
     * enough for infinite inputs, and atanUncheckedTurns() will not be.
     *
     * @param i an input to the inverse tangent function; any double is accepted
     * @return an output from the inverse tangent function in turns, from {@code -0.25} to {@code 0.25} inclusive
     * @see #atanUnchecked(double) If you know the input will be finite, you can use atanUnchecked() instead.
     */
    public static double atanTurns(double i) {
        // We use double precision internally, because some constants need double precision.
        // This clips infinite inputs at Double.MAX_VALUE, which still probably becomes infinite
        // again when converted back to double.
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
        return Math.signum(i) * (0.125
                + (0.15915132390848943 * c - 0.052938669438878753 * c3 + 0.030803398362108523 * c5
                - 0.01853086679887605 * c7 + 0.008380036148199356 * c9 - 0.0018654869189687236 * c11));
    }

}
