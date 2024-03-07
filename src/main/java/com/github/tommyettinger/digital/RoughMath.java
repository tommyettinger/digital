/*=====================================================================*
 *                   Copyright (C) 2011 Paul Mineiro                   *
 * All rights reserved.                                                *
 *                                                                     *
 * Redistribution and use in source and binary forms, with             *
 * or without modification, are permitted provided that the            *
 * following conditions are met:                                       *
 *                                                                     *
 *     * Redistributions of source code must retain the                *
 *     above copyright notice, this list of conditions and             *
 *     the following disclaimer.                                       *
 *                                                                     *
 *     * Redistributions in binary form must reproduce the             *
 *     above copyright notice, this list of conditions and             *
 *     the following disclaimer in the documentation and/or            *
 *     other materials provided with the distribution.                 *
 *                                                                     *
 *     * Neither the name of Paul Mineiro nor the names                *
 *     of other contributors may be used to endorse or promote         *
 *     products derived from this software without specific            *
 *     prior written permission.                                       *
 *                                                                     *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND              *
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,         *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES               *
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE             *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER               *
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                 *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES            *
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE           *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR                *
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF          *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT           *
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY              *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE             *
 * POSSIBILITY OF SUCH DAMAGE.                                         *
 *                                                                     *
 * Contact: Paul Mineiro <paul@mineiro.com>                            *
 *=====================================================================*/

package com.github.tommyettinger.digital;

/**
 * Fast approximations of math methods that reach roughly the right answer most of the time.
 * Ported from <a href="https://code.google.com/archive/p/fastapprox/">fastapprox</a>, which is open source
 * under the New BSD License.
 */
public final class RoughMath {
    private RoughMath() {
    }

    // EXPONENTIAL AND LOGARITHMIC FUNCTIONS

    /**
     * Approximates {@code Math.pow(2.0, p)} with single-precision, somewhat roughly.
     * @param p the power to raise 2 to; can be any float
     * @return an approximation of 2 raised to the p power; can be any float greater than 0
     */
    public static float pow2Rough (float p)
    {
        final float clip = Math.max(-126.0f, p);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return BitConversion.intBitsToFloat((int) ( (1 << 23) * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z)));
    }
    /**
     * Approximates {@code Math.pow(2.0, p)} with single-precision, very roughly.
     * @param p the power to raise 2 to; can be any float
     * @return an approximation of 2 raised to the p power; can be any float greater than 0
     */
    public static float pow2Rougher (float p)
    {
        return BitConversion.intBitsToFloat( (int)((1 << 23) * (Math.max(-126.0f, p) + 126.94269504f)));
    }

    /**
     * Approximates {@code Math.pow(Math.E, p)} with single-precision, somewhat roughly.
     * @param p the power to raise E to; can be any float
     * @return an approximation of E raised to the p power; can be any float greater than 0
     */
    public static float expRough (float p)
    {
        p *= 1.442695040f;
        final float clip = Math.max(-126.0f, p);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return BitConversion.intBitsToFloat((int) ( (1 << 23) * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z)));
    }

    /**
     * Approximates {@code Math.pow(Math.E, p)} with single-precision, very roughly.
     * @param p the power to raise E to; can be any float
     * @return an approximation of E raised to the p power; can be any float greater than 0
     */
    public static float expRougher (float p)
    {
        return BitConversion.intBitsToFloat( (int)((1 << 23) * (Math.max(-126.0f, 1.442695040f * p) + 126.94269504f)));
    }

    /**
     * Approximates the logarithm of {@code x} with base 2, using single-precision, somewhat roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base 2; can be any float
     */
    public static float log2Rough (float x)
    {
        final int vx = BitConversion.floatToIntBits(x);
        final float mx = BitConversion.intBitsToFloat((vx & 0x007FFFFF) | 0x3f000000);
        return vx * 1.1920928955078125e-7f - 124.22551499f - 1.498030302f * mx - 1.72587999f / (0.3520887068f + mx);
    }

    /**
     * Approximates the natural logarithm of {@code x} (that is, with base E), using single-precision, somewhat roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base E; can be any float
     */
    public static float logRough (float x)
    {
        final int vx = BitConversion.floatToIntBits(x);
        final float mx = BitConversion.intBitsToFloat((vx & 0x007FFFFF) | 0x3f000000);
        return (vx * 1.1920928955078125e-7f - 124.22551499f - 1.498030302f * mx - 1.72587999f / (0.3520887068f + mx)) * 0.69314718f;
    }

    /**
     * Approximates the logarithm of {@code x} with base 2, using single-precision, very roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base 2; can be any float
     */
    public static float log2Rougher (float x){
        return BitConversion.floatToIntBits(x) * 1.1920928955078125e-7f - 126.94269504f;
    }

    /**
     * Approximates the natural logarithm of {@code x} (that is, with base E), using single-precision, very roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base E; can be any float
     */
    public static float logRougher (float x){
        return Float.floatToIntBits(x) * 8.2629582881927490e-8f - 87.989971088f;
    }

    // HYPERBOLIC TRIGONOMETRIC FUNCTIONS

    /**
     * Approximates {@code Math.sinh(p)}, somewhat roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to sinh; can be any float
     * @return an approximation of the hyperbolic sine of p; can be any float
     */
    public static float sinhRough (float p)
    {
        return 0.5f * (expRough (p) - expRough (-p));
    }

    /**
     * Approximates {@code Math.sinh(p)}, very roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to sinh; can be any float
     * @return an approximation of the hyperbolic sine of p; can be any float
     */
    public static float sinhRougher (float p)
    {
        return 0.5f * (expRougher (p) - expRougher (-p));
    }

    /**
     * Approximates {@code Math.cosh(p)}, somewhat roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to cosh; can be any float
     * @return an approximation of the hyperbolic cosine of p; can be any float greater than or equal to 1
     */
    public static float coshRough (float p)
    {
        return 0.5f * (expRough (p) + expRough (-p));
    }

    /**
     * Approximates {@code Math.cosh(p)}, very roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to cosh; can be any float
     * @return an approximation of the hyperbolic cosine of p; can be any float greater than or equal to 1
     */
    public static float coshRougher (float p)
    {
        return 0.5f * (expRougher (p) + expRougher (-p));
    }

    /**
     * Approximates {@code Math.tanh(p)}, somewhat roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to tanh; can be any float
     * @return an approximation of the hyperbolic tangent of p; between -1 and 1 inclusive
     */
    public static float tanhRough (float p)
    {
        return -1.0f + 2.0f / (1.0f + expRough (-2.0f * p));
    }

    /**
     * Approximates {@code Math.tanh(p)}, very roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to tanh; can be any float
     * @return an approximation of the hyperbolic tangent of p; between -1 and 1 inclusive
     */
    public static float tanhRougher (float p)
    {
        return -1.0f + 2.0f / (1.0f + expRougher (-2.0f * p));
    }

    // LOGISTIC FUNCTION

    /**
     * Approximates the <a href="https://en.wikipedia.org/wiki/Logistic_function">standard logistic function</a>, somewhat roughly.
     * This is also called the sigmoid function, or expit. It is the same as {@link #tanhRough(float)}, scaled, with an offset.
     * @param x the parameter to the standard logistic function; can be any float
     * @return an approximation of the logistic function of x; between -1 and 1 inclusive
     */
    public static float logisticRough (float x)
    {
        return 1.0f / (1.0f + expRough (-x));
    }

    /**
     * Approximates the <a href="https://en.wikipedia.org/wiki/Logistic_function">standard logistic function</a>, very roughly.
     * This is also called the sigmoid function, or expit. It is the same as {@link #tanhRough(float)}, scaled, with an offset.
     * @param x the parameter to the standard logistic function; can be any float
     * @return an approximation of the logistic function of x; between -1 and 1 inclusive
     */
    public static float logisticRougher (float x)
    {
        return 1.0f / (1.0f + expRougher (-x));
    }

    // TRIGONOMETRIC FUNCTIONS

    public static final float FOUR_OVER_PI = 1.2732395447351627f;
    public static final float FOUR_OVER_PI_SQUARED = 0.40528473456935109f;
    public static final float PI2 = 6.2831853071795865f;
    public static final float PI2_INVERSE = 0.15915494309189534f;

    /**
     * Approximates {@code Math.sin(x)} in the domain between {@code -PI} and {@code PI}, somewhat roughly.
     *
     * @see TrigTools#sin(float)
     * @param x the argument to sin; must be between {@code -PI} and {@code PI}
     * @return an approximation of the sine of x; between -1 and 1 inclusive
     */
    public static float sinRoughLimited (float x)
    {
        final float q = 0.78444488374548933f;
        int p = 0x3E5086D7; // bits of 0.20363937680730309f;
        int r = 0x3C77CE9A; // bits of 0.015124940802184233f;
        int s = 0x3B533217; // bits of 0.0032225901625579573f;
        int vx = BitConversion.floatToIntBits(x);
        int sign = vx & 0x80000000;
        vx &= 0x7FFFFFFF;

        float approx = FOUR_OVER_PI * x - FOUR_OVER_PI_SQUARED * x * BitConversion.intBitsToFloat(vx);
        float approxSquared = approx * approx;

        p |= sign;
        r |= sign;
        s |= sign;

        return q * approx + approxSquared * (BitConversion.intBitsToFloat(p) + approxSquared * (BitConversion.intBitsToFloat(r) + approxSquared * BitConversion.intBitsToFloat(s)));
    }

    /**
     * Approximates {@code Math.sin(x)} over its full domain, somewhat roughly.
     *
     * @see TrigTools#sin(float)
     * @param x the argument to sin; can be any float
     * @return an approximation of the sine of x; between -1 and 1 inclusive
     */
    public static float sinRough (float x)
    {
        final float half = (x < 0) ? -0.5f : 0.5f;
        return sinRoughLimited ((half + (int)(x * PI2_INVERSE)) * PI2 - x);
    }
}
