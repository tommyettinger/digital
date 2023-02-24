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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Provides ways to encode digits in different base systems, or radixes, and decode numbers written in those bases. This
 * includes base systems such as binary ({@link #BASE2}, using just 0 and 1), octal ({@link #BASE8}, 0 through 7),
 * decimal ({@link #BASE10}, 0 through 9), hexadecimal ({@link #BASE16}, 0-9 then A-F), and the even larger
 * hexatrigesimal ({@link #BASE36}, 0 through 9 then A-Z). Of special note are the two different approaches to encoding
 * base-64 data: {@link #BASE64} is the standard format, and {@link #URI_SAFE} is the different format used when
 * encoding data for a URI (typically meant for the Internet). The newest is {@link #BASE86}, which is also the largest
 * base, and uses 0-9, A-Z, a-z, and then many punctuation characters. Even more bases can be created with
 * {@link #scrambledBase(Random)}, which when called creates a base-72 Base with randomized choices for digits;
 * this could be useful for obfuscating plain-text saved data so the average player can't read it.
 * <br>
 * Each of these base systems provides a way to write bytes, shorts, ints, and longs as variable-character-count signed
 * numbers or as fixed-character-count unsigned numbers, using {@link #signed(long)} and {@link #unsigned(long)}
 * respectively. There is only one reading method for each size of number, but it is capable of reading both the signed
 * and unsigned results, and never throws an Exception (it just returns 0 if no number could be read). Each base system
 * can also read and write floats and doubles, but only using their bit representation, treated as an int or long.
 */
@SuppressWarnings("ShiftOutOfRange")
public class Base {
    /**
     * Binary, using the digits 0 and 1.
     */
    public static final Base BASE2 = new Base("01", true, '$', '+', '-');
    /**
     * Octal, using the digits 0-7.
     */
    public static final Base BASE8 = new Base("01234567", true, '$', '+', '-');
    /**
     * Decimal, using the digits 0-9.
     */
    public static final Base BASE10 = new Base("0123456789", true, '$', '+', '-');
    /**
     * Hexadecimal, using the digits 0-9 and then A-F (case-insensitive).
     */
    public static final Base BASE16 = new Base("0123456789ABCDEF", true, 'p', '+', '-');
    /**
     * Hexatrigesimal, using the digits 0-9 and then A-Z (case-insensitive).
     */
    public static final Base BASE36 = new Base("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", true, '$', '+', '-');
    /**
     * One of three base-64 schemes available here, this is the more-standard one, using the digits A-Z, then a-z, then
     * 0-9, then + and / (case-sensitive). This uses * in place of + to indicate a positive sign, and - for negative.
     * Because this can use the / character, it sometimes needs quoting when used with libGDX's "minimal JSON" format.
     */
    public static final Base BASE64 = new Base("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", false, '=', '*', '-');
    /**
     * One of three base-64 schemes available here, this is meant for URI-encoding, using the digits A-Z, then a-z, then
     * 0-9, then + and - (case-sensitive). This uses * in place of + to indicate a positive sign, and ! in place of - .
     */
    public static final Base URI_SAFE = new Base("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-", false, '$', '*', '!');
    /**
     * One of three base-64 schemes available here, this is a non-standard one that is more in-line with common
     * expectations for how numbers should look. It uses the digits 0-9, then A-Z, then a-z, then ! and ? (all
     * case-sensitive). Unlike the other base-64 schemes, this uses + for its positive sign and - for its negative sign.
     */
    public static final Base SIMPLE64 = new Base("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!?", false, '$', '+', '-');
    /**
     * The largest base here, this uses digits 0-9 first, then A-Z, then a-z, them many punctuation characters:
     * <code>'/!@#$%^&amp;*()[]{}&lt;&gt;:?;|_=</code> . This uses + to indicate a positive sign, and - for negative.
     * This can encode a 32-bit number with 5 chars (unsigned); none of the other bases can. As a drawback, if a BASE86
     * encoded number is stored in libGDX's "minimal JSON" format, it will often need quoting, which of the other bases,
     * only {@link #BASE64} requires sometimes.
     */
    public static final Base BASE86 = new Base("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'/!@#$%^&*()[]{}<>:?;|_=", false, '\\', '+', '-');

    /**
     * All Base instances this knows about from its own constants.
     * We use Arrays.asList() here to ensure the returned List is immutable.
     */
    private static final List<Base> BASES = Arrays.asList(BASE2, BASE8, BASE10, BASE16, BASE36, BASE64, URI_SAFE, SIMPLE64, BASE86);

    /**
     * Returns an immutable List of the Base instances this knows about from the start. Mostly useful for testing.
     * This is something like an enum's values() method, but unlike an enum, this returns the same, immutable List every
     * time it is called.
     *
     * @return an immutable List of all Base instances this knows about from the start
     */
    public static List<Base> values() {
        return BASES;
    }

    /**
     * The digits this will encode to, in order from smallest to largest. These must all be in the ASCII range.
     * <br>
     * This should not be changed after the Base has been used; changing this makes a Base incompatible with its
     * previously-returned numbers as Strings.
     */
    public final char[] toEncoded;
    /**
     * An array of the digit values corresponding to different ASCII codepoints, with -1 used for codepoints that do
     * not correspond to any digit in this base.
     * <br>
     * This should not be changed after the Base has been used; changing this makes a Base incompatible with its
     * previously-returned numbers as Strings. You can change it in conjunction with {@link #toEncoded} as part of
     * creating a different Base, but if you are doing that to obfuscate output, you can use
     * {@link #scrambledBase(Random)} instead.
     */
    public final int[] fromEncoded;

    /**
     * When an encoding needs to indicate that a char is not considered part of a number, it uses this padding char;
     * this is mostly relevant for other code using Base-64 and URI-safe encodings, and is not used here. It
     * defaults to the space char, {@code ' '}, if not specified.
     */
    public final char paddingChar;
    /**
     * Can be used to indicate positive numbers; like {@code +} in most numeral systems, this is usually ignored.
     */
    public final char positiveSign;
    /**
     * Used to indicate negative numbers with {@link #signed(int)} and when reading them back with
     * {@link #readInt(CharSequence)}; like {@code -} in most numeral systems.
     */
    public final char negativeSign;
    /**
     * Will be true if this base system treats upper- and lower-case letters present in the encoding as the same.
     */
    public final boolean caseInsensitive;
    /**
     * What base or radix this uses; if you use {@link #unsigned(int)}, then base must be an even number.
     */
    public final int base;
    /**
     * Internal; stored lengths of the most common number sizes in this base.
     */
    private final int length1Byte, length2Byte, length4Byte, length8Byte;
    /**
     * Internal; used for temporary buffer space.
     */
    private transient final char[] progress;

    /**
     * Constructs a Base with the given digits, ordered from smallest to largest, with any letters in the digits treated
     * as case-insensitive, and the normal sign characters '+' and '-'. All digits must be unique when compared as
     * case-insensitive; this means you can't have 'a' and 'A' both in the digits String, or any other repeats. You also
     * can't use '$', '+', or '-' in digits, and all chars in it should usually be ASCII. In many cases, Unicode
     * numbering systems outside of ASCII, but within a block of 128 or fewer chars may work, but this isn't assured.
     *
     * @param digits a String with two or more ASCII characters, all unique; none can be '$', '+', or '-'
     */
    public Base(String digits) {
        this(digits, true, '$', '+', '-');
    }

    /**
     * Constructs a base with the given digits, ordered from smallest to largest, specified treatment for case, and
     * specified padding char (currently unused other than to provide a separator), positive sign, and negative sign.
     * All digits must be unique, and if caseInsensitive is true, must also be unique when compared as
     * case-insensitive; this means that if caseInsensitive is true, you can't have 'a' and 'A' both in the digits
     * String, and you can never have any repeats. You also can't use padding, positiveSign, or negativeSign in digits,
     * and all chars in it should usually be ASCII. In many cases, Unicode numbering systems outside of ASCII, but
     * within a block of 128 or fewer chars may work, but this isn't assured.
     *
     * @param digits          a String with two or more ASCII characters, all unique; none can be the same as the later sign parameters
     * @param caseInsensitive if true, digits will be converted to upper-case before any operations on them.
     * @param padding         only used to guarantee a separator is possible between numbers
     * @param positiveSign    typically '+'
     * @param negativeSign    typically '-'
     */
    public Base(String digits, boolean caseInsensitive, char padding, char positiveSign, char negativeSign) {
        paddingChar = padding;
        this.caseInsensitive = caseInsensitive;
        this.positiveSign = positiveSign;
        this.negativeSign = negativeSign;
        toEncoded = digits.toCharArray();
        base = toEncoded.length;
        fromEncoded = new int[128];

        Arrays.fill(fromEncoded, -1);

        for (int i = 0; i < base; i++) {
            char to = toEncoded[i];
            fromEncoded[to & 127] = i;
            if (caseInsensitive)
                fromEncoded[Character.toLowerCase(to) & 127] = i;
        }
        double logBase = 1.0 / Math.log(base);
        length1Byte = (int) Math.ceil(Math.log(0x1p8) * logBase);
        length2Byte = (int) Math.ceil(Math.log(0x1p16) * logBase);
        length4Byte = (int) Math.ceil(Math.log(0x1p32) * logBase);
        length8Byte = (int) Math.ceil(Math.log(0x1p64) * logBase);
        progress = new char[length8Byte + 1];
    }

    /**
     * An unlikely-to-be-used copy constructor. A Base doesn't have any changing state between method calls, so the only
     * reason you would need to copy an existing Base is to edit it. Even then, most changes would need to be made to
     * the contents of {@link #fromEncoded} and {@link #toEncoded}, since those can be edited, but other fields are
     * generally final here.
     *
     * @param other another Base; must be non-null
     */
    public Base(Base other) {
        paddingChar = other.paddingChar;
        caseInsensitive = other.caseInsensitive;
        positiveSign = other.positiveSign;
        negativeSign = other.negativeSign;
        base = other.base;
        toEncoded = Arrays.copyOf(other.toEncoded, base);
        fromEncoded = Arrays.copyOf(other.fromEncoded, 128);
        length1Byte = other.length1Byte;
        length2Byte = other.length2Byte;
        length4Byte = other.length4Byte;
        length8Byte = other.length8Byte;
        progress = new char[length8Byte + 1];
    }

    /**
     * Returns a seemingly-gibberish Base that uses a radix of 72 and a randomly-ordered set of characters to represent
     * the different digit values. This is randomized by a Random generator, so if the parameter is seeded identically
     * (and is the same implementation), then an equivalent Base will be produced. This randomly chooses 72 digits from
     * a large set, <code>ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789?!@#$%^&amp;*-|=+</code>, and
     * sets the positive and negative signs to two different chars left over. The padding char is always space, ' '.
     *
     * @param random a Random used to shuffle the possible digits
     * @return a new Base with 72 random digits, as well as a random positive and negative sign
     */
    public static Base scrambledBase(Random random) {
        char[] options = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789?!@#$%^&*-|=+;".toCharArray();
        // Apply Fisher-Yates shuffle to the options.
        for (int i = options.length - 1; i > 0; i--) {
            int ii = random.nextInt(i + 1);
            char temp = options[i];
            options[i] = options[ii];
            options[ii] = temp;
        }

        char pad = options[options.length - 3], plus = options[options.length - 2], minus = options[options.length - 1];

        // The actual chars here don't matter, because they are replaced with the shuffled options.
        Base base = new Base("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789?!@#$%^&*-", false, pad, plus, minus);
        System.arraycopy(options, 0, base.toEncoded, 0, 72);
        // Makes any digits that don't represent a number use the placeholder -1 value.
        Arrays.fill(base.fromEncoded, -1);

        for (int i = 0; i < base.base; i++) {
            // Makes from and to arrays match.
            base.fromEncoded[base.toEncoded[i] & 127] = i;
        }

        return base;
    }

    /**
     * Copies this Base and shuffles the digit values in the copy. Uses the given Random to perform the shuffle. This
     * can be useful to lightly obfuscate save files, or better, parts of save files, so that certain fields appear to
     * be one number but are actually a different one. If users don't know which entries use which scrambled Base, then
     * that could be a nice way to at least slow down unskilled tampering.
     *
     * @param random a Random used to shuffle the possible digits
     * @return a new Base that uses the same digits, but almost always with different values for those digits
     */
    public Base scramble(Random random) {
        Base base = new Base(this);
        Arrays.fill(base.fromEncoded, -1);

        // Apply Fisher-Yates shuffle to the options.
        for (int i = base.toEncoded.length - 1; i > 0; i--) {
            int ii = random.nextInt(i + 1);
            char temp = base.toEncoded[i];
            base.toEncoded[i] = base.toEncoded[ii];
            base.toEncoded[ii] = temp;
        }

        for (int i = 0; i < base.base; i++) {
            // Makes from and to arrays match.
            base.fromEncoded[base.toEncoded[i] & 127] = i;
        }
        return base;
    }

    /**
     * Stores this Base as a compact String; the String this produces is usually given to
     * {@link #deserializeFromString(String)} to restore the Base. Note that if you are using
     * {@link #scrambledBase(Random)}, you are also able to serialize the Random or its state, and that
     * can be used to produce a scrambled base again; this could be useful to conceal a scrambled base slightly.
     *
     * @return a String that can be given to {@link #deserializeFromString(String)} to obtain this Base again
     */
    public String serializeToString() {
        return String.valueOf(toEncoded) + (caseInsensitive ? '1' : '0') + paddingChar + positiveSign + negativeSign;
    }

    /**
     * Given a String of a serialized Base (almost always produced by {@link #serializeToString()}), this re-creates
     * that Base and returns it.
     *
     * @param data a String that was almost always produced by {@link #serializeToString()}
     * @return the Base that {@code data} stores
     */
    public static Base deserializeFromString(String data) {
        int len;
        if ((len = data.length()) >= 5) {
            return new Base(data.substring(0, len - 4), data.charAt(len - 4) != '0', data.charAt(len - 3), data.charAt(len - 2), data.charAt(len - 1));
        }
        throw new IllegalArgumentException("The given data does not store a serialized Base.");
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any long
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String unsigned(long number) {
        final int len = length8Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            long quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[(int) (number - quotient * base)];
            number = quotient;
        }
        return String.valueOf(progress, 0, length8Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any long
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendUnsigned(StringBuilder builder, long number) {
        final int len = length8Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            long quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[(int) (number - quotient * base)];
            number = quotient;
        }
        return builder.append(progress, 0, length8Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any long
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String signed(long number) {
        int run = length8Byte;
        final long sign = number >> -1;
        // number is made negative because 0x8000000000000000L and -(0x8000000000000000L) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[(int) -(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any long
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendSigned(StringBuilder builder, long number) {
        int run = length8Byte;
        final long sign = number >> -1;
        // number is made negative because 0x8000000000000000L and -(0x8000000000000000L) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[(int) -(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the long they represent, or 0 if nothing could be read. The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(long)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @return the long that cs represents
     */
    public long readLong(final CharSequence cs) {
        return readLong(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the long they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(long)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the long that cs represents
     */
    public long readLong(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length8Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length8Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length8Byte;
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return data * len;
            data *= base;
            data += h;
        }
        return data * len;
    }

    /**
     * Reads in a char array containing only the digits present in this Base, with an optional sign at the
     * start, and returns the long they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(long)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a char array containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the long that cs represents
     */
    public long readLong(final char[] cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length8Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length8Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length8Byte;
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs[i] & 127]) < 0)
                return data * len;
            data *= base;
            data += h;
        }
        return data * len;
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any int
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String unsigned(int number) {
        final int len = length4Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[number - quotient * base];
            number = quotient;
        }
        return String.valueOf(progress, 0, length4Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any int
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendUnsigned(StringBuilder builder, int number) {
        final int len = length4Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (number >>> 1) / halfBase;
            progress[len - i] = toEncoded[number - quotient * base];
            number = quotient;
        }
        return builder.append(progress, 0, length4Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any int
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String signed(int number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any int
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendSigned(StringBuilder builder, int number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the int they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(int)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFF" would return the int -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier
     * JDKs. This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit,
     * or stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @return the int that cs represents
     */
    public int readInt(final CharSequence cs) {
        return readInt(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the int they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(int)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFF" would return the int -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the int that cs represents
     */
    public int readInt(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length4Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length4Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length4Byte;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return data * len;
            data *= base;
            data += h;
        }
        return data * len;
    }

    /**
     * Reads in a char array containing only the digits present in this Base, with an optional sign at the
     * start, and returns the int they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(int)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFFFFFF" would return the int -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a char array containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the int that cs represents
     */
    public int readInt(final char[] cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length4Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length4Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length4Byte;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs[i] & 127]) < 0)
                return data * len;
            data *= base;
            data += h;
        }
        return data * len;
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any short
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String unsigned(short number) {
        final int len = length2Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFFFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (short) quotient;
        }
        return String.valueOf(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any short
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendUnsigned(StringBuilder builder, short number) {
        final int len = length2Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFFFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (short) quotient;
        }
        return builder.append(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any short
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String signed(short number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (short) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any short
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendSigned(StringBuilder builder, short number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (short) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the short they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(short)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFF" would return the short -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for shorts.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @return the short that cs represents
     */
    public short readShort(final CharSequence cs) {
        return readShort(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the short they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(short)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFF" would return the short -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for shorts.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the short that cs represents
     */
    public short readShort(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length2Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length2Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length2Byte;
        }
        short data = (short) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return (short) (data * len);
            data *= base;
            data += h;
        }
        return (short) (data * len);
    }

    /**
     * Reads in a char array containing only the digits present in this Base, with an optional sign at the
     * start, and returns the short they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(short)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FFFF" would return the short -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for shorts.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a char array containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the short that cs represents
     */
    public short readShort(final char[] cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length2Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length2Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length2Byte;
        }
        short data = (short) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs[i] & 127]) < 0)
                return (short) (data * len);
            data *= base;
            data += h;
        }
        return (short) (data * len);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any byte
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String unsigned(byte number) {
        final int len = length1Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFF) - quotient * base];
            number = (byte) quotient;
        }
        return String.valueOf(progress, 0, length1Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any byte
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendUnsigned(StringBuilder builder, byte number) {
        final int len = length1Byte - 1;
        final int halfBase = base >>> 1;
        for (int i = 0; i <= len; i++) {
            int quotient = (((number & 0xFF) >>> 1) / halfBase);
            progress[len - i] = toEncoded[(number & 0xFF) - quotient * base];
            number = (byte) quotient;
        }
        return builder.append(progress, 0, length1Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any byte
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String signed(byte number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (byte) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any byte
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendSigned(StringBuilder builder, byte number) {
        int run = length8Byte;
        final int sign = number >> -1;
        // number is made negative because 0x80000000 and -(0x80000000) are both negative.
        // then modulus later will also return a negative number or 0, and we can negate that to get a good index.
        number = (byte) -(number + sign ^ sign);
        for (; ; run--) {
            progress[run] = toEncoded[-(number % base)];
            if ((number /= base) == 0)
                break;
        }
        if (sign != 0) {
            progress[--run] = negativeSign;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the byte they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(byte)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FF" would return the byte -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for bytes.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @return the byte that cs represents
     */
    public byte readByte(final CharSequence cs) {
        return readByte(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the byte they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(byte)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FF" would return the byte -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for bytes.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the byte that cs represents
     */
    public byte readByte(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length1Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length1Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length1Byte;
        }
        byte data = (byte) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return (byte) (data * len);
            data *= base;
            data += h;
        }
        return (byte) (data * len);
    }

    /**
     * Reads in a char array containing only the digits present in this Base, with an optional sign at the
     * start, and returns the byte they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * This can also represent negative numbers as they are printed by such methods as String.format
     * given a %x in the formatting string, or this class' {@link #unsigned(byte)} method; that is, if the first
     * char of a max-length digit sequence is in the upper half of possible digits (such as 8 for hex digits or 4
     * for octal), then the whole number represents a negative number, using two's complement and so on. This means
     * when using base-16, "FF" would return the byte -1 when passed to this, though you could also
     * simply use "-1". If you use both '-' at the start and have the most significant digit as 8 or higher, such as
     * with "-FF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for bytes.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a char array containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the byte that cs represents
     */
    public byte readByte(final char[] cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length1Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length1Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length1Byte;
        }
        byte data = (byte) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs[i] & 127]) < 0)
                return (byte) (data * len);
            data *= base;
            data += h;
        }
        return (byte) (data * len);
    }

    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any double
     * @return a new String containing the bits of {@code number} in the radix this specifies.
     */
    public String unsigned(double number) {
        return '.' + unsigned(BitConversion.doubleToRawLongBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any double
     * @return {@code builder}, with the bits of {@code number} appended in the radix this specifies
     */
    public StringBuilder appendUnsigned(StringBuilder builder, double number) {
        return appendUnsigned(builder.append('.'), BitConversion.doubleToRawLongBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any double
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String signed(double number) {
        return signed(BitConversion.doubleToReversedLongBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any double
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendSigned(StringBuilder builder, double number) {
        return appendSigned(builder, BitConversion.doubleToReversedLongBits(number));
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the double those bits represent, or 0.0 if nothing could be read. The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(double)}, {@link #unsigned(double)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @return the double that cs represents
     */
    public double readDouble(final CharSequence cs) {
        return readDouble(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the double those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(double)}, {@link #unsigned(double)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the double that cs represents
     */
    public double readDouble(final CharSequence cs, final int start, int end) {
        if(cs.length() == 0) return 0.0;
        if(cs.charAt(start) == '.') return BitConversion.longBitsToDouble(readLong(cs, start+1, end));
        return BitConversion.reversedLongBitsToDouble(readLong(cs, start, end));
    }

    /**
     * Reads in a char array containing only the digits present in this Base, with an optional sign at the
     * start, and returns the double those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(double)}, {@link #unsigned(double)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a char array containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the double that cs represents
     */
    public double readDouble(final char[] cs, final int start, int end) {
        if(cs.length == 0) return 0.0;
        if(cs[start] == '.') return BitConversion.longBitsToDouble(readLong(cs, start+1, end));
        return BitConversion.reversedLongBitsToDouble(readLong(cs, start, end));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any float
     * @return a new String containing the bits of {@code number} in the radix this specifies.
     */
    public String unsigned(float number) {
        return '.'+unsigned(BitConversion.floatToRawIntBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any float
     * @return {@code builder}, with the bits of {@code number} appended in the radix this specifies
     */
    public StringBuilder appendUnsigned(StringBuilder builder, float number) {
        return appendUnsigned(builder.append('.'), BitConversion.floatToRawIntBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any float
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String signed(float number) {
        return signed(BitConversion.floatToReversedIntBits(number));
    }

    /**
     * Converts the bits of the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any float
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendSigned(StringBuilder builder, float number) {
        return appendSigned(builder, BitConversion.floatToReversedIntBits(number));
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the float those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(float)}, {@link #unsigned(float)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @return the float that cs represents
     */
    public float readFloat(final CharSequence cs) {
        return readFloat(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the float those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(float)}, {@link #unsigned(float)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the float that cs represents
     */
    public float readFloat(final CharSequence cs, final int start, int end) {
        if(cs.length() == 0) return 0f;
        if(cs.charAt(start) == '.') return BitConversion.intBitsToFloat(readInt(cs, start+1, end));
        return BitConversion.reversedIntBitsToFloat(readInt(cs, start, end));
    }

    /**
     * Reads in a char array containing only the digits present in this Base, with an optional sign at the
     * start, and returns the float those bits represent, or 0.0 if nothing could be read.  The leading sign can be
     * {@link #positiveSign} or {@link #negativeSign} if present, and is almost always '+' or '-'.
     * This is meant entirely for non-human-editable content, and the digit strings this can read
     * will almost always be produced by {@link #signed(float)}, {@link #unsigned(float)}, or their append versions.
     * <br>
     * This doesn't throw on invalid input, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a char array containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the float that cs represents
     */
    public float readFloat(final char[] cs, final int start, int end) {
        if(cs.length == 0) return 0f;
        if(cs[start] == '.') return BitConversion.intBitsToFloat(readInt(cs, start+1, end));
        return BitConversion.reversedIntBitsToFloat(readInt(cs, start, end));
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, returning a new String.
     * This always uses the same number of chars in any String it returns, as long as the Base is the same.
     *
     * @param number any char
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String unsigned(char number) {
        final int len = length2Byte - 1;
        for (int i = 0; i <= len; i++) {
            int quotient = number / base;
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (char) quotient;
        }
        return String.valueOf(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as unsigned, appending the result to
     * {@code builder}.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any char
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendUnsigned(StringBuilder builder, char number) {
        final int len = length2Byte - 1;
        for (int i = 0; i <= len; i++) {
            int quotient = number / base;
            progress[len - i] = toEncoded[(number & 0xFFFF) - quotient * base];
            number = (char) quotient;
        }
        return builder.append(progress, 0, length2Byte);
    }

    /**
     * Converts the given {@code number} to this Base as signed, returning a new String.
     * This can vary in how many chars it uses, since it does not show leading zeroes and may use a {@code -} sign.
     *
     * @param number any char
     * @return a new String containing {@code number} in the radix this specifies.
     */
    public String signed(char number) {
        int run = length8Byte;
        for (; ; run--) {
            progress[run] = toEncoded[number % base];
            if ((number /= base) == 0)
                break;
        }
        return String.valueOf(progress, run, length8Byte + 1 - run);
    }

    /**
     * Converts the given {@code number} to this Base as signed, appending the result to
     * {@code builder}. This can vary in how many chars it uses, since it does not show leading zeroes and may use a
     * {@code -} sign.
     *
     * @param builder a non-null StringBuilder that will be modified (appended to)
     * @param number  any char
     * @return {@code builder}, with the encoded {@code number} appended
     */
    public StringBuilder appendSigned(StringBuilder builder, char number) {
        int run = length8Byte;
        for (; ; run--) {
            progress[run] = toEncoded[number % base];
            if ((number /= base) == 0)
                break;
        }
        return builder.append(progress, run, length8Byte + 1 - run);
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the char they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * Note that chars are unsigned 16-bit numbers by default, so even having a sign runs counter to the normal
     * behavior; {@link #unsigned(char)} behaves as Java expects it, while {@link #signed(char)} is the anomaly.
     * This means chars are always in the 0 to 65535 range, so if you give this a String representing a negative number,
     * it treats it like a negative short and effectively casts it to char.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for chars.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @return the char that cs represents
     */
    public char readChar(final CharSequence cs) {
        return readChar(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only the digits present in this Base, with an optional sign at the
     * start, and returns the char they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * Note that chars are unsigned 16-bit numbers by default, so even having a sign runs counter to the normal
     * behavior; {@link #unsigned(char)} behaves as Java expects it, while {@link #signed(char)} is the anomaly.
     * This means chars are always in the 0 to 65535 range, so if you give this a String representing a negative number,
     * it treats it like a negative short and effectively casts it to char.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for chars.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the char that cs represents
     */
    public char readChar(final CharSequence cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length2Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length2Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length2Byte;
        }
        char data = (char) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs.charAt(i) & 127]) < 0)
                return (char) (data * len);
            data *= base;
            data += h;
        }
        return (char) (data * len);
    }

    /**
     * Reads in a char array containing only the digits present in this Base, with an optional sign at the
     * start, and returns the char they represent, or 0 if nothing could be read.  The leading sign can be the
     * {@link #positiveSign} or {@link #negativeSign} if present; these are almost always '+' and '-'.
     * Note that chars are unsigned 16-bit numbers by default, so even having a sign runs counter to the normal
     * behavior; {@link #unsigned(char)} behaves as Java expects it, while {@link #signed(char)} is the anomaly.
     * This means chars are always in the 0 to 65535 range, so if you give this a String representing a negative number,
     * it treats it like a negative short and effectively casts it to char.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which doesn't exist for chars.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a valid digit, or
     * stopping the parse process early if an invalid digit is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a char array containing only the digits in this Base and/or an optional initial sign (usually + or -)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this after reading enough chars to represent the largest possible value)
     * @return the char that cs represents
     */
    public char readChar(final char[] cs, final int start, int end) {
        int len, h, lim;
        if (start < 0 || end <= 0 || end - start <= 0 || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if (c == negativeSign) {
            len = -1;
            h = 0;
            lim = length2Byte + 1;
        } else if (c == positiveSign) {
            len = 1;
            h = 0;
            lim = length2Byte + 1;
        } else if ((h = fromEncoded[c & 127]) < 0)
            return 0;
        else {
            len = 1;
            lim = length2Byte;
        }
        char data = (char) h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((h = fromEncoded[cs[i] & 127]) < 0)
                return (char) (data * len);
            data *= base;
            data += h;
        }
        return (char) (data * len);
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * long array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source     a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter  the String that separates numbers in the source
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a long array of the numbers found in source
     */
    public long[] longSplit(String source, String delimiter, int startIndex, int endIndex) {
        if (delimiter.length() == 0 || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new long[0];
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0)
            return new long[]{readLong(source, startIndex, endIndex)};
        long[] splat = new long[amount + 1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = readLong(source, idx + dl, idx = source.indexOf(delimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(delimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount] = readLong(source, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount] = readLong(source, idx + dl, idx2);
        }
        return splat;
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * long array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source    a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter the String that separates numbers in the source
     * @return a long array of the numbers found in source
     */
    public long[] longSplit(String source, String delimiter) {
        return longSplit(source, delimiter, 0, source.length());
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as an
     * int array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source     a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter  the String that separates numbers in the source
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return an int array of the numbers found in source
     */
    public int[] intSplit(String source, String delimiter, int startIndex, int endIndex) {
        if (delimiter.length() == 0 || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new int[0];
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0)
            return new int[]{readInt(source, startIndex, endIndex)};
        int[] splat = new int[amount + 1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = readInt(source, idx + dl, idx = source.indexOf(delimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(delimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount] = readInt(source, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount] = readInt(source, idx + dl, idx2);
        }
        return splat;
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as an
     * int array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source    a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter the String that separates numbers in the source
     * @return an int array of the numbers found in source
     */
    public int[] intSplit(String source, String delimiter) {
        return intSplit(source, delimiter, 0, source.length());
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * short array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source     a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter  the String that separates numbers in the source
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a short array of the numbers found in source
     */
    public short[] shortSplit(String source, String delimiter, int startIndex, int endIndex) {
        if (delimiter.length() == 0 || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new short[0];
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0)
            return new short[]{readShort(source, startIndex, endIndex)};
        short[] splat = new short[amount + 1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = readShort(source, idx + dl, idx = source.indexOf(delimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(delimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount] = readShort(source, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount] = readShort(source, idx + dl, idx2);
        }
        return splat;
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * short array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source    a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter the String that separates numbers in the source
     * @return a short array of the numbers found in source
     */
    public short[] shortSplit(String source, String delimiter) {
        return shortSplit(source, delimiter, 0, source.length());
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * byte array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source     a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter  the String that separates numbers in the source
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a byte array of the numbers found in source
     */
    public byte[] byteSplit(String source, String delimiter, int startIndex, int endIndex) {
        if (delimiter.length() == 0 || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new byte[0];
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0)
            return new byte[]{readByte(source, startIndex, endIndex)};
        byte[] splat = new byte[amount + 1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = readByte(source, idx + dl, idx = source.indexOf(delimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(delimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount] = readByte(source, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount] = readByte(source, idx + dl, idx2);
        }
        return splat;
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * byte array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source    a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter the String that separates numbers in the source
     * @return a byte array of the numbers found in source
     */
    public byte[] byteSplit(String source, String delimiter) {
        return byteSplit(source, delimiter, 0, source.length());
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * char array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source     a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter  the String that separates numbers in the source
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a char array of the numbers found in source
     */
    public char[] charSplit(String source, String delimiter, int startIndex, int endIndex) {
        if (delimiter.length() == 0 || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new char[0];
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0)
            return new char[]{readChar(source, startIndex, endIndex)};
        char[] splat = new char[amount + 1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = readChar(source, idx + dl, idx = source.indexOf(delimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(delimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount] = readChar(source, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount] = readChar(source, idx + dl, idx2);
        }
        return splat;
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * char array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source    a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter the String that separates numbers in the source
     * @return a char array of the numbers found in source
     */
    public char[] charSplit(String source, String delimiter) {
        return charSplit(source, delimiter, 0, source.length());
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * double array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source     a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter  the String that separates numbers in the source
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a double array of the numbers found in source
     */
    public double[] doubleSplit(String source, String delimiter, int startIndex, int endIndex) {
        if (delimiter.length() == 0 || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new double[0];
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0)
            return new double[]{readDouble(source, startIndex, endIndex)};
        double[] splat = new double[amount + 1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = readDouble(source, idx + dl, idx = source.indexOf(delimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(delimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount] = readDouble(source, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount] = readDouble(source, idx + dl, idx2);
        }
        return splat;
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * double array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source    a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter the String that separates numbers in the source
     * @return a double array of the numbers found in source
     */
    public double[] doubleSplit(String source, String delimiter) {
        return doubleSplit(source, delimiter, 0, source.length());
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * float array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source     a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter  the String that separates numbers in the source
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a float array of the numbers found in source
     */
    public float[] floatSplit(String source, String delimiter, int startIndex, int endIndex) {
        if (delimiter.length() == 0 || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new float[0];
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0)
            return new float[]{readFloat(source, startIndex, endIndex)};
        float[] splat = new float[amount + 1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = readFloat(source, idx + dl, idx = source.indexOf(delimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(delimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount] = readFloat(source, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount] = readFloat(source, idx + dl, idx2);
        }
        return splat;
    }

    /**
     * Given a String containing numbers in this Base, separated by instances of delimiter, returns those numbers as a
     * float array. If source or delimiter is null, or if source or delimiter is empty, this returns an empty array.
     *
     * @param source    a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param delimiter the String that separates numbers in the source
     * @return a float array of the numbers found in source
     */
    public float[] floatSplit(String source, String delimiter) {
        return floatSplit(source, delimiter, 0, source.length());
    }

    /**
     * Given a long array and a delimiter to separate the items of that array, produces a String containing all longs
     * from elements, in this Base, separated by delimiter.
     *
     * @param delimiter the separator to put between numbers
     * @param elements  a long array; if null, this returns an empty String
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public String join(String delimiter, long[] elements) {
        if (elements.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb.toString();
    }

    /**
     * Given a long array, a delimiter to separate the items of that array, and a StringBuilder to append to, appends to
     * the StringBuilder all longs from elements, in this Base, separated by delimiter.
     *
     * @param sb        the StringBuilder to append to; if null, this returns null
     * @param delimiter the separator to put between numbers
     * @param elements  a long array; if null, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public StringBuilder appendJoined(StringBuilder sb, String delimiter, long[] elements) {
        if (elements.length == 0)
            return sb;
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb;
    }

    /**
     * Given an int array and a delimiter to separate the items of that array, produces a String containing all ints
     * from elements, in this Base, separated by delimiter.
     *
     * @param delimiter the separator to put between numbers
     * @param elements  an int array; if null, this returns an empty String
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public String join(String delimiter, int[] elements) {
        if (elements.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb.toString();
    }

    /**
     * Given an int array, a delimiter to separate the items of that array, and a StringBuilder to append to, appends to
     * the StringBuilder all ints from elements, in this Base, separated by delimiter.
     *
     * @param sb        the StringBuilder to append to; if null, this returns null
     * @param delimiter the separator to put between numbers
     * @param elements  an int array; if null, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public StringBuilder appendJoined(StringBuilder sb, String delimiter, int[] elements) {
        if (elements.length == 0)
            return sb;
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb;
    }

    /**
     * Given a short array and a delimiter to separate the items of that array, produces a String containing all shorts
     * from elements, in this Base, separated by delimiter.
     *
     * @param delimiter the separator to put between numbers
     * @param elements  a short array; if null, this returns an empty String
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public String join(String delimiter, short[] elements) {
        if (elements.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb.toString();
    }

    /**
     * Given a short array, a delimiter to separate the items of that array, and a StringBuilder to append to, appends to
     * the StringBuilder all shorts from elements, in this Base, separated by delimiter.
     *
     * @param sb        the StringBuilder to append to; if null, this returns null
     * @param delimiter the separator to put between numbers
     * @param elements  a short array; if null, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public StringBuilder appendJoined(StringBuilder sb, String delimiter, short[] elements) {
        if (elements.length == 0)
            return sb;
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb;
    }

    /**
     * Given a byte array and a delimiter to separate the items of that array, produces a String containing all bytes
     * from elements, in this Base, separated by delimiter.
     *
     * @param delimiter the separator to put between numbers
     * @param elements  a byte array; if null, this returns an empty String
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public String join(String delimiter, byte[] elements) {
        if (elements.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb.toString();
    }

    /**
     * Given a byte array, a delimiter to separate the items of that array, and a StringBuilder to append to, appends to
     * the StringBuilder all bytes from elements, in this Base, separated by delimiter.
     *
     * @param sb        the StringBuilder to append to; if null, this returns null
     * @param delimiter the separator to put between numbers
     * @param elements  a byte array; if null, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public StringBuilder appendJoined(StringBuilder sb, String delimiter, byte[] elements) {
        if (elements.length == 0)
            return sb;
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb;
    }

    /**
     * Given a char array and a delimiter to separate the items of that array, produces a String containing all chars
     * (as numbers) from elements, in this Base, separated by delimiter.
     *
     * @param delimiter the separator to put between numbers
     * @param elements  a char array; if null, this returns an empty String
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public String join(String delimiter, char[] elements) {
        if (elements.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb.toString();
    }

    /**
     * Given a char array, a delimiter to separate the items of that array, and a StringBuilder to append to, appends to
     * the StringBuilder all chars (as numbers) from elements, in this Base, separated by delimiter.
     *
     * @param sb        the StringBuilder to append to; if null, this returns null
     * @param delimiter the separator to put between numbers
     * @param elements  a char array; if null, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public StringBuilder appendJoined(StringBuilder sb, String delimiter, char[] elements) {
        if (elements.length == 0)
            return sb;
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb;
    }

    /**
     * Given a double array and a delimiter to separate the items of that array, produces a String containing all doubles
     * from elements, in this Base, separated by delimiter. This uses {@link #appendSigned(StringBuilder, double)},
     * which means this does not produce human-readable numbers.
     *
     * @param delimiter the separator to put between numbers
     * @param elements  a double array; if null, this returns an empty String
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public String join(String delimiter, double[] elements) {
        if (elements.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb.toString();
    }

    /**
     * Given a double array, a delimiter to separate the items of that array, and a StringBuilder to append to, appends to
     * the StringBuilder all doubles from elements, in this Base, separated by delimiter. This uses
     * {@link #appendSigned(StringBuilder, double)}, which means this does not produce human-readable numbers.
     *
     * @param sb        the StringBuilder to append to; if null, this returns null
     * @param delimiter the separator to put between numbers
     * @param elements  a double array; if null, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public StringBuilder appendJoined(StringBuilder sb, String delimiter, double[] elements) {
        if (elements.length == 0)
            return sb;
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb;
    }

    /**
     * Given a float array and a delimiter to separate the items of that array, produces a String containing all floats
     * from elements, in this Base, separated by delimiter. This uses {@link #appendSigned(StringBuilder, float)},
     * which means this does not produce human-readable numbers.
     *
     * @param delimiter the separator to put between numbers
     * @param elements  a float array; if null, this returns an empty String
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public String join(String delimiter, float[] elements) {
        if (elements.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb.toString();
    }

    /**
     * Given a float array, a delimiter to separate the items of that array, and a StringBuilder to append to, appends to
     * the StringBuilder all floats from elements, in this Base, separated by delimiter. This uses
     * {@link #appendSigned(StringBuilder, float)}, which means this does not produce human-readable numbers.
     *
     * @param sb        the StringBuilder to append to; if null, this returns null
     * @param delimiter the separator to put between numbers
     * @param elements  a float array; if null, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by delimiter
     */
    public StringBuilder appendJoined(StringBuilder sb, String delimiter, float[] elements) {
        if (elements.length == 0)
            return sb;
        appendSigned(sb, elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            appendSigned(sb, elements[i]);
        }
        return sb;
    }

    /**
     * Given a long 2D array, a major delimiter to separate the inner arrays, a minor delimiter to separate the items in
     * each inner array, and a StringBuilder to append to, appends to the StringBuilder all longs from elements, in this
     * Base, separated by minor delimiter and then by major delimiter. For any non-null, non-empty elements, this will
     * append at least one major delimiter before it appends any items.
     *
     * @param sb             the StringBuilder to append to; if null, this returns null
     * @param majorDelimiter the separator to put between arrays
     * @param minorDelimiter the separator to put between numbers
     * @param elements       a long 2D array; if null or empty, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by the delimiters
     */
    public StringBuilder appendJoined2D(StringBuilder sb, String majorDelimiter, String minorDelimiter, long[][] elements) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (elements.length == 0)
            return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(majorDelimiter);
            appendJoined(sb, minorDelimiter, elements[i]);
        }
        return sb;
    }

    /**
     * Given an int 2D array, a major delimiter to separate the inner arrays, a minor delimiter to separate the items in
     * each inner array, and a StringBuilder to append to, appends to the StringBuilder all ints from elements, in this
     * Base, separated by minor delimiter and then by major delimiter. For any non-null, non-empty elements, this will
     * append at least one major delimiter before it appends any items.
     *
     * @param sb             the StringBuilder to append to; if null, this returns null
     * @param majorDelimiter the separator to put between arrays
     * @param minorDelimiter the separator to put between numbers
     * @param elements       an int 2D array; if null or empty, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by the delimiters
     */
    public StringBuilder appendJoined2D(StringBuilder sb, String majorDelimiter, String minorDelimiter, int[][] elements) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (elements.length == 0)
            return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(majorDelimiter);
            appendJoined(sb, minorDelimiter, elements[i]);
        }
        return sb;
    }

    /**
     * Given a short 2D array, a major delimiter to separate the inner arrays, a minor delimiter to separate the items in
     * each inner array, and a StringBuilder to append to, appends to the StringBuilder all shorts from elements, in this
     * Base, separated by minor delimiter and then by major delimiter. For any non-null, non-empty elements, this will
     * append at least one major delimiter before it appends any items.
     *
     * @param sb             the StringBuilder to append to; if null, this returns null
     * @param majorDelimiter the separator to put between arrays
     * @param minorDelimiter the separator to put between numbers
     * @param elements       a short 2D array; if null or empty, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by the delimiters
     */
    public StringBuilder appendJoined2D(StringBuilder sb, String majorDelimiter, String minorDelimiter, short[][] elements) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (elements.length == 0)
            return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(majorDelimiter);
            appendJoined(sb, minorDelimiter, elements[i]);
        }
        return sb;
    }

    /**
     * Given a byte 2D array, a major delimiter to separate the inner arrays, a minor delimiter to separate the items in
     * each inner array, and a StringBuilder to append to, appends to the StringBuilder all bytes from elements, in this
     * Base, separated by minor delimiter and then by major delimiter. For any non-null, non-empty elements, this will
     * append at least one major delimiter before it appends any items.
     *
     * @param sb             the StringBuilder to append to; if null, this returns null
     * @param majorDelimiter the separator to put between arrays
     * @param minorDelimiter the separator to put between numbers
     * @param elements       a byte 2D array; if null or empty, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by the delimiters
     */
    public StringBuilder appendJoined2D(StringBuilder sb, String majorDelimiter, String minorDelimiter, byte[][] elements) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (elements.length == 0)
            return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(majorDelimiter);
            appendJoined(sb, minorDelimiter, elements[i]);
        }
        return sb;
    }

    /**
     * Given a char 2D array, a major delimiter to separate the inner arrays, a minor delimiter to separate the items in
     * each inner array, and a StringBuilder to append to, appends to the StringBuilder all chars from elements, in this
     * Base, separated by minor delimiter and then by major delimiter. For any non-null, non-empty elements, this will
     * append at least one major delimiter before it appends any items.
     *
     * @param sb             the StringBuilder to append to; if null, this returns null
     * @param majorDelimiter the separator to put between arrays
     * @param minorDelimiter the separator to put between numbers
     * @param elements       a char 2D array; if null or empty, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by the delimiters
     */
    public StringBuilder appendJoined2D(StringBuilder sb, String majorDelimiter, String minorDelimiter, char[][] elements) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (elements.length == 0)
            return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(majorDelimiter);
            appendJoined(sb, minorDelimiter, elements[i]);
        }
        return sb;
    }

    /**
     * Given a double 2D array, a major delimiter to separate the inner arrays, a minor delimiter to separate the items in
     * each inner array, and a StringBuilder to append to, appends to the StringBuilder all doubles from elements, in this
     * Base, separated by minor delimiter and then by major delimiter. For any non-null, non-empty elements, this will
     * append at least one major delimiter before it appends any items.
     *
     * @param sb             the StringBuilder to append to; if null, this returns null
     * @param majorDelimiter the separator to put between arrays
     * @param minorDelimiter the separator to put between numbers
     * @param elements       a double 2D array; if null or empty, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by the delimiters
     */
    public StringBuilder appendJoined2D(StringBuilder sb, String majorDelimiter, String minorDelimiter, double[][] elements) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (elements.length == 0)
            return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(majorDelimiter);
            appendJoined(sb, minorDelimiter, elements[i]);
        }
        return sb;
    }

    /**
     * Given a float 2D array, a major delimiter to separate the inner arrays, a minor delimiter to separate the items in
     * each inner array, and a StringBuilder to append to, appends to the StringBuilder all floats from elements, in this
     * Base, separated by minor delimiter and then by major delimiter. For any non-null, non-empty elements, this will
     * append at least one major delimiter before it appends any items.
     *
     * @param sb             the StringBuilder to append to; if null, this returns null
     * @param majorDelimiter the separator to put between arrays
     * @param minorDelimiter the separator to put between numbers
     * @param elements       a float 2D array; if null or empty, this returns sb without changes
     * @return a String containing all numbers in elements, written in this Base, separated by the delimiters
     */
    public StringBuilder appendJoined2D(StringBuilder sb, String majorDelimiter, String minorDelimiter, float[][] elements) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (elements.length == 0)
            return sb;
        for (int i = 0; i < elements.length; i++) {
            sb.append(majorDelimiter);
            appendJoined(sb, minorDelimiter, elements[i]);
        }
        return sb;
    }

    private static final long[][] long2D = new long[0][0];

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * long array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, long[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @param startIndex     the first index, inclusive, in source to split from
     * @param endIndex       the last index, exclusive, in source to split from
     * @return a 2D long array of the numbers found in source
     */
    public long[][] longSplit2D(String source, String majorDelimiter, String minorDelimiter, int startIndex, int endIndex) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return long2D;
        int amount = count(source, majorDelimiter, startIndex, endIndex);
        if (amount <= 0)
            return Arrays.copyOf(long2D, 0);
        long[][] splat = Arrays.copyOf(long2D, amount);
        int dl = majorDelimiter.length(), idx = startIndex, idx2;
        for (int i = 0; i < amount - 1; i++) {
            splat[i] = longSplit(source, minorDelimiter, idx + dl, idx = source.indexOf(majorDelimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(majorDelimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount - 1] = longSplit(source, minorDelimiter, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount - 1] = longSplit(source, minorDelimiter, idx + dl, idx2);
        }
        return splat;

    }

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * long array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, long[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @return a 2D long array of the numbers found in source
     */
    public long[][] longSplit2D(String source, String majorDelimiter, String minorDelimiter) {
        return longSplit2D(source, majorDelimiter, minorDelimiter, 0, source.length());
    }

    private static final int[][] int2D = new int[0][0];

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * int array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, int[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @param startIndex     the first index, inclusive, in source to split from
     * @param endIndex       the last index, exclusive, in source to split from
     * @return a 2D int array of the numbers found in source
     */
    public int[][] intSplit2D(String source, String majorDelimiter, String minorDelimiter, int startIndex, int endIndex) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return int2D;
        int amount = count(source, majorDelimiter, startIndex, endIndex);
        if (amount <= 0)
            return Arrays.copyOf(int2D, 0);
        int[][] splat = Arrays.copyOf(int2D, amount);
        int dl = majorDelimiter.length(), idx = startIndex, idx2;
        for (int i = 0; i < amount - 1; i++) {
            splat[i] = intSplit(source, minorDelimiter, idx + dl, idx = source.indexOf(majorDelimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(majorDelimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount - 1] = intSplit(source, minorDelimiter, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount - 1] = intSplit(source, minorDelimiter, idx + dl, idx2);
        }
        return splat;

    }

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * int array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, int[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @return a 2D int array of the numbers found in source
     */
    public int[][] intSplit2D(String source, String majorDelimiter, String minorDelimiter) {
        return intSplit2D(source, majorDelimiter, minorDelimiter, 0, source.length());
    }

    private static final short[][] short2D = new short[0][0];

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * short array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, short[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @param startIndex     the first index, inclusive, in source to split from
     * @param endIndex       the last index, exclusive, in source to split from
     * @return a 2D short array of the numbers found in source
     */
    public short[][] shortSplit2D(String source, String majorDelimiter, String minorDelimiter, int startIndex, int endIndex) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return short2D;
        int amount = count(source, majorDelimiter, startIndex, endIndex);
        if (amount <= 0)
            return Arrays.copyOf(short2D, 0);
        short[][] splat = Arrays.copyOf(short2D, amount);
        int dl = majorDelimiter.length(), idx = startIndex, idx2;
        for (int i = 0; i < amount - 1; i++) {
            splat[i] = shortSplit(source, minorDelimiter, idx + dl, idx = source.indexOf(majorDelimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(majorDelimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount - 1] = shortSplit(source, minorDelimiter, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount - 1] = shortSplit(source, minorDelimiter, idx + dl, idx2);
        }
        return splat;

    }

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * short array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, short[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @return a 2D short array of the numbers found in source
     */
    public short[][] shortSplit2D(String source, String majorDelimiter, String minorDelimiter) {
        return shortSplit2D(source, majorDelimiter, minorDelimiter, 0, source.length());
    }

    private static final byte[][] byte2D = new byte[0][0];

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * byte array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, byte[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @param startIndex     the first index, inclusive, in source to split from
     * @param endIndex       the last index, exclusive, in source to split from
     * @return a 2D byte array of the numbers found in source
     */
    public byte[][] byteSplit2D(String source, String majorDelimiter, String minorDelimiter, int startIndex, int endIndex) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return byte2D;
        int amount = count(source, majorDelimiter, startIndex, endIndex);
        if (amount <= 0)
            return Arrays.copyOf(byte2D, 0);
        byte[][] splat = Arrays.copyOf(byte2D, amount);
        int dl = majorDelimiter.length(), idx = startIndex, idx2;
        for (int i = 0; i < amount - 1; i++) {
            splat[i] = byteSplit(source, minorDelimiter, idx + dl, idx = source.indexOf(majorDelimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(majorDelimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount - 1] = byteSplit(source, minorDelimiter, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount - 1] = byteSplit(source, minorDelimiter, idx + dl, idx2);
        }
        return splat;

    }

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * byte array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, byte[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @return a 2D byte array of the numbers found in source
     */
    public byte[][] byteSplit2D(String source, String majorDelimiter, String minorDelimiter) {
        return byteSplit2D(source, majorDelimiter, minorDelimiter, 0, source.length());
    }

    private static final char[][] char2D = new char[0][0];

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * char array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, char[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @param startIndex     the first index, inclusive, in source to split from
     * @param endIndex       the last index, exclusive, in source to split from
     * @return a 2D char array of the numbers found in source
     */
    public char[][] charSplit2D(String source, String majorDelimiter, String minorDelimiter, int startIndex, int endIndex) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return char2D;
        int amount = count(source, majorDelimiter, startIndex, endIndex);
        if (amount <= 0)
            return Arrays.copyOf(char2D, 0);
        char[][] splat = Arrays.copyOf(char2D, amount);
        int dl = majorDelimiter.length(), idx = startIndex, idx2;
        for (int i = 0; i < amount - 1; i++) {
            splat[i] = charSplit(source, minorDelimiter, idx + dl, idx = source.indexOf(majorDelimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(majorDelimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount - 1] = charSplit(source, minorDelimiter, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount - 1] = charSplit(source, minorDelimiter, idx + dl, idx2);
        }
        return splat;

    }

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * char array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, char[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @return a 2D char array of the numbers found in source
     */
    public char[][] charSplit2D(String source, String majorDelimiter, String minorDelimiter) {
        return charSplit2D(source, majorDelimiter, minorDelimiter, 0, source.length());
    }

    private static final double[][] double2D = new double[0][0];

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * double array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, double[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @param startIndex     the first index, inclusive, in source to split from
     * @param endIndex       the last index, exclusive, in source to split from
     * @return a 2D double array of the numbers found in source
     */
    public double[][] doubleSplit2D(String source, String majorDelimiter, String minorDelimiter, int startIndex, int endIndex) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return double2D;
        int amount = count(source, majorDelimiter, startIndex, endIndex);
        if (amount <= 0)
            return Arrays.copyOf(double2D, 0);
        double[][] splat = Arrays.copyOf(double2D, amount);
        int dl = majorDelimiter.length(), idx = startIndex, idx2;
        for (int i = 0; i < amount - 1; i++) {
            splat[i] = doubleSplit(source, minorDelimiter, idx + dl, idx = source.indexOf(majorDelimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(majorDelimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount - 1] = doubleSplit(source, minorDelimiter, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount - 1] = doubleSplit(source, minorDelimiter, idx + dl, idx2);
        }
        return splat;

    }

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * double array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, double[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @return a 2D double array of the numbers found in source
     */
    public double[][] doubleSplit2D(String source, String majorDelimiter, String minorDelimiter) {
        return doubleSplit2D(source, majorDelimiter, minorDelimiter, 0, source.length());
    }

    private static final float[][] float2D = new float[0][0];

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * float array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, float[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @param startIndex     the first index, inclusive, in source to split from
     * @param endIndex       the last index, exclusive, in source to split from
     * @return a 2D float array of the numbers found in source
     */
    public float[][] floatSplit2D(String source, String majorDelimiter, String minorDelimiter, int startIndex, int endIndex) {
        if (majorDelimiter.equals(minorDelimiter) || majorDelimiter.length() == 0 || minorDelimiter.length() == 0)
            throw new IllegalArgumentException("The delimiters must be different, non-null, and non-empty.");
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return float2D;
        int amount = count(source, majorDelimiter, startIndex, endIndex);
        if (amount <= 0)
            return Arrays.copyOf(float2D, 0);
        float[][] splat = Arrays.copyOf(float2D, amount);
        int dl = majorDelimiter.length(), idx = startIndex, idx2;
        for (int i = 0; i < amount - 1; i++) {
            splat[i] = floatSplit(source, minorDelimiter, idx + dl, idx = source.indexOf(majorDelimiter, idx + dl));
        }
        if ((idx2 = source.indexOf(majorDelimiter, idx + dl)) < 0 || idx2 >= endIndex) {
            splat[amount - 1] = floatSplit(source, minorDelimiter, idx + dl, Math.min(source.length(), endIndex));
        } else {
            splat[amount - 1] = floatSplit(source, minorDelimiter, idx + dl, idx2);
        }
        return splat;

    }

    /**
     * Given a String containing sequences of numbers in this Base, with the sequences separated by instances of
     * majorDelimiter and the numbers within a sequence separated by minorDelimiter returns those numbers as a 2D
     * float array. This is specifically meant to read the format produced by
     * {@link #appendJoined(StringBuilder, String, float[])}, including the initial majorDelimiter before each sequence.
     *
     * @param source         a String of numbers in this base, separated by a delimiter, with no trailing delimiter
     * @param majorDelimiter the separator between sequences
     * @param minorDelimiter the separator between numbers
     * @return a 2D float array of the numbers found in source
     */
    public float[][] floatSplit2D(String source, String majorDelimiter, String minorDelimiter) {
        return floatSplit2D(source, majorDelimiter, minorDelimiter, 0, source.length());
    }

    /**
     * Scans repeatedly in {@code source} (only using the area from startIndex, inclusive, to endIndex, exclusive) for
     * the String {@code search}, not scanning the same char twice except as part of a larger String, and returns the
     * number of instances of search that were found, or 0 if source or search is null or if the searched area is empty.
     * If endIndex is negative, this will search from startIndex until the end of the source.
     * <br>
     * This really belongs in a different class with String operations, but it's the only method of its type we're using
     * here, so it can be in Base.
     *
     * @param source     a String to look through
     * @param search     a String to look for
     * @param startIndex the first index to search through, inclusive
     * @param endIndex   the last index to search through, exclusive; if negative this will search the rest of source
     * @return the number of times search was found in source
     */
    protected static int count(final String source, final String search, final int startIndex, int endIndex) {
        if (endIndex < 0)
            endIndex = 0x7fffffff;
        if (source.isEmpty() || search.isEmpty() || startIndex < 0 || startIndex >= endIndex)
            return 0;
        int amount = 0, idx = startIndex - 1;
        while ((idx = source.indexOf(search, idx + 1)) >= 0 && idx < endIndex)
            ++amount;
        return amount;
    }
    /**
     * Checks whether the String is a valid Java number.
     *
     * <p>Valid numbers include hexadecimal marked with the {@code 0x} or
     * {@code 0X} qualifier, octal numbers, scientific notation and
     * numbers marked with a type qualifier (e.g. 123L).</p>
     *
     * <p>Non-hexadecimal strings beginning with a leading zero are
     * treated as octal values. Thus the string {@code 09} will return
     * {@code false}, since {@code 9} is not a valid octal value.
     * However, numbers beginning with {@code 0.} are treated as decimal.</p>
     *
     * <p>{@code null} and empty/blank {@link String} will return
     * {@code false}.</p>
     * <br>
     * This is copied nearly-verbatim from Apache Commons Lang method NumberUtils.isCreatable(String),
     * <a href="https://github.com/apache/commons-lang/blob/469013a4f5a5cb666b35d72122690bb7f355c0b5/src/main/java/org/apache/commons/lang3/math/NumberUtils.java#L1601">available here</a>.
     * Some changes were made to avoid depending on the rest of Apache Commons.
     * <br>
     * This method isn't able to check for some valid Java numbers. Floats
     * and doubles formatted as hex floats won't be parsed at all, such as
     * {@code 0x1.FEp-1}. Floats and doubles that have leading zeroes but
     * do not have a decimal point in them will try to be parsed as octal
     * integers, which can yield erroneous results.
     *
     * @param str the {@link String} to check
     * @return {@code true} if the string is a correctly formatted number
     */
    public static boolean isJavaNumber(final String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        final char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = chars[0] == '-' || chars[0] == '+' ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0' && !str.contains(".")) { // leading 0, skip if is a decimal number
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                            && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
            if (Character.isDigit(chars[start + 1])) {
                // leading 0, but not hex, must be octal
                int i = start + 1;
                for (; i < chars.length; i++) {
                    if (chars[i] < '0' || chars[i] > '7') {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwards
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                    && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                    || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    public static boolean isValidFloatingPoint(final CharSequence str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        int sz = str.length();
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        char first = str.charAt(0);
        final int start = first == '-' || first == '+' ? 1 : 0;
        sz--; // don't want to loop to the last char, check it afterwards for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz) {
            if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (str.charAt(i) == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (str.charAt(i) == 'e' || str.charAt(i) == 'E') {
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (str.charAt(i) == '+' || str.charAt(i) == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < str.length()) {
            if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (str.charAt(i) == 'e' || str.charAt(i) == 'E') {
                // can't have an E at the last char
                return false;
            }
            if (str.charAt(i) == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                    && (str.charAt(i) == 'd'
                    || str.charAt(i) == 'D'
                    || str.charAt(i) == 'f'
                    || str.charAt(i) == 'F')) {
                return foundDigit;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Base base = (Base) o;

        if (paddingChar != base.paddingChar)
            return false;
        if (positiveSign != base.positiveSign)
            return false;
        if (negativeSign != base.negativeSign)
            return false;
        if (caseInsensitive != base.caseInsensitive)
            return false;
        return Arrays.equals(toEncoded, base.toEncoded);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(toEncoded);
        result = 31 * result + (caseInsensitive ? 1 : 0);
        result = 31 * result + (int) paddingChar;
        result = 31 * result + (int) positiveSign;
        result = 31 * result + (int) negativeSign;
        return result;
    }
}
