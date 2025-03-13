package com.github.tommyettinger.digital;

import org.junit.Test;

import static com.github.tommyettinger.digital.TrigTools.PI2;
import static com.github.tommyettinger.digital.TrigTools.degreesToRadians;
import static org.junit.Assert.assertEquals;

public class MathToolsTest {

    @Test
    public void lerpAngle () {
        assertEquals(10*degreesToRadians, (MathTools.lerpAngle(10*degreesToRadians, 30*degreesToRadians, 0.0f) + 0.005) % PI2 - 0.005f, 0.01f);
        assertEquals(20*degreesToRadians, (MathTools.lerpAngle(10*degreesToRadians, 30*degreesToRadians, 0.5f) + 0.005) % PI2 - 0.005f, 0.01f);
        assertEquals(30*degreesToRadians, (MathTools.lerpAngle(10*degreesToRadians, 30*degreesToRadians, 1.0f) + 0.005) % PI2 - 0.005f, 0.01f);
    }

    @Test
    public void lerpAngleCrossingZero () {
        assertEquals(350*degreesToRadians, (MathTools.lerpAngle(350*degreesToRadians, 10*degreesToRadians, 0.0f) + 0.005) % PI2 - 0.005f, 0.01f);
        assertEquals(0, (MathTools.lerpAngle(350*degreesToRadians, 10*degreesToRadians, 0.5f) + 0.005) % PI2 - 0.005f, 0.01f);
        assertEquals(10*degreesToRadians, (MathTools.lerpAngle(350*degreesToRadians, 10*degreesToRadians, 1.0f) + 0.005) % PI2 - 0.005f, 0.01f);
        assertEquals(279.5f*degreesToRadians, (MathTools.lerpAngle(-521*degreesToRadians, 0, 0.5f) + 0.005) % PI2 - 0.005f, 0.01f);
    }

    @Test
    public void lerpAngleCrossingZeroBackwards () {
        assertEquals(10*degreesToRadians, (MathTools.lerpAngle(10*degreesToRadians, 350*degreesToRadians, 0.0f) + 0.005) % PI2 - 0.005f, 0.01f);
        assertEquals(0, (MathTools.lerpAngle(10*degreesToRadians, 350*degreesToRadians, 0.5f) + 0.005) % PI2 - 0.005f, 0.01f);
        assertEquals(350*degreesToRadians, (MathTools.lerpAngle(10*degreesToRadians, 350*degreesToRadians, 1.0f) + 0.005) % PI2 - 0.005f, 0.01f);
    }
    
    @Test
    public void lerpAngleDeg () {
        assertEquals(10, MathTools.lerpAngleDeg(10, 30, 0.0f), 0.01f);
        assertEquals(20, MathTools.lerpAngleDeg(10, 30, 0.5f), 0.01f);
        assertEquals(30, MathTools.lerpAngleDeg(10, 30, 1.0f), 0.01f);
    }

    @Test
    public void lerpAngleDegCrossingZero () {
        assertEquals(350, MathTools.lerpAngleDeg(350, 10, 0.0f), 0.01f);
        assertEquals(0, MathTools.lerpAngleDeg(350, 10, 0.5f), 0.01f);
        assertEquals(10, MathTools.lerpAngleDeg(350, 10, 1.0f), 0.01f);
        assertEquals(279.5f, MathTools.lerpAngleDeg(-521, 0, 0.5f), 0.01f);
    }

    @Test
    public void lerpAngleDegCrossingZeroBackwards () {
        assertEquals(10, MathTools.lerpAngleDeg(10, 350, 0.0f), 0.01f);
        assertEquals(0, MathTools.lerpAngleDeg(10, 350, 0.5f), 0.01f);
        assertEquals(350, MathTools.lerpAngleDeg(10, 350, 1.0f), 0.01f);
    }
    
    @Test
    public void lerpAngleTurns () {
        assertEquals(10/360f, MathTools.lerpAngleTurns(10/360f, 30/360f, 0.0f), 0.01f);
        assertEquals(20/360f, MathTools.lerpAngleTurns(10/360f, 30/360f, 0.5f), 0.01f);
        assertEquals(30/360f, MathTools.lerpAngleTurns(10/360f, 30/360f, 1.0f), 0.01f);
    }

    @Test
    public void lerpAngleTurnsCrossingZero () {
        assertEquals(350/360f, MathTools.lerpAngleTurns(350/360f, 10/360f, 0.0f), 0.01f);
        assertEquals(0, MathTools.lerpAngleTurns(350/360f, 10/360f, 0.5f), 0.01f);
        assertEquals(10/360f, MathTools.lerpAngleTurns(350/360f, 10/360f, 1.0f), 0.01f);
        assertEquals(279.5f/360f, MathTools.lerpAngleTurns(-521/360f, 0, 0.5f), 0.01f);
    }

    @Test
    public void lerpAngleTurnsCrossingZeroBackwards () {
        assertEquals(10/360f, MathTools.lerpAngleTurns(10/360f, 350/360f, 0.0f), 0.01f);
        assertEquals(0, MathTools.lerpAngleTurns(10/360f, 350/360f, 0.5f), 0.01f);
        assertEquals(350/360f, MathTools.lerpAngleTurns(10/360f, 350/360f, 1.0f), 0.01f);
        assertEquals(10/360.0, MathTools.lerpAngleTurns(10/360.0, 350/360.0, 0.0), 0.01);
        assertEquals(0, MathTools.lerpAngleTurns(10/360.0, 350/360.0, 0.5), 0.01);
        assertEquals(350/360.0, MathTools.lerpAngleTurns(10/360.0, 350/360.0, 1.0), 0.01);
    }

    @Test
    public void testNorm () {
        assertEquals(-1.0f, MathTools.norm(10f, 20f, 0f), 0.01f);
        assertEquals(0.0f, MathTools.norm(10f, 20f, 10f), 0.01f);
        assertEquals(0.5f, MathTools.norm(10f, 20f, 15f), 0.01f);
        assertEquals(1.0f, MathTools.norm(10f, 20f, 20f), 0.01f);
        assertEquals(2.0f, MathTools.norm(10f, 20f, 30f), 0.01f);
    }

    @Test
    public void testMap () {
        assertEquals(0f, MathTools.map(10f, 20f, 100f, 200f, 0f), 0.01f);
        assertEquals(100f, MathTools.map(10f, 20f, 100f, 200f, 10f), 0.01f);
        assertEquals(150f, MathTools.map(10f, 20f, 100f, 200f, 15f), 0.01f);
        assertEquals(200f, MathTools.map(10f, 20f, 100f, 200f, 20f), 0.01f);
        assertEquals(300f, MathTools.map(10f, 20f, 100f, 200f, 30f), 0.01f);
    }

    @Test
    public void testCbrt() {
        for (float i = -256f; i < 256f; i+= 0x1p-8f) {
            assertEquals(i + " failed", Math.cbrt(i), MathTools.cbrt(i), 1.0E-05f);
        }
        for (double i = -256.0; i < 256.0; i+= 0x1p-8) {
            assertEquals(i + " failed", Math.cbrt(i), MathTools.cbrt(i), 1.0E-15);
        }
    }

//    public static float approach(float a, float b, float delta, float halfLife){
//        final float x = -delta/halfLife;
//        return b + (a - b) * (float)Math.pow(2f, x);
//    }

    public static float sigmoid_0_2(float x) {
        return x / (0.2f + x);
    }

    public static float sigmoid_0_5(float x) {
        return x / (0.5f + x);
    }

    public static float sigmoid_1_0(float x) {
        return x / (1f + x);
    }

    public static float sigRoot_1_0(float x) {
        return x / (float)Math.sqrt(1f + x * x);
    }

    /**
     * Given a value x that starts at 0 and goes up, this returns floats that gradually approach 1; how gradually
     * depends on, naturally, howGradual. The howGradual parameter should usually be about 1f for a lazy, smooth
     * approach, can be higher to get an even more shallow "takeoff" angle, or can be lower than 1 (but must be greater
     * than 0) to make the takeoff more rapid or jumpy.
     * @param x any non-negative float
     * @param howGradual often 1.0, but should be adjusted higher if the approach should be very gradual, or as low as 0.1 if the approach should be rapid
     * @return a float that starts at 0.0 and gradually approaches 1.0 as x goes from 0.0 to higher values
     */
    public static float sigRoot(float x, float howGradual) {
        return x / (float)Math.sqrt(howGradual + x * x);
    }

    @Test
    public void testApproach() {
        float a = 0f, b = 100f;
        float aSmoothing60 = a, aApproach60 = a, aTanh60 = a, aSigmoid_0_2_60 = a, aSigmoid_0_5_60 = a, aSigmoid_1_0_60 = a, aSigRoot_1_0_60 = a;
        float aSmoothing90 = a, aApproach90 = a, aTanh90 = a, aSigmoid_0_2_90 = a, aSigmoid_0_5_90 = a, aSigmoid_1_0_90 = a, aSigRoot_1_0_90 = a;
        float aSmoothing10 = a, aApproach10 = a, aTanh10 = a, aSigmoid_0_2_10 = a, aSigmoid_0_5_10 = a, aSigmoid_1_0_10 = a, aSigRoot_1_0_10 = a;
        for (int i = 0; i <= 180; i++) {
            float time60 = i / 60f;
            float time90 = i / 90f;
            float time10 = i / 10f;
            System.out.println("Iteration " + i + ", time60: " + time60  + ", time90: " + time90  + ", time10: " + time10);
            aTanh60 = MathTools.lerp(a, b, RoughMath.tanhRough(time60 * 1.1f));
            aTanh90 = MathTools.lerp(a, b, RoughMath.tanhRough(time90 * 1.1f));
            aTanh10 = MathTools.lerp(a, b, RoughMath.tanhRough(time10 * 1.1f));
            aSigmoid_0_2_60 = MathTools.lerp(a, b, sigmoid_0_2(time60));
            aSigmoid_0_2_90 = MathTools.lerp(a, b, sigmoid_0_2(time90));
            aSigmoid_0_2_10 = MathTools.lerp(a, b, sigmoid_0_2(time10));
            aSigmoid_0_5_60 = MathTools.lerp(a, b, sigmoid_0_5(time60));
            aSigmoid_0_5_90 = MathTools.lerp(a, b, sigmoid_0_5(time90));
            aSigmoid_0_5_10 = MathTools.lerp(a, b, sigmoid_0_5(time10));
            aSigmoid_1_0_60 = MathTools.lerp(a, b, sigmoid_1_0(time60));
            aSigmoid_1_0_90 = MathTools.lerp(a, b, sigmoid_1_0(time90));
            aSigmoid_1_0_10 = MathTools.lerp(a, b, sigmoid_1_0(time10));
            aSigRoot_1_0_60 = MathTools.lerp(a, b, sigRoot_1_0(time60));
            aSigRoot_1_0_90 = MathTools.lerp(a, b, sigRoot_1_0(time90));
            aSigRoot_1_0_10 = MathTools.lerp(a, b, sigRoot_1_0(time10));
            System.out.println("aSmoothing60: " + Base.BASE10.decimal(aSmoothing60, 9) + " vs. aApproach60: " + Base.BASE10.decimal(aApproach60, 9) + " vs. aTanh60: " + Base.BASE10.decimal(aTanh60, 9) + "\nvs. aSigmoid_0_2_60: " + Base.BASE10.decimal(aSigmoid_0_2_60, 9) + " vs. aSigmoid_0_5_60: " + Base.BASE10.decimal(aSigmoid_0_5_60, 9) + " vs. aSigmoid_1_0_60: " + Base.BASE10.decimal(aSigmoid_1_0_60, 9) + " vs. aSigRoot_1_0_60: " + Base.BASE10.decimal(aSigRoot_1_0_60, 9));
            System.out.println("aSmoothing90: " + Base.BASE10.decimal(aSmoothing90, 9) + " vs. aApproach90: " + Base.BASE10.decimal(aApproach90, 9) + " vs. aTanh90: " + Base.BASE10.decimal(aTanh90, 9) + "\nvs. aSigmoid_0_2_90: " + Base.BASE10.decimal(aSigmoid_0_2_90, 9) + " vs. aSigmoid_0_5_90: " + Base.BASE10.decimal(aSigmoid_0_5_90, 9) + " vs. aSigmoid_1_0_90: " + Base.BASE10.decimal(aSigmoid_1_0_90, 9) + " vs. aSigRoot_1_0_90: " + Base.BASE10.decimal(aSigRoot_1_0_90, 9));
            System.out.println("aSmoothing10: " + Base.BASE10.decimal(aSmoothing10, 9) + " vs. aApproach10: " + Base.BASE10.decimal(aApproach10, 9) + " vs. aTanh10: " + Base.BASE10.decimal(aTanh10, 9) + "\nvs. aSigmoid_0_2_10: " + Base.BASE10.decimal(aSigmoid_0_2_10, 9) + " vs. aSigmoid_0_5_10: " + Base.BASE10.decimal(aSigmoid_0_5_10, 9) + " vs. aSigmoid_1_0_10: " + Base.BASE10.decimal(aSigmoid_1_0_10, 9) + " vs. aSigRoot_1_0_10: " + Base.BASE10.decimal(aSigRoot_1_0_10, 9));
            aSmoothing60 = MathTools.lerp(aSmoothing60, b, 1.5f / 60f);
            aSmoothing90 = MathTools.lerp(aSmoothing90, b, 1.5f / 90f);
            aSmoothing10 = MathTools.lerp(aSmoothing10, b, 1.5f / 10f);
            aApproach60 = MathTools.approach(aApproach60, b, 1f / 60f, 0.5f);
            aApproach90 = MathTools.approach(aApproach90, b, 1f / 90f, 0.5f);
            aApproach10 = MathTools.approach(aApproach10, b, 1f / 10f, 0.5f);
        }
    }

    /**
     * NOT WORKING; DO NOT USE.
     * When used to repeatedly mutate {@code a} every frame, and a frame had {@code delta} seconds between it and its
     * predecessor, this will make {@code a} get closer and closer to the value of {@code b} as it is repeatedly called.
     * The {@code halfLife} is the number of seconds it takes for {@code a} to halve its difference to {@code b}. Note
     * that a will never actually reach b in a specific timeframe, it will just get very close. The {@code interpolator}
     * changes (or could change) the rate {@code a} moves at different distances to {@code b}. How the interpolator
     * works here, or if it works at all as expected, is not known yet.
     * This is typically called with: {@code a = approach(a, b, deltaTime, halfLife, interpolator);}
     * <br>
     * Uses a 3/3 Padé approximant to {@code Math.pow(2.0, x)}, but otherwise is very close to
     * <a href="https://mastodon.social/@acegikmo/111931613710775864">how Freya Holmér implemented this first</a>.
     * <br>
     * This version of approach() is experimental. It may not be framerate-independent and could fail entirely.
     *
     * @param a the current float value, and the one that the result of approach() should be assigned to
     * @param b the target float value; will not change
     * @param delta the number of (typically) seconds since the last call to this movement of {@code a}
     * @param halfLife how many (typically) seconds it should take for {@code (b - a)} to become halved
     * @param interpolator an Interpolator instance, such as {@link Interpolations#smooth}
     * @return a new value to assign to {@code a}, which will be closer to {@code b}
     * @deprecated This method does not achieve any of its goals, and is not at all framerate-independent.
     */
    @Deprecated
    public static float approach(float a, float b, float delta, float halfLife, Interpolations.Interpolator interpolator){
        final float x = -delta/halfLife;
        return interpolator.apply(a, b, 1f
                - (-275.988f + x * (-90.6997f + (-11.6318f - 0.594604f * x) * x))
                / (-275.988f + x * (100.601f + (-15.0623f + x) * x)));
    }

    /**
     * Shows that the Interpolator version simply fails to achieve any of its goals.
     */
    @Test
    public void testApproachInterpolator() {
        Interpolations.Interpolator[] interpolators = {Interpolations.smooth, Interpolations.smoother, Interpolations.sineIn, Interpolations.sineOut};
        float a = 0f, b = 100f;
        for(Interpolations.Interpolator ir : interpolators) {
            float aSmoothing60 = a, aApproach60 = a, aTanh60 = a, aSigmoid_0_2_60 = a, aSigmoid_0_5_60 = a, aSigmoid_1_0_60 = a, aSigRoot_1_0_60 = a;
            float aSmoothing90 = a, aApproach90 = a, aTanh90 = a, aSigmoid_0_2_90 = a, aSigmoid_0_5_90 = a, aSigmoid_1_0_90 = a, aSigRoot_1_0_90 = a;
            float aSmoothing10 = a, aApproach10 = a, aTanh10 = a, aSigmoid_0_2_10 = a, aSigmoid_0_5_10 = a, aSigmoid_1_0_10 = a, aSigRoot_1_0_10 = a;
            for (int i = 0; i <= 180; i++) {
                float time60 = i / 60f;
                float time90 = i / 90f;
                float time10 = i / 10f;
                System.out.println("Using " + ir.tag + ", Iteration " + i + ", time60: " + time60 + ", time90: " + time90 + ", time10: " + time10);
                aTanh60 = MathTools.lerp(a, b, RoughMath.tanhRough(time60 * 1.1f));
                aTanh90 = MathTools.lerp(a, b, RoughMath.tanhRough(time90 * 1.1f));
                aTanh10 = MathTools.lerp(a, b, RoughMath.tanhRough(time10 * 1.1f));
                aSigmoid_0_2_60 = MathTools.lerp(a, b, sigmoid_0_2(time60));
                aSigmoid_0_2_90 = MathTools.lerp(a, b, sigmoid_0_2(time90));
                aSigmoid_0_2_10 = MathTools.lerp(a, b, sigmoid_0_2(time10));
                aSigmoid_0_5_60 = MathTools.lerp(a, b, sigmoid_0_5(time60));
                aSigmoid_0_5_90 = MathTools.lerp(a, b, sigmoid_0_5(time90));
                aSigmoid_0_5_10 = MathTools.lerp(a, b, sigmoid_0_5(time10));
                aSigmoid_1_0_60 = MathTools.lerp(a, b, sigmoid_1_0(time60));
                aSigmoid_1_0_90 = MathTools.lerp(a, b, sigmoid_1_0(time90));
                aSigmoid_1_0_10 = MathTools.lerp(a, b, sigmoid_1_0(time10));
                aSigRoot_1_0_60 = MathTools.lerp(a, b, sigRoot_1_0(time60));
                aSigRoot_1_0_90 = MathTools.lerp(a, b, sigRoot_1_0(time90));
                aSigRoot_1_0_10 = MathTools.lerp(a, b, sigRoot_1_0(time10));
                System.out.println("aSmoothing60: " + Base.BASE10.decimal(aSmoothing60, 9) + " vs. aApproach60: " + Base.BASE10.decimal(aApproach60, 9) + " vs. aTanh60: " + Base.BASE10.decimal(aTanh60, 9) + "\nvs. aSigmoid_0_2_60: " + Base.BASE10.decimal(aSigmoid_0_2_60, 9) + " vs. aSigmoid_0_5_60: " + Base.BASE10.decimal(aSigmoid_0_5_60, 9) + " vs. aSigmoid_1_0_60: " + Base.BASE10.decimal(aSigmoid_1_0_60, 9) + " vs. aSigRoot_1_0_60: " + Base.BASE10.decimal(aSigRoot_1_0_60, 9));
                System.out.println("aSmoothing90: " + Base.BASE10.decimal(aSmoothing90, 9) + " vs. aApproach90: " + Base.BASE10.decimal(aApproach90, 9) + " vs. aTanh90: " + Base.BASE10.decimal(aTanh90, 9) + "\nvs. aSigmoid_0_2_90: " + Base.BASE10.decimal(aSigmoid_0_2_90, 9) + " vs. aSigmoid_0_5_90: " + Base.BASE10.decimal(aSigmoid_0_5_90, 9) + " vs. aSigmoid_1_0_90: " + Base.BASE10.decimal(aSigmoid_1_0_90, 9) + " vs. aSigRoot_1_0_90: " + Base.BASE10.decimal(aSigRoot_1_0_90, 9));
                System.out.println("aSmoothing10: " + Base.BASE10.decimal(aSmoothing10, 9) + " vs. aApproach10: " + Base.BASE10.decimal(aApproach10, 9) + " vs. aTanh10: " + Base.BASE10.decimal(aTanh10, 9) + "\nvs. aSigmoid_0_2_10: " + Base.BASE10.decimal(aSigmoid_0_2_10, 9) + " vs. aSigmoid_0_5_10: " + Base.BASE10.decimal(aSigmoid_0_5_10, 9) + " vs. aSigmoid_1_0_10: " + Base.BASE10.decimal(aSigmoid_1_0_10, 9) + " vs. aSigRoot_1_0_10: " + Base.BASE10.decimal(aSigRoot_1_0_10, 9));
                aSmoothing60 = ir.apply(aSmoothing60, b, 1.5f / 60f);
                aSmoothing90 = ir.apply(aSmoothing90, b, 1.5f / 90f);
                aSmoothing10 = ir.apply(aSmoothing10, b, 1.5f / 10f);
                aApproach60 = approach(aApproach60, b, 1f / 60f, 0.5f, ir);
                aApproach90 = approach(aApproach90, b, 1f / 90f, 0.5f, ir);
                aApproach10 = approach(aApproach10, b, 1f / 10f, 0.5f, ir);
            }
            System.out.println();
        }
    }

    public static float cbrtNewton0(float y) {
        return BitConversion.intBitsToFloat(0x2a510680 + (BitConversion.floatToIntBits(y) / 3)); // log-approx hack
    }

    public static float cbrtNewton1(float y) {
        float x = BitConversion.intBitsToFloat(0x2a543aa3 + (BitConversion.floatToIntBits(y) / 3)); // log-approx hack
        return 0.652748f * x + 0.347252f * y / (x*x); // newtonian step #1
    }

    public static float cbrtNewton2(float y) {
        float x = BitConversion.intBitsToFloat(0x2a4fcd03 + (BitConversion.floatToIntBits(y) / 3)); // log-approx hack
        x = 0.666182f * x + 0.333818f * y / (x*x); // newtonian step #1
        x = 0.666182f * x + 0.333818f * y / (x*x); // newtonian step #2
        return x;
    }
    public static float cbrtRetry(float cube) {
        int ix = BitConversion.floatToIntBits(cube);
        /*
mean squared error: 0.0000000000000557
mean error: -0.0000000016092396
min error: -0.0000002948072876 at 13.3972063064575200
max error: 0.0000009216367450 at 1.0986944437026978
         */
//        final int sign = (ix & 0x80000000);
//        ix &= 0x7FFFFFFF;
//        ix = (ix >>> 2) + (ix >>> 4);
//        ix += ix >>> 4;
//        ix = ix + (ix >>> 8) + 0x2A5137A0 | sign;
//        float x = BitConversion.intBitsToFloat(ix);
        /*
mean squared error: 0.0000000000000556
mean error: -0.0000000000441931
min error: -0.0000002959369270 at 0.2093089073896408
max error: 0.0000009699978846 at 0.2499718964099884
         */
        float x = BitConversion.intBitsToFloat((ix & 0x7FFFFFFF) / 3 + 0x2A5137A0 | (ix & 0x80000000));
        /*
mean squared error: 0.0000000000000556
mean error: -0.0000000000562560
min error: -0.0000002959369270 at 0.2093089073896408
max error: 0.0000009699978846 at 0.2499718964099884
         */
//        float x = BitConversion.intBitsToFloat((ix & 0x7FFFFFFF) / 3 + 0x2A51379F | (ix & 0x80000000));
        x = 0.6666667f * x + 0.33333334f * cube / (x * x);
        x = 0.6666665f * x + 0.33333332f * cube / (x * x);
        return x;
    }
    public static float cbrtConfigurable(float cube, int magic, float nx1, float nc1, float nx2, float nc2) {
        int ix = BitConversion.floatToIntBits(cube);
        float x = BitConversion.intBitsToFloat((ix & 0x7FFFFFFF) / 3 + magic | (ix & 0x80000000));
        x = nx1 * x + nc1 * cube / (x * x);
        x = nx2 * x + nc2 * cube / (x * x);
        return x;
    }

    public static void main(String[] args) {
/*
MathTools.cbrt():
mean squared error: 0.0000000000000761
mean error: 0.0000001503098200
min error: -0.0000001338998894 at 1.5752552747726440
max error: 0.0000010528937143 at 8.7879924774169920
 */
        System.out.println("MathTools.cbrt(): ");
        testApprox(3, MathTools::cbrt, 0.0625f, 16f);
        /*
cbrtRetry():
mean squared error: 0.0000000000000556
mean error: -0.0000000000441931
min error: -0.0000002959369270 at 0.2093089073896408
max error: 0.0000009699978846 at 0.2499718964099884
         */
        System.out.println("cbrtRetry(): ");
        testApprox(3, MathToolsTest::cbrtRetry, 0.0625f, 16f);
//        System.out.println("cbrtNewton0(): ");
//        testApprox(3, MathToolsTest::cbrtNewton0, 0.0625f, 16f);
//        System.out.println("cbrtNewton1(): ");
//        testApprox(3, MathToolsTest::cbrtNewton1, 0.0625f, 16f);
//        System.out.println("cbrtNewton2(): ");
//        testApprox(3, MathToolsTest::cbrtNewton2, 0.0625f, 16f);

        System.out.println();

        double bestError = Float.MAX_VALUE;
        int bestM = -33, bestX1 = -11, bestC1 = -11, bestX2 = -11, bestC2 = -11;
        for (int mSteps = -16; mSteps <= 16; mSteps++) {
            for (int x1 = -6; x1 <= 6; x1++) {
                for (int c1 = -6; c1 <= 6; c1++) {
                    for (int x2 = -6; x2 <= 6; x2++) {
                        double lastImprovedError = Float.MAX_VALUE;
                        for (int c2 = -6; c2 <= 20; c2++) {
                            System.out.printf("mSteps=%d, x1=%d, c1=%d, x2=%d, c2=%d:\n", mSteps, x1, c1, x2, c2);
                            int finalMSteps = (0x2A5137A0 + mSteps);
                            float finalX = MathTools.towardsZero(0.6666666f, -x1);
                            float finalC = MathTools.towardsZero(0.33333333f, -c1);
                            float finalX1 = MathTools.towardsZero(0.6666666f, -x2);
                            float finalC1 = MathTools.towardsZero(0.33333333f, -c2);
                            double meanSquareError = testApprox(3, (f -> cbrtConfigurable(f, finalMSteps, finalX, finalC, finalX1, finalC1)), 0.0625f, 16f);
                            if(meanSquareError > lastImprovedError) break;
                            lastImprovedError = meanSquareError;
                            if(meanSquareError < bestError){
                                bestError = meanSquareError;
                                bestM = mSteps;
                                bestX1 = x1;
                                bestC1 = c1;
                                bestX2 = x2;
                                bestC2 = c2;
                            }
                            System.out.printf("Current best mean squared error: %.16f\n", bestError);
                        }
                    }
                }
            }
            System.out.println("Completed step " + (mSteps + 32) + "/65");
        }
        System.out.printf("\n\nBEST:\nMean squared error: %.16f\nmSteps=%d, x1=%d, c1=%d, x2=%d, c2=%d:\n", bestError, bestM, bestX1, bestC1, bestX2, bestC2);

    }

    public static double testApprox(int inversePower, PrecisionTest.FloatUnaryOperator approx, float minTest, float maxTest) {
        int ib = BitConversion.floatToRawIntBits(minTest);
        int ie = BitConversion.floatToRawIntBits(maxTest);

//        float sum_error = 0.0f, sum_sq_error = 0.0f,
//                min_error     = 1e20f, max_error     = -1e20f,
//                min_error_arg = 0.0f, max_error_arg = 0.0f;

        double sum_error = 0.0, sum_sq_error = 0.0,
                min_error     = 1e20, max_error     = -1e20;
        float min_error_arg = 0.0f, max_error_arg = 0.0f;
        for (int i = ib; i <= ie; ++i)
        {
            float y = BitConversion.intBitsToFloat(i);
            double x = Math.pow(y, 1.0/inversePower);
            double error = (approx.applyAsFloat(y) - x) / x;
            sum_error += error;
            sum_sq_error += error*error;
            if (error < min_error) {min_error = error; min_error_arg = y;}
            if (error > max_error) {max_error = error; max_error_arg = y;}
        }
        double samples = (ie - ib + 1);

        System.out.printf("mean squared error: %.16f\n" +
                        "mean error: %.16f\n" +
                        "min error: %.16f at %.16f\n" +
                        "max error: %.16f at %.16f\n",
                sum_sq_error /= samples,
                sum_error / samples,
                min_error, min_error_arg,
                max_error, max_error_arg);
        return sum_sq_error;

    }
    // from root-cellar, https://github.com/EvanBalster/root-cellar/blob/master/root_cellar.h , Apache-licensed
    /*
	template<int ROOT_INDEX, typename T_Approx, typename T_Float>
	inline PowApprox_Stats Test_Root_Approx(
		const T_Approx &approx,
		T_Float range_min,
		T_Float range_max)
	{
		using float_t = T_Float;
		using int_t = float_as_int_t<float_t>;
		int_t
			ib = reinterpret_float_int(range_min),
			ie = reinterpret_float_int(range_max);

		// Measurements...
		using measure_t = float_t;
		measure_t sum_error = 0.0, sum_sq_error = 0.0,
			min_error     = 1e20, max_error     = -1e20,
			min_error_arg = 0.0, max_error_arg = 0.0;
		for (int i = ib; i <= ie; ++i)
		{
			float_t y = reinterpret_int_float(i), x = root_i<ROOT_INDEX>(y);
			measure_t error = (approx(y) - x) / x;
			sum_error += error;
			sum_sq_error += error*error;
			if (error < min_error) {min_error = error; min_error_arg = y;}
			if (error > max_error) {max_error = error; max_error_arg = y;}
		}
		double samples = double(ie - ib);
		return {
			sum_sq_error / samples,
			sum_error / samples,
			min_error, min_error_arg,
			max_error, max_error_arg};
	}
     */
}
