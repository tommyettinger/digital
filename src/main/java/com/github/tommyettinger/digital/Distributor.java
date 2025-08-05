package com.github.tommyettinger.digital;

import java.util.Random;

import static com.github.tommyettinger.digital.RoughMath.logRough;

/**
 * Different methods for distributing input {@code long} or {@code double} values from a given domain into specific
 * distributions, such as the normal distribution. {@link #probit(double)} and {@link #probitHighPrecision(double)} take
 * a double in the 0.0 to 1.0 range (typically exclusive, but not required to be), and produce a normal-distributed
 * double centered on 0.0 with standard deviation 1.0, using an algorithm by Acklam. The suffixed
 * {@link #probitD(double)}, {@link #probitF(float)}, {@link #probitL(long)}, and {@link #probitI(int)} use a faster but
 * still fairly-high-quality approximation by Voutier, and are usually indistinguishable from the earlier
 * {@link #probit(double)}. The float and double versions also take inputs in the 0.0 to 1.0 range, but the int and long
 * versions can take any int or any long, with the lowest values mapping to the lowest results, highest to highest, near
 * 0 to near 0, etc. Using the suffixed probit() methods, such as {@link #probitF(float)}, is recommended when
 * generating normal-distributed floats or doubles.
 * <br>
 * All of these ways will preserve patterns in the input, so inputs close to the lowest
 * possible input (0.0 for probit(), {@link Long#MIN_VALUE} for normal(), {@link Integer#MIN_VALUE} for normalF()) will
 * produce the lowest possible output (-8.375 for probit(), -26.48372928592822 for probitD() and probitL(), or -9.082295
 * for probitF() and probitI()),
 * and similarly for the highest possible inputs producing the highest possible outputs.
 * <br>
 * There's also {@link #normal(long)} and {@link #normalF(int)}, which use the
 * <a href="https://en.wikipedia.org/wiki/Ziggurat_algorithm">Ziggurat method</a> and do not preserve input patterns.
 * The Ziggurat method does get closer to the correct normal distribution in the trail (where very positive
 * or very negative values are) relative to probit methods. Surprisingly, {@link #probitF(float)} is a little faster
 * than {@link #normalF(int)}, even considering that generating random floats for input is slower than generating random
 * ints. However, {@link #normal(long)} is somewhat faster than {@link #probitL(long)}.
 * <br>
 * This class also still contains some older methods, for compatibility with earlier versions of this library. The
 * "Linnormal" methods {@link #linearNormal(long)} and {@link #linearNormalF(int)} are like {@link #probitL(long)},
 * though they used an algorithm that approximated the normal distribution very poorly. They are currently aliases for
 * {@link #probitL(long)} and {@link #probitI(int)}.
 */
public final class Distributor {

    private Distributor() {}
    private static final int    ZIG_TABLE_ITEMS = 256;
    private static final double R               = 3.65415288536100716461;
    private static final double INV_R           = 1.0 / R;
    private static final double AREA            = 0.00492867323397465524494;
    private static final double[] ZIG_TABLE  = new double[257];

    private static final int   ZIG_TABLE_ITEMS_F = 128;
    private static final float R_F               = 3.4426198558966522559f;
    private static final float INV_R_F           = 1f / R_F;
    private static final float AREA_F            = 0.00991256303533646112916f;
    private static final float[] ZIG_TABLE_F  = new float[ZIG_TABLE_ITEMS_F+1];

    static {
        double f = Math.exp(-0.5 * R * R);
        ZIG_TABLE[0] = AREA / f;
        ZIG_TABLE[1] = R;
        for (int i = 2; i < ZIG_TABLE_ITEMS; i++) {
            double xx = Math.log(AREA /
                    ZIG_TABLE[i - 1] + f);
            ZIG_TABLE[i] = Math.sqrt(-2 * xx);
            f = Math.exp(xx);
        }
        ZIG_TABLE[ZIG_TABLE_ITEMS] = 0.0;

        float ff = (float) Math.exp(-0.5 * R_F * R_F);
        ZIG_TABLE_F[0] = AREA_F / ff;
        ZIG_TABLE_F[1] = R_F;
        for (int i = 2; i < ZIG_TABLE_ITEMS_F; i++) {
            float xx = (float)Math.log(AREA_F /
                    ZIG_TABLE_F[i - 1] + ff);
            ZIG_TABLE_F[i] = (float) Math.sqrt(-2f * xx);
            ff = (float) Math.exp(xx);
        }
        ZIG_TABLE_F[ZIG_TABLE_ITEMS_F] = 0f;
    }

    // constants used by probitI() and probitF()
    private static final float
            a0f = 0.195740115269792f,
            a1f = -0.652871358365296f,
            a2f = 1.246899760652504f,
            b0f = 0.155331081623168f,
            b1f = -0.839293158122257f,
            c3f = -1.000182518730158122f,
            c0f = 16.682320830719986527f,
            c1f = 4.120411523939115059f,
            c2f = 0.029814187308200211f,
            d0f = 7.173787663925508066f,
            d1f = 8.759693508958633869f;


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

    // Easier-to-read, slightly, version of probitL(long)
    // Meant to be copied with the above constants.
//    public static double probit(long l) {
//    /* 5.421010862427522E-20 is 0x1p-64 or Math.pow(2, -64) */
//    final double h = l * 5.421010862427522E-20;
//        if(-0.4535 > h) {
//        /* 0.5000000000000001 is the smallest representable double that is greater than 0.5. */
//        final double r = Math.sqrt(Math.log(0.5000000000000001 + h) * -2f);
//        return c3 * r + c2 + (c1 * r + c0) / (r * (r + d1) + d0);
//    } else if(0.4535 < h) {
//        /* 0.5000000000000001 is the smallest representable double that is greater than 0.5. */
//        final double r = Math.sqrt(Math.log(0.5000000000000001 - h) * -2f);
//        return -c3 * r - c2 - (c1 * r + c0) / (r * (r + d1) + d0);
//    } else {
//        final double r = h * h;
//        return h * (a2 + (a1 * r + a0) / (r * (r + b1) + b0));
//    }
//    }

    /**
     * A single-precision probit() approximation that takes a float between 0 and 1 inclusive and returns an
     * approximately-Gaussian-distributed float between -9.082295 and 9.082295 .
     * The function maps the lowest inputs to the most negative outputs, the highest inputs to the most
     * positive outputs, and inputs near 0.5 to outputs near 0.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param p should be between 0 and 1, inclusive.
     * @return an approximately-Gaussian-distributed float between -9.082295 and 9.082295
     */
    public static float probitF(float p) {
        if(0.0465f > p){
            final float r = (float)Math.sqrt(logRough(p) * -2f);
            return c3f * r + c2f + (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else if(0.9535f < p) {
            final float r = (float)Math.sqrt(logRough(1f - p) * -2f);
            return -c3f * r - c2f - (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else {
            final float q = p - 0.5f, r = q * q;
            return q * (a2f + (a1f * r + a0f) / (r * (r + b1f) + b0f));
        }
    }

    /**
     * A double-precision probit() approximation that takes a double between 0 and 1 inclusive and returns an
     * approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822 .
     * The function maps the lowest inputs to the most negative outputs, the highest inputs to the most
     * positive outputs, and inputs near 0.5 to outputs near 0.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param p should be between 0 and 1, inclusive.
     * @return an approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822
     */
    public static double probitD(double p) {
        if(0.0465 > p){
            /* 0.4.9E-324 is Double.MIN_VALUE, the smallest non-zero double */
            final double r = Math.sqrt(Math.log(p + 4.9E-324) * -2.0);
            return c3 * r + c2 + (c1 * r + c0) / (r * (r + d1) + d0);
        } else if(0.9535 < p) {
            /* 0.4.9E-324 is Double.MIN_VALUE, the smallest non-zero double */
            final double r = Math.sqrt(Math.log(1.0 - p + 4.9E-324) * -2.0);
            return -c3 * r - c2 - (c1 * r + c0) / (r * (r + d1) + d0);
        } else {
            final double q = p - 0.5, r = q * q;
            return q * (a2 + (a1 * r + a0) / (r * (r + b1) + b0));
        }
    }

    /**
     * A single-precision probit() approximation that takes any int and returns an
     * approximately-Gaussian-distributed float between -9.082295 and 9.082295 .
     * The function maps the most negative inputs to the most negative outputs, the most positive inputs to the most
     * positive outputs, and inputs near 0 to outputs near 0.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param i may be any int, though very close ints will not produce different results
     * @return an approximately-Gaussian-distributed float between -9.082295 and 9.082295
     */
    public static float probitI(int i) {
        /* 2.3283064E-10f is 0x1p-32f */
        final float h = 2.3283064E-10f * i;
        if(-0.4535f > h){
            final float r = (float)Math.sqrt(logRough(0.5f + h) * -2f);
            return c3f * r + c2f + (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else if(0.4535f < h) {
            final float r = (float)Math.sqrt(logRough(0.5f - h) * -2f);
            return -c3f * r - c2f - (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else {
            final float r = h * h;
            return h * (a2f + (a1f * r + a0f) / (r * (r + b1f) + b0f));
        }
    }

    /**
     * A double-precision probit() approximation that takes any long and returns an
     * approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822 .
     * The function maps the most negative inputs to the most negative outputs, the most positive inputs to the most
     * positive outputs, and inputs near 0 to outputs near 0.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param l may be any long, though very close longs will not produce different results
     * @return an approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822
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
     * A way of taking a double in the (0.0, 1.0) range and mapping it to a Gaussian or normal distribution, so high
     * inputs correspond to high outputs, and similarly for the low range. This is centered on 0.0 and its standard
     * deviation seems to be 1.0 (the same as {@link Random#nextGaussian()}). If this is given an input of 0.0
     * or less, it returns -8.375, which is slightly less than the result when given {@link Double#MIN_VALUE}. If it is
     * given an input of 1.0 or more, it returns 8.375, which is significantly larger than the result when given the
     * largest double less than 1.0 (this value is further from 1.0 than {@link Double#MIN_VALUE} is from 0.0). If
     * given {@link Double#NaN}, it returns whatever {@link Math#copySign(double, double)} returns for the arguments
     * {@code 8.375, Double.NaN}, which is implementation-dependent.
     * <br>
     * This uses an algorithm by Peter John Acklam, as implemented by Sherali Karimov.
     * <a href="https://web.archive.org/web/20150910002142/http://home.online.no/~pjacklam/notes/invnorm/impl/karimov/StatUtil.java">Original source</a>.
     * <a href="https://web.archive.org/web/20151030215612/http://home.online.no/~pjacklam/notes/invnorm/">Information on the algorithm</a>.
     * <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia's page on the probit function</a> may help, but
     * is more likely to just be confusing.
     * <br>
     * Acklam's algorithm and Karimov's implementation are both competitive on speed with the Box-Muller Transform and
     * Marsaglia's Polar Method, but slower than Ziggurat and the {@link #probitD(double)} method here. This isn't quite
     * as precise as Box-Muller or Marsaglia Polar, and can't produce as extreme min and max results in the extreme
     * cases they should appear. If given a typical uniform random {@code double} that's exclusive on 1.0, it won't
     * produce a result higher than
     * {@code 8.209536145151493}, and will only produce results of at least {@code -8.209536145151493} if 0.0 is
     * excluded from the inputs (if 0.0 is an input, the result is {@code -8.375}). This requires a fair amount of
     * floating-point multiplication and one division for all {@code d} where it is between 0 and 1 exclusive, but
     * roughly 1/20 of the time it need a {@link Math#sqrt(double)} and {@link Math#log(double)} as well.
     * <br>
     * This can be used both as an optimization for generating Gaussian random values, and as a way of generating
     * Gaussian values that match a pattern present in the inputs (which you could have by using a sub-random sequence
     * as the input, such as those produced by a van der Corput, Halton, Sobol or R2 sequence). Most methods of generating
     * Gaussian values (e.g. Box-Muller and Marsaglia polar) do not have any way to preserve a particular pattern. Note
     * that if you don't need to preserve patterns in input, then either the Ziggurat method (which is available via the
     * {@link #normal(long)} method) or the Marsaglia polar method (which is the default in the JDK Random class) will
     * perform better in each one's optimal circumstances. The {@link #probitD(double)} method here (using a faster
     * probit algorithm) preserves patterns in input (given the same range of double), and should usually be preferred
     * to this older method. The {@link #probitF(float)} method provides a float-to-float variant, and others take int
     * or long arguments and return floats or doubles. You could also use {@link #linearNormal(long)} to take long
     * arguments and return doubles, but it loses a lot of accuracy.
     *
     * @param d should be between 0 and 1, exclusive, but other values are tolerated
     * @return a normal-distributed double centered on 0.0; all results will be between -8.375 and 8.375, both inclusive
     * @see #probitHighPrecision(double) There is a higher-precision, slower variant on this method.
     * @see #probitD(double) There is a more-recent approximation that should be faster and more precise than this one.
     */
    public static double probit (final double d) {
        if (d <= 0 || d >= 1) {
            return Math.copySign(8.375, d - 0.5);
        } else if (d < 0.02425) {
            final double q = Math.sqrt(-2.0 * Math.log(d));
            return (((((-7.784894002430293e-03 * q - 3.223964580411365e-01) * q - 2.400758277161838e+00) * q - 2.549732539343734e+00) * q + 4.374664141464968e+00) * q + 2.938163982698783e+00) / (
                    (((7.784695709041462e-03 * q + 3.224671290700398e-01) * q + 2.445134137142996e+00) * q + 3.754408661907416e+00) * q + 1.0);
        } else if (0.97575 < d) {
            final double q = Math.sqrt(-2.0 * Math.log(1 - d));
            return -(((((-7.784894002430293e-03 * q - 3.223964580411365e-01) * q - 2.400758277161838e+00) * q - 2.549732539343734e+00) * q + 4.374664141464968e+00) * q + 2.938163982698783e+00) / (
                    (((7.784695709041462e-03 * q + 3.224671290700398e-01) * q + 2.445134137142996e+00) * q + 3.754408661907416e+00) * q + 1.0);
        }
        final double q = d - 0.5;
        final double r = q * q;
        return (((((-3.969683028665376e+01 * r + 2.209460984245205e+02) * r - 2.759285104469687e+02) * r + 1.383577518672690e+02) * r - 3.066479806614716e+01) * r + 2.506628277459239e+00) * q / (
                ((((-5.447609879822406e+01 * r + 1.615858368580409e+02) * r - 1.556989798598866e+02) * r + 6.680131188771972e+01) * r - 1.328068155288572e+01) * r + 1.0);
    }

    /**
     * Complementary error function, partial implementation.
     * <a href="https://en.wikipedia.org/wiki/Error_function#Complementary_error_function">See Wikipedia for more</a>.
     * @param x any non-negative double
     * @return a double between 0 and 1... I think?
     */
    private static double erfcBase(double x) {
        return ((0.56418958354775629) / (x + 2.06955023132914151)) *
                ((x * (x + 2.71078540045147805) + 5.80755613130301624) / (x * (x + 3.47954057099518960) + 12.06166887286239555)) *
                ((x * (x + 3.47469513777439592) + 12.07402036406381411) / (x * (x + 3.72068443960225092) + 8.44319781003968454)) *
                ((x * (x + 4.00561509202259545) + 9.30596659485887898) / (x * (x + 3.90225704029924078) + 6.36161630953880464)) *
                ((x * (x + 5.16722705817812584) + 9.12661617673673262) / (x * (x + 4.03296893109262491) + 5.13578530585681539)) *
                ((x * (x + 5.95908795446633271) + 9.19435612886969243) / (x * (x + 4.11240942957450885) + 4.48640329523408675)) *
                Math.exp(-x * x);
    }

    /**
     * The complementary error function, equivalent to {@code 1.0 - erf(x)}.
     * This uses a different approximation that should have extremely low error (below {@code 0x1p-53}, or
     * {@code 1.1102230246251565E-16}).
     * <a href="https://en.wikipedia.org/wiki/Error_function#Complementary_error_function">See Wikipedia for more</a>.
     *
     * @param x any finite double
     * @return a double between 0 and 2, inclusive
     */
    private static double erfc(double x) {
        return x >= 0 ? erfcBase(x) : 2.0 - erfcBase(-x);
    }

    /**
     * This is the same as {@link #probit(double)},
     * except that it performs an additional step of post-processing to
     * bring the result even closer to the normal distribution.
     * It also produces normal-distributed doubles (with standard deviation 1.0)
     * given inputs between 0.0 and 1.0, exclusive.
     *
     * @param d should be between {@link Double#MIN_NORMAL}, inclusive, and 1, exclusive; subnormal values may return NaN
     * @return a normal-distributed double centered on 0.0
     * @see #probit(double) There is a lower-precision, faster variant on this method, which this uses internally.
     * @see #probitD(double) A more-recently-published approximation that should be faster, and may have comparable quality
     */
    public static double probitHighPrecision(double d)
    {
        double x = probit(d);
        if( d > 0.0 && d < 1.0 && d != 0.5) {
            double e = 0.5 * erfc(x * -0.7071067811865475) - d; /* -0.7071067811865475 == -1.0 / Math.sqrt(2.0) */
            double u = e * 2.5066282746310002 * Math.exp(0.5 * x * x); /* 2.5066282746310002 == Math.sqrt(2*Math.PI) */
            x = x - u / (1.0 + 0.5 * x * u);
        }
        return x;
    }

    /**
     * This is an alias for {@link #probitL(long)}; see its docs for details.
     * @param n may be any long, though very close longs will not produce different results
     * @return an approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822
     */
    public static double linearNormal(long n) {
        return probitL(n);
    }
    /**
     * This is an alias for {@link #probitI(int)}; see its docs for details.
     * @param n may be any int, though very close ints will not produce different results
     * @return an approximately-Gaussian-distributed float between -9.082295 and 9.082295
     */
    public static float linearNormalF(int n) {
        return probitI(n);
    }

    /**
     * Given a long where all bits are sufficiently (independently) random, this produces a normal-distributed
     * (Gaussian) variable as if by a normal distribution with mean (mu) 0.0 and standard deviation (sigma) 1.0.
     * This uses the Ziggurat algorithm, and takes one {@code long} input to produce one {@code double} value.
     * Note that no additive counters are considered sufficiently random for this, and linear congruential generators
     * might not be random enough either if they return the low-order bits without changes.
     * Patterns between different {@code state} values provided to this will generally not be preserved in the
     * output, but this may not be true all the time for patterns on all bits.
     * <br>
     * The range this can produce is at least from -7.6719775673883905 to 7.183851151080583, and is almost certainly larger
     * (only 4 billion distinct inputs were tested, and there are over 18 quintillion inputs possible).
     * <br>
     * From <a href="https://github.com/camel-cdr/cauldron/blob/7d5328441b1a1bc8143f627aebafe58b29531cb9/cauldron/random.h#L2013-L2265">Cauldron</a>,
     * MIT-licensed. This in turn is based on Doornik's form of the Ziggurat method:
     * <br>
     *      Doornik, Jurgen A (2005):
     *      "An improved ziggurat method to generate normal random samples."
     *      University of Oxford: 77.
     *
     * @param state a long that should be sufficiently random; quasi-random longs may not be enough
     * @return a normal-distributed double with mean (mu) 0.0 and standard deviation (sigma) 1.0
     */
    public static double normal(long state) {
        double x, y, f0, f1, u;
        int idx;

        while (true) {
            /* To minimize calls to the RNG, we use every bit for its own
             * purposes:
             *    - The 53 most significant bits are used to generate
             *      a random floating-point number in the range [0.0,1.0).
             *    - The first to the eighth least significant bits are used
             *      to generate an index in the range [0,256).
             *    - The ninth least significant bit is treated as the sign
             *      bit of the result, unless the result is in the trail.
             *    - If the random variable is in the trail, the state will
             *      be modified instead of generating a new random number.
             *      This could yield lower quality, but variables in the
             *      trail are already rare (1/256 values or fewer).
             *    - If the result is in the trail, the parity of the
             *      complete state is used to randomly set the sign of the
             *      return value.
             */
            idx = (int)(state & (ZIG_TABLE_ITEMS - 1));
            /* 1.11022302462516E-16 is 0x1p-53 */
            u = (state >>> 11) * 1.11022302462516E-16 * ZIG_TABLE[idx];

            /* Take a random box from TABLE
             * and get the value of a random x-coordinate inside it.
             * If it's also inside TABLE[idx + 1] we already know to accept
             * this value. */
            if (u < ZIG_TABLE[idx + 1])
                break;

            /* If our random box is at the bottom, we can't use the lookup
             * table and need to generate a variable for the trail of the
             * normal distribution, as described by Marsaglia in 1964: */
            if (idx == 0) {
                /* If idx is 0, then the bottom 8 bits of state must all be 0,
                 * and u must be on the larger side. */
                do {
                    /* 1.11022302462516E-16 is 0x1p-53 */
                    x = Math.log((((state = (state ^ 0xF1357AEA2E62A9C5L) * 0xABC98388FB8FAC03L) >>> 11) + 1L) * 1.11022302462516E-16) * INV_R;
                    y = Math.log((((state = (state ^ 0xF1357AEA2E62A9C5L) * 0xABC98388FB8FAC03L) >>> 11) + 1L) * 1.11022302462516E-16);
                } while (-(y + y) < x * x);
                return (Long.bitCount(state) & 1L) == 0L ?
                        x - R :
                        R - x;
            }

            /* Take a random x-coordinate u in between TABLE[idx] and TABLE[idx+1]
             * and return x if u is inside the normal distribution,
             * otherwise, repeat the entire ziggurat method. */
            y = u * u;
            f0 = Math.exp(-0.5 * (ZIG_TABLE[idx]     * ZIG_TABLE[idx]     - y));
            f1 = Math.exp(-0.5 * (ZIG_TABLE[idx + 1] * ZIG_TABLE[idx + 1] - y));
            /* 1.11022302462516E-16 is 0x1p-53 */
            if (f1 + (((state = (state ^ 0xF1357AEA2E62A9C5L) * 0xABC98388FB8FAC03L) >>> 11) * 1.11022302462516E-16) * (f0 - f1) < 1.0)
                break;
        }
        /* (Zero-indexed ) bits 8, 9, and 10 aren't used in the calculations for idx
         * or u, so we use bit 9 as a sign bit here. */
        return Math.copySign(u, 256L - (state & 512L));
    }

    /**
     * Given an int where all bits are sufficiently (independently) random, this produces a normal-distributed float
     * (Gaussian) variable as if by a normal distribution with mean (mu) 0.0 and standard deviation (sigma) 1.0.
     * This uses the Ziggurat algorithm, and takes one {@code int} input to produce one {@code float} value.
     * Note that no additive counters are considered sufficiently random for this, and linear congruential generators
     * might not be random enough either if they return the low-order bits without changes.
     * Patterns between different {@code state} values provided to this will generally not be preserved in the
     * output, but this may not be true all the time for patterns on all bits.
     * <br>
     * The range this can produce is from -6.127281f to 6.158781f, both inclusive. This was tested exhaustively.
     * <br>
     * From <a href="https://github.com/camel-cdr/cauldron/blob/7d5328441b1a1bc8143f627aebafe58b29531cb9/cauldron/random.h#L2013-L2265">Cauldron</a>,
     * MIT-licensed. This in turn is based on Doornik's form of the Ziggurat method:
     * <br>
     *      Doornik, Jurgen A (2005):
     *      "An improved ziggurat method to generate normal random samples."
     *      University of Oxford: 77.
     *
     * @param state an int that should be sufficiently random; quasi-random longs may not be enough
     * @return a normal-distributed float with mean (mu) 0.0 and standard deviation (sigma) 1.0
     */
    public static float normalF(int state) {
        float x, y, f0, f1, u;
        int idx;

        while (true) {
            /* To minimize calls to the RNG, we use every bit for its own
             * purposes:
             *    - The 24 most significant bits are used to generate
             *      a random floating-point number in the range [0.0,1.0).
             *    - The first to the seventh least significant bits are used
             *      to generate an index in the range [0,128).
             *    - The eighth least significant bit is treated as the sign
             *      bit of the result, unless the result is in the trail.
             *    - If the random variable is in the trail, the state will
             *      be modified instead of generating a new random number.
             *      This could yield lower quality, but variables in the
             *      trail are already rare (1/128 values or fewer).
             *    - If the result is in the trail, the parity of the
             *      complete state is used to randomly set the sign of the
             *      return value.
             */
            idx = (state & (ZIG_TABLE_ITEMS_F - 1));
            u = (state >>> 8) * 0x1p-24f * ZIG_TABLE_F[idx];

            /* Take a random box from TABLE
             * and get the value of a random x-coordinate inside it.
             * If it's also inside TABLE[idx + 1] we already know to accept
             * this value. */
            if (u < ZIG_TABLE_F[idx + 1])
                break;

            /* If our random box is at the bottom, we can't use the lookup
             * table and need to generate a variable for the trail of the
             * normal distribution, as described by Marsaglia in 1964: */
            if (idx == 0) {
                /* If idx is 0, then the bottom 7 bits of state must all be 0,
                 * and u must be on the larger side. */
                do {
                    x = (float) Math.log((((state = BitConversion.imul(state ^ state >>> 8 ^ 0xFE62A9C5, 0xABC98383)) >>> 8) + 1) * 0x1p-24f) * INV_R_F;
                    y = (float) Math.log((((state = BitConversion.imul(state ^ state >>> 8 ^ 0xFE62A9C5, 0xABC98383)) >>> 8) + 1) * 0x1p-24f);
                } while (-(y + y) < x * x);
                return (Integer.bitCount(state) & 1) == 0 ?
                        x - R_F :
                        R_F - x;
            }

            /* Take a random x-coordinate u in between TABLE[idx] and TABLE[idx+1]
             * and return x if u is inside the normal distribution,
             * otherwise, repeat the entire ziggurat method. */
            y = u * u;
            f0 = (float) Math.exp(-0.5f * (ZIG_TABLE_F[idx]     * ZIG_TABLE_F[idx]     - y));
            f1 = (float) Math.exp(-0.5f * (ZIG_TABLE_F[idx + 1] * ZIG_TABLE_F[idx + 1] - y));
            if (f1 + (((state = BitConversion.imul(state ^ state >>> 8 ^ 0xFE62A9C5, 0xABC98383)) >>> 8) * 0x1p-24f) * (f0 - f1) < 1f)
                break;
        }
        /* (Zero-indexed) bit 8 isn't used in the calculations for idx
         * or u, so we use bit 8 as a sign bit here. */
        return Math.copySign(u, 128 - (state & 256));
    }
}
