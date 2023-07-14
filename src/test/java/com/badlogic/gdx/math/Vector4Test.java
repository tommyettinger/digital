
package com.badlogic.gdx.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class Vector4Test {
	@Test
	public void testToString () {
		assertEquals("(-5.0,42.00055,44444.32,-1.975)", new Vector4(-5f, 42.00055f, 44444.32f, -1.975f).toString());
	}

	@Test
	public void testFromString () {
		assertEquals(new Vector4(-5f, 42.00055f, 44444.32f, -1.975f), new Vector4().fromString("(-5,42.00055,44444.32,-1.9750)"));
	}

	@Test
	public void testOnLine () {
		Vector4 a = new Vector4(2, 1, 0, 0), b = new Vector4(-1, -0.5f, 0, 0);
		assertTrue(a.isOnLine(b));
		assertTrue(b.isOnLine(a));
		a.w = -Float.MAX_VALUE;
		assertFalse(a.isOnLine(b));
		assertFalse(b.isOnLine(a));
		b.w = Float.MAX_VALUE * 0.5f;
		assertTrue(a.isOnLine(b));
		assertTrue(b.isOnLine(a));
	}
}
