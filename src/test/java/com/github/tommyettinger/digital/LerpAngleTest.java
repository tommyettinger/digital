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
        for (float a = 0.05f; a <= 1f; a += 0.1f) {
            for (float l : left) {
                for (float r : right) {
                    Assert.assertEquals("l: " + l + ", r: " + r + ", a: " + a, MathUtils.lerpAngleDeg(l * 360, r * 360, a), MathTools.lerpAngleTurns(l, r, (double)a) * 360, 0.01);
                }
            }
        }
    }
}
