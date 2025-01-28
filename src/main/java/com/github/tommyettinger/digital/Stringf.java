/*
 * Copyright (c) 2024 See AUTHORS file.
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

/**
 * A limited polyfill for {@code String.format()} where the platform doesn't provide it, such as on GWT or TeaVM.
 * This supports {@link #format(String, Object...)} with only some of the formatting options in the actual, much-larger
 * Formatter class: {@code %% , %s , %d , %f , %x , %X}, and some precision options, such as {@code %.3f} . Scientific
 * notation is supported, but not with specified precision, yet. Some features will probably never be added here, like
 * time and date formatting, because this is GWT-compatible and GWT is missing lots of time-related code.
 * <br>
 * Some useful features here are that {@link #appendf(StringBuilder, String, Object...)} allows reusing a StringBuilder,
 * {@link #printf(String, Object...)} acts like a method on OutputStream (which is typically not provided if
 * String.format() isn't), and {@link #printfn(String, Object...)} acts like printf() with a newline like println().
 */
public final class Stringf {
    private Stringf(){}

    /**
     * Uses the given {@code args} to fill out the format String {@code fmt}, puts that in a new {@link StringBuilder},
     * and returns that.
     * @param fmt the format String; only supports a subset of Formatter options
     * @param args the parameters to {@code fmt}, as an array or varargs
     * @return a new {@link StringBuilder} containing the formatted text
     */
    public static StringBuilder appendf(String fmt, Object... args) {
        return appendf(new StringBuilder(fmt.length()), fmt, args);
    }

    /**
     * Appends the given format String {@code fmt} using the given {@code args}, if any, to {@code sb}, and returns
     * {@code sb} after changing it.
     * @param sb will be modified in-place
     * @param fmt the format String; only supports a subset of Formatter options
     * @param args the parameters to {@code fmt}, as an array or varargs
     * @return {@code sb}, after modifications
     */
    public static StringBuilder appendf(StringBuilder sb, String fmt, Object... args) {
        final int len = fmt.length();
        int arg = 0;
        for (int i = 0; i < len; i++) {
            char curr = fmt.charAt(i);
            if(curr == '%') {
                curr = fmt.charAt(++i);
                if (curr == '%') {
                    // literal percent sign
                    sb.append('%');
                } else if (curr == 's') {
                    // string
                    sb.append(args[arg++].toString());
                } else if (curr == 'd') {
                    // digit, decimal
                    sb.append(((Number) args[arg++]).longValue());
                } else if (curr == 'f') {
                    // float
                    double d = ((Number) args[arg++]).doubleValue();
                    Base.BASE10.appendDecimal(sb, d, -10000, 6);
                } else if (curr == 'e') {
                    // general, lower case
                    double d = ((Number) args[arg++]).doubleValue();
                    Base.BASE10.appendScientific(sb, d, false);
                } else if (curr == 'E') {
                    // general, lower case
                    double d = ((Number) args[arg++]).doubleValue();
                    Base.BASE10.appendScientific(sb, d, true);
                } else if (curr == 'g') {
                    // general, lower case
                    double d = ((Number) args[arg++]).doubleValue();
                    Base.BASE10.appendGeneral(sb, d, false);
                } else if (curr == 'G') {
                    // general, lower case
                    double d = ((Number) args[arg++]).doubleValue();
                    Base.BASE10.appendGeneral(sb, d, true);
                } else if (curr == 'x') {
                    // digit, hex (uncapitalized)
                    sb.append(Base.BASE16.signed(((Number) args[arg++]).longValue()).toLowerCase());
                } else if (curr == 'X') {
                    // digit, hex (capitalized)
                    Base.BASE16.appendSigned(sb, ((Number) args[arg++]).longValue());
                } else if (curr == '0') {
                    // zero-padding
                    curr = fmt.charAt(++i);
                    if(curr == 'x'){
                        sb.append(Base.BASE16.unsigned(((Number) args[arg++]).longValue()).toLowerCase());
                    } else if(curr == 'X'){
                        Base.BASE16.appendUnsigned(sb, ((Number) args[arg++]).longValue());
                    }
                    else {
                        int length = Base.BASE10.readInt(fmt, i, len);
                        while ((curr = fmt.charAt(++i)) >= '0' && curr <= '9'){
                        }
                        if(curr == 'x')
                            sb.append(TextTools.safeSubstring(Base.BASE16.unsigned(((Number) args[arg++]).longValue()), 16 - length, 16).toLowerCase());
                        else if(curr == 'X')
                            sb.append(TextTools.safeSubstring(Base.BASE16.unsigned(((Number) args[arg++]).longValue()), 16 - length, 16));
                        else if(curr == 'd')
                        {
                            String num = Base.BASE10.unsigned(((Number) args[arg++]).longValue());
                            sb.append(TextTools.safeSubstring(num, num.length() - length, num.length()));
                        }
                    }
                } else if (curr == '.') {
                    curr = fmt.charAt(++i);
                    if(curr == 'f'){
                        Base.BASE10.appendDecimal(sb, ((Number) args[arg++]).doubleValue());
                    } else if(curr == 'e'){
                        Base.BASE10.appendScientific(sb, ((Number) args[arg++]).doubleValue(), false);
                    } else if(curr == 'E'){
                        Base.BASE10.appendScientific(sb, ((Number) args[arg++]).doubleValue(), true);
                    } else if(curr == 'g'){
                        Base.BASE10.appendGeneral(sb, ((Number) args[arg++]).doubleValue(), false);
                    } else if(curr == 'G'){
                        Base.BASE10.appendGeneral(sb, ((Number) args[arg++]).doubleValue(), true);
                    }
                    else {
                        int precision = Base.BASE10.readInt(fmt, i, len);
                        while ((curr = fmt.charAt(++i)) >= '0' && curr <= '9') {
                        }
                        if (curr == 'f') {
                            Base.BASE10.appendDecimal(sb, ((Number) args[arg++]).doubleValue(), -10000, precision);
                        } else if (curr == 'e') {
                            Base.BASE10.appendScientific(sb, ((Number) args[arg++]).doubleValue(), false);
                        } else if (curr == 'E') {
                            Base.BASE10.appendScientific(sb, ((Number) args[arg++]).doubleValue(), true);
                        } else if (curr == 'g') {
                            Base.BASE10.appendGeneral(sb, ((Number) args[arg++]).doubleValue(), false);
                        } else if (curr == 'G') {
                            Base.BASE10.appendGeneral(sb, ((Number) args[arg++]).doubleValue(), true);
                        }
                    }
                } else if (curr >= '1' && curr <= '9') {
                    int length = Base.BASE10.readInt(fmt, i, len);
                    while ((curr = fmt.charAt(++i)) >= '0' && curr <= '9'){
                    }
                    if(curr == 'x')
                    {
                        String num = Base.BASE16.signed(((Number) args[arg++]).longValue());
                        sb.append(TextTools.safeSubstring(num, num.length() - length, num.length()).toLowerCase());
                    } else if(curr == 'X')
                    {
                        String num = Base.BASE16.signed(((Number) args[arg++]).longValue());
                        sb.append(TextTools.safeSubstring(num, num.length() - length, num.length()));
                    } else if(curr == 'd')
                    {
                        String num = Base.BASE10.signed(((Number) args[arg++]).longValue());
                        sb.append(TextTools.safeSubstring(num, num.length() - length, num.length()));
                    }
                }
            }
            else {
                sb.append(curr);
            }
        }
        return sb;
    }

    /**
     * Uses the given {@code args} to fill out the format String {@code fmt}, and returns that as a String
     * @param fmt the format String; only supports a subset of Formatter options
     * @param args the parameters to {@code fmt}, as an array or varargs
     * @return a new String containing the formatted text
     */
    public static String format(String fmt, Object... args) {
        return appendf(fmt, args).toString();
    }

    /**
     * Uses the given {@code args} to fill out the format String {@code fmt}, and prints that to {@code stdout}.
     * This is comparable to {@code java.io.PrintStream#print}.
     * @param fmt the format String; only supports a subset of Formatter options
     * @param args the parameters to {@code fmt}, as an array or varargs
     */
    public static void printf(String fmt, Object... args) {
        System.out.print(appendf(fmt, args));
    }

    /**
     * Uses the given {@code args} to fill out the format String {@code fmt}, and prints that to {@code stdout} with an
     * appended newline. This is comparable to {@code java.io.PrintStream#println}.
     * @param fmt the format String; only supports a subset of Formatter options
     * @param args the parameters to {@code fmt}, as an array or varargs
     */
    public static void printfn(String fmt, Object... args) {
        System.out.println(appendf(fmt, args));
    }

//    public static void main(String[] args) {
//        printfn("Coming up to the plate, it's number %d, %s!", 42, "Jackie Robinson");
//        printfn("His batting average is an outstanding %.3f, truly remarkable.", 0.415);
//    }
}
