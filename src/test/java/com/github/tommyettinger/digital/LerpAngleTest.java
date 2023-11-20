package com.github.tommyettinger.digital;

import com.badlogic.gdx.math.MathUtils;
import org.junit.Assert;
import org.junit.Test;

public class LerpAngleTest {
    public static final float[] left  = {-2.9f, -2.1f, -1.9f, -1.1f, -0.9f, -0.1f, 0.1f, 0.9f, 1.1f, 1.9f, 2.1f, 2.9f};
    public static final float[] right = {-2.7f, -2.2f, -1.7f, -1.2f, -0.7f, -0.2f, 0.2f, 0.7f, 1.2f, 1.7f, 2.2f, 2.7f};
    @Test
    public void testTurnsFloat() {
        for (float a = 0.05f; a <= 1f; a += 0.123f) {
            for (float l : left) {
                for (float r : right) {
                    Assert.assertEquals("l: " + l + ", r: " + r + ", a: " + a, MathUtils.lerpAngleDeg(l * 360, r * 360, a), MathTools.lerpAngleTurns(l, r, a) * 360, 0.01f);
                }
            }
        }
    }
    @Test
    public void testTurnsDouble() {
        for (float a = 0.05f; a <= 1f; a += 0.123f) {
            for (float l : left) {
                for (float r : right) {
                    Assert.assertEquals("l: " + l + ", r: " + r + ", a: " + a, MathUtils.lerpAngleDeg(l * 360, r * 360, a), MathTools.lerpAngleTurns(l, r, (double)a) * 360, 0.01);
                }
            }
        }
    }
    @Test
    public void testDegreesFloat() {
        for (float a = 0.05f; a <= 1f; a += 0.123f) {
            for (float lb : left) {
                float l = lb * 360;
                for (float rb : right) {
                    float r = rb * 360;
                    Assert.assertEquals("l: " + l + ", r: " + r + ", a: " + a, MathUtils.lerpAngleDeg(l, r, a), MathTools.lerpAngleDeg(l, r, a), 0.01f);
                }
            }
        }
    }
    @Test
    public void testDegreesDouble() {
        for (float a = 0.05f; a <= 1f; a += 0.123f) {
            for (float lb : left) {
                float l = lb * 360;
                for (float rb : right) {
                    float r = rb * 360;
                    Assert.assertEquals("l: " + l + ", r: " + r + ", a: " + a, MathUtils.lerpAngleDeg(l, r, a), MathTools.lerpAngleDeg(l, r, (double)a), 0.01);
                }
            }
        }
    }

    /**
     * Linearly interpolates between two angles in degrees. Takes into account that angles wrap at 360 degrees and
     * always takes the direction with the smallest delta angle.
     *
     * @param fromDegrees start angle in degrees
     * @param toDegrees   target angle in degrees
     * @param progress    interpolation value in the range [0, 1]
     * @return the interpolated angle in the range [0, 360)
     */
    public static float lerpAngleDegAlt(float fromDegrees, float toDegrees, float progress) {
//        float delta = ((toDegrees - fromDegrees)) % 360f;
//        return ((fromDegrees + delta * progress) % 360f + 360f) % 360f;

        float d = toDegrees - fromDegrees;
        d = fromDegrees + progress * ((d % 360f + 360f + 180f) % 360f - 180f);
        return (d % 360f + 360f) % 360f;
    }

    @Test
    public void testDegreesAltFloat() {
        for (float a = 0.05f; a <= 1f; a += 0.123f) {
            for (float lb : left) {
                float l = lb * 360;
                for (float rb : right) {
                    float r = rb * 360;
                    Assert.assertEquals("l: " + l + ", r: " + r + ", a: " + a, MathUtils.lerpAngleDeg(l, r, a), lerpAngleDegAlt(l, r, a), 0.01f);
                }
            }
        }
    }
}
