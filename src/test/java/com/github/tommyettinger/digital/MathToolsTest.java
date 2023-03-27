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

}
