package com.github.tommyettinger.digital;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuaternionDoubleTest {
	private static final double epsilon = 0x1p-17f;

	@Test
	public void testRoundTrip () {
		double yaw = -5;
		double pitch = 42.00055;
		double roll = 164.32;

		QuaternionDouble q = new QuaternionDouble().setEulerAngles(yaw, pitch, roll);

		assertEquals(yaw, q.getYaw(), epsilon);
		assertEquals(pitch, q.getPitch(), epsilon);
		assertEquals(roll, q.getRoll(), epsilon);
	}

	@Test
	public void testMultipleRotations () {
		QuaternionDouble q = new QuaternionDouble();
		QuaternionDouble rot = new QuaternionDouble().setEulerAngles(4.0, 0, 0);

		for (int i = 0; i < 90; i++) {
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}

		assertEquals(0.0, q.getYaw(), epsilon);
	}
}