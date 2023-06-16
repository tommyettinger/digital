package com.github.tommyettinger.digital;

import org.junit.Ignore;
import org.junit.Test;

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

    @Test
    public void testAtan2(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstY = 0, worstX = 0;
        long counter = 0L;
        for (int i = Float.floatToIntBits(1f), n = Float.floatToIntBits(2f); i < n; i+=511) {
            float x = Float.intBitsToFloat(i) - 1.5f;
            for (int j = Float.floatToIntBits(1f); j < n; j+=511) {
                float y = Float.intBitsToFloat(j) - 1.5f;
                double tru = Math.atan2(y, x),
                        err = TrigTools.atan2(y, x) - tru,
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

    /**
     * <pre>
     * Running sinNewTable
     * Mean absolute error:     0.0000075369
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003835472
     * Maximum rel. error:   2538.7736816406
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.734020233154297000000000
     * Best output (lo):       -0.5219752789 (0xBF05A02C)
     * Correct output (lo):    -0.5219752789 (0xBF05A02C)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:   2538.7736816406
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       6.282034873962402000000000
     * Worst output (abs):     -0.0015339801 (0xBAC90FD5)
     * Correct output (abs):   -0.0011504330 (0xBA96CA20)
     * Running sinOldTable
     * Mean absolute error:     0.0000059195
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:   1267.9167480469
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
     * Running sin00Prior
     * Mean absolute error:     0.0000075369
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003835472
     * Maximum rel. error:   2538.7736816406
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.734020233154297000000000
     * Best output (lo):       -0.5219752789 (0xBF05A02C)
     * Correct output (lo):    -0.5219752789 (0xBF05A02C)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:   2538.7736816406
     * Worst output (hi):       0.0003834952 (0x39C90FDA)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       6.282034873962402000000000
     * Worst output (abs):     -0.0015339801 (0xBAC90FD5)
     * Correct output (abs):   -0.0011504330 (0xBA96CA20)
     * Running sin05Prior
     * Mean absolute error:     0.0000059195
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:   1268.8868408203
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.243493556976318000000000
     * Best output (lo):       -0.0396813303 (0xBD2288E4)
     * Correct output (lo):    -0.0396813303 (0xBD2288E4)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:   1268.8868408203
     * Worst output (hi):       0.0001917476 (0x39490FDB)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       0.000383495178539305900000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):    0.0003834952 (0x39C90FDA)
     * Running sin05
     * Mean absolute error:     0.0000059195
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:   1267.9167480469
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
     * Running sin025
     * Mean absolute error:     0.0000075369
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:   1902.6645507813
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.282897949218750000000000
     * Best output (lo):       -0.0002873580 (0xB996A888)
     * Correct output (lo):    -0.0002873580 (0xB996A888)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:   1902.6645507813
     * Worst output (hi):       0.0002874454 (0x3996B444)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       0.000383495178539305900000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):    0.0003834952 (0x39C90FDA)
     * Running sinPhi
     * Mean absolute error:     0.0000059195
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:    967.9114990234
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283039093017578000000000
     * Best output (lo):       -0.0001462142 (0xB9195111)
     * Correct output (lo):    -0.0001462142 (0xB9195111)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:    967.9114990234
     * Worst output (hi):       0.0001463016 (0x39196888)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       0.000383495178539305900000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):    0.0003834952 (0x39C90FDA)
     * Running sin0625
     * Mean absolute error:     0.0000059195
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:    950.5427856445
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283041477203369000000000
     * Best output (lo):       -0.0001438300 (0xB916D111)
     * Correct output (lo):    -0.0001438300 (0xB916D111)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:    950.5427856445
     * Worst output (hi):       0.0001436790 (0x3916A888)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       0.000383495178539305900000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):    0.0003834952 (0x39C90FDA)
     * Running sin065625
     * Mean absolute error:     0.0000065251
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:    871.5940551758
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283053874969482000000000
     * Best output (lo):       -0.0001314322 (0xB909D111)
     * Correct output (lo):    -0.0001314322 (0xB909D111)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:    871.5940551758
     * Worst output (hi):       0.0001317580 (0x390A2888)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       0.000383495178539305900000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):    0.0003834952 (0x39C90FDA)
     * Running sin075
     * Mean absolute error:     0.0000075369
     * Mean relative error:     0.0154355764
     * Maximum abs. error:      0.0003834952
     * Maximum rel. error:    633.1688842773
     * Lowest output rel:       0.0000000000
     * Best input (lo):         6.283089637756348000000000
     * Best output (lo):       -0.0000956694 (0xB8C8A221)
     * Correct output (lo):    -0.0000956694 (0xB8C8A221)
     * Worst input (hi):        3.141592502593994000000000
     * Highest output rel:    633.1688842773
     * Worst output (hi):       0.0000957568 (0x38C8D111)
     * Correct output (hi):     0.0000001510 (0x34222169)
     * Worst input (abs):       0.000383495178539305900000
     * Worst output (abs):      0.0000000000 (0x00000000)
     * Correct output (abs):    0.0003834952 (0x39C90FDA)
     * Running sinSmooth
     * Mean absolute error:     0.0000037685
     * Mean relative error:     0.0000075412
     * Maximum abs. error:      0.0003550053
     * Maximum rel. error:      0.9999999404
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.759525299072266000000000
     * Best output (lo):       -0.5000530481 (0xBF00037A)
     * Correct output (lo):    -0.5000530481 (0xBF00037A)
     * Worst input (hi):        6.283185482025146500000000
     * Highest output rel:      0.9999999404
     * Worst output (hi):       0.0000000000 (0x00000000)
     * Correct output (hi):     0.0000001748 (0x343BBD2E)
     * Worst input (abs):       4.208482265472412000000000
     * Worst output (abs):     -0.8753479123 (0xBF6016CD)
     * Correct output (abs):   -0.8757029176 (0xBF602E11)
     * Running sinSmoother
     * Mean absolute error:     0.0000000013
     * Mean relative error:     0.0000000233
     * Maximum abs. error:      0.0000004470
     * Maximum rel. error:      0.9999999404
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
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinNewTable", TrigTools::sin);
        functions.put("sinOldTable", OldTrigTools::sin);
//        functions.put("sin00Prior", (f) -> sin(prior00, f));
//        functions.put("sin05Prior", (f) -> sin(prior05, f));
//        functions.put("sin05", (f) -> sin(table05, f));
//        functions.put("sin025", (f) -> sin(table025, f));
//        functions.put("sinPhi", (f) -> sin(tablePhi, f));
//        functions.put("sin0625", (f) -> sin(table0625, f));
//        functions.put("sin065625", (f) -> sin(table065625, f));
//        functions.put("sin075", (f) -> sin(table075, f));
        functions.put("sinSmooth", TrigTools::sinSmooth);
        functions.put("sinSmoother", TrigTools::sinSmoother);

//        functions.put("sinCurve", PrecisionTest::sinCurve);
//        functions.put("sinNick", PrecisionTest::sinNick);
//        functions.put("sinLeibovici", PrecisionTest::sinLeibovici);
//        functions.put("sinSteadman", PrecisionTest::sinSteadman);
//        functions.put("sinBhaskara2", PrecisionTest::sinBhaskaraI);
        functions.put("sinGreen", PrecisionTest::sinBhaskaraI);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (int i = PI2BITS; i >= 0; i--) {
                float x = Float.intBitsToFloat(i);
                float tru = (float) Math.sin(x),
                        err = tru - op.applyAsFloat(x),
                        ae = abs(err),
                        re = Math.abs(err / Math.nextAfter(tru, Math.copySign(Float.MAX_VALUE, tru)));
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

    @Test
    public void testSinDeg() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinSmootherDeg", TrigTools::sinSmootherDeg);
        functions.put("sinSmoothDeg", TrigTools::sinSmoothDeg);
        functions.put("sinDegNewTable", TrigTools::sinDeg);
        functions.put("sinDegOldTable", OldTrigTools::sinDeg);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            double absError = 0.0, relError = 0.0, maxError = 0.0;
            float worstX = 0;
            long counter = 0L;
            for (float x = -360; x <= 360; x += 0x1p-14f) {

                double tru = (float) Math.sin(Math.toRadians(x)),
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
                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, op.applyAsFloat(worstX), (float) Math.sin(Math.toRadians(worstX)));
        }
    }

    @Test
    public void testSinTurns() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinSmootherTurns", TrigTools::sinSmootherTurns);
        functions.put("sinSmoothTurns", TrigTools::sinSmoothTurns);
        functions.put("sinTurnsNewTable", TrigTools::sinTurns);
        functions.put("sinTurnsOldTable", OldTrigTools::sinTurns);

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
     * <pre>
     * Running sinNewTable
     * Mean absolute error:     0.0001220703
     * Mean relative error:     0.0027473188
     * Maximum abs. error:      0.0003834943
     * Maximum rel. error:  96636.8593865985
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  96636.8593865985
     * Worst output (hi):       0.0003834952 (0x3F3921FB49EE4A61)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):       3.142743138474873600000000
     * Worst output (abs):     -0.0007669903 (0xBF4921FB2AECB1DF)
     * Correct output (abs):   -0.0011504846 (0xBF52D97B776AE8FA)
     * Running sinOldTable
     * Mean absolute error:     0.0001220982
     * Mean relative error:     0.0022596092
     * Maximum abs. error:      0.0005755700
     * Maximum rel. error:  48281.0178162729
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  48281.0178162729
     * Worst output (hi):       0.0001916011 (0x3F291D1108B21DD4)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -0.001150461035319239000000
     * Worst output (abs):     -0.0005748907 (0xBF42D688743A52A1)
     * Correct output (abs):   -0.0011504608 (0xBF52D961DBA64784)
     * Running sin00Prior
     * Mean absolute error:     0.0001220703
     * Mean relative error:     0.0027473188
     * Maximum abs. error:      0.0003834943
     * Maximum rel. error:  96636.8593865985
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  96636.8593865985
     * Worst output (hi):       0.0003834952 (0x3F3921FB49EE4A61)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):       3.142743138474873600000000
     * Worst output (abs):     -0.0007669903 (0xBF4921FB2AECB1DF)
     * Correct output (abs):   -0.0011504846 (0xBF52D97B776AE8FA)
     * Running sin05Prior
     * Mean absolute error:     0.0001220703
     * Mean relative error:     0.0022598198
     * Maximum abs. error:      0.0005752416
     * Maximum rel. error:  48317.9305816451
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  48317.9305816451
     * Worst output (hi):       0.0001917476 (0x3F2921FB51AEDA09)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -3.141209157305308300000000
     * Worst output (abs):     -0.0009587378 (0xBF4F6A79D89661F2)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA249E)
     * Running sin05
     * Mean absolute error:     0.0001220703
     * Mean relative error:     0.0022598198
     * Maximum abs. error:      0.0005752416
     * Maximum rel. error:  48317.9305816451
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  48317.9305816451
     * Worst output (hi):       0.0001917476 (0x3F2921FB51AEDA09)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -3.141209157305308300000000
     * Worst output (abs):     -0.0009587378 (0xBF4F6A79D89661F2)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA249E)
     * Running sin025
     * Mean absolute error:     0.0001118998
     * Mean relative error:     0.0024166884
     * Maximum abs. error:      0.0004793678
     * Maximum rel. error:  72477.3953172247
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  72477.3953172247
     * Worst output (hi):       0.0002876214 (0x3F32D97C7AD6EEFB)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -3.141209157305308300000000
     * Worst output (abs):     -0.0008628641 (0xBF4C463A83EF9ED0)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA249E)
     * Running sinPhi
     * Mean absolute error:     0.0001339413
     * Mean relative error:     0.0022477665
     * Maximum abs. error:      0.0006205070
     * Maximum rel. error:  36911.3784584334
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  36911.3784584334
     * Worst output (hi):       0.0001464821 (0x3F23331FEAD36BA6)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -3.141209157305308300000000
     * Worst output (abs):     -0.0010040033 (0xBF507318535651AD)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA249E)
     * Running sin0625
     * Mean absolute error:     0.0001347835
     * Mean relative error:     0.0022483351
     * Maximum abs. error:      0.0006231784
     * Maximum rel. error:  36238.1980334245
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  36238.1980334245
     * Worst output (hi):       0.0001438107 (0x3F22D97C7E1C4330)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -3.141209157305308300000000
     * Worst output (abs):     -0.0010066747 (0xBF507E4CC09031EA)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA249E)
     * Running sin065625
     * Mean absolute error:     0.0001387564
     * Mean relative error:     0.0022526745
     * Maximum abs. error:      0.0006351627
     * Maximum rel. error:  33218.2648821613
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  33218.2648821613
     * Worst output (hi):       0.0001318265 (0x3F21475CC917DFD8)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -3.141209157305308300000000
     * Worst output (abs):     -0.0010186589 (0xBF50B090B5885059)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA249E)
     * Running sin075
     * Mean absolute error:     0.0001525821
     * Mean relative error:     0.0022836264
     * Maximum abs. error:      0.0006711153
     * Maximum rel. error:  24158.4654018163
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796342688151300000000
     * Best output (lo):       -1.0000000000 (0xBFF0000000000000)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFFF)
     * Worst input (hi):        3.141592649621419000000000
     * Highest output rel:  24158.4654018163
     * Worst output (hi):       0.0000958738 (0x3F1921FB539EC565)
     * Correct output (hi):     0.0000000040 (0x3E310B4608D3131A)
     * Worst input (abs):      -3.141209157305308300000000
     * Worst output (abs):     -0.0010546116 (0xBF51475C94326A6D)
     * Correct output (abs):   -0.0003834963 (0xBF3921FFF5AA249E)
     * Running sinSmooth
     * Mean absolute error:     0.0001498261
     * Mean relative error:     0.0002467752
     * Maximum abs. error:      0.0003547083
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):        -1.570796402292796000000000
     * Best output (lo):       -1.0000000000 (0xBFEFFFFFFFFFFFE6)
     * Correct output (lo):    -1.0000000000 (0xBFEFFFFFFFFFFFE6)
     * Worst input (hi):       -3.141592653589793000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):      -0.0000000000 (0x8000000000000000)
     * Correct output (hi):    -0.0000000000 (0xBCA1A62633145C07)
     * Worst input (abs):       5.215633002919606000000000
     * Worst output (abs):     -0.8756679714 (0xBFEC0578D667BC27)
     * Correct output (abs):   -0.8760226797 (0xBFEC0860B6F7C06A)
     * Running sinSmoother
     * Mean absolute error:     0.0000000078
     * Mean relative error:     0.0000000186
     * Maximum abs. error:      0.0000000184
     * Maximum rel. error:      1.0000000000
     * Lowest output rel:       0.0000000000
     * Best input (lo):         5.076709417504720000000000
     * Best output (lo):       -0.9343661149 (0xBFEDE653C445B3A2)
     * Correct output (lo):    -0.9343661149 (0xBFEDE653C445B3A5)
     * Worst input (hi):       -3.141592653589793000000000
     * Highest output rel:      1.0000000000
     * Worst output (hi):       0.0000000000 (0x0000000000000000)
     * Correct output (hi):    -0.0000000000 (0xBCA1A62633145C07)
     * Worst input (abs):      -1.570988090830393700000000
     * Worst output (abs):     -0.9999999632 (0xBFEFFFFFEC425456)
     * Correct output (abs):   -0.9999999816 (0xBFEFFFFFF620F2B6)
     * </pre>
     */
    @Test
    public void testSinD() {
        double[] prior00 = makeTableDoublePrior(0.0);
        double[] prior05 = makeTableDoublePrior(0.5);
        double[] table05 = makeTableDouble(0.5);
        double[] table025 = makeTableDouble(0.25);
        double[] tablePhi = makeTableDouble(MathTools.GOLDEN_RATIO_INVERSE_D);
        double[] table0625 = makeTableDouble(0.625);
        double[] table065625 = makeTableDouble(0.65625);
        double[] table075 = makeTableDouble(0.75);

        LinkedHashMap<String, DoubleUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("sinNewTable", TrigTools::sin);
        functions.put("sinOldTable", OldTrigTools::sin);
        functions.put("sin00Prior", (f) -> sin(prior00, f));
        functions.put("sin05Prior", (f) -> sin(prior05, f));
        functions.put("sin05", (f) -> sin(table05, f));
        functions.put("sin025", (f) -> sin(table025, f));
        functions.put("sinPhi", (f) -> sin(tablePhi, f));
        functions.put("sin0625", (f) -> sin(table0625, f));
        functions.put("sin065625", (f) -> sin(table065625, f));
        functions.put("sin075", (f) -> sin(table075, f));
        functions.put("sinSmooth", TrigTools::sinSmooth);
        functions.put("sinSmoother", PrecisionTest::sinSmoother);

        for (Map.Entry<String, DoubleUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final DoubleUnaryOperator op = ent.getValue();
            double absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            double worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (double x = -PI_D; x <= PI2_D; x += 0x1p-24) {

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


    @Test
    public void testCos() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("cosSmoother", PrecisionTest::cosSmoother);
        functions.put("cosSmooth", TrigTools::cosSmooth);
        functions.put("cosNewTable", TrigTools::cos);
        functions.put("cosOldTable", OldTrigTools::cos);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (float x = -TrigTools.PI; x <= TrigTools.PI2; x += 0x1p-20f) {

                float tru = (float) Math.cos(x),
                        err = tru - op.applyAsFloat(x),
                        ae = abs(err),
                        re = Math.abs(err / Math.nextAfter(tru, Math.copySign(Float.MAX_VALUE, tru)));
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

    @Test
    public void testTan() {
        LinkedHashMap<String, FloatUnaryOperator> functions = new LinkedHashMap<>(8);
        functions.put("tanSmoother", PrecisionTest::tanSmoother);
        functions.put("tanNoTable", TrigTools::tan);
        functions.put("tanTable", PrecisionTest::tanTable);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            float absError = 0.0f, relError = 0.0f, maxAbsError = 0.0f, maxRelError = 0.0f, minRelError = Float.MAX_VALUE;
            float worstAbsX = 0, highestRelX = 0, lowestRelX = 0;
            long counter = 0L;
            for (float x = -1.57f; x <= 1.57f; x += 0x1p-20f) {

                float tru = (float) Math.tan(x),
                        err = tru - op.applyAsFloat(x),
                        ae = abs(err),
                        re = Math.abs(err / Math.nextAfter(tru, Math.copySign(Float.MAX_VALUE, tru)));
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
                x = 0.7853981633974483f - radians - floor * 0.7853981633974483f;
                final float x2 = x * x;
                x *= (1f + x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
                return (float)Math.sqrt(1f - x * x);
            }
            case 6: {
                x = 0.7853981633974483f - radians - floor * 0.7853981633974483f;
                final float x2 = x * x;
                x *= (-1f - x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
                return (float)Math.sqrt(1f - x * x);
            }
            case 7: {
                x = radians - floor * 0.7853981633974483f;
                final float x2 = x * x;
                return x * (-1f - x2 * (-0x2aaaa9p-24f + x2 * (0.00833220803f + x2 * 0.000195168955f)));
            }
            default:
                throw new UnsupportedOperationException("AAA NOT DONE YET!!!");
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

        radians *= radToIndex;
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
        functions.put("sinSmoother", PrecisionTest::sinSmoother);
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

}
