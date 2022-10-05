package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

// REMOVE the @Ignore if you want to run any tests!
@Ignore
public class BaseFPTest {

	public static final List<BaseFP> BASES = Arrays.asList(BaseFP.BASE2, BaseFP.BASE8, BaseFP.BASE10, BaseFP.BASE16,
			BaseFP.BASE36, BaseFP.BASE64, BaseFP.URI_SAFE, BaseFP.SIMPLE64, BaseFP.BASE86,
			BaseFP.scrambledBase(new Random(123L)), BaseFP.scrambledBase(new Random(12345678890L)),
			BaseFP.BASE10.scramble(new Random(-12345L)), BaseFP.BASE36.scramble(new Random(-1234567L)));

	@Test
	public void testReadDouble(){
		double[] inputs = {0.0, -0.0, 1.0, -1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, 1.5, -1.1};

		double[][] inputs2D = {
			{0.0, -0.0, 1.0, -1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, 1.5, -1.1},
			{1.0, -1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, 1.5, -1.1},
			{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL, 1.5, -1.1},
		};
		for(double in : inputs) {
			System.out.println("TCE 0x" + BaseFP.BASE16.signed(in));
			System.out.println("JDK " + Double.toHexString(in));
		}
		for(BaseFP enc : BASES)
//		BaseFP enc = BaseFP.BASE64;
		{
			for(double in : inputs){
				System.out.println(BaseFP.BASE16.unsigned(in) + " is " + in + ", in base " + enc.base + ", " + enc.signed(in)
						+ ", in JDK hex " + Double.toHexString(in));
				Assert.assertEquals(in, enc.readDouble(enc.signed(in)), 0.0);
				Assert.assertEquals(in, enc.readDouble(enc.unsigned(in)), 0.0);
			}
//			Assert.assertTrue(Double.isNaN(enc.readDouble(enc.signed(Double.NaN))));
//			Assert.assertTrue(Double.isNaN(enc.readDouble(enc.unsigned(Double.NaN))));
//			Assert.assertArrayEquals(enc.doubleSplit(enc.join(" ", inputs), " "), inputs, 0.00001);
//			Assert.assertArrayEquals(enc.doubleSplit(" " + enc.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs, 0.00001);
//			Assert.assertArrayEquals(enc.doubleSplit2D(enc.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
//			Assert.assertArrayEquals(enc.doubleSplit2D(" " + enc.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);
		}
	}

	@Test
	public void testReadFloat(){
		float[] inputs = {0.0f, -0.0f, 1.0f, -1.0f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
			Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, 1.5f, -1.1f};

		float[][] inputs2D = {{0.0f, -0.0f, 1.0f, -1.0f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
			Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, 1.5f, -1.1f},
			{1.0f, -1.0f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
				Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, 1.5f, -1.1f},
			{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
				Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL, 1.5f, -1.1f},};

		for(BaseFP enc : BASES)
		{
			for(float in : inputs){
				Assert.assertEquals(in, enc.readFloat(enc.signed(in)), Float.MIN_VALUE);
				Assert.assertEquals(in, enc.readFloat(enc.unsigned(in)), Float.MIN_VALUE);
			}
			Assert.assertTrue(Float.isNaN(enc.readFloat(enc.signed(Float.NaN))));
			Assert.assertTrue(Float.isNaN(enc.readFloat(enc.unsigned(Float.NaN))));
			Assert.assertArrayEquals(enc.floatSplit(enc.join(" ", inputs), " "), inputs, 0.00001f);
			Assert.assertArrayEquals(enc.floatSplit(" " + enc.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs, 0.00001f);
			Assert.assertArrayEquals(enc.floatSplit2D(enc.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
			Assert.assertArrayEquals(enc.floatSplit2D(" " + enc.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);
		}
	}

	public static void main(String[] args){
		for(BaseFP b : BASES){
			System.out.println(b.serializeToString());
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			for(BaseFP b : BASES){
				System.out.print(b.unsigned(i));
				System.out.print("    ");
			}
			System.out.println();
		}
		System.out.println();
		for(BaseFP b : BASES){
			System.out.println(b.base + ": " + b.signed(4.89684f));
		}
	}
}
