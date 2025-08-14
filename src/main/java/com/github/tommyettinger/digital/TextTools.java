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

import java.io.IOException;
import java.util.Iterator;

/**
 * Various methods for searching, joining, splitting, replacing, counting, and padding String and sometimes
 * CharSequence items. This also has code to join boolean arrays into Strings, and split those results into boolean
 * arrays, which is a similar feature to what exists in {@link Base} for numeric types.
 */
public final class TextTools {

    /**
     * No need to instantiate.
     */
    private TextTools(){
    }

    /**
     * Returns a String made from every {@code char[]} item in {@code elements}, with each {@code char[]} separated by
     * {@code delimiter}. Each {@code char[]} is written using the rules of {@link String#valueOf(char[])}.
     * @param delimiter a CharSequence to place between each {@code char[]}
     * @param elements an array or varargs of char arrays; if null, this returns the empty String
     * @return the items in {@code elements}, joined by {@code delimiter}
     */
    public static String joinArrays(CharSequence delimiter, char[]... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    /**
     * Appends every {@code char[]} item in {@code elements} to {@code sb}, with each {@code char[]} separated by
     * {@code delimiter}. Each {@code char[]} is written using the rules of {@link String#valueOf(char[])}.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param delimiter a CharSequence to place between each {@code char[]}
     * @param elements an array or varargs of char arrays; if null, this returns the empty String
     * @return {@code sb}, after having {@code elements} appended
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public static <T extends CharSequence & Appendable> T appendJoinedArrays(T sb, CharSequence delimiter, char[]... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        try {
            for (int i = 0, n = elements[0].length; i < n; i++) {
                sb.append(elements[0][i]);
            }
            for (int i = 1; i < elements.length; i++) {
                sb.append(delimiter);
                for (int j = 0, n = elements[i].length; j < n; j++) {
                    sb.append(elements[i][j]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb;
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a String, using "1" for true and "0" for false.
     * This is "dense" because it doesn't have any delimiters between elements.
     * @param elements an array or vararg of booleans
     * @return a String using 1 for true elements and 0 for false, or the empty string if elements is null or empty
     */
    public static String joinDense(boolean... elements) {
        return joinDense('1', '0', elements);
    }
    /**
     * Joins the boolean array {@code elements} without delimiters into a String, using the char {@code t} for
     * true and the char {@code f} for false. This is "dense" because it doesn't have any delimiters between
     * elements.
     * @param t the char to write for true values
     * @param f the char to write for false values
     * @param elements an array or vararg of booleans
     * @return a String using t for true elements and f for false, or the empty string if elements is null or empty
     */
    public static String joinDense(char t, char f, boolean... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(elements.length);
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i] ? t : f);
        }
        return sb.toString();
    }

    /**
     * Joins the part of the boolean array {@code elements} starting at {@code start} and extending for {@code length}
     * items, without delimiters, into a String, using the char {@code '1'} for true and the char {@code '0'} for false.
     * This is "dense" because it doesn't have any delimiters between elements.
     * @param elements an array or vararg of booleans
     * @param start the first index in elements to use
     * @param length how many items to use from elements, at most
     * @return a String using 1 for true elements and 0 for false, or the empty string if elements is null or empty
     */
    public static String joinDense(boolean[] elements, int start, int length) {
        return joinDense('1', '0', elements, start, length);
    }

    /**
     * Joins the part of the boolean array {@code elements} starting at {@code start} and extending for {@code length}
     * items, without delimiters, into a String, using the char {@code t} for true and the char {@code f} for false.
     * This is "dense" because it doesn't have any delimiters between elements.
     * @param t the char to write for true values
     * @param f the char to write for false values
     * @param elements an array or vararg of booleans
     * @param start the first index in elements to use
     * @param length how many items to use from elements, at most
     * @return a String using t for true elements and f for false, or the empty string if elements is null or empty
     */
    public static String joinDense(char t, char f, boolean[] elements, int start, int length) {
        if (elements == null || elements.length <= start || length <= 0)
            return "";
        StringBuilder sb = new StringBuilder(length);
        for (int c = 0; c < length && start < elements.length; start++, c++) {
            sb.append(elements[start] ? t : f);
        }
        return sb.toString();
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a StringBuilder, using "1" for true and "0" for
     * false. This is "dense" because it doesn't have any delimiters between elements.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param elements an array or vararg of booleans
     * @return sb after modifications (if elements was non-null)
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public static <T extends CharSequence & Appendable> T appendJoinedDense(T sb, boolean... elements) {
        return appendJoinedDense(sb, '1', '0', elements);
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a StringBuilder, using the char {@code t} for
     * true and the char {@code f} for false. This is "dense" because it doesn't have any delimiters between
     * elements.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param t the char to write for true values
     * @param f the char to write for false values
     * @param elements an array or vararg of booleans
     * @return sb after modifications (if elements was non-null)
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public static <T extends CharSequence & Appendable> T appendJoinedDense(T sb, char t, char f, boolean... elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        try {
            for (int i = 0; i < elements.length; i++) {
                sb.append(elements[i] ? t : f);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb;
    }

    /**
     * Joins the part of the boolean array {@code elements} starting at {@code start} and extending for {@code length}
     * items, without delimiters, into a String, using the char {@code '1'} for true and the char {@code '0'} for false.
     * This is "dense" because it doesn't have any delimiters between elements.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param elements an array or vararg of booleans
     * @param start the first index in elements to use
     * @param length how many items to use from elements, at most
     * @return sb, with at most length items appended
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public static <T extends CharSequence & Appendable> T appendJoinedDense(T sb, boolean[] elements, int start, int length) {
        return appendJoinedDense(sb, '1', '0', elements, start, length);
    }

    /**
     * Joins the part of the boolean array {@code elements} starting at {@code start} and extending for {@code length}
     * items, without delimiters, into a String, using the char {@code t} for true and the char {@code f} for false.
     * This is "dense" because it doesn't have any delimiters between elements.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param t the char to write for true values
     * @param f the char to write for false values
     * @param elements an array or vararg of booleans
     * @param start the first index in elements to use
     * @param length how many items to use from elements, at most
     * @return sb, with at most length items appended
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public static <T extends CharSequence & Appendable> T appendJoinedDense(T sb, char t, char f, boolean[] elements, int start, int length) {
        if (elements == null || elements.length <= start || length <= 0)
            return sb;
        try {
            for (int c = 0; c < length && start < elements.length; start++, c++) {
                sb.append(elements[start] ? t : f);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb;
    }

    /**
     * Given a CharSequence that may contain the char {@code '1'}, gets a boolean array where an occurrence of '1' in
     * the CharSequence produces true and any other char produces false. If source is null, or if source is empty, this
     * returns an empty array.
     *
     * @param source    a CharSequence, such as a String; if null, this returns an empty array
     * @return a boolean array that has true values where {@code '1'} was encountered in source
     */
    public static boolean[] booleanSplitDense(CharSequence source) {
        return booleanSplitDense(source, '1', 0, source.length());
    }

    /**
     * Given a CharSequence that may contain the char {@code t}, gets a boolean array where an occurrence of t in the
     * CharSequence produces true and any other char produces false. If source is null, or if source is empty, this
     * returns an empty array.
     *
     * @param source    a CharSequence, such as a String; if null, this returns an empty array
     * @param t         the char that signifies a true value
     * @return a boolean array that has true values where {@code t} was encountered in source
     */
    public static boolean[] booleanSplitDense(CharSequence source, char t) {
        return booleanSplitDense(source, t, 0, source.length());
    }

    /**
     * Given a CharSequence that may contain the char {@code '1'}, gets a boolean array where an occurrence of '1' in
     * the CharSequence produces true and any other char produces false. If source is null, or if source is empty, this
     * returns an empty array. This starts reading at {@code startIndex} and stops reading before {@code endIndex}. The
     * endIndex can safely be given as {@link Integer#MAX_VALUE} if you don't have or know an ending boundary; in that
     * case, this simply uses {@code source.length()} as endIndex.
     *
     * @param source     a CharSequence; if null, this returns an empty array
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a boolean array that has true values where {@code '1'} was encountered in source
     */
    public static boolean[] booleanSplitDense(CharSequence source, int startIndex, int endIndex) {
        return booleanSplitDense(source, '1', startIndex, endIndex);
    }


    /**
     * Given a CharSequence that may contain the char {@code t}, gets a boolean array where an occurrence of t in the
     * CharSequence produces true and any other char produces false. If source is null, or if source is empty, this
     * returns an empty array. This starts reading at {@code startIndex} and stops reading before {@code endIndex}. The
     * endIndex can safely be given as {@link Integer#MAX_VALUE} if you don't have or know an ending boundary; in that
     * case, this simply uses {@code source.length()} as endIndex.
     *
     * @param source     a CharSequence; if null, this returns an empty array
     * @param t          the char that signifies a true value
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a boolean array that has true values where {@code t} was encountered in source
     */
    public static boolean[] booleanSplitDense(CharSequence source, char t, int startIndex, int endIndex) {
        if (endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new boolean[0];
        endIndex = Math.min(endIndex, source.length());
        int amount = endIndex - startIndex;
        boolean[] splat = new boolean[amount];
        for (int i = 0; i < amount; i++) {
            if (source.charAt(startIndex++) == t)
                splat[i] = true;
        }
        return splat;
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. Unlike other join methods in this class, this does
     * not take a vararg of Object items, since that would cause confusion with the overloads that take one object, such
     * as {@link #join(CharSequence, Iterable)}; it takes a non-vararg Object array instead.
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if the array is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     */
    public static String join(CharSequence delimiter, Object[] elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        sb.append(elements[0]);
        if(delimiter == null) delimiter = "";
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    public String join(CharSequence delimiter, Object[] elements, int start, int length) {
        if (elements == null || elements.length <= start || length <= 0)
            return "";
        StringBuilder sb = new StringBuilder(elements.length << 3);
        sb.append(elements[start]);
        if(delimiter == null) delimiter = "";
        ++start;
        for (int c = 1; c < length && start < elements.length; start++, c++) {
            sb.append(delimiter).append(elements[start]);
        }
        return sb.toString();
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. This can take any Iterable of any type for its
     * {@code elements} parameter.
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if Iterable is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     */
    public static String join(CharSequence delimiter, Iterable<?> elements) {
        if (elements == null) return "";
        Iterator<?> it = elements.iterator();
        if(!it.hasNext()) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(it.next());
        if(delimiter == null) delimiter = "";
        while(it.hasNext()) {
            sb.append(delimiter).append(it.next());
        }
        return sb.toString();
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. Unlike other join methods in this class, this does
     * not take a vararg of Object items, since that would cause confusion with the overloads that take one object, such
     * as {@link #join(CharSequence, Iterable)}; it takes a non-vararg Object array instead.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if the array is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return sb after modifications (if elements was non-null)
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public static <T extends CharSequence & Appendable> T appendJoined(T sb, CharSequence delimiter, Object[] elements) {
        if (sb == null || elements == null || elements.length == 0) return sb;
        try {
            sb.append(elements[0].toString());
            if (delimiter == null) delimiter = "";
            for (int i = 1; i < elements.length; i++) {
                sb.append(delimiter).append(elements[i].toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb;
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}.
     * <br>
     * Unlike other join methods in this class, this does
     * not take a vararg of Object items, since that would cause confusion with the overloads that take one object, such
     * as {@link #join(CharSequence, Iterable)}; it takes a non-vararg Object array instead.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements  the Object items to stringify and join into one String; if the array is null or empty, this
     *                  returns an empty String, and if items are null, they are shown as "null"
     * @param start     the first index, inclusive, in elements to read from
     * @param length    the number of items, at most, to join together
     * @return sb after modifications (if elements was non-null)
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public <T extends CharSequence & Appendable> T appendJoined(T sb, CharSequence delimiter, Object[] elements, int start, int length) {
        if (sb == null || elements == null || elements.length <= start || length <= 0)
            return sb;
        try {
            sb.append(elements[start].toString());
            if (delimiter == null) delimiter = "";
            ++start;
            for (int c = 1; c < length && start < elements.length; start++, c++) {
                sb.append(delimiter).append(elements[start].toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb;
    }


    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. This can take any Iterable of any type for its
     * {@code elements} parameter.
     * This accepts any types for {@code sb} that are both CharSequence and Appendable, such as {@link StringBuilder},
     * {@link StringBuffer}, and {@link java.nio.CharBuffer}.
     * @param sb a StringBuilder or similar that will be modified in-place
     * @param delimiter the String or other CharSequence to separate items in elements with; if null, uses ""
     * @param elements the Object items to stringify and join into one String; if Iterable is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return sb after modifications (if elements was non-null)
     * @param <T> any type that is both a CharSequence and an Appendable, such as StringBuilder, StringBuffer, or CharBuffer
     */
    public static <T extends CharSequence & Appendable> T appendJoined(T sb, CharSequence delimiter, Iterable<?> elements) {
        if (sb == null || elements == null) return sb;
        Iterator<?> it = elements.iterator();
        if(!it.hasNext()) return sb;
        try {
            sb.append(it.next().toString());
            if (delimiter == null) delimiter = "";
            while (it.hasNext()) {
                sb.append(delimiter).append(it.next().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb;
    }

    /**
     * Searches text for the exact contents of the char array search; returns true if text contains search.
     * @param text a CharSequence, such as a String or StringBuilder, that might contain search
     * @param search a char array to try to find in text
     * @return true if search was found
     */
    public static boolean contains(CharSequence text, CharSequence search) {
        return !(text == null || text.length() == 0 || search == null || search.length() == 0)
                && containsPart(text, search) == search.length();
    }

    /**
     * Tries to find as much of the char array {@code search} in the CharSequence {@code text}, always starting from the
     * beginning of search (if the beginning isn't found, then it finds nothing), and returns the length of the found
     * part of search (0 if not found).
     * @param text a CharSequence to search in
     * @param search a char array to look for
     * @return the length of the searched-for char array that was found
     */
    public static int containsPart(CharSequence text, CharSequence search)
    {
        if(text == null || text.length() == 0 || search == null || (search.length() == 0))
            return 0;
        int sl = search.length(), tl = text.length() - sl, f = 0;
        char s = search.charAt(0);
        PRIMARY:
        for (int i = 0; i <= tl; i++) {
            if (text.charAt(i) == s) {
                for (int j = i + 1, x = 1; x < sl; j++, x++) {
                    if (text.charAt(j) != search.charAt(x)) {
                        f = Math.max(f, x);
                        continue PRIMARY;
                    }
                }
                return sl;
            }
        }
        return f;
    }

    /**
     * Searches text for the exact contents of the char array search; returns true if text contains search.
     * @param text a CharSequence, such as a String or StringBuilder, that might contain search
     * @param search a char array to try to find in text
     * @return true if search was found
     */
    public static boolean contains(CharSequence text, char[] search) {
        return !(text == null || text.length() == 0 || search == null || search.length == 0)
                && containsPart(text, search) == search.length;
    }

    /**
     * Tries to find as much of the char array {@code search} in the CharSequence {@code text}, always starting from the
     * beginning of search (if the beginning isn't found, then it finds nothing), and returns the length of the found
     * part of search (0 if not found).
     * @param text a CharSequence to search in
     * @param search a char array to look for
     * @return the length of the searched-for char array that was found
     */
    public static int containsPart(CharSequence text, char[] search)
    {
        if(text == null || text.length() == 0 || search == null || (search.length == 0))
            return 0;
        int sl = search.length, tl = text.length() - sl, f = 0;
        char s = search[0];
        PRIMARY:
        for (int i = 0; i <= tl; i++) {
            if (text.charAt(i) == s) {
                for (int j = i + 1, x = 1; x < sl; j++, x++) {
                    if (text.charAt(j) != search[x]) {
                        f = Math.max(f, x);
                        continue PRIMARY;
                    }
                }
                return sl;
            }
        }
        return f;
    }

    /**
     * Really simple; just returns {@code text.toString().replace(before, after)}.
     * It's only here for convenience, really.
     * @param text optimally a String, but may be any non-null CharSequence
     * @param before the CharSequence (often a String) to search for
     * @param after the CharSequence (often a String) to replace with
     * @return a String resulting from replacing occurrences of before in text with after
     */
    public static String replace(CharSequence text, CharSequence before, CharSequence after) {
        return text.toString().replace(before, after);
    }

    /**
     * Scans repeatedly in {@code source} for the String {@code search}, not scanning the same char twice except as part
     * of a larger String, and returns the number of instances of search that were found, or 0 if source is null or if
     * search is null or empty.
     * @param source a String to look through
     * @param search a String to look for
     * @return the number of times search was found in source
     */
    public static int count(final String source, final String search)
    {
        if(source == null || search == null || source.isEmpty() || search.isEmpty())
            return 0;
        int amount = 0, idx = -1;
        while ((idx = source.indexOf(search, idx+1)) >= 0)
            ++amount;
        return amount;
    }

    /**
     * Scans repeatedly in {@code source} for the codepoint {@code searchChar} (which is usually a char literal), not
     * scanning the same section twice, and returns the number of instances of searchChar that were found, or 0 if source is
     * null.
     * @param source a String to look through
     * @param searchChar a codepoint or char to look for
     * @return the number of times searchChar was found in source
     */
    public static int count(final String source, final int searchChar)
    {
        if(source == null || source.isEmpty())
            return 0;
        int amount = 0, idx = -1;
        while ((idx = source.indexOf(searchChar, idx+1)) >= 0)
            ++amount;
        return amount;
    }
    /**
     * Scans repeatedly in {@code source} (only using the area from startIndex, inclusive, to endIndex, exclusive) for
     * the String {@code search}, not scanning the same char twice except as part of a larger String, and returns the
     * number of instances of search that were found, or 0 if source or search is null or if the searched area is empty.
     * If endIndex is negative, this will search from startIndex until the end of the source.
     * @param source a String to look through
     * @param search a String to look for
     * @param startIndex the first index to search through, inclusive
     * @param endIndex the last index to search through, exclusive; if negative this will search the rest of source
     * @return the number of times search was found in source
     */
    public static int count(final String source, final String search, final int startIndex, int endIndex)
    {
        if(endIndex < 0) endIndex = 0x7fffffff;
        if(source == null || search == null || source.isEmpty() || search.isEmpty()
                || startIndex < 0 || startIndex >= endIndex)
            return 0;
        int amount = 0, idx = startIndex-1, slen = search.length();
        while ((idx = source.indexOf(search, idx+1)) >= 0 && idx + slen <= endIndex)
            ++amount;
        return amount;
    }

    /**
     * Scans repeatedly in {@code source} (only using the area from startIndex, inclusive, to endIndex, exclusive) for
     * the codepoint {@code searchChar} (which is usually a char literal), not scanning the same section twice, and returns
     * the number of instances of searchChar that were found, or 0 if source is null or if the searched area is empty.
     * If endIndex is negative, this will search from startIndex until the end of the source.
     * @param source a String to look through
     * @param searchChar a codepoint or char to look for
     * @param startIndex the first index to search through, inclusive
     * @param endIndex the last index to search through, exclusive; if negative this will search the rest of source
     * @return the number of times searchChar was found in source
     */
    public static int count(final String source, final int searchChar, final int startIndex, int endIndex)
    {
        if(endIndex < 0) endIndex = 0x7fffffff;
        if(source == null || source.isEmpty() || startIndex < 0 || startIndex >= endIndex)
            return 0;
        int amount = 0, idx = startIndex-1;
        while ((idx = source.indexOf(searchChar, idx+1)) >= 0 && idx < endIndex)
            ++amount;
        return amount;
    }

    /**
     * Like {@link String#substring(int, int)} but returns "" instead of throwing any sort of Exception.
     * @param source the String to get a substring from
     * @param beginIndex the first index, inclusive; will be treated as 0 if negative
     * @param endIndex the index after the last character (exclusive); if negative this will be source.length()
     * @return the substring of source between beginIndex and endIndex, or "" if any parameters are null/invalid
     */
    public static String safeSubstring(String source, int beginIndex, int endIndex)
    {
        if(source == null || source.isEmpty()) return "";
        if(beginIndex < 0) beginIndex = 0;
        if(endIndex < 0 || endIndex > source.length()) endIndex = source.length();
        if(beginIndex >= endIndex) return "";
        return source.substring(beginIndex, endIndex);
    }

    /**
     * Like {@link String#split(String)} but doesn't use any regex for splitting (the delimiter is a literal String).
     * This can be used to split groups of Strings joined by {@link #join(CharSequence, Object[])}.
     * @param source the String to get split-up substrings from
     * @param delimiter the literal String to split on (not a regex); will not be included in the returned String array
     * @return a String array consisting of at least one String (the entirety of Source if nothing was split)
     */
    public static String[] split(String source, String delimiter) {
        int amount = count(source, delimiter);
        if (amount <= 0) return new String[]{source};
        String[] splat = new String[amount+1];
        int dl = delimiter.length(), idx = -dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = safeSubstring(source, idx+dl, idx = source.indexOf(delimiter, idx+dl));
        }
        if((idx2 = source.indexOf(delimiter, idx+dl)) < 0)
        {
            splat[amount] = safeSubstring(source, idx+dl, source.length());
        }
        else
        {
            splat[amount] = safeSubstring(source, idx+dl, idx2);
        }
        return splat;
    }

    /**
     * Like {@link String#split(String)} but doesn't use any regex for splitting (the delimiter is a literal String).
     * This overload allows specifying an (inclusive) start index and (exclusive) end index.
     * This can be used to split groups of Strings joined by {@link #join(CharSequence, Object[])}.
     * @param source the String to get split-up substrings from
     * @param delimiter the literal String to split on (not a regex); will not be included in the returned String array
     * @param startIndex the first index, inclusive, in source to split from
     * @param endIndex   the last index, exclusive, in source to split from
     * @return a String array consisting of at least one String (the span of Source between startIndex and endIndex if nothing was split)
     */
    public static String[] split(String source, String delimiter, int startIndex, int endIndex) {
        if (source == null || delimiter == null || endIndex <= startIndex || startIndex < 0 || startIndex >= source.length())
            return new String[0];
        endIndex = Math.min(endIndex, source.length());
        int amount = count(source, delimiter, startIndex, endIndex);
        if (amount <= 0) return new String[]{source.substring(startIndex, endIndex)};
        String[] splat = new String[amount+1];
        int dl = delimiter.length(), idx = startIndex - dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = safeSubstring(source, idx+dl, idx = source.indexOf(delimiter, idx+dl));
        }
        if((idx2 = source.indexOf(delimiter, idx+dl)) < 0)
        {
            splat[amount] = safeSubstring(source, idx+dl, endIndex);
        }
        else
        {
            splat[amount] = safeSubstring(source, idx+dl, idx2);
        }
        return splat;
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the right with spaces until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with spaces to reach the given minimum length
     */
    public static String padRight(String text, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padRightStrict(text, ' ', minimumLength);
        return text;
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the right with padChar
     * until it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param padChar the char to use to pad text, if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with padChar to reach the given minimum length
     */
    public static String padRight(String text, char padChar, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padRightStrict(text, padChar, minimumLength);
        return text;
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its right side with spaces until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly extra spaces
     */
    public static String padRightStrict(String text, int totalLength) {
        return padRightStrict(text, ' ', totalLength);
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its right side with padChar until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param padChar the char to use to fill any remaining length
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly padChar
     */
    public static String padRightStrict(String text, char padChar, int totalLength) {
        char[] c = new char[totalLength];
        int len = text.length();
        text.getChars(0, Math.min(len, totalLength), c, 0);
        for (int i = len; i < totalLength; i++) {
            c[i] = padChar;
        }
        return String.valueOf(c);
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the left with spaces until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with spaces to reach the given minimum length
     */
    public static String padLeft(String text, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padLeftStrict(text, ' ', minimumLength);
        return text;
    }
    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the left with padChar until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param padChar the char to use to pad text, if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with padChar to reach the given minimum length
     */
    public static String padLeft(String text, char padChar, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padLeftStrict(text, padChar, minimumLength);
        return text;
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its left side with spaces until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly extra spaces
     */
    public static String padLeftStrict(String text, int totalLength) {
        return padLeftStrict(text, ' ', totalLength);
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its left side with padChar until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param padChar the char to use to fill any remaining length
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly padChar
     */
    public static String padLeftStrict(String text, char padChar, int totalLength) {
        char[] c = new char[totalLength];
        int len = text.length();
        text.getChars(0, Math.min(len, totalLength), c, Math.max(0, totalLength - len));
        for (int i = totalLength - len - 1; i >= 0; i--) {
            c[i] = padChar;
        }
        return String.valueOf(c);
    }

    /**
     * Creates a String made by repeating {@code item} {@code amount} times, with no delimiter.
     * @param item any non-null CharSequence, such as a String, to repeat
     * @param amount how many times to repeat {@code item}
     * @return a new String containing {@code item} {@code amount} times
     */
    public static String repeat(CharSequence item, int amount) {
        if(item == null) return null;
        return appendRepeated(new StringBuilder(item.length() * amount), item, amount).toString();
    }

    /**
     * Creates a String made by repeating {@code item} {@code amount} times, separating repetitions of {@code item} with
     * {@code delimiter}.
     * @param item any non-null CharSequence, such as a String, to repeat
     * @param amount how many times to repeat {@code item}
     * @param delimiter a non-null CharSequence to append between repetitions of {@code item}
     * @return a new String containing {@code item} {@code amount} times, separated by {@code delimiter}
     */
    public static String repeat(CharSequence item, int amount, CharSequence delimiter) {
        if(item == null || delimiter == null) return null;
        if(amount <= 0) return "";
        return appendRepeated(new StringBuilder(item.length() * amount + delimiter.length() * (amount - 1)),
                item, amount, delimiter).toString();
    }

    /**
     * Appends the text {@code item} to the StringBuilder {@code sb} repeatedly, {@code amount} times.
     * Returns {@code sb}.
     * @param sb a non-null StringBuilder that will be appended to
     * @param item any non-null CharSequence, such as a String, to repeat
     * @param amount how many times to repeat {@code item}
     * @return {@code sb}, after modifications
     */
    public static StringBuilder appendRepeated(StringBuilder sb, CharSequence item, int amount) {
        if(sb == null || item == null || amount <= 0) return sb;
        for (int i = 0; i < amount; i++) {
            sb.append(item);
        }
        return sb;
    }

    /**
     * Appends the text {@code item} to the StringBuilder {@code sb} repeatedly, {@code amount} times, separating
     * repetitions of {@code item} with {@code delimiter}.
     * Returns {@code sb}.
     * @param sb a non-null StringBuilder that will be appended to
     * @param item any non-null CharSequence, such as a String, to repeat
     * @param amount how many times to repeat {@code item}
     * @param delimiter a non-null CharSequence to append between repetitions of {@code item}
     * @return {@code sb}, after modifications
     */
    public static StringBuilder appendRepeated(StringBuilder sb, CharSequence item, int amount, CharSequence delimiter) {
        if(sb == null || item == null || delimiter == null || amount <= 0) return sb;
        sb.append(item);
        for (int i = 1; i < amount; i++) {
            sb.append(delimiter).append(item);
        }
        return sb;
    }
}
