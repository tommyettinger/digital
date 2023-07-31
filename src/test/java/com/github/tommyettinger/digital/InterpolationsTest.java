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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.junit.Assert;
import org.junit.Test;

public class InterpolationsTest {
    @Test
    public void compareResults() throws ReflectionException {
        Field[] interpolationFields = ClassReflection.getFields(Interpolation.class);

        // see how many fields are actually interpolations (for safety; other fields may be added in the future)
        int interpolationMembers = 0;
        for (int i = 0; i < interpolationFields.length; i++)
            if (ClassReflection.isAssignableFrom(Interpolation.class, interpolationFields[i].getDeclaringClass()))
                interpolationMembers++;

        // get interpolation names
        String[] interpolationNames = new String[interpolationMembers];
        for (int i = 0; i < interpolationFields.length; i++)
            if (ClassReflection.isAssignableFrom(Interpolation.class, interpolationFields[i].getDeclaringClass()))
                interpolationNames[i] = interpolationFields[i].getName();

        // compare results with Interpolations from here
        Interpolations.Interpolator current;
        for (int i = 0; i < interpolationNames.length; i++) {
            Assert.assertNotNull(current = Interpolations.get(interpolationNames[i]));
            Interpolation interp = (Interpolation) interpolationFields[i].get(Interpolation.class);
            Method applier = ClassReflection.getMethod(Interpolation.class, "apply", Float.TYPE);
            System.out.println(current.tag + " dig: 0.0->" + current.apply(0f) + ": 0.25->" + current.apply(0.25f) + ": 0.75->"+current.apply(0.75f) + ": 1.0->"+current.apply(1f));
            System.out.println(current.tag + " GDX: 0.0->" + applier.invoke(interp, 0f) + ": 0.25->" + applier.invoke(interp, 0.25f) + ": 0.75->"+applier.invoke(interp, 0.75f) + ": 1.0->"+applier.invoke(interp, 1f));
            for (int j = 0; j <= 16; j++) {
                Assert.assertEquals(
                        interpolationNames[i] + " on " + (j * 0.0625f),
//                if(!MathTools.isEqual(
                        current.apply(j * 0.0625f)
                        , (Float) applier.invoke(interp, j * 0.0625f)
                        , 2e-4)
//                )
                    ;
//                {
//                    System.out.println("PROBLEM: " + interpolationNames[i] + " at " + (j * 0.0625f));
//                    System.out.println("  " + current.apply(j * 0.0625f) + " vs. " + applier.invoke(interp, j * 0.0625f));
//                }
            }
        }
    }

    @Test
    public void testMonotonicity() {
        float old = 0f, next = 0f;
        int failures = 0, boundFailures = 0;
        AlternateRandom r = new AlternateRandom();
        for (int i = 1; i < 0x1000000; i++) {
            float a = i * 0x1p-24f;
            //0x1p-24f: Out of 16777216 tested floats, there were 2099462 failures in monotonicity and 6451 violations of the bounds.
            //0x1p-23f: Out of 16777216 tested floats, there were 1157781 failures in monotonicity.
            next = a * a * a * (a * (a * 5.9999995f - 15f) + 10f);
            //0x1p-24f: Out of 16777216 tested floats, there were 2099942 failures in monotonicity and 0 violations of the bounds.
            next = a * a * a * (a * (a * 6f - 15f) + 9.999998f); // returns 0.99999887f when given 1f.
            //0x1p-24f: Out of 16777216 tested floats, there were 2100008 failures in monotonicity and 22237 violations of the bounds.
            //0x1p-23f: Out of 16777216 tested floats, there were 1010361 failures in monotonicity.
//            next = a * a * a * (a * (a * 6 - 15) + 10);
            //0x1p-24f: Out of 16777216 tested floats, there were 2099439 failures in monotonicity and 22050 violations of the bounds.
            //0x1p-23f: Out of 16777216 tested floats, there were 1010475 failures in monotonicity.
//            next = a * a * a * (a * (a * 6f - 14.999999f) + 9.999999f);
            //0x1p-24f: Out of 16777216 tested floats, there were 2100806 failures in monotonicity and 6959 violations of the bounds.
            //0x1p-23f: Out of 16777216 tested floats, there were 1157750 failures in monotonicity.
//            next = a * a * a * (a * (a * 5.9999995f - 14.999999f) + 9.999999f);
//            next = a * a * BitConversion.intBitsToFloat(BitConversion.floatToIntBits(a) & -2) * (a * (a * 6 - 15) + 10);
            if(next < old){
                failures++;
//                if(r.next(12) == 0)
//                    System.out.println("Problem at " + Base.BASE10.friendly(a) + ": " + Base.BASE10.friendly(old) + "> " + Base.BASE10.friendly(next));
            }
            if(next > 1f)
                boundFailures++;
            old = next;
        }
        System.out.println("0x1p-24f: Out of " + 0x1000000 + " tested floats, there were " + failures + " failures in monotonicity and " + boundFailures + " violations of the bounds.");
        System.out.println("Ended with 1.0 producing " + next);
    }
}
