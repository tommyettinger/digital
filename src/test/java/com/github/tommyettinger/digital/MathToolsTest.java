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

    @Test
    public void testApproach() {
        float a = 0f, b = 100f;
        float aSmoothing60 = a, aApproach60 = a, aTanh60 = a;
        float aSmoothing90 = a, aApproach90 = a, aTanh90 = a;
        float aSmoothing10 = a, aApproach10 = a, aTanh10 = a;
        for (int i = 0; i <= 180; i++) {
            float time60 = i / 60f;
            float time90 = i / 90f;
            float time10 = i / 10f;
            aTanh60 = MathTools.lerp(a, b, RoughMath.tanhRough(time60 * 1.1f));
            aTanh90 = MathTools.lerp(a, b, RoughMath.tanhRough(time90 * 1.1f));
            aTanh10 = MathTools.lerp(a, b, RoughMath.tanhRough(time10 * 1.1f));
            System.out.println("Iteration " + i + ", time60: " + time60  + ", time90: " + time90  + ", time10: " + time10);
            System.out.println("aSmoothing60: " + Base.BASE10.decimal(aSmoothing60, 10) + " vs. aApproach60: " + Base.BASE10.decimal(aApproach60, 10) + " vs. aTanh60: " + Base.BASE10.decimal(aTanh60, 10));
            System.out.println("aSmoothing90: " + Base.BASE10.decimal(aSmoothing90, 10) + " vs. aApproach90: " + Base.BASE10.decimal(aApproach90, 10) + " vs. aTanh90: " + Base.BASE10.decimal(aTanh90, 10));
            System.out.println("aSmoothing10: " + Base.BASE10.decimal(aSmoothing10, 10) + " vs. aApproach10: " + Base.BASE10.decimal(aApproach10, 10) + " vs. aTanh10: " + Base.BASE10.decimal(aTanh10, 10));
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
            float aSmoothing60 = a, aApproach60 = a, aTanh60 = a;
            float aSmoothing90 = a, aApproach90 = a, aTanh90 = a;
            float aSmoothing10 = a, aApproach10 = a, aTanh10 = a;
            for (int i = 0; i <= 180; i++) {
                float time60 = i / 60f;
                float time90 = i / 90f;
                float time10 = i / 10f;
                aTanh60 = ir.apply(a, b, RoughMath.tanhRough(time60 * 1.1f));
                aTanh90 = ir.apply(a, b, RoughMath.tanhRough(time90 * 1.1f));
                aTanh10 = ir.apply(a, b, RoughMath.tanhRough(time10 * 1.1f));
                System.out.println("Using " + ir.tag + ", Iteration " + i + ", time60: " + time60 + ", time90: " + time90 + ", time10: " + time10);
                System.out.println("aSmoothing60: " + Base.BASE10.decimal(aSmoothing60, 10) + " vs. aApproach60: " + Base.BASE10.decimal(aApproach60, 10) + " vs. aTanh60: " + Base.BASE10.decimal(aTanh60, 10));
                System.out.println("aSmoothing90: " + Base.BASE10.decimal(aSmoothing90, 10) + " vs. aApproach90: " + Base.BASE10.decimal(aApproach90, 10) + " vs. aTanh90: " + Base.BASE10.decimal(aTanh90, 10));
                System.out.println("aSmoothing10: " + Base.BASE10.decimal(aSmoothing10, 10) + " vs. aApproach10: " + Base.BASE10.decimal(aApproach10, 10) + " vs. aTanh10: " + Base.BASE10.decimal(aTanh10, 10));
                aSmoothing60 = ir.apply(aSmoothing60, b, 1.5f / 60f);
                aSmoothing90 = ir.apply(aSmoothing90, b, 1.5f / 90f);
                aSmoothing10 = ir.apply(aSmoothing10, b, 1.5f / 10f);
                aApproach60 = approach(aApproach60, b, 1f / 60f, 0.5f, ir);
                aApproach90 = approach(aApproach90, b, 1f / 90f, 0.5f, ir);
                aApproach10 = approach(aApproach10, b, 1f / 10f, 0.5f, ir);
            }
        }
    }
}
