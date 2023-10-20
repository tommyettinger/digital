package com.github.tommyettinger.digital;

public class RyuChecks {
    public static double nextExclusiveDouble (AlternateRandom random) {
        final long bits = random.nextLong();
        return BitConversion.longBitsToDouble(1022L - Long.numberOfTrailingZeros(bits) << 52 | bits >>> 12);
    }
    public static float nextExclusiveFloat (AlternateRandom random) {
        final long bits = random.nextLong();
        return BitConversion.intBitsToFloat(126 - Long.numberOfTrailingZeros(bits) << 23 | (int)(bits >>> 41));
    }

    public static void mainBounds(String[] args) {
        char[] buffer = new char[100];
        for (int low = -3; low >= -32; low--) {
            int limit = 0;
            double d = -Math.pow(10.0, low);
            for (int i = 0; i < 0x800000; i++) {
                limit = Math.max(limit, RyuDouble.general(d, buffer, low, 7));
                d = Math.nextDown(d);
            }
            System.out.println("With lower exponent bound " + low + ", this needs " + limit + " chars");
        }
        for (int high = 7; high <= 64; high++) {
            int limit = 0;
            double d = -Math.pow(10.0, high);
            for (int i = 0; i < 0x800000; i++) {
                limit = Math.max(limit, RyuDouble.general(d, buffer, -3, high));
                d = Math.nextUp(d);
            }
            System.out.println("With upper exponent bound " + high + ", this needs " + limit + " chars");
        }
    }
    /**
     * Some interesting results:
     * It should be obvious when you run this that Ryu prints a lot more digits. I'm not sure why this is.
     * The decimal number Ryu prints as {@code 0.00039954273149628435} gets printed differently by Java depending on
     * whether it is using {@code %g} or {@code %f}, with {@code 0.000399543} for the former and {@code 0.000400} for
     * the latter.
     * @param args unused
     */
    public static void main(String[] args) {
        AlternateRandom random = new AlternateRandom(1234567890L);
        for (int i = 0; i < 100; i++) {
            double d = Math.scalb((nextExclusiveDouble(random) / nextExclusiveDouble(random)) * (nextExclusiveDouble(random) - 0.5), -5);
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s, Ryu friendly : %-20s\n", RyuDouble.general(d), RyuDouble.decimal(d), RyuDouble.scientific(d), RyuDouble.friendly(d));
        }
        random.setSeed(1234567890L);
        for (int i = 0; i < 100; i++) {
            float d = Math.scalb((nextExclusiveFloat(random) / nextExclusiveFloat(random)) * (nextExclusiveFloat(random) - 0.5f), -5);
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s, Ryu friendly : %-20s\n", RyuFloat.general(d), RyuFloat.decimal(d), RyuFloat.scientific(d), RyuFloat.friendly(d));
        }
        //Java: 3.9954273149628435E-4 , Ryu general : 3.9954273149628435E-4 , Ryu friendly: 0.00039954273149628435

        System.out.println("\nDOUBLE SPECIALS:\n");
        for (double d : new double[]{Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NaN, 0.0, -0.0}) {
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s, Ryu friendly : %-20s\n", RyuDouble.general(d), RyuDouble.decimal(d), RyuDouble.scientific(d), RyuDouble.friendly(d));
        }

        System.out.println("\nFLOAT SPECIALS:\n");
        for (float d : new float[]{Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NaN, 0.0f, -0.0f}) {
            System.out.println(d);
            System.out.printf("Java general: %-20g, Java decimal: %-20f, Java scientific: %-20E\n", d, d, d);
            System.out.printf("Ryu general : %-20s, Ryu decimal : %-20s, Ryu scientific : %-20s, Ryu friendly : %-20s\n", RyuFloat.general(d), RyuFloat.decimal(d), RyuFloat.scientific(d), RyuFloat.friendly(d));
        }
        System.out.println();
        {
            String text = "MIN_NORMAL: ";
            String sb = RyuDouble.appendDecimal(new StringBuilder(text), Double.MIN_NORMAL).append(", and more").toString();
            double parsed = Base.BASE10.readDouble(sb, text.length(), Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Double.MIN_NORMAL);
        }
        System.out.println();
        {
            String text = "MIN_NORMAL to only 5 places: ";
            String sb = RyuDouble.appendDecimal(new StringBuilder(text), Double.MIN_NORMAL, 5).append(", and more").toString();
            double parsed = Base.BASE10.readDouble(sb, text.length(), Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Double.MIN_NORMAL);
        }
        System.out.println();
        {
            String text = "PI to only 12 places: ";
            String sb = RyuDouble.appendDecimal(new StringBuilder(text), TrigTools.PI, 12).append(", and more").toString();
            double parsed = Base.BASE10.readDouble(sb, text.length(), Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == TrigTools.PI);
        }
        System.out.println();
        {
            String text = "MIN_NORMAL: ";
            String sb = RyuFloat.appendDecimal(new StringBuilder(text), Float.MIN_NORMAL).append(", and more").toString();
            float parsed = Base.BASE10.readFloat(sb, text.length(), Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Float.MIN_NORMAL);
        }
        System.out.println();
        {
            String text = "MIN_NORMAL to only 5 places: ";
            String sb = RyuFloat.appendDecimal(new StringBuilder(text), Float.MIN_NORMAL, 5).append(", and more").toString();
            float parsed = Base.BASE10.readFloat(sb, text.length(), Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Float.MIN_NORMAL);
        }
        System.out.println();
        {
            String text = "PI to only 12 places: ";
            String sb = RyuFloat.appendDecimal(new StringBuilder(text), TrigTools.PI, 12).append(", and more").toString();
            float parsed = Base.BASE10.readFloat(sb, text.length(), Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == TrigTools.PI);
        }
        System.out.println();
        {
            String sb = RyuDouble.appendScientific(new StringBuilder("junk: "), Double.MIN_NORMAL).append(", and more").toString();
            double parsed = Base.BASE10.readDouble(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Double.MIN_NORMAL);
        }
        System.out.println();
        {
            String sb = RyuFloat.appendScientific(new StringBuilder("junk: "), Float.MIN_NORMAL).append(", and more").toString();
            float parsed = Base.BASE10.readFloat(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Float.MIN_NORMAL);
        }
        System.out.println();
        {
            char[] sb = RyuDouble.appendDecimal(new StringBuilder("junk: "), Double.MIN_NORMAL).append(", and more").toString().toCharArray();
            double parsed = Base.BASE10.readDouble(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Double.MIN_NORMAL);
        }
        System.out.println();
        {
            char[] sb = RyuFloat.appendDecimal(new StringBuilder("junk: "), Float.MIN_NORMAL).append(", and more").toString().toCharArray();
            float parsed = Base.BASE10.readFloat(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Float.MIN_NORMAL);
        }
        System.out.println();
        {
            char[] sb = RyuDouble.appendScientific(new StringBuilder("junk: "), Double.MIN_NORMAL).append(", and more").toString().toCharArray();
            double parsed = Base.BASE10.readDouble(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Double.MIN_NORMAL);
        }
        System.out.println();
        {
            char[] sb = RyuFloat.appendScientific(new StringBuilder("junk: "), Float.MIN_NORMAL).append(", and more").toString().toCharArray();
            float parsed = Base.BASE10.readFloat(sb, 5, Integer.MAX_VALUE);
            System.out.println(sb);
            System.out.println(parsed);
            System.out.println(parsed == Float.MIN_NORMAL);
        }
        System.out.println();

        System.out.println(isJavaNumber("0009E0")); // incorrect
        System.out.println(isValidFloatingPoint("0009E0")); // correct!

//Well, now you know why we use scientific notation -- so we don't print Double.MIN_NORMAL as 0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000022250738585072014
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
     * This is copied nearly-verbatim from the Apache Commons Lang method NumberUtils.isCreatable(String),
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

    /**
     * Validates if the given {@code str} can be parsed as a valid float or double. If {@code str} is null
     * or empty, this returns {@code false} rather than throwing an exception. This only correctly handles decimal or
     * scientific notation formats (in a format string, "%f", "%e", and "%g" will work, but "%a" will not).
     * <br>
     * Much of this method is from the Apache Commons Lang method NumberUtils.isCreatable(String),
     * <a href="https://github.com/apache/commons-lang/blob/469013a4f5a5cb666b35d72122690bb7f355c0b5/src/main/java/org/apache/commons/lang3/math/NumberUtils.java#L1601">available here</a>.
     * Changes were made so that it doesn't try to parse hexadecimal numbers (including hex floats) or octal numbers.
     *
     * @param str a CharSequence, such as a String, that may contain a valid float or double that can be parsed
     * @return true if a valid float or double can be parsed from the requested str, or false otherwise
     */
    public static boolean isValidFloatingPoint(final CharSequence str) {
        return isValidFloatingPoint(str, 0, Integer.MAX_VALUE);
    }

    /**
     * Validates if a range of the given {@code str} can be parsed as a valid float or double. Here, {@code begin} and
     * {@code end} are indices in the given {@code CharSequence}, and end must be greater than begin.  If begin is
     * negative, it will be clamped to be treated as {@code 0}. If end is greater
     * than the length of {@code str}, it will be clamped to be treated as {@code str.length()}. If {@code str} is null
     * or empty, this returns {@code false} rather than throwing an exception. This only correctly handles decimal or
     * scientific notation formats (in a format string, "%f", "%e", and "%g" will work, but "%a" will not).
     * <br>
     * Much of this method is from the Apache Commons Lang method NumberUtils.isCreatable(String),
     * <a href="https://github.com/apache/commons-lang/blob/469013a4f5a5cb666b35d72122690bb7f355c0b5/src/main/java/org/apache/commons/lang3/math/NumberUtils.java#L1601">available here</a>.
     * Changes were made so that it doesn't try to parse hexadecimal numbers (including hex floats) or octal numbers.
     *
     * @param str a CharSequence, such as a String, that may contain a valid float or double that can be parsed
     * @param begin the inclusive index to start reading at
     * @param end the exclusive index to stop reading before
     * @return true if a valid float or double can be parsed from the requested range, or false otherwise
     */
    public static boolean isValidFloatingPoint(final CharSequence str, int begin, int end) {
        if (str == null || (begin = Math.max(begin, 0)) >= end || str.length() < (end = Math.min(str.length(), end)) - begin) {
            return false;
        }
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;

        while (str.charAt(begin) <= ' ') {
            ++begin;
        }
        if(begin >= end) return false;

        // deal with any possible sign up front
        char first = str.charAt(begin);
        final int start = first == '-' || first == '+' ? begin + 1 : begin;
        end--; // don't want to loop to the last char, check it afterwards for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < end) {
            char ith = str.charAt(i);

            if (ith >= '0' && ith <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (ith == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (ith == 'e' || ith == 'E') {
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (ith == '+' || ith == '-') {
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
        if (i <= end) {
            char ith = str.charAt(i);
            if (ith >= '0' && ith <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (ith == 'e' || ith == 'E') {
                // can't have an E at the last char
                return false;
            }
            if (ith == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                    && (ith == 'd'
                    || ith == 'D'
                    || ith == 'f'
                    || ith == 'F')) {
                return foundDigit;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

}
