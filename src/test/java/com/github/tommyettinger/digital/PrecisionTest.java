package com.github.tommyettinger.digital;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.abs;

// REMOVE the @Ignore if you want to run any tests! They take a while to run as a whole, though.
//@Ignore
public class PrecisionTest {
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

    @Test
    public void testSin() {
        HashMap<String, FloatUnaryOperator> functions = new HashMap<>(8);
        functions.put("sinSmooth", TrigTools::sinSmooth);
        functions.put("sinNewTable", TrigTools::sin);
        functions.put("sinOldTable", OldTrigTools::sin);
        functions.put("sinNick", PrecisionTest::sinNick);
        functions.put("sinLeibovici", PrecisionTest::sinLeibovici);
        functions.put("sinSteadman", PrecisionTest::sinSteadman);

        for (Map.Entry<String, FloatUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final FloatUnaryOperator op = ent.getValue();
            double absError = 0.0, relError = 0.0, maxError = 0.0;
            float worstX = 0;
            long counter = 0L;
            for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

                double tru = (float) Math.sin(x),
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
                    "Mean absolute error: %3.10f\n" +
                            "Mean relative error: %3.10f\n" +
                            "Maximum error:       %3.8f\n" +
                            "Worst input:         %3.8f\n" +
                            "Worst approx output: %3.8f\n" +
                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, op.applyAsFloat(worstX), (float) Math.sin(worstX));
        }
    }

    @Test
    public void testSinDeg() {
        HashMap<String, FloatUnaryOperator> functions = new HashMap<>(8);
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
        HashMap<String, FloatUnaryOperator> functions = new HashMap<>(8);
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


    @Test
    public void testSinD() {
        HashMap<String, DoubleUnaryOperator> functions = new HashMap<>(8);
        functions.put("sinSmooth", TrigTools::sinSmooth);
        functions.put("sinNewTable", TrigTools::sin);
        functions.put("sinOldTable", OldTrigTools::sin);

        for (Map.Entry<String, DoubleUnaryOperator> ent : functions.entrySet()) {
            System.out.println("Running " + ent.getKey());
            final DoubleUnaryOperator op = ent.getValue();
            double absError = 0.0, relError = 0.0, maxError = 0.0;
            float worstX = 0;
            long counter = 0L;
            for (float x = -TrigTools.PI2; x <= TrigTools.PI2; x += 0x1p-20f) {

                double tru = Math.sin(x),
                        err = op.applyAsDouble(x) - tru,
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
                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, op.applyAsDouble(worstX), Math.sin(worstX));
        }
    }


    @Test
    public void testCos() {
        HashMap<String, FloatUnaryOperator> functions = new HashMap<>(8);
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

                double tru =  (float) Math.cos(x),
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
                            "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, op.applyAsFloat(worstX), (float) Math.cos(worstX));
        }
    }

    @Test
    public void testSinSquared() {
        HashMap<String, FloatUnaryOperator> functions = new HashMap<>(8);
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
        HashMap<String, FloatUnaryOperator> functions = new HashMap<>(8);
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


    @Test
    public void testTan(){
        double absError = 0.0, relError = 0.0, maxError = 0.0;
        float worstX = 0;
        long counter = 0L;
        // 1.57 is just inside half-pi. This avoids testing the extremely large results at close to half-pi.
        // near half-pi, the correct result becomes tremendously large, and this doesn't grow as quickly.
        for (float x = -1.57f; x <= 1.57f; x += 0x1p-20f) {

            double tru = (float) Math.tan(x),
                    err = TrigTools.tan(x) - tru,
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
                "Correct output:      %3.8f\n", absError / counter, relError / counter, maxError, worstX, TrigTools.tan(worstX), (float)Math.tan(worstX));
    }

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
        return TrigTools.SIN_TABLE[(int) (radians * TrigTools.radToIndex) & TrigTools.TABLE_MASK];
    }

    public static float sinNewTable2(float radians) {
        //Absolute error:   0.00015258
        //Relative error:   -0.00000001
        //Maximum error:    0.00057527
        //Worst input:      -3.14101744
        //Worst approx output: -0.00115049
        //Correct output:      -0.00057522
        return TrigTools.SIN_TABLE[(int) (radians * TrigTools.radToIndex + 0.5f) & TrigTools.TABLE_MASK];
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
        //Mean absolute error: 0.0001498343
        //Mean relative error: 0.0002477639
        //Maximum error:       0.00035501
        //Worst input:         -4.20848227
        //Worst approx output: 0.87534791
        //Correct output:      0.87570292
        radians = radians * (TrigTools.PI_INVERSE * 2f);
        final int ceil = (int) Math.ceil(radians) & -2;
        radians -= ceil;
        final float x2 = radians * radians, x3 = radians * x2;
        return (((11 * radians - 3 * x3) / (7 + x2)) * (1 - (ceil & 2)));
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
        final int idx = (int) (radians * TrigTools.TABLE_SIZE / TrigTools.PI2) & TrigTools.TABLE_MASK;
        return TrigTools.SIN_TABLE[idx] / TrigTools.SIN_TABLE[idx + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
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
}
