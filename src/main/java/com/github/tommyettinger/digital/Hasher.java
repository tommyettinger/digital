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

package com.github.tommyettinger.digital;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
 * Variations on wyhash are used depending on the types being hashed. There is also a different
 * algorithm present here in the {@link #hashBulk} and {@link #hashBulk64} methods, based very
 * loosely on the <a href="https://github.com/jonmaiga/mx3">MX3 hash</a>. This algorithm, called
 * Ax, is faster at hashing large arrays of longs than any of the Wyhash variants I've tried,
 * and also passes the SMHasher 3 test suite. It does best with larger array sizes, so the name
 * includes "Bulk", but it tends to be faster starting at lengths of maybe 20 long items. The
 * bulk hash functions also include ways to connect with other hash functions to hash
 * multidimensional arrays or arrays of types this doesn't provide a hash function for.
 * <br>
 * This provides an object-based API and a static API, where a Hasher object is
 * instantiated with a seed, and the static methods take a seed as their first argument.
 * Any hash that this returns is always 0 when given null to hash. Arrays with
 * identical elements of identical types will hash identically. Arrays with identical
 * numerical values but different types will sometimes hash differently. This class
 * always provides 64-bit hashes via hash64() and 32-bit hashes via hash(). The additional
 * hashBulk64() and hashBulk() methods use the Ax algorithm instead of one based on
 * Wyhash, as mentioned before, and can hash ByteBuffer objects and long arrays. They
 * also provide some alternative options to hash arrays of some type using a user-provided
 * hash for that type. Predefined functional interfaces are present in this class, such as
 * {@link HashFunction64} for member {@link #hash64} methods, {@link HashFunction} for member
 * {@link #hash} methods, {@link SeededHashFunction64} for static hash64 methods such as
 * the one referenced in the {@link #longArrayHash64} constant, and {@link SeededHashFunction}
 * for static hash methods such as the one referenced in the {@link #longArrayHash} constant.
 * Using the static seeded variants is preferred when passing in a hash function, because the
 * constants don't have any name clash. That means if you were to try to use
 * {@code int result = Hasher.alpha.hashBulk(Hasher.alpha::hash, data);}, that would probably fail to compile
 * because Java can't infer which hash() method is intended (the one with one argument of the
 * correct type, or that argument as well as two ints). You can make hashBulk with a HashFunction
 * work by casting {@code Hasher.alpha::hash} to the correct type, like so (where data is a {@code long[][]}):
 * {@code int result = Hasher.alpha.hashBulk((Hasher.HashFunction64<long[]>)Hasher.alpha::hash, data);}
 * Or, you can use the static variants:
 * {@code int result = Hasher.hashBulk(seed, Hasher.longArrayHash, data);}
 * <br>
 * The hash64() and hash() methods use 64-bit math even when producing
 * 32-bit hashes, for GWT reasons. GWT doesn't have the same behavior as desktop and
 * Android applications when using ints because it treats ints mostly like doubles,
 * sometimes, due to it using JavaScript. If we use mainly longs, though, GWT emulates
 * the longs with a more complex technique behind-the-scenes, that behaves the same on
 * the web as it does on desktop or on a phone. Since Hasher is supposed to be stable
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
 * There are also 428 predefined instances of Hasher that you can either
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
public class Hasher {

    /**
     * A functional interface type for 32-bit hash() functions that take one item, typically of an array type.
     * @param <T> typically an array type, such as {@code int[]}
     */
    public interface HashFunction<T> {
        int hash(T data);
    }

    /**
     * A functional interface type for 64-bit hash64() functions that take one item, typically of an array type.
     * @param <T> typically an array type, such as {@code int[]}
     */
    public interface HashFunction64<T> {
        long hash64(T data);
    }

    /**
     * A functional interface type for 32-bit hash() functions that take a long seed and one item, typically of an
     * array type.
     * @param <T> typically an array type, such as {@code int[]}
     */
    public interface SeededHashFunction<T> {
        int hash(long seed, T data);
    }

    /**
     * A functional interface type for 64-bit hash64() functions that take a long seed and one item, typically of an
     * array type.
     * @param <T> typically an array type, such as {@code int[]}
     */
    public interface SeededHashFunction64<T> {
        long hash64(long seed, T data);
    }

    /**
     * The seed used by all non-static hash() and hash64() methods in this class (the methods that don't take a seed).
     * You can create many different Hasher objects, all with different seeds, and get very different hashes as a result
     * of any calls on them. Because making this field hidden in some way doesn't meaningfully contribute to security,
     * and only makes it harder to use this class, {@code seed} is public (and final, so it can't be accidentally
     * modified, but still can if needed via reflection).
     */
    public final long seed;

    /**
     * Creates a new Hasher seeded, arbitrarily, with the constant 0xC4CEB9FE1A85EC53L, or -4265267296055464877L .
     */
    public Hasher() {
        this(0xC4CEB9FE1A85EC53L);
    }

    /**
     * Initializes this Hasher with the given seed, verbatim; it is recommended to use {@link #randomize3(long)} on the
     * seed if you don't know if it is adequately-random. If the seed is the same for two different Hasher instances,
     * and they are given the same inputs, they will produce the same results. If the seed is even slightly different,
     * the results of the two Hashers given the same input should be significantly different.
     *
     * @param seed a long that will be used to change the output of hash() and hash64() methods on the new Hasher
     */
    public Hasher(long seed) {
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
     * it will have excellent quality for most patterns in input but will be about 30% slower than {@code randomize1()},
     * though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
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
     * Constructs a Hasher by hashing {@code seed} with {@link #hash64(long, CharSequence)}, and then running the result
     * through {@link #randomize3(long)}. This is the same as calling the constructor {@link #Hasher(long)} and passing
     * it {@code randomize3(hash64(1L, seed))} .
     *
     * @param seed a CharSequence, such as a String, that will be used to seed the Hasher.
     */
    public Hasher(final CharSequence seed) {
        this(randomize3(hash64(1L, seed)));
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
     * A long constant used as a multiplier by the MX3 unary hash.
     * Used in {@link #mix(long)} and {@link #mixStream(long, long)}, as well as when hashing one Object.
     */
    public static final long C = 0xBEA225F9EB34556DL;
    /**
     * A 64-bit probable prime, found with {@link java.math.BigInteger#probablePrime(int, Random)}.
     */
    public static final long Q = 0xD1B92B09B92266DDL;
    /**
     * A 64-bit probable prime, found with {@link java.math.BigInteger#probablePrime(int, Random)}.
     */
    public static final long R = 0x9995988B72E0D285L;
    /**
     * A 64-bit probable prime, found with {@link java.math.BigInteger#probablePrime(int, Random)}.
     */
    public static final long S = 0x8FADF5E286E31587L;
    /**
     * A 64-bit probable prime, found with {@link java.math.BigInteger#probablePrime(int, Random)}.
     */
    public static final long T = 0xFCF8B405D3D0783BL;

    /**
     * Takes two arguments that are technically longs, and should be very different, and uses them to get a result
     * that is technically a long and mixes the bits of the inputs. The arguments and result are only technically
     * longs because their lower 32 bits matter much more than their upper 32, and giving just any long won't work.
     * Used by {@link #hash64(int[])}, {@link #hash(int[])}, and other hashes that use 32-bit or smaller items.
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
        return n ^ (n >>> 30);
    }

    /**
     * A slower but higher-quality variant on {@link #mum(long, long)} that can take two arbitrary longs (with any
     * of their 64 bits containing relevant data) instead of mum's 32-bit sections of its inputs, and outputs a
     * 64-bit result that can have any of its bits used.
     * Used by {@link #hash64(long[])}, {@link #hash(long[])}, and other hashes that use 64-bit items.
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

    /**
     * A medium-quality, but fast, way to scramble a 64-bit input and get a 64-bit output.
     * Used by {@link #hashBulk64} and {@link #hashBulk}.
     * <br>
     * This is reversible, which allows all outputs to be possible for the hashing functions to produce.
     * However, this also allows the seed to be recovered if a zero-length input is supplied. That's why this
     * is a non-cryptographic hashing algorithm!
     * @param x any long
     * @return any long
     */
    public static long mix(long x) {
        x ^= (x << 23 | x >>> 41) ^ (x << 43 | x >>> 21);
        x *= C;
        return x ^ (x << 11 | x >>> 53) ^ (x << 50 | x >>> 14);
    }

    /**
     * A low-to-medium-quality and fast way to combine two 64-bit inputs to get one 64-bit result.
     * Used by {@link #hashBulk64} and {@link #hashBulk}.
     * <br>
     * This is not reversible unless you know one of the parameters in full.
     * @param h any long, typically a counter; will be scrambled much less
     * @param x any long, typically an item being hashed; will be scrambled much more
     * @return any long
     */
    public static long mixStream(long h, long x) {
        x *= C;
        x ^= x >>> 39;
        return (x * C + h) * C;
    }

    /**
     * Performs part of the hashing step applied to four 64-bit inputs at once, and typically added to a running
     * hash value directly.
     * Used by {@link #hashBulk64} and {@link #hashBulk}.
     * <br>
     * This is not reversible under normal circumstances. It may be possible to recover one parameter if the other three
     * are known in full. This uses four 64-bit primes as multipliers; the exact numbers don't matter as long as
     * they are odd and have sufficiently well-distributed bits (close to 32 '1' bits, and so on). If this is only
     * added to a running total, the result won't have very random low-order bits, so performing bitwise rotations
     * after at least some calls to this (or xorshifting right) is critical to keeping the hash high-quality.
     * @param a any long, typically an item being hashed
     * @param b any long, typically an item being hashed
     * @param c any long, typically an item being hashed
     * @param d any long, typically an item being hashed
     * @return any long
     */
    public static long mixStreamBulk(long a, long b, long c, long d) {
        return
                ((a << 29 | a >>> 35) - c) * Q
                        + ((b << 29 | b >>> 35) - d) * R
                        + ((c << 29 | c >>> 35) - b) * S
                        + ((d << 29 | d >>> 35) - a) * T;
    }


    /**
     * A very minimalist way to scramble inputs to be used as seeds; can be inverted using {@link #reverse(long)}.
     * This simply performs the XOR-rotate-XOR-rotate operation on x, using left rotations of 29 and 47.
     * @param x any long
     * @return a slightly scrambled version of x
     */
    public static long forward(long x) {
        return x ^ (x << 29 | x >>> 35) ^ (x << 47 | x >>> 17);
    }

    /**
     * Unscrambles the result of {@link #forward(long)} to get its original argument back.
     * @param x a long produced by {@link #forward(long)} or obtained from {@link #seed}
     * @return the original long that was provided to {@link #forward(long)}, before scrambling
     */
    public static long reverse(long x) {
        x ^= x ^ (x << 29 | x >>> 35) ^ (x << 47 | x >>> 17);
        x ^= x ^ (x << 58 | x >>>  6) ^ (x << 30 | x >>> 34);
        x ^= x ^ (x << 52 | x >>> 12) ^ (x << 60 | x >>>  4);
        x ^= x ^ (x << 40 | x >>> 24) ^ (x << 56 | x >>>  8);
        x ^= x ^ (x << 16 | x >>> 48) ^ (x << 48 | x >>> 16);
        return x;
    }


    public static final Hasher alpha = new Hasher("alpha"), beta = new Hasher("beta"), gamma = new Hasher("gamma"),
            delta = new Hasher("delta"), epsilon = new Hasher("epsilon"), zeta = new Hasher("zeta"),
            eta = new Hasher("eta"), theta = new Hasher("theta"), iota = new Hasher("iota"),
            kappa = new Hasher("kappa"), lambda = new Hasher("lambda"), mu = new Hasher("mu"),
            nu = new Hasher("nu"), xi = new Hasher("xi"), omicron = new Hasher("omicron"), pi = new Hasher("pi"),
            rho = new Hasher("rho"), sigma = new Hasher("sigma"), tau = new Hasher("tau"),
            upsilon = new Hasher("upsilon"), phi = new Hasher("phi"), chi = new Hasher("chi"), psi = new Hasher("psi"),
            omega = new Hasher("omega"),
            alpha_ = new Hasher("ALPHA"), beta_ = new Hasher("BETA"), gamma_ = new Hasher("GAMMA"),
            delta_ = new Hasher("DELTA"), epsilon_ = new Hasher("EPSILON"), zeta_ = new Hasher("ZETA"),
            eta_ = new Hasher("ETA"), theta_ = new Hasher("THETA"), iota_ = new Hasher("IOTA"),
            kappa_ = new Hasher("KAPPA"), lambda_ = new Hasher("LAMBDA"), mu_ = new Hasher("MU"),
            nu_ = new Hasher("NU"), xi_ = new Hasher("XI"), omicron_ = new Hasher("OMICRON"), pi_ = new Hasher("PI"),
            rho_ = new Hasher("RHO"), sigma_ = new Hasher("SIGMA"), tau_ = new Hasher("TAU"),
            upsilon_ = new Hasher("UPSILON"), phi_ = new Hasher("PHI"), chi_ = new Hasher("CHI"), psi_ = new Hasher("PSI"),
            omega_ = new Hasher("OMEGA"),
            baal = new Hasher("baal"), agares = new Hasher("agares"), vassago = new Hasher("vassago"), samigina = new Hasher("samigina"),
            marbas = new Hasher("marbas"), valefor = new Hasher("valefor"), amon = new Hasher("amon"), barbatos = new Hasher("barbatos"),
            paimon = new Hasher("paimon"), buer = new Hasher("buer"), gusion = new Hasher("gusion"), sitri = new Hasher("sitri"),
            beleth = new Hasher("beleth"), leraje = new Hasher("leraje"), eligos = new Hasher("eligos"), zepar = new Hasher("zepar"),
            botis = new Hasher("botis"), bathin = new Hasher("bathin"), sallos = new Hasher("sallos"), purson = new Hasher("purson"),
            marax = new Hasher("marax"), ipos = new Hasher("ipos"), aim = new Hasher("aim"), naberius = new Hasher("naberius"),
            glasya_labolas = new Hasher("glasya_labolas"), bune = new Hasher("bune"), ronove = new Hasher("ronove"), berith = new Hasher("berith"),
            astaroth = new Hasher("astaroth"), forneus = new Hasher("forneus"), foras = new Hasher("foras"), asmoday = new Hasher("asmoday"),
            gaap = new Hasher("gaap"), furfur = new Hasher("furfur"), marchosias = new Hasher("marchosias"), stolas = new Hasher("stolas"),
            phenex = new Hasher("phenex"), halphas = new Hasher("halphas"), malphas = new Hasher("malphas"), raum = new Hasher("raum"),
            focalor = new Hasher("focalor"), vepar = new Hasher("vepar"), sabnock = new Hasher("sabnock"), shax = new Hasher("shax"),
            vine = new Hasher("vine"), bifrons = new Hasher("bifrons"), vual = new Hasher("vual"), haagenti = new Hasher("haagenti"),
            crocell = new Hasher("crocell"), furcas = new Hasher("furcas"), balam = new Hasher("balam"), alloces = new Hasher("alloces"),
            caim = new Hasher("caim"), murmur = new Hasher("murmur"), orobas = new Hasher("orobas"), gremory = new Hasher("gremory"),
            ose = new Hasher("ose"), amy = new Hasher("amy"), orias = new Hasher("orias"), vapula = new Hasher("vapula"),
            zagan = new Hasher("zagan"), valac = new Hasher("valac"), andras = new Hasher("andras"), flauros = new Hasher("flauros"),
            andrealphus = new Hasher("andrealphus"), kimaris = new Hasher("kimaris"), amdusias = new Hasher("amdusias"), belial = new Hasher("belial"),
            decarabia = new Hasher("decarabia"), seere = new Hasher("seere"), dantalion = new Hasher("dantalion"), andromalius = new Hasher("andromalius"),
            baal_ = new Hasher("BAAL"), agares_ = new Hasher("AGARES"), vassago_ = new Hasher("VASSAGO"), samigina_ = new Hasher("SAMIGINA"),
            marbas_ = new Hasher("MARBAS"), valefor_ = new Hasher("VALEFOR"), amon_ = new Hasher("AMON"), barbatos_ = new Hasher("BARBATOS"),
            paimon_ = new Hasher("PAIMON"), buer_ = new Hasher("BUER"), gusion_ = new Hasher("GUSION"), sitri_ = new Hasher("SITRI"),
            beleth_ = new Hasher("BELETH"), leraje_ = new Hasher("LERAJE"), eligos_ = new Hasher("ELIGOS"), zepar_ = new Hasher("ZEPAR"),
            botis_ = new Hasher("BOTIS"), bathin_ = new Hasher("BATHIN"), sallos_ = new Hasher("SALLOS"), purson_ = new Hasher("PURSON"),
            marax_ = new Hasher("MARAX"), ipos_ = new Hasher("IPOS"), aim_ = new Hasher("AIM"), naberius_ = new Hasher("NABERIUS"),
            glasya_labolas_ = new Hasher("GLASYA_LABOLAS"), bune_ = new Hasher("BUNE"), ronove_ = new Hasher("RONOVE"), berith_ = new Hasher("BERITH"),
            astaroth_ = new Hasher("ASTAROTH"), forneus_ = new Hasher("FORNEUS"), foras_ = new Hasher("FORAS"), asmoday_ = new Hasher("ASMODAY"),
            gaap_ = new Hasher("GAAP"), furfur_ = new Hasher("FURFUR"), marchosias_ = new Hasher("MARCHOSIAS"), stolas_ = new Hasher("STOLAS"),
            phenex_ = new Hasher("PHENEX"), halphas_ = new Hasher("HALPHAS"), malphas_ = new Hasher("MALPHAS"), raum_ = new Hasher("RAUM"),
            focalor_ = new Hasher("FOCALOR"), vepar_ = new Hasher("VEPAR"), sabnock_ = new Hasher("SABNOCK"), shax_ = new Hasher("SHAX"),
            vine_ = new Hasher("VINE"), bifrons_ = new Hasher("BIFRONS"), vual_ = new Hasher("VUAL"), haagenti_ = new Hasher("HAAGENTI"),
            crocell_ = new Hasher("CROCELL"), furcas_ = new Hasher("FURCAS"), balam_ = new Hasher("BALAM"), alloces_ = new Hasher("ALLOCES"),
            caim_ = new Hasher("CAIM"), murmur_ = new Hasher("MURMUR"), orobas_ = new Hasher("OROBAS"), gremory_ = new Hasher("GREMORY"),
            ose_ = new Hasher("OSE"), amy_ = new Hasher("AMY"), orias_ = new Hasher("ORIAS"), vapula_ = new Hasher("VAPULA"),
            zagan_ = new Hasher("ZAGAN"), valac_ = new Hasher("VALAC"), andras_ = new Hasher("ANDRAS"), flauros_ = new Hasher("FLAUROS"),
            andrealphus_ = new Hasher("ANDREALPHUS"), kimaris_ = new Hasher("KIMARIS"), amdusias_ = new Hasher("AMDUSIAS"), belial_ = new Hasher("BELIAL"),
            decarabia_ = new Hasher("DECARABIA"), seere_ = new Hasher("SEERE"), dantalion_ = new Hasher("DANTALION"), andromalius_ = new Hasher("ANDROMALIUS"),
            hydrogen = new Hasher("hydrogen"), helium = new Hasher("helium"), lithium = new Hasher("lithium"), beryllium = new Hasher("beryllium"), boron = new Hasher("boron"), carbon = new Hasher("carbon"), nitrogen = new Hasher("nitrogen"), oxygen = new Hasher("oxygen"), fluorine = new Hasher("fluorine"), neon = new Hasher("neon"), sodium = new Hasher("sodium"), magnesium = new Hasher("magnesium"), aluminium = new Hasher("aluminium"), silicon = new Hasher("silicon"), phosphorus = new Hasher("phosphorus"), sulfur = new Hasher("sulfur"), chlorine = new Hasher("chlorine"), argon = new Hasher("argon"), potassium = new Hasher("potassium"), calcium = new Hasher("calcium"), scandium = new Hasher("scandium"), titanium = new Hasher("titanium"), vanadium = new Hasher("vanadium"), chromium = new Hasher("chromium"), manganese = new Hasher("manganese"), iron = new Hasher("iron"), cobalt = new Hasher("cobalt"), nickel = new Hasher("nickel"), copper = new Hasher("copper"), zinc = new Hasher("zinc"), gallium = new Hasher("gallium"), germanium = new Hasher("germanium"), arsenic = new Hasher("arsenic"), selenium = new Hasher("selenium"), bromine = new Hasher("bromine"), krypton = new Hasher("krypton"), rubidium = new Hasher("rubidium"), strontium = new Hasher("strontium"), yttrium = new Hasher("yttrium"), zirconium = new Hasher("zirconium"), niobium = new Hasher("niobium"), molybdenum = new Hasher("molybdenum"), technetium = new Hasher("technetium"), ruthenium = new Hasher("ruthenium"), rhodium = new Hasher("rhodium"), palladium = new Hasher("palladium"), silver = new Hasher("silver"), cadmium = new Hasher("cadmium"), indium = new Hasher("indium"), tin = new Hasher("tin"), antimony = new Hasher("antimony"), tellurium = new Hasher("tellurium"), iodine = new Hasher("iodine"), xenon = new Hasher("xenon"), caesium = new Hasher("caesium"), barium = new Hasher("barium"), lanthanum = new Hasher("lanthanum"), cerium = new Hasher("cerium"), praseodymium = new Hasher("praseodymium"), neodymium = new Hasher("neodymium"), promethium = new Hasher("promethium"), samarium = new Hasher("samarium"), europium = new Hasher("europium"), gadolinium = new Hasher("gadolinium"), terbium = new Hasher("terbium"), dysprosium = new Hasher("dysprosium"), holmium = new Hasher("holmium"), erbium = new Hasher("erbium"), thulium = new Hasher("thulium"), ytterbium = new Hasher("ytterbium"), lutetium = new Hasher("lutetium"), hafnium = new Hasher("hafnium"), tantalum = new Hasher("tantalum"), tungsten = new Hasher("tungsten"), rhenium = new Hasher("rhenium"), osmium = new Hasher("osmium"), iridium = new Hasher("iridium"), platinum = new Hasher("platinum"), gold = new Hasher("gold"), mercury = new Hasher("mercury"), thallium = new Hasher("thallium"), lead = new Hasher("lead"), bismuth = new Hasher("bismuth"), polonium = new Hasher("polonium"), astatine = new Hasher("astatine"), radon = new Hasher("radon"), francium = new Hasher("francium"), radium = new Hasher("radium"), actinium = new Hasher("actinium"), thorium = new Hasher("thorium"), protactinium = new Hasher("protactinium"), uranium = new Hasher("uranium"), neptunium = new Hasher("neptunium"), plutonium = new Hasher("plutonium"), americium = new Hasher("americium"), curium = new Hasher("curium"), berkelium = new Hasher("berkelium"), californium = new Hasher("californium"), einsteinium = new Hasher("einsteinium"), fermium = new Hasher("fermium"), mendelevium = new Hasher("mendelevium"), nobelium = new Hasher("nobelium"), lawrencium = new Hasher("lawrencium"), rutherfordium = new Hasher("rutherfordium"), dubnium = new Hasher("dubnium"), seaborgium = new Hasher("seaborgium"), bohrium = new Hasher("bohrium"), hassium = new Hasher("hassium"), meitnerium = new Hasher("meitnerium"), darmstadtium = new Hasher("darmstadtium"), roentgenium = new Hasher("roentgenium"), copernicium = new Hasher("copernicium"), nihonium = new Hasher("nihonium"), flerovium = new Hasher("flerovium"), moscovium = new Hasher("moscovium"), livermorium = new Hasher("livermorium"), tennessine = new Hasher("tennessine"), oganesson = new Hasher("oganesson"),
            hydrogen_ = new Hasher("HYDROGEN"), helium_ = new Hasher("HELIUM"), lithium_ = new Hasher("LITHIUM"), beryllium_ = new Hasher("BERYLLIUM"), boron_ = new Hasher("BORON"), carbon_ = new Hasher("CARBON"), nitrogen_ = new Hasher("NITROGEN"), oxygen_ = new Hasher("OXYGEN"), fluorine_ = new Hasher("FLUORINE"), neon_ = new Hasher("NEON"), sodium_ = new Hasher("SODIUM"), magnesium_ = new Hasher("MAGNESIUM"), aluminium_ = new Hasher("ALUMINIUM"), silicon_ = new Hasher("SILICON"), phosphorus_ = new Hasher("PHOSPHORUS"), sulfur_ = new Hasher("SULFUR"), chlorine_ = new Hasher("CHLORINE"), argon_ = new Hasher("ARGON"), potassium_ = new Hasher("POTASSIUM"), calcium_ = new Hasher("CALCIUM"), scandium_ = new Hasher("SCANDIUM"), titanium_ = new Hasher("TITANIUM"), vanadium_ = new Hasher("VANADIUM"), chromium_ = new Hasher("CHROMIUM"), manganese_ = new Hasher("MANGANESE"), iron_ = new Hasher("IRON"), cobalt_ = new Hasher("COBALT"), nickel_ = new Hasher("NICKEL"), copper_ = new Hasher("COPPER"), zinc_ = new Hasher("ZINC"), gallium_ = new Hasher("GALLIUM"), germanium_ = new Hasher("GERMANIUM"), arsenic_ = new Hasher("ARSENIC"), selenium_ = new Hasher("SELENIUM"), bromine_ = new Hasher("BROMINE"), krypton_ = new Hasher("KRYPTON"), rubidium_ = new Hasher("RUBIDIUM"), strontium_ = new Hasher("STRONTIUM"), yttrium_ = new Hasher("YTTRIUM"), zirconium_ = new Hasher("ZIRCONIUM"), niobium_ = new Hasher("NIOBIUM"), molybdenum_ = new Hasher("MOLYBDENUM"), technetium_ = new Hasher("TECHNETIUM"), ruthenium_ = new Hasher("RUTHENIUM"), rhodium_ = new Hasher("RHODIUM"), palladium_ = new Hasher("PALLADIUM"), silver_ = new Hasher("SILVER"), cadmium_ = new Hasher("CADMIUM"), indium_ = new Hasher("INDIUM"), tin_ = new Hasher("TIN"), antimony_ = new Hasher("ANTIMONY"), tellurium_ = new Hasher("TELLURIUM"), iodine_ = new Hasher("IODINE"), xenon_ = new Hasher("XENON"), caesium_ = new Hasher("CAESIUM"), barium_ = new Hasher("BARIUM"), lanthanum_ = new Hasher("LANTHANUM"), cerium_ = new Hasher("CERIUM"), praseodymium_ = new Hasher("PRASEODYMIUM"), neodymium_ = new Hasher("NEODYMIUM"), promethium_ = new Hasher("PROMETHIUM"), samarium_ = new Hasher("SAMARIUM"), europium_ = new Hasher("EUROPIUM"), gadolinium_ = new Hasher("GADOLINIUM"), terbium_ = new Hasher("TERBIUM"), dysprosium_ = new Hasher("DYSPROSIUM"), holmium_ = new Hasher("HOLMIUM"), erbium_ = new Hasher("ERBIUM"), thulium_ = new Hasher("THULIUM"), ytterbium_ = new Hasher("YTTERBIUM"), lutetium_ = new Hasher("LUTETIUM"), hafnium_ = new Hasher("HAFNIUM"), tantalum_ = new Hasher("TANTALUM"), tungsten_ = new Hasher("TUNGSTEN"), rhenium_ = new Hasher("RHENIUM"), osmium_ = new Hasher("OSMIUM"), iridium_ = new Hasher("IRIDIUM"), platinum_ = new Hasher("PLATINUM"), gold_ = new Hasher("GOLD"), mercury_ = new Hasher("MERCURY"), thallium_ = new Hasher("THALLIUM"), lead_ = new Hasher("LEAD"), bismuth_ = new Hasher("BISMUTH"), polonium_ = new Hasher("POLONIUM"), astatine_ = new Hasher("ASTATINE"), radon_ = new Hasher("RADON"), francium_ = new Hasher("FRANCIUM"), radium_ = new Hasher("RADIUM"), actinium_ = new Hasher("ACTINIUM"), thorium_ = new Hasher("THORIUM"), protactinium_ = new Hasher("PROTACTINIUM"), uranium_ = new Hasher("URANIUM"), neptunium_ = new Hasher("NEPTUNIUM"), plutonium_ = new Hasher("PLUTONIUM"), americium_ = new Hasher("AMERICIUM"), curium_ = new Hasher("CURIUM"), berkelium_ = new Hasher("BERKELIUM"), californium_ = new Hasher("CALIFORNIUM"), einsteinium_ = new Hasher("EINSTEINIUM"), fermium_ = new Hasher("FERMIUM"), mendelevium_ = new Hasher("MENDELEVIUM"), nobelium_ = new Hasher("NOBELIUM"), lawrencium_ = new Hasher("LAWRENCIUM"), rutherfordium_ = new Hasher("RUTHERFORDIUM"), dubnium_ = new Hasher("DUBNIUM"), seaborgium_ = new Hasher("SEABORGIUM"), bohrium_ = new Hasher("BOHRIUM"), hassium_ = new Hasher("HASSIUM"), meitnerium_ = new Hasher("MEITNERIUM"), darmstadtium_ = new Hasher("DARMSTADTIUM"), roentgenium_ = new Hasher("ROENTGENIUM"), copernicium_ = new Hasher("COPERNICIUM"), nihonium_ = new Hasher("NIHONIUM"), flerovium_ = new Hasher("FLEROVIUM"), moscovium_ = new Hasher("MOSCOVIUM"), livermorium_ = new Hasher("LIVERMORIUM"), tennessine_ = new Hasher("TENNESSINE"), oganesson_ = new Hasher("OGANESSON");

    /**
     * Has a length of 428, which may be relevant if automatically choosing a predefined hash functor.
     */
    public static final Hasher[] predefined = new Hasher[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
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
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) - seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 2:
                seed = mum((data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum((data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) + mum(seed ^ b5, b4 ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) - seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data.charAt(start + len - 1)));
                break;
            case 2:
                seed = mum(data.charAt(start + len - 2) - seed, b0 ^ data.charAt(start + len - 1));
                break;
            case 3:
                seed = mum(data.charAt(start + len - 3) - seed, b2 ^ data.charAt(start + len - 2)) + mum(b5 ^ seed, b4 ^ (data.charAt(start + len - 1)));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum((data[start + len - 1] >>> 16) - seed, b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum((data[start + len - 1] >>> 16) ^ seed, b4 ^ (data[start + len - 1] & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        int n;
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) - seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum((n >>> 16) - seed, b3 ^ (n & 0xFFFFL));
                break;
            case 2:
                seed = mum(floatToRawIntBits(data[start + len - 2]) - seed, b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum(floatToRawIntBits(data[start + len - 3]) - seed, b2 ^ floatToRawIntBits(data[start + len - 2])) + mum((n >>> 16) ^ seed, b4 ^ (n & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public long hash64(final double[] data) {
        if (data == null) return 0;
        return hash64(data, 0, data.length);
    }
    public long hash64(final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        while (it.hasNext()) {
            ++len;
            a ^= hash64(it.next()) * b1;
            a = (a << 23 | a >>> 41) * b3;
            if(it.hasNext()) {
                ++len;
                b ^= hash64(it.next()) * b2;
                b = (b << 25 | b >>> 39) * b4;
            }
            if(it.hasNext()) {
                ++len;
                c ^= hash64(it.next()) * b3;
                c = (c << 29 | c >>> 35) * b5;
            }
            if(it.hasNext()) {
                ++len;
                d ^= hash64(it.next()) * b4;
                d = (d << 31 | d >>> 33) * b1;
            }
            seed += a + b + c + d;
        }
        seed += b5;
        seed = wow(b1 - seed, b4 + seed);
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data.get(i - 3)) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data.get(i - 2)) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data.get(i - 1)) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data.get(i)) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data.get(start + len - 1)));
                break;
            case 2:
                seed = wow(seed + hash64(data.get(start + len - 2)), b2 ^ hash64(data.get(start + len - 1)));
                break;
            case 3:
                seed = wow(seed + hash64(data.get(start + len - 3)), b2 + hash64(data.get(start + len - 2))) + wow(seed + hash64(data.get(start + len - 1)), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public long hash64(final Object data) {
        if (data == null)
            return 0;
        return wow(data.hashCode() ^ b4, b5 - seed) ^ seed;
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
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) - seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 2:
                seed = mum((data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum((data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) + mum(seed ^ b5, b4 ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) - seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data.charAt(start + len - 1)));
                break;
            case 2:
                seed = mum(data.charAt(start + len - 2) - seed, b0 ^ data.charAt(start + len - 1));
                break;
            case 3:
                seed = mum(data.charAt(start + len - 3) - seed, b2 ^ data.charAt(start + len - 2)) + mum(b5 ^ seed, b4 ^ (data.charAt(start + len - 1)));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum((data[start + len - 1] >>> 16) - seed, b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum((data[start + len - 1] >>> 16) ^ seed, b4 ^ (data[start + len - 1] & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        int n;
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) - seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum((n >>> 16) - seed, b3 ^ (n & 0xFFFFL));
                break;
            case 2:
                seed = mum(floatToRawIntBits(data[start + len - 2]) - seed, b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum(floatToRawIntBits(data[start + len - 3]) - seed, b2 ^ floatToRawIntBits(data[start + len - 2])) + mum((n >>> 16) ^ seed, b4 ^ (n & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public int hash(final double[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }
    public int hash(final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long seed = this.seed;
        final int len = Math.min(length, data.length - start);
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public int hash(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        while (it.hasNext()) {
            ++len;
            a ^= hash64(it.next()) * b1;
            a = (a << 23 | a >>> 41) * b3;
            if(it.hasNext()) {
                ++len;
                b ^= hash64(it.next()) * b2;
                b = (b << 25 | b >>> 39) * b4;
            }
            if(it.hasNext()) {
                ++len;
                c ^= hash64(it.next()) * b3;
                c = (c << 29 | c >>> 35) * b5;
            }
            if(it.hasNext()) {
                ++len;
                d ^= hash64(it.next()) * b4;
                d = (d << 31 | d >>> 33) * b1;
            }
            seed += a + b + c + d;
        }
        seed += b5;
        seed = wow(b1 - seed, b4 + seed);
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data.get(i - 3)) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data.get(i - 2)) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data.get(i - 1)) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data.get(i)) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data.get(start + len - 1)));
                break;
            case 2:
                seed = wow(seed + hash64(data.get(start + len - 2)), b2 ^ hash64(data.get(start + len - 1)));
                break;
            case 3:
                seed = wow(seed + hash64(data.get(start + len - 3)), b2 + hash64(data.get(start + len - 2))) + wow(seed + hash64(data.get(start + len - 1)), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(data[start + len - 2]), b2 ^ hash64(data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(data[start + len - 3]), b2 + hash64(data[start + len - 2])) + wow(seed + hash64(data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public int hash(final Object data) {
        if (data == null) return 0;
        return (int)(mum(data.hashCode() ^ b2, b3 - seed) ^ seed);
    }


    public static long hash64(long seed, final boolean[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) - seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 2:
                seed = mum((data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum((data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) + mum(seed ^ b5, b4 ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final byte[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final short[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        final int len = Math.min(length, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) - seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data.charAt(start + len - 1)));
                break;
            case 2:
                seed = mum(data.charAt(start + len - 2) - seed, b0 ^ data.charAt(start + len - 1));
                break;
            case 3:
                seed = mum(data.charAt(start + len - 3) - seed, b2 ^ data.charAt(start + len - 2)) + mum(b5 ^ seed, b4 ^ (data.charAt(start + len - 1)));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final int[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum((data[start + len - 1] >>> 16) - seed, b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum((data[start + len - 1] >>> 16) ^ seed, b4 ^ (data[start + len - 1] & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45);
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final float[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        int n;
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) - seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum((n >>> 16) - seed, b3 ^ (n & 0xFFFFL));
                break;
            case 2:
                seed = mum(floatToRawIntBits(data[start + len - 2]) - seed, b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum(floatToRawIntBits(data[start + len - 3]) - seed, b2 ^ floatToRawIntBits(data[start + len - 2])) + mum((n >>> 16) ^ seed, b4 ^ (n & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final double[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final byte[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final byte[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final char[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final char[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final float[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final float[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final double[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final double[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));

    }

    public static long hash64(long seed, final int[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final int[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final long[][] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final long[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final CharSequence[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final CharSequence[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final CharSequence[]... data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final CharSequence[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        while (it.hasNext()) {
            ++len;
            a ^= hash64(seed, it.next()) * b1;
            a = (a << 23 | a >>> 41) * b3;
            if(it.hasNext()) {
                ++len;
                b ^= hash64(seed, it.next()) * b2;
                b = (b << 25 | b >>> 39) * b4;
            }
            if(it.hasNext()) {
                ++len;
                c ^= hash64(seed, it.next()) * b3;
                c = (c << 29 | c >>> 35) * b5;
            }
            if(it.hasNext()) {
                ++len;
                d ^= hash64(seed, it.next()) * b4;
                d = (d << 31 | d >>> 33) * b1;
            }
            seed += a + b + c + d;
        }
        seed += b5;
        seed = wow(b1 - seed, b4 + seed);
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.size());
    }
    public static long hash64(long seed, final List<? extends CharSequence> data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.size())
            return 0;
        final int len = Math.min(length, data.size() - start);
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data.get(i - 3)) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data.get(i - 2)) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data.get(i - 1)) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data.get(i)) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data.get(start + len - 1)));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data.get(start + len - 2)), b2 ^ hash64(seed, data.get(start + len - 1)));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data.get(start + len - 3)), b2 + hash64(seed, data.get(start + len - 2))) + wow(seed + hash64(seed, data.get(start + len - 1)), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final Object[] data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length);
    }
    public static long hash64(long seed, final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static long hash64(long seed, final Object data) {
        if (data == null)
            return 0;
        return wow(data.hashCode() ^ b4, b5 - seed) ^ seed;
    }

    public static int hash(long seed, final boolean[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final boolean[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum((data[i - 3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i - 2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) - seed,
                    mum((data[i - 1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 2:
                seed = mum((data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b0 ^ (data[len - 1] ? 0x9E3779B9L : 0x7F4A7C15L));
                break;
            case 3:
                seed = mum((data[len - 3] ? 0x9E3779B9L : 0x7F4A7C15L) - seed, b2 ^ (data[len - 2] ? 0x9E3779B9L : 0x7F4A7C15L)) + mum(seed ^ b5, b4 ^ (data[len - 1] ? 0x9E3779B9 : 0x7F4A7C15));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final byte[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final short[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final char[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data[start + len - 1]));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum(b5 ^ seed, b4 ^ (data[start + len - 1]));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        final int len = Math.min(length, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) - seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data.charAt(start + len - 1)));
                break;
            case 2:
                seed = mum(data.charAt(start + len - 2) - seed, b0 ^ data.charAt(start + len - 1));
                break;
            case 3:
                seed = mum(data.charAt(start + len - 3) - seed, b2 ^ data.charAt(start + len - 2)) + mum(b5 ^ seed, b4 ^ (data.charAt(start + len - 1)));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));

    }

    public static int hash(long seed, final int[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data[i - 3] ^ b1, data[i - 2] ^ b2) - seed,
                    mum(data[i - 1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum((data[start + len - 1] >>> 16) - seed, b3 ^ (data[start + len - 1] & 0xFFFFL));
                break;
            case 2:
                seed = mum(data[start + len - 2] - seed, b0 ^ data[start + len - 1]);
                break;
            case 3:
                seed = mum(data[start + len - 3] - seed, b2 ^ data[start + len - 2]) + mum((data[start + len - 1] >>> 16) ^ seed, b4 ^ (data[start + len - 1] & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
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
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final float[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final float[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        final int len = Math.min(length, data.length - start);
        int n;
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i - 3]) ^ b1, floatToRawIntBits(data[i - 2]) ^ b2) - seed,
                    mum(floatToRawIntBits(data[i - 1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum((n >>> 16) - seed, b3 ^ (n & 0xFFFFL));
                break;
            case 2:
                seed = mum(floatToRawIntBits(data[start + len - 2]) - seed, b0 ^ floatToRawIntBits(data[start + len - 1]));
                break;
            case 3:
                n = floatToRawIntBits(data[start + len - 1]);
                seed = mum(floatToRawIntBits(data[start + len - 3]) - seed, b2 ^ floatToRawIntBits(data[start + len - 2])) + mum((n >>> 16) ^ seed, b4 ^ (n & 0xFFFFL));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final double[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final double[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
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
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
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
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));

    }

    public static int hash(long seed, final byte[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final byte[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final char[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final char[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final float[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final float[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final double[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final double[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final int[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final int[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final long[][] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final long[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final CharSequence[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final CharSequence[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final CharSequence[]... data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final CharSequence[][] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        while (it.hasNext()) {
            ++len;
            a ^= hash64(seed, it.next()) * b1;
            a = (a << 23 | a >>> 41) * b3;
            if(it.hasNext()) {
                ++len;
                b ^= hash64(seed, it.next()) * b2;
                b = (b << 25 | b >>> 39) * b4;
            }
            if(it.hasNext()) {
                ++len;
                c ^= hash64(seed, it.next()) * b3;
                c = (c << 29 | c >>> 35) * b5;
            }
            if(it.hasNext()) {
                ++len;
                d ^= hash64(seed, it.next()) * b4;
                d = (d << 31 | d >>> 33) * b1;
            }
            seed += a + b + c + d;
        }
        seed += b5;
        seed = wow(b1 - seed, b4 + seed);
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.size());
    }
    public static int hash(long seed, final List<? extends CharSequence> data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.size())
            return 0;
        final int len = Math.min(length, data.size() - start);
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data.get(i - 3)) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data.get(i - 2)) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data.get(i - 1)) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data.get(i)) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data.get(start + len - 1)));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data.get(start + len - 2)), b2 ^ hash64(seed, data.get(start + len - 1)));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data.get(start + len - 3)), b2 + hash64(seed, data.get(start + len - 2))) + wow(seed + hash64(seed, data.get(start + len - 1)), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final Object[] data) {
        if (data == null) return 0;
        return hash(seed, data, 0, data.length);
    }
    public static int hash(long seed, final Object[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        long a = seed + b4, b = a ^ b3, c = b - b2, d = c ^ b1;
        final int len = Math.min(length, data.length - start);
        for (int i = start + 3; i < len; i += 4) {
            a ^= hash64(seed, data[i - 3]) * b1;
            a = (a << 23 | a >>> 41) * b3;
            b ^= hash64(seed, data[i - 2]) * b2;
            b = (b << 25 | b >>> 39) * b4;
            c ^= hash64(seed, data[i - 1]) * b3;
            c = (c << 29 | c >>> 35) * b5;
            d ^= hash64(seed, data[i]) * b4;
            d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 0:
                seed = wow(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = wow(seed, b1 ^ hash64(seed, data[start + len - 1]));
                break;
            case 2:
                seed = wow(seed + hash64(seed, data[start + len - 2]), b2 ^ hash64(seed, data[start + len - 1]));
                break;
            case 3:
                seed = wow(seed + hash64(seed, data[start + len - 3]), b2 + hash64(seed, data[start + len - 2])) + wow(seed + hash64(seed, data[start + len - 1]), seed ^ b3);
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (int)(seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

    public static int hash(long seed, final Object data) {
        if (data == null)
            return 0;
        return (int)(mum(data.hashCode() ^ b2, b3 - seed) ^ seed);
    }

    // bulk hashing section, member functions

    /**
     * A hashing function that is likely to outperform {@link #hash64(long[])} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param data input array
     * @return the 64-bit hash of data
     */
    public long hashBulk64(final long[] data) {
        if (data == null) return 0;
        return hashBulk64(data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash64(long[], int, int)} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 64-bit hash of data
     */
    public long hashBulk64(final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return mix(h);
    }


    /**
     * A hashing function that is likely to outperform {@link #hash64(int[])} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param data input array
     * @return the 64-bit hash of data
     */
    public long hashBulk64(final int[] data) {
        if (data == null) return 0;
        return hashBulk64(data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash64(int[], int, int)} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 64-bit hash of data
     */
    public long hashBulk64(final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return mix(h);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(long[])} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashBulk(final long[] data) {
        if (data == null) return 0;
        return hashBulk(data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(long[], int, int)} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashBulk(final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return (int)mix(h);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(int[])} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hashBulk(final int[] data) {
        if (data == null) return 0;
        return hashBulk(data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(int[], int, int)} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hashBulk(final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return (int)mix(h);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public <T> long hashBulk64(final HashFunction64<T> function, final T[] data) {
        if (data == null) return 0;
        return hashBulk64(function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public <T> long hashBulk64(final HashFunction64<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash64(data[i  ]), function.hash64(data[i+1]), function.hash64(data[i+2]), function.hash64(data[i+3]));
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash64(data[i+4]), function.hash64(data[i+5]), function.hash64(data[i+6]), function.hash64(data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash64(data[i++]));
        }
        return mix(h);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash} method here.
     * @param function typically a method reference to a {@link #hash} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public <T> long hashBulk64(final HashFunction<T> function, final T[] data) {
        if (data == null) return 0;
        return hashBulk64(function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash} method here.
     * @param function typically a method reference to a {@link #hash} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public <T> long hashBulk64(final HashFunction<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash(data[i  ]), function.hash(data[i+1]), function.hash(data[i+2]), function.hash(data[i+3]));
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash(data[i+4]), function.hash(data[i+5]), function.hash(data[i+6]), function.hash(data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash(data[i++]));
        }
        return mix(h);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public <T> int hashBulk(final HashFunction64<T> function, final T[] data) {
        if (data == null) return 0;
        return hashBulk(function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public <T> int hashBulk(final HashFunction64<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash64(data[i  ]), function.hash64(data[i+1]), function.hash64(data[i+2]), function.hash64(data[i+3]));
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash64(data[i+4]), function.hash64(data[i+5]), function.hash64(data[i+6]), function.hash64(data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash64(data[i++]));
        }
        return (int)mix(h);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash} method here.
     * @param function typically a method reference to a {@link #hash} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public <T> int hashBulk(final HashFunction<T> function, final T[] data) {
        if (data == null) return 0;
        return hashBulk(function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link HashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash} method here.
     * @param function typically a method reference to a {@link #hash} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 32-bit hash of data
     */
    public <T> int hashBulk(final HashFunction<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash(data[i  ]), function.hash(data[i+1]), function.hash(data[i+2]), function.hash(data[i+3]));
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash(data[i+4]), function.hash(data[i+5]), function.hash(data[i+6]), function.hash(data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash(data[i++]));
        }
        return (int)mix(h);
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, hashing everything from index 0 to just before index
     * {@link ByteBuffer#limit()}. The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash64(byte[])} on all but
     * the smallest sequences of bytes (under 20 bytes).
     * @param data an input ByteBuffer
     * @return the 64-bit hash of data
     */
    public long hashBulk64(final ByteBuffer data) {
        return hashBulk64(data, 0, data.limit());
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, using the given {@code start} index (measured in bytes)
     * and {@code length} (also in bytes). The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash64(byte[], int, int)}
     * on all but the smallest sequences of bytes (under 20 bytes).
     * @param data an input ByteBuffer
     * @param start the starting index, measured in bytes
     * @param length the number of bytes to hash
     * @return the 64-bit hash of data
     */
    public long hashBulk64(final ByteBuffer data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.limit())
            return 0;
        int len = Math.min(length, data.limit() - start);
        data.position(start);
        long h = len ^ forward(seed);
        while(len >= 64){
            h *= C;
            len -= 64;
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
        }
        while(len >= 8){
            len -= 8;
            h = mixStream(h, data.getLong());
        }
        switch (len) {
            case 1: return  mix(mixStream(h, (data.get())));
            case 2: return  mix(mixStream(h, (data.getShort())));
            case 3: return  mix(mixStream(h, (data.getShort()) ^ ((long)data.get()) << 16));
            case 4: return  mix(mixStream(h, (data.getInt())));
            case 5: return  mix(mixStream(h, (data.getInt()) ^ ((long)data.get()) << 32));
            case 6: return  mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32));
            case 7: return  mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32 ^ ((long)data.get()) << 48));
            default: return mix(h);
        }
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, hashing everything from index 0 to just before index
     * {@link ByteBuffer#limit()}. The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash(byte[])} on all but
     * the smallest sequences of bytes (under 20 bytes).
     * @param data an input ByteBuffer
     * @return the 32-bit hash of data
     */
    public int hashBulk(final ByteBuffer data) {
        return hashBulk(data, 0, data.limit());
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, using the given {@code start} index (measured in bytes)
     * and {@code length} (also in bytes). The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash(byte[], int, int)}
     * on all but the smallest sequences of bytes (under 20 bytes).
     * @param data an input ByteBuffer
     * @param start the starting index, measured in bytes
     * @param length the number of bytes to hash
     * @return the 32-bit hash of data
     */
    public int hashBulk(final ByteBuffer data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.limit())
            return 0;
        int len = Math.min(length, data.limit() - start);
        data.position(start);
        long h = len ^ forward(seed);
        while(len >= 64){
            h *= C;
            len -= 64;
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
        }
        while(len >= 8){
            len -= 8;
            h = mixStream(h, data.getLong());
        }
        switch (len) {
            case 1: return  (int)mix(mixStream(h, (data.get())));
            case 2: return  (int)mix(mixStream(h, (data.getShort())));
            case 3: return  (int)mix(mixStream(h, (data.getShort()) ^ ((long)data.get()) << 16));
            case 4: return  (int)mix(mixStream(h, (data.getInt())));
            case 5: return  (int)mix(mixStream(h, (data.getInt()) ^ ((long)data.get()) << 32));
            case 6: return  (int)mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32));
            case 7: return  (int)mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32 ^ ((long)data.get()) << 48));
            default: return (int)mix(h);
        }
    }
    
    // bulk hashing section, seeded static functions

    /**
     * A hashing function that is likely to outperform {@link #hash64(long[])} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param seed any long seed
     * @param data input array
     * @return the 64-bit hash of data
     */
    public static long hashBulk64(final long seed, final long[] data) {
        if (data == null) return 0;
        return hashBulk64(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash64(long[], int, int)} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param seed any long seed
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 64-bit hash of data
     */
    public static long hashBulk64(final long seed, final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return mix(h);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash64(int[])} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param seed any long seed
     * @param data input array
     * @return the 64-bit hash of data
     */
    public static long hashBulk64(final long seed, final int[] data) {
        if (data == null) return 0;
        return hashBulk64(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash64(int[], int, int)} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param seed any long seed
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 64-bit hash of data
     */
    public static long hashBulk64(final long seed, final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return mix(h);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(long[])} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashBulk(final long seed, final long[] data) {
        if (data == null) return 0;
        return hashBulk(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(long[], int, int)} on longer input arrays
     * (length 50 and up). It is probably a little slower on the smallest input arrays.
     * @param seed any long seed
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashBulk(final long seed, final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return (int)mix(h);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(int[])} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param seed any long seed
     * @param data input array
     * @return the 32-bit hash of data
     */
    public static int hashBulk(final long seed, final int[] data) {
        if (data == null) return 0;
        return hashBulk(seed, data, 0, data.length);
    }

    /**
     * A hashing function that is likely to outperform {@link #hash(int[], int, int)} on much longer input arrays
     * (length 5000 and up). It is probably a little slower on smaller input arrays.
     * @param seed any long seed
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public static int hashBulk(final long seed, final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(data[i  ], data[i+1], data[i+2], data[i+3]);
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data[i+4], data[i+5], data[i+6], data[i+7]);
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, data[i++]);
        }
        return (int)mix(h);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> long hashBulk64(final long seed, final SeededHashFunction64<T> function, final T[] data) {
        if (data == null) return 0;
        return hashBulk64(seed, function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> long hashBulk64(final long seed, final SeededHashFunction64<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash64(seed, data[i  ]), function.hash64(seed, data[i+1]), function.hash64(seed, data[i+2]), function.hash64(seed, data[i+3])); h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash64(seed, data[i+4]), function.hash64(seed, data[i+5]), function.hash64(seed, data[i+6]), function.hash64(seed, data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash64(seed, data[i++]));
        }
        return mix(h);
    }
    
    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> long hashBulk64(final long seed, final SeededHashFunction<T> function, final T[] data) {
        if (data == null) return 0;
        return hashBulk64(seed, function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> long hashBulk64(final long seed, final SeededHashFunction<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash(seed, data[i  ]), function.hash(seed, data[i+1]), function.hash(seed, data[i+2]), function.hash(seed, data[i+3])); h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash(seed, data[i+4]), function.hash(seed, data[i+5]), function.hash(seed, data[i+6]), function.hash(seed, data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash(seed, data[i++]));
        }
        return mix(h);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> int hashBulk(final long seed, final SeededHashFunction64<T> function, final T[] data) {
        if (data == null) return 0;
        return (int)hashBulk(seed, function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction64} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> int hashBulk(final long seed, final SeededHashFunction64<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash64(seed, data[i  ]), function.hash64(seed, data[i+1]), function.hash64(seed, data[i+2]), function.hash64(seed, data[i+3])); h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash64(seed, data[i+4]), function.hash64(seed, data[i+5]), function.hash64(seed, data[i+6]), function.hash64(seed, data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash64(seed, data[i++]));
        }
        return (int)mix(h);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash64} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash64} method here
     * @param data input array
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> int hashBulk(final long seed, final SeededHashFunction<T> function, final T[] data) {
        if (data == null) return 0;
        return (int)hashBulk(seed, function, data, 0, data.length);
    }

    /**
     * Meant to handle hashing larger 2D arrays (or higher dimensions), this lets you pass a {@link SeededHashFunction} as
     * the first parameter, and then this uses that function to get a hash for each T item in data. T is usually an
     * array type, and function is usually a method reference to a {@link #hash} method here.
     * @param seed any long seed
     * @param function typically a method reference to a {@link #hash} method here
     * @param data input array
     * @param start starting index in data
     * @param length how many items to use from data
     * @param <T> typically an array type, often of primitive items; may be more than one-dimensional
     * @return the 64-bit hash of data
     */
    public static <T> int hashBulk(final long seed, final SeededHashFunction<T> function, final T[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        long h = len ^ forward(seed);
        int i = start;
        while(len >= 8){
            h *= C;
            len -= 8;
            h += mixStreamBulk(function.hash(seed, data[i  ]), function.hash(seed, data[i+1]), function.hash(seed, data[i+2]), function.hash(seed, data[i+3])); h = (h << 37 | h >>> 27);
            h += mixStreamBulk(function.hash(seed, data[i+4]), function.hash(seed, data[i+5]), function.hash(seed, data[i+6]), function.hash(seed, data[i+7]));
            i += 8;
        }
        while(len >= 1){
            len--;
            h = mixStream(h, function.hash(seed, data[i++]));
        }
        return (int)mix(h);
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, hashing everything from index 0 to just before index
     * {@link ByteBuffer#limit()}. The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash64(byte[])} on all but
     * the smallest sequences of bytes (under 20 bytes).
     * @param seed any long seed
     * @param data an input ByteBuffer
     * @return the 64-bit hash of data
     */
    public static long hashBulk64(final long seed, final ByteBuffer data) {
        return hashBulk64(seed, data, 0, data.limit());
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, using the given {@code start} index (measured in bytes)
     * and {@code length} (also in bytes). The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash64(byte[], int, int)}
     * on all but the smallest sequences of bytes (under 20 bytes).
     * @param seed any long seed
     * @param data an input ByteBuffer
     * @param start the starting index, measured in bytes
     * @param length the number of bytes to hash
     * @return the 64-bit hash of data
     */
    public static long hashBulk64(final long seed, final ByteBuffer data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.limit())
            return 0;
        int len = Math.min(length, data.limit() - start);
        data.position(start);
        long h = len ^ forward(seed);
        while(len >= 64){
            h *= C;
            len -= 64;
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
        }
        while(len >= 8){
            len -= 8;
            h = mixStream(h, data.getLong());
        }
        switch (len) {
            case 1: return  mix(mixStream(h, (data.get())));
            case 2: return  mix(mixStream(h, (data.getShort())));
            case 3: return  mix(mixStream(h, (data.getShort()) ^ ((long)data.get()) << 16));
            case 4: return  mix(mixStream(h, (data.getInt())));
            case 5: return  mix(mixStream(h, (data.getInt()) ^ ((long)data.get()) << 32));
            case 6: return  mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32));
            case 7: return  mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32 ^ ((long)data.get()) << 48));
            default: return mix(h);
        }
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, hashing everything from index 0 to just before index
     * {@link ByteBuffer#limit()}. The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash(byte[])} on all but
     * the smallest sequences of bytes (under 20 bytes).
     * @param seed any long seed
     * @param data an input ByteBuffer
     * @return the 32-bit hash of data
     */
    public static int hashBulk(final long seed, final ByteBuffer data) {
        return hashBulk(seed, data, 0, data.limit());
    }

    /**
     * A hashing function that operates on a {@link ByteBuffer}, using the given {@code start} index (measured in bytes)
     * and {@code length} (also in bytes). The {@link ByteBuffer#limit() limit} must be set on data; this will not read
     * past the limit.
     * <br>
     * This is likely to significantly outperform {@link #hash(byte[], int, int)}
     * on all but the smallest sequences of bytes (under 20 bytes).
     * @param seed any long seed
     * @param data an input ByteBuffer
     * @param start the starting index, measured in bytes
     * @param length the number of bytes to hash
     * @return the 32-bit hash of data
     */
    public static int hashBulk(final long seed, final ByteBuffer data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.limit())
            return 0;
        int len = Math.min(length, data.limit() - start);
        data.position(start);
        long h = len ^ forward(seed);
        while(len >= 64){
            h *= C;
            len -= 64;
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
            h = (h << 37 | h >>> 27);
            h += mixStreamBulk(data.getLong(), data.getLong(), data.getLong(), data.getLong());
        }
        while(len >= 8){
            len -= 8;
            h = mixStream(h, data.getLong());
        }
        switch (len) {
            case 1: return  (int)mix(mixStream(h, (data.get())));
            case 2: return  (int)mix(mixStream(h, (data.getShort())));
            case 3: return  (int)mix(mixStream(h, (data.getShort()) ^ ((long)data.get()) << 16));
            case 4: return  (int)mix(mixStream(h, (data.getInt())));
            case 5: return  (int)mix(mixStream(h, (data.getInt()) ^ ((long)data.get()) << 32));
            case 6: return  (int)mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32));
            case 7: return  (int)mix(mixStream(h, (data.getInt()) ^ ((long)data.getShort()) << 32 ^ ((long)data.get()) << 48));
            default: return (int)mix(h);
        }
    }

    // predefined HashFunction instances, to avoid lots of casting

    public static final SeededHashFunction64<boolean[]> booleanArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<byte[]> byteArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<short[]> shortArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<int[]> intArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<long[]> longArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<float[]> floatArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<double[]> doubleArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<char[]> charArrayHash64 = Hasher::hash64;
    public static final SeededHashFunction64<Object[]> objectArrayHash64 = Hasher::hash64;

    public static final SeededHashFunction<boolean[]> booleanArrayHash = Hasher::hash;
    public static final SeededHashFunction<byte[]> byteArrayHash = Hasher::hash;
    public static final SeededHashFunction<short[]> shortArrayHash = Hasher::hash;
    public static final SeededHashFunction<int[]> intArrayHash = Hasher::hash;
    public static final SeededHashFunction<long[]> longArrayHash = Hasher::hash;
    public static final SeededHashFunction<float[]> floatArrayHash = Hasher::hash;
    public static final SeededHashFunction<double[]> doubleArrayHash = Hasher::hash;
    public static final SeededHashFunction<char[]> charArrayHash = Hasher::hash;
    public static final SeededHashFunction<Object[]> objectArrayHash = Hasher::hash;

    public static final SeededHashFunction64<long[]> longArrayHashBulk64 = Hasher::hashBulk64;
    public static final SeededHashFunction64<ByteBuffer> byteBufferHashBulk64 = Hasher::hashBulk64;

    public static final SeededHashFunction<long[]> longArrayHashBulk = Hasher::hashBulk;
    public static final SeededHashFunction<ByteBuffer> byteBufferHashBulk = Hasher::hashBulk;

    // normal Java Object stuff

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hasher hasher = (Hasher) o;

        return seed == hasher.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }

    @Override
    public String toString() {
        return "Hasher{" +
                "seed=" + seed +
                '}';
    }
}
