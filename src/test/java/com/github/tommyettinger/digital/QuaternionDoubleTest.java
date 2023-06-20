package com.github.tommyettinger.digital;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuaternionDoubleTest {
	private static final float epsilon = 0x1p-15f;

	@Test
	public void testRoundTrip () {
		float yaw = -5f;
		float pitch = 42.00055f;
		float roll = 164.32f;

		QuaternionDouble q = new QuaternionDouble().setEulerAngles(yaw, pitch, roll);

		assertEquals(yaw, q.getYaw(), epsilon);
		assertEquals(pitch, q.getPitch(), epsilon);
		assertEquals(roll, q.getRoll(), epsilon);
	}

	@Test
	public void testMultipleRotations () {
		QuaternionDouble q = new QuaternionDouble();
		QuaternionDouble rot = new QuaternionDouble().setEulerAngles(4f, 0, 0);

		for (int i = 0; i < 90; i++) {
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}

		assertEquals(0f, q.getYaw(), epsilon);
	}
}