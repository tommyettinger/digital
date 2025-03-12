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
 */

package com.github.tommyettinger.digital;

import java.util.LinkedHashMap;
import java.util.function.DoubleUnaryOperator;

import static com.github.tommyettinger.digital.MathTools.barronSpline;
import static com.github.tommyettinger.digital.MathTools.fract;
import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * Provides predefined {@link Interpolator} constants and ways to generate {@link InterpolationFunction} instances, as
 * well as acting as the registry for known Interpolator values so that they can be looked up by name.
 * <a href="https://tommyettinger.github.io/digital/interpolators.html">You can view the graphs for every Interpolator here</a>.
 */
public final class Interpolations {
    /**
     * No need to instantiate.
     */
    private Interpolations() {
    }

    private static final LinkedHashMap<String, Interpolator> REGISTRY = new LinkedHashMap<>(128);

    /**
     * Looks up the given {@code tag} in a registry of Interpolators, and if there exists one with that name, returns
     * it. Otherwise, this returns null.
     * @param tag a tag used to register an Interpolator here
     * @return the Interpolator registered with the given tag, or null if none exists for that tag
     */
    public static Interpolator get(String tag) {
        return REGISTRY.get(tag);
    }

    /**
     * Allocates a new String array, fills it with every tag registered for an Interpolator, and returns that array.
     * @return an array containing every String tag registered for an Interpolator
     */
    public static String[] getTagArray() {
        return REGISTRY.keySet().toArray(new String[0]);
    }

    /**
     * Allocates a new Interpolator array, fills it with every registered Interpolator, and returns that array.
     * @return an array containing every Interpolator registered
     */
    public static Interpolator[] getInterpolatorArray() {
        return REGISTRY.values().toArray(new Interpolator[0]);
    }

    /**
     * A type of function that takes a float from 0 to 1 (usually, and also usually inclusive) and returns a float that
     * is typically in the 0 to 1 range or just outside it. Meant for easier communication with libGDX's Interpolation
     * class by using one of its Interpolation constants with a method reference. This is a functional interface whose
     * functional method is {@link #apply(float)}.
     */
    public interface InterpolationFunction {
        /**
         * Given a float {@code alpha}, which is almost always between 0 and 1 inclusive, gets a typically-different
         * float that is usually (but not always) between 0 and 1 inclusive.
         * @param alpha almost always between 0 and 1, inclusive
         * @return a value that is usually between 0 and 1, inclusive, for inputs between 0 and 1
         */
        float apply(float alpha);

        /**
         * Maps a call to {@link #apply(float)} from the 0-1 range to the {@code start}-{@code end} range.
         * Usually (but not always), this returns a float between start and end, inclusive.
         * @param start the inclusive lower bound; some functions can return less
         * @param end the inclusive upper bound; some functions can return more
         * @param alpha almost always between 0 and 1, inclusive
         * @return a value that is usually between start and end, inclusive, for alpha between 0 and 1
         */
        default float apply(float start, float end, float alpha) {
            return start + apply(alpha) * (end - start);
        }

        /**
         * Effectively splits the interpolation function at its midway point (where alpha is 0.5) and returns a new
         * InterpolationFunction that interpolates like the first half when alpha is greater than 0.5, and interpolates
         * like the second half when alpha is less than 0.5. In both cases, the returned function will be offset so that
         * it starts at 0 when alpha is 0, ends at 1 when alpha is 1, and returns 0.5 when alpha is 0.5, but only so
         * long as this original InterpolationFunction also has those behaviors. If this InterpolationFunction does not
         * return 0 at 0, 1 at 1, and 0.5 at 0.5, then the InterpolationFunction this returns may not be continuous.
         * <br>
         * This is meant to create variants on "In, Out" interpolation functions that instead go "Out, In."
         *
         * @return a new InterpolationFunction that acts like this one, but with its starting and ending halves switched
         */
        default InterpolationFunction flip() {
            return a -> apply(fract(a+0.5f)) + Math.copySign(0.5f, a - 0.5f);
        }
    }

    /**
     * A simple wrapper around an {@link InterpolationFunction} so it is associated with a String {@link #tag}. This
     * also implements InterpolationFunction, and wraps the {@link #fn} it stores to clamp its input to the 0 to 1
     * range (preventing potentially troublesome complications when large or negative inputs come in).
     */
    public static class Interpolator implements InterpolationFunction {
        /**
         * A unique String that identifies this object.
         * @see #getTag()
         */
        public final String tag;
        /**
         * The InterpolationFunction this actually uses to do its math work.
         * @see #getFn()
         */
        public final InterpolationFunction fn;

        /**
         * Calls {@link #Interpolator(String, InterpolationFunction)} with {@code "linear"} and {@link #linearFunction}.
         * Because {@link #linear} is already registered with that tag and function, this isn't very useful.
         */
        public Interpolator() {
            this("linear", linearFunction);
        }

        /**
         * Creates an Interpolator that will use the given {@code fn} and registers it with the given tag. The tag must
         * be unique; if {@link Interpolations#get(String)} returns a non-null value when looking up the given tag, then
         * if you create an Interpolator with that tag, the existing value will be overwritten.
         * @param tag a unique String that can be used as a key to access this with {@link Interpolations#get(String)}
         * @param fn an {@link InterpolationFunction} to wrap
         */
        public Interpolator(String tag, InterpolationFunction fn) {
            this.tag = tag;
            this.fn = fn;
            REGISTRY.put(tag, this);
        }

        /**
         * Does bounds-checking on the input before passing it to {@link #fn}. If alpha is less than 0, it is treated as
         * 0; if alpha is greater than 1, it is treated as 1. Note that the output is still unrestricted, so
         * InterpolationFunctions that can produce results outside the 0-1 range still can do that.
         * @param alpha almost always between 0 and 1, inclusive, and will be clamped to ensure that
         * @return an interpolated value based on alpha, which may (for some functions) be negative, or greater than 1
         */
        @Override
        public float apply(float alpha) {
            return fn.apply(Math.min(Math.max(alpha, 0f), 1f));
        }

        /**
         * Gets the tag for this Interpolator, which is a unique String that identifies this object. If another
         * Interpolator tries to use the same tag, this Interpolator will be un-registered and will no longer be
         * returnable from {@link #get(String)}.
         * @return the tag String
         */
        public String getTag() {
            return tag;
        }

        /**
         * Gets the InterpolationFunction this actually uses to do its math work. Calling this function on its own does
         * not behave the same way as calling {@link Interpolator#apply(float)} on this Interpolator; the Interpolator
         * method clamps the result if the {@code alpha} parameter is below 0 or above 1.
         * @return the InterpolationFunction this uses
         */
        public InterpolationFunction getFn() {
            return fn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Interpolator that = (Interpolator) o;

            return tag.equals(that.tag);
        }

        @Override
        public int hashCode() {
            return tag.hashCode();
        }

        @Override
        public String toString() {
            return tag;
        }
    }

    /**
     * Linear interpolation; just returns its argument.
     */
    public static final InterpolationFunction linearFunction = (a -> a);

    /**
     * Plain linear interpolation, or "lerp"; this just returns the alpha it is given.
     */
    public static final Interpolator linear = new Interpolator("linear", linearFunction);
    /**
     * "Smoothstep" or a cubic Hermite spline.
     * <br>
     * This has been modified slightly for numerical correctness. The form of this spline usually given,
     * {code a * a * (3 - 2 * a)}, can exceed 1 for many inputs that are just less than 1, which makes that form much
     * harder to use in a table lookup because an output larger than 1 could mean an out-of-bounds index. Instead, we
     * use the form {@code a * a * (1 - a - a + 2)}, which always stays in the range 0.0f to 1.0f, inclusive.
     */
    public static final Interpolator smooth = new Interpolator("smooth", a -> a * a * (1 - a - a + 2));
    /**
     * "Smoothstep" or a cubic Hermite spline, but flipped.
     * <br>
     * This has been modified slightly for numerical correctness. The form of this spline usually given,
     * {code a * a * (3 - 2 * a)}, can exceed 1 for many inputs that are just less than 1, which makes that form much
     * harder to use in a table lookup because an output larger than 1 could mean an out-of-bounds index. Instead, we
     * use the form {@code a * a * (1 - a - a + 2)}, which always stays in the range 0.0f to 1.0f, inclusive.
     */
    public static final Interpolator smoothOutIn = new Interpolator("smoothOutIn", smooth.fn.flip());
    /**
     * "Smoothstep" or a cubic Hermite spline, applied twice.
     * <br>
     * This has been modified slightly for numerical correctness. The form of the cubic Hermite spline usually given,
     * {code a * a * (3 - 2 * a)}, can exceed 1 for many inputs that are just less than 1, which makes that form much
     * harder to use in a table lookup because an output larger than 1 could mean an out-of-bounds index. Instead, we
     * use the form {@code a * a * (1 - a - a + 2)}, which always stays in the range 0.0f to 1.0f, inclusive.
     */
    public static final Interpolator smooth2 = new Interpolator("smooth2", a -> (a *= a * (1 - a - a + 2)) * a * (1 - a - a + 2));
    /**
     * "Smoothstep" or a cubic Hermite spline, applied twice, but flipped.
     * <br>
     * This has been modified slightly for numerical correctness. The form of the cubic Hermite spline usually given,
     * {code a * a * (3 - 2 * a)}, can exceed 1 for many inputs that are just less than 1, which makes that form much
     * harder to use in a table lookup because an output larger than 1 could mean an out-of-bounds index. Instead, we
     * use the form {@code a * a * (1 - a - a + 2)}, which always stays in the range 0.0f to 1.0f, inclusive.
     */
    public static final Interpolator smooth2OutIn = new Interpolator("smooth2OutIn", smooth2.fn.flip());
    /**
     * A quintic Hermite spline by Ken Perlin.
     * <br>
     * This was modified slightly because the original constants were meant for doubles, and here we use floats. Without
     * this tiny change (the smallest possible change here, from 10.0f to 9.999998f), giving an input of 0.99999994f, or
     * one of thousands of other inputs, would unexpectedly produce an output greater than 1.0f .
     */
    public static final Interpolator smoother = new Interpolator("smoother", a -> a * a * a * (a * (a * 6f - 15f) + 9.999998f));
    /**
     * A quintic Hermite spline by Ken Perlin, but flipped.
     * <br>
     * This was modified; see {@link #smoother}.
     */
    public static final Interpolator smootherOutIn = new Interpolator("smootherOutIn", smoother.fn.flip());
    /**
     * A quintic Hermite spline by Ken Perlin; this uses the same function as {@link #smoother}.
     * <br>
     * This was modified slightly because the original constants were meant for doubles, and here we use floats. Without
     * this tiny change (the smallest possible change here, from 10.0f to 9.999998f), giving an input of 0.99999994f, or
     * one of thousands of other inputs, would unexpectedly produce an output greater than 1.0f .
     */
    public static final Interpolator fade = new Interpolator("fade", smoother.fn);
    /**
     * A quintic Hermite spline by Ken Perlin, but flipped; this uses the same function as {@link #smootherOutIn}.
     * <br>
     * This was modified; see {@link #fade}.
     */
    public static final Interpolator fadeOutIn = new Interpolator("fadeOutIn", smootherOutIn.fn);
    /**
     * Produces an InterpolationFunction that uses the given power variable.
     * When power is greater than 1, this starts slowly, speeds up in the middle and slows down at the end. The
     * rate of acceleration and deceleration changes based on the parameter. Non-integer parameters are supported,
     * unlike the Pow in libGDX. Negative powers are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction powFunction(final float power) {
        return a -> {
            if (a <= 0.5f) return (float) Math.pow(a + a, power) * 0.5f;
            return (float) Math.pow(2f - a - a, power) * -0.5f + 1f;
        };
    }
    /**
     * Produces an InterpolationFunction that uses the given power variable.
     * When power is greater than 1, this starts quickly, slows down in the middle and speeds up at the end. The
     * rate of acceleration and deceleration changes based on the parameter. Non-integer parameters are supported,
     * unlike the Pow in libGDX. Negative powers are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction powOutInFunction(final float power) {
        return a -> {
            if (a > 0.5f) return (float) Math.pow(a + a - 1f, power) * 0.5f + 0.5f;
            return (float) Math.pow(1f - a - a, power) * -0.5f + 0.5f;
        };
    }

    /**
     * Produces an InterpolationFunction that uses the given power variable.
     * When power is greater than 1, this starts slowly and speeds up toward the end. The
     * rate of acceleration changes based on the parameter. Non-integer parameters are supported,
     * unlike the PowIn in libGDX. Negative powers are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction powInFunction(final float power) {
        return a -> (float) Math.pow(a, power);
    }
    /**
     * Produces an InterpolationFunction that uses the given power variable.
     * When power is greater than 1, this starts quickly and slows down toward the end. The
     * rate of deceleration changes based on the parameter. Non-integer parameters are supported,
     * unlike the PowOut in libGDX. Negative powers are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction powOutFunction(final float power) {
        return a -> 1f - (float) Math.pow(1f - a, power);
    }

    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 2.
     */
    public static final Interpolator pow2 = new Interpolator("pow2", powFunction(2f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 3.
     */
    public static final Interpolator pow3 = new Interpolator("pow3", powFunction(3f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 4.
     */
    public static final Interpolator pow4 = new Interpolator("pow4", powFunction(4f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 5.
     */
    public static final Interpolator pow5 = new Interpolator("pow5", powFunction(5f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 0.75.
     */
    public static final Interpolator pow0_75 = new Interpolator("pow0_75", powFunction(0.75f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 0.5. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow0_5 = new Interpolator("pow0_5", a -> {
        if (a <= 0.5f) return (float) Math.sqrt(a + a) * 0.5f;
        return (float) Math.sqrt(2f - a - a) * -0.5f + 1f;
    });
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 0.25.
     */
    public static final Interpolator pow0_25 = new Interpolator("pow0_25", powFunction(0.25f));

//    // This might make sense to PR to libGDX, because it should avoid a modulus and conditional.
//    public static float oPow(float a, int power){
//        if (a <= 0.5f) return (float) Math.pow(a * 2, power) * 0.5f;
//        return (float) Math.pow((a - 1) * 2, power) * ((power & 1) - 0.5f) + 1;
//    }
//
//    // This might make more sense as a replacement, since it allows non-integer powers.
//    public static float aPow(float a, float power){
//        if (a <= 0.5f) return (float) Math.pow(a + a, power) * 0.5f;
//        return (float) Math.pow(2 - a - a, power) * -0.5f + 1;
//    }

    /**
     * Accelerates using {@link #powInFunction(float)} and power of 2.
     */
    public static final Interpolator pow2In = new Interpolator("pow2In", powInFunction(2f));
    /**
     * Slow, then fast. This uses the same function as {@link #pow2In}.
     */
    public static final Interpolator slowFast = new Interpolator("slowFast", pow2In.fn);
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 3.
     */
    public static final Interpolator pow3In = new Interpolator("pow3In", powInFunction(3f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 4.
     */
    public static final Interpolator pow4In = new Interpolator("pow4In", powInFunction(4f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 5.
     */
    public static final Interpolator pow5In = new Interpolator("pow5In", powInFunction(5f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 0.75.
     */
    public static final Interpolator pow0_75In = new Interpolator("pow0_75In", powInFunction(0.75f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 0.5. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow0_5In = new Interpolator("pow0_5In", a -> (float) Math.sqrt(a));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 0.25.
     */
    public static final Interpolator pow0_25In = new Interpolator("pow0_25In", powInFunction(0.25f));
    /**
     * An alias for {@link #pow0_5In}, this is the inverse for {@link #pow2In}. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow2InInverse = new Interpolator("pow2InInverse", a -> (float) Math.sqrt(a));
    /**
     * This is the inverse for {@link #pow3In}. Its function is simply a method reference to
     * {@link MathTools#cbrt(float)}.
     */
    public static final Interpolator pow3InInverse = new Interpolator("pow3InInverse", MathTools::cbrt);

    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 2.
     */
    public static final Interpolator pow2Out = new Interpolator("pow2Out", powOutFunction(2f));
    /**
     * Fast, then slow. This uses the same function as {@link #pow2Out}.
     */
    public static final Interpolator fastSlow = new Interpolator("fastSlow", pow2Out.fn);
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 3.
     */
    public static final Interpolator pow3Out = new Interpolator("pow3Out", powOutFunction(3f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 4.
     */
    public static final Interpolator pow4Out = new Interpolator("pow4Out", powOutFunction(4f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 5.
     */
    public static final Interpolator pow5Out = new Interpolator("pow5Out", powOutFunction(5f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 0.75.
     */
    public static final Interpolator pow0_75Out = new Interpolator("pow0_75Out", powOutFunction(0.75f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 0.5. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow0_5Out = new Interpolator("pow0_5Out", a -> 1f - (float) Math.sqrt(1f - a));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 0.25.
     */
    public static final Interpolator pow0_25Out = new Interpolator("pow0_25Out", powOutFunction(0.25f));
    /**
     * An alias for {@link #pow0_5Out}, this is the inverse of {@link #pow2Out}. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow2OutInverse = new Interpolator("pow2OutInverse", a -> 1f - (float) Math.sqrt(1f - a));
    /**
     * This is the inverse for {@link #pow3Out}. Optimized with {@link MathTools#cbrt(float)}.
     */
    public static final Interpolator pow3OutInverse = new Interpolator("pow3OutInverse", a -> 1f - MathTools.cbrt(1f - a));

    /**
     * Accelerates/decelerates using {@link #powOutInFunction(float)} and power of 2.
     */
    public static final Interpolator pow2OutIn = new Interpolator("pow2OutIn", powOutInFunction(2f));
    /**
     * Fast, then slow, then fast. This uses the same function as {@link #pow2OutIn}.
     */
    public static final Interpolator fastSlowFast = new Interpolator("fastSlowFast", pow2OutIn.fn);
    /**
     * Accelerates/decelerates using {@link #powOutInFunction(float)} and power of 3.
     */
    public static final Interpolator pow3OutIn = new Interpolator("pow3OutIn", powOutInFunction(3f));
    /**
     * Accelerates/decelerates using {@link #powOutInFunction(float)} and power of 4.
     */
    public static final Interpolator pow4OutIn = new Interpolator("pow4OutIn", powOutInFunction(4f));
    /**
     * Accelerates/decelerates using {@link #powOutInFunction(float)} and power of 5.
     */
    public static final Interpolator pow5OutIn = new Interpolator("pow5OutIn", powOutInFunction(5f));
    /**
     * Accelerates/decelerates using {@link #powOutInFunction(float)} and power of 0.75.
     */
    public static final Interpolator pow0_75OutIn = new Interpolator("pow0_75OutIn", powOutInFunction(0.75f));
    /**
     * Accelerates/decelerates using {@link #powOutInFunction(float)} and power of 0.5.
     */
    public static final Interpolator pow0_5OutIn = new Interpolator("pow0_5OutIn", powOutInFunction(0.5f));
    /**
     * Accelerates/decelerates using {@link #powOutInFunction(float)} and power of 0.25.
     */
    public static final Interpolator pow0_25OutIn = new Interpolator("pow0_25OutIn", powOutInFunction(0.25f));

    /**
     * Produces an InterpolationFunction that uses the given value and power variables.
     * When power is greater than 1, this starts slowly, speeds up in the middle and slows down at the end. The
     * rate of acceleration and deceleration changes based on the parameter. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction expFunction(final float value, final float power) {
        final float min = (float) Math.pow(value, -power), scale = 1f / (1f - min);
        return a -> {
            if (a <= 0.5f) return ((float)Math.pow(value, power * (a * 2f - 1f)) - min) * scale * 0.5f;
            return (2f - ((float)Math.pow(value, -power * (a * 2f - 1f)) - min) * scale) * 0.5f;
        };
    }

    /**
     * Produces an InterpolationFunction that uses the given value and power variables.
     * When power is greater than 1, this starts slowly and speeds up toward the end. The
     * rate of acceleration changes based on the parameter. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction expInFunction(final float value, final float power) {
        final float min = (float) Math.pow(value, -power), scale = 1f / (1f - min);
        return a -> ((float)Math.pow(value, power * (a - 1f)) - min) * scale;
    }

    /**
     * Produces an InterpolationFunction that uses the given value and power variables.
     * When power is greater than 1, this starts quickly and slows down toward the end. The
     * rate of deceleration changes based on the parameter. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction expOutFunction(final float value, final float power) {
        final float min = (float) Math.pow(value, -power), scale = 1f / (1f - min);
        return a -> 1f - ((float)Math.pow(value, -power * a) - min) * scale;
    }

    /**
     * Accelerates and decelerates using {@link #expFunction(float, float)}, value of 2 and power of 5.
     */
    public static final Interpolator exp5 = new Interpolator("exp5", expFunction(2f, 5f));

    /**
     * Accelerates and decelerates using {@link #expFunction(float, float)}, value of 2 and power of 10.
     */
    public static final Interpolator exp10 = new Interpolator("exp10", expFunction(2f, 10f));

    /**
     * Accelerates using {@link #expInFunction(float, float)}, value of 2 and power of 5.
     */
    public static final Interpolator exp5In = new Interpolator("exp5In", expInFunction(2f, 5f));

    /**
     * Accelerates using {@link #expInFunction(float, float)}, value of 2 and power of 10.
     */
    public static final Interpolator exp10In = new Interpolator("exp10In", expInFunction(2f, 10f));

    /**
     * Decelerates using {@link #expOutFunction(float, float)}, value of 2 and power of 5.
     */
    public static final Interpolator exp5Out = new Interpolator("exp5Out", expOutFunction(2f, 5f));

    /**
     * Decelerates using {@link #expOutFunction(float, float)}, value of 2 and power of 10.
     */
    public static final Interpolator exp10Out = new Interpolator("exp10Out", expOutFunction(2f, 10f));


    /**
     * Accelerates and decelerates using {@link #expFunction(float, float)}, value of 2 and power of 5, but flipped.
     */
    public static final Interpolator exp5OutIn = new Interpolator("exp5OutIn", exp5.fn.flip());

    /**
     * Accelerates and decelerates using {@link #expFunction(float, float)}, value of 2 and power of 10, but flipped.
     */
    public static final Interpolator exp10OutIn = new Interpolator("exp10OutIn", exp10.fn.flip());


    /**
     * Produces an InterpolationFunction that uses the possible shapes of the Kumaraswamy distribution, but without
     * involving a random component. This can produce a wide range of shapes for the interpolation, and may require
     * generating several during development to get a particular shape you want. The a and b parameters must be greater
     * than 0.0, but have no other requirements. Most curves that this method produces are somewhat asymmetrical.
     * @see <a href="https://en.wikipedia.org/wiki/Kumaraswamy_distribution">Wikipedia's page on this distribution.</a>
     * @param a the Kumaraswamy distribution's a parameter
     * @param b the Kumaraswamy distribution's b parameter
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction kumaraswamyFunction(final double a, final double b) {
        final double A = 1.0 / a;
        final double B = 1.0 / b;
        return x -> (float) Math.pow(1.0 - Math.pow(1.0 - x, B), A);
    }

    /**
     * Produces an InterpolationFunction that uses the given shape and turning variables.
     * A wrapper around {@link MathTools#barronSpline(float, float, float)} to use it
     * as an Interpolator or InterpolationFunction. Useful because it can imitate the wide variety of symmetrical
     * interpolations by setting turning to 0.5 and shape to some value greater than 1, while also being able to produce
     * the inverse of those interpolations by setting shape to some value between 0 and 1. It can also produce
     * asymmetrical interpolations by using a turning value other than 0.5 .
     * @param shape   must be greater than or equal to 0; values greater than 1 are "normal interpolations"
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction biasGainFunction(final float shape, final float turning) {
        return a -> barronSpline(a, shape, turning);
    }

    /**
     * Produces more results in the center; the first level of centrality. Uses {@code biasGainFunction(0.75f, 0.5f)}.
     */
    public static final Interpolator biasGainCenteredA = new Interpolator("biasGainCenteredA", biasGainFunction(0.75f, 0.5f));
    /**
     * Produces more results in the center; the second level of centrality. Uses {@code biasGainFunction(0.5f, 0.5f)}.
     */
    public static final Interpolator biasGainCenteredB = new Interpolator("biasGainCenteredB", biasGainFunction(0.5f, 0.5f));
    /**
     * Produces more results in the center; the third level of centrality. Uses {@code biasGainFunction(0.25f, 0.5f)}.
     */
    public static final Interpolator biasGainCenteredC = new Interpolator("biasGainCenteredC", biasGainFunction(0.25f, 0.5f));
    /**
     * Produces more results near 0 and near 1; the third level of extremity. Uses {@code biasGainFunction(2f, 0.5f)}.
     */
    public static final Interpolator biasGainExtremeA = new Interpolator("biasGainExtremeA", biasGainFunction(2f, 0.5f));
    /**
     * Produces more results near 0 and near 1; the third level of extremity. Uses {@code biasGainFunction(3f, 0.5f)}.
     */
    public static final Interpolator biasGainExtremeB = new Interpolator("biasGainExtremeB", biasGainFunction(3f, 0.5f));
    /**
     * Produces more results near 0 and near 1; the third level of extremity. Uses {@code biasGainFunction(4f, 0.5f)}.
     */
    public static final Interpolator biasGainExtremeC = new Interpolator("biasGainExtremeC", biasGainFunction(4f, 0.5f));
    /**
     * Produces more results near 0. Uses {@code biasGainFunction(3f, 0.9f)}.
     */
    public static final Interpolator biasGainMostlyLow = new Interpolator("biasGainMostlyLow", biasGainFunction(3f, 0.9f));
    /**
     * Produces more results near 1. Uses {@code biasGainFunction(3f, 0.1f)}.
     */
    public static final Interpolator biasGainMostlyHigh = new Interpolator("biasGainMostlyHigh", biasGainFunction(3f, 0.1f));

    /**
     * Produces more results toward the edges. Uses {@code kumaraswamyFunction(0.75f, 0.75f)}.
     */
    public static final Interpolator kumaraswamyExtremeA = new Interpolator("kumaraswamyExtremeA", kumaraswamyFunction(0.75f, 0.75f));
    /**
     * Produces more results toward the edges. Uses {@code kumaraswamyFunction(0.5f, 0.5f)}.
     */
    public static final Interpolator kumaraswamyExtremeB = new Interpolator("kumaraswamyExtremeB", kumaraswamyFunction(0.5f, 0.5f));
    /**
     * Produces more results toward the edges. Uses {@code kumaraswamyFunction(0.25f, 0.25f)}.
     */
    public static final Interpolator kumaraswamyExtremeC = new Interpolator("kumaraswamyExtremeC", kumaraswamyFunction(0.25f, 0.25f));
    /**
     * Produces more results in the center. Uses {@code kumaraswamyFunction(2f, 2f)}.
     */
    public static final Interpolator kumaraswamyCentralA = new Interpolator("kumaraswamyCentralA", kumaraswamyFunction(2f, 2f));
    /**
     * Produces more results in the center. Uses {@code kumaraswamyFunction(4f, 4f)}.
     */
    public static final Interpolator kumaraswamyCentralB = new Interpolator("kumaraswamyCentralB", kumaraswamyFunction(4f, 4f));
    /**
     * Produces more results in the center. Uses {@code kumaraswamyFunction(6f, 6f)}.
     */
    public static final Interpolator kumaraswamyCentralC = new Interpolator("kumaraswamyCentralC", kumaraswamyFunction(6f, 6f));
    /**
     * Produces more results near 0. Uses {@code kumaraswamyFunction(1f, 5f)}.
     */
    public static final Interpolator kumaraswamyMostlyLow = new Interpolator("kumaraswamyMostlyLow", kumaraswamyFunction(1f, 5f));
    /**
     * Produces more results near 1. Uses {@code kumaraswamyFunction(5f, 1f)}.
     */
    public static final Interpolator kumaraswamyMostlyHigh = new Interpolator("kumaraswamyMostlyHigh", kumaraswamyFunction(5f, 1f));

// The "sine" implementations use the SIN and COS tables directly to avoid a few operations inside sinTurns().
// They use SIN_TO_COS as an offset because if the number of bits in the table changes, SIN_TO_COS will adapt.
    /**
     * Moves like a sine wave does; starts slowly, rises quickly, then ends slowly.
     */
    public static final Interpolator sine = new Interpolator("sine", a -> (a = SIN_TABLE[(int) (a * SIN_TO_COS) & TABLE_MASK]) * a);

    /**
     * Moves like a sine wave does; starts slowly and rises quickly.
     */
    public static final Interpolator sineIn = new Interpolator("sineIn", a -> (1f - COS_TABLE[(int) (a * SIN_TO_COS) & TABLE_MASK]));
    /**
     * Moves like a sine wave does; starts quickly and slows down.
     */
    public static final Interpolator sineOut = new Interpolator("sineOut", a -> SIN_TABLE[(int) (a * SIN_TO_COS) & TABLE_MASK]);
    /**
     * Moves like a sine wave does, but flipped; starts quickly, rises slowly, then ends quickly.
     */
    public static final Interpolator sineOutIn = new Interpolator("sineOutIn", sine.fn.flip());

// This is here so that we can validate the old circle output against the new.
//    public static final Interpolator circleOld = new Interpolator("circle", a -> {
//        if (a <= 0.5f) {
//            a *= 2;
//            return (1 - (float)Math.sqrt(1 - a * a)) / 2;
//        }
//        a--;
//        a *= 2;
//        return ((float)Math.sqrt(1 - a * a) + 1) / 2;
//    });
//
    /**
     * When graphed, forms two circular arcs; it starts slowly, accelerating rapidly towards the middle, then slows down
     * towards the end.
     */
    public static final Interpolator circle = new Interpolator("circle", a -> (a <= 0.5f
            ? (1f - (float)Math.sqrt(1f - a * a * 4f)) * 0.5f
            : ((float)Math.sqrt(1f - 4f * (a * (a - 2f) + 1f)) + 1f) * 0.5f));
    /**
     * When graphed, forms one circular arc, starting slowly and accelerating at the end.
     */
    public static final Interpolator circleIn = new Interpolator("circleIn", a -> (1f - (float)Math.sqrt(1f - a * a)));
    /**
     * When graphed, forms one circular arc, starting rapidly and decelerating at the end.
     */
    public static final Interpolator circleOut = new Interpolator("circleOut", a -> ((float)Math.sqrt(a * (2f - a))));
    /**
     * When graphed, forms two circular arcs; it starts quickly, decelerating towards the middle, then speeds up
     * towards the end.
     */
    public static final Interpolator circleOutIn = new Interpolator("circleOutIn", circle.fn.flip());

    /**
     * Produces an InterpolationFunction that uses the given {@code width, height, width, height, ...} float array.
     * Unlike {@link #bounceOutFunction(float...)}, this bounces at both the start and end of its interpolation.
     * Fair warning; using this is atypically complicated, and you should generally stick to using a predefined
     * Interpolator, such as {@link #bounce4}. You can also hand-edit the values in pairs; if you do, every even
     * index is a width, and every odd index is a height. Later widths are no greater than earlier ones; this is also
     * true for heights. No width is typically greater than 1.5f, and they are always positive and less than 2f.
     *
     * @param pairs width, height, width, height... in pairs; typically none are larger than 1.5f, and all are positive
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction bounceFunction(final float... pairs) {
        final InterpolationFunction bOut = bounceOutFunction(pairs), iOut = o -> {
            float test = o + pairs[0] * 0.5f;
            if (test < pairs[0]) return test / (pairs[0] * 0.5f) - 1f;
            return bOut.apply(o);
        };

        return a -> {
            if(a <= 0.5f) return (1f - iOut.apply(1f - a - a)) * 0.5f;
            return iOut.apply(a + a - 1) * 0.5f + 0.5f;
        };
    }

    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 2 bounces.
     */
    public static final Interpolator bounce2 = new Interpolator("bounce2", bounceFunction(1.2f, 1f, 0.4f, 0.33f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 3 bounces.
     */
    public static final Interpolator bounce3 = new Interpolator("bounce3", bounceFunction(0.8f, 1f, 0.4f, 0.33f, 0.2f, 0.1f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 4 bounces.
     */
    public static final Interpolator bounce4 = new Interpolator("bounce4", bounceFunction(0.65f, 1f, 0.325f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 4 bounces. While both this and
     * {@link #bounce4} use 4 bounces, this matches the behavior of bounce in libGDX.
     */
    public static final Interpolator bounce = new Interpolator("bounce", bounceFunction(0.68f, 1f, 0.34f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 5 bounces.
     */
    public static final Interpolator bounce5 = new Interpolator("bounce5", bounceFunction(0.61f, 1f, 0.31f, 0.45f, 0.21f, 0.3f, 0.11f, 0.15f, 0.06f, 0.06f));

    /**
     * Produces an InterpolationFunction that uses the given {@code width, height, width, height, ...} float array.
     * This bounces at the end of its interpolation.
     * Fair warning; using this is atypically complicated, and you should generally stick to using a predefined
     * Interpolator, such as {@link #bounce4Out}. You can also hand-edit the values in pairs; if you do, every even
     * index is a width, and every odd index is a height. Later widths are no greater than earlier ones; this is also
     * true for heights. No width is typically greater than 1.5f, and they are always positive and less than 2f.
     *
     * @param pairs width, height, width, height... in pairs; typically none are larger than 1.5f, and all are positive
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction bounceOutFunction(final float... pairs) {
        return a -> {
//            if(a >= 1f) return 1f;
            float b = a + pairs[0] * 0.5f;
            float width = 0f, height = 0f;
            for (int i = 0, n = (pairs.length & -2) - 1; i < n; i += 2) {
                width = pairs[i];
                if (b <= width) {
                    height = pairs[i + 1];
                    break;
                }
                b -= width;
            }
            float z = 4f / (width * width) * height * b;
            float f = 1f - z * (width - b);
            // pretty sure this is equivalent to the 2 lines above. Not certain.
//            a /= width;
//            float z = 4 / width * height * a;
//            return 1 - (z - z * a) * width;
            return a >= 0.98f ? MathTools.lerp(f, 1f, 50f * (a - 0.98f)) : f;

        };
    }

    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 2 bounces.
     */
    public static final Interpolator bounce2Out = new Interpolator("bounce2Out", bounceOutFunction(1.2f, 1f, 0.4f, 0.33f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 3 bounces.
     */
    public static final Interpolator bounce3Out = new Interpolator("bounce3Out", bounceOutFunction(0.8f, 1f, 0.4f, 0.33f, 0.2f, 0.1f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 4 bounces.
     */
    public static final Interpolator bounce4Out = new Interpolator("bounce4Out", bounceOutFunction(0.65f, 1f, 0.325f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 4 bounces. While both this and
     * {@link #bounce4Out} use 4 bounces, this matches the behavior of bounceOut in libGDX.
     */
    public static final Interpolator bounceOut = new Interpolator("bounceOut", bounceOutFunction(0.68f, 1f, 0.34f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 5 bounces.
     */
    public static final Interpolator bounce5Out = new Interpolator("bounce5Out", bounceOutFunction(0.61f, 1f, 0.31f, 0.45f, 0.21f, 0.3f, 0.11f, 0.15f, 0.06f, 0.06f));

    /**
     * Produces an InterpolationFunction that uses the given {@code width, height, width, height, ...} float array.
     * This bounces at the start of its interpolation.
     * Fair warning; using this is atypically complicated, and you should generally stick to using a predefined
     * Interpolator, such as {@link #bounce4In}. You can also hand-edit the values in pairs; if you do, every even
     * index is a width, and every odd index is a height. Later widths are no greater than earlier ones; this is also
     * true for heights. No width is typically greater than 1.5f, and they are always positive and less than 2f.
     *
     * @param pairs width, height, width, height... in pairs; typically none are larger than 1.5f, and all are positive
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction bounceInFunction(final float... pairs) {
        final InterpolationFunction bOut = bounceOutFunction(pairs);
        return a -> 1f - bOut.apply(1f - a);
    }

    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 2 bounces.
     */
    public static final Interpolator bounce2In = new Interpolator("bounce2In", bounceInFunction(1.2f, 1f, 0.4f, 0.33f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 3 bounces.
     */
    public static final Interpolator bounce3In = new Interpolator("bounce3In", bounceInFunction(0.8f, 1f, 0.4f, 0.33f, 0.2f, 0.1f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 4 bounces.
     */
    public static final Interpolator bounce4In = new Interpolator("bounce4In", bounceInFunction(0.65f, 1f, 0.325f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 4 bounces. While both this and
     * {@link #bounce4In} use 4 bounces, this matches the behavior of bounceIn in libGDX.
     */
    public static final Interpolator bounceIn = new Interpolator("bounceIn", bounceInFunction(0.68f, 1f, 0.34f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 5 bounces.
     */
    public static final Interpolator bounce5In = new Interpolator("bounce5In", bounceInFunction(0.61f, 1f, 0.31f, 0.45f, 0.21f, 0.3f, 0.11f, 0.15f, 0.06f, 0.06f));

    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 2 bounces, but flipped.
     */
    public static final Interpolator bounce2OutIn = new Interpolator("bounce2OutIn", bounce2.fn.flip());
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 3 bounces, but flipped.
     */
    public static final Interpolator bounce3OutIn = new Interpolator("bounce3OutIn", bounce3.fn.flip());
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 4 bounces, but flipped.
     */
    public static final Interpolator bounce4OutIn = new Interpolator("bounce4OutIn", bounce4.fn.flip());
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 4 bounces, but flipped.
     * While both this and {@link #bounce4OutIn} use 4 bounces, this matches the behavior of bounce in libGDX (flipped).
     */
    public static final Interpolator bounceOutIn = new Interpolator("bounceOutIn", bounce.fn.flip());
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 5 bounces, but flipped.
     */
    public static final Interpolator bounce5OutIn = new Interpolator("bounce5OutIn", bounce5.fn.flip());

    /**
     * Produces an InterpolationFunction that uses the given scale variable.
     * This drops below 0.0 at the start of the range, accelerates very rapidly, exceeds 1.0 at the middle of the input
     * range, and ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction swingFunction(final float scale) {
        final float sc = scale + scale;
        return a -> {
            if (a <= 0.5f) return ((sc + 1f) * (a += a) - sc) * a * a * 0.5f;
            return ((sc + 1f) * (a += a - 2f) + sc) * a * a * 0.5f + 1f;
        };
    }
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 2.
     */
    public static final Interpolator swing2 = new Interpolator("swing2", swingFunction(2f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 1.5.
     */
    public static final Interpolator swing = new Interpolator("swing", swingFunction(1.5f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 3.
     */
    public static final Interpolator swing3 = new Interpolator("swing3", swingFunction(3f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 0.75.
     */
    public static final Interpolator swing0_75 = new Interpolator("swing0_75", swingFunction(0.75f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 0.5.
     */
    public static final Interpolator swing0_5 = new Interpolator("swing0_5", swingFunction(0.5f));

    /**
     * Produces an InterpolationFunction that uses the given scale variable.
     * This accelerates very rapidly, exceeds 1.0 at the middle of the input range, and ends returning 1.0. Negative
     * parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction swingOutFunction(final float scale) {
        return a -> ((scale + 1f) * --a + scale) * a * a + 1f;
    }
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 2.
     */
    public static final Interpolator swing2Out = new Interpolator("swing2Out", swingOutFunction(2f));
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 2. This uses the same function as
     * {@link #swing2Out}.
     */
    public static final Interpolator swingOut = new Interpolator("swingOut", swing2Out.fn);
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 3.
     */
    public static final Interpolator swing3Out = new Interpolator("swing3Out", swingOutFunction(3f));
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 0.75.
     */
    public static final Interpolator swing0_75Out = new Interpolator("swing0_75Out", swingOutFunction(0.75f));
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 0.5.
     */
    public static final Interpolator swing0_5Out = new Interpolator("swing0_5Out", swingOutFunction(0.5f));

    /**
     * Produces an InterpolationFunction that uses the given scale variable.
     * This drops below 0.0 before the middle of the input range, later speeds up rapidly, and ends returning 1.0.
     * Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction swingInFunction(final float scale) {
        return a -> a * a * ((scale + 1f) * a - scale);
    }

    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 2.
     */
    public static final Interpolator swing2In = new Interpolator("swing2In", swingInFunction(2f));
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 2. This uses the same function as
     * {@link #swing2In}.
     */
    public static final Interpolator swingIn = new Interpolator("swingIn", swing2In.fn);
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 3.
     */
    public static final Interpolator swing3In = new Interpolator("swing3In", swingInFunction(3f));
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 0.75.
     */
    public static final Interpolator swing0_75In = new Interpolator("swing0_75In", swingInFunction(0.75f));
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 0.5.
     */
    public static final Interpolator swing0_5In = new Interpolator("swing0_5In", swingInFunction(0.5f));

    /**
     * Should stay in-range, using {@link #swingFunction(float)} and scale of 2, but flipped.
     */
    public static final Interpolator swing2OutIn = new Interpolator("swing2OutIn", swing2.fn.flip());
    /**
     * Should stay in-range, using {@link #swingFunction(float)} and scale of 1.5, but flipped.
     */
    public static final Interpolator swingOutIn = new Interpolator("swingOutIn", swing.fn.flip());
    /**
     * Should stay in-range, using {@link #swingFunction(float)} and scale of 3, but flipped.
     */
    public static final Interpolator swing3OutIn = new Interpolator("swing3OutIn", swing3.fn.flip());
    /**
     * Should stay in-range, using {@link #swingFunction(float)} and scale of 0.75, but flipped.
     */
    public static final Interpolator swing0_75OutIn = new Interpolator("swing0_75OutIn", swing0_75.fn.flip());
    /**
     * Should stay in-range, using {@link #swingFunction(float)} and scale of 0.5, but flipped.
     */
    public static final Interpolator swing0_5OutIn = new Interpolator("swing0_5OutIn", swing0_5.fn.flip());

    /**
     * Produces an InterpolationFunction that uses the given value, power, bounces, and scale variables.
     * This drops below 0.0 near the middle of the range, accelerates near-instantly, exceeds 1.0 just after that,
     * and ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction elasticFunction(final float value, final float power, final int bounces,
                                                        final float scale) {
        final float bounce = bounces * (0.5f - (bounces & 1));
        return a -> (a <= 0.5f)
                ? (float)Math.pow(value, power * ((a += a) - 1f)) * TrigTools.sinTurns(a * bounce) * scale * 0.5f
                : 1f - (float)Math.pow(value, power * ((a = 2f - a - a) - 1f)) * TrigTools.sinTurns(a * bounce) * scale * 0.5f;
    }
    /**
     * Goes extra low, then extra-high, using {@link #elasticFunction(float, float, int, float)}. Value is 2, power is
     * 10, bounces are 7, and scale is 1.
     */
    public static final Interpolator elastic = new Interpolator("elastic", elasticFunction(2f, 10f, 7, 1f));

    /**
     * Produces an InterpolationFunction that uses the given value, power, bounces, and scale variables.
     * This exceeds 1.0 just after the start of the range,
     * and ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static Interpolations.InterpolationFunction elasticOutFunction(final float value, final float power, final int bounces,
                                                                          final float scale) {
        final float bounce = bounces * (0.5f - (bounces & 1));
        return a -> {
            float f = (1f - (float) Math.pow(value, power * -a) * TrigTools.sinTurns(bounce - a * bounce) * scale);
            return a <= 0.02f ? MathTools.lerp(0f, f, a * 50f) : f;
        };
    }

    /**
     * Goes extra-high near the start, using {@link #elasticOutFunction(float, float, int, float)}. Value is 2, power is
     * 10, bounces are 7, and scale is 1.
     */
    public static final Interpolator elasticOut = new Interpolator("elasticOut", elasticOutFunction(2f, 10f, 7, 1f));

    /**
     * Produces an InterpolationFunction that uses the given value, power, bounces, and scale variables.
     * This drops below 0.0 just before the end of the range,
     * but jumps up so that it ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static Interpolations.InterpolationFunction elasticInFunction(final float value, final float power, final int bounces,
                                                                         final float scale) {
        final float bounce = bounces * (0.5f - (bounces & 1));
        return a -> {
            float f = (float) Math.pow(value, power * (a - 1)) * TrigTools.sinTurns(a * bounce) * scale;
            return a >= 0.98f ? MathTools.lerp(f, 1f, 50f * (a - 0.98f)) : f;
        };
    }
    /**
     * Goes extra-low near the end, using {@link #elasticInFunction(float, float, int, float)}. Value is 2, power is
     * 10, bounces are 6, and scale is 1.
     */
    public static final Interpolator elasticIn = new Interpolator("elasticIn", elasticInFunction(2f, 10f, 6, 1f));

    /**
     * Produces an InterpolationFunction that uses the given value, power, bounces, and scale variables.
     * This accelerates near-instantly, wiggles in to settle near the middle of the range, accelerates again near the
     * end, and finishes returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static Interpolations.InterpolationFunction elasticOutInFunction(final float value, final float power, final int bounces,
                                                                            final float scale) {
        final float bounce = bounces * (0.5f - (bounces & 1)) - 0.25f;
        return a -> (a > 0.5f)
                ? (float)Math.pow(value, power * ((a += a - 1) - 1f)) * TrigTools.sinTurns(a * bounce) * scale * 0.5f + 0.5f
                : 0.5f - (float)Math.pow(value, power * ((a = 1f - a - a) - 1f)) * TrigTools.sinTurns(a * bounce) * scale * 0.5f;
    }
    /**
     * Stays within the mid-range, using {@link #elasticOutInFunction(float, float, int, float)}. Value is 2, power
     * is 10, bounces are 7, and scale is 1.
     */
    public static final Interpolator elasticOutIn = new Interpolator("elasticOutIn", elasticOutInFunction(2f, 10f, 7, 1f));


    // Aliases
    /**
     * Alias for {@link #pow2}.
     */
    public static final Interpolator quadInOut = new Interpolator("quadInOut", pow2.fn);
    /**
     * Alias for {@link #pow2In}.
     */
    public static final Interpolator quadIn = new Interpolator("quadIn", pow2In.fn);
    /**
     * Alias for {@link #pow2Out}.
     */
    public static final Interpolator quadOut = new Interpolator("quadOut", pow2Out.fn);
    /**
     * Alias for {@link #pow2OutIn}.
     */
    public static final Interpolator quadOutIn = new Interpolator("quadOutIn", pow2OutIn.fn);
    /**
     * Alias for {@link #pow3}.
     */
    public static final Interpolator cubicInOut = new Interpolator("cubicInOut", pow3.fn);
    /**
     * Alias for {@link #pow3In}.
     */
    public static final Interpolator cubicIn = new Interpolator("cubicIn", pow3In.fn);
    /**
     * Alias for {@link #pow3Out}.
     */
    public static final Interpolator cubicOut = new Interpolator("cubicOut", pow3Out.fn);
    /**
     * Alias for {@link #pow3OutIn}.
     */
    public static final Interpolator cubicOutIn = new Interpolator("cubicOutIn", pow3OutIn.fn);
    /**
     * Alias for {@link #pow4}.
     */
    public static final Interpolator quartInOut = new Interpolator("quartInOut", pow4.fn);
    /**
     * Alias for {@link #pow4In}.
     */
    public static final Interpolator quartIn = new Interpolator("quartIn", pow4In.fn);
    /**
     * Alias for {@link #pow4Out}.
     */
    public static final Interpolator quartOut = new Interpolator("quartOut", pow4Out.fn);
    /**
     * Alias for {@link #pow4OutIn}.
     */
    public static final Interpolator quartOutIn = new Interpolator("quartOutIn", pow4OutIn.fn);
    /**
     * Alias for {@link #pow5}.
     */
    public static final Interpolator quintInOut = new Interpolator("quintInOut", pow5.fn);
    /**
     * Alias for {@link #pow5In}.
     */
    public static final Interpolator quintIn = new Interpolator("quintIn", pow5In.fn);
    /**
     * Alias for {@link #pow5Out}.
     */
    public static final Interpolator quintOut = new Interpolator("quintOut", pow5Out.fn);
    /**
     * Alias for {@link #pow5OutIn}.
     */
    public static final Interpolator quintOutIn = new Interpolator("quintOutIn", pow5OutIn.fn);

    /**
     * Alias for {@link #exp10}.
     */
    public static final Interpolator expoInOut = new Interpolator("expoInOut", exp10.fn);
    /**
     * Alias for {@link #exp10In}.
     */
    public static final Interpolator expoIn = new Interpolator("expoIn", exp10In.fn);
    /**
     * Alias for {@link #exp10Out}.
     */
    public static final Interpolator expoOut = new Interpolator("expoOut", exp10Out.fn);
    /**
     * Alias for {@link #exp10OutIn}.
     */
    public static final Interpolator expoOutIn = new Interpolator("expoOutIn", exp10OutIn.fn);

    /**
     * Alias for {@link #circle}.
     */
    public static final Interpolator circInOut = new Interpolator("circInOut", circle.fn);
    /**
     * Alias for {@link #circleIn}.
     */
    public static final Interpolator circIn = new Interpolator("circIn", circleIn.fn);
    /**
     * Alias for {@link #circleOut}.
     */
    public static final Interpolator circOut = new Interpolator("circOut", circleOut.fn);
    /**
     * Alias for {@link #circleOutIn}.
     */
    public static final Interpolator circOutIn = new Interpolator("circOutIn", circleOutIn.fn);
    
    /**
     * Alias for {@link #swing}. Probably not an exact duplicate of the similarly-named Penner easing function.
     */
    public static final Interpolator backInOut = new Interpolator("backInOut", swing.fn);
    /**
     * Alias for {@link #swingIn}. Probably not an exact duplicate of the similarly-named Penner easing function.
     */
    public static final Interpolator backIn = new Interpolator("backIn", swingIn.fn);
    /**
     * Alias for {@link #swingOut}. Probably not an exact duplicate of the similarly-named Penner easing function.
     */
    public static final Interpolator backOut = new Interpolator("backOut", swingOut.fn);
    /**
     * Alias for {@link #swingOutIn}. Probably not an exact duplicate of the similarly-named Penner easing function.
     */
    public static final Interpolator backOutIn = new Interpolator("backOutIn", swingOutIn.fn);

}
