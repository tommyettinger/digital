package com.github.tommyettinger.digital;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.tommyettinger.digital.v037.TrigTools037;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import static com.github.tommyettinger.digital.TrigTools.*;
import static java.lang.Math.abs;

// REMOVE the @Ignore if you want to run any tests! They take a while to run as a whole, though.
//@Ignore
public class PrecisionTest {

    public static final int PI_BITS = Float.floatToIntBits(TrigTools.PI);
    public static final int PI2BITS = Float.floatToIntBits(TrigTools.PI2);

    /**
     * TrigTools.atan2 :
     * Absolute error:   0.00000105
     * Relative error:   0.00000961
     * Maximum error :   0.00000180
     * Worst result  :   2.02927470
     * True result   :   2.02927651
     * Worst position:   -0.21211123,0.42976010
     * Took 13.4951845 s
     *
     * Math.atan2 :
     * Absolute error:   0.00000003
     * Relative error:   0.00000002
     * Maximum error :   0.00000012
     * Worst result  :   2.21243620
     * True result   :   2.21243608
     * Worst position:   -0.14016950,0.18761921
     * Took 26.3899404 s
     *
     * PrecisionTest.atan2Gilcher :
     * Absolute error:   0.00000003
     * Relative error:   0.00000003
     * Maximum error :   0.00006103
     * Worst result  :   -2.35613346
     * True result   :   -2.35619449
     * Worst position:   -0.00000191,-0.00000191
     * Took 78.41480870000001 s
     *
     * PrecisionTest.atan2Gilcher2 :
     * Absolute error:   0.05835590
     * Relative error:   2.18065146
     * Maximum error :   0.37079254
     * Worst result  :   -2.77079630
     * True result   :   -3.14158884
     * Worst position:   -0.50000000,-0.00000191
     * Took 13.8825128 s
     *
     * PrecisionTest.atan2Gilcher3 :
     * Absolute error:   0.03017152
     * Relative error:   1.54958105
     * Maximum error :   0.26776221
     * Worst result  :   -0.26776603
     * True result   :   -0.00000381
     * Worst position:   0.49999619,-0.00000191
     * Took 13.900070900000001 s
     *
     * PrecisionTest.atan2GilcherHand :
     * Absolute error:   0.00002225
     * Relative error:   0.00001983
     * Maximum error :   0.00007367
     * Worst result  :   -2.35612082
     * True result   :   -2.35619449
     * Worst position:   -0.00000191,-0.00000191
     * Took 14.033064000000001 s
     *
     * PrecisionTest.atan2GilcherRuud :
     * Absolute error:   0.00923482
     * Relative error:   0.01912464
     * Maximum error :   0.01676460
     * Worst result  :   3.12210035
     * True result   :   3.10533576
     * Worst position:   -0.35093868,0.01272953
     * Took 13.922539 s
     *
     * PrecisionTest.atan2GilcherTT :
     * Absolute error:   0.00002225
     * Relative error:   0.00001983
     * Maximum error :   0.00007367
     * Worst result  :   -2.35612082
     * True result   :   -2.35619449
     * Worst position:   -0.00000191,-0.00000191
     * Took 13.956294000000002 s
     *
     * PrecisionTest.atan2GilcherA :
     * Absolute error:   0.04317880
     * Relative error:   0.06340295
     * Maximum error :   0.07168073
     * Worst result  :   2.76060724
     * True result   :   2.83228798
     * Worst position:   -0.00018466,0.00005901
     * Took 13.9918274 s
     *
     * LATER...
     *
     * TrigTools.atan2 :
     * Absolute error:   0.00000105
     * Relative error:   0.00000310
     * Maximum error :   0.00000180
     * Worst result  :   2.02955461
     * True result   :   2.02955641
     * Worst position:   -0.66515315,1.34672165
     * Took 60.0815083 s
     *
     * Math.atan2 :
     * Absolute error:   0.00000005
     * Relative error:   0.00000002
     * Maximum error :   0.00000012
     * Worst result  :   2.49713755
     * True result   :   2.49713743
     * Worst position:   -1.67274380,1.25705338
     * Took 89.7106749 s
     *
     * PrecisionTest.atan2Jolt (double) :
     * Absolute error:   0.00000005
     * Relative error:   0.00000002
     * Maximum error :   0.00000013
     * Worst result  :   -2.74888802
     * True result   :   -2.74888789
     * Worst position:   -1.76372182,-0.73056924
     * Took 84.9840113 s
     *
     * PrecisionTest.atan2Jolt (float) :
     * Absolute error:   0.00000007
     * Relative error:   0.00000003
     * Maximum error :   0.00000030
     * Worst result  :   -2.14120007
     * True result   :   -2.14119977
     * Worst position:   -0.43988597,-0.68567419
     * Took 60.416935 s
     */
    @Test
    public void testAtan2() {
        LinkedHashMap<String, FloatBinaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("TrigTools.atan2", TrigTools::atan2);
        functions.put("Math.atan2", (y, x) -> (float) Math.atan2(y, x));
        functions.put("PrecisionTest.atan2Jolt (double)", (y1, x1) -> (float)atan2Jolt((double) y1, (double) x1));
        functions.put("PrecisionTest.atan2Jolt (float)", PrecisionTest::atan2Jolt);
//        functions.put("PrecisionTest.atan2Gilcher", PrecisionTest::atan2Gilcher);
//        functions.put("PrecisionTest.atan2Gilcher2", PrecisionTest::atan2Gilcher2);
//        functions.put("PrecisionTest.atan2Gilcher3", PrecisionTest::atan2Gilcher3);
//        functions.put("PrecisionTest.atan2GilcherHand", PrecisionTest::atan2GilcherHand);
//        functions.put("PrecisionTest.atan2GilcherRuud", PrecisionTest::atan2GilcherRuud);
//        functions.put("PrecisionTest.atan2GilcherTT", PrecisionTest::atan2GilcherTT);
//        functions.put("PrecisionTest.atan2GilcherA", PrecisionTest::atan2GilcherA);
        for (Map.Entry<String, FloatBinaryOperator> entry : functions.entrySet()) {
            FloatBinaryOperator func = entry.getValue();
            double absError = 0.0, relError = 0.0, maxError = 0.0, shouldBe = 0.0, worstResult = 0.0;
            float worstY = 0, worstX = 0;
            long counter = 0L;
            long time = System.nanoTime();
            for (int i = Float.floatToIntBits(0.25f), n = Float.floatToIntBits(4f); i < n; i += 511) {
                float x = Float.intBitsToFloat(i) - 2.125f;
                for (int j = Float.floatToIntBits(1f); j < n; j += 511) {
                    float y = Float.intBitsToFloat(j) - 2.125f;
                    double tru = Math.atan2(y, x),
                            result = func.applyAsFloat(y, x),
                            err = result - tru,
                            ae = abs(err);
                    relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
                    absError += ae;
                    if (maxError != (maxError = Math.max(maxError, ae))) {
                        worstX = x;
                        worstY = y;
                        worstResult = result;
                        shouldBe = tru;
                    }
                    counter++;
                }
            }
            System.out.printf("\n%s :\n" +
                    "Absolute error:   %3.8f\n" +
                    "Relative error:   %3.8f\n" +
                    "Maximum error :   %3.8f\n" +
                    "Worst result  :   %3.8f\n" +
                    "True result   :   %3.8f\n" +
                    "Worst position:   %3.8f,%3.8f\n", entry.getKey(), absError / counter, relError / counter, maxError, worstResult, shouldBe, worstX, worstY);
            System.out.println("Took " + (System.nanoTime() - time) * 1E-9 + " s");
        }
    }
    public static double acosTT(double a) {
        double a2 = a * a; // a squared
        double a3 = a * a2; // a cubed
        if (a >= 0.0) {
            return Math.sqrt(1.0 - a) * (1.5707288 - 0.2121144 * a + 0.0742610 * a2 - 0.0187293 * a3);
        }
        return Math.PI
                - Math.sqrt(1.0 + a) * (1.5707288 + 0.2121144 * a + 0.0742610 * a2 + 0.0187293 * a3);
    }
    public static double acos2(double a) {
        return 6 * a / (a * a - 6) + HALF_PI_D;
    }
    public static double acos3(double a) {
        return (60 * PI_D + a * (-120 + a * (-27 * PI_D + 34 * a)))/(120 - 54 * a * a);
        //(34 x^3 - 27 π x^2 - 120 x + 60 π)/(120 - 54 x^2)
        //(60 Pi - 120 x - 27 Pi x^2 + 34 x^3)/(120 - 54 x^2)
    }
    public static double acosHand(double a) {
        double x = abs(a);
        double ret = -0.0187293;
        ret = ret * x;
        ret = ret + 0.0742610;
        ret = ret * x;
        ret = ret - 0.2121144;
        ret = ret * x;
        ret = ret + 1.5707288;
        ret = ret * Math.sqrt(1.0-x);
        return (a >= 0) ? ret : 3.14159265358979 - ret;
    }

    /**
     * <a href="https://stackoverflow.com/a/36387954">From here</a>.
     * @param x
     * @return
     */
    public static double acosRuud(double x) {
        final double a = -0.939115566365855;
        final double b =  0.9217841528914573;
        final double c = -1.2845906244690837;
        final double d =  0.295624144969963174;
        final double x2 = x * x;
        final double x3 = x * x2;
        final double x4 = x2 * x2;
        return HALF_PI_D + (a * x + b * x3) / (1.0 + c * x2 + d * x4);
                //acos(x) ≈ π/2 + (ax + bx³) / (1 + cx² + dx⁴)
    }

    public static float acosFastGilcher(float x) {
        float o = -0.156583f * Math.abs(x) + HALF_PI;
        o *= Math.sqrt(1f - abs(x));
        return x > 0.0 ? o : PI - o;
//        return Math.copySign(o - HALF_PI, x) + HALF_PI;
    }

    /**
     * "newfastatan2" by P. Gilcher, <a href="https://www.shadertoy.com/view/flSXRV">see ShaderToy</a>.
     * MIT licensed.
     * @param y
     * @param x
     * @return
     */
    public static float atan2Gilcher(double y, double x) {
        double dot = x * x + y * y + 0x1p-50;
//        if(MathTools.isZero(dot, 1E-10)) return (float)Math.copySign(Math.abs(x) < Math.abs(y) ? HALF_PI_D : Math.copySign(HALF_PI_D, x) - HALF_PI_D, y);//, 6.3E-5
        double cat = x / Math.sqrt(dot);
        double t = Math.acos(cat);
        return (float) Math.copySign(t, y);
    }

    /**
     * "newfastatan2" by P. Gilcher, <a href="https://www.shadertoy.com/view/flSXRV">see ShaderToy</a>.
     * MIT licensed.
     * @param y
     * @param x
     * @return
     */
    public static float atan2GilcherA(float y, float x) {
        float cosatan2 = x / (abs(x) + abs(y) + 0x1p-23f);
        float t = HALF_PI - cosatan2 * HALF_PI;
        return (float) Math.copySign(t, y);
    }
    public static float atan2GilcherTT(double y, double x) {
        double dot = x * x + y * y + 0x1p-50;
        double cat = x / Math.sqrt(dot);
        double t = acosTT(cat);
        return (float) Math.copySign(t, y);
    }
    public static float atan2Gilcher2(double y, double x) {
        double dot = x * x + y * y + 0x1p-50;
        double cat = x / Math.sqrt(dot);
        double t = acos2(cat);
        return (float) Math.copySign(t, y);
    }
    public static float atan2Gilcher3(double y, double x) {
        double dot = x * x + y * y + 0x1p-50;
        double cat = x / Math.sqrt(dot);
        double t = acos3(cat);
        return (float) Math.copySign(t, y);
    }
    public static float atan2GilcherHand(double y, double x) {
        double dot = x * x + y * y + 0x1p-50;
        double cat = x / Math.sqrt(dot);
        double t = acosHand(cat);
        return (float) Math.copySign(t, y);
    }
    public static float atan2GilcherRuud(double y, double x) {
        double dot = x * x + y * y + 0x1p-50;
        double cat = x / Math.sqrt(dot);
        double t = acosRuud(cat);
        return (float) Math.copySign(t, y);
    }

    @Test
    @Ignore
    public void testAtan2Deg(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double tru = Math.toDegrees(Math.atan2(y, x)),
                        err = TrigTools.atan2Deg(y, x) - tru,
                        ae = abs(err);
                relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
                absError += ae;
                if(maxError != (maxError = Math.max(maxError, ae))){
                    worstX = x;
                    worstY = y;
                }
                counter++;
            }
        }
        System.out.printf("Absolute error:   %3.8f\n" +
                          "Relative error:   %3.8f\n" +
                          "Maximum error:    %3.8f\n" +
                          "Worst position:   %3.8f,%3.8f\n", absError / counter, relError / counter, maxError, worstX, worstY);
    }
    @Test
    @Ignore
    public void testAtan2Turns(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double tru = (Math.atan2(y, x) / 2.0 / Math.PI);
                if(tru < 0.0) tru += 1.0;
                double err = TrigTools.atan2Turns(y, x) - tru,
                        ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));

                absError += ae;
                if(maxError != (maxError = Math.max(maxError, ae))){
                    worstX = x;
                    worstY = y;
                }
                counter++;
            }
        }
        System.out.printf("Absolute error:   %3.8f\n" +
                          "Relative error:   %3.8f\n" +
                          "Maximum error:    %3.8f\n" +
                          "Worst position:   %3.8f,%3.8f\n", absError / counter, relError / counter, maxError, worstX, worstY);
    }
    @Test
    @Ignore
    public void testAtan2Deg360(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double tru = Math.toDegrees(Math.atan2(y, x));
                if(tru < 0.0) tru += 360.0;
                double err = TrigTools.atan2Deg360(y, x) - tru,
                        ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));

                absError += ae;
                if(maxError != (maxError = Math.max(maxError, ae))){
                    worstX = x;
                    worstY = y;
                }
                counter++;
            }
        }
        System.out.printf("Absolute error:   %3.8f\n" +
                          "Relative error:   %3.8f\n" +
                          "Maximum error:    %3.8f\n" +
                          "Worst position:   %3.8f,%3.8f\n", absError / counter, relError / counter, maxError, worstX, worstY);
    }

    @Test
    public void testSinSmooth(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

            double tru = (float) Math.sin(x),
                    err = sinOldSmooth(x) - tru,
                    ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
            absError += ae;
            if (maxError != (maxError = Math.max(maxError, ae))) {
                worstX = x;
            }
            ++counter;
        }
        System.out.printf(
                "Absolute error:   %3.8f\n" +
                        "Relative error:   %3.8f\n" +
                        "Maximum error:    %3.8f\n" +
                        "Worst input:      %3.8f\n" +
                        "Worst approx output: %3.8f\n" +
                        "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, sinOldSmooth(worstX), (float)Math.sin(worstX));
    }

    @Test
    public void testSinBhaskaroid(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

            double tru = (float) Math.sin(x),
                    err = sinBhaskaroid(x) - tru,
                    ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
            absError += ae;
            if (maxError != (maxError = Math.max(maxError, ae))) {
                worstX = x;
            }
            ++counter;
        }
        System.out.printf(
                        "Mean absolute error: %3.10f\n" +
                        "Mean relative error: %3.10f\n" +
                        "Maximum error:       %3.10f\n" +
                        "Worst input:         %3.10f\n" +
                        "Worst approx output: %3.10f\n" +
                        "Correct output:      %3.10f\n", absError / counter, relError / counter, maxError, worstX, sinBhaskaroid(worstX), (float)Math.sin(worstX));
    }

    @Test
    public void testSinNewTable(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

            double tru = (float) Math.sin(x),
                    err = sinNewTable(x) - tru,
                    ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
            absError += ae;
            if (maxError != (maxError = Math.max(maxError, ae))) {
                worstX = x;
            }
            ++counter;
        }
        System.out.printf(
                "Absolute error:   %3.8f\n" +
                        "Relative error:   %3.8f\n" +
                        "Maximum error:    %3.8f\n" +
                        "Worst input:      %3.8f\n" +
                        "Worst approx output: %3.8f\n" +
                        "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, sinNewTable(worstX), (float)Math.sin(worstX));
    }

    @Test
    public void testSinNewTable2(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

            double tru = (float) Math.sin(x),
                    err = sinNewTable2(x) - tru,
                    ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
            absError += ae;
            if (maxError != (maxError = Math.max(maxError, ae))) {
                worstX = x;
            }
            ++counter;
        }
        System.out.printf(
                "Absolute error:   %3.8f\n" +
                        "Relative error:   %3.8f\n" +
                        "Maximum error:    %3.8f\n" +
                        "Worst input:      %3.8f\n" +
                        "Worst approx output: %3.8f\n" +
                        "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, sinNewTable2(worstX), (float)Math.sin(worstX));
    }

    @Test
    public void testSinOldTable(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

            double tru = (float) Math.sin(x),
                    err = sinOldTable(x) - tru,
                    ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
            absError += ae;
            if (maxError != (maxError = Math.max(maxError, ae))) {
                worstX = x;
            }
            ++counter;
        }
        System.out.printf(
                "Absolute error:   %3.8f\n" +
                        "Relative error:   %3.8f\n" +
                        "Maximum error:    %3.8f\n" +
                        "Worst input:      %3.8f\n" +
                        "Worst approx output: %3.8f\n" +
                        "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, sinOldTable(worstX), (float)Math.sin(worstX));
    }

    @FunctionalInterface
    public interface FloatUnaryOperator {
        float applyAsFloat(float x);
    }
    @FunctionalInterface
    public interface FloatBinaryOperator {
        float applyAsFloat(float a, float b);
    }

    /**
     * Testing from PI2 to -PI2 in decrements of 0x1p-20f...
     * <br>
     * Low-precision approximations:
     * <pre>
     * Running sinOldTable
     * Mean absolute error:     0.0001522401
     * Mean relative error:     0.0023401673
     * Maximum abs. error:      0.0005752884
     * Maximum rel. error:   3809.6450195313
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.282993793487549000000000
     * Best output (lo):       -0.0001915137 (0xB948D111)
     * Correct output (lo):    -0.0001915137 (0xB948D111)
     * Worst input (hi):       -3.141592502593994000000000
     * Highest output rel:   3809.6447753906
     * Worst output (hi):      -0.0005753914 (0xBA16D5DD)
     * Correct output (hi):    -0.0000001510 (0xB4222169)
     * Worst input (abs):      -6.280500888824463000000000
     * Worst output (abs):      0.0032597035 (0x3B55A0C0)
     * Correct output (abs):    0.0026844151 (0x3B2FED03)
     * Running sin037Table
     * Mean absolute error:     0.0001203808
     * Mean relative error:     0.0020755415
     * Maximum abs. error:      0.0003835417
     * Maximum rel. error:   2538.7736816406
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.648884296417236000000000
     * Best output (lo):       -0.5926146507 (0xBF17B598)
     * Correct output (lo):    -0.5926146507 (0xBF17B598)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:   2538.7736816406
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       6.280500888824463000000000
     * Worst output (abs):     -0.0030679568 (0xBB490FC6)
     * Correct output (abs):   -0.0026844151 (0xBB2FED03)
     * Running sinNewTable
     * Mean absolute error:     0.0000601880
     * Mean relative error:     0.0006230396
     * Maximum abs. error:      0.0001917388
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       3.131813526153564500000000
     * Worst output (abs):      0.0095872330 (0x3C1D13C5)
     * Correct output (abs):    0.0097789718 (0x3C2037FB)
     * Running sinFF
     * Mean absolute error:     0.0000601880
     * Mean relative error:     0.0006230396
     * Maximum abs. error:      0.0001917388
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       3.131813526153564500000000
     * Worst output (abs):      0.0095872330 (0x3C1D13C5)
     * Correct output (abs):    0.0097789718 (0x3C2037FB)
     * Running sinShifty
     * Mean absolute error:     0.0000601960
     * Mean relative error:     0.0006447678
     * Maximum abs. error:      0.0005745887
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):      -0.000574588775634765600000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):   -0.0005745887 (0xBA169FFF)
     * Running sinBonus
     * Mean absolute error:     0.0000907262
     * Mean relative error:     0.0013683814
     * Maximum abs. error:      0.0003835417
     * Maximum rel. error:   2538.7736816406
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):       -3.141592502593994000000000
     * Highest output rel:   2538.7736816406
     * Worst output (hi):      -0.0003834952 (0xB9C90FDA)
     * Correct output (hi):    -0.0000001510 (0xB4222169)
     * Worst input (abs):      -6.280500888824463000000000
     * Worst output (abs):      0.0030679568 (0x3B490FC6)
     * Correct output (abs):    0.0026844151 (0x3B2FED03)
     * Running sinRound
     * Mean absolute error:     0.0000601881
     * Mean relative error:     0.0006230407
     * Maximum abs. error:      0.0001921654
     * Maximum rel. error:      1.0024425983
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):       -6.282993793487549000000000
     * Highest output rel:      1.0024424791
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0001915137 (0x3948D111)
     * Worst input (abs):      -6.269955158233643000000000
     * Worst output (abs):      0.0134219285 (0x3C5BE7A6)
     * Correct output (abs):    0.0132297631 (0x3C58C1A6)
     * Running sinGdx
     * Mean absolute error:     0.0001522401
     * Mean relative error:     0.0023401673
     * Maximum abs. error:      0.0005752884
     * Maximum rel. error:   3809.6450195313
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.282993793487549000000000
     * Best output (lo):       -0.0001915137 (0xB948D111)
     * Correct output (lo):    -0.0001915137 (0xB948D111)
     * Worst input (hi):       -3.141592502593994000000000
     * Highest output rel:   3809.6447753906
     * Worst output (hi):      -0.0005753914 (0xBA16D5DD)
     * Correct output (hi):    -0.0000001510 (0xB4222169)
     * Worst input (abs):      -6.280500888824463000000000
     * Worst output (abs):      0.0032597035 (0x3B55A0C0)
     * Correct output (abs):    0.0026844151 (0x3B2FED03)
     * Running sinFloaty
     * Mean absolute error:     0.0000601882
     * Mean relative error:     0.0006230420
     * Maximum abs. error:      0.0001921714
     * Maximum rel. error:      1.0024425983
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):       -6.282993793487549000000000
     * Highest output rel:      1.0024424791
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0001915137 (0x3948D111)
     * Worst input (abs):       3.148686885833740200000000
     * Worst output (abs):     -0.0072863442 (0xBBEEC249)
     * Correct output (abs):   -0.0070941728 (0xBBE8763C)
     * Running sinFloatyMP
     * Mean absolute error:     0.0000601881
     * Mean relative error:     0.0006230407
     * Maximum abs. error:      0.0001921654
     * Maximum rel. error:      1.0024425983
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):       -6.282993793487549000000000
     * Highest output rel:      1.0024424791
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0001915137 (0x3948D111)
     * Worst input (abs):      -6.269955158233643000000000
     * Worst output (abs):      0.0134219285 (0x3C5BE7A6)
     * Correct output (abs):    0.0132297631 (0x3C58C1A6)
     * Running sinFloatyHP
     * Mean absolute error:     0.0000601880
     * Mean relative error:     0.0006230396
     * Maximum abs. error:      0.0001917388
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.221826076507568000000000
     * Best output (lo):       -0.0613207370 (0xBD7B2B74)
     * Correct output (lo):    -0.0613207370 (0xBD7B2B74)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       3.131813526153564500000000
     * Worst output (abs):      0.0095872330 (0x3C1D13C5)
     * Correct output (abs):    0.0097789718 (0x3C2037FB)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     * </pre>
     * <br>
     * High-precision approximations:
     * <pre>
     * Running sinSmootherOldTable
     * Mean absolute error:     0.0000001205
     * Mean relative error:     0.0007681165
     * Maximum abs. error:      0.0002874465
     * Maximum rel. error:   1644.0024414063
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.282993793487549000000000
     * Best output (lo):       -0.0001915137 (0xB948D111)
     * Correct output (lo):    -0.0001915137 (0xB948D111)
     * Worst input (hi):       -6.283185482025146500000000
     * Highest output rel:   1644.0023193359
     * Worst output (hi):      -0.0002876214 (0xB996CBE3)
     * Correct output (hi):    -0.0000001748 (0xB43BBD2E)
     * Worst input (abs):      -6.283185482025146500000000
     * Worst output (abs):     -0.0002876214 (0xB996CBE3)
     * Correct output (abs):   -0.0000001748 (0xB43BBD2E)
     * Running sinSmoother037Table
     * Mean absolute error:     0.0000000824
     * Mean relative error:     0.0000013559
     * Maximum abs. error:      0.0000004470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.909108638763428000000000
     * Best output (lo):       -0.3654132187 (0xBEBB1771)
     * Correct output (lo):    -0.3654132187 (0xBEBB1771)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       6.156139850616455000000000
     * Worst output (abs):     -0.1267044097 (0xBE01BECD)
     * Correct output (abs):   -0.1267039627 (0xBE01BEAF)
     * Running sinSmootherNewTable
     * Mean absolute error:     0.0000000891
     * Mean relative error:     0.0000014705
     * Maximum abs. error:      0.0000005439
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.431733608245850000000000
     * Best output (lo):       -0.7522377372 (0xBF4092A7)
     * Correct output (lo):    -0.7522377372 (0xBF4092A7)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       6.219690799713135000000000
     * Worst output (abs):     -0.0634523928 (0xBD81F354)
     * Correct output (abs):   -0.0634518489 (0xBD81F30B)
     * Running sinSmooth
     * Mean absolute error:     0.0001505142
     * Mean relative error:     0.0002476233
     * Maximum abs. error:      0.0003550053
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.759520053863525000000000
     * Best output (lo):       -0.5000575781 (0xBF0003C6)
     * Correct output (lo):    -0.5000575781 (0xBF0003C6)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.208482265472412000000000
     * Worst output (abs):     -0.8753479123 (0xBF6016CD)
     * Correct output (abs):   -0.8757029176 (0xBF602E11)
     * Running sinSmoothly
     * Mean absolute error:     0.0000000824
     * Mean relative error:     0.0000013559
     * Maximum abs. error:      0.0000004470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.909108638763428000000000
     * Best output (lo):       -0.3654132187 (0xBEBB1771)
     * Correct output (lo):    -0.3654132187 (0xBEBB1771)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       6.156139850616455000000000
     * Worst output (abs):     -0.1267044097 (0xBE01BECD)
     * Correct output (abs):   -0.1267039627 (0xBE01BEAF)
     * Running sinSmootherFloatFF
     * Mean absolute error:     0.0000000824
     * Mean relative error:     0.0000013559
     * Maximum abs. error:      0.0000004470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.909108638763428000000000
     * Best output (lo):       -0.3654132187 (0xBEBB1771)
     * Correct output (lo):    -0.3654132187 (0xBEBB1771)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       6.156139850616455000000000
     * Worst output (abs):     -0.1267044097 (0xBE01BECD)
     * Correct output (abs):   -0.1267039627 (0xBE01BEAF)
     * Running sinRough
     * Mean absolute error:     0.0021535610
     * Mean relative error:     0.0024209579
     * Maximum abs. error:      0.0064319372
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.077738285064697000000000
     * Best output (lo):       -0.2040047944 (0xBE50E6A2)
     * Correct output (lo):    -0.2040047944 (0xBE50E6A2)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.713915348052978500000000
     * Worst output (abs):     -1.0064307451 (0xBF80D2B9)
     * Correct output (abs):   -0.9999988079 (0xBF7FFFEC)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     * </pre>
     */
    @Test
    public void testSin() {
//        float[] prior00 = makeTableFloatPrior(0.0);
//        float[] prior05 = makeTableFloatPrior(0.5);
//        float[] table05 = makeTableFloat(0.5);
//        float[] table025 = makeTableFloat(0.25);
//        float[] tablePhi = makeTableFloat(MathTools.GOLDEN_RATIO_INVERSE_D);
//        float[] table0625 = makeTableFloat(0.625);
//        float[] table065625 = makeTableFloat(0.65625);
//        float[] table075 = makeTableFloat(0.75);
//        float[] tableMixed = makeTableFloatMixed();
//        float[] table32 = makeTableFloatVar(32);
//        float[] table64 = makeTableFloatVar(64);
//        float[] table128 = makeTableFloatVar(128);
//        float[] table256 = makeTableFloatVar(256);
//        float[] table1024 = makeTableFloatVar(1024);
//        float[] table4096 = makeTableFloatVar(4096);
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinNewTable", TrigTools::sin);
        functions.put("sinSmootherNewTable", TrigTools::sinSmoother);
        functions.put("sinSmooth", TrigTools::sinSmooth);


//        functions.put("sinOldTable", OldTrigTools::sin);
//        functions.put("sin037Table", TrigTools037::sin);
//        functions.put("sinNewTable", TrigTools::sin);
//        functions.put("sinRound", PrecisionTest::sinRound);
//        functions.put("sinFloaty", PrecisionTest::sinFloaty);
//        functions.put("sinGdx", MathUtils::sin);
//        functions.put("sinFloatyMP", PrecisionTest::sinFloatyMP);
//        functions.put("sinFloatyHP", PrecisionTest::sinFloatyHP);

//        functions.put("sinFloaty8192", PrecisionTest::sinFloaty8192);
//        functions.put("sinFloaty32768", PrecisionTest::sinFloaty32768);
//        functions.put("sinFloaty49152", PrecisionTest::sinFloaty49152);
//        functions.put("sinFF", (radians) -> SIN_TABLE[(int) (radians * radToIndexD + 16384.5) - 16384 & TABLE_MASK]);
//        functions.put("sinCTable", CosTools::sin);
//        functions.put("sinOldShifty", OldTrigTools::sinShifty);
//        functions.put("sinNewShifty", PrecisionTest::sinShifty);
//        functions.put("sinSmootherOldTable", OldTrigTools::sinSmoother);
//        functions.put("sinSmoother037Table", TrigTools037::sinSmoother);
//        functions.put("sinSmootherNewTable", TrigTools::sinSmoother);
//        functions.put("sinSmootherFF", (radians) -> {
//            final double r = radians * radToIndexD;
//            final int floor = (int) (r + 16384.0) - 16384;
//            final int masked = floor & TABLE_MASK;
//            final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
//            return from + (to - from) * ((float)r - floor);
//        });
//        functions.put("sinSmootherFloatFF", (radians) -> {
//                    radians *= radToIndex;
//                    final int floor = (int) (radians + 16384.0) - 16384;
//                    final int masked = floor & TABLE_MASK;
//                    final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
//                    return from + (to - from) * (radians - floor);
//        });
//        functions.put("sinSmootherCTable", CosTools::sinSmoother);
//        functions.put("sinSmooth", TrigTools::sinSmooth);
//        functions.put("sinSmoothly", PrecisionTest::sinSmoothly);
//        functions.put("sinSmoothesque", PrecisionTest::sinSmoothesque);
//        functions.put("sinSmootherAlternate", PrecisionTest::sinSmoother);
//        functions.put("sinTable32", (f) -> sinVar(table32, f));
//        functions.put("sinTable64", (f) -> sinVar(table64, f));
//        functions.put("sinTable128", (f) -> sinVar(table128, f));
//        functions.put("sinTable256", (f) -> sinVar(table256, f));
//        functions.put("sinTable1024", (f) -> sinVar(table1024, f));
//        functions.put("sinTable4096", (f) -> sinVar(table4096, f));
//        functions.put("sinSmootherTable32", (f) -> sinSmootherVar(table32, f));
//        functions.put("sinSmootherTable64", (f) -> sinSmootherVar(table64, f));
//        functions.put("sinSmootherTable128", (f) -> sinSmootherVar(table128, f));
//        functions.put("sinSmootherTable256", (f) -> sinSmootherVar(table256, f));
//        functions.put("sinSmootherTable1024", (f) -> sinSmootherVar(table1024, f));
//        functions.put("sinSmootherTable4096", (f) -> sinSmootherVar(table4096, f));

//        functions.put("sinRough", RoughMath::sinRough);

//        functions.put("sinReallyOld", OldNumberTools::sin);

//        functions.put("sinMixed", (f) -> sinMixed(tableMixed, f));
//        functions.put("sinGreen", PrecisionTest::sinGreen);

//        functions.put("sin00Prior", (f) -> sin(prior00, f));
//        functions.put("sin05Prior", (f) -> sin(prior05, f));
//        functions.put("sin05", (f) -> sin(table05, f));
//        functions.put("sin025", (f) -> sin(table025, f));
//        functions.put("sinPhi", (f) -> sin(tablePhi, f));
//        functions.put("sin0625", (f) -> sin(table0625, f));
//        functions.put("sin065625", (f) -> sin(table065625, f));
//        functions.put("sin075", (f) -> sin(table075, f));

//        functions.put("sinCurve", PrecisionTest::sinCurve);
//        functions.put("sinNick", PrecisionTest::sinNick);
//        functions.put("sinLeibovici", PrecisionTest::sinLeibovici);
//        functions.put("sinSteadman", PrecisionTest::sinSteadman);
//        functions.put("sinBhaskara2", PrecisionTest::sinBhaskaraI);
//        functions.put("sinHastings", PrecisionTest::sinHastings);

        functions.put("sinPade", PrecisionTest::sinPade);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
//            for (int i = 1; i <= 6; i++) {
//                System.out.printf("sin(%d), approximate: %.10f\n", i, op.applyAsFloat(i));
//                System.out.printf("sin(%d), should be:   %.10f\n", i, Math.sin(i));
//            }
//            for (int i = PI2BITS; i >= 0; i--) {
//                float x = Float.intBitsToFloat(i);
            for (float x = PI2; x >= -PI2; x-= 0x1p-20f) {
                float tru = (float) Math.sin(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 1E-10) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 1E-10)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = (float) Math.sin(worstAbsX),
                    highestTru = (float) Math.sin(highestRelX),
                    lowestTru = (float) Math.sin(lowestRelX),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }

    //Mean absolute error:     0.0000001121
    //Mean relative error:     0.0000018874
    //Maximum abs. error:      0.0000008196
    //Maximum rel. error:      1.0000000000
    /**
     * Credit to <a href="https://stackoverflow.com/a/524606">Darius Bacon's Stack Overflow answer</a>.
     * The algorithm is by Hastings, from Approximations For Digital Computers.
     * The use of a triangle wave to reduce the range was my idea. This doesn't use a LUT.
     * @param radians the angle to get the sine of, in radians
     * @return the sine of the given angle
     */
    private static float sinHastings(float radians) {
        radians = radians * (PI_INVERSE * 0.5f) + 0.25f;
        radians = 4f * Math.abs(radians - ((int)(radians + 16384.5) - 16384)) - 1f;
        float r2 = radians * radians;
        return ((((0.00015148419f * r2
                - 0.00467376557f) * r2
                + 0.07968967928f) * r2
                - 0.64596371106f) * r2
                + 1.57079631847f) * radians;
    }

    /**
     * Testing from -360 to 360 in increments of 1E-4f...
     * <pre>
     * Running sinSmootherDeg
     * Mean absolute error:     0.0000000590
     * Mean relative error:     0.0000008347
     * Maximum abs. error:      0.0000003576
     * Maximum rel. error:      0.2968730032
     * Lowest output rel:       0.0000000000
     * Best input (lo):      -359.995880126953100000000000
     * Best output (lo):        0.0000719053 (0x3896CBE4)
     * Correct output (lo):     0.0000719053 (0x3896CBE4)
     * Worst input (hi):      179.999969482421880000000000
     * Highest output rel:      0.2968729734
     * Worst output (hi):       0.0000003745 (0x34C91000)
     * Correct output (hi):     0.0000005326 (0x350EFA35)
     * Worst input (abs):    -345.023620605468750000000000
     * Worst output (abs):      0.2584204674 (0x3E844FB0)
     * Correct output (abs):    0.2584208250 (0x3E844FBC)
     * Running sinSmoothDeg
     * Mean absolute error:     0.0001496345
     * Mean relative error:     0.0002429262
     * Maximum abs. error:      0.0003549457
     * Maximum rel. error:      0.2965919971
     * Lowest output rel:       0.0000000000
     * Best input (lo):      -330.027282714843750000000000
     * Best output (lo):        0.4995875657 (0x3EFFC9F1)
     * Correct output (lo):     0.4995875657 (0x3EFFC9F1)
     * Worst input (hi):      179.999969482421880000000000
     * Highest output rel:      0.2965919673
     * Worst output (hi):       0.0000003747 (0x34C92492)
     * Correct output (hi):     0.0000005326 (0x350EFA35)
     * Worst input (abs):    -298.954559326171900000000000
     * Worst output (abs):      0.8746489882 (0x3F5FE8FF)
     * Correct output (abs):    0.8750039339 (0x3F600042)
     * Running sinDegNewTable
     * Mean absolute error:     0.0001487592
     * Mean relative error:     0.0017648943
     * Maximum abs. error:      0.0005752109
     * Maximum rel. error:    238.9999847412
     * Lowest output rel:       0.0000000000
     * Best input (lo):      -270.013916015625000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):     -359.999908447265600000000000
     * Highest output rel:    238.9999694824
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0000015979 (0x35D67750)
     * Worst input (abs):      -0.560302376747131300000000
     * Worst output (abs):     -0.0092037544 (0xBC16CB58)
     * Correct output (abs):   -0.0097789653 (0xBC2037F4)
     * Running sinDeg037Table
     * Mean absolute error:     0.0001201583
     * Mean relative error:     0.0018105424
     * Maximum abs. error:      0.0003834828
     * Maximum rel. error:    719.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):      -359.978027343750000000000000
     * Best output (lo):        0.0003834952 (0x39C90FDA)
     * Correct output (lo):     0.0003834952 (0x39C90FDA)
     * Worst input (hi):      179.999969482421880000000000
     * Highest output rel:    718.9998779297
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0000005326 (0x350EFA35)
     * Worst input (abs):       0.175780624151229860000000
     * Worst output (abs):      0.0026844630 (0x3B2FEDD1)
     * Correct output (abs):    0.0030679458 (0x3B490F97)
     * Running sinDegOldTable
     * Mean absolute error:     0.0001487850
     * Mean relative error:     0.0019631863
     * Maximum abs. error:      0.0005754356
     * Maximum rel. error:    358.9999389648
     * Lowest output rel:       0.0000000000
     * Best input (lo):      -270.013916015625000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):     -359.999908447265600000000000
     * Highest output rel:    358.9999084473
     * Worst output (hi):       0.0005752427 (0x3A16CBE3)
     * Correct output (hi):     0.0000015979 (0x35D67750)
     * Worst input (abs):      -0.219724908471107480000000
     * Worst output (abs):     -0.0032594781 (0xBB559CF8)
     * Correct output (abs):   -0.0038349137 (0xBB7B532D)
     * Running sinShiftyDeg
     * Mean absolute error:     0.0000601517
     * Mean relative error:     0.0006477939
     * Maximum abs. error:      0.0005745929
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):      -359.978027343750000000000000
     * Best output (lo):        0.0003834952 (0x39C90FDA)
     * Correct output (lo):     0.0003834952 (0x39C90FDA)
     * Worst input (hi):     -359.999908447265600000000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000015979 (0x35D67750)
     * Worst input (abs):      -0.032921746373176575000000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):   -0.0005745929 (0xBA16A046)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     * </pre>
     */
    @Test
    public void testSinDeg() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinSmootherDeg", TrigTools::sinSmootherDeg);
        functions.put("sinSmoothDeg", TrigTools::sinSmoothDeg);
        functions.put("sinDegNewTable", TrigTools::sinDeg);
        functions.put("sinDeg037Table", TrigTools037::sinDeg);
        functions.put("sinDegOldTable", OldTrigTools::sinDeg);
        functions.put("sinShiftyDeg", PrecisionTest::sinShiftyDeg);
        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (float x = -360; x <= 360; x += 1E-4f) {
                float tru = (float) Math.sin(Math.toRadians(x)),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 1E-10) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 1E-10)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = (float) Math.sin(Math.toRadians(worstAbsX)),
                    highestTru = (float) Math.sin(Math.toRadians(highestRelX)),
                    lowestTru = (float) Math.sin(Math.toRadians(lowestRelX)),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }

    /**
     * Running sinSmootherTurns
     * Absolute error:   0.00000001
     * Relative error:   0.00000097
     * Maximum error:    0.00000006
     * Worst input:      -0.91666651
     * Worst approx output: 0.50000083
     * Correct output:      0.50000089
     * Running sinSmoothTurns
     * Absolute error:   0.00014983
     * Relative error:   0.00024772
     * Maximum error:    0.00035477
     * Worst input:      -0.83077192
     * Worst approx output: 0.87360507
     * Correct output:      0.87395984
     * Running sinTurnsNewTable
     * Absolute error:   0.00012112
     * Relative error:   0.00161588
     * Maximum error:    0.00038050
     * Worst input:      -0.50006056
     * Worst approx output: 0.00000000
     * Correct output:      0.00038050
     * Running sinTurnsKaze
     * Absolute error:   0.00333274
     * Relative error:   0.00964342
     * Maximum error:    0.00722286
     * Worst input:      -0.93186998
     * Worst approx output: 0.40789607
     * Correct output:      0.41511893
     */
    @Test
    public void testSinTurns() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinSmootherTurns", TrigTools::sinSmootherTurns);
        functions.put("sinSmoothTurns", TrigTools::sinSmoothTurns);
        functions.put("sinTurnsNewTable", TrigTools::sinTurns);
//        functions.put("sinTurnsOldTable", OldTrigTools::sinTurns);
        functions.put("sinTurnsKaze", PrecisionTest::sinKazeTurns);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            double absError = 0.0, relError = 0.0, maxError = 0.0;
            float worstX = 0;
            long counter = 0L;
            for (float x = -1; x <= 1; x += 0x1p-21f) {

                double tru = (float) Math.sin(x * 2 * Math.PI),
                        err = op.applyAsFloat(x) - tru,
                        ae = abs(err);
                relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
                absError += ae;
                if (maxError != (maxError = Math.max(maxError, ae))) {
                    worstX = x;
                }
                ++counter;
            }
            System.out.printf(
                    "Absolute error:   %3.8f\n" +
                            "Relative error:   %3.8f\n" +
                            "Maximum error:    %3.8f\n" +
                            "Worst input:      %3.8f\n" +
                            "Worst approx output: %3.8f\n" +
                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, op.applyAsFloat(worstX), (float) Math.sin(worstX * 2 * Math.PI));
        }
    }


    /**
     * Testing from PI2 to 0 in decrements of 0x1p-24:
     * <pre>
     * Running sinOldTable
     * Mean absolute error:     0.0000610469
     * Mean relative error:     0.0011362897
     * Maximum abs. error:      0.0003834685
     * Maximum rel. error:   6886.6332071030
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.712388996277944000000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592625771657800000000
     * Highest output rel:   6886.6332071030
     * Worst output (hi):       0.0001916011 (0x3F291D1108B21DD4)
     * Correct output (hi):     0.0000000278 (0x3E5DDE974234C4C5)
     * Worst input (abs):       3.141976122056142600000000
     * Worst output (abs):      0.0000000000 (0x0000000000000000)
     * Correct output (abs):   -0.0003834685 (0xBF3921887B4DA65C)
     * Running sin037Table
     * Mean absolute error:     0.0001220703
     * Mean relative error:     0.0021964348
     * Maximum abs. error:      0.0003834940
     * Maximum rel. error:  13784.7976836700
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.712388996277944000000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592625771657800000000
     * Highest output rel:  13784.7976836700
     * Worst output (hi):       0.0003834952 (0x3F3921FB49EE4A61)
     * Correct output (hi):     0.0000000278 (0x3E5DDE974234C4C5)
     * Worst input (abs):       6.282801810895101000000000
     * Worst output (abs):     -0.0007669903 (0xBF4921FB2AECAF1B)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA2D71)
     * Running sinNewTable
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482137
     * Maximum abs. error:      0.0001917470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.712388996277944000000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        6.283185128365652000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000001788 (0xBE880000008D310E)
     * Worst input (abs):       6.282993559037344000000000
     * Worst output (abs):     -0.0003834952 (0xBF3921FB49EE7334)
     * Correct output (abs):   -0.0001917481 (0xBF2921FFFD6AAA3F)
     * Running sinSmooth
     * Mean absolute error:     0.0001498261
     * Mean relative error:     0.0002467784
     * Maximum abs. error:      0.0003547083
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.712388996277944000000000
     * Best output (lo):       -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        6.283185307179586000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000000000 (0xBCB1A62633145C07)
     * Worst input (abs):       5.215633277093069000000000
     * Worst output (abs):     -0.8756678392 (0xBFEC05788F6AC7E7)
     * Correct output (abs):   -0.8760225474 (0xBFEC08606FFACC2B)
     * Running sinSmoother
     * Mean absolute error:     0.0000000078
     * Mean relative error:     0.0000000217
     * Maximum abs. error:      0.0000000184
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.748821024094717000000000
     * Best output (lo):       -0.9993364265 (0xBFEFFA9062B02CA2)
     * Correct output (lo):    -0.9993364265 (0xBFEFFA9062B02CA6)
     * Worst input (hi):        6.283185307179586000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000000000 (0xBCB1A62633145C07)
     * Worst input (abs):       4.712580744420187000000000
     * Worst output (abs):     -0.9999999632 (0xBFEFFFFFEC425456)
     * Correct output (abs):   -0.9999999816 (0xBFEFFFFFF620F2B6)
     * Running sinSmoothly
     * Mean absolute error:     0.0000000078
     * Mean relative error:     0.0000000217
     * Maximum abs. error:      0.0000000184
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.748821024094717000000000
     * Best output (lo):       -0.9993364265 (0xBFEFFA9062B02CA3)
     * Correct output (lo):    -0.9993364265 (0xBFEFFA9062B02CA6)
     * Worst input (hi):        6.283185307179586000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000000000 (0xBCB1A62633145C07)
     * Worst input (abs):       4.712580744420187000000000
     * Worst output (abs):     -0.9999999632 (0xBFEFFFFFEC425456)
     * Correct output (abs):   -0.9999999816 (0xBFEFFFFFF620F2B6)
     * Running sinSmoothesque
     * Mean absolute error:     0.0000000078
     * Mean relative error:     0.0000000217
     * Maximum abs. error:      0.0000000184
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.748821024094717000000000
     * Best output (lo):       -0.9993364265 (0xBFEFFA9062B02CA2)
     * Correct output (lo):    -0.9993364265 (0xBFEFFA9062B02CA6)
     * Worst input (hi):        6.283185307179586000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000000000 (0xBCB1A62633145C07)
     * Worst input (abs):       4.712580744420187000000000
     * Worst output (abs):     -0.9999999632 (0xBFEFFFFFEC425456)
     * Correct output (abs):   -0.9999999816 (0xBFEFFFFFF620F2B6)
     * Running floaty14
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482137
     * Maximum abs. error:      0.0001917470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.712388996277944000000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        6.283185128365652000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000001788 (0xBE880000008D310E)
     * Worst input (abs):       6.282993559037344000000000
     * Worst output (abs):     -0.0003834952 (0xBF3921FB49EE7334)
     * Correct output (abs):   -0.0001917481 (0xBF2921FFFD6AAA3F)
     * Running floaty15
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482137
     * Maximum abs. error:      0.0001917470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.712388996277944000000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        6.283185128365652000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000001788 (0xBE880000008D310E)
     * Worst input (abs):       6.282993559037344000000000
     * Worst output (abs):     -0.0003834952 (0xBF3921FB49EE7334)
     * Correct output (abs):   -0.0001917481 (0xBF2921FFFD6AAA3F)
     * Running floaty16
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482137
     * Maximum abs. error:      0.0001917470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.712388996277944000000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        6.283185128365652000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000001788 (0xBE880000008D310E)
     * Worst input (abs):       6.282993559037344000000000
     * Worst output (abs):     -0.0003834952 (0xBF3921FB49EE7334)
     * Correct output (abs):   -0.0001917481 (0xBF2921FFFD6AAA3F)
     * </pre>
     */
    @Test
    public void testSinD() {
//        double[] prior00 = makeTableDoublePrior(0.0);
//        double[] prior05 = makeTableDoublePrior(0.5);
//        double[] table05 = makeTableDouble(0.5);
//        double[] table025 = makeTableDouble(0.25);
//        double[] tablePhi = makeTableDouble(MathTools.GOLDEN_RATIO_INVERSE_D);
//        double[] table0625 = makeTableDouble(0.625);
//        double[] table065625 = makeTableDouble(0.65625);
//        double[] table075 = makeTableDouble(0.75);
//        DoubleUnaryOperator[] sins = makeSinFloaty();

        LinkedHashMap<String, DoubleUnaryOperator> functions = new LinkedHashMap<>(8);
//        functions.put("sinOldTable", OldTrigTools::sin);
//        functions.put("sin037Table", TrigTools037::sin);
//        functions.put("sinNewTable", TrigTools::sin);
//        functions.put("sinSmooth", TrigTools::sinSmooth);
//        functions.put("sinSmoother", TrigTools::sinSmoother);
//        functions.put("sinSmoothly", PrecisionTest::sinSmoothly);
        functions.put("sinSmoothesque", PrecisionTest::sinSmoothesque);
//        functions.put("sin00Prior", (f) -> sin(prior00, f));
//        functions.put("sin05Prior", (f) -> sin(prior05, f));
//        functions.put("sin05", (f) -> sin(table05, f));
//        functions.put("sin025", (f) -> sin(table025, f));
//        functions.put("sinPhi", (f) -> sin(tablePhi, f));
//        functions.put("sin0625", (f) -> sin(table0625, f));
//        functions.put("sin065625", (f) -> sin(table065625, f));
//        functions.put("sin075", (f) -> sin(table075, f));
//        for (int i = 0; i < 3; i++) {
//            functions.put("floaty"+(i+14), sins[i]);
//        }

        for (Map.Entry<String, DoubleUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final DoubleUnaryOperator op = ent.getValue();
            double absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            double worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (double x = PI2_D; x >= -PI2_D; x -= 0x1p-24) {

                double tru = Math.sin(x),
                        err = tru - op.applyAsDouble(x),
                        ae = abs(err),
                        re = Math.abs(err / Math.nextAfter(tru, Math.copySign(Double.MAX_VALUE, tru)));
                relError += re;
                if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                    highestRelX = x;
                }
                if (minRelError != (minRelError = Math.min(minRelError, re))) {
                    lowestRelX = x;
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            double worstAbs = op.applyAsDouble(worstAbsX),
                    worstTru = Math.sin(worstAbsX),
                    highestTru = Math.sin(highestRelX),
                    lowestTru = Math.sin(lowestRelX),
                    lowestErr = lowestTru - op.applyAsDouble(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Double.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsDouble(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Double.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%016X)\n" +
                            "Correct output (lo): %16.10f (0x%016X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%016X)\n" +
                            "Correct output (hi): %16.10f (0x%016X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%016X)\n" +
                            "Correct output (abs):%16.10f (0x%016X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsDouble(lowestRelX), Double.doubleToLongBits(op.applyAsDouble(lowestRelX)), lowestTru, Double.doubleToLongBits(lowestTru),
                    highestRelX, highestRel, op.applyAsDouble(highestRelX), Double.doubleToLongBits(op.applyAsDouble(highestRelX)), highestTru, Double.doubleToLongBits(highestTru),
                    worstAbsX, worstAbs, Double.doubleToLongBits(worstAbs), worstTru, Double.doubleToLongBits(worstTru));
        }
    }

    /**
     * Testing from PI2 to 0 in decrements of 0x1p-24:
     * <pre>
     * Running cosOldTable
     * Mean absolute error:     0.0000610469
     * Mean relative error:     0.0012491322
     * Maximum abs. error:      0.0003834844
     * Maximum rel. error:  16066.3400824541
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        1.570796314870016000000000
     * Highest output rel:  16066.3400824541
     * Worst output (hi):       0.0001916011 (0x3F291D1108B21DD4)
     * Correct output (hi):     0.0000000119 (0x3E499BC5C234C4C6)
     * Worst input (abs):       1.571179811154500800000000
     * Worst output (abs):      0.0000000000 (0x0000000000000000)
     * Correct output (abs):   -0.0003834844 (0xBF3921CCBE1ED691)
     * Running cos037Table
     * Mean absolute error:     0.0001220703
     * Mean relative error:     0.0024223202
     * Maximum abs. error:      0.0003834916
     * Maximum rel. error:  32158.2473105283
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        1.570796314870016000000000
     * Highest output rel:  32158.2473105283
     * Worst output (hi):       0.0003834952 (0x3F3921FB49EE4A61)
     * Correct output (hi):     0.0000000119 (0x3E499BC5C234C4C6)
     * Worst input (abs):       1.574631277714864600000000
     * Worst output (abs):     -0.0034514499 (0xBF6C463710FC08F3)
     * Correct output (abs):   -0.0038349415 (0xBF6F6A748D21750C)
     * Running cosNewTable
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482048
     * Maximum abs. error:      0.0001917456
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        4.712580684815542000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):     0.0001917044 (0x3F292088830DE041)
     * Worst input (abs):       1.574823025857107000000000
     * Worst output (abs):     -0.0038349426 (0xBF6F6A751D67D785)
     * Correct output (abs):   -0.0040266882 (0xBF707E49E0BEC15C)
     * Running cosSmooth
     * Mean absolute error:     0.0001498261
     * Mean relative error:     0.0002467689
     * Maximum abs. error:      0.0003547083
     * Maximum rel. error:      0.0004103000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        3.693314377461568700000000
     * Highest output rel:      0.0004103000
     * Worst output (hi):      -0.8512739148 (0xBFEB3DA2CAFFF202)
     * Correct output (hi):    -0.8516233358 (0xBFEB407F94FE668F)
     * Worst input (abs):       5.779941383992330000000000
     * Worst output (abs):      0.8756680193 (0x3FEC0578F020C341)
     * Correct output (abs):    0.8760227276 (0x3FEC0860D0B0C784)
     * Running cosSmoother
     * Mean absolute error:     0.0000000078
     * Mean relative error:     0.0000000123
     * Maximum abs. error:      0.0000000184
     * Maximum rel. error:      0.0000000377
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        4.712388936673300000000000
     * Highest output rel:      0.0000000377
     * Worst output (hi):      -0.0000000437 (0xBE6777A5C2D46000)
     * Correct output (hi):    -0.0000000437 (0xBE6777A5D1A79393)
     * Worst input (abs):       6.282993559037344000000000
     * Worst output (abs):      0.9999999632 (0x3FEFFFFFEC42BF8F)
     * Correct output (abs):    0.9999999816 (0x3FEFFFFFF6215DF1)
     * Running cosSmoothly
     * Mean absolute error:     0.0000000078
     * Mean relative error:     0.0000000123
     * Maximum abs. error:      0.0000000184
     * Maximum rel. error:      0.0000000757
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        1.570796314870016000000000
     * Highest output rel:      0.0000000757
     * Worst output (hi):       0.0000000119 (0x3E499BC5A1B20000)
     * Correct output (hi):     0.0000000119 (0x3E499BC5C234C4C6)
     * Worst input (abs):       6.282993559037344000000000
     * Worst output (abs):      0.9999999632 (0x3FEFFFFFEC42BF8F)
     * Correct output (abs):    0.9999999816 (0x3FEFFFFFF6215DF1)
     * Running floaty14
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482048
     * Maximum abs. error:      0.0001917456
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        4.712580684815542000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):     0.0001917044 (0x3F292088830DE041)
     * Worst input (abs):       1.574823025857107000000000
     * Worst output (abs):     -0.0038349426 (0xBF6F6A751D67D785)
     * Correct output (abs):   -0.0040266882 (0xBF707E49E0BEC15C)
     * Running floaty15
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482048
     * Maximum abs. error:      0.0001917456
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        4.712580684815542000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):     0.0001917044 (0x3F292088830DE041)
     * Worst input (abs):       1.574823025857107000000000
     * Worst output (abs):     -0.0038349426 (0xBF6F6A751D67D785)
     * Correct output (abs):   -0.0040266882 (0xBF707E49E0BEC15C)
     * Running floaty16
     * Mean absolute error:     0.0000610352
     * Mean relative error:     0.0006482048
     * Maximum abs. error:      0.0001917456
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185307179586000000000
     * Best output (lo):        1.0000000000 (0x3FF0000000000000)
     * Correct output (lo):     1.0000000000 (0x3FF0000000000000)
     * Worst input (hi):        4.712580684815542000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):     0.0001917044 (0x3F292088830DE041)
     * Worst input (abs):       1.574823025857107000000000
     * Worst output (abs):     -0.0038349426 (0xBF6F6A751D67D785)
     * Correct output (abs):   -0.0040266882 (0xBF707E49E0BEC15C)
     * </pre>
     */
    @Test
    public void testCosD() {
//        DoubleUnaryOperator[] coss = makeCosFloaty();

        LinkedHashMap<String, DoubleUnaryOperator> functions = new LinkedHashMap<>(8);
//        functions.put("cosOldTable", OldTrigTools::cos);
//        functions.put("cos037Table", TrigTools037::cos);
//        functions.put("cosNewTable", TrigTools::cos);
//        functions.put("cosSmooth", TrigTools::cosSmooth);
//        functions.put("cosSmoother", TrigTools::cosSmoother);
        functions.put("cosSmoothly", PrecisionTest::cosSmoothly);
        functions.put("cosSmoothesque", PrecisionTest::cosSmoothesque);
//        functions.put("cos00Prior", (f) -> cos(prior00, f));
//        functions.put("cos05Prior", (f) -> cos(prior05, f));
//        functions.put("cos05", (f) -> cos(table05, f));
//        functions.put("cos025", (f) -> cos(table025, f));
//        functions.put("cosPhi", (f) -> cos(tablePhi, f));
//        functions.put("cos0625", (f) -> cos(table0625, f));
//        functions.put("cos065625", (f) -> cos(table065625, f));
//        functions.put("cos075", (f) -> cos(table075, f));
//        for (int i = 0; i < 3; i++) {
//            functions.put("floaty"+(i+14), coss[i]);
//        }

        for (Map.Entry<String, DoubleUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final DoubleUnaryOperator op = ent.getValue();
            double absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            double worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (double x = PI2_D; x >= 0; x -= 0x1p-24) {

                double tru = Math.cos(x),
                        err = tru - op.applyAsDouble(x),
                        ae = abs(err),
                        re = Math.abs(err / Math.nextAfter(tru, Math.copySign(Double.MAX_VALUE, tru)));
                relError += re;
                if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                    highestRelX = x;
                }
                if (minRelError != (minRelError = Math.min(minRelError, re))) {
                    lowestRelX = x;
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            double worstAbs = op.applyAsDouble(worstAbsX),
                    worstTru = Math.cos(worstAbsX),
                    highestTru = Math.cos(highestRelX),
                    lowestTru = Math.cos(lowestRelX),
                    lowestErr = lowestTru - op.applyAsDouble(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Double.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsDouble(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Double.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%016X)\n" +
                            "Correct output (lo): %16.10f (0x%016X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%016X)\n" +
                            "Correct output (hi): %16.10f (0x%016X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%016X)\n" +
                            "Correct output (abs):%16.10f (0x%016X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsDouble(lowestRelX), Double.doubleToLongBits(op.applyAsDouble(lowestRelX)), lowestTru, Double.doubleToLongBits(lowestTru),
                    highestRelX, highestRel, op.applyAsDouble(highestRelX), Double.doubleToLongBits(op.applyAsDouble(highestRelX)), highestTru, Double.doubleToLongBits(highestTru),
                    worstAbsX, worstAbs, Double.doubleToLongBits(worstAbs), worstTru, Double.doubleToLongBits(worstTru));
        }
    }

    /**
     * Testing from PI2 to -PI2 in decrements of 0x1p-20f:
     * <pre>
     * Running cosOldTable
     * Mean absolute error:     0.0001520912
     * Mean relative error:     0.0020495371
     * Maximum abs. error:      0.0005754033
     * Maximum rel. error:   1236.6342773438
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):       -4.712388515472412000000000
     * Highest output rel:   1236.6342773438
     * Worst output (hi):      -0.0005753914 (0xBA16D5DD)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):      -1.571563243865966800000000
     * Worst output (abs):     -0.0001915137 (0xB948D111)
     * Correct output (abs):   -0.0007669170 (0xBA490AEE)
     * Running cos037Table
     * Mean absolute error:     0.0001202006
     * Mean relative error:     0.0017872408
     * Maximum abs. error:      0.0003834893
     * Maximum rel. error:    823.8764038086
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        4.712388515472412000000000
     * Highest output rel:    823.8763427734
     * Worst output (hi):      -0.0003834952 (0xB9C90FDA)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):       4.707787036895752000000000
     * Worst output (abs):     -0.0049854168 (0xBBA35CB5)
     * Correct output (abs):   -0.0046019275 (0xBB96CBC4)
     * Running cosNewTable
     * Mean absolute error:     0.0000600990
     * Mean relative error:     0.0006195969
     * Maximum abs. error:      0.0001921159
     * Maximum rel. error:      1.0023180246
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):       -1.570604801177978500000000
     * Highest output rel:      1.0023179054
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0001915256 (0x3948D444)
     * Worst input (abs):       1.572521686553955000000000
     * Worst output (abs):     -0.0019174748 (0xBAFB53C7)
     * Correct output (abs):   -0.0017253589 (0xBAE22570)
     * Running cosGdx
     * Mean absolute error:     0.0001294330
     * Mean relative error:     0.0016387656
     * Maximum abs. error:      0.0005754033
     * Maximum rel. error:   1236.6342773438
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):       -4.712388515472412000000000
     * Highest output rel:   1236.6342773438
     * Worst output (hi):      -0.0005753914 (0xBA16D5DD)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):      -1.571563243865966800000000
     * Worst output (abs):     -0.0001915137 (0xB948D111)
     * Correct output (abs):   -0.0007669170 (0xBA490AEE)
     * Running cosRound
     * Mean absolute error:     0.0000600989
     * Mean relative error:     0.0006195963
     * Maximum abs. error:      0.0001920797
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        4.712580204010010000000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0001912236 (0x39488333)
     * Worst input (abs):      -4.728304386138916000000000
     * Worst output (abs):      0.0157226548 (0x3C80CCCC)
     * Correct output (abs):    0.0159147345 (0x3C825F9E)
     * Running cosFloaty
     * Mean absolute error:     0.0000600990
     * Mean relative error:     0.0006195969
     * Maximum abs. error:      0.0001921159
     * Maximum rel. error:      1.0023180246
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):       -1.570604801177978500000000
     * Highest output rel:      1.0023179054
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0001915256 (0x3948D444)
     * Worst input (abs):       1.572521686553955000000000
     * Worst output (abs):     -0.0019174748 (0xBAFB53C7)
     * Correct output (abs):   -0.0017253589 (0xBAE22570)
     * Running cosSmooth
     * Mean absolute error:     0.0001507175
     * Mean relative error:     0.0002473110
     * Maximum abs. error:      0.0003551245
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        4.712389469146728500000000
     * Highest output rel:      0.9999998808
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000004888 (0x35033379)
     * Worst input (abs):       5.784777164459228500000000
     * Worst output (abs):      0.8779895306 (0x3F60C3EC)
     * Correct output (abs):    0.8783446550 (0x3F60DB32)
     * Running cosSmoother
     * Mean absolute error:     0.0000000841
     * Mean relative error:     0.0000012143
     * Maximum abs. error:      0.0000004172
     * Maximum rel. error:      0.6110913754
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        4.712388515472412000000000
     * Highest output rel:      0.6110913157
     * Worst output (hi):      -0.0000007490 (0xB5491000)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):       5.303077220916748000000000
     * Worst output (abs):      0.5569323897 (0x3F0E931F)
     * Correct output (abs):    0.5569328070 (0x3F0E9326)
     * Running cosSmoothly
     * Mean absolute error:     0.0000000841
     * Mean relative error:     0.0000012143
     * Maximum abs. error:      0.0000004172
     * Maximum rel. error:      0.6110913754
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        4.712388515472412000000000
     * Highest output rel:      0.6110913157
     * Worst output (hi):      -0.0000007490 (0xB5491000)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):       5.303077220916748000000000
     * Worst output (abs):      0.5569323897 (0x3F0E931F)
     * Correct output (abs):    0.5569328070 (0x3F0E9326)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     * </pre>
     */
    @Test
    public void testCos() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
//        functions.put("cosNewTable", TrigTools::cos);
//        functions.put("cosSmooth", TrigTools::cosSmooth);
//        functions.put("cosSmoother", TrigTools::cosSmoother);

//        functions.put("cosOldTable", OldTrigTools::cos);
//        functions.put("cos037Table", TrigTools037::cos);
//        functions.put("cosRound", PrecisionTest::cosRound);
//        functions.put("cosFloaty", PrecisionTest::cosFloaty);
//        functions.put("cosNoAbs", PrecisionTest::cosNoAbs);
//        functions.put("cosGdx", MathUtils::cos);
//        functions.put("cosSmoothly", PrecisionTest::cosSmoothly);

//        functions.put("cosShifty", PrecisionTest::cosShifty);
//        functions.put("cosShifty2", PrecisionTest::cosShifty2);

        functions.put("cosPade", PrecisionTest::cosPade);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (float x = TrigTools.PI2; x >= -PI2; x -= 0x1p-20f) {

                float tru = (float) Math.cos(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 1E-10) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 1E-10)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = (float) Math.cos(worstAbsX),
                    highestTru = (float) Math.cos(highestRelX),
                    lowestTru = (float) Math.cos(lowestRelX),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }

    /**
     * Testing from -1.57f to 1.57f in increments of 0x1p-20f:
     * <pre>
     * Running tanSmoother
     * Mean absolute error:     0.0000502852
     * Mean relative error:     0.0000002945
     * Maximum abs. error:      0.1672363281
     * Maximum rel. error:      0.0001353590
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.477615714073181200000000
     * Best output (lo):      -10.7007675171 (0xC12B3658)
     * Correct output (lo):   -10.7007675171 (0xC12B3658)
     * Worst input (hi):        1.569986939430236800000000
     * Highest output rel:      0.0001353590
     * Worst output (hi):    1235.3348388672 (0x449A6AB7)
     * Correct output (hi):  1235.5020751953 (0x449A7011)
     * Worst input (abs):       1.569986939430236800000000
     * Worst output (abs):   1235.3348388672 (0x449A6AB7)
     * Correct output (abs): 1235.5020751953 (0x449A7011)
     * Running tanSmoother037
     * Mean absolute error:     0.0000502852
     * Mean relative error:     0.0000002945
     * Maximum abs. error:      0.1672363281
     * Maximum rel. error:      0.0001353590
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.477615714073181200000000
     * Best output (lo):      -10.7007675171 (0xC12B3658)
     * Correct output (lo):   -10.7007675171 (0xC12B3658)
     * Worst input (hi):        1.569986939430236800000000
     * Highest output rel:      0.0001353590
     * Worst output (hi):    1235.3348388672 (0x449A6AB7)
     * Correct output (hi):  1235.5020751953 (0x449A7011)
     * Worst input (abs):       1.569986939430236800000000
     * Worst output (abs):   1235.3348388672 (0x449A6AB7)
     * Correct output (abs): 1235.5020751953 (0x449A7011)
     * Running tanNoTable
     * Mean absolute error:     0.0088905813
     * Mean relative error:     0.0000341421
     * Maximum abs. error:     17.9890136719
     * Maximum rel. error:      0.0575222000
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.005164504051208500000000
     * Best output (lo):       -1.5752424002 (0xBFC9A18B)
     * Correct output (lo):    -1.5752424002 (0xBFC9A18B)
     * Worst input (hi):        0.000000596046447753906200
     * Highest output rel:      0.0575221963
     * Worst output (hi):       0.0000005618 (0x3516CBE4)
     * Correct output (hi):     0.0000005960 (0x35200000)
     * Worst input (abs):       1.569998383522033700000000
     * Worst output (abs):   1235.2326660156 (0x449A6772)
     * Correct output (abs): 1253.2216796875 (0x449CA718)
     * Running tanGdx
     * Mean absolute error:     0.0088819480
     * Mean relative error:     0.0000340990
     * Maximum abs. error:     17.9890136719
     * Maximum rel. error:      0.0575222000
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.005164504051208500000000
     * Best output (lo):       -1.5752424002 (0xBFC9A18B)
     * Correct output (lo):    -1.5752424002 (0xBFC9A18B)
     * Worst input (hi):        0.000000596046447753906200
     * Highest output rel:      0.0575221963
     * Worst output (hi):       0.0000005618 (0x3516CBE4)
     * Correct output (hi):     0.0000005960 (0x35200000)
     * Worst input (abs):       1.569998383522033700000000
     * Worst output (abs):   1235.2326660156 (0x449A6772)
     * Correct output (abs): 1253.2216796875 (0x449CA718)
     * Running tanTable
     * Mean absolute error:     0.1380470246
     * Mean relative error:     0.0020219306
     * Maximum abs. error:    386.6504516602
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.391320586204528800000000
     * Best output (lo):       -5.5118293762 (0xC0B060E8)
     * Correct output (lo):    -5.5118293762 (0xC0B060E8)
     * Worst input (hi):       -0.000382781028747558600000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):    -0.0003827811 (0xB9C8B001)
     * Worst input (abs):      -1.570000052452087400000000
     * Worst output (abs):   -869.1978149414 (0xC4594CA9)
     * Correct output (abs):-1255.8482666016 (0xC49CFB25)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     * </pre>
     */
    @Test
    public void testTan() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("tanSmoother", TrigTools::tanSmoother);
        functions.put("tanSmoother037", TrigTools037::tanSmoother);
        functions.put("tanNoTable", TrigTools::tan);
        functions.put("tanTable", PrecisionTest::tanTable);
        functions.put("tanGdx", MathUtils::tan);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (float x = -1.57f; x <= 1.57f; x += 0x1p-20f) {

                float tru = (float) Math.tan(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 1E-10) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 1E-10)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = (float) Math.tan(worstAbsX),
                    highestTru = (float) Math.tan(highestRelX),
                    lowestTru = (float) Math.tan(lowestRelX),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }

    /**
     * Testing from -1.57 to 1.57 in increments of 0x1p-24:
     * <pre>
     * Running tanSmoother
     * Mean absolute error:     0.0000000005
     * Mean relative error:     0.0000000000
     * Maximum abs. error:      0.0000012732
     * Maximum rel. error:      0.0000000462
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.522092468738556000000000
     * Best output (lo):      -20.5160171332 (0xC0348419B2E754F3)
     * Correct output (lo):   -20.5160171332 (0xC0348419B2E754F3)
     * Worst input (hi):       -0.000000007152557435219364
     * Highest output rel:      0.0000000462
     * Worst output (hi):      -0.0000000072 (0xBE3EB851D82B2E53)
     * Correct output (hi):    -0.0000000072 (0xBE3EB851F0000000)
     * Worst input (abs):      -1.569957561492920000000000
     * Worst output (abs):  -1192.2283434975 (0xC092A0E9D2E0B8DF)
     * Correct output (abs):-1192.2283422244 (0xC092A0E9D28B478D)
     * Running tanSmoother037
     * Mean absolute error:     0.0000000005
     * Mean relative error:     0.0000000000
     * Maximum abs. error:      0.0000012720
     * Maximum rel. error:      0.0000000245
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.554306037425995000000000
     * Best output (lo):      -60.6362528136 (0xC04E5170BB713611)
     * Correct output (lo):   -60.6362528136 (0xC04E5170BB713611)
     * Worst input (hi):       -0.000000007152557435219364
     * Highest output rel:      0.0000000245
     * Worst output (hi):      -0.0000000072 (0xBE3EB851E35E2E53)
     * Correct output (hi):    -0.0000000072 (0xBE3EB851F0000000)
     * Worst input (abs):      -1.569957621097564800000000
     * Worst output (abs):  -1192.3130721210 (0xC092A14095FA6368)
     * Correct output (abs):-1192.3130708489 (0xC092A14095A505E2)
     * Running tanNoTable
     * Mean absolute error:     0.0088965244
     * Mean relative error:     0.0000339260
     * Maximum abs. error:     17.8672924837
     * Maximum rel. error:      0.0142282068
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -0.157722480297088690000000
     * Best output (lo):       -0.1590434814 (0xBFC45B896B919697)
     * Correct output (lo):    -0.1590434814 (0xBFC45B896B919697)
     * Worst input (hi):       -1.570000000000000000000000
     * Highest output rel:      0.0142282068
     * Worst output (hi):   -1237.8982990170 (0xC0935797DBB29105)
     * Correct output (hi): -1255.7655915008 (0xC0939F0FF737E7F3)
     * Worst input (abs):      -1.570000000000000000000000
     * Worst output (abs):  -1237.8982990170 (0xC0935797DBB29105)
     * Correct output (abs):-1255.7655915008 (0xC0939F0FF737E7F3)
     * </pre>
     */
    @Test
    public void testTanD() {
        LinkedHashMap<String, DoubleUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("tanSmoother", TrigTools::tanSmoother);
        functions.put("tanSmoother037", TrigTools037::tanSmoother);
        functions.put("tanNoTable", TrigTools::tan);

        for (Map.Entry<String, DoubleUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final DoubleUnaryOperator op = ent.getValue();
            double absError = 0.0, relError = 0.0, maxAbsError = 0.0, maxRelError = 0.0, minRelError = Double.MAX_VALUE;
            double worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (double x = -1.57; x <= 1.57; x += 0x1p-24) {

                double tru = Math.tan(x),
                        approx = op.applyAsDouble(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 1E-10) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 1E-10)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            double worstAbs = op.applyAsDouble(worstAbsX),
                    worstTru = Math.tan(worstAbsX),
                    highestTru = Math.tan(highestRelX),
                    lowestTru = Math.tan(lowestRelX),
                    lowestErr = lowestTru - op.applyAsDouble(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Double.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsDouble(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Double.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%016X)\n" +
                            "Correct output (lo): %16.10f (0x%016X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%016X)\n" +
                            "Correct output (hi): %16.10f (0x%016X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%016X)\n" +
                            "Correct output (abs):%16.10f (0x%016X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsDouble(lowestRelX), Double.doubleToLongBits(op.applyAsDouble(lowestRelX)), lowestTru, Double.doubleToLongBits(lowestTru),
                    highestRelX, highestRel, op.applyAsDouble(highestRelX), Double.doubleToLongBits(op.applyAsDouble(highestRelX)), highestTru, Double.doubleToLongBits(highestTru),
                    worstAbsX, worstAbs, Double.doubleToLongBits(worstAbs), worstTru, Double.doubleToLongBits(worstTru));
        }
    }

    @Test
    public void testSinSquared() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinOldSmooth", PrecisionTest::sinOldSmooth);
        functions.put("sinBhaskaroid", TrigTools::sinSmooth);
        functions.put("sinNewTable", TrigTools::sin);
        functions.put("sinOldTable", OldTrigTools::sin);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            double absError = 0.0, relError = 0.0, maxError = 0.0;
            float worstX = 0;
            long counter = 0L;
            for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

                double tru = (float) Math.sin(x) * (float) abs(Math.sin(x)),
                        err = op.applyAsFloat(x) * abs(op.applyAsFloat(x)) - tru,
                        ae = abs(err);
                relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
                absError += ae;
                if (maxError != (maxError = Math.max(maxError, ae))) {
                    worstX = x;
                }
                ++counter;
            }
            System.out.printf(
                    "Absolute error:   %3.8f\n" +
                            "Relative error:   %3.8f\n" +
                            "Maximum error:    %3.8f\n" +
                            "Worst input:      %3.8f\n" +
                            "Worst approx output: %3.8f\n" +
                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, op.applyAsFloat(worstX) * abs(op.applyAsFloat(worstX)), (float) Math.sin(worstX) * (float) abs(Math.sin(worstX)));
        }
    }

    @Test
    public void testCosSquared() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("cosSmooth", TrigTools::cosSmooth);
        functions.put("cosNewTable", TrigTools::cos);
        functions.put("cosOldTable", OldTrigTools::cos);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            double absError = 0.0, relError = 0.0, maxError = 0.0;
            float worstX = 0;
            long counter = 0L;
            for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

                double tru = (float) Math.cos(x) * (float) abs(Math.cos(x)),
                        err = op.applyAsFloat(x) * abs(op.applyAsFloat(x)) - tru,
                        ae = abs(err);
                relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
                absError += ae;
                if (maxError != (maxError = Math.max(maxError, ae))) {
                    worstX = x;
                }
                ++counter;
            }
            System.out.printf(
                    "Absolute error:   %3.8f\n" +
                            "Relative error:   %3.8f\n" +
                            "Maximum error:    %3.8f\n" +
                            "Worst input:      %3.8f\n" +
                            "Worst approx output: %3.8f\n" +
                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, op.applyAsFloat(worstX) * abs(op.applyAsFloat(worstX)), (float) Math.cos(worstX) * (float) abs(Math.cos(worstX)));
        }
    }


//    @Test
//    public void testTan(){
//        double absError = 0.0, relError = 0.0, maxError = 0.0;
//        float worstX = 0;
//        long counter = 0L;
//        // 1.57 is just inside half-pi. This avoids testing the extremely large results at close to half-pi.
//        // near half-pi, the correct result becomes tremendously large, and this doesn't grow as quickly.
//        for (float x = -1.57f; x <= 1.57f; x += 0x1p-20f) {
//
//            double tru = (float) Math.tan(x),
//                    err = TrigTools.tan(x) - tru,
//                    ae = abs(err);
//            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
//            absError += ae;
//            if (maxError != (maxError = Math.max(maxError, ae))) {
//                worstX = x;
//            }
//            ++counter;
//        }
//        System.out.printf(
//                "Absolute error:   %3.8f\n" +
//                "Relative error:   %3.8f\n" +
//                "Maximum error:    %3.8f\n" +
//                "Worst input:      %3.8f\n" +
//                "Worst approx output: %3.8f\n" +
//                "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, TrigTools.tan(worstX), (float)Math.tan(worstX));
//    }

    @Test
    public void testTanNewTable(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        // 1.57 is just inside half-pi. This avoids testing the extremely large results at close to half-pi.
        // near half-pi, the correct result becomes tremendously large, and this doesn't grow as quickly.
        for (float x = -1.57f; x <= 1.57f; x += 0x1p-20f) {

            double tru = (float) Math.tan(x),
                    err = tanNewTable(x) - tru,
                    ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
            absError += ae;
            if (maxError != (maxError = Math.max(maxError, ae))) {
                worstX = x;
            }
            ++counter;
        }
        System.out.printf(
                "Absolute error:   %3.8f\n" +
                "Relative error:   %3.8f\n" +
                "Maximum error:    %3.8f\n" +
                "Worst input:      %3.8f\n" +
                "Worst approx output: %3.8f\n" +
                "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, tanNewTable(worstX), (float)Math.tan(worstX));
    }

    @Test
    public void testTanOldTable(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        // 1.57 is just inside half-pi. This avoids testing the extremely large results at close to half-pi.
        // near half-pi, the correct result becomes tremendously large, and this doesn't grow as quickly.
        for (float x = -1.57f; x <= 1.57f; x += 0x1p-20f) {

            double tru = (float) Math.tan(x),
                    err = tanOldTable(x) - tru,
                    ae = abs(err);
            relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));
            absError += ae;
            if (maxError != (maxError = Math.max(maxError, ae))) {
                worstX = x;
            }
            ++counter;
        }
        System.out.printf(
                "Absolute error:   %3.8f\n" +
                "Relative error:   %3.8f\n" +
                "Maximum error:    %3.8f\n" +
                "Worst input:      %3.8f\n" +
                "Worst approx output: %3.8f\n" +
                "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, tanOldTable(worstX), (float)Math.tan(worstX));
    }

    public static float sinNewTable(float radians) {
        //Absolute error:   0.00012207
        //Relative error:   0.00000000
        //Maximum error:    0.00038354
        //Worst input:      -6.28050089
        //Worst approx output: 0.00306796
        //Correct output:      0.00268442
        return SIN_TABLE[(int) (radians * radToIndex) & TABLE_MASK];
    }

    public static float sinNewTable2(float radians) {
        //Absolute error:   0.00015258
        //Relative error:   -0.00000001
        //Maximum error:    0.00057527
        //Worst input:      -3.14101744
        //Worst approx output: -0.00115049
        //Correct output:      -0.00057522
        return SIN_TABLE[(int) (radians * radToIndex + 0.5f) & TABLE_MASK];
    }

    public static float sinOldTable(float radians) {
        //Absolute error:   0.00015261
        //Relative error:   -0.00000000
        //Maximum error:    0.00057529
        //Worst input:      -6.28050089
        //Worst approx output: 0.00325970
        //Correct output:      0.00268442
        return OldTrigTools.sin(radians);
//        return OldTrigTools.SIN_TABLE[(int) (radians * OldTrigTools.radToIndex) & OldTrigTools.TABLE_MASK];
    }

    public static float sinOldSmooth(float radians)
    {
        //Absolute error:   0.00050517
        //Relative error:   -0.00000000
        //Maximum error:    0.00109063
        //Worst input:      -3.33434725
        //Worst approx output: 0.19047257
        //Correct output:      0.19156320
        radians *= 0.6366197723675814f;
        final int floor = (int)Math.floor(radians) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }

    public static float sinFuzz(float radians, float alpha)
    {
        // With alpha == 0.22401
        //Absolute error:   0.00052595
        //Relative error:   -0.00000000
        //Maximum error:    0.00091943
        //Worst input:      -3.31969690
        //Worst approx output: 0.17624471
        //Correct output:      0.17716414
        radians *= 0.6366197723675814f;
        final int floor = (int)Math.floor(radians) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-1f + alpha - alpha * radians) * ((floor & 2) - 1);
    }

    /**
     * Eventually I am going to try the original Bhaskara I approximation. This doesn't do it yet.
     * @param radians
     * @return
     */
    public static float sinBhaskaraI(float radians) {
        //Mean absolute error: 0.0001498343
        //Mean relative error: 0.0002477639
        //Maximum error:       0.00035501
        //Worst input:         -4.20848227
        //Worst approx output: 0.87534791
        //Correct output:      0.87570292
        radians *= 0.63661975f;
        final int ceil = (int) Math.ceil(radians) & -2;
        radians -= ceil;
        final float x2 = radians * radians, x3 = radians * x2;
        return (((11 * radians - 3 * x3) / (7 + x2)) * (1 - (ceil & 2)));
    }

    /**
     * From <a href="https://basesandframes.files.wordpress.com/2016/05/rgreenfastermath_gdc02.pdf">Robin Green</a>.
     * @param radians
     * @return
     */
    public static float sinGreen(float radians) {
        float x = radians * 1.2732395447351628f;
        final int floor = (int)(x + 16384.0) - 16384;
        final int oct = floor & 7;
        switch (oct){
            case 0: {
                x = radians - floor * 0.7853981633974483f;
                final float x2 = x * x;
                return x * (1f + x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
            }
            case 1: {
                x = 0.7853981633974483f - radians + floor * 0.7853981633974483f;
                final float x2 = x * x;
                x *= (1f + x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
                return (float)Math.sqrt(1f - x * x);
            }
            case 2: {
                x = radians - floor * 0.7853981633974483f;
                final float x2 = x * x;
                x *= (1f + x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
                return (float)Math.sqrt(1f - x * x);
            }
            case 3: {
                x = 0.7853981633974483f - radians + floor * 0.7853981633974483f;
                final float x2 = x * x;
                return x * (1f + x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
            }
            case 4: {
                x = floor * 0.7853981633974483f - radians;
                final float x2 = x * x;
                return x * (1f + x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
            }
            case 5: {
                x = 0.7853981633974483f - radians + floor * 0.7853981633974483f;
                final float x2 = x * x;
                x *= (1f + x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
                return -(float)Math.sqrt(1f - x * x);
            }
            case 6: {
                x = radians - floor * 0.7853981633974483f;
                final float x2 = x * x;
                x *= (-1f - x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
                return -(float)Math.sqrt(1f - x * x);
            }
//            case 7:
            default: {
                x = 0.7853981633974483f - radians + floor * 0.7853981633974483f;
                final float x2 = x * x;
                return x * (-1f - x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
            }
        }
    }

    @Test
    @Ignore
    public void basicSinGreenTest() {
        float in = 0.125f;
        for (int i = 0; i < 64; i++) {
            System.out.printf("%11.9f => %11.9f and should be %11.9f\n", in, sinGreen(in), Math.sin(in));
            in += 0.125f;
        }
    }

    public static void main(String[] args) {
        /*
Best input (lo):        -2.617993116378784000000000
Worst input (hi):       -3.141592741012573200000000
Worst input (abs):       4.205234527587891000000000
         */
        System.out.printf("%30.24f should be %30.24f\n", sinBhaskaraI(-2.617993116378784000000000f), (float)Math.sin(-2.617993116378784000000000));
        System.out.printf("%30.24f should be %30.24f\n", sinBhaskaraI(-3.141592741012573200000000f), (float)Math.sin(-3.141592741012573200000000));
        System.out.printf("%30.24f should be %30.24f\n", sinBhaskaraI( 4.205234527587891000000000f), (float)Math.sin( 4.205234527587891000000000));
    }

    //sinNewTable
    // 15 bits
    //Mean absolute error:     0.0000605661
    //Mean relative error:     0.0009589014
    //Maximum abs. error:      0.0001917998
    //Maximum rel. error:    633.9434204102
    // 14 bits
    //Mean absolute error:     0.0001211311
    //Mean relative error:     0.0019176021
    //Maximum abs. error:      0.0003835472
    //Maximum rel. error:   1268.8868408203
    // 13 bits
    //Mean absolute error:     0.0002422611
    //Mean relative error:     0.0038344343
    //Maximum abs. error:      0.0007669241
    //Maximum rel. error:   2538.7734375000
    // 12 bits
    //Mean absolute error:     0.0004845174
    //Mean relative error:     0.0076436219
    //Maximum abs. error:      0.0015337468
    //Maximum rel. error:   5078.5454101563
    // 10 bits
    //Mean absolute error:     0.0019380411
    //Mean relative error:     0.0305681583
    //Maximum abs. error:      0.0061356635
    //Maximum rel. error:  20317.0605468750
    // 8 bits
    //Mean absolute error:     0.0077518616
    //Mean relative error:     0.1222654060
    //Maximum abs. error:      0.0245410595
    //Maximum rel. error:  81263.6015625000

    /**
     * A smooth sine approximation (not table-based) built around Bhaskara I's sine approximation from the 7th century.
     * This was updated more recently than the 7th century, and has better precision than the original. You may want to
     * use this if you notice statistical issues with the tabular approximation of sin(); in particular, only 16384
     * outputs are possible from {@link TrigTools#sin(float)}, and about half of those are duplicates, so if you need
     * more possible results in-between the roughly 8192 possible sin() returns, you can use this.
     * <br>
     * Credit to <a href="https://math.stackexchange.com/a/3886664">This StackExchange answer by WimC</a>.
     * @param radians an angle in radians; most precise between -PI2 and PI2
     * @return the approximate sine of the given angle, from -1 to 1 inclusive
     */
    public static float sinBhaskaroid(float radians) {
        //Mean absolute error:     0.0001500618
        //Mean relative error:     0.0002456330
        //Maximum abs. error:      0.0003549457
        //Maximum rel. error:      0.9999999404
        //Worst input (hi):       -3.141592741012573200000000
        //Highest output rel:      0.9999999404
        //Worst output (hi):      -0.0000000000 (0x80000000)
        //Correct output (hi):     0.0000000874 (0x33BBBD2E)
        //Worst input (abs):       4.205234527587891000000000
        //Worst output (abs):     -0.8737751842 (0xBF5FAFBB)
        //Correct output (abs):   -0.8741301298 (0xBF5FC6FE)
        radians = radians * (TrigTools.PI_INVERSE * 2f);
        final int ceil = (int) Math.ceil(radians) & -2;
        radians -= ceil;
        final float x2 = radians * radians, x3 = radians * x2;
        return (((11 * radians - 3 * x3) / (7 + x2)) * (1 - (ceil & 2)));
    }

    /**
     * {@code
     * return (x * (137.919f + x2 * -35.8822f))/(87.8021f + x2 * (13.2639f + x2)) * (1 - (ceil & 2));
     * Running sinPade
     * Mean absolute error:     0.0000300636
     * Mean relative error:     0.0000319437
     * Maximum abs. error:      0.0002860427
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.243394374847412000000000
     * Best output (lo):       -0.0397804342 (0xBD22F0CF)
     * Correct output (lo):    -0.0397804342 (0xBD22F0CF)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.712404727935791000000000
     * Worst output (abs):     -0.9997139573 (0xBF7FED41)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     *
     * return (x * (137.9199f + x2 * -35.8822f))/(87.802f + x2 * (13.2525f + x2)) * (1 - (ceil & 2));
     * Running sinPade
     * Mean absolute error:     0.0000246790
     * Mean relative error:     0.0000328162
     * Maximum abs. error:      0.0001646280
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.901302814483643000000000
     * Best output (lo):       -0.9822087884 (0xBF7B7209)
     * Correct output (lo):    -0.9822087884 (0xBF7B7209)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.712404727935791000000000
     * Worst output (abs):     -0.9998353720 (0xBF7FF536)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     *
     * return (x * (137.92f + x2 * -35.8821f))/(87.802f + x2 * (13.245f + x2)) * (1 - (ceil & 2));
     * Running sinPade
     * Mean absolute error:     0.0000373686
     * Mean relative error:     0.0000474176
     * Maximum abs. error:      0.0000892878
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.797290325164795000000000
     * Best output (lo):       -0.9963980317 (0xBF7F13F1)
     * Correct output (lo):    -0.9963980317 (0xBF7F13F1)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.712412357330322000000000
     * Worst output (abs):     -0.9999107122 (0xBF7FFA26)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     *
     * return (x * (137.9199f + x2 * -35.84f))/(87.802f + x2 * (13.2875f + x2)) * (1 - (ceil & 2));
     * Running sinPade
     * Mean absolute error:     0.0000178068
     * Mean relative error:     0.0000231204
     * Maximum abs. error:      0.0000941157
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.840942859649658000000000
     * Best output (lo):       -0.9917483330 (0xBF7DE338)
     * Correct output (lo):    -0.9917483330 (0xBF7DE338)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.712399959564209000000000
     * Worst output (abs):     -0.9999058843 (0xBF7FF9D5)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     *
     * return (x * (137.9199f + x2 * -35.84f)) / (87.802f + x2 * (13.288f + x2)) * (1 - (ceil & 2));
     * Running sinPade
     * Mean absolute error:     0.0000166354
     * Mean relative error:     0.0000222836
     * Maximum abs. error:      0.0000989437
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.850412845611572000000000
     * Best output (lo):       -0.9904898405 (0xBF7D90BE)
     * Correct output (lo):    -0.9904898405 (0xBF7D90BE)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.712421894073486000000000
     * Worst output (abs):     -0.9999010563 (0xBF7FF984)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     * }
     * @param x
     * @return
     */
    public static float sinPade(float x) {
        x = x * (TrigTools.PI_INVERSE * 2f);
        final int ceil = (int) Math.ceil(x) & -2;
        x -= ceil;
        final float x2 = x * x;
        return (x * (137.9199f + x2 * -35.84f)) / (87.802f + x2 * (13.288f + x2)) * (1 - (ceil & 2));
    }

    /**
     * Running cosPade
     * Mean absolute error:     0.0000165813
     * Mean relative error:     0.0000220270
     * Maximum abs. error:      0.0000989437
     * Maximum rel. error:      0.6110966206
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.145555973052978500000000
     * Best output (lo):        0.9905440211 (0x3F7D944B)
     * Correct output (lo):     0.9905440211 (0x3F7D944B)
     * Worst input (hi):        4.712388515472412000000000
     * Highest output rel:      0.6110966206
     * Worst output (hi):      -0.0000007490 (0xB549102B)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):       6.283184528350830000000000
     * Worst output (abs):      0.9999010563 (0x3F7FF984)
     * Correct output (abs):    1.0000000000 (0x3F800000)
     *
     *
     * @param x
     * @return
     */
    public static float cosPade(float x) {
        x = Math.abs(x * (TrigTools.PI_INVERSE * 2f));
        final int floor = (int)x | 1;
        x -= floor;
        final float x2 = x * x;
        return (x * (137.9199f + x2 * -35.84f)) / (87.802f + x2 * (13.288f + x2)) * ((floor & 2) - 1);
    }

    /**
     * An attempt at using a four-piece cubic Bézier curve to implement sin().
     * Based on <a href="https://spencermortensen.com/articles/bezier-circle/">this article</a>
     * by Spencer Mortensen.
     * @param radians angle
     * @return between -1 and 1
     */
    public static float sinCurve(float radians) {
        //Mean absolute error:     0.0031656511
        //Mean relative error:     0.0113971122
        //Maximum abs. error:      0.0074013174
        //Maximum rel. error:      0.9999999404

        //y=1.00005519 * i * ii + (3 * 0.99873585) * ii * t+ (3 * 0.55342686) * i * t * t;
        radians = radians * (TrigTools.PI_INVERSE * 2f);
        final int floor = (int) Math.floor(radians);
        radians -= floor;
        radians = Math.abs(radians - (~floor & 1));
        final float i = 1f - radians, ii = i * i;
        return (1.00005519f * i * ii + (3 * 0.99873585f) * ii * radians + (3 * 0.55342686f) * i * radians * radians)
                * (1 - (floor & 2));
    }
    public static float sinRound(float radians) {
        return SIN_TABLE[Math.round(radians * radToIndex) & TABLE_MASK];
    }

    public static float sinFloaty(final float radians) {
        return SIN_TABLE[(int) (radians * radToIndex + 16384.5f) & TABLE_MASK];
    }

    public static float sinFloatyMP(final float radians) {
        return SIN_TABLE[(int) (radians * radToIndex + 16384.5) & TABLE_MASK];
    }
    public static float sinFloatyHP(final float radians) {
        return SIN_TABLE[(int) (radians * radToIndexD + 16384.5) & TABLE_MASK];
    }

    public static float sinFloaty8192(final float radians) {
        return SIN_TABLE[(int) (radians * radToIndex + 8192.5f) & TABLE_MASK];
    }

    public static float sinFloaty32768(final float radians) {
        return SIN_TABLE[(int) (radians * radToIndex + 32768.5f) & TABLE_MASK];
    }

    public static float sinFloaty49152(final float radians) {
        return SIN_TABLE[(int) (radians * radToIndex + 49152.5f) & TABLE_MASK];
    }
    public static float cosRound(float radians) {
        return COS_TABLE[Math.round(radians * radToIndex) & TABLE_MASK];
    }

    public static float cosFloaty(final float radians) {
        return COS_TABLE[(int) (radians * radToIndex + 16384.5f) & TABLE_MASK];
    }

    public static float cosNoAbs(final float radians) {
        return COS_TABLE[((int)(radians * radToIndex)) & TABLE_MASK];
    }

    public static DoubleUnaryOperator[] makeSinFloaty(){
        final DoubleUnaryOperator[] r = new DoubleUnaryOperator[40];
        for (int i = 0; i < 40; i++) {
            long pow2 = 1L << i + 14;
            r[i] = radians -> SIN_TABLE_D[(int) (radians * radToIndexD + (pow2 + 0.5)) & TABLE_MASK];
        }
        return r;
    }

    public static DoubleUnaryOperator[] makeCosFloaty(){
        final DoubleUnaryOperator[] r = new DoubleUnaryOperator[40];
        for (int i = 0; i < 40; i++) {
            long pow2 = 1L << i + 14;
            r[i] = radians -> COS_TABLE_D[(int) (radians * radToIndexD + (pow2 + 0.5)) & TABLE_MASK];
        }
        return r;
    }

    public static float sinShifty(final float radians) {
        //Mean absolute error:     0.0000601960
        //Mean relative error:     0.0006447678
        //Maximum abs. error:      0.0005745887
        //Maximum rel. error:      1.0000000000
        final int idx = (int)(radians * radToIndex + 0.5f);
        return SIN_TABLE[(idx + (idx >> 31)) & TABLE_MASK];
        //Mean absolute error:     0.0001203791
        //Mean relative error:     0.0019150462
        //Maximum abs. error:      0.0003835417
        //Maximum rel. error:   2538.7736816406
//        final int idx = (int)(radians * radToIndex);
//        return SIN_TABLE[(idx + (idx >> 31)) & TABLE_MASK];
    }
    public static float sinShiftyDeg(final float radians) {
        final int idx = (int)(radians * degToIndex + 0.5f);
        return SIN_TABLE[(idx + (idx >> 31)) & TABLE_MASK];
    }
    public static float sinShiftyTurns(final float radians) {
        final int idx = (int)(radians * turnToIndex + 0.5f);
        return SIN_TABLE[(idx + (idx >> 31)) & TABLE_MASK];
    }
    public static final float radToIndexBonus = (TABLE_SIZE << 1) / PI2;
    public static final int TABLE_MASK_BONUS = (TABLE_SIZE << 1) - 1;

    public static float sinBonus(float radians) {
        final int idx = (int)(radians * radToIndexBonus) & TABLE_MASK_BONUS;
        return SIN_TABLE[(idx & 1) + (idx >>> 1)];
    }

    public static float sinSmoother(float radians) {
        // 15 bits
        //Mean absolute error:     0.0000000692
        //Mean relative error:     0.0000011315
        //Maximum abs. error:      0.0000004470
        //Maximum rel. error:      0.9999999404
        // 14 bits
        //Mean absolute error:     0.0000000698
        //Mean relative error:     0.0000011298
        //Maximum abs. error:      0.0000004470
        //Maximum rel. error:      0.9999999404
        // 13 bits
        //Mean absolute error:     0.0000000783
        //Mean relative error:     0.0000011377
        //Maximum abs. error:      0.0000004470
        //Maximum rel. error:      0.9999999404
        // 12 bits
        //Mean absolute error:     0.0000001439
        //Mean relative error:     0.0000012020
        //Maximum abs. error:      0.0000004768
        //Maximum rel. error:      0.9999999404
        // 10 bits
        //Mean absolute error:     0.0000019551
        //Mean relative error:     0.0000038662
        //Maximum abs. error:      0.0000047684
        //Maximum rel. error:      0.9999999404
        // 8 bits
        //Mean absolute error:     0.0000315707
        //Mean relative error:     0.0000492464
        //Maximum abs. error:      0.0000753403
        //Maximum rel. error:      0.9999999404

        radians = radians * radToIndex + 0.5f;
        final int floor = (int)(radians + 16384.0) - 16384;
        final int masked = floor & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static double sinSmoother(double radians) {
        // 14 bits
        //Mean absolute error:     0.0000000078
        //Mean relative error:     0.0000001134
        //Maximum abs. error:      0.0000000184
        //Maximum rel. error:      1.0000000000
        radians *= radToIndexD;
        final int floor = (int) Math.floor(radians);
        final int masked = floor & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }
    public static float cosShifty(final float radians) {
        final int idx = (int)(radians * radToIndex + 0.5f);
        return SIN_TABLE[(idx + (idx >> 31) + SIN_TO_COS) & TABLE_MASK];
    }
    public static float cosShifty2(final float radians) {
        final int idx = (int)(radians * radToIndex + 4096.5f);
        return SIN_TABLE[(idx + (idx >> 31)) & TABLE_MASK];
    }
    public static float cosShiftyDeg(final float radians) {
        final int idx = (int)(radians * degToIndex + 0.5f);
        return SIN_TABLE[(idx + (idx >> 31) + SIN_TO_COS) & TABLE_MASK];
    }
    public static float cosShiftyTurns(final float radians) {
        final int idx = (int)(radians * turnToIndex + 0.5f);
        return SIN_TABLE[(idx + (idx >> 31) + SIN_TO_COS) & TABLE_MASK];
    }

    public static float cosSmoother(float radians) {
        radians *= radToIndex;
        final int floor = (int)(radians + 16384.0) - 16384;
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static double cosSmoother(double radians) {
        radians *= radToIndexD;
        final int floor = (int) Math.floor(radians);
        final int masked = floor + SIN_TO_COS & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }


    public static float sinSmoothly(float radians) {
        radians *= radToIndex;
        final int floor = (int)(radians + 16384f) - 16384;
        final int masked = floor & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static float cosSmoothly(float radians) {
        radians *= radToIndex;
        final int floor = (int)(radians + 16384f) - 16384;
        final int masked = floor & TABLE_MASK;
        final float from = COS_TABLE[masked], to = COS_TABLE[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static double sinSmoothly(double radians) {
        radians = radians * radToIndexD + 16384.0;
        final int floor = (int)(radians);
        final int masked = floor & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static double cosSmoothly(double radians) {
        radians = radians * radToIndexD + 16384.0;
        final int floor = (int)(radians);
        final int masked = floor & TABLE_MASK;
        final double from = COS_TABLE_D[masked], to = COS_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static float sinSmoothesque(float radians) {
        radians = radians * radToIndex + 16384;
        final int floor = (int)radians;
        final int masked = floor & TABLE_MASK;
        final float from = SIN_TABLE[masked], to = SIN_TABLE[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static float cosSmoothesque(float radians) {
        radians = radians * radToIndex + 16384;
        final int floor = (int)radians;
        final int masked = floor & TABLE_MASK;
        final float from = COS_TABLE[masked], to = COS_TABLE[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static double sinSmoothesque(double radians) {
        radians = radians * radToIndexD + 16384;
        final int floor = (int)radians;
        final int masked = floor & TABLE_MASK;
        final double from = SIN_TABLE_D[masked], to = SIN_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static double cosSmoothesque(double radians) {
        radians = radians * radToIndexD + 16384;
        final int floor = (int)radians;
        final int masked = floor & TABLE_MASK;
        final double from = COS_TABLE_D[masked], to = COS_TABLE_D[masked+1];
        return from + (to - from) * (radians - floor);
    }

    //tanNoTable (Soonts)
    //Mean absolute error:     0.0088905813
    //Mean relative error:     0.0000341421
    //Maximum abs. error:     17.9890136719
    //Maximum rel. error:      0.0575221963


    public static float tanSmoother(float radians) {
        //Mean absolute error:     0.0000502852
        //Mean relative error:     0.0000002945
        //Maximum abs. error:      0.1672363281
        //Maximum rel. error:      0.0001353590
        radians *= radToIndex;
        final int floor = (int)(radians + 16384.0) - 16384;
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final float fromS = SIN_TABLE[maskedS], toS = SIN_TABLE[maskedS+1];
        final float fromC = SIN_TABLE[maskedC], toC = SIN_TABLE[maskedC+1];
        return (fromS + (toS - fromS) * (radians - floor))/(fromC + (toC - fromC) * (radians - floor));
    }

    public static double tanSmoother(double radians) {
        radians *= radToIndexD;
        final int floor = (int)Math.floor(radians);
        final int maskedS = floor & TABLE_MASK;
        final int maskedC = floor + SIN_TO_COS & TABLE_MASK;
        final double fromS = SIN_TABLE_D[maskedS], toS = SIN_TABLE_D[maskedS+1];
        final double fromC = SIN_TABLE_D[maskedC], toC = SIN_TABLE_D[maskedC+1];
        return (fromS + (toS - fromS) * (radians - floor))/(fromC + (toC - fromC) * (radians - floor));
    }

    public static float tanTable(float radians) {
        //Mean absolute error:     0.1380470246
        //Mean relative error:     0.0020219306
        //Maximum abs. error:    386.6504516602
        //Maximum rel. error:      0.9999999404
        final int r = (int)(radians * radToIndex);
        return SIN_TABLE[r & TABLE_MASK] / SIN_TABLE[r + SIN_TO_COS & TABLE_MASK];
    }


    /**
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any float to the valid input range.
     * @param radians an angle in radians as a float, often from 0 to pi * 2, though not required to be.
     * @return the sine of the given angle, as a float between -1f and 1f (both inclusive)
     */
    public static float sinNick(float radians)
    {
        //Mean absolute error: 0.0005051695
        //Mean relative error: 0.0019536310
        //Maximum error:       0.00109063
        //Worst input:         -3.33434725
        //Worst approx output: 0.19047257
        //Correct output:      0.19156320
        radians *= 0.6366197723675814f;
        int floor = (int)radians;
        if(floor > radians) --floor;
        floor &= -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }

    public static double sinLeibovici(double x) {
        final double a = -0.13299564481202533;
        final double b = 0.0032172781382236062;
        final double c = 0.033670915730403934;
        final double d = 4.96282801828961E-4;
        final double x2 = x*x;
        final double x4 = x2*x2;
        return x * (1.0 + a*x2 + b*x4)/(1.0 + c*x2 + d*x4);
    }

    /**
     * Also from the same question as {@link #sinBhaskaroid(float)}, but a different answer:
     * <a href="https://math.stackexchange.com/a/3887193">This answer by Claude Leibovici</a>.
     * @param x angle in radians
     * @return sine from -1 to 1, inclusive
     */
    public static float sinLeibovici(float x) {
        x = ((x + TrigTools.PI) % TrigTools.PI2 + TrigTools.PI2) % TrigTools.PI2 - TrigTools.PI;
        final float a = -0.13299564481202533f;
        final float b = 0.0032172781382236062f;
        final float c = 0.033670915730403934f;
        final float d = 4.96282801828961E-4f;
        final float x2 = x*x;
        final float x4 = x2*x2;
        return x * ((1f + a*x2 + b*x4)/(1f + c*x2 + d*x4));
    }

    /**
     * <a href="https://www.nullhardware.com/blog/fixed-point-sine-and-cosine-for-embedded-systems/">From here</a>, by
     * Andrew Steadman. This uses almost entirely integer (fixed-point) math, and like the sine-table-based
     * approximations here, it can't produce a perfectly smooth, continuously curving line of outputs. It is actually
     * rather accurate, though, other than that.
     * @param radians an angle in radians
     * @return a sine value from -1 to 1
     */
    public static float sinSteadman(float radians) {
        //Mean absolute error: 0.0001392388
        //Mean relative error: 0.0007815455
        //Maximum error:       0.00047594
        //Worst input:         -3.52834749
        //Worst approx output: 0.37670898
        //Correct output:      0.37718493


        int i = (short)(radians * 10430.378350470453f);//10430.378350470453f
        /* Convert (signed) input to a value between 0 and 8192. (8192 is pi/2, which is the region of the curve fit). */
        /* ------------------------------------------------------------------- */

        int c = i >> 31; //carry for output pos/neg


        // this block and the commented code just below it should be identical, just one is branchless.
        int fl = (i&0x4000); // flip input value to corresponding value in range [0..8192)
        int sg = -fl >> 31;
        i = (i + sg ^ sg);
//        if(0x4000 == (i&0x4000)) // flip input value to corresponding value in range [0..8192)
//            i = (1<<15) - i;


        i = (i & 0x7FFF) >>> 1;
        /* ------------------------------------------------------------------- */

    /* The following section implements the formula:
     = y * 2^-n * ( A1 - 2^(q-p)* y * 2^-n * y * 2^-n * [B1 - 2^-r * y * 2^-n * C1 * y]) * 2^(a-q)
    Where the constants are defined as follows:
    */
        final int A1 = 0xC8EC8A4B, B1 = 0xA3B2292C, C1 = 0x47645;

        int n=13, p=32, q=31, r=3, a=12;

        int y = (C1*i)>>>n;
        y = B1 - ((i*y)>>>r);
        y = i * (y>>>n);
        y = i * (y>>>n);
        y = A1 - (y>>>(p-q));
        y = i * (y>>>n);
        y = (y+(1<<(q-a-1)))>>>(q-a); // Rounding

        return (y + c ^ c) * 0x1p-12f;
    }

    public static float tanNewTable(float radians) {
        //Between -1.57 and 1.57, separated by 0x1p-20f:

        // NEW TABLE, doesn't add 0.5

        //Absolute error:   0.13866072
        //Relative error:   0.00007237
        //Maximum error:    386.65045166
        //Worst input:      -1.57000005
        //Worst approx output: -869.19781494
        //Correct output:      -1255.84826660
        final int idx = (int) (radians * TrigTools.TABLE_SIZE / TrigTools.PI2) & TABLE_MASK;
        return SIN_TABLE[idx] / SIN_TABLE[idx + TrigTools.SIN_TO_COS & TABLE_MASK];
    }


    public static float tanOldTable(float radians) {
        //Between -1.57 and 1.57, separated by 0x1p-20f:

        // OLD TABLE

        //Absolute error:   0.16286708
        //Relative error:   0.12471680
        //Maximum error:    510.82177734
        //Worst input:      -1.57000005
        //Worst approx output: -745.02648926
        //Correct output:      -1255.84826660
        final int idx = (int) (radians * OldTrigTools.TABLE_SIZE / OldTrigTools.PI2) & OldTrigTools.TABLE_MASK;
        return OldTrigTools.SIN_TABLE[idx] / OldTrigTools.SIN_TABLE[idx + OldTrigTools.SIN_TO_COS & OldTrigTools.TABLE_MASK];
    }

    public static long fibonacciRound(int n) {
        return (long) ((Math.pow(1.618033988749895, n)) / 2.236067977499795 + 0.49999999999999917); // used 2.236067977499794 before
//        return (long) ((Math.pow(MathTools.PHI_D, n) - Math.pow(MathTools.PSI_D, n)) / MathTools.ROOT5_D);
    }
    public static double fibonacciBase(long n) {
        return Math.pow(MathTools.PHI_D, n) / MathTools.ROOT5_D;
    }

    @Test
    public void fibonacciTest() {
        {
            int idx = 2;
            int old = MathTools.fibonacci(1), ancient = MathTools.fibonacci(0), t;
            while (old + ancient == (t = MathTools.fibonacci(idx))) {
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Int failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
        {
            long idx = 2;
            long old = MathTools.fibonacci(1L), ancient = MathTools.fibonacci(0L), t;
            while (old + ancient == (t = MathTools.fibonacci(idx))) {
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Long failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
        {
            int idx = 2;
            long old = MathTools.fibonacci(1L), ancient = MathTools.fibonacci(0L), t;
            while (old + ancient == (t = fibonacciRound(idx))) {
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Round failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
        {
            long idx = 2;
            long old = MathTools.fibonacci(1L), ancient = MathTools.fibonacci(0L), t;
            while (old + ancient == (t = MathTools.fibonacci(idx))) {
                System.out.printf("At index %d, Floored %d , Unfloored %20.20f\n", idx, t, fibonacciBase(idx));
                idx++;
                ancient = old;
                old = t;
            }
            System.out.println("Long failed at " + idx + " with calculated value " + t + " but correct value " + (old + ancient));
            System.out.println("Previous value " + old);
        }
    }
    public static long fibonacciFuzz(int n, double phi, double root) {
        return (long) ((Math.pow(phi, n)) / root + 0.5);
    }

    @Ignore
    @Test
    public void testFibonacciFuzz() {
        double phi = MathTools.PHI_D, root = 2.236067977499794, bestPhi = phi, bestRoot = root;
        int furthest = 0;
        System.out.printf("Starting with phi = %22.20f, root = %22.20f\n", bestPhi, bestRoot);
        long seed = Hasher.randomize3(System.nanoTime()); // this can use a fixed seed as well.
        System.out.println("Using seed " + seed);
        for (int i = 0; i < 1000; i++) {
            int idx = 2;
            long old = 1L, ancient = 0L, t;
            while (old + ancient == (t = fibonacciFuzz(idx, phi, root))) {
                idx++;
                ancient = old;
                old = t;
            }
            if(furthest < (furthest = Math.max(idx, furthest))){
                bestPhi = phi;
                bestRoot = root;
            }
            long r = Hasher.randomize1(seed++);
            if(r < 0x1000000000000000L){
                if(t > old + ancient) {
                    root = Math.nextUp(root);
//                    System.out.println("Root up: " + root);
                }
                else {
                    root = Math.nextDown(root);
//                    System.out.println("Root down: " + root);
                }
            } else {
                if(t > old + ancient) {
                    phi = Math.nextDown(phi);
//                    System.out.println("Phi down: " + phi);
                }
                else
                {
                    phi = Math.nextUp(phi);
//                    System.out.println("Phi up: " + phi);
                }
            }
            // an attempt to break out of locally optimal, globally suboptimal values.
            if((r & 127L) == 0L){
                if((r & 128L) == 0)
                    root = Math.nextUp(Math.nextUp(root));
                else
                    root = Math.nextDown(Math.nextDown(root));
            }
        }
        System.out.printf("Got up to index %d with phi = %22.20f, root = %22.20f\n", furthest, bestPhi, bestRoot);
        //Got up to index 77 with phi = 1.61803398874989500000, root = 2.23606797749979500000
    }
    public static long fibonacciFuzz2(int n, double phi, double half, double root) {
        return (long) ((Math.pow(phi, n)) / root + half);
    }

    @Ignore
    @Test
    public void testFibonacciFuzz2() {
        double phi = MathTools.PHI_D, half = 0.5, root = 2.236067977499794, bestPhi = phi, bestHalf = half, bestRoot = root;
        int furthest = 0;
        System.out.printf("Starting with phi = %22.20f, half = %22.20f, root = %22.20f\n", bestPhi, bestHalf, bestRoot);
        long seed = Hasher.randomize3(System.nanoTime()); // this can use a fixed seed as well.
        System.out.println("Using seed " + seed);
        for (int i = 0; i < 1000000; i++) {
            int idx = 2;
            long old = 1L, ancient = 0L, t;
            while (old + ancient == (t = fibonacciFuzz2(idx, phi, half, root))) {
                idx++;
                ancient = old;
                old = t;
            }
            if(furthest < (furthest = Math.max(idx, furthest))){
                bestPhi = phi;
                bestHalf = half;
                bestRoot = root;
            }
            long r = Hasher.randomize1(seed++);
//            if((r & 512) == 0) {
//                if ((r & 256) == 0)
//                    phi = Math.nextUp(phi);
//                else
//                    phi = Math.nextDown(phi);
//            }
            if(r > 0x3000000000000000L){
                if(t > old + ancient) {
                    root = Math.nextUp(root);
//                    System.out.println("Root up: " + root);
                }
                else {
                    root = Math.nextDown(root);
//                    System.out.println("Root down: " + root);
                }
            } else {
                if(t > old + ancient) {
                    half = Math.nextDown(half);
//                    System.out.println("Phi down: " + phi);
                }
                else
                {
                    half = Math.nextUp(half);
//                    System.out.println("Phi up: " + phi);
                }
            }
            // an attempt to break out of locally optimal, globally suboptimal values.
            if((r & 127L) == 0L){
                if((r & 128L) == 0)
                    root = Math.nextUp(Math.nextUp(root));
                else
                    root = Math.nextDown(Math.nextDown(root));
            }
        }
        System.out.printf("Got up to index %d with phi = %22.20f, half = %22.20f, root = %22.20f\n",
                furthest, bestPhi, bestHalf, bestRoot);
        // Got up to index 78 with half = 0.49999999999999920000, root = 2.23606797749979500000
    }



    @Ignore
    @Test
    public void testSinFuzz() {
        float alpha = 0.22401f, bestAlpha = alpha;
        double bestMax = 0.0011f;
        System.out.printf("Starting with alpha = %22.20f\n", bestAlpha);
        long seed = Hasher.randomize3(System.nanoTime()); // this can use a fixed seed as well.
        System.out.println("Using seed " + seed);
        for (int i = 0; i < 1000; i++) {
            double absError = 0.0, relError = 0.0, maxError = 0.0;
            float worstX = 0;
            long counter = 0L;
            for (float x = 0f; x <= TrigTools.PI2; x += 0x1p-16f) {
                double tru = (float) Math.sin(x),
                        err = sinFuzz(x, alpha) - tru,
                        ae = abs(err);
                relError += Math.abs(ae / Math.nextAfter(tru, Math.copySign(Float.POSITIVE_INFINITY, tru)));

                absError += ae;
                if (maxError != (maxError = Math.max(maxError, ae))) {
                    worstX = x;
                }
                ++counter;
            }
//            System.out.printf(
//                    "Absolute error:   %3.8f\n" +
//                            "Relative error:   %3.8f\n" +
//                            "Maximum error:    %3.8f\n" +
//                            "Worst input:      %3.8f\n" +
//                            "Worst approx output: %3.8f\n" +
//                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, sinSmooth(worstX), (float)Math.sin(worstX));

            if(bestMax > (bestMax = Math.min(maxError, bestMax))){
                bestAlpha = alpha;
            }
            long r = Hasher.randomize1(seed++);
            alpha += r * 0x1p-73;
//            if(r < 0x400000000000000L){
//                alpha = Math.nextDown(alpha);
//            }
//            else
//            {
//                alpha = Math.nextUp(alpha);
//            }
            // an attempt to break out of locally optimal, globally suboptimal values.
            if((r & 127L) == 0L){
                alpha += Hasher.randomize2(r) * 0x1p-71;
            }
        }
        System.out.printf("Found alpha = %22.20f, gets best max error = %22.20f\n", bestAlpha, bestMax);
        //Got up to index 77 with phi = 1.61803398874989500000, root = 2.23606797749979500000
    }


    /**
     * <pre>
     * Running sin05
     * 41648/1086918619 possible floats between 0 and PI2 were correct, a success rate of 0.00003832.
     * Running sin0625
     * 41866/1086918619 possible floats between 0 and PI2 were correct, a success rate of 0.00003852.
     * Running tablePhi
     * 41899/1086918619 possible floats between 0 and PI2 were correct, a success rate of 0.00003855.
     * Running sinNewTable
     * 18078/1086918619 possible floats between 0 and PI2 were correct, a success rate of 0.00001663.
     * Running sinOldTable
     * 41648/1086918619 possible floats between 0 and PI2 were correct, a success rate of 0.00003832.
     * Running sinSmoother
     * 72304986/1086918619 possible floats between 0 and PI2 were correct, a success rate of 0.06652291.
     * Running sinSmooth
     * 32971/1086918619 possible floats between 0 and PI2 were correct, a success rate of 0.00003033.
     * </pre>
     */
    @Test
    @Ignore
    public void testSinExactMatch() {
        float[] table05 = makeTableFloat(0.5);
        float[] table0625 = makeTableFloat(0.625);
        float[] tablePhi = makeTableFloat(MathTools.GOLDEN_RATIO_INVERSE_D);
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sin05", (f) -> sin(table05, f));
        functions.put("sin0625", (f) -> sin(table0625, f));
        functions.put("tablePhi", (f) -> sin(tablePhi, f));
        functions.put("sinNewTable", TrigTools::sin);
        functions.put("sinOldTable", OldTrigTools::sin);
        functions.put("sinSmoother", TrigTools::sinSmoother);
        functions.put("sinSmooth", TrigTools::sinSmooth);
//        functions.put("sinCurve", PrecisionTest::sinCurve);
//        functions.put("sinNick", PrecisionTest::sinNick);
//        functions.put("sinLeibovici", PrecisionTest::sinLeibovici);
//        functions.put("sinSteadman", PrecisionTest::sinSteadman);
//        functions.put("sinBhaskara2", PrecisionTest::sinBhaskaraI);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            long counter = 0L;
            for (int i = PI2BITS; i >= 0; i--) {
                float x = Float.intBitsToFloat(i);
                float tru = (float) Math.sin(x);
                if(op.applyAsFloat(x) == tru)
                    ++counter;
            }
            System.out.printf("%d/%d possible floats between 0 and PI2 were correct, a success rate of %10.8f.\n", counter, PI2BITS, (double)counter/(double) PI2BITS);
        }
    }

    public static float[] makeTableFloatMixed() {
        float[] SIN_TABLE = new float[0x4000];
        for (int i = 1; i < 0x2000; i++)
        {
            float later = (float) (Math.sin((double) i * 0x1p-13 * PI2_D));
            SIN_TABLE[i] = later;
            SIN_TABLE[i-1|0x2000] = later - SIN_TABLE[i-1];
        }
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE[0] = 0f;
        SIN_TABLE[(int) (90  * 0x1p13 / 360.0)] = 1f;
        SIN_TABLE[(int) (180 * 0x1p13 / 360.0)] = 0f;
        SIN_TABLE[(int) (270 * 0x1p13 / 360.0)] = -1.0f;

        for (int i = 0; i <= 270; i+= 90) {
            int idx = (int) (i * 0x1p13 / 360.0);
            SIN_TABLE[(idx - 1 & 0x1FFF) | 0x2000] = SIN_TABLE[idx] - SIN_TABLE[idx-1 & 0x1FFF];
            SIN_TABLE[idx | 0x2000] = SIN_TABLE[idx+1] - SIN_TABLE[idx];
        }
        return SIN_TABLE;
    }


    public static float[] makeTableFloat(final double offset) {
        float[] SIN_TABLE = new float[TABLE_SIZE+1];
        final float off = (float) offset;
        for (int i = 0; i < TABLE_SIZE; i++)
            SIN_TABLE[i] = (float) (Math.sin((i + off) / TABLE_SIZE * PI2));
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE[0] = 0f;
        SIN_TABLE[TABLE_SIZE] = 0f;
        SIN_TABLE[(int) (90 * degToIndex) & TABLE_MASK] = 1f;
        SIN_TABLE[(int) (180 * degToIndex) & TABLE_MASK] = 0f;
        SIN_TABLE[(int) (270 * degToIndex) & TABLE_MASK] = -1.0f;
        return SIN_TABLE;
    }

    public static double[] makeTableDouble(final double offset) {
        double[] SIN_TABLE_D = new double[TABLE_SIZE+1];
        for (int i = 0; i < TABLE_SIZE; i++)
            SIN_TABLE_D[i] = (Math.sin((i + offset) / TABLE_SIZE * PI2_D));
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE_D[0] = 0.0;
        SIN_TABLE_D[TABLE_SIZE] = 0.0;
        SIN_TABLE_D[(int) (90 * degToIndexD) & TABLE_MASK] = 1.0;
        SIN_TABLE_D[(int) (180 * degToIndexD) & TABLE_MASK] = 0.0;
        SIN_TABLE_D[(int) (270 * degToIndexD) & TABLE_MASK] = -1.0;
        return SIN_TABLE_D;
    }


    public static float[] makeTableFloatPrior(final double offset) {
        float[] SIN_TABLE = new float[TABLE_SIZE+1];
        for (int i = 0; i < TABLE_SIZE; i++)
            SIN_TABLE[i] = (float) Math.sin((i + offset) / TABLE_SIZE * PI2_D);
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE[0] = 0f;
        SIN_TABLE[TABLE_SIZE] = 0f;
        SIN_TABLE[(int) (90 * degToIndex) & TABLE_MASK] = 1f;
        SIN_TABLE[(int) (180 * degToIndex) & TABLE_MASK] = 0f;
        SIN_TABLE[(int) (270 * degToIndex) & TABLE_MASK] = -1.0f;
        return SIN_TABLE;
    }

    public static double[] makeTableDoublePrior(final double offset) {
        double[] SIN_TABLE_D = new double[TABLE_SIZE+1];
        for (int i = 0; i < TABLE_SIZE; i++)
            SIN_TABLE_D[i] = Math.sin((i + offset) / TABLE_SIZE * PI2_D);
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE_D[0] = 0.0;
        SIN_TABLE_D[TABLE_SIZE] = 0.0;
        SIN_TABLE_D[(int) (90 * degToIndexD) & TABLE_MASK] = 1.0;
        SIN_TABLE_D[(int) (180 * degToIndexD) & TABLE_MASK] = 0.0;
        SIN_TABLE_D[(int) (270 * degToIndexD) & TABLE_MASK] = -1.0;
        return SIN_TABLE_D;
    }

    public static float sinMixed(float[] SIN_TABLE, float radians) {
        radians *= 1303.7972f;
        final int floor = (int)(radians + 16384.0) - 16384;
        final int masked = floor & 0x1FFF;
        final float from = SIN_TABLE[masked], grad = SIN_TABLE[masked|0x2000];
        return from + grad * (radians - floor);
    }

    public static float sin(float[] SIN_TABLE, float radians) {
        return SIN_TABLE[(int) (radians * radToIndex) & TABLE_MASK];
    }

    public static float cos(float[] SIN_TABLE, float radians) {
        return SIN_TABLE[(int) (radians * radToIndex) + SIN_TO_COS & TABLE_MASK];
    }

    public static double sin(double[] SIN_TABLE_D, double radians) {
        return SIN_TABLE_D[(int) (radians * radToIndexD) & TABLE_MASK];
    }

    public static double cos(double[] SIN_TABLE_D, double radians) {
        return SIN_TABLE_D[(int) (radians * radToIndexD) + SIN_TO_COS & TABLE_MASK];
    }


    public static float[] makeTableFloatVar(final int TABLE_SIZE) {
        final int TABLE_MASK = TABLE_SIZE -1;
        float[] SIN_TABLE = new float[TABLE_SIZE+1];
        for (int i = 0; i < TABLE_SIZE; i++)
            SIN_TABLE[i] = (float) (Math.sin((double) i / TABLE_SIZE * PI2));
        // The four right angles get extra-precise values, because they are
        // the most likely to need to be correct.
        SIN_TABLE[0] = 0f;
        SIN_TABLE[TABLE_SIZE] = 0f;
        SIN_TABLE[(int) (0.25f * TABLE_SIZE) & TABLE_MASK] = 1f;
        SIN_TABLE[(int) (0.5f  * TABLE_SIZE) & TABLE_MASK] = 0f;
        SIN_TABLE[(int) (0.75f * TABLE_SIZE) & TABLE_MASK] = -1.0f;
        return SIN_TABLE;
    }

    public static float sinVar(float[] table, float radians) {
        return table[(int) (radians * (table.length-1) * (0.5f * PI_INVERSE)) & table.length - 2];
    }

    public static float cosVar(float[] table, float radians) {
        return table[(int) (radians * (table.length-1) * (0.5f * PI_INVERSE)) + (table.length >>> 2) & table.length - 2];
    }

    public static float sinSmootherVar(float[] table, float radians) {
        final int len = table.length - 1;
        radians *= len * (0.5f * PI_INVERSE);
        final int floor = (int)(radians + 16384.0) - 16384;
        final int masked = floor & len - 1;
        final float from = table[masked], to = table[masked+1];
        return from + (to - from) * (radians - floor);
    }

    public static float cosSmootherVar(float[] table, float radians) {
        final int len = table.length - 1;
        radians *= len * (0.5f * PI_INVERSE);
        final int floor = (int)(radians + 16384.0) - 16384;
        final int masked = floor + (len >>> 2) & len - 1;
        final float from = table[masked], to = table[masked+1];
        return from + (to - from) * (radians - floor);
    }


    // These don't use BitConversion to make copying them into a jshell session easier.

    public static float invSqrt(float x) {
        int i = 0x5F1FFFF9 - (Float.floatToIntBits(x) >> 1);
        float y = Float.intBitsToFloat(i);
        return y * (0.703952253f * (2.38924456f - x * y * y));
    }
    public static float invSqrtOld(float x) {
        int i = 0x5F3759DF - (Float.floatToIntBits(x) >> 1);
        float y = Float.intBitsToFloat(i);
        return y * (1.5f - 0.5f * x * y * y);
    }
    public static double invSqrt(double x) {
        long i = 0x5FE6EB50C7B537A9L - (Double.doubleToLongBits(x) >> 1);
        double y = Double.longBitsToDouble(i);
        return y * (1.5 - 0.5 * x * y * y);
    }
    public static double invSqrtOld(double x) {
        long i = 0x5FE6EC85E7DE30DAL - (Double.doubleToLongBits(x) >> 1);
        double y = Double.longBitsToDouble(i);
        return y * (1.5 - 0.5 * x * y * y);
    }

    /**
     * This is an attempt to port Kaze Emanuar's N64-optimized sincos() approximation, using just the sin() part.
     * @param angle in turns, to make this possible to test alongside sinTurns()
     * @return a sine value between -1 and 1 inclusive
     */
    public static float sinKazeTurns(float angle) {
        short int_angle = (short) (int)((angle - MathTools.fastFloor(angle)) * 0x1p16f);
        int shifter = (int_angle ^ (int_angle << 1)) & 0xC000;
        float x = (short)(((int_angle + shifter) << 17) >>> 16);
        float cosx = (1f - 0.0000000010911122665310369f * x * x);
        float sinx = (float) Math.sqrt(1f - cosx * cosx);
        if ((shifter & 0x4000) != 0)
            sinx = cosx;
        if(int_angle < 0)
            sinx = -sinx;
        return sinx;
    }

    // Kaze Emanuar's N64-optimized sincos() approximation
    /*
#define SECOND_ORDER_COEFFICIENT 0.0000000010911122665310369f
#define quasi_cos_2(x) (ONE - SECOND_ORDER_COEFFICIENT * x * x)
// only uses 1/8 range using some symmetries. is a lot more accurate and...???
CONST f32x2 sincos(s16 int_angle) {
  s32 shifter = (int_angle ^ (int_angle << 1)) & 0xC000;
  f32 x = (f32) (((int_angle + shifter) << 17) >> 16);
  float cosx = quasi_cos_2(x);
  float sinx = sqrtf(ONE - cosx * cosx);
  if (shifter & Ox4000) {
    float temp = cosx;
    cosx = sinx;
    sinx = temp;
  }
  if (int_angle < 0) {
    sinx = -sinx;
  }
  if (shifter & Ox8000) {
    cosx = -cosx;
  }
  // imaginary part in cosine to give the reader mental damage
  return F32X2_NEW(sinx, cosx);
}
     */

    public static float sinJolt(float angle) {
        // Implementation based on sinf.c from the cephes library, combines sinf and cosf in a single function, changes octants to quadrants and vectorizes it
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        float x = Math.abs(angle);
        int quadrant = (int)(0.6366197723675814 * x + 0.5);
        x = ((x - quadrant * 1.5703125f) - quadrant * 0.0004837512969970703125f) - quadrant * 7.549789948768648e-8f;
        float x2 = x * x, s;
        switch (quadrant & 3) {
            case 0:
                s = ((-1.9515295891e-4f * x2 + 8.3321608736e-3f) * x2 - 1.6666654611e-1f) * x2 * x + x;
                break;
            case 1:
                s = ((2.443315711809948e-5f * x2 - (1.388731625493765e-3f)) * x2 + (4.166664568298827e-2f)) * x2 * x2 - 0.5f * x2 + 1f;
                break;
            case 2:
                s = (((1.9515295891e-4f * x2 - 8.3321608736e-3f) * x2 + 1.6666654611e-1f) * x2 * x - x);
                break;
            default:
                s = (((-2.443315711809948e-5f * x2 + 1.388731625493765e-3f) * x2 - 4.166664568298827e-2f) * x2 * x2 + 0.5f * x2 - 1f);
        }
        return s * Math.signum(angle);
    }

    public static float cosJolt(float angle) {
        // Implementation based on sinf.c from the cephes library, combines sinf and cosf in a single function, changes octants to quadrants and vectorizes it
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        float x = Math.abs(angle);
        int quadrant = (int)(0.6366197723675814f * x + 0.5);
        x = ((x - quadrant * 1.5703125f) - quadrant * 0.0004837512969970703125f) - quadrant * 7.549789948768648e-8f;
        float x2 = x * x, s;
        switch (quadrant & 3) {
            case 3:
                s = ((-1.9515295891e-4f * x2 + 8.3321608736e-3f) * x2 - 1.6666654611e-1f) * x2 * x + x;
                break;
            case 0:
                s = ((2.443315711809948e-5f * x2 - (1.388731625493765e-3f)) * x2 + (4.166664568298827e-2f)) * x2 * x2 - 0.5f * x2 + 1f;
                break;
            case 1:
                s = (((1.9515295891e-4f * x2 - 8.3321608736e-3f) * x2 + 1.6666654611e-1f) * x2 * x - x);
                break;
            default:
                s = (((-2.443315711809948e-5f * x2 + 1.388731625493765e-3f) * x2 - 4.166664568298827e-2f) * x2 * x2 + 0.5f * x2 - 1f);
        }
        return s;
    }

    public static float sinDegJolt(float angle) {
        // Implementation based on sinf.c from the cephes library, combines sinf and cosf in a single function, changes octants to quadrants and vectorizes it
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        float x = Math.abs(angle);
        int quadrant = (int)(0.011111111f * x + 0.5f);
        x = (x - quadrant * 90f) * (PI2 / 360f);
        float x2 = x * x, s;
        switch (quadrant & 3) {
            case 0:
                s = ((-1.9515295891e-4f * x2 + 8.3321608736e-3f) * x2 - 1.6666654611e-1f) * x2 * x + x;
                break;
            case 1:
                s = ((2.443315711809948e-5f * x2 - (1.388731625493765e-3f)) * x2 + (4.166664568298827e-2f)) * x2 * x2 - 0.5f * x2 + 1f;
                break;
            case 2:
                s = (((1.9515295891e-4f * x2 - 8.3321608736e-3f) * x2 + 1.6666654611e-1f) * x2 * x - x);
                break;
            default:
                s = (((-2.443315711809948e-5f * x2 + 1.388731625493765e-3f) * x2 - 4.166664568298827e-2f) * x2 * x2 + 0.5f * x2 - 1f);
        }
        return s * Math.signum(angle);
    }

    public static float cosDegJolt(float angle) {
        // Implementation based on sinf.c from the cephes library, combines sinf and cosf in a single function, changes octants to quadrants and vectorizes it
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        float x = Math.abs(angle);
        int quadrant = (int)(0.011111111f * x + 0.5f);
        x = (x - quadrant * 90f) * (PI2 / 360f);
        float x2 = x * x, s;
        switch (quadrant & 3) {
            case 3:
                s = ((-1.9515295891e-4f * x2 + 8.3321608736e-3f) * x2 - 1.6666654611e-1f) * x2 * x + x;
                break;
            case 0:
                s = ((2.443315711809948e-5f * x2 - (1.388731625493765e-3f)) * x2 + (4.166664568298827e-2f)) * x2 * x2 - 0.5f * x2 + 1f;
                break;
            case 1:
                s = (((1.9515295891e-4f * x2 - 8.3321608736e-3f) * x2 + 1.6666654611e-1f) * x2 * x - x);
                break;
            default:
                s = (((-2.443315711809948e-5f * x2 + 1.388731625493765e-3f) * x2 - 4.166664568298827e-2f) * x2 * x2 + 0.5f * x2 - 1f);
        }
        return s;
    }

    public static float sinTurnsJolt(float angle) {
        // Implementation based on sinf.c from the cephes library, combines sinf and cosf in a single function, changes octants to quadrants and vectorizes it
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        float x = Math.abs(angle);
        int quadrant = (int)(4 * x + 0.5f);
        x = (x - quadrant * 0.25f) * PI2;
        float x2 = x * x, s;
        switch (quadrant & 3) {
            case 0:
                s = ((-1.9515295891e-4f * x2 + 8.3321608736e-3f) * x2 - 1.6666654611e-1f) * x2 * x + x;
                break;
            case 1:
                s = ((2.443315711809948e-5f * x2 - (1.388731625493765e-3f)) * x2 + (4.166664568298827e-2f)) * x2 * x2 - 0.5f * x2 + 1f;
                break;
            case 2:
                s = (((1.9515295891e-4f * x2 - 8.3321608736e-3f) * x2 + 1.6666654611e-1f) * x2 * x - x);
                break;
            default:
                s = (((-2.443315711809948e-5f * x2 + 1.388731625493765e-3f) * x2 - 4.166664568298827e-2f) * x2 * x2 + 0.5f * x2 - 1f);
        }
        return s * Math.signum(angle);
    }

    public static float cosTurnsJolt(float angle) {
        // Implementation based on sinf.c from the cephes library, combines sinf and cosf in a single function, changes octants to quadrants and vectorizes it
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        float x = Math.abs(angle);
        int quadrant = (int)(4 * x + 0.5f);
        x = (x - quadrant * 0.25f) * PI2;
        float x2 = x * x, s;
        switch (quadrant & 3) {
            case 3:
                s = ((-1.9515295891e-4f * x2 + 8.3321608736e-3f) * x2 - 1.6666654611e-1f) * x2 * x + x;
                break;
            case 0:
                s = ((2.443315711809948e-5f * x2 - (1.388731625493765e-3f)) * x2 + (4.166664568298827e-2f)) * x2 * x2 - 0.5f * x2 + 1f;
                break;
            case 1:
                s = (((1.9515295891e-4f * x2 - 8.3321608736e-3f) * x2 + 1.6666654611e-1f) * x2 * x - x);
                break;
            default:
                s = (((-2.443315711809948e-5f * x2 + 1.388731625493765e-3f) * x2 - 4.166664568298827e-2f) * x2 * x2 + 0.5f * x2 - 1f);
        }
        return s;
    }

//    void Vec4::SinCos(Vec4 &outSin, Vec4 &outCos) const
//    {
//        // Implementation based on sinf.c from the cephes library, combines sinf and cosf in a single function, changes octants to quadrants and vectorizes it
//        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
//
//        // Make argument positive and remember sign for sin only since cos is symmetric around x (highest bit of a float is the sign bit)
//        UVec4 sin_sign = UVec4::sAnd(ReinterpretAsInt(), UVec4::sReplicate(0x80000000U));
//        Vec4 x = Vec4::sXor(*this, sin_sign.ReinterpretAsFloat());
//
//        // x / (PI / 2) rounded to nearest int gives us the quadrant closest to x
//        UVec4 quadrant = (0.6366197723675814f * x + Vec4::sReplicate(0.5f)).ToInt();
//
//        // Make x relative to the closest quadrant.
//        // This does x = x - quadrant * PI / 2 using a two step Cody-Waite argument reduction.
//        // This improves the accuracy of the result by avoiding loss of significant bits in the subtraction.
//        // We start with x = x - quadrant * PI / 2, PI / 2 in hexadecimal notation is 0x3fc90fdb, we remove the lowest 16 bits to
//        // get 0x3fc90000 (= 1.5703125) this means we can now multiply with a number of up to 2^16 without losing any bits.
//        // This leaves us with: x = (x - quadrant * 1.5703125) - quadrant * (PI / 2 - 1.5703125).
//        // PI / 2 - 1.5703125 in hexadecimal is 0x39fdaa22, stripping the lowest 12 bits we get 0x39fda000 (= 0.0004837512969970703125)
//        // This leaves uw with: x = ((x - quadrant * 1.5703125) - quadrant * 0.0004837512969970703125) - quadrant * (PI / 2 - 1.5703125 - 0.0004837512969970703125)
//        // See: https://stackoverflow.com/questions/42455143/sine-cosine-modular-extended-precision-arithmetic
//        // After this we have x in the range [-PI / 4, PI / 4].
//        Vec4 float_quadrant = quadrant.ToFloat();
//        x = ((x - float_quadrant * 1.5703125f) - float_quadrant * 0.0004837512969970703125f) - float_quadrant * 7.549789948768648e-8f;
//
//        // Calculate x2 = x^2
//        Vec4 x2 = x * x;
//
//        // Taylor expansion:
//        // Cos(x) = 1 - x^2/2! + x^4/4! - x^6/6! + x^8/8! + ... = (((x2/8!- 1/6!) * x2 + 1/4!) * x2 - 1/2!) * x2 + 1
//        Vec4 taylor_cos = ((2.443315711809948e-5f * x2 - Vec4::sReplicate(1.388731625493765e-3f)) * x2 + Vec4::sReplicate(4.166664568298827e-2f)) * x2 * x2 - 0.5f * x2 + Vec4::sOne();
//        // Sin(x) = x - x^3/3! + x^5/5! - x^7/7! + ... = ((-x2/7! + 1/5!) * x2 - 1/3!) * x2 * x + x
//        Vec4 taylor_sin = ((-1.9515295891e-4f * x2 + Vec4::sReplicate(8.3321608736e-3f)) * x2 - Vec4::sReplicate(1.6666654611e-1f)) * x2 * x + x;
//
//        // The lowest 2 bits of quadrant indicate the quadrant that we are in.
//        // Let x be the original input value and x' our value that has been mapped to the range [-PI / 4, PI / 4].
//        // since cos(x) = sin(x - PI / 2) and since we want to use the Taylor expansion as close as possible to 0,
//        // we can alternate between using the Taylor expansion for sin and cos according to the following table:
//        //
//        // quadrant  sin(x)      cos(x)
//        // XXX00b    sin(x')     cos(x')
//        // XXX01b    cos(x')    -sin(x')
//        // XXX10b   -sin(x')    -cos(x')
//        // XXX11b   -cos(x')     sin(x')
//        //
//        // So: sin_sign = bit2, cos_sign = bit1 ^ bit2, bit1 determines if we use sin or cos Taylor expansion
//        UVec4 bit1 = quadrant.LogicalShiftLeft<31>();
//        UVec4 bit2 = UVec4::sAnd(quadrant.LogicalShiftLeft<30>(), UVec4::sReplicate(0x80000000U));
//
//        // Select which one of the results is sin and which one is cos
//        Vec4 s = Vec4::sSelect(taylor_sin, taylor_cos, bit1);
//        Vec4 c = Vec4::sSelect(taylor_cos, taylor_sin, bit1);
//
//        // Update the signs
//        sin_sign = UVec4::sXor(sin_sign, bit2);
//        UVec4 cos_sign = UVec4::sXor(bit1, bit2);
//
//        // Correct the signs
//        outSin = Vec4::sXor(s, sin_sign.ReinterpretAsFloat());
//        outCos = Vec4::sXor(c, cos_sign.ReinterpretAsFloat());
//    }

    public static double atan2Jolt(final double y, double x) {
        double n = y / x;
        if (n != n)
            n = (y == x ? 1.0 : -1.0); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0.0; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return atanJolt(n);
        else if (x < 0) {
            if (y >= 0) return (atanJolt(n) + Math.PI);
            return (atanJolt(n) - Math.PI);
        } else if (y > 0)
            return x + HALF_PI_D;
        else if (y < 0) return x - HALF_PI_D;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    public static float atan2Jolt(final float y, float x) {
        float n = y / x;
        if (n != n)
            n = (y == x ? 1f : -1f); // if both y and x are infinite, n would be NaN
        else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if (x > 0)
            return atanJolt(n);
        else if (x < 0) {
            if (y >= 0) return atanJolt(n) + TrigTools.PI;
            return atanJolt(n) - TrigTools.PI;
        } else if (y > 0)
            return x + HALF_PI;
        else if (y < 0) return x - HALF_PI;
        return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    public static float atanJolt(float n) {
        // Implementation based on atanf.c from the cephes library
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        float m = Math.abs(n), x, y;

        if(m > 2.414213562373095f){
            x = -1f / m;
            y = HALF_PI;
        } else if(m > 0.4142135623730950f){
            x = (m - 1f) / (m + 1f);
            y = QUARTER_PI;
        } else {
            x = m;
            y = 0f;
        }
        float z = x * x;
        return Math.copySign(y + (((8.05374449538e-2f * z - 1.38776856032e-1f) * z + 1.99777106478e-1f)
                * z - 3.33329491539e-1f) * z * x + x, n);
    }


    public static double atanJolt(double n) {
        // Implementation based on atanf.c from the cephes library
        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
        double m = Math.abs(n), x, y;
        if(m > 2.414213562373095){
            x = -1. / m;
            y = HALF_PI_D;
        } else if(m > 0.4142135623730950){
            x = (m - 1.) / (m + 1.);
            y = QUARTER_PI_D;
        } else {
            x = m;
            y = 0.;
        }
        double z = x * x;
        return Math.copySign(y + (((8.05374449538e-2 * z - 1.38776856032e-1) * z + 1.99777106478e-1)
                * z - 3.33329491539e-1) * z * x + x, n);
    }

//    Vec4 Vec4::ATan() const
//    {
//        // Implementation based on atanf.c from the cephes library
//        // Original implementation by Stephen L. Moshier (See: http://www.moshier.net/)
//
//        // Make argument positive
//        UVec4 atan_sign = UVec4::sAnd(ReinterpretAsInt(), UVec4::sReplicate(0x80000000U));
//        Vec4 x = Vec4::sXor(*this, atan_sign.ReinterpretAsFloat());
//        Vec4 y = Vec4::sZero();
//
//        // If x > Tan(PI / 8)
//        UVec4 greater1 = Vec4::sGreater(x, Vec4::sReplicate(0.4142135623730950f));
//        Vec4 x1 = (x - Vec4::sOne()) / (x + Vec4::sOne());
//
//        // If x > Tan(3 * PI / 8)
//        UVec4 greater2 = Vec4::sGreater(x, Vec4::sReplicate(2.414213562373095f));
//        Vec4 x2 = Vec4::sReplicate(-1.0f) / (x JPH_IF_FLOATING_POINT_EXCEPTIONS_ENABLED(+ Vec4::sReplicate(FLT_MIN))); // Add small epsilon to prevent div by zero, works because x is always positive
//
//        // Apply first if
//        x = Vec4::sSelect(x, x1, greater1);
//        y = Vec4::sSelect(y, Vec4::sReplicate(0.25f * JPH_PI), greater1);
//
//        // Apply second if
//        x = Vec4::sSelect(x, x2, greater2);
//        y = Vec4::sSelect(y, Vec4::sReplicate(0.5f * JPH_PI), greater2);
//
//        // Polynomial approximation
//        Vec4 z = x * x;
//        y += (((8.05374449538e-2f * z - Vec4::sReplicate(1.38776856032e-1f)) * z + Vec4::sReplicate(1.99777106478e-1f)) * z - Vec4::sReplicate(3.33329491539e-1f)) * z * x + x;
//
//        // Put the sign back
//        return Vec4::sXor(y, atan_sign.ReinterpretAsFloat());
//    }

    /**
     * A big test that can handle lots of different comparisons.
     * <br>
     * Running Math.sin vs. TrigTools.sin
     * Mean absolute error:     0.0000037685
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0001921779
     * Maximum rel. error:      1.0039137602
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.263627052307129000000000
     * Best output (lo):       -0.0195570085 (0xBCA03605)
     * Correct output (lo):    -0.0195570085 (0xBCA03605)
     * Worst input (hi):        0.000191373095731250940000
     * Highest output rel:      1.0039136410
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0001913731 (0x3948AB53)
     * Worst input (abs):       3.147152900695801000000000
     * Worst output (abs):     -0.0057523963 (0xBBBC7E99)
     * Correct output (abs):   -0.0055602184 (0xBBB6327E)
     * Running Math.sin vs. TrigTools.sinSmoother
     * Mean absolute error:     0.0000000013
     * Mean relative error:     0.0000000233
     * Maximum abs. error:      0.0000004470
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.909108638763428000000000
     * Best output (lo):       -0.3654132187 (0xBEBB1771)
     * Correct output (lo):    -0.3654132187 (0xBEBB1771)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       6.156139850616455000000000
     * Worst output (abs):     -0.1267044097 (0xBE01BECD)
     * Correct output (abs):   -0.1267039627 (0xBE01BEAF)
     * Running Math.sin vs. TrigTools.sinSmooth
     * Mean absolute error:     0.0000004711
     * Mean relative error:     0.0000005390
     * Maximum abs. error:      0.0000989437
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         4.850440979003906000000000
     * Best output (lo):       -0.9904859662 (0xBF7D907D)
     * Correct output (lo):    -0.9904859662 (0xBF7D907D)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.712437629699707000000000
     * Worst output (abs):     -0.9999010563 (0xBF7FF984)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     * Running Math.cos vs. TrigTools.cos
     * Mean absolute error:     0.0000018842
     * Mean relative error:     0.0000175666
     * Maximum abs. error:      0.0001921206
     * Maximum rel. error:      1.0038977861
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        1.570987701416015600000000
     * Highest output rel:      1.0038976669
     * Worst output (hi):      -0.0003834952 (0xB9C90FDA)
     * Correct output (hi):    -0.0001913746 (0xB948ABBC)
     * Worst input (abs):       1.570987701416015600000000
     * Worst output (abs):     -0.0003834952 (0xB9C90FDA)
     * Correct output (abs):   -0.0001913746 (0xB948ABBC)
     * Running Math.cos vs. TrigTools.cosSmoother
     * Mean absolute error:     0.0000000015
     * Mean relative error:     0.0000000255
     * Maximum abs. error:      0.0000004172
     * Maximum rel. error:      0.6110913754
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        4.712388515472412000000000
     * Highest output rel:      0.6110913157
     * Worst output (hi):      -0.0000007490 (0xB5491000)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):       5.339963912963867000000000
     * Worst output (abs):      0.5871831179 (0x3F1651A2)
     * Correct output (abs):    0.5871835351 (0x3F1651A9)
     * Running Math.cos vs. TrigTools.cosSmooth
     * Mean absolute error:     0.0000018842
     * Mean relative error:     0.0000018842
     * Maximum abs. error:      0.0000989437
     * Maximum rel. error:      0.6110966206
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.145606994628906000000000
     * Best output (lo):        0.9905509949 (0x3F7D94C0)
     * Correct output (lo):     0.9905509949 (0x3F7D94C0)
     * Worst input (hi):        4.712388515472412000000000
     * Highest output rel:      0.6110966206
     * Worst output (hi):      -0.0000007490 (0xB549102B)
     * Correct output (hi):    -0.0000004649 (0xB4F9990F)
     * Worst input (abs):       6.283184528350830000000000
     * Worst output (abs):      0.9999010563 (0x3F7FF984)
     * Correct output (abs):    1.0000000000 (0x3F800000)
     * Running Math.sin vs. MathUtils.sin
     * Mean absolute error:     0.0000059195
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:   1267.9168701172
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.282993793487549000000000
     * Best output (lo):       -0.0001915137 (0xB948D111)
     * Correct output (lo):    -0.0001915137 (0xB948D111)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:   1267.9167480469
     * Worst output (hi):       0.0001916011 (0x3948E888)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       0.000383495178539305900000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):    0.0003834952 (0x39C90FDA)
     * Running Math.cos vs. MathUtils.cos
     * Mean absolute error:     0.0000018842
     * Mean relative error:     0.0000301476
     * Maximum abs. error:      0.0003833016
     * Maximum rel. error:   2536.8337402344
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        1.570796251296997000000000
     * Highest output rel:   2536.8334960938
     * Worst output (hi):       0.0001916011 (0x3948E888)
     * Correct output (hi):     0.0000000755 (0x33A22169)
     * Worst input (abs):       1.571179628372192400000000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):   -0.0003833016 (0xB9C8F5DE)
     * Running Math.sin vs. sinJolt
     * Mean absolute error:     0.0000000002
     * Mean relative error:     0.0000000003
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000001227
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        0.0000001748 (0x343BBD2E)
     * Correct output (lo):     0.0000001748 (0x343BBD2E)
     * Worst input (hi):        3.648786783218384000000000
     * Highest output rel:      0.0000001227
     * Worst output (hi):      -0.4857264757 (0xBEF8B124)
     * Correct output (hi):    -0.4857265353 (0xBEF8B126)
     * Worst input (abs):       5.759584426879883000000000
     * Worst output (abs):     -0.5000017881 (0xBF00001E)
     * Correct output (abs):   -0.5000018477 (0xBF00001F)
     * Running Math.cos vs. cosJolt
     * Mean absolute error:     0.0000000003
     * Mean relative error:     0.0000000005
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000001192
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283185482025146500000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        1.318116068840026900000000
     * Highest output rel:      0.0000001192
     * Worst output (hi):       0.2500000298 (0x3E800001)
     * Correct output (hi):     0.2500000000 (0x3E800000)
     * Worst input (abs):       6.261464595794678000000000
     * Worst output (abs):      0.9997640848 (0x3F7FF08A)
     * Correct output (abs):    0.9997641444 (0x3F7FF08B)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     */
    @Test
    public void testPairs_0_PI2() {
        OrderedMap<String, FloatUnaryOperator> baselines = new OrderedMap<>(8);
        ArrayList<FloatUnaryOperator> functions = new ArrayList<>(8);
//        baselines.put("Math.sin vs. TrigTools.sin", (x) -> (float) Math.sin(x));
//        functions.add(TrigTools::sin);
//        baselines.put("Math.sin vs. TrigTools.sinSmoother", (x) -> (float) Math.sin(x));
//        functions.add(TrigTools::sinSmoother);
//        baselines.put("Math.sin vs. TrigTools.sinSmooth", (x) -> (float) Math.sin(x));
//        functions.add(TrigTools::sinSmooth);

//        baselines.put("Math.cos vs. TrigTools.cos", (x) -> (float) Math.cos(x));
//        functions.add(TrigTools::cos);
//        baselines.put("Math.cos vs. TrigTools.cosSmoother", (x) -> (float) Math.cos(x));
//        functions.add(TrigTools::cosSmoother);
//        baselines.put("Math.cos vs. TrigTools.cosSmooth", (x) -> (float) Math.cos(x));
//        functions.add(TrigTools::cosSmooth);

//        baselines.put("Math.sin vs. MathUtils.sin", (x) -> (float) Math.sin(x));
//        functions.add(MathUtils::sin);
//        baselines.put("Math.cos vs. MathUtils.cos", (x) -> (float) Math.cos(x));
//        functions.add(MathUtils::cos);

        baselines.put("Math.sin vs. sinJolt", (x) -> (float) Math.sin(x));
        functions.add(PrecisionTest::sinJolt);

        baselines.put("Math.cos vs. cosJolt", (x) -> (float) Math.cos(x));
        functions.add(PrecisionTest::cosJolt);

        for (int f = 0; f < baselines.size; f++) {
            String runName = baselines.orderedKeys().get(f);
            System.out.println("Running " + runName);
            FloatUnaryOperator baseline = baselines.get(runName);
            FloatUnaryOperator op = functions.get(f);
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            int pi2Bits = BitConversion.floatToIntBits(PI2);
            for (int ix = pi2Bits; ix >= 0; ix--) {
                final float x = BitConversion.intBitsToFloat(ix);
                float tru = baseline.applyAsFloat(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 0x1p-24f) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 0x1p-24f)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = baseline.applyAsFloat(worstAbsX),
                    highestTru = baseline.applyAsFloat(highestRelX),
                    lowestTru = baseline.applyAsFloat(lowestRelX),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
            Assert.assertTrue("Mean absolute error is broken", absError / counter < 0.5);
            Assert.assertTrue("Max absolute error is broken", maxAbsError < 0.5);
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }

    /**
     * A big test that can handle lots of different comparisons on degree inputs. Doesn't run on every possible float;
     * skips 256 ULPs at a time.
     * <br>
     * Running Math.sin vs. TrigTools.sinDeg
     * Mean absolute error:     0.0000116260
     * Mean relative error:     0.0918901265
     * Maximum abs. error:      0.0001921221
     * Maximum rel. error:      1.0039137602
     * Lowest output rel:       0.0000000000
     * Best input (lo):       359.648437500000000000000000
     * Best output (lo):       -0.0061358847 (0xBBC90F88)
     * Correct output (lo):    -0.0061358847 (0xBBC90F88)
     * Worst input (hi):        0.010964870452880860000000
     * Highest output rel:      1.0039136410
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0001913731 (0x3948AB53)
     * Worst input (abs):       0.010964870452880860000000
     * Worst output (abs):      0.0003834952 (0x39C90FDA)
     * Correct output (abs):    0.0001913731 (0x3948AB53)
     * Running Math.sin vs. TrigTools.sinDegSmoother
     * Mean absolute error:     0.0000000013
     * Mean relative error:     0.0000000129
     * Maximum abs. error:      0.0000003427
     * Maximum rel. error:      0.0005405607
     * Lowest output rel:       0.0000000000
     * Best input (lo):       359.648437500000000000000000
     * Best output (lo):       -0.0061358847 (0xBBC90F88)
     * Correct output (lo):    -0.0061358847 (0xBBC90F88)
     * Worst input (hi):      180.027343750000000000000000
     * Highest output rel:      0.0005405607
     * Worst output (hi):      -0.0004774964 (0xB9FA587C)
     * Correct output (hi):    -0.0004772384 (0xB9FA35DC)
     * Worst input (abs):     355.703125000000000000000000
     * Worst output (abs):     -0.0749239996 (0xBD9971C1)
     * Correct output (abs):   -0.0749243423 (0xBD9971EF)
     * Running Math.sin vs. TrigTools.sinSmooth
     * Mean absolute error:     0.0000005705
     * Mean relative error:     0.0000017226
     * Maximum abs. error:      0.0000988841
     * Maximum rel. error:      0.0005467201
     * Lowest output rel:       0.0000000000
     * Best input (lo):       358.828125000000000000000000
     * Best output (lo):       -0.0204516519 (0xBCA78A39)
     * Correct output (lo):    -0.0204516519 (0xBCA78A39)
     * Worst input (hi):      180.027343750000000000000000
     * Highest output rel:      0.0005467201
     * Worst output (hi):      -0.0004774994 (0xB9FA58E1)
     * Correct output (hi):    -0.0004772384 (0xB9FA35DC)
     * Worst input (abs):     270.000000000000000000000000
     * Worst output (abs):     -0.9999011159 (0xBF7FF985)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     * Running Math.cos vs. TrigTools.cos
     * Mean absolute error:     0.0000023237
     * Mean relative error:     0.0000173411
     * Maximum abs. error:      0.0001874865
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):       360.000000000000000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):      270.007812500000000000000000
     * Highest output rel:      0.9999998808
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0001363538 (0x390EFA35)
     * Worst input (abs):     270.054687500000000000000000
     * Worst output (abs):      0.0007669903 (0x3A490FD9)
     * Correct output (abs):    0.0009544768 (0x3A7A35DA)
     * Running Math.cos vs. TrigTools.cosSmoother
     * Mean absolute error:     0.0000000026
     * Mean relative error:     0.0000000116
     * Maximum abs. error:      0.0000003055
     * Maximum rel. error:      0.0006713507
     * Lowest output rel:       0.0000000000
     * Best input (lo):       360.000000000000000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):      270.023437500000000000000000
     * Highest output rel:      0.0006713506
     * Worst output (hi):       0.0004093361 (0x39D69C2B)
     * Correct output (hi):     0.0004090615 (0x39D6774F)
     * Worst input (abs):     266.414062500000000000000000
     * Worst output (abs):     -0.0625452623 (0xBD8017BB)
     * Correct output (abs):   -0.0625455678 (0xBD8017E4)
     * Running Math.cos vs. TrigTools.cosSmooth
     * Mean absolute error:     0.0000895480
     * Mean relative error:     0.0000896972
     * Maximum abs. error:      0.0000989437
     * Maximum rel. error:      0.0006775405
     * Lowest output rel:       0.0000000000
     * Best input (lo):       352.101562500000000000000000
     * Best output (lo):        0.9905132055 (0x3F7D9246)
     * Correct output (lo):     0.9905132055 (0x3F7D9246)
     * Worst input (hi):      270.023437500000000000000000
     * Highest output rel:      0.0006775405
     * Worst output (hi):       0.0004093387 (0x39D69C82)
     * Correct output (hi):     0.0004090615 (0x39D6774F)
     * Worst input (abs):       0.003194451332092285000000
     * Worst output (abs):      0.9999010563 (0x3F7FF984)
     * Correct output (abs):    1.0000000000 (0x3F800000)
     * Running Math.sin vs. sinDegJolt
     * Mean absolute error:     0.0000000003
     * Mean relative error:     0.0000000028
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000001235
     * Lowest output rel:       0.0000000000
     * Best input (lo):       359.992187500000000000000000
     * Best output (lo):       -0.0001363538 (0xB90EFA35)
     * Correct output (lo):    -0.0001363538 (0xB90EFA35)
     * Worst input (hi):       28.860839843750000000000000
     * Highest output rel:      0.0000001235
     * Worst output (hi):       0.4826838672 (0x3EF72257)
     * Correct output (hi):     0.4826839268 (0x3EF72259)
     * Worst input (abs):     329.906250000000000000000000
     * Worst output (abs):     -0.5014163256 (0xBF005CD2)
     * Correct output (abs):   -0.5014163852 (0xBF005CD3)
     * Running Math.cos vs. cosDegJolt
     * Mean absolute error:     0.0000000003
     * Mean relative error:     0.0000000005
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000001229
     * Lowest output rel:       0.0000000000
     * Best input (lo):       360.000000000000000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):      240.988281250000000000000000
     * Highest output rel:      0.0000001229
     * Worst output (hi):      -0.4849884510 (0xBEF85068)
     * Correct output (hi):    -0.4849885106 (0xBEF8506A)
     * Worst input (abs):     357.890625000000000000000000
     * Worst output (abs):      0.9993224144 (0x3F7FD398)
     * Correct output (abs):    0.9993223548 (0x3F7FD397)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     */
    @Test
    public void testPairs_0_360() {
        OrderedMap<String, FloatUnaryOperator> baselines = new OrderedMap<>(8);
        ArrayList<FloatUnaryOperator> functions = new ArrayList<>(8);
        baselines.put("Math.sin vs. TrigTools.sinDeg", (x) -> (float) Math.sin(Math.toRadians(x)));
        functions.add(TrigTools::sinDeg);
        baselines.put("Math.sin vs. TrigTools.sinDegSmoother", (x) -> (float) Math.sin(Math.toRadians(x)));
        functions.add(TrigTools::sinSmootherDeg);
        baselines.put("Math.sin vs. TrigTools.sinSmooth", (x) -> (float) Math.sin(Math.toRadians(x)));
        functions.add(TrigTools::sinSmoothDeg);

        baselines.put("Math.cos vs. TrigTools.cos", (x) -> (float) Math.cos(Math.toRadians(x)));
        functions.add(TrigTools::cosDeg);
        baselines.put("Math.cos vs. TrigTools.cosSmoother", (x) -> (float) Math.cos(Math.toRadians(x)));
        functions.add(TrigTools::cosSmootherDeg);
        baselines.put("Math.cos vs. TrigTools.cosSmooth", (x) -> (float) Math.cos(Math.toRadians(x)));
        functions.add(TrigTools::cosSmoothDeg);

//        baselines.put("Math.sin vs. MathUtils.sin", (x) -> (float) Math.sin(x));
//        functions.add(MathUtils::sin);
//        baselines.put("Math.cos vs. MathUtils.cos", (x) -> (float) Math.cos(x));
//        functions.add(MathUtils::cos);

        baselines.put("Math.sin vs. sinDegJolt", (x) -> (float) Math.sin(Math.toRadians(x)));
        functions.add(PrecisionTest::sinDegJolt);

        baselines.put("Math.cos vs. cosDegJolt", (x) -> (float) Math.cos(Math.toRadians(x)));
        functions.add(PrecisionTest::cosDegJolt);

        for (int f = 0; f < baselines.size; f++) {
            String runName = baselines.orderedKeys().get(f);
            System.out.println("Running " + runName);
            FloatUnaryOperator baseline = baselines.get(runName);
            FloatUnaryOperator op = functions.get(f);
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            int degreeBits = BitConversion.floatToIntBits(360);
            for (int ix = degreeBits; ix >= 0; ix-= 256) {
                final float x = BitConversion.intBitsToFloat(ix);
                float tru = baseline.applyAsFloat(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 0x1p-24f) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 0x1p-24f)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = baseline.applyAsFloat(worstAbsX),
                    highestTru = baseline.applyAsFloat(highestRelX),
                    lowestTru = baseline.applyAsFloat(lowestRelX),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
            Assert.assertTrue("Mean absolute error is broken", absError / counter < 0.5);
            Assert.assertTrue("Max absolute error is broken", maxAbsError < 0.5);
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }

    /**
     * Running Math.asin vs. TrigTools.asin
     * Mean absolute error:     0.0000284235
     * Mean relative error:     0.0007816033
     * Maximum abs. error:      0.0000675268
     * Maximum rel. error:     70.8044586182
     * Lowest output rel:       0.0000000000
     * Best input (lo):         1.000000000000000000000000
     * Best output (lo):        1.5707963705 (0x3FC90FDB)
     * Correct output (lo):     1.5707963705 (0x3FC90FDB)
     * Worst input (hi):        0.000000953674316406250000
     * Highest output rel:     70.8044509888
     * Worst output (hi):       0.0000684781 (0x388F9BE2)
     * Correct output (hi):     0.0000009537 (0x35800000)
     * Worst input (abs):       0.000000000000000000000000
     * Worst output (abs):      0.0000675268 (0x388D9D2C)
     * Correct output (abs):    0.0000000000 (0x00000000)
     * Running Math.acos vs. TrigTools.acos
     * Mean absolute error:     0.0000284155
     * Mean relative error:     0.0000215517
     * Maximum abs. error:      0.0000675917
     * Maximum rel. error:      0.0000477664
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.957683563232421900000000
     * Best output (lo):        0.2919530571 (0x3E957ADF)
     * Correct output (lo):     0.2919530571 (0x3E957ADF)
     * Worst input (hi):        0.999991416931152300000000
     * Highest output rel:      0.0000477664
     * Worst output (hi):       0.0041430090 (0x3B87C214)
     * Correct output (hi):     0.0041432069 (0x3B87C3BD)
     * Worst input (abs):       0.000014305114746093750000
     * Worst output (abs):      1.5707144737 (0x3FC90D2C)
     * Correct output (abs):    1.5707820654 (0x3FC90F63)
     * Running Math.asin vs. MathUtils.asin
     * Mean absolute error:     0.0000284152
     * Mean relative error:     0.0007822137
     * Maximum abs. error:      0.0000675917
     * Maximum rel. error:     70.8750000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         1.000000000000000000000000
     * Best output (lo):        1.5707963705 (0x3FC90FDB)
     * Correct output (lo):     1.5707963705 (0x3FC90FDB)
     * Worst input (hi):        0.000000953674316406250000
     * Highest output rel:     70.8749923706
     * Worst output (hi):       0.0000685453 (0x388FC000)
     * Correct output (hi):     0.0000009537 (0x35800000)
     * Worst input (abs):       0.000039100646972656250000
     * Worst output (abs):      0.0001066923 (0x38DFC000)
     * Correct output (abs):    0.0000391006 (0x38240000)
     * Running Math.acos vs. MathUtils.acos
     * Mean absolute error:     0.0000284044
     * Mean relative error:     0.0000215457
     * Maximum abs. error:      0.0000675917
     * Maximum rel. error:      0.0000478233
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.957729339599609400000000
     * Best output (lo):        0.2917939723 (0x3E956605)
     * Correct output (lo):     0.2917939723 (0x3E956605)
     * Worst input (hi):        0.999974250793457000000000
     * Highest output rel:      0.0000478233
     * Worst output (hi):       0.0071759117 (0x3BEB23E9)
     * Correct output (hi):     0.0071762549 (0x3BEB26CA)
     * Worst input (abs):       0.000039100646972656250000
     * Worst output (abs):      1.5706896782 (0x3FC90C5C)
     * Correct output (abs):    1.5707572699 (0x3FC90E93)
     * Running Math.acos vs. acosHand
     * Mean absolute error:     0.0000284155
     * Mean relative error:     0.0000215517
     * Maximum abs. error:      0.0000675917
     * Maximum rel. error:      0.0000477664
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.957683563232421900000000
     * Best output (lo):        0.2919530571 (0x3E957ADF)
     * Correct output (lo):     0.2919530571 (0x3E957ADF)
     * Worst input (hi):        0.999991416931152300000000
     * Highest output rel:      0.0000477664
     * Worst output (hi):       0.0041430090 (0x3B87C214)
     * Correct output (hi):     0.0041432069 (0x3B87C3BD)
     * Worst input (abs):       0.000014305114746093750000
     * Worst output (abs):      1.5707144737 (0x3FC90D2C)
     * Correct output (abs):    1.5707820654 (0x3FC90F63)
     * Running Math.acos vs. acosRuud
     * Mean absolute error:     0.0098056532
     * Mean relative error:     0.0089038955
     * Maximum abs. error:      0.0167646408
     * Maximum rel. error:      0.9779748321
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.942748069763183600000000
     * Best output (lo):        0.3400197923 (0x3EAE1713)
     * Correct output (lo):     0.3400197923 (0x3EAE1713)
     * Worst input (hi):        0.999999046325683600000000
     * Highest output rel:      0.9779747128
     * Worst output (hi):       0.0000304183 (0x37FF2AB2)
     * Correct output (hi):     0.0013810680 (0x3AB504F4)
     * Worst input (abs):      -0.999341011047363300000000
     * Worst output (abs):      3.1220512390 (0x4047CFB0)
     * Correct output (abs):    3.1052865982 (0x4046BD04)
     * Running Math.acos vs. acosFastGilcher
     * Mean absolute error:     0.0054346938
     * Mean relative error:     0.0039696740
     * Maximum abs. error:      0.0090129375
     * Maximum rel. error:      0.0078372313
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.999998092651367200000000
     * Best output (lo):        0.0019531252 (0x3B000001)
     * Correct output (lo):     0.0019531252 (0x3B000001)
     * Worst input (hi):        0.462948799133300800000000
     * Highest output rel:      0.0078372303
     * Worst output (hi):       1.0980156660 (0x3F8C8BC7)
     * Correct output (hi):     1.0894771814 (0x3F8B73FD)
     * Worst input (abs):       0.361389160156250000000000
     * Worst output (abs):      1.2100518942 (0x3F9AE2FB)
     * Correct output (abs):    1.2010389566 (0x3F99BBA5)
     * Running Math.sin vs. sinTurns
     * Mean absolute error:     0.0000610204
     * Mean relative error:     0.0006442947
     * Maximum abs. error:      0.0001917476
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.999938964843750000000000
     * Best output (lo):       -0.0003834952 (0xB9C90FDA)
     * Correct output (lo):    -0.0003834952 (0xB9C90FDA)
     * Worst input (hi):        0.999999046325683600000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):    -0.0000059921 (0xB6C90FDB)
     * Worst input (abs):       0.999969482421875000000000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):   -0.0001917476 (0xB9490FDB)
     * Running Math.sin vs. sinSmoothTurns
     * Mean absolute error:     0.0000172673
     * Mean relative error:     0.0000216576
     * Maximum abs. error:      0.0000988841
     * Maximum rel. error:      0.0000988841
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.771974563598632800000000
     * Best output (lo):       -0.9904834628 (0xBF7D9053)
     * Correct output (lo):    -0.9904834628 (0xBF7D9053)
     * Worst input (hi):        0.750017166137695300000000
     * Highest output rel:      0.0000988841
     * Worst output (hi):      -0.9999011159 (0xBF7FF985)
     * Correct output (hi):    -1.0000000000 (0xBF800000)
     * Worst input (abs):       0.750017166137695300000000
     * Worst output (abs):     -0.9999011159 (0xBF7FF985)
     * Correct output (abs):   -1.0000000000 (0xBF800000)
     * Running Math.sin vs. sinSmootherTurns
     * Mean absolute error:     0.0000000118
     * Mean relative error:     0.0000000190
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000020491
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.999988555908203100000000
     * Best output (lo):       -0.0000719053 (0xB896CBE4)
     * Correct output (lo):    -0.0000719053 (0xB896CBE4)
     * Worst input (hi):        0.999999046325683600000000
     * Highest output rel:      0.0000020491
     * Worst output (hi):      -0.0000059921 (0xB6C90FC0)
     * Correct output (hi):    -0.0000059921 (0xB6C90FDB)
     * Worst input (abs):       0.916658401489257800000000
     * Worst output (abs):     -0.5000449419 (0xBF0002F2)
     * Correct output (abs):   -0.5000450015 (0xBF0002F3)
     * Running Math.sin vs. sinTurnsJolt
     * Mean absolute error:     0.0000000087
     * Mean relative error:     0.0000000188
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000001243
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.999999046325683600000000
     * Best output (lo):       -0.0000059921 (0xB6C90FDB)
     * Correct output (lo):    -0.0000059921 (0xB6C90FDB)
     * Worst input (hi):        0.920406341552734400000000
     * Highest output rel:      0.0000001243
     * Worst output (hi):      -0.4795148373 (0xBEF582F8)
     * Correct output (hi):    -0.4795147777 (0xBEF582F6)
     * Worst input (abs):       0.920406341552734400000000
     * Worst output (abs):     -0.4795148373 (0xBEF582F8)
     * Correct output (abs):   -0.4795147777 (0xBEF582F6)
     * Running Math.cos vs. cosTurns
     * Mean absolute error:     0.0000610216
     * Mean relative error:     0.0006445739
     * Maximum abs. error:      0.0001917476
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         1.000000000000000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        0.750029563903808600000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0001857555 (0x3942C75C)
     * Worst input (abs):       0.749969482421875000000000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):   -0.0001917476 (0xB9490FDB)
     * Running Math.cos vs. cosSmoothTurns
     * Mean absolute error:     0.0000172754
     * Mean relative error:     0.0000216233
     * Maximum abs. error:      0.0000988841
     * Maximum rel. error:      0.0000988841
     * Lowest output rel:       0.0000000000
     * Best input (lo):         0.978088378906250000000000
     * Best output (lo):        0.9905377626 (0x3F7D93E2)
     * Correct output (lo):     0.9905377626 (0x3F7D93E2)
     * Worst input (hi):        1.000000000000000000000000
     * Highest output rel:      0.0000988841
     * Worst output (hi):       0.9999011159 (0x3F7FF985)
     * Correct output (hi):     1.0000000000 (0x3F800000)
     * Worst input (abs):       1.000000000000000000000000
     * Worst output (abs):      0.9999011159 (0x3F7FF985)
     * Correct output (abs):    1.0000000000 (0x3F800000)
     * Running Math.cos vs. cosSmootherTurns
     * Mean absolute error:     0.0000000118
     * Mean relative error:     0.0000000190
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000020491
     * Lowest output rel:       0.0000000000
     * Best input (lo):         1.000000000000000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        0.749999046325683600000000
     * Highest output rel:      0.0000020491
     * Worst output (hi):      -0.0000059921 (0xB6C90FC0)
     * Correct output (hi):    -0.0000059921 (0xB6C90FDB)
     * Worst input (abs):       0.999968528747558600000000
     * Worst output (abs):      0.9999999404 (0x3F7FFFFF)
     * Correct output (abs):    1.0000000000 (0x3F800000)
     * Running Math.cos vs. cosTurnsJolt
     * Mean absolute error:     0.0000000087
     * Mean relative error:     0.0000000188
     * Maximum abs. error:      0.0000000596
     * Maximum rel. error:      0.0000001243
     * Lowest output rel:       0.0000000000
     * Best input (lo):         1.000000000000000000000000
     * Best output (lo):        1.0000000000 (0x3F800000)
     * Correct output (lo):     1.0000000000 (0x3F800000)
     * Worst input (hi):        0.829593658447265600000000
     * Highest output rel:      0.0000001243
     * Worst output (hi):       0.4795148373 (0x3EF582F8)
     * Correct output (hi):     0.4795147777 (0x3EF582F6)
     * Worst input (abs):       0.994140625000000000000000
     * Worst output (abs):      0.9993224144 (0x3F7FD398)
     * Correct output (abs):    0.9993223548 (0x3F7FD397)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     */
    @Test
    public void testPairs_1_1() {
        OrderedMap<String, FloatUnaryOperator> baselines = new OrderedMap<>(8);
        ArrayList<FloatUnaryOperator> functions = new ArrayList<>(8);
//        baselines.put("Math.asin vs. TrigTools.asin", (x) -> (float) Math.asin(x));
//        functions.add(TrigTools::asin);
//        baselines.put("Math.acos vs. TrigTools.acos", (x) -> (float) Math.acos(x));
//        functions.add(TrigTools::acos);
//
//        baselines.put("Math.asin vs. MathUtils.asin", (x) -> (float) Math.asin(x));
//        functions.add(MathUtils::asin);
//        baselines.put("Math.acos vs. MathUtils.acos", (x) -> (float) Math.acos(x));
//        functions.add(MathUtils::acos);
//
//        baselines.put("Math.acos vs. acosHand", (x) -> (float) Math.acos(x));
//        functions.add((x) -> (float) PrecisionTest.acosHand(x));
//
//        baselines.put("Math.acos vs. acosRuud", (x) -> (float) Math.acos(x));
//        functions.add((x) -> (float) PrecisionTest.acosRuud(x));
//
//        baselines.put("Math.acos vs. acosFastGilcher", (x) -> (float) Math.acos(x));
//        functions.add(PrecisionTest::acosFastGilcher);

        baselines.put("Math.sin vs. sinTurns", (x) -> (float) Math.sin(x * PI2_D));
        functions.add(TrigTools::sinTurns);
        baselines.put("Math.sin vs. sinSmoothTurns", (x) -> (float) Math.sin(x * PI2_D));
        functions.add(TrigTools::sinSmoothTurns);
        baselines.put("Math.sin vs. sinSmootherTurns", (x) -> (float) Math.sin(x * PI2_D));
        functions.add(TrigTools::sinSmootherTurns);

        baselines.put("Math.sin vs. sinTurnsJolt", (x) -> (float) Math.sin(x * PI2_D));
        functions.add(PrecisionTest::sinTurnsJolt);


        baselines.put("Math.cos vs. cosTurns", (x) -> (float) Math.cos(x * PI2_D));
        functions.add(TrigTools::cosTurns);
        baselines.put("Math.cos vs. cosSmoothTurns", (x) -> (float) Math.cos(x * PI2_D));
        functions.add(TrigTools::cosSmoothTurns);
        baselines.put("Math.cos vs. cosSmootherTurns", (x) -> (float) Math.cos(x * PI2_D));
        functions.add(TrigTools::cosSmootherTurns);

        baselines.put("Math.cos vs. cosTurnsJolt", (x) -> (float) Math.cos(x * PI2_D));
        functions.add(PrecisionTest::cosTurnsJolt);

        for (int f = 0; f < baselines.size; f++) {
            String runName = baselines.orderedKeys().get(f);
            System.out.println("Running " + runName);
            FloatUnaryOperator baseline = baselines.get(runName);
            FloatUnaryOperator op = functions.get(f);
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (float x = 1f; x >= -1f; x-= 0x1p-20f) {
                float tru = baseline.applyAsFloat(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 1E-10) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 1E-10)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = baseline.applyAsFloat(worstAbsX),
                    highestTru = baseline.applyAsFloat(highestRelX),
                    lowestTru = baseline.applyAsFloat(lowestRelX),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
//            Assert.assertTrue("Mean absolute error is broken", absError / counter < 0.5);
//            Assert.assertTrue("Max absolute error is broken", maxAbsError < 0.5);
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }

    /**
     * Running Math.atan vs. TrigTools.atan
     * Mean absolute error:     0.0000009209
     * Mean relative error:     0.2908751965
     * Maximum abs. error:      0.0000016689
     * Maximum rel. error:     27.9064464569
     * Lowest output rel:       0.0000000000
     * Best input (lo):        28.402832031250000000000000
     * Best output (lo):        1.5356031656 (0x3FC48EA5)
     * Correct output (lo):     1.5356031656 (0x3FC48EA5)
     * Worst input (hi):        0.000000059605099522741510
     * Highest output rel:     27.9064445496
     * Worst output (hi):       0.0000017230 (0x35E740DB)
     * Correct output (hi):     0.0000000596 (0x33800040)
     * Worst input (abs):      49.982910156250000000000000
     * Worst output (abs):      1.5507937670 (0x3FC68069)
     * Correct output (abs):    1.5507920980 (0x3FC6805B)
     * Running Math.atan vs. TrigTools.atanUnchecked
     * Mean absolute error:     0.0000009209
     * Mean relative error:     0.2908751965
     * Maximum abs. error:      0.0000016689
     * Maximum rel. error:     27.9064464569
     * Lowest output rel:       0.0000000000
     * Best input (lo):        28.402832031250000000000000
     * Best output (lo):        1.5356031656 (0x3FC48EA5)
     * Correct output (lo):     1.5356031656 (0x3FC48EA5)
     * Worst input (hi):        0.000000059605099522741510
     * Highest output rel:     27.9064445496
     * Worst output (hi):       0.0000017230 (0x35E740DB)
     * Correct output (hi):     0.0000000596 (0x33800040)
     * Worst input (abs):      49.982910156250000000000000
     * Worst output (abs):      1.5507937670 (0x3FC68069)
     * Correct output (abs):    1.5507920980 (0x3FC6805B)
     * Running Math.atan vs. atanJolt
     * Mean absolute error:     0.0000000020
     * Mean relative error:     0.0000000021
     * Maximum abs. error:      0.0000002384
     * Maximum rel. error:      0.0000002370
     * Lowest output rel:       0.0000000000
     * Best input (lo):        50.000000000000000000000000
     * Best output (lo):        1.5507990122 (0x3FC68095)
     * Correct output (lo):     1.5507990122 (0x3FC68095)
     * Worst input (hi):        0.550205230712890600000000
     * Highest output rel:      0.0000002370
     * Worst output (hi):       0.5030008554 (0x3F00C4AA)
     * Correct output (hi):     0.5030007362 (0x3F00C4A8)
     * Worst input (abs):       3.909286499023437500000000
     * Worst output (abs):      1.3203654289 (0x3FA901BC)
     * Correct output (abs):    1.3203651905 (0x3FA901BA)
     * -------
     * Epsilon is:              0.0000000596
     * -------
     */
    @Test
    public void testPairs_50_50() {
        OrderedMap<String, FloatUnaryOperator> baselines = new OrderedMap<>(8);
        ArrayList<FloatUnaryOperator> functions = new ArrayList<>(8);
        baselines.put("Math.atan vs. TrigTools.atan", (x) -> (float) Math.atan(x));
        functions.add(TrigTools::atan);
        baselines.put("Math.atan vs. TrigTools.atanUnchecked", (x) -> (float) Math.atan(x));
        functions.add(i -> (float) TrigTools.atanUnchecked(i));
        baselines.put("Math.atan vs. atanJolt", (x) -> (float) Math.atan(x));
        functions.add(PrecisionTest::atanJolt);

        for (int f = 0; f < baselines.size; f++) {
            String runName = baselines.orderedKeys().get(f);
            System.out.println("Running " + runName);
            FloatUnaryOperator baseline = baselines.get(runName);
            FloatUnaryOperator op = functions.get(f);
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            int degreeBits = BitConversion.floatToIntBits(50), endBits = degreeBits | 0x80000000;
            for (int ix = degreeBits; ix >= 0; ix-= 64) {
                final float x = BitConversion.intBitsToFloat(ix);
                float tru = baseline.applyAsFloat(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 0x1p-24f) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 0x1p-24f)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            for (int ix = endBits; ix > 0x80000000; ix-= 64) {
                final float x = BitConversion.intBitsToFloat(ix);
                float tru = baseline.applyAsFloat(x),
                        approx = op.applyAsFloat(x),
                        err = tru - approx,
                        ae = abs(err),
                        re = MathTools.isZero(tru, 0x1p-24f) ? 0f : Math.abs(err / tru);
                if(!MathTools.isZero(tru, 0x1p-24f)) {
                    relError += re;
                    if (maxRelError != (maxRelError = Math.max(maxRelError, re))) {
                        highestRelX = x;
                    }
                    if (minRelError != (minRelError = Math.min(minRelError, re))) {
                        lowestRelX = x;
                    }
                }
                absError += ae;
                if (maxAbsError != (maxAbsError = Math.max(maxAbsError, ae))) {
                    worstAbsX = x;
                }
                ++counter;
            }
            float worstAbs = op.applyAsFloat(worstAbsX),
                    worstTru = baseline.applyAsFloat(worstAbsX),
                    highestTru = baseline.applyAsFloat(highestRelX),
                    lowestTru = baseline.applyAsFloat(lowestRelX),
                    lowestErr = lowestTru - op.applyAsFloat(lowestRelX),
                    lowestRel = abs(lowestErr / Math.nextAfter(lowestTru, Math.copySign(Float.MAX_VALUE, lowestTru))),
                    highestErr = highestTru - op.applyAsFloat(highestRelX),
                    highestRel = abs(highestErr / Math.nextAfter(highestTru, Math.copySign(Float.MAX_VALUE, highestTru)));
            System.out.printf(
                    "Mean absolute error: %16.10f\n" +
                            "Mean relative error: %16.10f\n" +
                            "Maximum abs. error:  %16.10f\n" +
                            "Maximum rel. error:  %16.10f\n" +
                            "Lowest output rel:   %16.10f\n" +
                            "Best input (lo):     %30.24f\n" +
                            "Best output (lo):    %16.10f (0x%08X)\n" +
                            "Correct output (lo): %16.10f (0x%08X)\n" +
                            "Worst input (hi):    %30.24f\n" +
                            "Highest output rel:  %16.10f\n" +
                            "Worst output (hi):   %16.10f (0x%08X)\n" +
                            "Correct output (hi): %16.10f (0x%08X)\n" +
                            "Worst input (abs):   %30.24f\n" +
                            "Worst output (abs):  %16.10f (0x%08X)\n" +
                            "Correct output (abs):%16.10f (0x%08X)\n", absError / counter, relError / counter,
                    maxAbsError, maxRelError,
                    lowestRel, lowestRelX, op.applyAsFloat(lowestRelX), Float.floatToIntBits(op.applyAsFloat(lowestRelX)), lowestTru, Float.floatToIntBits(lowestTru),
                    highestRelX, highestRel, op.applyAsFloat(highestRelX), Float.floatToIntBits(op.applyAsFloat(highestRelX)), highestTru, Float.floatToIntBits(highestTru),
                    worstAbsX, worstAbs, Float.floatToIntBits(worstAbs), worstTru, Float.floatToIntBits(worstTru));
            Assert.assertTrue("Mean absolute error is broken", absError / counter < 0.5);
            Assert.assertTrue("Max absolute error is broken", maxAbsError < 0.5);
        }
        System.out.printf("-------\n" +
                "Epsilon is:          %16.10f\n-------\n", 0x1p-24f);
    }
}
