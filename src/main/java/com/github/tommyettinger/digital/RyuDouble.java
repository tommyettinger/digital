/*
 * Copyright 2018 Ulf Adams
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

import java.math.BigInteger;

/**
 * An implementation of the Ryu double-to-String algorithm.
 * See <a href="https://github.com/ulfjack/ryu">Ryu's GitHub repo</a> for more info.
 * <br>
 * Ryu is licensed under Apache 2.0, the same as digital. It was written by Ulf Adams and contributors.
 */
final class RyuDouble {
  private static final int DOUBLE_MANTISSA_BITS = 52;
  private static final long DOUBLE_MANTISSA_MASK = (1L << DOUBLE_MANTISSA_BITS) - 1;

  private static final int DOUBLE_EXPONENT_BITS = 11;
  private static final int DOUBLE_EXPONENT_MASK = (1 << DOUBLE_EXPONENT_BITS) - 1;
  private static final int DOUBLE_EXPONENT_BIAS = (1 << (DOUBLE_EXPONENT_BITS - 1)) - 1;

  private static final int POS_TABLE_SIZE = 326;
  private static final int NEG_TABLE_SIZE = 291;

  private static final int POW5_BITCOUNT = 121; // max 3*31 = 124
  private static final int POW5_QUARTER_BITCOUNT = 31;
  private static final int[][] POW5_SPLIT = new int[POS_TABLE_SIZE][4];

  private static final int POW5_INV_BITCOUNT = 122; // max 3*31 = 124
  private static final int POW5_INV_QUARTER_BITCOUNT = 31;
  private static final int[][] POW5_INV_SPLIT = new int[NEG_TABLE_SIZE][4];

  private static final char[] result = new char[32];

  static {
    BigInteger mask = BigInteger.ONE.shiftLeft(POW5_QUARTER_BITCOUNT).subtract(BigInteger.ONE);
    BigInteger invMask = BigInteger.ONE.shiftLeft(POW5_INV_QUARTER_BITCOUNT).subtract(BigInteger.ONE);
    for (int i = 0; i < POS_TABLE_SIZE; i++) {
      BigInteger pow = BigInteger.valueOf(5).pow(i);
      int pow5len = pow.bitLength();
      int expectedPow5Bits = pow5bits(i);
      if (expectedPow5Bits != pow5len) {
        throw new IllegalStateException(pow5len + " != " + expectedPow5Bits);
      }
      for (int j = 0; j < 4; j++) {
        POW5_SPLIT[i][j] = pow
                .shiftRight(pow5len - POW5_BITCOUNT + (3 - j) * POW5_QUARTER_BITCOUNT)
                .and(mask)
                .intValue();
      }

      if (i < POW5_INV_SPLIT.length) {
        // We want floor(log_2 5^q) here, which is pow5len - 1.
        int j = pow5len - 1 + POW5_INV_BITCOUNT;
        BigInteger inv = BigInteger.ONE.shiftLeft(j).divide(pow).add(BigInteger.ONE);
        for (int k = 0; k < 4; k++) {
          if (k == 0) {
            POW5_INV_SPLIT[i][k] = inv.shiftRight((3 - k) * POW5_INV_QUARTER_BITCOUNT).intValue();
          } else {
            POW5_INV_SPLIT[i][k] = inv.shiftRight((3 - k) * POW5_INV_QUARTER_BITCOUNT).and(invMask).intValue();
          }
        }
      }
    }
  }

  public static int general(double value, char[] result) {
    return general(value, result, -3, 7);
  }

  public static String general(double value) {
    final int index = general(value, result, -3, 7);
    return new String(result, 0, index);
  }

  public static StringBuilder appendGeneral(StringBuilder builder, double value) {
    return appendGeneral(builder, value, result, -3, 7);
  }

  public static StringBuilder appendGeneral(StringBuilder builder, double value, char[] result) {
    return appendGeneral(builder, value, result, -3, 7);
  }

  public static int friendly(double value, char[] result) {
    return general(value, result, -10, 10);
  }

  public static String friendly(double value) {
    final int index = general(value, result, -10, 10);
    return new String(result, 0, index);
  }

  public static StringBuilder appendFriendly(StringBuilder builder, double value) {
    return appendGeneral(builder, value, result, -10, 10);
  }

  public static StringBuilder appendFriendly(StringBuilder builder, double value, char[] result) {
    return appendGeneral(builder, value, result, -10, 10);
  }

  public static StringBuilder appendGeneral(StringBuilder builder, double value, char[] result, int low, int high) {
    final int index = general(value, result, low, high);
    return builder.append(result, 0, index);
  }

  public static int general(double value, char[] result, int low, int high) {
    // Step 1: Decode the floating point number, and unify normalized and subnormal cases.
    // First, handle all the trivial cases.
    if (Double.isNaN(value)) {
      result[0] = 'N';
      result[1] = 'a';
      result[2] = 'N';
      return 3;
    }
    if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY) {
      int idx = 0;
      if (value == Double.NEGATIVE_INFINITY) {
        result[idx++] = '-';
      }
      result[idx++] = 'I';
      result[idx++] = 'n';
      result[idx++] = 'f';
      result[idx++] = 'i';
      result[idx++] = 'n';
      result[idx++] = 'i';
      result[idx++] = 't';
      result[idx++] = 'y';
      return idx;
    }
    long bits = BitConversion.doubleToLongBits(value);
    if (bits == 0) {
      result[0] = '0';
      result[1] = '.';
      result[2] = '0';
      return 3;
    }
    if (bits == 0x8000000000000000L){
      result[0] = '-';
      result[1] = '0';
      result[2] = '.';
      result[3] = '0';
      return 4;
    }

    // Otherwise extract the mantissa and exponent bits and run the full algorithm.
    int ieeeExponent = (int) ((bits >>> DOUBLE_MANTISSA_BITS) & DOUBLE_EXPONENT_MASK);
    long ieeeMantissa = bits & DOUBLE_MANTISSA_MASK;
    int e2;
    long m2;
    if (ieeeExponent == 0) {
      // Denormal number - no implicit leading 1, and the exponent is 1, not 0.
      e2 = 1 - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
      m2 = ieeeMantissa;
    } else {
      // Add implicit leading 1.
      e2 = ieeeExponent - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
      m2 = ieeeMantissa | (1L << DOUBLE_MANTISSA_BITS);
    }

    boolean sign = bits < 0;

    // Step 2: Determine the interval of legal decimal representations.
    boolean even = (m2 & 1) == 0;
    final long mv = 4 * m2;
    final long mp = 4 * m2 + 2;
    final int mmShift = ((m2 != (1L << DOUBLE_MANTISSA_BITS)) || (ieeeExponent == 1)) ? 1 : 0;
    final long mm = 4 * m2 - 1 - mmShift;
    e2 -= 2;

    // Step 3: Convert to a decimal power base using 128-bit arithmetic.
    // -1077 = 1 - 1023 - 53 - 2 <= e_2 - 2 <= 2046 - 1023 - 53 - 2 = 968
    long dv, dp, dm;
    final int e10;
    boolean dmIsTrailingZeros = false, dvIsTrailingZeros = false;
    if (e2 >= 0) {
      final int q = Math.max(0, ((e2 * 78913) >>> 18) - 1);
      // k = constant + floor(log_2(5^q))
      final int k = POW5_INV_BITCOUNT + pow5bits(q) - 1;
      final int i = -e2 + q + k;
      dv = mulPow5InvDivPow2(mv, q, i);
      dp = mulPow5InvDivPow2(mp, q, i);
      dm = mulPow5InvDivPow2(mm, q, i);
      e10 = q;

      if (q <= 21) {
        if (mv % 5 == 0) {
          dvIsTrailingZeros = multipleOfPowerOf5(mv, q);
        } else if (even) {
          dmIsTrailingZeros = multipleOfPowerOf5(mm, q);
        } else if (multipleOfPowerOf5(mp, q)) {
          dp--;
        }
      }
    } else {
      final int q = Math.max(0, ((-e2 * 732923) >>> 20) - 1);
      final int i = -e2 - q;
      final int k = pow5bits(i) - POW5_BITCOUNT;
      final int j = q - k;
      dv = mulPow5divPow2(mv, i, j);
      dp = mulPow5divPow2(mp, i, j);
      dm = mulPow5divPow2(mm, i, j);
      e10 = q + e2;
      if (q <= 1) {
        dvIsTrailingZeros = true;
        if (even) {
          dmIsTrailingZeros = mmShift == 1;
        } else {
          dp--;
        }
      } else if (q < 63) {
        dvIsTrailingZeros = (mv & ((1L << (q - 1)) - 1)) == 0;
      }
    }

    // Step 4: Find the shortest decimal representation in the interval of legal representations.
    //
    // We do some extra work here in order to follow Float/Double.toString semantics. In particular,
    // that requires printing in scientific format if and only if the exponent is between -3 and 7,
    // and it requires printing at least two decimal digits.
    //
    // Above, we moved the decimal dot all the way to the right, so now we need to count digits to
    // figure out the correct exponent for scientific notation.
    final int vplength = decimalLength(dp);
    int exp = e10 + vplength - 1;

    // Double.toString semantics requires using scientific notation if and only if outside this range.
    boolean scientificNotation = !((exp >= low) && (exp < high));

    int removed = 0;

    int lastRemovedDigit = 0;
    long output;
    if (dmIsTrailingZeros || dvIsTrailingZeros) {
      while (dp / 10 > dm / 10) {
        if ((dp < 100) && scientificNotation) {
          // Double.toString semantics requires printing at least two digits.
          break;
        }
        dmIsTrailingZeros &= dm % 10 == 0;
        dvIsTrailingZeros &= lastRemovedDigit == 0;
        lastRemovedDigit = (int) (dv % 10);
        dp /= 10;
        dv /= 10;
        dm /= 10;
        removed++;
      }
      if (dmIsTrailingZeros) {
        while (dm % 10 == 0) {
          if ((dp < 100) && scientificNotation) {
            // Double.toString semantics requires printing at least two digits.
            break;
          }
          dvIsTrailingZeros &= lastRemovedDigit == 0;
          lastRemovedDigit = (int) (dv % 10);
          dp /= 10;
          dv /= 10;
          dm /= 10;
          removed++;
        }
      }
      if (dvIsTrailingZeros && (lastRemovedDigit == 5) && ((dv & 1) == 0)) {
        // Round even if the exact numbers is .....50..0.
        lastRemovedDigit = 4;
      }
      output = dv +
          (dv == dm && !dmIsTrailingZeros || lastRemovedDigit >= 5 ? 1 : 0);
    } else {
      while (dp / 10 > dm / 10) {
        if ((dp < 100) && scientificNotation) {
          // Double.toString semantics requires printing at least two digits.
          break;
        }
        lastRemovedDigit = (int) (dv % 10);
        dp /= 10;
        dv /= 10;
        dm /= 10;
        removed++;
      }
      output = dv + ((dv == dm || (lastRemovedDigit >= 5)) ? 1 : 0);
    }
    int olength = vplength - removed;

    // Step 5: Print the decimal representation.
    // We follow Double.toString semantics here.
    int index = 0;
    if (sign) {
      result[index++] = '-';
    }

    // Values in the interval [1E-3, 1E7) are special.
    if (scientificNotation) {
      // Print in the format x.xxxxxE-yy.
      for (int i = 0; i < olength - 1; i++) {
        int c = (int) (output % 10); output /= 10;
        result[index + olength - i] = (char) ('0' + c);
      }
      result[index] = (char) ('0' + output % 10);
      result[index + 1] = '.';
      index += olength + 1;
      if (olength == 1) {
        result[index++] = '0';
      }

      // Print 'E', the exponent sign, and the exponent, which has at most three digits.
      result[index++] = 'E';
      if (exp < 0) {
        result[index++] = '-';
        exp = -exp;
      }
      if (exp >= 100) {
        result[index++] = (char) ('0' + exp / 100);
        exp %= 100;
        result[index++] = (char) ('0' + exp / 10);
      } else if (exp >= 10) {
        result[index++] = (char) ('0' + exp / 10);
      }
      result[index++] = (char) ('0' + exp % 10);
    } else {
      // Otherwise follow the Java spec for values in the interval [1E-3, 1E7).
      if (exp < 0) {
        // Decimal dot is before any of the digits.
        result[index++] = '0';
        result[index++] = '.';
        for (int i = -1; i > exp; i--) {
          result[index++] = '0';
        }
        int current = index;
        for (int i = 0; i < olength; i++) {
          result[current + olength - i - 1] = (char) ('0' + output % 10);
          output /= 10;
          index++;
        }
      } else if (exp + 1 >= olength) {
        // Decimal dot is after any of the digits.
        for (int i = 0; i < olength; i++) {
          result[index + olength - i - 1] = (char) ('0' + output % 10);
          output /= 10;
        }
        index += olength;
        for (int i = olength; i < exp + 1; i++) {
          result[index++] = '0';
        }
        result[index++] = '.';
        result[index++] = '0';
      } else {
        // Decimal dot is somewhere between the digits.
        int current = index + 1;
        for (int i = 0; i < olength; i++) {
          if (olength - i - 1 == exp) {
            result[current + olength - i - 1] = '.';
            current--;
          }
          result[current + olength - i - 1] = (char) ('0' + output % 10);
          output /= 10;
        }
        index += olength + 1;
      }
    }
    return index;
  }

  public static String decimal(double value) {
    return appendDecimal(new StringBuilder(), value).toString();
  }

  public static StringBuilder appendDecimal(StringBuilder builder, double value) {
    // Step 1: Decode the floating point number, and unify normalized and subnormal cases.
    // First, handle all the trivial cases.
    if (Double.isNaN(value)) {
      return builder.append("NaN");
    }
    if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY) {
      if (value == Double.NEGATIVE_INFINITY) {
        builder.append('-');
      }
      return builder.append("Infinity");
    }
    long bits = BitConversion.doubleToLongBits(value);
    if (bits == 0) {
      return builder.append("0.0");
    }
    if (bits == 0x8000000000000000L){
      return builder.append("-0.0");
    }

    // Otherwise extract the mantissa and exponent bits and run the full algorithm.
    int ieeeExponent = (int) ((bits >>> DOUBLE_MANTISSA_BITS) & DOUBLE_EXPONENT_MASK);
    long ieeeMantissa = bits & DOUBLE_MANTISSA_MASK;
    int e2;
    long m2;
    if (ieeeExponent == 0) {
      // Denormal number - no implicit leading 1, and the exponent is 1, not 0.
      e2 = 1 - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
      m2 = ieeeMantissa;
    } else {
      // Add implicit leading 1.
      e2 = ieeeExponent - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
      m2 = ieeeMantissa | (1L << DOUBLE_MANTISSA_BITS);
    }

    boolean sign = bits < 0;

    // Step 2: Determine the interval of legal decimal representations.
    boolean even = (m2 & 1) == 0;
    final long mv = 4 * m2;
    final long mp = 4 * m2 + 2;
    final int mmShift = ((m2 != (1L << DOUBLE_MANTISSA_BITS)) || (ieeeExponent == 1)) ? 1 : 0;
    final long mm = 4 * m2 - 1 - mmShift;
    e2 -= 2;

    // Step 3: Convert to a decimal power base using 128-bit arithmetic.
    // -1077 = 1 - 1023 - 53 - 2 <= e_2 - 2 <= 2046 - 1023 - 53 - 2 = 968
    long dv, dp, dm;
    final int e10;
    boolean dmIsTrailingZeros = false, dvIsTrailingZeros = false;
    if (e2 >= 0) {
      final int q = Math.max(0, ((e2 * 78913) >>> 18) - 1);
      // k = constant + floor(log_2(5^q))
      final int k = POW5_INV_BITCOUNT + pow5bits(q) - 1;
      final int i = -e2 + q + k;
      dv = mulPow5InvDivPow2(mv, q, i);
      dp = mulPow5InvDivPow2(mp, q, i);
      dm = mulPow5InvDivPow2(mm, q, i);
      e10 = q;

      if (q <= 21) {
        if (mv % 5 == 0) {
          dvIsTrailingZeros = multipleOfPowerOf5(mv, q);
        } else if (even) {
          dmIsTrailingZeros = multipleOfPowerOf5(mm, q);
        } else if (multipleOfPowerOf5(mp, q)) {
          dp--;
        }
      }
    } else {
      final int q = Math.max(0, ((-e2 * 732923) >>> 20) - 1);
      final int i = -e2 - q;
      final int k = pow5bits(i) - POW5_BITCOUNT;
      final int j = q - k;
      dv = mulPow5divPow2(mv, i, j);
      dp = mulPow5divPow2(mp, i, j);
      dm = mulPow5divPow2(mm, i, j);
      e10 = q + e2;
      if (q <= 1) {
        dvIsTrailingZeros = true;
        if (even) {
          dmIsTrailingZeros = mmShift == 1;
        } else {
          dp--;
        }
      } else if (q < 63) {
        dvIsTrailingZeros = (mv & ((1L << (q - 1)) - 1)) == 0;
      }
    }

    // Step 4: Find the shortest decimal representation in the interval of legal representations.
    //
    // We do some extra work here in order to follow Float/Double.toString semantics. In particular,
    // that requires printing in scientific format if and only if the exponent is between -3 and 7,
    // and it requires printing at least two decimal digits.
    //
    // Above, we moved the decimal dot all the way to the right, so now we need to count digits to
    // figure out the correct exponent for scientific notation.
    final int vplength = decimalLength(dp);
    int exp = e10 + vplength - 1;

    int removed = 0;

    int lastRemovedDigit = 0;
    long output;
    if (dmIsTrailingZeros || dvIsTrailingZeros) {
      while (dp / 10 > dm / 10) {
        dmIsTrailingZeros &= dm % 10 == 0;
        dvIsTrailingZeros &= lastRemovedDigit == 0;
        lastRemovedDigit = (int) (dv % 10);
        dp /= 10;
        dv /= 10;
        dm /= 10;
        removed++;
      }
      if (dmIsTrailingZeros) {
        while (dm % 10 == 0) {
          dvIsTrailingZeros &= lastRemovedDigit == 0;
          lastRemovedDigit = (int) (dv % 10);
          dp /= 10;
          dv /= 10;
          dm /= 10;
          removed++;
        }
      }
      if (dvIsTrailingZeros && (lastRemovedDigit == 5) && ((dv & 1) == 0)) {
        // Round even if the exact numbers is .....50..0.
        lastRemovedDigit = 4;
      }
      output = dv +
          (dv == dm && !dmIsTrailingZeros || lastRemovedDigit >= 5 ? 1 : 0);
    } else {
      while (dp / 10 > dm / 10) {
        lastRemovedDigit = (int) (dv % 10);
        dp /= 10;
        dv /= 10;
        dm /= 10;
        removed++;
      }
      output = dv + ((dv == dm || (lastRemovedDigit >= 5)) ? 1 : 0);
    }
    int olength = vplength - removed;

    // Step 5: Print the decimal representation.
    // We follow Double.toString semantics here.
    int index = builder.length();
    if (sign) {
      builder.append('-');
    }

    // Values in the interval [1E-3, 1E7) are special.
    // Otherwise, follow the Java spec for values in the interval [1E-3, 1E7).
    if (exp < 0) {
      // Decimal dot is before any of the digits.
      builder.append("0.");
      for (int i = -1; i > exp; i--) {
        builder.append('0');
      }
      int current = builder.length();
      for (int i = 0; i < olength; i++) {
        builder.insert(current, (char) ('0' + output % 10));
        output /= 10;
        index++;
      }
    } else if (exp + 1 >= olength) {
      index = builder.length();
      // Decimal dot is after any of the digits.
      for (int i = 0; i < olength; i++) {
        builder.insert(index, (char) ('0' + output % 10));
        output /= 10;
      }
      for (int i = olength; i < exp + 1; i++) {
        builder.append('0');
      }
      builder.append(".0");
    } else {
      // Decimal dot is somewhere between the digits.
      int current = builder.length();
      for (int i = 0; i < olength; i++) {
        if (olength - i - 1 == exp) {
          builder.insert(current, '.');
        }
        builder.insert(current, (char) ('0' + output % 10));
        output /= 10;
      }
    }
    return builder;
  }

  public static String scientific(double value) {
    final int index = scientific(value, result);
    return new String(result, 0, index);
  }

  public static StringBuilder appendScientific(StringBuilder builder, double value) {
    return appendScientific(builder, value, result);
  }

  public static StringBuilder appendScientific(StringBuilder builder, double value, char[] result) {
    final int index = scientific(value, result);
    return builder.append(result, 0, index);
  }

  public static int scientific(double value, char[] result) {
    // Step 1: Decode the floating point number, and unify normalized and subnormal cases.
    // First, handle all the trivial cases.
    if (Double.isNaN(value)) {
      result[0] = 'N';
      result[1] = 'a';
      result[2] = 'N';
      return 3;
    }
    if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY) {
      int idx = 0;
      if (value == Double.NEGATIVE_INFINITY) {
        result[idx++] = '-';
      }
      result[idx++] = 'I';
      result[idx++] = 'n';
      result[idx++] = 'f';
      result[idx++] = 'i';
      result[idx++] = 'n';
      result[idx++] = 'i';
      result[idx++] = 't';
      result[idx++] = 'y';
      return idx;
    }
    long bits = BitConversion.doubleToLongBits(value);
    if (bits == 0) {
      result[0] = '0';
      result[1] = '.';
      result[2] = '0';
      result[3] = 'E';
      result[4] = '0';
      return 5;
    }
    if (bits == 0x8000000000000000L){
      result[0] = '-';
      result[1] = '0';
      result[2] = '.';
      result[3] = '0';
      result[4] = 'E';
      result[5] = '0';
      return 6;
    }

    // Otherwise extract the mantissa and exponent bits and run the full algorithm.
    int ieeeExponent = (int) ((bits >>> DOUBLE_MANTISSA_BITS) & DOUBLE_EXPONENT_MASK);
    long ieeeMantissa = bits & DOUBLE_MANTISSA_MASK;
    int e2;
    long m2;
    if (ieeeExponent == 0) {
      // Denormal number - no implicit leading 1, and the exponent is 1, not 0.
      e2 = 1 - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
      m2 = ieeeMantissa;
    } else {
      // Add implicit leading 1.
      e2 = ieeeExponent - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
      m2 = ieeeMantissa | (1L << DOUBLE_MANTISSA_BITS);
    }

    boolean sign = bits < 0;

    // Step 2: Determine the interval of legal decimal representations.
    boolean even = (m2 & 1) == 0;
    final long mv = 4 * m2;
    final long mp = 4 * m2 + 2;
    final int mmShift = ((m2 != (1L << DOUBLE_MANTISSA_BITS)) || (ieeeExponent == 1)) ? 1 : 0;
    final long mm = 4 * m2 - 1 - mmShift;
    e2 -= 2;

    // Step 3: Convert to a decimal power base using 128-bit arithmetic.
    // -1077 = 1 - 1023 - 53 - 2 <= e_2 - 2 <= 2046 - 1023 - 53 - 2 = 968
    long dv, dp, dm;
    final int e10;
    boolean dmIsTrailingZeros = false, dvIsTrailingZeros = false;
    if (e2 >= 0) {
      final int q = Math.max(0, ((e2 * 78913) >>> 18) - 1);
      // k = constant + floor(log_2(5^q))
      final int k = POW5_INV_BITCOUNT + pow5bits(q) - 1;
      final int i = -e2 + q + k;
      dv = mulPow5InvDivPow2(mv, q, i);
      dp = mulPow5InvDivPow2(mp, q, i);
      dm = mulPow5InvDivPow2(mm, q, i);
      e10 = q;

      if (q <= 21) {
        if (mv % 5 == 0) {
          dvIsTrailingZeros = multipleOfPowerOf5(mv, q);
        } else if (even) {
          dmIsTrailingZeros = multipleOfPowerOf5(mm, q);
        } else if (multipleOfPowerOf5(mp, q)) {
          dp--;
        }
      }
    } else {
      final int q = Math.max(0, ((-e2 * 732923) >>> 20) - 1);
      final int i = -e2 - q;
      final int k = pow5bits(i) - POW5_BITCOUNT;
      final int j = q - k;
      dv = mulPow5divPow2(mv, i, j);
      dp = mulPow5divPow2(mp, i, j);
      dm = mulPow5divPow2(mm, i, j);
      e10 = q + e2;
      if (q <= 1) {
        dvIsTrailingZeros = true;
        if (even) {
          dmIsTrailingZeros = mmShift == 1;
        } else {
          dp--;
        }
      } else if (q < 63) {
        dvIsTrailingZeros = (mv & ((1L << (q - 1)) - 1)) == 0;
      }
    }

    // Step 4: Find the shortest decimal representation in the interval of legal representations.
    //
    // We do some extra work here in order to follow Float/Double.toString semantics. In particular,
    // that requires printing in scientific format if and only if the exponent is between -3 and 7,
    // and it requires printing at least two decimal digits.
    //
    // Above, we moved the decimal dot all the way to the right, so now we need to count digits to
    // figure out the correct exponent for scientific notation.
    final int vplength = decimalLength(dp);
    int exp = e10 + vplength - 1;

    int removed = 0;

    int lastRemovedDigit = 0;
    long output;
    if (dmIsTrailingZeros || dvIsTrailingZeros) {
      while (dp / 10 > dm / 10) {
        if (dp < 100) {
          // Double.toString semantics requires printing at least two digits.
          break;
        }
        dmIsTrailingZeros &= dm % 10 == 0;
        dvIsTrailingZeros &= lastRemovedDigit == 0;
        lastRemovedDigit = (int) (dv % 10);
        dp /= 10;
        dv /= 10;
        dm /= 10;
        removed++;
      }
      if (dmIsTrailingZeros) {
        while (dm % 10 == 0) {
          if (dp < 100) {
            // Double.toString semantics requires printing at least two digits.
            break;
          }
          dvIsTrailingZeros &= lastRemovedDigit == 0;
          lastRemovedDigit = (int) (dv % 10);
          dp /= 10;
          dv /= 10;
          dm /= 10;
          removed++;
        }
      }
      if (dvIsTrailingZeros && (lastRemovedDigit == 5) && ((dv & 1) == 0)) {
        // Round even if the exact numbers is .....50..0.
        lastRemovedDigit = 4;
      }
      output = dv +
          (dv == dm && !dmIsTrailingZeros || lastRemovedDigit >= 5 ? 1 : 0);
    } else {
      while (dp / 10 > dm / 10) {
        if (dp < 100) {
          // Double.toString semantics requires printing at least two digits.
          break;
        }
        lastRemovedDigit = (int) (dv % 10);
        dp /= 10;
        dv /= 10;
        dm /= 10;
        removed++;
      }
      output = dv + ((dv == dm || (lastRemovedDigit >= 5)) ? 1 : 0);
    }
    int olength = vplength - removed;

    // Step 5: Print the decimal representation.
    // We follow Double.toString semantics here.
    int index = 0;
    if (sign) {
      result[index++] = '-';
    }

    // Values in the interval [1E-3, 1E7) are special.
    // Print in the format x.xxxxxE-yy.
    for (int i = 0; i < olength - 1; i++) {
      int c = (int) (output % 10); output /= 10;
      result[index + olength - i] = (char) ('0' + c);
    }
    result[index] = (char) ('0' + output % 10);
    result[index + 1] = '.';
    index += olength + 1;
    if (olength == 1) {
      result[index++] = '0';
    }

    // Print 'E', the exponent sign, and the exponent, which has at most three digits.
    result[index++] = 'E';
    if (exp < 0) {
      result[index++] = '-';
      exp = -exp;
    }
    if (exp >= 100) {
      result[index++] = (char) ('0' + exp / 100);
      exp %= 100;
      result[index++] = (char) ('0' + exp / 10);
    } else if (exp >= 10) {
      result[index++] = (char) ('0' + exp / 10);
    }
    result[index++] = (char) ('0' + exp % 10);
    return index;
  }

  private static int pow5bits(int e) {
    return (BitConversion.imul(e, 1217359) >>> 19) + 1;
  }

  private static int decimalLength(long v) {
    if (v >= 1000000000000000000L) return 19;
    if (v >= 100000000000000000L) return 18;
    if (v >= 10000000000000000L) return 17;
    if (v >= 1000000000000000L) return 16;
    if (v >= 100000000000000L) return 15;
    if (v >= 10000000000000L) return 14;
    if (v >= 1000000000000L) return 13;
    if (v >= 100000000000L) return 12;
    if (v >= 10000000000L) return 11;
    if (v >= 1000000000L) return 10;
    if (v >= 100000000L) return 9;
    if (v >= 10000000L) return 8;
    if (v >= 1000000L) return 7;
    if (v >= 100000L) return 6;
    if (v >= 10000L) return 5;
    if (v >= 1000L) return 4;
    if (v >= 100L) return 3;
    if (v >= 10L) return 2;
    return 1;
  }

  private static boolean multipleOfPowerOf5(long value, int q) {
    return pow5Factor(value) >= q;
  }

  private static int pow5Factor(long value) {
    // We want to find the largest power of 5 that divides value.
    if ((value % 5) != 0) return 0;
    if ((value % 25) != 0) return 1;
    if ((value % 125) != 0) return 2;
    if ((value % 625) != 0) return 3;
    int count = 4;
    value /= 625;
    while (value > 0) {
      if (value % 5 != 0) {
        return count;
      }
      value /= 5;
      count++;
    }
    throw new IllegalArgumentException("" + value);
  }

  /**
   * Compute the high digits of m * 5^p / 10^q = m * 5^(p - q) / 2^q = m * 5^i / 2^j, with q chosen
   * such that m * 5^i / 2^j has sufficiently many decimal digits to represent the original floating
   * point number.
   */
  private static long mulPow5divPow2(long m, int i, int j) {
    // m has at most 55 bits.
    long mHigh = m >>> 31;
    long mLow = m & 0x7fffffff;
    long bits13 = mHigh * POW5_SPLIT[i][0]; // 124
    long bits03 = mLow * POW5_SPLIT[i][0];  // 93
    long bits12 = mHigh * POW5_SPLIT[i][1]; // 93
    long bits02 = mLow * POW5_SPLIT[i][1];  // 62
    long bits11 = mHigh * POW5_SPLIT[i][2]; // 62
    long bits01 = mLow * POW5_SPLIT[i][2];  // 31
    long bits10 = mHigh * POW5_SPLIT[i][3]; // 31
    long bits00 = mLow * POW5_SPLIT[i][3];  // 0
    int actualShift = j - 3 * 31 - 21;
    if (actualShift < 0) {
      throw new IllegalArgumentException("" + actualShift);
    }
    return ((((((
        ((bits00 >>> 31) + bits01 + bits10) >>> 31)
                         + bits02 + bits11) >>> 31)
                         + bits03 + bits12) >>> 21)
                         + (bits13 << 10)) >>> actualShift;
  }

  /**
   * Compute the high digits of m / 5^i / 2^j such that the result is accurate to at least 9
   * decimal digits. i and j are already chosen appropriately.
   */
  private static long mulPow5InvDivPow2(long m, int i, int j) {
    // m has at most 55 bits.
    long mHigh = m >>> 31;
    long mLow = m & 0x7fffffff;
    long bits13 = mHigh * POW5_INV_SPLIT[i][0];
    long bits03 = mLow * POW5_INV_SPLIT[i][0];
    long bits12 = mHigh * POW5_INV_SPLIT[i][1];
    long bits02 = mLow * POW5_INV_SPLIT[i][1];
    long bits11 = mHigh * POW5_INV_SPLIT[i][2];
    long bits01 = mLow * POW5_INV_SPLIT[i][2];
    long bits10 = mHigh * POW5_INV_SPLIT[i][3];
    long bits00 = mLow * POW5_INV_SPLIT[i][3];

    int actualShift = j - 3 * 31 - 21;
    if (actualShift < 0) {
      throw new IllegalArgumentException("" + actualShift);
    }
    return ((((((
        ((bits00 >>> 31) + bits01 + bits10) >>> 31)
                         + bits02 + bits11) >>> 31)
                         + bits03 + bits12) >>> 21)
                         + (bits13 << 10)) >>> actualShift;
  }
}
