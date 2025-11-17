/* ******************************************************************************
 * Copyright 2020 See AUTHORS file.
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

import static com.badlogic.gdx.math.MathUtils.PI;

/**
 * Math helper functions.
 */
public final class GtMathUtils {
    private GtMathUtils() {
    }

    private static final int ATAN2_SIZE = 10318;
    // ATAN2_SIZE was optimized for LCH color interpolation accuracy. ColorConversion is off by as much as 1/255
    // for any of the three color channels. This is the minimum look-up table size to avoid increasing this maximum
    // error in LCH ColorConversion.
    private static final float[] ATAN2 = new float[ATAN2_SIZE + 1];
    static {
        for (int i = 0; i <= ATAN2_SIZE; i++) {
            ATAN2[i] = (float) Math.atan2((double) i / ATAN2_SIZE, 1.0);
        }
    }

    /**
     * Fast atan2, based on a look-up-table. More accurate than MathUtils.atan2. Average error 0.0004 radians
     * (0.022 degrees), largest error of 0.00010 radians (0.056 degrees).
     * <br>
     * EDITOR'S NOTE: the above largest error is wrong because it can't be less than the average error.
     * It is probably 0.001 radians, which matches the degrees given.
     * <p>
     * Thanks to Icecore on JavaGaming.org for the algorithm, and to mooman219 on JavaGaming.org for benchmarking it.
     * These accuracy values are the same as mooman219's despite the much smaller look-up table.
     * <br>
     * <a href="https://github.com/CypherCove/gdx-tween/blob/799dba160535995cf08374ab5601cdff68aeb78f/gdx-tween/src/main/java/com/cyphercove/gdxtween/math/GtMathUtils.java">From here.</a>
     * Not currently used, with good reason it turns out! This method is only about 5% faster than Math.atan2(), but has
     * between 700 and 2000 times more error than Math gets from converting to and from float.
     * @param y arctan numerator
     * @param x arctan denominator
     * @return A fast approximate atan2 angle in radians.
     */
    static public float atan2Gt(float y, float x) {
        if (y < 0) {
            if (x < 0) {
                if (y < x) {
                    return -PI / 2 - ATAN2[(int) (x / y * ATAN2_SIZE)];
                } else {
                    return -PI + ATAN2[(int) (y / x * ATAN2_SIZE)];
                }
            } else {
                y = -y;
                if (y > x) {
                    return -PI / 2 + ATAN2[(int) (x / y * ATAN2_SIZE)];
                } else {
                    return -ATAN2[(int) (y / x * ATAN2_SIZE)];
                }
            }
        } else {
            if (x < 0) {
                x = -x;
                if (y > x) {
                    return PI / 2 + ATAN2[(int) (x / y * ATAN2_SIZE)];
                } else {
                    return PI - ATAN2[(int) (y / x * ATAN2_SIZE)];
                }
            } else {
                if (y > x) {
                    return PI / 2 - ATAN2[(int) (x / y * ATAN2_SIZE)];
                } else {
                    return ATAN2[(int) (y / x * ATAN2_SIZE)];
                }
            }
        }
    }

    /**
     * Fast approximate atan2. Significantly more accurate than {@link com.badlogic.gdx.math.MathUtils#atan2(float, float)}..
     * <p>
     * Credit to user imuli on dsprelated.com for the algorithm.
     * <br>
     * <a href="https://github.com/CypherCove/gdx-tween/blob/master/gdxtween/src/main/java/com/cyphercove/gdxtween/math/GtMath.java">From here.</a>
     * Used in the current version of gdx-tween, but references a much older version of GDX MathUtils.
     *
     * @param y arctan numerator
     * @param x arctan denominator
     * @return A fast approximate atan2 angle in radians.
     */
    public static float atan2imuli(float y, float x) {
        float ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        float z = invert ? ax / ay : ay / ax;
        z = ((((0.141499f * z) - 0.343315f) * z - 0.016224f) * z + 1.003839f) * z - 0.000158f;
        if (invert) z = 1.5707963267948966f - z;
        if (x < 0) z = 3.141592653589793f - z;
        return Math.copySign(z, y);
    }
}
