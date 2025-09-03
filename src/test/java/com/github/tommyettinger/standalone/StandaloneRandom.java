/*
 * Copyright (c) 2025 See AUTHORS file.
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

package com.github.tommyettinger.standalone;

import java.util.Random;

/**
 * A drop-in replacement for {@link Random} that adds few new APIs, but is faster, has better statistical quality, and
 * has a guaranteed longer minimum period (also called cycle length). This is similar to AceRandom in the Juniper
 * library, and uses the same algorithm, but only extends Random, not Juniper's EnhancedRandom class. If you depend on
 * Juniper, you lose nothing from using the EnhancedRandom classes (they also extend Random), but this class doesn't
 * have as many features as Juniper's AceRandom. StandaloneRandom doesn't depend on anything outside the JDK.
 * <br>
 * This does have some small additions to what a Random can provide: There is a constructor that takes all five states
 * verbatim, the states themselves are all public longs, there is a {@link #copy()} method that does what it says. It
 * doesn't use any AtomicLong state like java.util.Random does, and doesn't use the {@code synchronized} keyword
 * anywhere. Even {@link #nextGaussian()} isn't synchronized.
 * <br>
 * Normal usage to get random numbers from this immediately seeds this with one long, using
 * {@link #StandaloneRandom(long)} or {@link #setSeed(long)}. To save the results, you can get the public fields
 * {@link #stateA}, {@link #stateB}, {@link #stateC}, {@link #stateD}, and {@link #stateE}, store them however you need
 * to, and load them later with {@link #StandaloneRandom(long, long, long, long, long)} or by simply setting the public
 * fields again on any StandaloneRandom. No values are specifically disallowed for any state.
 */
public class StandaloneRandom extends Random {
    /**
     * The first state; can be any long. This state is the counter, and it is not affected by the other states.
     */
    public long stateA;
    /**
     * The second state; can be any long.
     */
    public long stateB;
    /**
     * The third state; can be any long.
     */
    public long stateC;
    /**
     * The fourth state; can be any long.
     */
    public long stateD;
    /**
     * The fifth state; can be any long. Returned verbatim on the first call to {@link #nextLong()}.
     */
    public long stateE;

    private static long seedFromMath () {
        return (long)((Math.random() - 0.5) * 0x1p52) ^ (long)((Math.random() - 0.5) * 0x1p64);
    }
    /**
     * Creates a new StandaloneRandom with a random state.
     */
    public StandaloneRandom() {
        stateA = seedFromMath();
        stateB = seedFromMath();
        stateC = seedFromMath();
        stateD = seedFromMath();
        stateE = seedFromMath();
    }

    /**
     * Creates a new StandaloneRandom with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     *
     * @param seed any {@code long} value
     */
    public StandaloneRandom(long seed) {
        setSeed(seed);
    }

    /**
     * Creates a new StandaloneRandom with the given five states; all {@code long} values are permitted.
     * The states will be not be changed at all, which can be useful to reproduce problematic conditions.
     *
     * @param a any {@code long} value
     * @param b any {@code long} value
     * @param c any {@code long} value
     * @param d any {@code long} value
     * @param e any {@code long} value
     */
    public StandaloneRandom(long a, long b, long c, long d, long e) {
        stateA = a;
        stateB = b;
        stateC = c;
        stateD = d;
        stateE = e;
    }
    /**
     * This initializes all 5 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here.
     *
     * @param seed the initial seed; may be any long
     */
    public void setSeed (long seed) {
        seed = (seed ^ 0x1C69B3F74AC4AE35L) * 0x3C79AC492BA7B653L; // an XLCG
        stateA = seed ^ ~0xC6BC279692B5C323L;
        seed ^= seed >>> 32;
        stateB = seed ^ 0xD3833E804F4C574BL;
        seed *= 0xBEA225F9EB34556DL;                               // MX3 unary hash
        seed ^= seed >>> 29;
        stateC = seed ^ ~0xD3833E804F4C574BL;                      // updates are spread across the MX3 hash
        seed *= 0xBEA225F9EB34556DL;
        seed ^= seed >>> 32;
        stateD = seed ^ 0xC6BC279692B5C323L;
        seed *= 0xBEA225F9EB34556DL;
        seed ^= seed >>> 29;
        stateE = seed;
    }

    public long nextLong () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fa + 0x9E3779B97F4A7C15L;
        stateB = fa ^ fe;
        stateC = fb + fd;
        stateD = (fc << 52 | fc >>> 12);
        stateE = fb - fc;
        return fe;
    }

    public int next (int bits) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fa + 0x9E3779B97F4A7C15L;
        stateB = fa ^ fe;
        stateC = fb + fd;
        stateD = (fc << 52 | fc >>> 12);
        stateE = fb - fc;
        return (int) (fe) >>> (32 - bits);
    }

    public int nextInt() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fa + 0x9E3779B97F4A7C15L;
        stateB = fa ^ fe;
        stateC = fb + fd;
        stateD = (fc << 52 | fc >>> 12);
        stateE = fb - fc;
        return (int) (fe);
    }

    public int nextInt (int bound) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        final int result = (int)(bound * (fe & 0xFFFFFFFFL) >> 32) & ~(bound >> 31);
        stateA = fa + 0x9E3779B97F4A7C15L;
        stateB = fa ^ fe;
        stateC = fb + fd;
        stateD = (fc << 52 | fc >>> 12);
        stateE = fb - fc;
        return result;
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code int} value between the
     * specified {@code innerBound} (inclusive) and the specified {@code outerBound}
     * (exclusive). If {@code outerBound} is less than or equal to {@code innerBound},
     * this always returns {@code innerBound}.
     *
     * <br> If outerBound is less than innerBound here, this simply returns innerBound.
     *
     * @param innerBound the inclusive inner bound; may be any int, allowing negative
     * @param outerBound the exclusive outer bound; must be greater than innerBound (otherwise this returns innerBound)
     * @return a pseudorandom int between innerBound (inclusive) and outerBound (exclusive)
     */
    public int nextInt(int innerBound, int outerBound) {
        return (int) (innerBound + ((((outerBound - innerBound) & 0xFFFFFFFFL) * (nextLong() & 0xFFFFFFFFL) >>> 32) & ~((long) outerBound - (long) innerBound >> 63)));
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code long} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.  The general contract of
     * {@code nextLong} is that one {@code long} value in the specified range
     * is pseudorandomly generated and returned.  All {@code bound} possible
     * {@code long} values are produced with (approximately) equal
     * probability, though there is a small amount of bias depending on the bound.
     *
     * <br> Note that this advances the state by the same amount as a single call to
     * {@link #nextLong()}. This will also advance the state if {@code bound} is 0
     * or negative, so usage with a variable bound will advance the state reliably.
     *
     * @param bound the upper bound (exclusive). If negative or 0, this always returns 0.
     * @return the next pseudorandom, uniformly distributed {@code long}
     * value between zero (inclusive) and {@code bound} (exclusive)
     * from this random number generator's sequence
     */
    public long nextLong(long bound) {
        return nextLong(0L, bound);
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code long} value between the
     * specified {@code innerBound} (inclusive) and the specified {@code outerBound}
     * (exclusive). If {@code outerBound} is less than or equal to {@code innerBound},
     * this always returns {@code innerBound}.
     *
     * @param inner the inclusive inner bound; may be any long, allowing negative
     * @param outer the exclusive outer bound; must be greater than innerBound (otherwise this returns innerBound)
     * @return a pseudorandom long between innerBound (inclusive) and outerBound (exclusive)
     */
    public long nextLong(long inner, long outer) {
        final long rand = nextLong();
        if (inner >= outer)
            return inner;
        final long bound = outer - inner;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        final long randHigh = (rand >>> 32);
        final long boundHigh = (bound >>> 32);
        return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
    }

    public boolean nextBoolean() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fa + 0x9E3779B97F4A7C15L;
        stateB = fa ^ fe;
        stateC = fb + fd;
        stateD = (fc << 52 | fc >>> 12);
        stateE = fb - fc;
        return fe < 0L;
    }

    public float nextFloat () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        final float result = (fe >>> 40) * 0x1p-24f;
        stateA = fa + 0x9E3779B97F4A7C15L;
        stateB = fa ^ fe;
        stateC = fb + fd;
        stateD = (fc << 52 | fc >>> 12);
        stateE = fb - fc;
        return result;
    }

    public double nextDouble () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        final double result = (fe >>> 11) * 0x1.0p-53;
        stateA = fa + 0x9E3779B97F4A7C15L;
        stateB = fa ^ fe;
        stateC = fb + fd;
        stateD = (fc << 52 | fc >>> 12);
        stateE = fb - fc;
        return result;
    }
    // constants used by probitL() and probitD()
    private static final double
            a0 = 0.195740115269792,
            a1 = -0.652871358365296,
            a2 = 1.246899760652504,
            b0 = 0.155331081623168,
            b1 = -0.839293158122257,
            c3 = -1.000182518730158122,
            c0 = 16.682320830719986527,
            c1 = 4.120411523939115059,
            c2 = 0.029814187308200211,
            d0 = 7.173787663925508066,
            d1 = 8.759693508958633869;

    /**
     * A double-precision probit() approximation that takes any long and returns an
     * approximately-Gaussian-distributed double between -38.467454509186325 and 38.467454509186325 .
     * The function maps the most negative inputs to the most negative outputs, the most positive inputs to the most
     * positive outputs, and inputs near 0 to outputs near 0.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param l may be any long, though very close longs will not produce different results
     * @return an approximately-Gaussian-distributed double between -38.467454509186325 and 38.467454509186325
     */
    public static double probitL(long l) {
        /* 5.421010862427522E-20 is 0x1p-64 or Math.pow(2, -64) */
        final double h = l * 5.421010862427522E-20;
        if(-0.4535 > h) {
            /* 0.4.9E-324 is Double.MIN_VALUE, the smallest non-zero double */
            final double r = Math.sqrt(Math.log(0.5 + h + 4.9E-324) * -2f);
            return c3 * r + c2 + (c1 * r + c0) / (r * (r + d1) + d0);
        } else if(0.4535 < h) {
            /* 0.4.9E-324 is Double.MIN_VALUE, the smallest non-zero double */
            final double r = Math.sqrt(Math.log(0.5 - h + 4.9E-324) * -2f);
            return -c3 * r - c2 - (c1 * r + c0) / (r * (r + d1) + d0);
        } else {
            final double r = h * h;
            return h * (a2 + (a1 * r + a0) / (r * (r + b1) + b0));
        }
    }

    /**
     * Calls {@link #probitL(long)} on {@link #nextLong()}. Not synchronized, because it doesn't need to be.
     *
     * @return a normal-distributed double with a mean of 0.0 and a standard deviation of 1.0
     */
    public double nextGaussian() {
        return probitL(nextLong());
    }

    /**
     * Calls {@link #probitL(long)} on {@link #nextLong()}, and adjusts that for the requested standard deviation and
     * mean. Not synchronized, because it doesn't need to be. This uses the same algorithm as {@link #nextGaussian()},
     * unlike the version in java.util.Random in Java 17 and later.
     *
     * @return a normal-distributed double with the requested mean and standard deviation
     */
    public double nextGaussian(double mean, double stdDev) {
        return mean + stdDev * nextGaussian();
    }

    @Override
    public void nextBytes (byte[] bytes) {
        for (int i = 0; i < bytes.length; ) {
            for (long r = nextLong(), n = Math.min(bytes.length - i, 8); n-- > 0; r >>>= 8) {
                bytes[i++] = (byte)r;
            }
        }
    }

    public StandaloneRandom copy() {
        return new StandaloneRandom(stateA, stateB, stateC, stateD, stateE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StandaloneRandom random = (StandaloneRandom) o;

        if (stateA != random.stateA) return false;
        if (stateB != random.stateB) return false;
        if (stateC != random.stateC) return false;
        if (stateD != random.stateD) return false;
        return stateE == random.stateE;
    }

    @Override
    public int hashCode() {
        int result = (int) (stateA ^ (stateA >>> 32));
        result = 31 * result + (int) (stateB ^ (stateB >>> 32));
        result = 31 * result + (int) (stateC ^ (stateC >>> 32));
        result = 31 * result + (int) (stateD ^ (stateD >>> 32));
        result = 31 * result + (int) (stateE ^ (stateE >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "StandaloneRandom{" +
                "stateA=" + stateA +
                ", stateB=" + stateB +
                ", stateC=" + stateC +
                ", stateD=" + stateD +
                ", stateE=" + stateE +
                '}';
    }
}
