/*
 * Copyright (c) 2023 See AUTHORS file.
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

import java.util.Random;

/**
 * A drop-in replacement for {@link Random} that adds few new APIs, but is faster, has better statistical quality, and
 * has a guaranteed longer minimum period (also called cycle length). This is similar to PasarRandom in the Juniper
 * library, and uses the same algorithm, but only extends Random, not Juniper's EnhancedRandom class. If you depend on
 * Juniper, you lose nothing from using the EnhancedRandom classes (they also extend Random), but this class doesn't
 * have as many features as Juniper's PasarRandom. AlternateRandom doesn't depend on anything outside the JDK, though,
 * so it fits easily as a small addition into digital.
 * <br>
 * This is mostly here to speed up the default shuffling methods in {@link ArrayTools}, and to allow those to produce
 * many more possible shuffles. The number of possible shuffles that can be produced by that type of algorithm depends
 * on the number of possible states of the random number generator; that number here is 2 to the 320, or
 * 2135987035920910082395021706169552114602704522356652769947041607822219725780640550022962086936576 (but the actual
 * amount in practice is smaller, to a hard minimum of 2 to the 64, or 18446744073709551616).
 * <br>
 * This does have some small additions to what a Random can provide: There is a constructor that takes all five states
 * verbatim, the states themselves are all public longs, there is a {@link #copy()} method that does what it says, and
 * there are {@link #serializeToString()} and {@link #deserializeFromString(String)} methods to read and write the state
 * using {@link Base#SIMPLE64}. The serialization format is very terse, just 11 chars per state, in A through E order,
 * appended one after the next without spaces. This format is not at all compatible with Juniper. Having all this allows
 * AlternateRandom to be correctly handled by jdkgdxds-interop for libGDX Json serialization, and kryo-more for Kryo.
 */
public class AlternateRandom extends Random {
    /**
     * The first state; can be any long.
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
     * The fourth state; can be any long. This state is the counter, and it is not affected by the other states.
     */
    public long stateD;
    /**
     * The fifth state; can be any long.
     */
    public long stateE;

    private static long seedFromMath () {
        return (long)((Math.random() - 0.5) * 0x1p52) ^ (long)((Math.random() - 0.5) * 0x1p64);
    }
    /**
     * Creates a new AlternateRandom with a random state.
     */
    public AlternateRandom() {
        stateA = seedFromMath();
        stateB = seedFromMath();
        stateC = seedFromMath();
        stateD = seedFromMath();
        stateE = seedFromMath();
    }

    /**
     * Creates a new AlternateRandom with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     *
     * @param seed any {@code long} value
     */
    public AlternateRandom(long seed) {
        setSeed(seed);
    }

    /**
     * Creates a new AlternateRandom with the given five states; all {@code long} values are permitted.
     * The states will be not be changed at all, which can be useful to reproduce problematic conditions.
     *
     * @param a any {@code long} value
     * @param b any {@code long} value
     * @param c any {@code long} value
     * @param d any {@code long} value
     * @param e any {@code long} value
     */
    public AlternateRandom(long a, long b, long c, long d, long e) {
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
        stateA = seed;
        seed ^= seed >>> 32;
        seed *= 0xbea225f9eb34556dL;
        seed ^= seed >>> 29;
        seed *= 0xbea225f9eb34556dL;
        seed ^= seed >>> 32;
        seed *= 0xbea225f9eb34556dL;
        seed ^= seed >>> 29;
        stateB = seed;
        stateC = seed ^ ~0xC6BC279692B5C323L;
        stateD = ~seed;
        stateE = seed ^ 0xC6BC279692B5C323L;
    }

    public long nextLong () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        return stateE = fa ^ fc;
    }

    public int next (int bits) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        return (int) (stateE = fa ^ fc) >>> (32 - bits);
    }

    public int nextInt() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        return (int) (stateE = fa ^ fc);
    }

    public int nextInt (int bound) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        return (int)(bound * ((stateE = fa ^ fc) & 0xFFFFFFFFL) >> 32) & ~(bound >> 31);
    }

    public boolean nextBoolean() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        return (stateE = fa ^ fc) < 0L;
    }

    public float nextFloat () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        return ((stateE = fa ^ fc) >>> 40) * 0x1p-24f;
    }

    public double nextDouble () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        return ((stateE = fa ^ fc) >>> 11) * 0x1.0p-53;
    }

    public double nextGaussian() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long fe = stateE;
        stateA = fe * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + fd;
        stateD = fd + 0x9E3779B97F4A7C15L;
        final long bits = (stateE = fa ^ fc);
        return Distributor.normal(bits);
    }

    @Override
    public void nextBytes (byte[] bytes) {
        for (int i = 0; i < bytes.length; ) {
            for (long r = nextLong(), n = Math.min(bytes.length - i, 8); n-- > 0; r >>>= 8) {
                bytes[i++] = (byte)r;
            }
        }
    }

    /**
     * Produces a String that holds the entire state of this AlternateRandom. You can recover this state from such a
     * String by calling {@link #deserializeFromString(String)} on any AlternateRandom, which will set that
     * AlternateRandom's state. This does not serialize any fields inherited from {@link Random}, so the methods that
     * use Random's side entirely, such as the Stream methods, won't be affected if this state is loaded.
     * @return a String holding the current state of this AlternateRandom, to be loaded by {@link #deserializeFromString(String)}
     */
    public String serializeToString() {
        StringBuilder sb = new StringBuilder(55);
        Base.SIMPLE64.appendUnsigned(sb, stateA);
        Base.SIMPLE64.appendUnsigned(sb, stateB);
        Base.SIMPLE64.appendUnsigned(sb, stateC);
        Base.SIMPLE64.appendUnsigned(sb, stateD);
        Base.SIMPLE64.appendUnsigned(sb, stateE);
        return sb.toString();
    }

    /**
     * Given a String produced by {@link #serializeToString()}, this sets the state of this AlternateRandom to the state
     * stored in that String.This does not deserialize any fields inherited from {@link Random}, so the methods that
     * use Random's side entirely, such as the Stream methods, won't be affected by this state.
     * @param data a String produced by {@link #serializeToString()}
     * @return this AlternateRandom, after its state has been loaded from the given String
     */
    public AlternateRandom deserializeFromString(String data) {
        if(data == null || data.length() < 55) return this;
        stateA = Base.SIMPLE64.readLong(data, 0, 11);
        stateB = Base.SIMPLE64.readLong(data, 11, 22);
        stateC = Base.SIMPLE64.readLong(data, 22, 33);
        stateD = Base.SIMPLE64.readLong(data, 33, 44);
        stateE = Base.SIMPLE64.readLong(data, 44, 55);
        return this;
    }

    public AlternateRandom copy() {
        return new AlternateRandom(stateA, stateB, stateC, stateD, stateE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlternateRandom random = (AlternateRandom) o;

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
        return "AlternateRandom{" +
                "stateA=" + stateA +
                ", stateB=" + stateB +
                ", stateC=" + stateC +
                ", stateD=" + stateD +
                ", stateE=" + stateE +
                '}';
    }
}
