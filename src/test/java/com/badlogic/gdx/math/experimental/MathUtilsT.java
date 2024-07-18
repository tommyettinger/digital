package com.badlogic.gdx.math.experimental;

import com.github.tommyettinger.digital.TrigTools;

public final class MathUtilsT {
    private MathUtilsT() {}

    public static float sin(float t) {
        return TrigTools.sinSmoother(t);
    }

    public static float cos(float t) {
        return TrigTools.cosSmoother(t);
    }

    public static float atan2(float y, float x) {
        return TrigTools.atan2(y, x);
    }
}
