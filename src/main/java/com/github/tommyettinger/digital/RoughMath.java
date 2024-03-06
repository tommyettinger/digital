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

    /**
     * Approximates {@code Math.pow(2.0, p)} with single-precision, somewhat roughly.
     * @param p the power to raise 2 to
     * @return an approximation of 2 raised to the p power
     */
    public static float pow2Rough (float p)
    {
        final float clip = Math.max(-126.0f, p);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return BitConversion.intBitsToFloat((int) ( (1 << 23) * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z)));
    }
    /**
     * Approximates {@code Math.pow(2.0, p)} with single-precision, very roughly.
     * @param p the power to raise 2 to
     * @return an approximation of 2 raised to the p power
     */
    public static float pow2Rougher (float p)
    {
        return BitConversion.intBitsToFloat( (int)((1 << 23) * (Math.max(-126.0f, p) + 126.94269504f)));
    }

    /**
     * Approximates {@code Math.pow(Math.E, p)} with single-precision, somewhat roughly.
     * @param p the power to raise E to
     * @return an approximation of E raised to the p power
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
     * @param p the power to raise E to
     * @return an approximation of E raised to the p power
     */
    public static float expRougher (float p)
    {
        return BitConversion.intBitsToFloat( (int)((1 << 23) * (Math.max(-126.0f, 1.442695040f * p) + 126.94269504f)));
    }

    /**
     * Approximates the logarithm of {@code x} with base 2, using single-precision, somewhat roughly.
     * @param x the argument to the logarithm
     * @return an approximation of the logarithm of x with base 2
     */
    public static float log2Rough (float x)
    {
        final int vx = Float.floatToIntBits(x);
        final float mx = Float.intBitsToFloat((vx & 0x007FFFFF) | 0x3f000000);
        return vx * 1.1920928955078125e-7f - 124.22551499f - 1.498030302f * mx - 1.72587999f / (0.3520887068f + mx);
    }

    /**
     * Approximates the natural logarithm of {@code x} (that is, with base E), using single-precision, somewhat roughly.
     * @param x the argument to the logarithm
     * @return an approximation of the logarithm of x with base E
     */
    public static float logRough (float x)
    {
        final int vx = Float.floatToIntBits(x);
        final float mx = Float.intBitsToFloat((vx & 0x007FFFFF) | 0x3f000000);
        return (vx * 1.1920928955078125e-7f - 124.22551499f - 1.498030302f * mx - 1.72587999f / (0.3520887068f + mx)) * 0.69314718f;
    }

}
