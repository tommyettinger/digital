package com.github.tommyettinger.digital;

import com.badlogic.gdx.math.QuaternionDouble;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuaternionDoubleTest {
	private static final double epsilon = 0x1p-32;

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

		for (int i = 0; i < 9000; i++) {
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}

		assertEquals(0.0, q.getYaw(), epsilon);

		q.idt();
		rot.setEulerAngles(0, 4.0, 0);

		for (int i = 0; i < 9000; i++) {
			q.mul(rot);
			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
		}

		assertEquals(0.0, q.getPitch(), epsilon);

//		q.idt();
//		rot.setEulerAngles(0, 0, 4.0);
//
//		for (int i = 0; i < 900; i++) {
//			q.mul(rot);
//			q.setEulerAngles(q.getYaw(), q.getPitch(), q.getRoll());
//		}
//
//		assertEquals(0.0, q.getRoll(), epsilon);
	}
}