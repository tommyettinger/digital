package com.github.tommyettinger.digital;

import java.util.Random;

/**
 * A drop-in replacement for {@link Random} that adds no new APIs, but is faster, has better statistical quality, and
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
 */
public class AlternateRandom extends Random {
    /**
     * The first state; can be any long.
     */
    private long stateA;
    /**
     * The second state; can be any long.
     */
    private long stateB;
    /**
     * The third state; can be any long.
     */
    private long stateC;
    /**
     * The fourth state; can be any long. This state is the counter, and it is not affected by the other states.
     */
    private long stateD;
    /**
     * The fifth state; can be any long.
     */
    private long stateE;

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
        long u = (stateE = fa ^ fc);
        final long c = Long.bitCount(u) - 32L << 32;
        u *= 0xC6AC29E4C6AC29E5L;
        return 0x1.fb760cp-35 * (c + (u & 0xFFFFFFFFL) - (u >>> 32));
    }

    @Override
    public void nextBytes (byte[] bytes) {
        for (int i = 0; i < bytes.length; ) {
            for (long r = nextLong(), n = Math.min(bytes.length - i, 8); n-- > 0; r >>>= 8) {
                bytes[i++] = (byte)r;
            }
        }
    }
}
