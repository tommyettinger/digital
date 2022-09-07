package com.github.tommyettinger.digital;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BaseTest {

	public static final List<Base> BASES = Arrays.asList(Base.BASE2, Base.BASE8, Base.BASE10, Base.BASE16,
			Base.BASE36, Base.BASE64, Base.URI_SAFE, Base.SIMPLE64, Base.BASE86,
			Base.scrambledBase(new Random(123L)), Base.scrambledBase(new Random(12345678890L)),
			Base.BASE10.scramble(new Random(-12345L)), Base.BASE36.scramble(new Random(-1234567L)));
	@Test
	public void testUnsignedInt() {
		int[] inputs = {0x00000000, 0x00000001, 0xFFFFFFFF, 0x7FFFFFFF, 0x80000000, 0x12345678, 0x89ABCDEF};
		for (int i : inputs) {
			Assert.assertEquals(String.format("%08X", i), Base.BASE16.unsigned(i));
		}
		for (int i : inputs) {
			Assert.assertEquals(String.format("%011o", i), Base.BASE8.unsigned(i));
		}
		StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
		for (int i : inputs) {
			Assert.assertEquals(sb.append(String.format("%08X", i)).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
			Assert.assertEquals(sb.append(String.format("%011o", i)).toString(), Base.BASE8.appendUnsigned(esb, i).toString());
		}
	}

	@Test
	public void testUnsignedLong() {
		long[] inputs = {0x00000000L, 0x00000001L, 0xFFFFFFFFL, 0x7FFFFFFFL,  0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
			0x80000000L,  0x8000000000000000L, 0x12345678L, 0x89ABCDEFL, 0x1234567890ABCDEFL, 0xFEDCBA0987654321L};
		for (long i : inputs) {
			Assert.assertEquals(String.format("%016X", i), Base.BASE16.unsigned(i));
		}
		for (long i : inputs) {
			Assert.assertEquals(String.format("%022o", i), Base.BASE8.unsigned(i));
		}
		StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
		for (long i : inputs) {
			Assert.assertEquals(sb.append(String.format("%016X", i)).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
			Assert.assertEquals(sb.append(String.format("%022o", i)).toString(), Base.BASE8.appendUnsigned(esb, i).toString());
		}
	}

	@Test
	public void testUnsignedShort() {
		short[] inputs = new short[]{0x0000, 0x0001, (short)0xFFFF, 0x7FFF,
			(short)0x8000, 0x1234, (short)0x89AB, (short)0xCDEF, (short)0x8765};
		for (short i : inputs) {
			Assert.assertEquals(String.format("%04X", i), Base.BASE16.unsigned(i));
		}
		for (short i : inputs) {
			Assert.assertEquals(String.format("%06o", i), Base.BASE8.unsigned(i));
		}
		StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
		for (short i : inputs) {
			Assert.assertEquals(sb.append(String.format("%04X", i)).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
			Assert.assertEquals(sb.append(String.format("%06o", i)).toString(), Base.BASE8.appendUnsigned(esb, i).toString());
		}
	}

	@Test
	public void testUnsignedChar() {
		char[] inputs = new char[]{0x0000, 0x0001, (char)0xFFFF, 0x7FFF,
			(char)0x8000, 0x1234, (char)0x89AB, (char)0xCDEF, (char)0x8765};
		for (char i : inputs) {
			// 0| is used to convert char to int
			Assert.assertEquals(String.format("%04X", 0|i), Base.BASE16.unsigned(i));
		}
		for (char i : inputs) {
			Assert.assertEquals(String.format("%06o", 0|i), Base.BASE8.unsigned(i));
		}
		StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
		for (char i : inputs) {
			Assert.assertEquals(sb.append(String.format("%04X", 0|i)).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
			Assert.assertEquals(sb.append(String.format("%06o", 0|i)).toString(), Base.BASE8.appendUnsigned(esb, i).toString());
		}
	}

	@Test
	public void testUnsignedByte() {
		byte[] inputs = new byte[]{0x00, 0x01, (byte)0xFF, 0x7F,
			(byte)0x80, 0x12, (byte)0x89, (byte)0xCD, (byte)0x65};
		for (byte i : inputs) {
			Assert.assertEquals(String.format("%02X", i), Base.BASE16.unsigned(i));
		}
		for (byte i : inputs) {
			Assert.assertEquals(String.format("%03o", i), Base.BASE8.unsigned(i));
		}
		StringBuilder sb = new StringBuilder("0x"), esb = new StringBuilder("0x");
		for (byte i : inputs) {
			Assert.assertEquals(sb.append(String.format("%02X", i)).toString(), Base.BASE16.appendUnsigned(esb, i).toString());
			Assert.assertEquals(sb.append(String.format("%03o", i)).toString(), Base.BASE8.appendUnsigned(esb, i).toString());
		}
	}

	@Test
	public void testSignedLong() {
		long[] inputs = {0L, 1L, -1L, 9223372036854775807L, -9223372036854775808L, 2147483647L, -2147483648L, 1234L, -98765L};
		for (long i : inputs) {
			Assert.assertTrue(Long.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
		}
		for (long i : inputs) {
			Assert.assertTrue(Long.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
		}
		StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
		for (long i : inputs) {
			Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
		}
		for (long i : inputs) {
			Assert.assertEquals(sb.append(Long.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
		}
		long[][] inputs2D = {
			{0L, 1L, -1L, 9223372036854775807L, -9223372036854775808L, 2147483647L, -2147483648L, 1234L, -98765L,},
			{1L, -1L, 9223372036854775807L, -9223372036854775808L, 2147483647L, -2147483648L, 1234L, -98765L,},
			{-1L, 9223372036854775807L, -9223372036854775808L, 2147483647L, -2147483648L, 1234L, -98765L,},
		};
		for(Base b : BASES){
			Assert.assertArrayEquals(b.longSplit(b.join(" ", inputs), " "), inputs);
			Assert.assertArrayEquals(b.longSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
			Assert.assertArrayEquals(b.longSplit2D(b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
			Assert.assertArrayEquals(b.longSplit2D(" " + b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);

			if(b == Base.BASE16 || b == Base.BASE36)
			{
				Assert.assertEquals(Long.parseLong("aaa", b.base), b.readLong("aaa"));
				Assert.assertEquals(Long.parseLong("AAA", b.base), b.readLong("AAA"));
				Assert.assertEquals(Long.parseLong("Aaa", b.base), b.readLong("Aaa"));
			}
		}
	}

	@Test
	public void testSignedInt() {
		int[] inputs = {0, 1, -1, 2147483647, -2147483648, 1234, -98765};
		for (int i : inputs) {
			Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
		}
		for (int i : inputs) {
			Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
		}
		StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
		for (int i : inputs) {
			Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
		}
		for (int i : inputs) {
			Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
		}
		int[][] inputs2D = {
			{0, 1, -1, 2147483647, -2147483648, 1234, -98765},
			{1, -1, 2147483647, -2147483648, 1234, -98765},
			{-1, 2147483647, -2147483648, 1234, -98765},
		};

		for(Base b : BASES){
			Assert.assertArrayEquals(b.intSplit(b.join(" ", inputs), " "), inputs);
			Assert.assertArrayEquals(b.intSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
			Assert.assertArrayEquals(b.intSplit2D(b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
			Assert.assertArrayEquals(b.intSplit2D(" " + b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);
		}
	}

	@Test
	public void testSignedShort() {
		short[] inputs = {0, 1, -1, 32767, -32768, 1234, -9876};
		for (short i : inputs) {
			Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
		}
		for (short i : inputs) {
			Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
		}
		StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
		for (short i : inputs) {
			Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
		}
		for (short i : inputs) {
			Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
		}
		short[][] inputs2D = {
			{0, 1, -1, 32767, -32768, 1234, -9876},
			{1, -1, 32767, -32768, 1234, -9876},
			{-1, 32767, -32768, 1234, -9876},
		};
		for(Base b : BASES){
			Assert.assertArrayEquals(b.shortSplit(b.join(" ", inputs), " "), inputs);
			Assert.assertArrayEquals(b.shortSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
			Assert.assertArrayEquals(b.shortSplit2D(b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
			Assert.assertArrayEquals(b.shortSplit2D(" " + b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);
		}
	}

	@Test
	public void testSignedChar() {
		char[] inputs = {0, 1, 0xFFFF, 32767, 0x8000, 1234, 49876};
		for (char i : inputs) {
			Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
		}
		for (char i : inputs) {
			Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
		}
		StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
		for (char i : inputs) {
			Assert.assertEquals(sb.append((int)i).toString(), Base.BASE10.appendSigned(esb, i).toString());
		}
		for (char i : inputs) {
			Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
		}
		char[][] inputs2D = {
			{0, 1, 0xFFFF, 32767, 0x8000, 1234, 49876},
			{1, 0xFFFF, 32767, 0x8000, 1234, 49876},
			{0xFFFF, 32767, 0x8000, 1234, 49876},
		};
		for(Base b : BASES){
			Assert.assertArrayEquals(b.charSplit(b.join(" ", inputs), " "), inputs);
			Assert.assertArrayEquals(b.charSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
			Assert.assertArrayEquals(b.charSplit2D(b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
			Assert.assertArrayEquals(b.charSplit2D(" " + b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);
		}
	}

	@Test
	public void testSignedByte() {
		byte[] inputs = {0, 1, -1, 127, -128, 12, -87};
		for (byte i : inputs) {
			Assert.assertTrue(Integer.toString(i).equalsIgnoreCase(Base.BASE10.signed(i)));
		}
		for (byte i : inputs) {
			Assert.assertTrue(Integer.toString(i, 36).equalsIgnoreCase(Base.BASE36.signed(i)));
		}
		StringBuilder sb = new StringBuilder(), esb = new StringBuilder();
		for (byte i : inputs) {
			Assert.assertEquals(sb.append(i).toString(), Base.BASE10.appendSigned(esb, i).toString());
		}
		for (byte i : inputs) {
			Assert.assertEquals(sb.append(Integer.toString(i, 2)).toString(), Base.BASE2.appendSigned(esb, i).toString());
		}
		byte[][] inputs2D = {
			{0, 1, -1, 127, -128, 12, -87},
			{1, -1, 127, -128, 12, -87},
			{-1, 127, -128, 12, -87},
		};
		for(Base b : BASES){
			Assert.assertArrayEquals(b.byteSplit(b.join(" ", inputs), " "), inputs);
			Assert.assertArrayEquals(b.byteSplit(" " + b.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs);
			Assert.assertArrayEquals(b.byteSplit2D(b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
			Assert.assertArrayEquals(b.byteSplit2D(" " + b.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);
		}
	}

	@Test
	public void testReadLong(){
		long[] inputs = {0x00000000L, 0x00000001L, 0xFFFFFFFFL, 0x7FFFFFFFL,
			0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
			0x80000000L,  0x8000000000000000L, 0x12345678L, 0x89ABCDEFL, 0x1234567890ABCDEFL, 0xFEDCBA0987654321L};

		for(Base enc : BASES)
		{
			for(long in : inputs){
				Assert.assertEquals(in, enc.readLong(enc.signed(in)));
				Assert.assertEquals(in, enc.readLong(enc.unsigned(in)));
			}
		}
	}

	@Test
	public void testReadInt(){
		int[] inputs = {0, 1, -1, 2147483647, -2147483647, -2147483648, 1234, -98765};

		for(Base enc : BASES)
		{
			for(int in : inputs){
				Assert.assertEquals(in, enc.readInt(enc.signed(in)));
				Assert.assertEquals(in, enc.readInt(enc.unsigned(in)));
			}
		}
	}

	@Test
	public void testReadShort(){
		short[] inputs = {0, 1, -1, 32767, -32768, 1234, -9876};

		for(Base enc : BASES)
		{
			for(short in : inputs){
				Assert.assertEquals(in, enc.readShort(enc.signed(in)));
				Assert.assertEquals(in, enc.readShort(enc.unsigned(in)));
			}
		}
	}

	@Test
	public void testReadChar(){
		char[] inputs = {0, 1, 0xFFFF, 32767, 0x8000, 1234, 49876};

		for(Base enc : BASES)
		{
			for(char in : inputs){
				Assert.assertEquals(in, enc.readChar(enc.signed(in)));
				Assert.assertEquals(in, enc.readChar(enc.unsigned(in)));
			}
		}
	}

	@Test
	public void testReadByte(){
		byte[] inputs = {0, 1, -1, 127, -128, 12, -87};

		for(Base enc : BASES)
		{
			for(byte in : inputs){
				Assert.assertEquals(in, enc.readByte(enc.signed(in)));
				Assert.assertEquals(in, enc.readByte(enc.unsigned(in)));
			}
		}
	}

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

		for(Base enc : BASES)
		{
			for(double in : inputs){
				Assert.assertEquals(in, enc.readDouble(enc.signed(in)), Double.MIN_VALUE);
				Assert.assertEquals(in, enc.readDouble(enc.unsigned(in)), Double.MIN_VALUE);
			}
			Assert.assertTrue(Double.isNaN(enc.readDouble(enc.signed(Double.NaN))));
			Assert.assertTrue(Double.isNaN(enc.readDouble(enc.unsigned(Double.NaN))));
			Assert.assertArrayEquals(enc.doubleSplit(enc.join(" ", inputs), " "), inputs, 0.00001);
			Assert.assertArrayEquals(enc.doubleSplit(" " + enc.join(" ", inputs), " ", 1, Integer.MAX_VALUE), inputs, 0.00001);
			Assert.assertArrayEquals(enc.doubleSplit2D(enc.appendJoined2D(new StringBuilder(), "`", ",", inputs2D).toString(), "`", ","), inputs2D);
			Assert.assertArrayEquals(enc.doubleSplit2D(" " + enc.appendJoined2D(new StringBuilder(), "`", ",", inputs2D), "`", ",", 1, Integer.MAX_VALUE), inputs2D);
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

		for(Base enc : BASES)
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

	@Test
	public void testScramble() {
		Random random = new Random(); // unseeded, expected to have different results between runs, but always pass.
		for (int i = 0; i < 1024; i++) {
			Base base = Base.scrambledBase(random);
			System.out.println(base.negativeSign + "  :  " + base.serializeToString());
			for (int j = 0; j < 512; j++) {
				long n = random.nextLong();
				String signed = base.signed(n);
				if(signed.charAt(0) == base.negativeSign && signed.indexOf(base.negativeSign, 1) >= 0)
					System.out.println("has both: " + signed);
				Assert.assertEquals(n, base.readLong(signed));
			}
		}
	}

	public static void main(String[] args){
		for(Base b : BASES){
			System.out.println(b.serializeToString());
		}
		System.out.println();
		for (int i = 0; i < 100; i++) {
			for(Base b : BASES){
				System.out.print(b.unsigned(i));
				System.out.print("    ");
			}
			System.out.println();
		}
		System.out.println();
		for(Base b : BASES){
			System.out.println(b.base + ": " + b.signed(4.89684f));
		}
	}

	@Test
	public void testTimePrint() {
		Random r = new Random(123);
		String low, high;
		for (int i = 0; i < 100000; i++) {
			float time = r.nextFloat() * 6000f;
			low = formatTimeMMSSF(time);
			high = formatterTimeMMSSF(time);
			if(!low.equals(high))
				System.out.println("Format: " + high + "\nCustom: " + low);
			Assert.assertEquals(low, high);
		}

	}
	public static final char[] DIGITS = "0123456789".toCharArray();
	public static final char[] chars = {'0', '0', ':', '0', '0', '.', '0'};

	/**
	 * Given a time in seconds, formats a String to store a 2-digit minute section and a 2-digit second section, with 1
	 * digit for tenths-of-a-second. Does not allocate except for the String it returns.
	 * @param time a float time in seconds; should be less than 6000 (which would require too many digits for minutes)
	 * @return a 7-character String storing the formatted time
	 */
	public static String formatTimeMMSSF(float time) {
		if(time < 0f || time >= 6000f) return "--:----";
		float div = time / 60f, mod = (time % 60f) * 10f;
		int mm = (int) Math.floor(div), ss = Math.round(mod);
		chars[0] = '0';
		chars[2] = ':';
		chars[3] = '0';
		chars[4] = '0';
		chars[5] = '.';
		chars[1] = DIGITS[mm % 10];
		if ((mm /= 10) != 0)
			chars[0] = DIGITS[mm];
		chars[6] = DIGITS[ss % 10];
		if ((ss /= 10) != 0)
		{
			chars[4] = DIGITS[ss % 10];
			if((ss /= 10) != 0)
				chars[3] = DIGITS[ss];
		}
		return new String(chars);
	}

	/**
	 * Just like {@link #formatTimeMMSSF(float)}, but uses {@link String#format(String, Object...)}, and allocates quite
	 * a lot more.
	 * @param time a float time in seconds; should be less than 6000 (which would require too many digits for minutes)
	 * @return a 7-character String storing the formatted time
	 */
	public static String formatterTimeMMSSF(float time) {
		if (time < 0f || time >= 6000f) return "--:----";
		return String.format("%02d:%04.1f", (int)Math.floor(time / 60f), time % 60f);
	}

//	fun formatTimeMMSSF(time: Float): String {
//		if (time == Float.MAX_VALUE) return "--:--"
//		return "%02d:%04.1f".format(floor(time / 60).toInt(), time % 60f)
//	}
}
