/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.tommyettinger.digital.v037;

import java.util.Iterator;
import java.util.List;

import static com.github.tommyettinger.digital.BitConversion.doubleToRawLongBits;
import static com.github.tommyettinger.digital.BitConversion.floatToRawIntBits;

import static com.github.tommyettinger.digital.MathTools.EPSILON;
import static com.github.tommyettinger.digital.MathTools.EPSILON_D;

/**
 * 64-bit and 32-bit hashing functions that we can rely on staying the same cross-platform.
 * This uses a family of algorithms all based on Wang Yi's wyhash, but using at most 64-bit
 * math. Wyhash was designed foremost for speed and also general-purpose usability, but not
 * cryptographic security. The functions here pass the stringent SMHasher test battery,
 * including the "bad seeds" test that wyhash itself fails. This is based on an early version
 * of wyhash,
 * <a href="https://github.com/wangyi-fudan/wyhash/blob/version_1/wyhash.h">source here</a>,
 * but has diverged significantly.
 * <br>
 * This provides an object-based API and a static API, where a OldHasher object is
 * instantiated with a seed, and the static methods take a seed as their first argument.
 * The hashes this returns are always 0 when given null to hash. Arrays with
 * identical elements of identical types will hash identically. Arrays with identical
 * numerical values but different types will sometimes hash differently. This class
 * always provides 64-bit hashes via hash64() and 32-bit hashes via hash().
 * The hash64() and hash() methods use 64-bit math even when producing
 * 32-bit hashes, for GWT reasons. GWT doesn't have the same behavior as desktop and
 * Android applications when using ints because it treats ints mostly like doubles,
 * sometimes, due to it using JavaScript. If we use mainly longs, though, GWT emulates
 * the longs with a more complex technique behind-the-scenes, that behaves the same on
 * the web as it does on desktop or on a phone. Since OldHasher is supposed to be stable
 * cross-platform, this is the way we need to go, despite it being slightly slower.
 * <br>
 * This class also provides static {@link #randomize1(long)}, {@link #randomize2(long)}, and
 * {@link #randomize3(long)} methods, which are unary hashes (hashes of one item, a number) with variants such as
 * {@link #randomize1Bounded(long, int)} and {@link #randomize2Float(long)}. The randomize1()
 * methods are faster but more sensitive to patterns in their input; they are meant to
 * work well on sequential inputs, like 1, 2, 3, etc. with relatively-short sequences
 * (ideally under a million, but if statistical quality isn't a concern, they can handle any
 * length). The randomize2() methods are more-involved, but should be able to handle most
 * kinds of input pattern across even rather-large sequences (billions) while returning
 * random results. The randomize3() methods are likely complete overkill for many cases, but
 * provide extremely strong randomization for any possible input pattern, using the MX3 unary
 * hash with an extra XOR at the beginning to prevent a fixed point at 0.
 * <br>
 * There are also 428 predefined instances of OldHasher that you can either
 * select from the array {@link #predefined} or select by hand, such as {@link #omega}.
 * The predefined instances are named after the 24 greek letters, then the same letters
 * with a trailing underscore, then
 * <a href="https://en.wikipedia.org/wiki/List_of_demons_in_the_Ars_Goetia">72 names of demons from the Ars Goetia</a>,
 * then the names of those demons with trailing underscores, then the names of 118 chemical elements, then those names
 * with trailing underscores. The greek letters are traditional, the demons are perfectly fitting for video games, and
 * chemistry has been closely linked with computing for many years now.
 *
 * @author Tommy Ettinger
 */
public class OldHasher {
    /**
     * The seed used by all non-static hash() and hash64() methods in this class (the methods that don't take a seed).
     * You can create many different OldHasher objects, all with different seeds, and get very different hashes as a result
     * of any calls on them. Because making this field hidden in some way doesn't meaningfully contribute to security,
     * and only makes it harder to use this class, {@code seed} is public (and final, so it can't be accidentally
     * modified, but still can if needed via reflection).
     */
    public final long seed;

    /**
     * Creates a new OldHasher seeded, arbitrarily, with the constant 0xC4CEB9FE1A85EC53L, or -4265267296055464877L .
     */
    public OldHasher() {
        this.seed = 0xC4CEB9FE1A85EC53L;
    }

    /**
     * Initializes this OldHasher with the given seed, verbatim; it is recommended to use {@link #randomize3(long)} on the
     * seed if you don't know if it is adequately-random. If the seed is the same for two different OldHasher instances and
     * they are given the same inputs, they will produce the same results. If the seed is even slightly different, the
     * results of the two Hashers given the same input should be significantly different.
     *
     * @param seed a long that will be used to change the output of hash() and hash64() methods on the new OldHasher
     */
    public OldHasher(long seed) {
        this.seed = seed;
    }

    /**
     * Fast static randomizing method that takes its state as a parameter; state is expected to change between calls to
     * this. It is recommended that you use {@code randomize1(++state)} or {@code randomize1(--state)}
     * to produce a sequence of different numbers, and you may have slightly worse quality with increments or decrements
     * other than 1. All longs are accepted by this method, and all longs can be produced. Passing 0 here does not
     * cause this to return 0.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize1(long state) {
        return (state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * Mid-quality static randomizing method that takes its state as a parameter; state is expected to change between
     * calls to this. It is suggested that you use {@code DiverRNG.randomize(++state)} or
     * {@code DiverRNG.randomize(--state)} to produce a sequence of different numbers, but any increments are allowed
     * (even-number increments won't be able to produce all outputs, but their quality will be fine for the numbers they
     * can produce). All longs are accepted by this method, and all longs can be produced. Passing 0 here does not
     * cause this to return 0.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize2(long state) {
        state ^= 0xD1B54A32D192ED03L;
        return (state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) ^ state >>> 23 ^ state >>> 51;
        // older Pelican mixer
//        return (state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28;
    }

    /**
     * Very thorough static randomizing method that takes its state as a parameter; state is expected to change between
     * calls to this. It is suggested that you use {@code randomize3(++state)} or {@code randomize3(--state)}
     * to produce a sequence of different numbers, but any odd-number increment should work well, as could another
     * source of different longs, such as a flawed random number generator. All longs are accepted by this method, and
     * all longs can be produced. Passing 0 here does not cause this to return 0.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize3(long state) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return state ^ state >>> 29;
    }

    /**
     * Fast static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is recommended that you
     * use {@code randomize1Bounded(++state, bound)} or {@code randomize1Bounded(--state, bound)} to
     * produce a sequence of different numbers. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int randomize1Bounded(long state, int bound) {
        return (bound = (int) ((bound * (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * Mid-quality static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is suggested that you
     * use {@code randomize2Bounded(++state)} or {@code randomize2Bounded(--state)} to produce a sequence of
     * different numbers, but any increments are allowed (even-number increments won't be able to produce all outputs,
     * but their quality will be fine for the numbers they can produce). All longs are accepted by this method, but not
     * all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any odd-number values for
     * bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */

    public static int randomize2Bounded(long state, int bound) {
        state ^= 0xD1B54A32D192ED03L;
        return (bound = (int) ((bound * (((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) ^ state >>> 23 ^ state >>> 51) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * Very thorough static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is suggested that you
     * use {@code randomize3Bounded(++state)} or {@code randomize3(--state)} to produce a sequence of
     * different numbers, but any increments are allowed (even-number increments won't be able to produce all outputs,
     * but their quality will be fine for the numbers they can produce). All longs are accepted by this method, but not
     * all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any odd-number values for
     * bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */

    public static int randomize3Bounded(long state, int bound) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return (bound = (int) ((bound * ((state ^ state >>> 29) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize1Float(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 30 possible floats between 0 and 1, and this can
     * only return 2 to the 24 of them (a requirement for the returned values to be uniform).
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize1Float(++state)} is recommended to go forwards or
     *              {@code randomize1Float(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomize1Float(long state) {
        return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * EPSILON;
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize2Float(++state)}, where the increment for state can be any value and should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by any odd
     * number, but there are only 2 to the 30 possible floats between 0 and 1, and this can only return 2 to the 24 of
     * them (a requirement for the returned values to be uniform).
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize2Float(++state)} is recommended to go forwards or
     *              {@code randomize2Float(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomize2Float(long state) {
        state ^= 0xD1B54A32D192ED03L;
        return ((((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) >>> 40) * EPSILON;
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize3Float(++state)}, where the increment for state can be any value and should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by any odd
     * number, but there are only 2 to the 30 possible floats between 0 and 1, and this can only return 2 to the 24 of
     * them (a requirement for the returned values to be uniform).
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize3Float(++state)} is recommended to go forwards or
     *              {@code randomize3Float(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomize3Float(long state) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return (state >>> 40) * EPSILON;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize1Double(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize1Double(++state)} is recommended to go forwards or
     *              {@code randomize1Double(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomize1Double(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * EPSILON_D;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize2Double(++state)}, where the increment for state can be any number but should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by 1, but
     * there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomizeDouble(++state)} is recommended to go forwards or
     *              {@code randomizeDouble(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomize2Double(long state) {
        state ^= 0xD1B54A32D192ED03L;
        return (((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) ^ state >>> 23) >>> 11) * EPSILON_D;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize3Double(++state)}, where the increment for state can be any number but should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by 1, but
     * there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize3Double(++state)} is recommended to go forwards or
     *              {@code randomize3Double(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomize3Double(long state) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return (state >>> 11 ^ state >>> 40) * EPSILON_D;
    }

    /**
     * Constructs a OldHasher by hashing {@code seed} with {@link #hash64(long, CharSequence)}, and then running the result
     * through {@link #randomize2(long)}. This is the same as calling the constructor {@link #OldHasher(long)} and passing
     * it {@code randomize2(hash64(1L, seed))} .
     *
     * @param seed a CharSequence, such as a String, that will be used to seed the OldHasher.
     */
    public OldHasher(final CharSequence seed) {
        this(randomize2(hash64(1L, seed)));
    }

    /**
     * Big constant 0.
     */
    public static final long b0 = 0xA0761D6478BD642FL;
    /**
     * Big constant 1.
     */
    public static final long b1 = 0xE7037ED1A0B428DBL;
    /**
     * Big constant 2.
     */
    public static final long b2 = 0x8EBC6AF09C88C6E3L;
    /**
     * Big constant 3.
     */
    public static final long b3 = 0x589965CC75374CC3L;
    /**
     * Big constant 4.
     */
    public static final long b4 = 0x1D8E4E27C47D124FL;
    /**
     * Big constant 5.
     */
    public static final long b5 = 0xEB44ACCAB455D165L;

    /**
     * Takes two arguments that are technically longs, and should be very different, and uses them to get a result
     * that is technically a long and mixes the bits of the inputs. The arguments and result are only technically
     * longs because their lower 32 bits matter much more than their upper 32, and giving just any long won't work.
     * <br>
     * This is very similar to wyhash's mum function, but doesn't use 128-bit math because it expects that its
     * arguments are only relevant in their lower 32 bits (allowing their product to fit in 64 bits).
     *
     * @param a a long that should probably only hold an int's worth of data
     * @param b a long that should probably only hold an int's worth of data
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long mum(final long a, final long b) {
        final long n = a * b;
        return n - (n >>> 32);
    }

    /**
     * A slower but higher-quality variant on {@link #mum(long, long)} that can take two arbitrary longs (with any
     * of their 64 bits containing relevant data) instead of mum's 32-bit sections of its inputs, and outputs a
     * 64-bit result that can have any of its bits used.
     * <br>
     * This was changed so that it distributes bits from both inputs a little better on July 6, 2019.
     *
     * @param a any long
     * @param b any long
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long wow(final long a, final long b) {
        final long n = (a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25));
        return n ^ (n >>> 32);
    }

    public static final OldHasher alpha = new OldHasher("alpha"), beta = new OldHasher("beta"), gamma = new OldHasher("gamma"),
            delta = new OldHasher("delta"), epsilon = new OldHasher("epsilon"), zeta = new OldHasher("zeta"),
            eta = new OldHasher("eta"), theta = new OldHasher("theta"), iota = new OldHasher("iota"),
            kappa = new OldHasher("kappa"), lambda = new OldHasher("lambda"), mu = new OldHasher("mu"),
            nu = new OldHasher("nu"), xi = new OldHasher("xi"), omicron = new OldHasher("omicron"), pi = new OldHasher("pi"),
            rho = new OldHasher("rho"), sigma = new OldHasher("sigma"), tau = new OldHasher("tau"),
            upsilon = new OldHasher("upsilon"), phi = new OldHasher("phi"), chi = new OldHasher("chi"), psi = new OldHasher("psi"),
            omega = new OldHasher("omega"),
            alpha_ = new OldHasher("ALPHA"), beta_ = new OldHasher("BETA"), gamma_ = new OldHasher("GAMMA"),
            delta_ = new OldHasher("DELTA"), epsilon_ = new OldHasher("EPSILON"), zeta_ = new OldHasher("ZETA"),
            eta_ = new OldHasher("ETA"), theta_ = new OldHasher("THETA"), iota_ = new OldHasher("IOTA"),
            kappa_ = new OldHasher("KAPPA"), lambda_ = new OldHasher("LAMBDA"), mu_ = new OldHasher("MU"),
            nu_ = new OldHasher("NU"), xi_ = new OldHasher("XI"), omicron_ = new OldHasher("OMICRON"), pi_ = new OldHasher("PI"),
            rho_ = new OldHasher("RHO"), sigma_ = new OldHasher("SIGMA"), tau_ = new OldHasher("TAU"),
            upsilon_ = new OldHasher("UPSILON"), phi_ = new OldHasher("PHI"), chi_ = new OldHasher("CHI"), psi_ = new OldHasher("PSI"),
            omega_ = new OldHasher("OMEGA"),
            baal = new OldHasher("baal"), agares = new OldHasher("agares"), vassago = new OldHasher("vassago"), samigina = new OldHasher("samigina"),
            marbas = new OldHasher("marbas"), valefor = new OldHasher("valefor"), amon = new OldHasher("amon"), barbatos = new OldHasher("barbatos"),
            paimon = new OldHasher("paimon"), buer = new OldHasher("buer"), gusion = new OldHasher("gusion"), sitri = new OldHasher("sitri"),
            beleth = new OldHasher("beleth"), leraje = new OldHasher("leraje"), eligos = new OldHasher("eligos"), zepar = new OldHasher("zepar"),
            botis = new OldHasher("botis"), bathin = new OldHasher("bathin"), sallos = new OldHasher("sallos"), purson = new OldHasher("purson"),
            marax = new OldHasher("marax"), ipos = new OldHasher("ipos"), aim = new OldHasher("aim"), naberius = new OldHasher("naberius"),
            glasya_labolas = new OldHasher("glasya_labolas"), bune = new OldHasher("bune"), ronove = new OldHasher("ronove"), berith = new OldHasher("berith"),
            astaroth = new OldHasher("astaroth"), forneus = new OldHasher("forneus"), foras = new OldHasher("foras"), asmoday = new OldHasher("asmoday"),
            gaap = new OldHasher("gaap"), furfur = new OldHasher("furfur"), marchosias = new OldHasher("marchosias"), stolas = new OldHasher("stolas"),
            phenex = new OldHasher("phenex"), halphas = new OldHasher("halphas"), malphas = new OldHasher("malphas"), raum = new OldHasher("raum"),
            focalor = new OldHasher("focalor"), vepar = new OldHasher("vepar"), sabnock = new OldHasher("sabnock"), shax = new OldHasher("shax"),
            vine = new OldHasher("vine"), bifrons = new OldHasher("bifrons"), vual = new OldHasher("vual"), haagenti = new OldHasher("haagenti"),
            crocell = new OldHasher("crocell"), furcas = new OldHasher("furcas"), balam = new OldHasher("balam"), alloces = new OldHasher("alloces"),
            caim = new OldHasher("caim"), murmur = new OldHasher("murmur"), orobas = new OldHasher("orobas"), gremory = new OldHasher("gremory"),
            ose = new OldHasher("ose"), amy = new OldHasher("amy"), orias = new OldHasher("orias"), vapula = new OldHasher("vapula"),
            zagan = new OldHasher("zagan"), valac = new OldHasher("valac"), andras = new OldHasher("andras"), flauros = new OldHasher("flauros"),
            andrealphus = new OldHasher("andrealphus"), kimaris = new OldHasher("kimaris"), amdusias = new OldHasher("amdusias"), belial = new OldHasher("belial"),
            decarabia = new OldHasher("decarabia"), seere = new OldHasher("seere"), dantalion = new OldHasher("dantalion"), andromalius = new OldHasher("andromalius"),
            baal_ = new OldHasher("BAAL"), agares_ = new OldHasher("AGARES"), vassago_ = new OldHasher("VASSAGO"), samigina_ = new OldHasher("SAMIGINA"),
            marbas_ = new OldHasher("MARBAS"), valefor_ = new OldHasher("VALEFOR"), amon_ = new OldHasher("AMON"), barbatos_ = new OldHasher("BARBATOS"),
            paimon_ = new OldHasher("PAIMON"), buer_ = new OldHasher("BUER"), gusion_ = new OldHasher("GUSION"), sitri_ = new OldHasher("SITRI"),
            beleth_ = new OldHasher("BELETH"), leraje_ = new OldHasher("LERAJE"), eligos_ = new OldHasher("ELIGOS"), zepar_ = new OldHasher("ZEPAR"),
            botis_ = new OldHasher("BOTIS"), bathin_ = new OldHasher("BATHIN"), sallos_ = new OldHasher("SALLOS"), purson_ = new OldHasher("PURSON"),
            marax_ = new OldHasher("MARAX"), ipos_ = new OldHasher("IPOS"), aim_ = new OldHasher("AIM"), naberius_ = new OldHasher("NABERIUS"),
            glasya_labolas_ = new OldHasher("GLASYA_LABOLAS"), bune_ = new OldHasher("BUNE"), ronove_ = new OldHasher("RONOVE"), berith_ = new OldHasher("BERITH"),
            astaroth_ = new OldHasher("ASTAROTH"), forneus_ = new OldHasher("FORNEUS"), foras_ = new OldHasher("FORAS"), asmoday_ = new OldHasher("ASMODAY"),
            gaap_ = new OldHasher("GAAP"), furfur_ = new OldHasher("FURFUR"), marchosias_ = new OldHasher("MARCHOSIAS"), stolas_ = new OldHasher("STOLAS"),
            phenex_ = new OldHasher("PHENEX"), halphas_ = new OldHasher("HALPHAS"), malphas_ = new OldHasher("MALPHAS"), raum_ = new OldHasher("RAUM"),
            focalor_ = new OldHasher("FOCALOR"), vepar_ = new OldHasher("VEPAR"), sabnock_ = new OldHasher("SABNOCK"), shax_ = new OldHasher("SHAX"),
            vine_ = new OldHasher("VINE"), bifrons_ = new OldHasher("BIFRONS"), vual_ = new OldHasher("VUAL"), haagenti_ = new OldHasher("HAAGENTI"),
            crocell_ = new OldHasher("CROCELL"), furcas_ = new OldHasher("FURCAS"), balam_ = new OldHasher("BALAM"), alloces_ = new OldHasher("ALLOCES"),
            caim_ = new OldHasher("CAIM"), murmur_ = new OldHasher("MURMUR"), orobas_ = new OldHasher("OROBAS"), gremory_ = new OldHasher("GREMORY"),
            ose_ = new OldHasher("OSE"), amy_ = new OldHasher("AMY"), orias_ = new OldHasher("ORIAS"), vapula_ = new OldHasher("VAPULA"),
            zagan_ = new OldHasher("ZAGAN"), valac_ = new OldHasher("VALAC"), andras_ = new OldHasher("ANDRAS"), flauros_ = new OldHasher("FLAUROS"),
            andrealphus_ = new OldHasher("ANDREALPHUS"), kimaris_ = new OldHasher("KIMARIS"), amdusias_ = new OldHasher("AMDUSIAS"), belial_ = new OldHasher("BELIAL"),
            decarabia_ = new OldHasher("DECARABIA"), seere_ = new OldHasher("SEERE"), dantalion_ = new OldHasher("DANTALION"), andromalius_ = new OldHasher("ANDROMALIUS"),
            hydrogen = new OldHasher("hydrogen"), helium = new OldHasher("helium"), lithium = new OldHasher("lithium"), beryllium = new OldHasher("beryllium"), boron = new OldHasher("boron"), carbon = new OldHasher("carbon"), nitrogen = new OldHasher("nitrogen"), oxygen = new OldHasher("oxygen"), fluorine = new OldHasher("fluorine"), neon = new OldHasher("neon"), sodium = new OldHasher("sodium"), magnesium = new OldHasher("magnesium"), aluminium = new OldHasher("aluminium"), silicon = new OldHasher("silicon"), phosphorus = new OldHasher("phosphorus"), sulfur = new OldHasher("sulfur"), chlorine = new OldHasher("chlorine"), argon = new OldHasher("argon"), potassium = new OldHasher("potassium"), calcium = new OldHasher("calcium"), scandium = new OldHasher("scandium"), titanium = new OldHasher("titanium"), vanadium = new OldHasher("vanadium"), chromium = new OldHasher("chromium"), manganese = new OldHasher("manganese"), iron = new OldHasher("iron"), cobalt = new OldHasher("cobalt"), nickel = new OldHasher("nickel"), copper = new OldHasher("copper"), zinc = new OldHasher("zinc"), gallium = new OldHasher("gallium"), germanium = new OldHasher("germanium"), arsenic = new OldHasher("arsenic"), selenium = new OldHasher("selenium"), bromine = new OldHasher("bromine"), krypton = new OldHasher("krypton"), rubidium = new OldHasher("rubidium"), strontium = new OldHasher("strontium"), yttrium = new OldHasher("yttrium"), zirconium = new OldHasher("zirconium"), niobium = new OldHasher("niobium"), molybdenum = new OldHasher("molybdenum"), technetium = new OldHasher("technetium"), ruthenium = new OldHasher("ruthenium"), rhodium = new OldHasher("rhodium"), palladium = new OldHasher("palladium"), silver = new OldHasher("silver"), cadmium = new OldHasher("cadmium"), indium = new OldHasher("indium"), tin = new OldHasher("tin"), antimony = new OldHasher("antimony"), tellurium = new OldHasher("tellurium"), iodine = new OldHasher("iodine"), xenon = new OldHasher("xenon"), caesium = new OldHasher("caesium"), barium = new OldHasher("barium"), lanthanum = new OldHasher("lanthanum"), cerium = new OldHasher("cerium"), praseodymium = new OldHasher("praseodymium"), neodymium = new OldHasher("neodymium"), promethium = new OldHasher("promethium"), samarium = new OldHasher("samarium"), europium = new OldHasher("europium"), gadolinium = new OldHasher("gadolinium"), terbium = new OldHasher("terbium"), dysprosium = new OldHasher("dysprosium"), holmium = new OldHasher("holmium"), erbium = new OldHasher("erbium"), thulium = new OldHasher("thulium"), ytterbium = new OldHasher("ytterbium"), lutetium = new OldHasher("lutetium"), hafnium = new OldHasher("hafnium"), tantalum = new OldHasher("tantalum"), tungsten = new OldHasher("tungsten"), rhenium = new OldHasher("rhenium"), osmium = new OldHasher("osmium"), iridium = new OldHasher("iridium"), platinum = new OldHasher("platinum"), gold = new OldHasher("gold"), mercury = new OldHasher("mercury"), thallium = new OldHasher("thallium"), lead = new OldHasher("lead"), bismuth = new OldHasher("bismuth"), polonium = new OldHasher("polonium"), astatine = new OldHasher("astatine"), radon = new OldHasher("radon"), francium = new OldHasher("francium"), radium = new OldHasher("radium"), actinium = new OldHasher("actinium"), thorium = new OldHasher("thorium"), protactinium = new OldHasher("protactinium"), uranium = new OldHasher("uranium"), neptunium = new OldHasher("neptunium"), plutonium = new OldHasher("plutonium"), americium = new OldHasher("americium"), curium = new OldHasher("curium"), berkelium = new OldHasher("berkelium"), californium = new OldHasher("californium"), einsteinium = new OldHasher("einsteinium"), fermium = new OldHasher("fermium"), mendelevium = new OldHasher("mendelevium"), nobelium = new OldHasher("nobelium"), lawrencium = new OldHasher("lawrencium"), rutherfordium = new OldHasher("rutherfordium"), dubnium = new OldHasher("dubnium"), seaborgium = new OldHasher("seaborgium"), bohrium = new OldHasher("bohrium"), hassium = new OldHasher("hassium"), meitnerium = new OldHasher("meitnerium"), darmstadtium = new OldHasher("darmstadtium"), roentgenium = new OldHasher("roentgenium"), copernicium = new OldHasher("copernicium"), nihonium = new OldHasher("nihonium"), flerovium = new OldHasher("flerovium"), moscovium = new OldHasher("moscovium"), livermorium = new OldHasher("livermorium"), tennessine = new OldHasher("tennessine"), oganesson = new OldHasher("oganesson"),
            hydrogen_ = new OldHasher("HYDROGEN"), helium_ = new OldHasher("HELIUM"), lithium_ = new OldHasher("LITHIUM"), beryllium_ = new OldHasher("BERYLLIUM"), boron_ = new OldHasher("BORON"), carbon_ = new OldHasher("CARBON"), nitrogen_ = new OldHasher("NITROGEN"), oxygen_ = new OldHasher("OXYGEN"), fluorine_ = new OldHasher("FLUORINE"), neon_ = new OldHasher("NEON"), sodium_ = new OldHasher("SODIUM"), magnesium_ = new OldHasher("MAGNESIUM"), aluminium_ = new OldHasher("ALUMINIUM"), silicon_ = new OldHasher("SILICON"), phosphorus_ = new OldHasher("PHOSPHORUS"), sulfur_ = new OldHasher("SULFUR"), chlorine_ = new OldHasher("CHLORINE"), argon_ = new OldHasher("ARGON"), potassium_ = new OldHasher("POTASSIUM"), calcium_ = new OldHasher("CALCIUM"), scandium_ = new OldHasher("SCANDIUM"), titanium_ = new OldHasher("TITANIUM"), vanadium_ = new OldHasher("VANADIUM"), chromium_ = new OldHasher("CHROMIUM"), manganese_ = new OldHasher("MANGANESE"), iron_ = new OldHasher("IRON"), cobalt_ = new OldHasher("COBALT"), nickel_ = new OldHasher("NICKEL"), copper_ = new OldHasher("COPPER"), zinc_ = new OldHasher("ZINC"), gallium_ = new OldHasher("GALLIUM"), germanium_ = new OldHasher("GERMANIUM"), arsenic_ = new OldHasher("ARSENIC"), selenium_ = new OldHasher("SELENIUM"), bromine_ = new OldHasher("BROMINE"), krypton_ = new OldHasher("KRYPTON"), rubidium_ = new OldHasher("RUBIDIUM"), strontium_ = new OldHasher("STRONTIUM"), yttrium_ = new OldHasher("YTTRIUM"), zirconium_ = new OldHasher("ZIRCONIUM"), niobium_ = new OldHasher("NIOBIUM"), molybdenum_ = new OldHasher("MOLYBDENUM"), technetium_ = new OldHasher("TECHNETIUM"), ruthenium_ = new OldHasher("RUTHENIUM"), rhodium_ = new OldHasher("RHODIUM"), palladium_ = new OldHasher("PALLADIUM"), silver_ = new OldHasher("SILVER"), cadmium_ = new OldHasher("CADMIUM"), indium_ = new OldHasher("INDIUM"), tin_ = new OldHasher("TIN"), antimony_ = new OldHasher("ANTIMONY"), tellurium_ = new OldHasher("TELLURIUM"), iodine_ = new OldHasher("IODINE"), xenon_ = new OldHasher("XENON"), caesium_ = new OldHasher("CAESIUM"), barium_ = new OldHasher("BARIUM"), lanthanum_ = new OldHasher("LANTHANUM"), cerium_ = new OldHasher("CERIUM"), praseodymium_ = new OldHasher("PRASEODYMIUM"), neodymium_ = new OldHasher("NEODYMIUM"), promethium_ = new OldHasher("PROMETHIUM"), samarium_ = new OldHasher("SAMARIUM"), europium_ = new OldHasher("EUROPIUM"), gadolinium_ = new OldHasher("GADOLINIUM"), terbium_ = new OldHasher("TERBIUM"), dysprosium_ = new OldHasher("DYSPROSIUM"), holmium_ = new OldHasher("HOLMIUM"), erbium_ = new OldHasher("ERBIUM"), thulium_ = new OldHasher("THULIUM"), ytterbium_ = new OldHasher("YTTERBIUM"), lutetium_ = new OldHasher("LUTETIUM"), hafnium_ = new OldHasher("HAFNIUM"), tantalum_ = new OldHasher("TANTALUM"), tungsten_ = new OldHasher("TUNGSTEN"), rhenium_ = new OldHasher("RHENIUM"), osmium_ = new OldHasher("OSMIUM"), iridium_ = new OldHasher("IRIDIUM"), platinum_ = new OldHasher("PLATINUM"), gold_ = new OldHasher("GOLD"), mercury_ = new OldHasher("MERCURY"), thallium_ = new OldHasher("THALLIUM"), lead_ = new OldHasher("LEAD"), bismuth_ = new OldHasher("BISMUTH"), polonium_ = new OldHasher("POLONIUM"), astatine_ = new OldHasher("ASTATINE"), radon_ = new OldHasher("RADON"), francium_ = new OldHasher("FRANCIUM"), radium_ = new OldHasher("RADIUM"), actinium_ = new OldHasher("ACTINIUM"), thorium_ = new OldHasher("THORIUM"), protactinium_ = new OldHasher("PROTACTINIUM"), uranium_ = new OldHasher("URANIUM"), neptunium_ = new OldHasher("NEPTUNIUM"), plutonium_ = new OldHasher("PLUTONIUM"), americium_ = new OldHasher("AMERICIUM"), curium_ = new OldHasher("CURIUM"), berkelium_ = new OldHasher("BERKELIUM"), californium_ = new OldHasher("CALIFORNIUM"), einsteinium_ = new OldHasher("EINSTEINIUM"), fermium_ = new OldHasher("FERMIUM"), mendelevium_ = new OldHasher("MENDELEVIUM"), nobelium_ = new OldHasher("NOBELIUM"), lawrencium_ = new OldHasher("LAWRENCIUM"), rutherfordium_ = new OldHasher("RUTHERFORDIUM"), dubnium_ = new OldHasher("DUBNIUM"), seaborgium_ = new OldHasher("SEABORGIUM"), bohrium_ = new OldHasher("BOHRIUM"), hassium_ = new OldHasher("HASSIUM"), meitnerium_ = new OldHasher("MEITNERIUM"), darmstadtium_ = new OldHasher("DARMSTADTIUM"), roentgenium_ = new OldHasher("ROENTGENIUM"), copernicium_ = new OldHasher("COPERNICIUM"), nihonium_ = new OldHasher("NIHONIUM"), flerovium_ = new OldHasher("FLEROVIUM"), moscovium_ = new OldHasher("MOSCOVIUM"), livermorium_ = new OldHasher("LIVERMORIUM"), tennessine_ = new OldHasher("TENNESSINE"), oganesson_ = new OldHasher("OGANESSON");

    /**
     * Has a length of 428, which may be relevant if automatically choosing a predefined hash functor.
     */
    public static final OldHasher[] predefined = new OldHasher[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
            kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega,
            alpha_, beta_, gamma_, delta_, epsilon_, zeta_, eta_, theta_, iota_,
            kappa_, lambda_, mu_, nu_, xi_, omicron_, pi_, rho_, sigma_, tau_, upsilon_, phi_, chi_, psi_, omega_,
            baal, agares, vassago, samigina, marbas, valefor, amon, barbatos,
            paimon, buer, gusion, sitri, beleth, leraje, eligos, zepar,
            botis, bathin, sallos, purson, marax, ipos, aim, naberius,
            glasya_labolas, bune, ronove, berith, astaroth, forneus, foras, asmoday,
            gaap, furfur, marchosias, stolas, phenex, halphas, malphas, raum,
            focalor, vepar, sabnock, shax, vine, bifrons, vual, haagenti,
            crocell, furcas, balam, alloces, caim, murmur, orobas, gremory,
            ose, amy, orias, vapula, zagan, valac, andras, flauros,
            andrealphus, kimaris, amdusias, belial, decarabia, seere, dantalion, andromalius,
            baal_, agares_, vassago_, samigina_, marbas_, valefor_, amon_, barbatos_,
            paimon_, buer_, gusion_, sitri_, beleth_, leraje_, eligos_, zepar_,
            botis_, bathin_, sallos_, purson_, marax_, ipos_, aim_, naberius_,
            glasya_labolas_, bune_, ronove_, berith_, astaroth_, forneus_, foras_, asmoday_,
            gaap_, furfur_, marchosias_, stolas_, phenex_, halphas_, malphas_, raum_,
            focalor_, vepar_, sabnock_, shax_, vine_, bifrons_, vual_, haagenti_,
            crocell_, furcas_, balam_, alloces_, caim_, murmur_, orobas_, gremory_,
            ose_, amy_, orias_, vapula_, zagan_, valac_, andras_, flauros_,
            andrealphus_, kimaris_, amdusias_, belial_, decarabia_, seere_, dantalion_, andromalius_,

            hydrogen, helium, lithium, beryllium, boron, carbon, nitrogen, oxygen, fluorine, neon,
            sodium, magnesium, aluminium, silicon, phosphorus, sulfur, chlorine, argon, potassium,
            calcium, scandium, titanium, vanadium, chromium, manganese, iron, cobalt, nickel,
            copper, zinc, gallium, germanium, arsenic, selenium, bromine, krypton, rubidium,
            strontium, yttrium, zirconium, niobium, molybdenum, technetium, ruthenium, rhodium,
            palladium, silver, cadmium, indium, tin, antimony, tellurium, iodine, xenon, caesium,
            barium, lanthanum, cerium, praseodymium, neodymium, promethium, samarium, europium,
            gadolinium, terbium, dysprosium, holmium, erbium, thulium, ytterbium, lutetium, hafnium,
            tantalum, tungsten, rhenium, osmium, iridium, platinum, gold, mercury, thallium, lead,
            bismuth, polonium, astatine, radon, francium, radium, actinium, thorium, protactinium,
            uranium, neptunium, plutonium, americium, curium, berkelium, californium, einsteinium,
            fermium, mendelevium, nobelium, lawrencium, rutherfordium, dubnium, seaborgium, bohrium,
            hassium, meitnerium, darmstadtium, roentgenium, copernicium, nihonium, flerovium, moscovium,
            livermorium, tennessine, oganesson,

            hydrogen_, helium_, lithium_, beryllium_, boron_, carbon_, nitrogen_, oxygen_, fluorine_, neon_,
            sodium_, magnesium_, aluminium_, silicon_, phosphorus_, sulfur_, chlorine_, argon_, potassium_,
            calcium_, scandium_, titanium_, vanadium_, chromium_, manganese_, iron_, cobalt_, nickel_,
            copper_, zinc_, gallium_, germanium_, arsenic_, selenium_, bromine_, krypton_, rubidium_,
            strontium_, yttrium_, zirconium_, niobium_, molybdenum_, technetium_, ruthenium_, rhodium_,
            palladium_, silver_, cadmium_, indium_, tin_, antimony_, tellurium_, iodine_, xenon_, caesium_,
            barium_, lanthanum_, cerium_, praseodymium_, neodymium_, promethium_, samarium_, europium_,
            gadolinium_, terbium_, dysprosium_, holmium_, erbium_, thulium_, ytterbium_, lutetium_, hafnium_,
            tantalum_, tungsten_, rhenium_, osmium_, iridium_, platinum_, gold_, mercury_, thallium_, lead_,
            bismuth_, polonium_, astatine_, radon_, francium_, radium_, actinium_, thorium_, protactinium_,
            uranium_, neptunium_, plutonium_, americium_, curium_, berkelium_, californium_, einsteinium_,
            fermium_, mendelevium_, nobelium_, lawrencium_, rutherfordium_, dubnium_, seaborgium_, bohrium_,
            hassium_, meitnerium_, darmstadtium_, roentgenium_, copernicium_, nihonium_, flerovium_, moscovium_,
            livermorium_, tennessine_, oganesson_,

    };

    public long hash64(final boolean[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[len - 1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len - 1] ? 0x79B9L : 0x7C15L));
                break;
            case 2:
                seed = mum(seed ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum(seed ^ (data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final byte[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final short[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final char[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }

    /**
     * This method changed in 0.2.1, from taking a start index and end index, to taking a start index and length.
     * Taking the length matches the behavior of more methods in the JVM.
     * @param data a char array
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the array)
     * @return a 64-bit hash of data
     */
    public long hash64(final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final CharSequence data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length());
    }

    /**
     * This method changed in 0.2.1, from taking a start index and end index, to taking a start index and length.
     * Taking the length matches the behavior of more methods in the JVM.
     * @param data  the String or other CharSequence to hash
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the CharSequence)
     * @return a 64-bit hash of data
     */
    public long hash64(final CharSequence data, final int start, final int length) {
        if (data == null || start < 0 || length < 0 || start >= length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), data.charAt(len - 1) ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3), data.charAt(len - 2) ^ b2) ^ mum(seed ^ data.charAt(len - 1), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final int[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[start + len - 1] >>> 16), b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], b2 ^ data[start + len - 2]) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final int[] data, final int length) {
        return hash64(data, 0, length);
    }

    public long hash64(final long[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ data[start + len - 1]);
                break;
            case 2:
                seed = wow(seed + data[start + len - 2], b2 ^ data[start + len - 1]);
                break;
            case 3:
                seed = wow(seed + data[start + len - 3], b2 + data[start + len - 2]) + wow(seed + data[start + len - 1], seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }

    public long hash64(final float[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (floatToRawIntBits(data[start + len - 1]) >>> 16), b3 ^ (floatToRawIntBits(data[start + len - 1]) & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 2]), b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 3]), b2 ^ floatToRawIntBits(data[start + len - 2])) ^ mum(seed ^ floatToRawIntBits(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final double[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= doubleToRawLongBits(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 2]), b2 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 3]), b2 + doubleToRawLongBits(data[start + len - 2])) + wow(seed + doubleToRawLongBits(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }

    public long hash64(final byte[][] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final byte[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final char[][] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final char[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final float[][] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final float[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final double[][] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final double[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final int[][] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final int[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final long[][] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final long[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final CharSequence[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final CharSequence[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final CharSequence[]... data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final CharSequence[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            seed = mum(
                    mum(hash(it.next()) ^ b1, (it.hasNext() ? hash(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        return hash64(data, 0, data.size());
    }
    public long hash64(final List<? extends CharSequence> data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.size())
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.size() - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data.get(i - 3)) ^ b1, hash(data.get(i - 2)) ^ b2) + seed,
                    mum(hash(data.get(i - 1)) ^ b3, hash(data.get(i)) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data.get(len - 1))) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data.get(len - 2)), b0 ^ hash(data.get(len - 1)));
                break;
            case 3:
                seed = mum(seed ^ hash(data.get(len - 3)), b2 ^ hash(data.get(len - 2))) ^ mum(seed ^ hash(data.get(len - 1)), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);

    }

    public long hash64(final Object[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final Object data) {
        if (data == null)
            return 0;
        final long h = (data.hashCode() + seed) * 0x9E3779B97F4A7C15L;
        return h - (h >>> 31) + (h << 33);
    }

    public int hash(final boolean[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[len - 1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len - 1] ? 0x79B9L : 0x7C15L));
                break;
            case 2:
                seed = mum(seed ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum(seed ^ (data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final byte[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final short[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final char[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    /**
     * This method changed in 0.2.1, from taking a start index and end index, to taking a start index and length.
     * Taking the length matches the behavior of more methods in the JVM.
     * @param data a char array
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the array)
     * @return a 32-bit hash of data
     */
    public int hash(final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final CharSequence data) {
        if (data == null) return 0;
        return hash(data, 0, data.length());
    }

    /**
     * This method changed in 0.2.1, from taking a start index and end index, to taking a start index and length.
     * Taking the length matches the behavior of more methods in the JVM.
     * @param data  the String or other CharSequence to hash
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the CharSequence)
     * @return a 32-bit hash of data
     */
    public int hash(final CharSequence data, final int start, final int length) {
        if (data == null || start < 0 || length < 0 || start >= length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), data.charAt(len - 1) ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3), data.charAt(len - 2) ^ b2) ^ mum(seed ^ data.charAt(len - 1), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final int[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[start + len - 1] >>> 16), b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], b2 ^ data[start + len - 2]) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final int[] data, final int length) {
        return hash(data, 0, length);
    }

    public int hash(final long[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ data[start + len - 1]);
                break;
            case 2:
                seed = wow(seed + data[start + len - 2], b2 ^ data[start + len - 1]);
                break;
            case 3:
                seed = wow(seed + data[start + len - 3], b2 + data[start + len - 2]) + wow(seed + data[start + len - 1], seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int) (seed ^ seed >>> 23 ^ seed >>> 42);
    }

    public int hash(final float[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (floatToRawIntBits(data[start + len - 1]) >>> 16), b3 ^ (floatToRawIntBits(data[start + len - 1]) & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 2]), b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 3]), b2 ^ floatToRawIntBits(data[start + len - 2])) ^ mum(seed ^ floatToRawIntBits(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final double[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= doubleToRawLongBits(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 2]), b2 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 3]), b2 + doubleToRawLongBits(data[start + len - 2])) + wow(seed + doubleToRawLongBits(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int) (seed ^ seed >>> 23 ^ seed >>> 42);
    }

    public int hash(final byte[][] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final byte[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final char[][] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final char[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final float[][] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final float[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final double[][] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final double[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final int[][] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final int[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final long[][] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final long[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final CharSequence[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final CharSequence[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final CharSequence[]... data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final CharSequence[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            seed = mum(
                    mum(hash(it.next()) ^ b1, (it.hasNext() ? hash(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        return hash(data, 0, data.size());
    }
    public int hash(final List<? extends CharSequence> data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.size())
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.size() - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data.get(i - 3)) ^ b1, hash(data.get(i - 2)) ^ b2) + seed,
                    mum(hash(data.get(i - 1)) ^ b3, hash(data.get(i)) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data.get(len - 1))) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data.get(len - 2)), b0 ^ hash(data.get(len - 1)));
                break;
            case 3:
                seed = mum(seed ^ hash(data.get(len - 3)), b2 ^ hash(data.get(len - 2))) ^ mum(seed ^ hash(data.get(len - 1)), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));

    }

    public int hash(final Object[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(data[i - 3]) ^ b1, hash(data[i - 2]) ^ b2) + seed,
                    mum(hash(data[i - 1]) ^ b3, hash(data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(data[start + len - 2]), b0 ^ hash(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(data[start + len - 3]), b2 ^ hash(data[start + len - 2])) ^ mum(seed ^ hash(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public int hash(final Object data) {
        if (data == null) return 0;
        return (int) ((data.hashCode() + seed) * 0x9E3779B97F4A7C15L >>> 32);
    }


    public static long hash64(long seed, final boolean[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[len - 1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len - 1] ? 0x79B9L : 0x7C15L));
                break;
            case 2:
                seed = mum(seed ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum(seed ^ (data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final byte[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final short[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final char[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }

    /**
     * This method changed in 0.2.1, from taking a start index and end index, to taking a start index and length.
     * Taking the length matches the behavior of more methods in the JVM.
     * @param seed the seed to use for this hash, as a long
     * @param data a char array
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the array)
     * @return a 64-bit hash of data
     */
    public static long hash64(long seed, final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final CharSequence data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length());
    }

    /**
     * This method changed in 0.2.1, from taking a start index and end index, to taking a start index and length.
     * Taking the length matches the behavior of more methods in the JVM.
     * @param seed the seed to use for this hash, as a long
     * @param data  the String or other CharSequence to hash
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the array)
     * @return a 64-bit hash of data
     */
    public static long hash64(long seed, final CharSequence data, final int start, final int length) {
        if (data == null || start < 0 || length < 0 || start >= length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), data.charAt(len - 1) ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3), data.charAt(len - 2) ^ b2) ^ mum(seed ^ data.charAt(len - 1), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final int[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[start + len - 1] >>> 16), b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], b2 ^ data[start + len - 2]) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final int[] data, final int length) {
        return hash64(seed, data, 0, length);
    }

    public static long hash64(long seed, final long[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ data[start + len - 1]);
                break;
            case 2:
                seed = wow(seed + data[start + len - 2], b2 ^ data[start + len - 1]);
                break;
            case 3:
                seed = wow(seed + data[start + len - 3], b2 + data[start + len - 2]) + wow(seed + data[start + len - 1], seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }

    public static long hash64(long seed, final float[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (floatToRawIntBits(data[start + len - 1]) >>> 16), b3 ^ (floatToRawIntBits(data[start + len - 1]) & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 2]), b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 3]), b2 ^ floatToRawIntBits(data[start + len - 2])) ^ mum(seed ^ floatToRawIntBits(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final double[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= doubleToRawLongBits(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 2]), b2 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 3]), b2 + doubleToRawLongBits(data[start + len - 2])) + wow(seed + doubleToRawLongBits(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }

    public static long hash64(long seed, final byte[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final byte[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final char[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final char[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final float[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final float[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final double[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final double[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final int[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final int[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final long[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final long[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final CharSequence[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final CharSequence[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final CharSequence[]... data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final CharSequence[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            seed = mum(
                    mum(hash(seed, it.next()) ^ b1, (it.hasNext() ? hash(seed, it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(seed, it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(seed, it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.size());
    }
    public static long hash64(long seed, final List<? extends CharSequence> data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.size())
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.size() - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data.get(i - 3)) ^ b1, hash(seed, data.get(i - 2)) ^ b2) + seed,
                    mum(hash(seed, data.get(i - 1)) ^ b3, hash(seed, data.get(i)) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data.get(len - 1))) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data.get(len - 2)), b0 ^ hash(seed, data.get(len - 1)));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data.get(len - 3)), b2 ^ hash(seed, data.get(len - 2))) ^ mum(seed ^ hash(seed, data.get(len - 1)), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);

    }

    public static long hash64(long seed, final Object[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final Object data) {
        if (data == null)
            return 0;
        final long h = (data.hashCode() + seed) * 0x9E3779B97F4A7C15L;
        return h - (h >>> 31) + (h << 33);
    }

    public static int hash(long seed, final boolean[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[len - 1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len - 1] ? 0x79B9L : 0x7C15L));
                break;
            case 2:
                seed = mum(seed ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum(seed ^ (data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final byte[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final short[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final char[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data[start + len - 1]);
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], data[start + len - 1] ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], data[start + len - 2] ^ b2) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final CharSequence data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length());
    }

    /**
     * This method changed in 0.2.1, from taking a start index and end index, to taking a start index and length.
     * Taking the length matches the behavior of more methods in the JVM.
     * @param data  the String or other CharSequence to hash
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the CharSequence)
     * @return a 32-bit hash of data
     */
    public static int hash(long seed, final CharSequence data, final int start, final int length) {
        if (data == null || start < 0 || length < 0 || start >= length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) + seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed, b3 ^ data.charAt(len - 1));
                break;
            case 2:
                seed = mum(seed ^ data.charAt(len - 2), data.charAt(len - 1) ^ b0);
                break;
            case 3:
                seed = mum(seed ^ data.charAt(len - 3), data.charAt(len - 2) ^ b2) ^ mum(seed ^ data.charAt(len - 1), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final int[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) + seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (data[start + len - 1] >>> 16), b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ data[start + len - 2], b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(seed ^ data[start + len - 3], b2 ^ data[start + len - 2]) ^ mum(seed ^ data[start + len - 1], b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final int[] data, final int length) {
        return hash(seed, data, 0, length);
    }

    public static int hash(long seed, final long[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= data[i - 3] * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= data[i - 2] * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= data[i - 1] * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= data[i] * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ data[start + len - 1]);
                break;
            case 2:
                seed = wow(seed + data[start + len - 2], b2 ^ data[start + len - 1]);
                break;
            case 3:
                seed = wow(seed + data[start + len - 3], b2 + data[start + len - 2]) + wow(seed + data[start + len - 1], seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int) (seed ^ seed >>> 23 ^ seed >>> 42);
    }

    public static int hash(long seed, final float[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ (floatToRawIntBits(data[start + len - 1]) >>> 16), b3 ^ (floatToRawIntBits(data[start + len - 1]) & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 2]), b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ floatToRawIntBits(data[start + len - 3]), b2 ^ floatToRawIntBits(data[start + len - 2])) ^ mum(seed ^ floatToRawIntBits(data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final double[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= doubleToRawLongBits(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1:
                seed = wow(seed, b1 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 2]), b2 ^ doubleToRawLongBits(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + doubleToRawLongBits(data[start + len - 3]), b2 + doubleToRawLongBits(data[start + len - 2])) + wow(seed + doubleToRawLongBits(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int) (seed ^ seed >>> 23 ^ seed >>> 42);
    }

    public static int hash(long seed, final byte[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final byte[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final char[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final char[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final float[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final float[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final double[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final double[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final int[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final int[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final long[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final long[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final CharSequence[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final CharSequence[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final CharSequence[]... data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final CharSequence[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext()) {
            ++len;
            seed = mum(
                    mum(hash(seed, it.next()) ^ b1, (it.hasNext() ? hash(seed, it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(seed, it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(seed, it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.size());
    }
    public static int hash(long seed, final List<? extends CharSequence> data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.size())
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.size() - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data.get(i - 3)) ^ b1, hash(seed, data.get(i - 2)) ^ b2) + seed,
                    mum(hash(seed, data.get(i - 1)) ^ b3, hash(seed, data.get(i)) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data.get(len - 1))) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data.get(len - 2)), b0 ^ hash(seed, data.get(len - 1)));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data.get(len - 3)), b2 ^ hash(seed, data.get(len - 2))) ^ mum(seed ^ hash(seed, data.get(len - 1)), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));

    }

    public static int hash(long seed, final Object[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(hash(seed, data[i - 3]) ^ b1, hash(seed, data[i - 2]) ^ b2) + seed,
                    mum(hash(seed, data[i - 1]) ^ b3, hash(seed, data[i]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0:
                seed = mum(b1 ^ seed, b4 + seed);
                break;
            case 1:
                seed = mum(seed ^ ((t = hash(seed, data[start + len - 1])) >>> 16), b3 ^ (t & 0xFFFFL));
                break;
            case 2:
                seed = mum(seed ^ hash(seed, data[start + len - 2]), b0 ^ hash(seed, data[start + len - 1]));
                break;
            case 3:
                seed = mum(seed ^ hash(seed, data[start + len - 3]), b2 ^ hash(seed, data[start + len - 2])) ^ mum(seed ^ hash(seed, data[start + len - 1]), b4);
                break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int) (seed - (seed >>> 32));
    }

    public static int hash(long seed, final Object data) {
        if (data == null)
            return 0;
        seed += b1;
        seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        return (int) ((data.hashCode() + seed) * 0x9E3779B97F4A7C15L >>> 32);
    }
}